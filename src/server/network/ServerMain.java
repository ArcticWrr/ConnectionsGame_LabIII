package server.network;

import server.services.ConfigLoader;
import server.services.GameLoader;
import server.services.GameManager;
import server.services.UserManager;
import server.model.Game;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.*;

public class ServerMain {

    private static final String SERVER_CONFIG_PATH = "config/server_config.json";

    public static void main(String[] args) {

        //Il programma cerca il file nella cartella config
        try {
            ConfigLoader config = ConfigLoader.loadConfig(SERVER_CONFIG_PATH); //metodo statico
            System.out.println("Configurazione server caricata correttamente.");
            System.out.println("porta in ascolto:" + config.port);

            //Caricamento dati partite
            List<Game> allGames = GameLoader.loadGames(config.gamesFilePath);
            System.out.println("Database con " + allGames.size() + " partite pronto.");

            NotificationServiceUDP notificationServiceUDP = new NotificationServiceUDP();
            UserManager userManager = new UserManager();
            GameManager gameManager = new GameManager(allGames, config.gameDuration, userManager, notificationServiceUDP);
            userManager.setGameManager(gameManager);

            //Avvio networking
            startServer(config, gameManager, userManager, notificationServiceUDP);

        } catch (IOException e) {
            // Caso: errore generico di Input/Output (es. permessi negati)
            System.err.println("ERRORE FATALE ALL'AVVIO: " + e.getMessage());
            System.err.println("Il server non può partire senza i file necessari.");
        } catch (Exception e) {
            // Caso: qualsiasi altro errore imprevisto
            System.err.println("ERRORE FATALE imprevisto: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private static void startServer(ConfigLoader config, GameManager gameManager, UserManager userManager, NotificationServiceUDP notificationServiceUDP) throws IOException {
        //creo threadPool di 10 thread (max 10 client)
        try (ExecutorService threadPool = Executors.newFixedThreadPool(10)) {

            try (ServerSocket serverSocket = new ServerSocket(config.port)) {
                System.out.println("Server in ascolto sulla porta: " + config.port);

                while (true) {
                    try {
                        // Il server si ferma qui finché un client non si connette
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("Nuovo client connesso!");

                        threadPool.execute(new ClientHandler(clientSocket, gameManager, userManager, notificationServiceUDP));

                    } catch (IOException e) {
                        System.err.println("Errore nell'accettazione del client: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                System.err.println("Impossibile avviare il server: " + e.getMessage());
            }
        }
    }
}
