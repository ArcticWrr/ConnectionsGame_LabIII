package test;

import shared.GameMessage;

import com.google.gson.Gson;
import java.io.*;
import java.net.Socket;
import java.io.*;

public class GameInfoTester {
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        String serverIp = "127.0.0.1";
        int port = 8080;

        try (Socket socket = new Socket(serverIp, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // 4. RICHIESTA INFORMAZIONI (Stato partita attuale)
            GameMessage loginReq = new GameMessage();
            loginReq.operation = "LOGIN";
            loginReq.username = "MarioRossi";
            loginReq.psw = "pasticceria";
            loginReq.clientPortUDP = 8081;
            out.println(gson.toJson(loginReq));
            System.out.println("2. Inviato Login... Risposta: " + in.readLine());

            GameMessage statusReq = new GameMessage();
            statusReq.operation = "REQUESTGAMEINFO";
            statusReq.gameId = -1; // Usiamo -1 per indicare "partita in corso"
            out.println(gson.toJson(statusReq));
            System.out.println("4.1 Richiesta Info Partita... Risposta: " + in.readLine());

            GameMessage statusReq2 = new GameMessage();
            statusReq2.operation = "REQUESTGAMEINFO";
            statusReq2.gameId = 318; // Usiamo -1 per indicare "partita in corso"
            out.println(gson.toJson(statusReq2));
            System.out.println("4.2 Richiesta Info Partita... Risposta: " + in.readLine());

            GameMessage statsReq = new GameMessage();
            statsReq.operation = "REQUESTGAMESTATS";
            statsReq.gameId = -1; // Usiamo -1 per indicare "partita in corso"
            out.println(gson.toJson(statsReq));
            System.out.println("4.3 Richiesta Stat Partita... Risposta: " + in.readLine());

            GameMessage statsReq2 = new GameMessage();
            statsReq2.operation = "REQUESTGAMESTATS";
            statsReq2.gameId = 318; // Usiamo -1 per indicare "partita in corso"
            out.println(gson.toJson(statsReq2));
            System.out.println("4.4 Richiesta Stat Partita... Risposta: " + in.readLine());


        } catch (IOException e) {
            System.err.println("Errore: il server è attivo? " + e.getMessage());
        }
    }
}
