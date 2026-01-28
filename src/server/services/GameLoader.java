package server.services;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import server.model.Game;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//legge file JSON  e trasforma in oggetti Game
public class GameLoader {
    public static List<Game> loadGames(String filePath) throws IOException {
        List<Game> games = new ArrayList<>();
        Gson gson = new Gson();

        try (JsonReader reader = new JsonReader(new FileReader(filePath))) {
            reader.beginArray();

            while (reader.hasNext()) {
                // Deserializza un singolo oggetto Game (che contiene i 4 gruppi da 4 parole)
                Game game = gson.fromJson(reader, Game.class);
                games.add(game);
            }

            reader.endArray();
            System.out.println("Partite caricate correttamente: " + games.size());
            return games;

        } catch (FileNotFoundException e) {
            // Specifica per il file non trovato
            throw new IOException("DATABASE NON TROVATO: Impossibile aprire il file delle partite in '" + filePath + "'.", e);
        } catch (JsonSyntaxException e) {
            // Specifica per errori nel formato JSON (es. virgole mancanti)
            throw new IOException("ERRORE FORMATO: Il file JSON delle partite in '" + filePath + "' non è valido.", e);
        } catch (IOException e) {
            // Per qualsiasi altro errore di I/O (es. file corrotto durante la lettura)
            throw new IOException("ERRORE LETTURA: Problema tecnico durante l'estrazione dei dati da '" + filePath + "'.", e);
        }
    }
}
