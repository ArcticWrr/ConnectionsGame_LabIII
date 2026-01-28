package server.model;

import java.util.*;

public class Game {
    public int gameId; // ID univoco della partita
    public List<WordGroup> groups; // I 4 gruppi tematici

    public Game(int gameId, List<WordGroup> groups) {
        this.gameId = gameId;
        this.groups = groups;
    }

    /**
     * Metodo di utilità per ottenere tutte le 16 parole mescolate
     * da inviare ai giocatori all'inizio della partita.
     */
    public List<String> getAllWords() {
        List<String> allWords = new ArrayList<>();
        for (WordGroup g : groups) {
            allWords.addAll(g.words);
        }
        return allWords;
    }

    public List<String> getShuffledWords() {
        List<String> shuffledWords = new ArrayList<>(getAllWords());
        Collections.shuffle(shuffledWords);
        return shuffledWords;
    }


    public List<WordGroup> getGroups() {return groups;}

    public int getGameId() {return gameId;}

    public WordGroup checkWordsMatch(List<String> proposal) {
        // Trasformiamo in Set per non preoccuparci dell'ordine
        Set<String> setProposta = new HashSet<>(proposal);

        for (WordGroup group : this.groups) {
            if (group.matches(setProposta)) {
                return group; // Ritorna il gruppo di parole (tema + parole)
            }
        }
        return null; // Nessun match trovato
    }
}
