package server.model;

import java.util.ArrayList;
import java.util.List;

//classe con statistiche generali dell'utente
public class User {
    //credenziali
    private String username;
    private String password;

    //statistiche generali
    private int gamesPlayed;
    private int gamesWon;
    private int gamesLost;
    private int lastScore;
    private int bestScore;
    private int totalScore;
    private int currentStreak;
    private int maxStreak;
    private int perfectGames;

    // Istogramma: [0] = 0 errori, [1] = 1 err, [2] = 2 err, [3] = 3 err, [4] = 4 err (Persa), [5] = Timeout
    private int oneMistakeGames;
    private int twoMistakeGames;
    private int threeMistakeGames;
    private int fourMistakeGames;
    private int gamesNotFinished;

    //storico dei punteggi
    private final List<Integer> scoreHistory;

    //costruttore vuoto per GSON
    public User() {
        this.scoreHistory = new ArrayList<>();
    }

    //Costruttore per la registrazione iniziale
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.gamesPlayed = 0;
        this.gamesWon = 0;
        this.lastScore = 0;
        this.bestScore = 0;
        this.totalScore = 0;
        this.currentStreak = 0;
        this.maxStreak = 0;
        this.perfectGames = 0;
        this.scoreHistory = new ArrayList<>();
    }

    public void incrementGamesPlayed() { this.gamesPlayed++; }
    public void incrementGamesWon() { this.gamesWon++; }
    public void incrementGameLost(){this.gamesLost++;}
    public void incrementPerfectGames() { this.perfectGames++; }
    public void incrementOneMistakeGames() { this.oneMistakeGames++; }
    public void incrementTwoMistakeGames() { this.twoMistakeGames++; }
    public void incrementThreeMistakeGames() { this.threeMistakeGames++; }
    public void incrementFourMistakeGames() { this.fourMistakeGames++; }
    public void incrementGamesNotFinished() { this.gamesNotFinished++; }

    public void incrementCurrentStreak() {
        this.currentStreak++;
        this.maxStreak = Math.max(this.maxStreak, this.currentStreak);
    }

    public void setLastScore(int score) {
        this.lastScore = score;
        this.totalScore += score;
        this.scoreHistory.add(score);
        if (score > this.bestScore) {
            this.bestScore = score;
        }
    }

    public double getWinRate(){
        if (gamesPlayed == 0) return 0.0;
        return (((double)gamesWon / gamesPlayed) * 100);
    }

    public double getLossRate() {
        if (gamesPlayed == 0) return 0.0;
        return (((double)gamesLost / gamesPlayed) * 100);
    }

    //GETTER
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public int getGamesPlayed() { return gamesPlayed; }
    public int getGamesWon() { return gamesWon; }
    public int getGamesLost() { return gamesLost; }
    public int getLastScore() { return lastScore; }
    public int getBestScore() { return bestScore; }
    public int getTotalScore() { return totalScore; }
    public List<Integer> getScoreHistory() { return scoreHistory;}
    public int getCurrentStreak() { return currentStreak; }
    public int getMaxStreak() { return maxStreak; }

    public int getPerfectGames() { return perfectGames; }
    public int getOneMistakeGames() { return oneMistakeGames; }
    public int getTwoMistakeGames() { return twoMistakeGames; }
    public int getThreeMistakeGames() { return threeMistakeGames; }
    public int getFourMistakeGames() { return fourMistakeGames; }
    public int getGamesNotFinished() { return gamesNotFinished; }

    //SETTER
    public void setUsername(String username) { this.username = username;}
    public void setPassword(String password) {this.password = password;}
    public void setCurrentStreak(Integer currentStreak) {this.currentStreak = currentStreak;}
}
