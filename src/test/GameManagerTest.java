package test;

import server.network.NotificationServiceUDP;
import server.services.GameLoader;
import server.services.GameManager;
import server.services.UserManager;
import server.model.Game;
import java.io.IOException;
import java.util.List;


public class GameManagerTest {
    public static void main(String[] args) {
        // Percorso relativo al file di dati
        String testFilePath = "data/Connections_Data.json";
        ;

        System.out.println("--- INIZIO TEST GAMEMANAGER ---");

        try {
            // Carichiamo la lista di partite
            List<Game> games = GameLoader.loadGames(testFilePath);
            NotificationServiceUDP notificationServiceUDP= new NotificationServiceUDP();
            UserManager userManager = new UserManager();
            GameManager gameManager = new GameManager(games, 10,  userManager, notificationServiceUDP);

            Game currentGame = gameManager.getCurrentGame();
             gameManager.getCurrentGame().getShuffledWords();
            System.out.println("parole partita corrente:" + currentGame.getAllWords());
            System.out.println("posizione in array partite:" + currentGame.gameId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
