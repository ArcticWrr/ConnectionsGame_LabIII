package server.model;

import java.util.ArrayList;
import java.util.List;

//classe temporanea per ogni partita
public class UserGameState {
    private String username;
    private final int gameId;
    private int mistakes; // Massimo 4 per partita
    private final List<String> foundThemes; // I temi (nomi dei gruppi) già indovinati
    private final List<String> foundWords; // Lista piatta di tutte le parole indovinate
    private boolean isFinished; // Indica se l'utente ha concluso la partita o finito il tempo
    private boolean isWon; //Indica se l'utente ha vinto
    private boolean isLost; //Indica se l'utente ha perso
    private boolean alreadySaved; // evita salvataggi duplicati
    private int currentScore;

    public UserGameState(String username, int gameId) {
        this.username = username;
        this.gameId = gameId;
        this.mistakes = 0;
        this.foundThemes = new ArrayList<>();
        this.foundWords = new ArrayList<>();
        this.isFinished = false;
        this.isWon = false;
        this.isLost = false;
        this.alreadySaved = false;
    }

    // --- LOGICA DI GIOCO ---

    public void addMistake() {
        this.mistakes++;
        if (this.mistakes >= 4) {
            this.isFinished = true; // Sconfitta per troppi errori
            this.isLost = true;
        }
    }

    public void addFoundGroup(WordGroup wordGroup) {
        String theme = wordGroup.getTheme();
        if (!foundThemes.contains(theme)) {
            foundThemes.add(theme);

            foundWords.addAll(wordGroup.getWords());
        }
        // Se indovina 3 gruppi, ha vinto (il 4° è automatico)
        if (foundThemes.size() >= 3) {
            this.isFinished = true;
            this.isWon = true;

        }
    }
    /**
     * Calcola il punteggio istantaneo seguendo le regole del PDF:
     * +6, +12, +18 per i gruppi corretti e -4 per ogni errore.
     */
    public int calculateCurrentScore() {
        int bonus = 0;
        if (foundThemes.size() == 1) bonus = 6;
        else if (foundThemes.size() == 2) bonus = 12;
        else if (foundThemes.size() >= 3) bonus = 18;

        int penalty = mistakes * 4;
        currentScore = bonus - penalty;
        return currentScore;
    }

    public void finishByTimeout() {
        this.isFinished = true;
        this.isWon = false;
    }

    // GETTER
    public int getMistakes() { return mistakes; }
    public List<String> getFoundThemes() { return foundThemes; }
    public boolean isFinished() { return isFinished; }
    public int getGameId() { return gameId; }
    public List<String> getFoundWords() {return foundWords;}
    public String getUsername() {return username;}
    public boolean isWon() { return isWon; }
    public boolean isLost() { return isLost; }
    public boolean isAlreadySaved() {return alreadySaved;}

    //SETTER
    public void setAlreadySaved(boolean alreadySaved) {this.alreadySaved = alreadySaved;}
    public void setUsername(String username) {this.username = username;}
}
