package server.model;

import server.services.GameManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerGameState {
    public transient GameManager gameManager;
    private int currentGameId;        // L'ID del puzzle Wordle/Connections attuale
    private long remainingTimeMillis; // Tempo rimasto prima della rotazione
    // La cronologia: ID Partita -> (Username -> Stato Giocatore)
    private final Map<Integer, Map<String, UserGameState>> gameHistory = new ConcurrentHashMap<>();

    public ServerGameState(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    //Restituisce e/o crea stato di giocatore del quale passiamo username
    public synchronized UserGameState getPlayerState(String username, int gameId) {
        int currentId = gameManager.getCurrentGame().getGameId();
        // Assicuriamoci che esista una entry per questa partita nell'archivio
        gameHistory.putIfAbsent(currentId, new ConcurrentHashMap<>());

        if (gameId == currentId) {

            // Recuperiamo la mappa dei giocatori specifica per QUESTA partita
            Map<String, UserGameState> currentMatchPlayers = gameHistory.get(currentId);

            // Cerchiamo lo stato dell'utente dentro la mappa della partita attuale
            UserGameState state = currentMatchPlayers.get(username);

            // Se non esiste, lo creiamo e lo salviamo direttamente nell'archivio
            if (state == null) {
                state = new UserGameState(username, currentId);
                currentMatchPlayers.put(username, state);
                System.out.println("DEBUG: Creato nuovo stato per " + username + " nella partita " + currentId);
            }

            return state;
        } else {
            // Recuperiamo la mappa dei giocatori per partita specificata, restituiamo solo e non creiamo entry
            Map<String, UserGameState> customIdMatchPlayers = gameHistory.get(gameId);
            if (customIdMatchPlayers == null) {
                return null;
            }else  {
                return customIdMatchPlayers.get(username);
            }
        }
    }

    //Restituisce tutti i giocatori della partita di cui forniamo l'id
    public Map<String, UserGameState> getAllStatesForGame(int targetId) {
        return gameHistory.get(targetId);
    }

    //cambia username di utenti il cui nome è stato cambiato in gameHistory
    public synchronized void renameUserInHistory(String oldUsername, String newUsername) {
        for (Map<String, UserGameState> matchMap : gameHistory.values()) {
            if (matchMap.containsKey(oldUsername)) {

                UserGameState state = matchMap.remove(oldUsername);
                state.setUsername(newUsername);
                matchMap.put(newUsername, state);
            }
        }
    }

    public Map< Integer ,Map <String, UserGameState>> getGameHistory() {return gameHistory;}
    public Integer getCurrentGameId() {return currentGameId;}
    public long getRemainingTimeMillis() {return remainingTimeMillis;}
    public void setRemainingTimeMillis(long remainingTimeMillis) {this.remainingTimeMillis = remainingTimeMillis;}
    public void setCurrentGameId(int currentGameId) {this.currentGameId = currentGameId;}
}
