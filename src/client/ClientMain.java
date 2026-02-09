package client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import shared.GameMessage;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

public class ClientMain {
    private static final String CLIENT_CONFIG_PATH = "config/client_config.json";
    public static void main(String[] args) {
        try {
            ClientConfigLoader config = ClientConfigLoader.loadConfig(CLIENT_CONFIG_PATH);
            InetSocketAddress serverAddress = new InetSocketAddress(config.serverAddress, config.serverPort);
            //Try-with-resources per chiudere il canale automaticamente
            try (SocketChannel clientChannel = SocketChannel.open(serverAddress)) {
                System.out.println("Connesso con successo.");

                Gson gson = new GsonBuilder().create();

                //Apriamo la porta UDP dinamica prima di fare login
                try (DatagramSocket udpSocket = new DatagramSocket(0)) {
                    int myUdpPort = udpSocket.getLocalPort();

                    startUdpListener(udpSocket);

                    //Facciamo partire il ciclo interattivo TCP
                    startInteractiveLoop(clientChannel, gson, myUdpPort);
                }

            } catch (IOException e) {
                System.err.println("Connessione con server persa: " + e.getMessage());
            }

        } catch (IOException e) {
            System.err.println("ERRORE FATALE ALL'AVVIO: " + e.getMessage());
        } catch (Exception e) {
            // Caso: qualsiasi altro errore imprevisto
            System.err.println("ERRORE FATALE imprevisto: " + e.getMessage());
            e.printStackTrace();
        }
    }
    //write e read sul/dal channel, clear prima di read flip prima di write
    private static void sendJson(SocketChannel clientChannel, String jsonRequest) throws IOException {
        if (!jsonRequest.endsWith("\n")) jsonRequest += "\n";
        byte[] messageBytes = jsonRequest.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.wrap(messageBytes); //buffer punta a array di byte e imposta i tre indici fondamentali: posizione 0, limit: messageByte.length, capacità: dim max buffer
        while (buffer.hasRemaining()) {
            clientChannel.write(buffer); // NIO scrive i byte dal buffer al canale
        }
    }

    private static String receiveJson(SocketChannel clientChannel) throws IOException {

        StringBuilder content = new StringBuilder();
        ByteBuffer buffer = ByteBuffer.allocate(1024); // Buffer da 1KB

        while (true) {
            buffer.clear(); // Prepara il buffer per una nuova lettura dalla rete
            int bytesRead = clientChannel.read(buffer);

            if (bytesRead == -1) throw new IOException("Connessione chiusa dal server");
            if (bytesRead == 0) continue; // Se non bloccante, riprova

            buffer.flip(); // Passa dalla fase "scrivo nel buffer" a "leggo dal buffer"

            // Decodifica i byte ricevuti in una stringa temporanea
            String chunk = StandardCharsets.UTF_8.decode(buffer).toString();
            content.append(chunk);

            // CONDIZIONE DI USCITA: Abbiamo trovato la fine del messaggio JSON?
            if (content.toString().contains("\n")) {
                break;
            }
        }
        // Restituiamo il messaggio pulito dagli spazi e dal carattere \n
        return content.toString().trim();
    }

