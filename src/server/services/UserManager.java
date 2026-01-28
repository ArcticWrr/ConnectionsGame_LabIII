package server.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import server.model.PlayerRank;
import server.model.User;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {
    private final String userFilePath = "data/users.json";
    private final ConcurrentHashMap<String, User> users; //mappa degli utenti condivisa da thread
    private GameManager gameManager;


    public UserManager() {

        this.users = loadUsers();
    }

    //trasforma utenti da formato json ad hashMap
    private ConcurrentHashMap<String, User> loadUsers() {
        Gson gson = new Gson();
        try (Reader reader = new FileReader(userFilePath)) {
            Type mapType = new TypeToken<ConcurrentHashMap<String, User>>() {}.getType();
            ConcurrentHashMap<String, User> loaded = gson.fromJson(reader, mapType);
            return (loaded != null) ? loaded : new ConcurrentHashMap<>();
        } catch (IOException e) {
            return new ConcurrentHashMap<>(); // Se il file non esiste, iniziamo da zero
        }
    }

    // Registra un nuovo utente
    public synchronized boolean register(String username, String password) {
        if (users.containsKey(username)) return false;
        users.put(username, new User(username, password));
        saveUsers();
        return true;
    }

    // Salva tutto su file
    public synchronized void saveUsers() {
        try (Writer writer = new FileWriter(userFilePath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(users, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User login(String username, String password) {
        //cerco l'utente nella mappa
        User user = users.get(username);

        if (user != null && user.getPassword().equals(password)) {
            System.out.println("Login riuscito per l'utente: " + username);
            return user; // Restituiamo l'oggetto User per tenere traccia della sessione
        }

        System.out.println("Tentativo di login fallito per: " + username);
        return null; // Credenziali errate o utente inesistente
    }

    public synchronized boolean updateCredentials(String oldUsername, String newUsername, String oldPassword, String newPassword) {
        // Verifichiamo che l'utente esista
        User user = users.get(oldUsername);
        if (user == null || !user.getPassword().equals(oldPassword)) return false;

        // Se lo username sta cambiando, controlliamo che il nuovo non sia già preso
        if (!oldUsername.equals(newUsername)) {
            if (users.containsKey(newUsername)) {
                return false; // Il nuovo username è già occupato
            }
            // Rimuoviamo il vecchio record e inseriamo il nuovo
            users.remove(oldUsername);
            user.setUsername(newUsername);
            users.put(newUsername, user);
        }
        user.setPassword(newPassword);
        saveUsers();
        // Salviamo la mappa aggiornata sul file JSON

        gameManager.getServerGameState().renameUserInHistory(oldUsername, newUsername);
        gameManager.saveMatchesToFile();
        return true;
    }

    public List<PlayerRank> getGlobalRanking() {
        List<PlayerRank> rankingList = new ArrayList<>();

        for (User u : users.values()) {
            rankingList.add(new PlayerRank(u.getUsername(), u.getTotalScore(), u.getGamesPlayed()));
        }
        // Ordiniamo in base al punteggio totale (decrescente)
        rankingList.sort((u1, u2) -> Integer.compare(u2.getTotalScore(), u1.getTotalScore()));

        for (int i = 0; i < rankingList.size(); i++) {
            rankingList.get(i).setRanking(i + 1);
        }

        return rankingList;
    }

    public User getUser(String username) {
        if (username == null) return null;
        return users.get(username);
    }

    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }
}



