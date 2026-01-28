package server.network;

import com.google.gson.Gson;

import server.model.PlayerRank;
import server.model.User;
import server.model.UserGameState;
import server.services.GameManager;
import server.services.UserManager;
import server.model.Game;
import shared.GameMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final GameManager gameManager;
    private final UserManager userManager;
    private final NotificationServiceUDP notificationServiceUDP;
    private final String ipClient;
    private User loggedUser = null; // Memorizza l'utente loggato in questa sessione
    Gson gson = new Gson();

    public ClientHandler(Socket socket, GameManager gameManager, UserManager userManager,  NotificationServiceUDP notificationServiceUDP) {
        this.clientSocket = socket;
        this.gameManager = gameManager;
        this.userManager = userManager;
        this.notificationServiceUDP = notificationServiceUDP;
        this.ipClient = socket.getInetAddress().getHostAddress();
    }

    @Override
    public void run() {
        try (clientSocket;
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

            String inputLine;
            while ((inputLine = in.readLine()) != null) {  //ricevi messaggio
                try {

                    GameMessage request = gson.fromJson(inputLine, GameMessage.class);

                    //Gestisce messaggio
                    GameMessage response = handleRequest(request);

                    String responseJson = gson.toJson(response);
                    out.println(responseJson); //Invia risposta a client

                    UserGameState currentState = gameManager.getServerGameState().getPlayerState(this.loggedUser.getUsername(), gameManager.getCurrentGame().getGameId());

                    if (currentState != null && currentState.isWon()) {
                        //notifica asincrona UDP per vittoria
                        GameMessage notification = new GameMessage();
                        notification.operation = "GAME_WON";
                        notification.gameId = gameManager.getCurrentGame().getGameId();
                        notification.message = "Partita vinta! aspetta avvio di una nuova partita";
                        notification.remainingTime = gameManager.getGameTimer().getRemainingSeconds();
                        notification.rankingList = userManager.getGlobalRanking();
                        gameManager.getNotificationService().send(notification, this.loggedUser.getUsername());
                    }

                    if (currentState != null && currentState.isLost()) {
                        //notifica asincrona UDP per vittoria
                        GameMessage notification = new GameMessage();
                        notification.operation = "GAME_LOST";
                        notification.gameId = gameManager.getCurrentGame().getGameId();
                        notification.message = "Partita persa! aspetta avvio di una nuova partita";
                        notification.remainingTime = gameManager.getGameTimer().getRemainingSeconds();
                        notification.rankingList = userManager.getGlobalRanking();
                        gameManager.getNotificationService().send(notification, this.loggedUser.getUsername());
                    }
                } catch (Exception e) {
                    // L'errore viene catturato QUI, il ciclo WHILE continua!
                    System.err.println("Errore nell'elaborazione della richiesta: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Errore di comunicazione: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("ERRORE FATALE NEL THREAD: ");
            e.printStackTrace();
        }
    }

    private GameMessage handleRequest(GameMessage request) {
        GameMessage response = new GameMessage();

        //gestisce messaggi malformati
        if (request == null || request.operation == null) {
            System.err.println("Server: Ricevuto messaggio malformato!");
            response.operation = "ERROR";
            response.success = false;
            response.message = "Campo 'operation' mancante nel JSON.";
            return response;
        }

        String operation = request.operation.toUpperCase();
        switch (operation) {

            case "LOGIN":

                response = loginResponse(request.username, request.psw, request.clientPortUDP);
                break;

            case "REGISTER":
                response = registerResponse(request.username, request.psw);
                break;

            case "UPDATECREDENTIALS":
                response = updateCredentialsResponse(request.username, request.newName, request.psw, request.newPsw);
                break;

            case "LOGOUT":
                response = logoutResponse();
                break;

            case "SUBMITPROPOSAL":
                if (this.loggedUser == null) {
                    response.success = false;
                    response.message = "Effettua il login per inviare proposte";
                    response.operation ="SUBMIT_PROPOSAL_RESPONSE";
                } else {
                    response = submitProposalResponse(request.words);
                }
                break;

            case "REQUESTGAMEINFO":
                if (this.loggedUser == null) {
                    response.success = false;
                    response.message = "Effettua il login per vedere le informazioni della partita";
                    response.operation ="REQUEST_GAMEINFO_RESPONSE";
                } else {
                    int idToSearch = (request.gameId == null) ? -1 : request.gameId;
                    response = gameManager.getGameInfo(this.loggedUser.getUsername(), idToSearch);
                }
                break;

            case "REQUESTGAMESTATS":
                if (this.loggedUser == null) {
                    response.success = false;
                    response.message = "Effettua il login per vedere le statistiche.";
                    response.operation = "REQUEST_GAMESTATS_RESPONSE";
                } else {
                    // Se il client non manda gameId, usiamo -1 per quella attuale
                    int idToSearch = (request.gameId == null) ? -1 : request.gameId;
                    response = gameManager.getGameStats(idToSearch);
                }
                break;

            case "REQUESTLEADERBOARD":
                if (this.loggedUser == null) {
                    response.success = false;
                    response.message = "Effettua il login per vedere la classifica.";
                    response.operation = "REQUEST_LEADERBOARD_RESPONSE";
                } else {
                    response = getLeaderboardResponse(request.playerName, request.topPlayers);
                }
                break;

            case "REQUESTPLAYERSTATS":
                if (this.loggedUser == null) {
                    response.success = false;
                    response.message = "Effettua il login per vedere la classifica.";
                    response.operation = "PLAYER_STATS_RESPONSE";
                } else {
                    response = requestPlayerStatsResponse();
                }
                break;

            default:
                response.success = false;
                response.message = "Operazione non valida.";
                response.operation = "INVALIDOPERATION";
                }

        return response;
    }

    public GameMessage loginResponse(String username, String password, Integer portUDP) {
        GameMessage response = new GameMessage();

        response.operation = "LOGIN_RESPONSE";
        User user = userManager.login(username, password);

        if (user != null) {
            // Successo: salviamo l'utente nel thread corrente
            loggedUser = user;
            response.success = true;
            response.message = "Bentornato, " + user.getUsername() + "!";

            // Informazioni Globali della Partita
            Game currentGame = gameManager.getCurrentGame();
            response.words = gameManager.getCurrentGame().getShuffledWords(); // Set di 16 parole casuali
            response.remainingTime = gameManager.getTimeRemainingSeconds(); // Tempo mancante
            response.gameId = currentGame.getGameId();

            //Informazioni Specifiche dell'Utente (stato personale dell'utente)
            // Recuperiamo i progressi fatti dall'utente in questa partita e associamo porta UDP
            UserGameState userState = gameManager.getServerGameState().getPlayerState(user.getUsername(), currentGame.gameId);
            notificationServiceUDP.registerClient(username, ipClient, portUDP); //registra porta UDP
            response.correctProposals = userState.getFoundWords(); // Parole già indovinate
            response.mistakes = userState.getMistakes(); // Numero di errori (0-4)
            response.currentScore = userState.calculateCurrentScore(); // Punteggio attuale

            System.out.println("Server: Login effettuato per " + user.getUsername());
        } else {
            // Fallimento: credenziali errate

            response.success = false;
            response.message = "Username o Password errati.";
            System.out.println("Server: Tentativo di login fallito per " + username);
        }
        return response;
    }

    public GameMessage registerResponse(String username, String password) {
        GameMessage response = new GameMessage();
        response.operation = "REGISTER_RESPONSE";
        boolean registered = userManager.register(username, password);

        if (registered) {
            response.success = true;
            response.message = "Registrazione completata con successo!";
        } else {
            response.success = false;
            response.message = "Errore: username già esistente.";
        }
        return response;
    }

    public GameMessage updateCredentialsResponse(String oldName, String newName, String oldPsw, String newPsw){
        GameMessage response = new GameMessage();
        response.operation = "UPDATECREDENTIALS_RESPONSE";

        if (this.loggedUser == null) {
            response.success = false;
            response.message = "Errore: devi essere loggato per cambiare le credenziali.";
        } else {

            boolean updated = userManager.updateCredentials(oldName, newName, oldPsw, newPsw);

            if (updated) {
                response.success = true;
                response.message = "Aggiornamento credenziali completato con successo!";
            } else {
                response.success = false;
                response.message = "Errore: credenziali attuali errate o nuovo username già preso.";
            }
        }
        return response;
    }

    public GameMessage logoutResponse(){
        GameMessage response = new GameMessage();

        Game currentGame = gameManager.getCurrentGame();
        UserGameState userState = gameManager.getServerGameState().getPlayerState(loggedUser.getUsername(), currentGame.gameId);
        response.operation = "LOGOUT_RESPONSE";

        if (this.loggedUser != null) {
            String username = this.loggedUser.getUsername();
            notificationServiceUDP.unregisterClient(loggedUser.getUsername()); //togli porta UDP
            this.loggedUser = null; // Rimuoviamo il riferimento: l'utente non è più loggato
            response.success = true;
            response.message = "Arrivederci " + username + ", a presto!";
            System.out.println("Server: Logout effettuato per " + username);
        } else {
            response.success = false;
            response.message = "Devi essere loggato per effettuare il logout.";
        }
        return response;
    }

    public GameMessage submitProposalResponse(List<String> words){
        GameMessage response = new GameMessage();
        response.operation = "SUBMITPROPOSAL_RESPONSE";

            String result = gameManager.processSubmission(words, this.loggedUser.getUsername());

            if ("MALFORMED".equals(result)) {
                response.success = false;
                response.message = "Errore: proposta malformata (parole non valide o già usate).";
            } else if (result == null) {
                response.success = true;
                response.message = "Proposta errata."; // Conta come errore
            } else {
                response.success = true;
                response.message = "Corretto! Gruppo: " + result;
            }
        return response;
    }

    public GameMessage getLeaderboardResponse(String username, Integer topPlayers){
        GameMessage response = new GameMessage();
        response.operation = "REQUEST_LEADERBOARD_RESPONSE";
        List<PlayerRank> ranking = userManager.getGlobalRanking();
        response.success = true;

        // CASO A: Richiesta dell'intera classifica (topPlayers = -1)
        if (topPlayers != null && topPlayers == -1) {
            response.rankingList = ranking;
        }

        // CASO B: Classifica dei Top K (topPlayers > 0)
        else if (topPlayers != null && topPlayers > 0) {
            int k = Math.min(topPlayers, ranking.size());
            response.rankingList = ranking.subList(0, k);
        }

        // CASO C: Posizione di un certo utente (playerName fornito)
        if (username != null && !username.isEmpty()) {
            int pos = -1;
            for (int i = 0; i < ranking.size(); i++) {
                if (ranking.get(i).getUsername().equals(username)) {
                    pos = i + 1;
                    break;
                }
            }
            response.userPosition = pos;
            if (pos == -1) {
                response.success = false;
                response.message = "Utente non trovato.";
            }
        }
        return response;
    }

    private GameMessage requestPlayerStatsResponse() {
        GameMessage response = new GameMessage();
        response.operation = "PLAYER_STATS_RESPONSE";

        // Recuperiamo l'utente loggato (che è un oggetto della classe User)
        User user = this.loggedUser;

        if (user == null) {
            response.success = false;
            response.message = "Utente non trovato.";
            return response;
        }

        // Popoliamo i campi della risposta usando i dati dell'oggetto User
        response.puzzlesCompleted = user.getGamesPlayed();
        response.perfectGames = user.getPerfectGames();
        response.currentStreak = user.getCurrentStreak();
        response.maxStreak = user.getMaxStreak();
        response.winRate = user.getWinRate();
        response.lossRate = user.getLossRate();
        response.histogram = new GameMessage.HistogramData(user.getPerfectGames(), user.getOneMistakeGames(),
                user.getTwoMistakeGames(), user.getThreeMistakeGames(), user.getFourMistakeGames(),
                user.getGamesNotFinished());
        response.success = true;
        return response;
    }
}
