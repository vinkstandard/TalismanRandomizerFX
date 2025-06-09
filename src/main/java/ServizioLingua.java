import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.*;

public class ServizioLingua {
    private static final String DEFAULT_LANG = "en";
    private static final String LANG_PATH = "/lang/";
    private static final Map<String, JsonNode> loadedLanguages = new HashMap<>();
    private static JsonNode currentLangNode;
    private static final ObjectMapper mapper = new ObjectMapper();


    public static void caricaLingua(String langCode){
        try (InputStream is = ServizioLingua.class.getResourceAsStream(LANG_PATH + langCode + ".json")) {
            if (is == null) throw new RuntimeException("Lingua non trovata: " + langCode);
            JsonNode root = mapper.readTree(is);
            loadedLanguages.put(langCode, root);
            currentLangNode = root;
        } catch (Exception e) {
            System.err.println("Errore nel caricamento della lingua: " + e.getMessage());
            if (!langCode.equals(DEFAULT_LANG)) {
                System.out.println("Caricamento lingua di default: " + DEFAULT_LANG);
                caricaLingua(DEFAULT_LANG);
            }
        }
    }

    public static String get(String... path){
        JsonNode nodo = currentLangNode;
        for (String p : path) {
            if (nodo != null) {
                nodo = nodo.get(p);
            }
        }
        return nodo != null ? nodo.asText() : String.join(".", path);
    }

    public static Set<String> getLingueDisponibili() {
        return Set.of("it", "en");
    }
    public static Map<String, String> getEspansioni() {
        JsonNode nodoEspansione = currentLangNode.path("expansions");
        Map<String, String> risultato = new LinkedHashMap<>();
        nodoEspansione.fieldNames().forEachRemaining(key -> risultato.put(key, nodoEspansione.get(key).asText()));
        return risultato;
    }
    public static List<String> getPersonaggiDaEspansione(String keyEspansione) {
        JsonNode personaggi = currentLangNode.path("characters").path(keyEspansione);
        List<String> risultato = new ArrayList<>();
        if (personaggi != null && personaggi.isObject()) {
            personaggi.fieldNames().forEachRemaining(risultato::add);
        }
        return risultato;
    }
    public static String getNomeOriginalePersonaggio(String nomeTradotto) {
        // carico la lingua italiana separatamente per trovare la mappatura dei nomi originali
        JsonNode nodoItaliano = loadedLanguages.get("it");
        if (nodoItaliano == null) {
            try (InputStream is = ServizioLingua.class.getResourceAsStream(LANG_PATH + "it.json")) {
                nodoItaliano = mapper.readTree(is);
                loadedLanguages.put("it", nodoItaliano);
            } catch (Exception e) {
                System.err.println("Errore caricamento it.json" + e.getMessage());
                return nomeTradotto;
            }
        }
        JsonNode nodoPersonaggi = nodoItaliano.path("characters");
        for (Iterator<String> it = nodoPersonaggi.fieldNames(); it.hasNext(); ) {
            String espansione = it.next();
            JsonNode expChars = nodoPersonaggi.get(espansione);
            for (Iterator<Map.Entry<String, JsonNode>> iter = expChars.fields(); iter.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = iter.next();
                if (entry.getValue().asText().equalsIgnoreCase(nomeTradotto)) {
                    return entry.getKey(); // restituisce il nome in italiano, che servirà per linkare le immagini
                }
            }
        }
        return nomeTradotto;
    }
    public static String getNomeTradotto(String nomeOriginale) {
        JsonNode nodoPersonaggi = currentLangNode.path("characters");
        for (Iterator<String> it = nodoPersonaggi.fieldNames(); it.hasNext(); ) {
            String espansione = it.next();
            JsonNode expChars = nodoPersonaggi.get(espansione);
            if (expChars.has(nomeOriginale)) {
                return expChars.get(nomeOriginale).asText();
            }
        }
        return nomeOriginale;  // fallback
    }
}
