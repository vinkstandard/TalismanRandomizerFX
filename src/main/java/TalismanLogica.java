import java.util.*;

public class TalismanLogica {

    private static final Map<String, List<String>> personaggi = new HashMap<>();


    static {
        personaggi.put("1", Arrays.asList( // Classic
                "Assassino", "Druido", "Nano", "Elfo", "Ghoul",
                "Menestrello", "Monaco", "Prete", "Profetessa",
                "Incantatrice", "Ladro", "Troll", "Guerriero", "Mago"
        ));

        personaggi.put("2", Arrays.asList( // Il mietitore
                "Cultista Oscura", "Cavaliere", "Mercante", "Saggio"
        ));

        personaggi.put("3", Arrays.asList( // Avanzata dei ghiacci
                "Leprecano", "Negromante", "Capitano Ogre", "Stregone"
        ));

        personaggi.put("4", Arrays.asList( // Dungeon
                "Amazzone", "Gladiatore", "Zingara", "Filosofo", "Spadaccino"
        ));

        personaggi.put("5", Arrays.asList( // Lande montuose
                "Spiritello", "Ladra", "Immortale", "Alchimista", "Valchiria", "Vampiressa"
        ));

        personaggi.put("6", Arrays.asList( // Lo stagno sacro
                "Cavaliere Errante", "Sacerdotessa", "Cavaliere del Terrore", "Magus"
        ));

        personaggi.put("7", Arrays.asList( // La città
                "Riparatore", "Spia", "Cacciatore di Taglie", "Elementalista", "Locandiera", "Ladra Acrobata"
        ));

        personaggi.put("8", Arrays.asList( // Luna di sangue
                "Profeta di Sventura", "Tombarolo", "Cacciatrice di Vampiri"
        ));

        personaggi.put("9", Arrays.asList( // Regno del fuoco
                "Derviscio", "Signore della Guerra", "Nomade", "Genio Mezzosangue"
        ));

        personaggi.put("10", Arrays.asList( // Drago
                "Minotauro", "Cacciatore di Draghi", "Mago del Fuoco", "Prestigiatrice", "Sacerdotessa dei Draghi", "Cavalcatrice di Draghi"
        ));

        personaggi.put("11", Arrays.asList( // Messaggero
                "Divina Ascendente", "Celestiale", "Posseduto"
        ));

        personaggi.put("12", Arrays.asList( // Terre Boscose
                "Antica Quercia", "Guerriero Totem", "Viandante Sincronico", "Regina dei Ragni"
        ));

        personaggi.put("13", Arrays.asList( // Cataclisma
                "Discendente Arcano", "Barbaro", "Cavaliere Nero", "Mutante", "Raccoglitrice"
        ));

        personaggi.put("14", Arrays.asList( // Regno delle anime?
                "Arconte", "Chiaroveggente", "Collezionista di Spettri"
        ));

        personaggi.put("15", Arrays.asList( // Bestie antiche
                "Cacciatore di Trofei", "Specialista"
        ));

        personaggi.put("16", Arrays.asList( // Regno meccanico
                "Artificiere", "Ingegnere", "Ingannatore"
        ));

    }
    public static List<String> getPersonaggiDaEspansione(List<String> espansioniAttive) {
        List<String> lista = new ArrayList<>();
        for (String esp : espansioniAttive) {
            lista.addAll(personaggi.getOrDefault(esp, Collections.emptyList()));
        }
        return lista;
    }
    public static List<String> estraiPersonaggi(int numGiocatori, List<String> espansioni) {
        List<String> lista = getPersonaggiDaEspansione(espansioni);
        Collections.shuffle(lista);
        return lista.subList(0, Math.min(numGiocatori, lista.size()));
    }
    public static Map<String, String> getLegenda() {
        Map<String, String> legendaIT = new LinkedHashMap<>();
        legendaIT.put("1", "Classic");
        legendaIT.put("2", "Mietitore");
        legendaIT.put("3", "Avanzata dei Ghiacci");
        legendaIT.put("4", "Dungeon");
        legendaIT.put("5", "Lande Montuose");
        legendaIT.put("6", "Stagno Sacro");
        legendaIT.put("7", "Città");
        legendaIT.put("8", "Luna di Sangue");
        legendaIT.put("9", "Lande del Fuoco");
        legendaIT.put("10", "Il Drago");
        legendaIT.put("11", "Il Messaggero");
        legendaIT.put("12", "Lande boscose");
        legendaIT.put("13", "Il Cataclisma");
        legendaIT.put("14", "Regno delle Anime");
        legendaIT.put("15", "Le Bestie Antiche");
        legendaIT.put("16", "Il Regno Meccanico");
        return legendaIT;
    }
}
