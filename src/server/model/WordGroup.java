package server.model;

import java.util.List;
import java.util.Set;

public class WordGroup {
    public String theme;
    public List<String> words;

    // Costruttore vuoto per JSON
    public WordGroup() {
    }

    public WordGroup(String theme, List<String> words) {
        this.theme = theme;
        this.words = words;
    }

    // Controlla se ogni parola di questo gruppo è presente nella proposta
    public boolean matches(Set<String> proposalSet) {
        for (String w : words) {
            if (!proposalSet.contains(w)) {
                return false;
            }
        }
        return true;
    }

    public String getTheme() { return theme; }

    public List<String> getWords() { return words; }

}

