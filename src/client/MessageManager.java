package client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import shared.GameMessage;

import java.util.Map;

public class MessageManager {
    private final Gson gson = new Gson();

    public void handleMessage(String json) {
        // 1. Analizziamo il JSON come oggetto generico per capire l'operazione
        GameMessage request = gson.fromJson(json, GameMessage.class);

        switch (request.operation) {
            case "PLAYER_STATS_RESPONSE":
                if (request.histogram != null) {
                    displayHistogram (request.histogram);
                }
                break;
        }
    }

    public void displayHistogram(GameMessage.HistogramData histogram) {
            System.out.println("\n--- MISTAKE HISTOGRAM ---");
            // Categorie: Solved with 0 to 4 mistakes
            drawBar("Solved (0 mistakes)", histogram.zero);
            drawBar("Solved (1 mistake) ", histogram.one);
            drawBar("Solved (2 mistakes)", histogram.two);
            drawBar("Solved (3 mistakes)", histogram.three);

            System.out.println("-------------------------");

            // Categorie: Failed and Not finished
            drawBar("Failed (4 mistakes)", histogram.four);
            drawBar("Timed out (Time)   ", histogram.timeout);
        System.out.println("========================================\n");
    }

    private void drawBar(String label, int count) {
        // Creiamo la barra visiva ripetendo il carattere
        String bar = "■".repeat(Math.max(0, count));

        // %-20s allinea l'etichetta a sinistra occupando 20 spazi
        System.out.printf("%-20s | %s (%d)\n", label, bar, count);
    }

}

