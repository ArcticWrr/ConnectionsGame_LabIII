package test;

import shared.GameMessage;

import java.util.Arrays;

import com.google.gson.Gson;
import shared.GameMessage;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

import com.google.gson.Gson;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;
public class RequestTester {
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        String serverIp = "127.0.0.1";
        int port = 8080;

        try (Socket socket = new Socket(serverIp, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            GameMessage loginReq = new GameMessage();
            loginReq.operation = "LOGIN";
            loginReq.username = "MarioRossi";
            loginReq.psw = "pasticceria";
            loginReq.clientPortUDP = 8081;

            out.println(gson.toJson(loginReq));
            System.out.println("2. Inviato Login... Risposta: " + in.readLine());
            // 3. INVIO PROPOSTA (4 parole)

            // Nota: queste parole dovrebbero essere prese dalla lista 'words' ricevuta nel login
            GameMessage proposalReq = new GameMessage();
            proposalReq.operation = "SUBMITPROPOSAL";
            proposalReq.words = Arrays.asList("SPOONING",
                    "HUGGING",
                    "SNUGGLING",
                    "CUDDLING");

            out.println(gson.toJson(proposalReq));
            System.out.println("3. Inviata Proposta 4 parole... Risposta: " + in.readLine());

            GameMessage proposalReq2 = new GameMessage();
            proposalReq2.operation = "SUBMITPROPOSAL";
            proposalReq2.words = Arrays.asList("NICKELODEON",
                    "HISTORY",
                    "DISCOVERY",
                    "OXYGEN");

            out.println(gson.toJson(proposalReq2));
            System.out.println("3. Inviata Proposta 4 parole... Risposta: " + in.readLine());

            GameMessage proposalReq3 = new GameMessage();
            proposalReq3.operation = "SUBMITPROPOSAL";
            proposalReq3.words = Arrays.asList("PENNYWISE",
                    "HOMEY",
                    "JOKER",
                    "DRIVE");

            out.println(gson.toJson(proposalReq3));
            System.out.println("3. Inviata Proposta 4 parole... Risposta: " + in.readLine());

            GameMessage proposalReq4 = new GameMessage();
            proposalReq4.operation = "SUBMITPROPOSAL";
            proposalReq4.words = Arrays.asList("LAMB",
                    "DELT",
                    "THE",
                    "BET");

            out.println(gson.toJson(proposalReq4));
            System.out.println("3. Inviata Proposta 4 parole... Risposta: " + in.readLine());
        } catch (IOException e) {
            System.err.println("Errore: il server è attivo? " + e.getMessage());
        }
    }
}
