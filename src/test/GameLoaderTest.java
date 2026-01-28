package test;

import server.network.NotificationServiceUDP;
import server.services.GameLoader;
import server.services.GameManager;
import server.services.UserManager;
import server.model.Game;
import java.io.IOException;
import java.util.List;

/**
 * classe di test per verificare il caricamento delle partite.
 */

public class GameLoaderTest {
    public static void main(String[] args) {
        // Percorso relativo al file di dati
        String testFilePath = "data/Connections_Data.json";;

        System.out.println("--- INIZIO TEST CARICAMENTO PARTITE ---");

        try {
            // Carichiamo la lista di partite
            List<Game> games = GameLoader.loadGames(testFilePath);
            NotificationServiceUDP notificationServiceUDP= new NotificationServiceUDP();
            UserManager userManager = new UserManager();
            GameManager gameManager = new GameManager(games, 10, userManager, notificationServiceUDP);

            if (games.isEmpty()) {
                System.out.println("Attenzione: Il file è stato letto ma non sono state trovate partite.");
            } else {
                System.out.println("Partite caricate: " + games.size());
                System.out.println("---------------------------------------");

                // Cicliamo ogni partita caricata
                for (int i = 0; i < games.size(); i++) {
                    Game g = games.get(i);
                    System.out.println("PARTITA NUMERO: " + (i + 1));

                    List<String> allWords = g.getAllWords(); // 'String' con la S maiuscola!

                    // Stampiamo le parole separate da una virgola
                    System.out.println("Tutte le parole (16): " + String.join(", ", allWords));
                    System.out.println("---------------------------------------");
                }
            }

        } catch (IOException e) {
            System.err.println("TEST FALLITO: " + e.getMessage());
        }

        System.out.println("--- FINE TEST ---");
    }
}