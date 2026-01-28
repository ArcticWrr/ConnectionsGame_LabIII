package test;
import com.google.gson.Gson;
import shared.GameMessage;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

import com.google.gson.Gson;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class LoginTester {
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        String serverIp = "127.0.0.1";
        int port = 8080;

        try (Socket socket = new Socket(serverIp, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("--- INIZIO FLUSSO TEST LOGIN---");

            // 1. REGISTRAZIONE (Sempre prima di tutto)
            GameMessage regReq = new GameMessage();
            regReq.operation = "REGISTER";
            regReq.username = "MarioRossi";
            regReq.psw = "pasticceria";

            out.println(gson.toJson(regReq));
            System.out.println("1. Inviata Registrazione... Risposta: " + in.readLine());

            GameMessage regReq2 = new GameMessage();
            regReq2.operation = "REGISTER";
            regReq2.username = "Pino";
            regReq2.psw = "ssa";

            out.println(gson.toJson(regReq2));
            System.out.println("1. Inviata Registrazione... Risposta: " + in.readLine());

            // 2. LOGIN (Necessario per le operazioni successive)
            GameMessage loginReq = new GameMessage();
            loginReq.operation = "LOGIN";
            loginReq.username = "MarioRossi";
            loginReq.psw = "pasticceria";
            loginReq.clientPortUDP = 8080;

            out.println(gson.toJson(loginReq));
            System.out.println("2. Inviato Login... Risposta: " + in.readLine());

            // 2.1 LOGIN (Necessario per le operazioni successive)
            GameMessage loginReq2 = new GameMessage();
            loginReq2.operation = "LOGIN";
            loginReq2.username = "Pino";
            loginReq2.psw = "ssa";
            loginReq2.clientPortUDP = 8081;


            out.println(gson.toJson(loginReq2));
            System.out.println("2. Inviato Login... Risposta: " + in.readLine());

            // 5. CAMBIO CREDENZIALI (Password)
            /*
            GameMessage pswReq = new GameMessage();
            pswReq.operation = "UPDATECREDENTIALS";
            pswReq.username = "MarioRossi";
            pswReq.psw = "pasticceria";        // Vecchia
            pswReq.newPsw = "nuovaPassword99"; // Nuova
            pswReq.newName= "Ciro";

            out.println(gson.toJson(pswReq));
            System.out.println("5. Inviato Cambio Password... Risposta: " + in.readLine());
               */
            // 6. LOGOUT

            GameMessage logoutReq = new GameMessage();
            logoutReq.operation = "LOGOUT";

            out.println(gson.toJson(logoutReq));
            System.out.println("6. Inviato Logout... Risposta: " + in.readLine());

            System.out.println("--- FINE TEST ---");


        } catch (IOException e) {
            System.err.println("Errore: il server è attivo? " + e.getMessage());
        }
    }
}