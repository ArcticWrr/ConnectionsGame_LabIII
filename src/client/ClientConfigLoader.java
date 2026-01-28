package client;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class ClientConfigLoader {
    public String serverAddress;
    public int serverPort;
    public int udpPort;

    //converte file config nella rispettiva classe java
    public static ClientConfigLoader loadConfig(String filePath) throws IOException {
        Gson gson = new Gson();
        try (Reader reader = new FileReader(filePath)) {
            // Converte il JSON direttamente in un oggetto Config
            return gson.fromJson(reader, ClientConfigLoader.class);

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

