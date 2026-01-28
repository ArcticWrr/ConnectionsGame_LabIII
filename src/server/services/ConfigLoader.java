package server.services;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class ConfigLoader {
    public int port;
    public String gamesFilePath;
    public int gameDuration;

    //converte file config nella rispettiva classe java
    public static ConfigLoader loadConfig(String filePath) throws IOException {
        Gson gson = new Gson();
        try (Reader reader = new FileReader(filePath)) {
            // Converte il JSON direttamente in un oggetto Config
            return gson.fromJson(reader, ConfigLoader.class);

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