    private static GameMessage createMessageFromInput(String command, String[] params, int udpPort) {
        GameMessage message = new GameMessage();
        switch (command) {
            case "LOGIN":
                if (params.length != 3){
                    System.out.println("Login errato: login <username> <password>");
                    return null;
                }
                message.operation = params[0];
                message.username = params[1];
                message.psw = params[2];
                message.clientPortUDP = udpPort;
                break;

            case "REGISTER":
                if (params.length != 3){
                    System.out.println("Register errato: register <username> <password>");
                    return null;
                }
                message.operation = params[0];
                message.username = params[1];
                message.psw = params[2];
                break;

            case "UPDATECREDENTIALS":
                if (params.length != 5){
                    System.out.println("Update credentials errato: updateCredentials <username> <password> <newUsername> <newPassword>");
                    return null;
                }
                message.operation = params[0];
                message.username = params[1];
                message.psw = params[2];
                message.newName = params[3];
                message.newPsw = params[4];
                break;

            case "LOGOUT":
                if (params.length != 1){
                    System.out.println("Logout errato: logout ");
                    return null;
                }
                message.operation = params[0];
                break;

            case "SUBMITPROPOSAL":
                if (params.length != 5){
                    System.out.println("SubmitProposal errato: submitProposal <word1> <word2> <word3> <word4>");
                    return null;
                }
                message.operation = params[0];
                message.words = Arrays.asList(params[1], params[2], params[3], params[4]);
                break;

            case "REQUESTGAMEINFO":
                if (params.length != 2){
                    System.out.println("requestGameInfo errato: requestGameInfo <gameId> (-1 per partita corrente)");
                    return null;
                }
                message.operation = params[0];
                message.gameId = Integer.parseInt(params[1]);
                break;

            case "REQUESTGAMESTATS":
                if (params.length != 2){
                    System.out.println("requestGameStats errato: requestGameStats <gameId> (-1 per partita corrente)");
                    return null;
                }
                message.operation = params[0];
                message.gameId = Integer.parseInt(params[1]);
                break;

            case "REQUESTLEADERBOARD":
                if (params.length > 2 || params.length ==1){
                    System.out.println("\n requestLeaderboard non valido:");
                    System.out.println("  > requestLeaderboard <playerName>  (Posizione di un giocatore)");
                    System.out.println("  > requestLeaderboard <k>           (Top K giocatori)");
                    System.out.println("  > requestLeaderboard -1            (Classifica completa)");
                    return null;
                }
                message.operation = params[0];

                //per capire se viene passato numero o string
                try{
                    message.topPlayers = Integer.parseInt(params[1]);
                } catch (NumberFormatException e) {
                    message.playerName = params[1];
                }
                break;

            case "REQUESTPLAYERSTATS":
                if (params.length != 1){
                    System.out.println("requestPlayerStats errato: requestPlayerStats");
                    return null;
                }
                message.operation = params[0];
                break;

            case "HELP":
                System.out.println("\n--- ELENCO COMANDI DISPONIBILI ---");
                System.out.println("• REGISTER <user> <psw>");
                System.out.println("• LOGIN <user> <psw>");
                System.out.println("• LOGOUT");
                System.out.println("• UPDATECREDENTIALS <user> <psw> <newU> <newP>");
                System.out.println("• SUBMITPROPOSAL <w1> <w2> <w3> <w4>");
                System.out.println("• REQUESTGAMEINFO <gameId>");
                System.out.println("• REQUESTGAMESTATS <gameId>");
                System.out.println("• REQUESTPLAYERSTATS");
                System.out.println("• REQUESTLEADERBOARD <user | k | -1>");
                System.out.println("• EXIT");
                System.out.println("----------------------------------\n");
                return null;

            default:
                System.out.println("comando non valido");
                return null;
        }
        return message;
    }

    private static void startInteractiveLoop(SocketChannel clientChannel, Gson gson, int udpPort) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Digita un comando (HELP per la lista).");

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+"); // Divide per spazi
            String command = parts[0].toUpperCase();

            if (command.equals("EXIT")) {
                System.out.println("Chiusura in corso...");
                break;
            }

            GameMessage message = createMessageFromInput(command, parts, udpPort);

            if (message != null) {
                try{
                    MessageManager messageManager = new MessageManager();

                    String json = gson.toJson(message);
                    sendJson(clientChannel, json);

                    String response = receiveJson(clientChannel);
                    messageManager.handleMessage(response);
                    System.out.println("Risposta: " + response);


                } catch (IOException e) {
                    System.err.println("connessione interrotta" + e.getMessage());
                    break;
                }
            }
        }
    }

    private static void startUdpListener(DatagramSocket udpSocket){
        Thread udpListenerThread = new Thread(new udpListener(udpSocket));
        udpListenerThread.start();
    }

}
