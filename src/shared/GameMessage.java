package shared;

import server.model.PlayerRank;
import server.model.User;
import server.model.WordGroup;

import java.util.List;

public class GameMessage {
    // Campo obbligatorio per identificare l'azione
    public String operation;

    // Campi per Registrazione, Login e Aggiornamento
    public String username;
    public String psw;
    public String newName;
    public String newPsw;
    public Integer clientPortUDP; //passata nel messaggio di login

    // Campo per l'invio delle proposte (4 parole)
    public List<String> words;
    public List<String> wordsRemaining; //parole ancora da raggruppare

    public List<String> correctProposals; //gruppi già indovinati
    public Integer mistakes;
    public Integer currentScore; //punteggio utente
    public List<WordGroup> fullSolution;

    // Campi per statistiche e classifiche
    public Integer gameId;
    public Long remainingTime;
    public String gameState;
    public String playerName;
    public Integer topPlayers;

    //Campi gameStats
    public Integer playersPlaying;
    public Integer playersFinished;
    public Integer playersWon;
    public Double averageScore;
    public Integer totalParticipants;

    //Campi classifica
    public List<PlayerRank> rankingList;
    public Integer userPosition;

    // Campi per statistiche personali
    public Integer currentStreak;
    public Integer maxStreak;
    public Double winRate;
    public Double lossRate;
    public Integer puzzlesCompleted;
    public Integer perfectGames;

    //Campi per la risposta del server
    public Boolean success;   // true se l'operazione è riuscita, false altrimenti
    public String message;   // eventuale messaggio di errore (es: "Password errata")

    public HistogramData histogram;
    public static class HistogramData {
        public int zero;
        public int one;
        public int two;
        public int three;
        public int four;
        public int timeout;

        public HistogramData(int zero, int one, int two, int three, int four, int timeout) {
            this.zero = zero;
            this.one = one;
            this.two = two;
            this.three = three;
            this.four = four;
            this.timeout = timeout;
        }
    }

    // Costruttore vuoto necessario per la serializzazione JSON
    public GameMessage() {}
}
