import java.util.*;

public class Traduttore {
    enum Lingua { IT, EN }

    private static Lingua currentLanguage = Lingua.EN;

    private static final Map<Lingua, Map<String, String>> dizionari = new HashMap<>();

    static{
        inizializzaTraduzioni();
    }

    private static void inizializzaTraduzioni() {
        Map<String, String> en = new HashMap<>();
        en.put("dark_mode", "Dark mode");
        en.put("num_players", "Number of players:");
        en.put("select_expansions", "Select expansions:");
        en.put("extract_characters", "Extract Characters");
        en.put("select_language", "Language");
        en.put("warning_title", "Warning");
        en.put("warning_insert_players", "Enter a valid number of players (1-6).");
        en.put("warning_select_expansion", "Select at least one expansion.");
        en.put("app_title", "Random Character Selector v2 by Vincent");

        Map<String, String> it = new HashMap<>();
        it.put("dark_mode", "Modalità scura");
        it.put("num_players", "Numero di giocatori:");
        it.put("select_expansions", "Seleziona le espansioni:");
        it.put("extract_characters", "Estrai Personaggi");
        it.put("select_language", "Lingua");
        it.put("warning_title", "Attenzione");
        it.put("warning_insert_players", "Inserisci un numero di giocatori valido (1-6).");
        it.put("warning_select_expansion", "Seleziona almeno un'espansione.");
        it.put("app_title", "Sistema Pseudointelligente di Estrazione Aleatoria Nominativa ad Alta Entropia v2");

        dizionari.put(Lingua.EN, en);
        dizionari.put(Lingua.IT, it);
    }

    public static String tr(String key) {
        return dizionari.getOrDefault(currentLanguage, dizionari.get(Lingua.EN)).getOrDefault(key, key);
    }

    public static void setLingua(Lingua nuovaLingua) {
        currentLanguage = nuovaLingua;
    }

    public static Lingua getLingua() {
        return currentLanguage;
    }

}
