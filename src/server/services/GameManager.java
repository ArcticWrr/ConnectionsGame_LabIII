package server.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import server.model.ServerGameState;
import server.model.User;
import server.model.UserGameState;
import server.network.NotificationServiceUDP;
import server.model.Game;
import shared.GameMessage;
import server.model.WordGroup;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class GameManager {
    private final List<Game> allGames; //caricati dal JSON
    private volatile Game currentGame;
    private final GameTimer gameTimer;
    private final NotificationServiceUDP notificationService;
    private final UserManager userManager;
    private final ServerGameState serverGameState;
    private final String gameFilePath = "data/games.json";

    public GameManager(List<Game> games, long durationMinutes, UserManager userManager,  NotificationServiceUDP notificationServiceUDP) {

        this.serverGameState = new ServerGameState(this);
        this.allGames = games;
        this.userManager = userManager;
        this.notificationService = notificationServiceUDP ;
        this.gameTimer = new GameTimer(durationMinutes, this::rotateGame); //this::rotateGame rimpiazza () -> this.rotateGame()

        boolean recovered = loadMatchesFromFile();

        if (!recovered) {
            //se non c'era un file di stato
            System.out.println("Nessuno stato precedente trovato. Avvio nuova sessione.");
            startNewGame();
        } else {
            // Se loadMatchesFromFile ha ripristinato currentGame e gameHistory:
            System.out.println("Stato ripristinato. Ripresa della partita in corso.");
        }
        gameTimer.resetStartTime();
        gameTimer.start();
    }
    // Avvia la prossima partita globale
    public synchronized void startNewGame() {
        this.currentGame = allGames.get(new Random().nextInt(allGames.size()));
        System.out.println("Nuova partita globale avviata! ID: " + currentGame.gameId);
    }

    public synchronized void rotateGame() {
        System.out.println("--- INIZIO ROTAZIONE PARTITA ---");

        //Finalizza partite di tutti i giocatori ancora attivi
        Map<String, UserGameState> currentPlayers = serverGameState.getGameHistory().get(currentGame.gameId);
        if (currentPlayers != null) {
            for (UserGameState state : currentPlayers.values()) {
                    finalizeMatch(state.getUsername(), state);
            }
        }
        //Scegliamo una nuova partita e resettiamo timer
        startNewGame();
        if (this.gameTimer != null) {
            this.gameTimer.resetStartTime();
        }
        //salvo il nuovo stato
        saveMatchesToFile();

        //notifica asincrona UDP
        // Creiamo il messaggio da inviare via UDP
        GameMessage notification = new GameMessage();
        notification.operation = "NEW_GAME_EVENT";
        notification.gameId = currentGame.getGameId();
        notification.words = currentGame.getShuffledWords();

        notificationService.broadcast(notification);

        System.out.println("--- ROTAZIONE COMPLETATA: Nuovo ID " + currentGame.gameId + " ---");
    }

     //Verifica se la proposta è malformata (contiene parole non in gioco o già usate correttamente)
    public boolean isProposalValid(List<String> words, UserGameState state) {
        if (words == null || words.size() != 4) return false;

        List<String> allWords = currentGame.getAllWords(); // Parole totali della partita attuale
        List<String> alreadyCorrect = state.getFoundWords(); // Parole già raggruppate dall'utente

        for (String word : words) {
            // La parola deve far parte della partita globale
            if (!allWords.contains(word) || alreadyCorrect.contains(word)) {
                //System.out.println(word);
                return false;
            }
        }
        return true;
    }

    //elabora la proposta e restituisce l'esito come stringa
    public synchronized String processSubmission( List<String> words, String username) {
        UserGameState state = serverGameState.getPlayerState(username, currentGame.gameId);
        words.replaceAll(s -> s.trim().toUpperCase());

        if (!isProposalValid(words, state)) {
            return "MALFORMED";
        }
        //Chiediamo alla classe Game se le parole sono un gruppo
        WordGroup wordGroupFound = currentGame.checkWordsMatch(words);
        if (wordGroupFound != null) {
            // aggiorna stato
            state.addFoundGroup(wordGroupFound);
            if (state.isFinished()) {
                finalizeMatch(username, state); //aggiorno statistiche utenti
            }
            return wordGroupFound.getTheme();
        } else {
            //  aggiunge un errore
            state.addMistake();
            if (state.isFinished()) {
                finalizeMatch(username, state); //aggiorno statistiche utenti
            }
            return null;
        }
    }

    public GameMessage getGameInfo(String username, int reqId) {
        GameMessage response = new GameMessage();
        response.operation = "GET_GAME_STATUS_RESPONSE";

        int targetId = (reqId == -1) ? currentGame.getGameId() : reqId;
        response.gameId = targetId;

        UserGameState state = serverGameState.getPlayerState(username, targetId);

        if (state == null) {
            response.success = false;
            response.message = "Nessun dato trovato per l'utente" + username + " nella partita " + targetId;
            return response;
        }

        //Verifichiamo se la partita richiesta è quella attiva
        boolean isCurrent = (reqId == -1 || reqId == currentGame.getGameId());

        //Controllo se per l'utente è FINITA (per tempo, vittoria o sconfitta)
        if (!isCurrent || gameTimer.isExpired() || state.isFinished()) {
            // --- CASO: PARTITA CONCLUSA ---
            response.gameState = "CONCLUDED";
            response.mistakes = state.getMistakes();
            response.currentScore = state.calculateCurrentScore();
            response.fullSolution = currentGame.getGroups();
        } else {
            // --- CASO: PARTITA IN CORSO ---
            response.gameState = "IN_PROGRESS";
            response.remainingTime = gameTimer.getRemainingSeconds();
            response.mistakes = state.getMistakes();
            response.currentScore = state.calculateCurrentScore();
            response.correctProposals = state.getFoundWords();

            //parole ancora da raggruppare
            List<String> remaining = new ArrayList<>(currentGame.getShuffledWords());
            remaining.removeAll(state.getFoundWords());
            response.wordsRemaining = remaining;
        }
        response.success = true;
        return response;
    }

    public GameMessage getGameStats(int reqId) {
        GameMessage response = new GameMessage();
        response.operation = "GET_GAME_STATS_RESPONSE";

        int targetId = (reqId == -1) ? currentGame.getGameId() : reqId;
        response.gameId = targetId;

        // Recupera tutti gli stati dei giocatori per quella partita
         Map<String, UserGameState> states = serverGameState.getAllStatesForGame(targetId);

        if (states == null || states.isEmpty()) {
            response.success = false;
            response.message = "Nessun dato trovato per la partita " + targetId;
            return response;
        }

        int playing = 0;
        int finished = 0;
        int winners = 0;
        double totalScore = 0;

        for (UserGameState s : states.values()) {
            if (s.isFinished()) {
                finished++;
                if (s.isWon()) winners++;
            } else {
                playing++;
            }
            totalScore += s.calculateCurrentScore();
        }

        //Distinzione In Corso vs Conclusa

        if (targetId == currentGame.getGameId() && !gameTimer.isExpired()) {
            response.gameState = "IN_PROGRESS";
            response.remainingTime = gameTimer.getRemainingSeconds();
            response.playersPlaying = playing;
            response.playersFinished = finished;
            response.playersWon = winners;
        } else {
            response.gameState = "CONCLUDED";
            response.totalParticipants = states.size();
            response.playersFinished = finished; // Coincide con i partecipanti se conclusa
            response.playersWon = winners;
            // Calcolo punteggio medio
            response.averageScore = states.isEmpty() ? 0.0 : (totalScore / states.size());
        }

        response.success = true;
        return response;
    }

    private synchronized void finalizeMatch(String username, UserGameState state) {
        // Evitiamo di salvare due volte se il metodo viene chiamato più volte
        if (state.isAlreadySaved()) return;

        // Recuperiamo l'oggetto User dallo UserManager
        User user = userManager.getUser(username);
        if (user == null) return;

        if (!state.isFinished()) {
            state.finishByTimeout(); // Imposta isWon = false e isFinished = true
            user.incrementGamesNotFinished();
        }

        if (state.getMistakes() == 0 && state.isWon()){
            user.incrementPerfectGames();
        } else if (state.getMistakes() == 1 && state.isWon()){
            user.incrementOneMistakeGames();
        } else if (state.getMistakes() == 2  && state.isWon()){
            user.incrementTwoMistakeGames();
        } else if (state.getMistakes() == 3 && state.isWon()){
            user.incrementThreeMistakeGames();
        } else if (state.getMistakes() == 4 && state.isWon()){
            user.incrementFourMistakeGames();
        }
        // Calcolo del punteggio finale della partita secondo la formula
        int matchScore = state.calculateCurrentScore();

        // Aggiorniamo la carriera dell'utente
        user.setLastScore(matchScore); // Aggiunge a ScoreHistory e salva miglior punteggio
        user.incrementGamesPlayed(); //Incrementa partite giocate

        if (state.isWon()) {
            user.incrementCurrentStreak();
            user.incrementGamesWon();   // Incrementa il contatore vittorie se ha vinto
        } else {
            user.setCurrentStreak(0);
        }
        if (state.isLost()){
            user.incrementGameLost(); //Incrementa contatore sconfitte se hai perso
        }

        //  Segniamo lo stato come salvato e aggiorniamo i file JSON
        state.setAlreadySaved(true);
        userManager.saveUsers();
        saveMatchesToFile();

        System.out.println("DEBUG: Partita salvata per " + username + ". Punti: " + matchScore);
    }

    public synchronized void saveMatchesToFile() {
        try (Writer writer = new FileWriter(gameFilePath)) {
            serverGameState.setCurrentGameId(this.currentGame.getGameId());
            serverGameState.setRemainingTimeMillis(this.gameTimer.getRemainingSeconds());
            //state.gameHistory = this.gameHistory;

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(serverGameState, writer);
            System.out.println("Persistenza partite completata.");
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio partite: " + e.getMessage());
        }
    }

    public boolean loadMatchesFromFile() {
        File file = new File(gameFilePath);

        if (!file.exists()) {
            return false;
        }

        try (Reader reader = new FileReader(file)) {
            ServerGameState savedState = new Gson().fromJson(reader, ServerGameState.class);

            if (savedState == null) return false;

            //Cerchiamo il gioco nel database tramite l'ID salvato
            Game recoveredGame = findGameById(savedState.getCurrentGameId());
            if (recoveredGame == null) {
                return false;
            }

            // Ripristiniamo i dati in memoria
            this.currentGame = recoveredGame;
            serverGameState.getGameHistory().clear();
            serverGameState.getGameHistory().putAll(savedState.getGameHistory());

            System.out.println("Stato caricato con successo: Partita #" + recoveredGame.getGameId());
            return true;

        } catch (IOException | JsonSyntaxException e) {
            System.err.println("Errore durante il caricamento del file di stato: " + e.getMessage());
            return false;
        }
    }

    private Game findGameById(int gameId) {
        for (Game game : allGames) {
            if (game.getGameId() == gameId) {
                return game;
            }
        }
        return null;
    }

    public Game getCurrentGame() {return currentGame;}
    public GameTimer getGameTimer() {return gameTimer;}
    public NotificationServiceUDP getNotificationService() {return notificationService;}
    public long getTimeRemainingSeconds() {return gameTimer.getRemainingSeconds();}
    public ServerGameState getServerGameState() {return serverGameState;}

}
