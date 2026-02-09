package server.services;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import server.model.Game;
import server.model.WordGroup;

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
                games.add(readGame(reader));
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

    private static Game readGame(JsonReader reader) throws IOException {
        int id = -1;
        List<WordGroup> groups = new ArrayList<>();

        reader.beginObject(); // Inizia a leggere l'oggetto {
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("gameId")) {
                id = reader.nextInt();
            } else if (name.equals("groups")) {
                reader.beginArray();
                while (reader.hasNext()) {
                    groups.add(readWordGroup(reader));
                }
                reader.endArray();
            } else {
                reader.skipValue(); // Salta campi sconosciuti per robustezza
            }
        }
        reader.endObject(); // Fine dell'oggetto }
        return new Game(id, groups);
    }

    private static WordGroup readWordGroup(JsonReader reader) throws IOException {
        String theme = "";
        List<String> words = new ArrayList<>();

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("theme")) {
                theme = reader.nextString();
            } else if (name.equals("words")) {
                reader.beginArray();
                while (reader.hasNext()) {
                    words.add(reader.nextString());
                }
                reader.endArray();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new WordGroup(theme, words);
    }
}
