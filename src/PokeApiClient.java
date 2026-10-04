import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;

public final class PokeApiClient {
    private static final String API_URL = "https://pokeapi.co/api/v2/pokemon/";
    private static final int MAX_POKEMON_ID = 1025;
    /** Los nombres de PokeAPI solo usan minusculas, numeros y guiones (ej. "mr-mime"). */
    private static final Pattern NOMBRE_VALIDO = Pattern.compile("[a-z0-9]+(-[a-z0-9]+)*");

    private final String apiUrl;
    private final HttpClient httpClient;
    private final Random random;

    public PokeApiClient() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build(), new Random());
    }

    PokeApiClient(HttpClient httpClient, Random random) {
        this(API_URL, httpClient, random);
    }

    /** Permite apuntar a otro servidor (por ejemplo, uno simulado en pruebas). */
    PokeApiClient(String apiUrl, HttpClient httpClient, Random random) {
        this.apiUrl = apiUrl;
        this.httpClient = httpClient;
        this.random = random;
    }

    public Pokemon buscarPorNombre(String nombre) throws PokeApiException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new PokeApiException("Debes indicar el nombre de un Pokemon");
        }
        String identificador = normalizarNombre(nombre);
        if (!NOMBRE_VALIDO.matcher(identificador).matches()) {
            throw new PokeApiException("Nombre no valido: \"" + nombre.trim()
                    + "\". Usa solo letras, numeros, espacios o guiones");
        }
        return solicitar(identificador, nombre.trim());
    }

    public Pokemon obtenerAleatorio() throws PokeApiException {
        int id = random.nextInt(MAX_POKEMON_ID) + 1;
        return solicitar(String.valueOf(id), "#" + id);
    }

    /**
     * Convierte lo que escribe el usuario al formato de PokeAPI:
     * "Mr. Mime" -> "mr-mime", "Farfetch'd" -> "farfetchd", "Type: Null" -> "type-null".
     * (Los simbolos de genero y los apostrofes tipograficos van como escapes Unicode.)
     */
    static String normalizarNombre(String nombre) {
        String texto = nombre.trim().replace("♀", "-f").replace("♂", "-m");
        texto = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")            // quita tildes
                .toLowerCase(Locale.ROOT)
                .replaceAll("[.'’:]", "")       // puntos, apostrofes y dos puntos
                .replaceAll("[\\s-]+", "-");         // espacios -> un solo guion
        return texto.replaceAll("^-|-$", "");
    }

    /**
     * @param identificador nombre normalizado o id numerico que se agrega a la URL
     * @param descripcion    como se lo mostramos al usuario en los mensajes de error
     */
    private Pokemon solicitar(String identificador, String descripcion) throws PokeApiException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + identificador))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new PokeApiException("La consulta a PokeAPI fue interrumpida", exception);
        } catch (HttpTimeoutException exception) {
            throw new PokeApiException("Error de red: PokeAPI tardo demasiado en responder", exception);
        } catch (IOException exception) {
            throw new PokeApiException("Error de red: no se pudo conectar con PokeAPI. "
                    + "Revisa tu conexion a internet", exception);
        }

        int status = response.statusCode();
        if (status == 404) {
            throw new PokeApiException("Pokemon no encontrado: " + descripcion);
        }
        if (status == 429) {
            throw new PokeApiException("PokeAPI recibio demasiadas consultas (HTTP 429). "
                    + "Espera unos segundos e intenta de nuevo");
        }
        if (status < 200 || status >= 300) {
            throw new PokeApiException("PokeAPI respondio con HTTP " + status);
        }

        try {
            return convertirPokemon(new JSONObject(response.body()));
        } catch (JSONException exception) {
            throw new PokeApiException("La respuesta de PokeAPI no tiene el formato esperado", exception);
        }
    }

    private Pokemon convertirPokemon(JSONObject json) throws PokeApiException {
        try {
            List<String> tipos = new ArrayList<>();
            JSONArray types = json.getJSONArray("types");
            for (int index = 0; index < types.length(); index++) {
                tipos.add(types.getJSONObject(index).getJSONObject("type").getString("name"));
            }

            JSONObject stats = extraerEstadisticas(json.getJSONArray("stats"));
            // Algunos Pokemon no tienen sprite frontal (front_default = null): se deja vacio.
            JSONObject sprites = json.optJSONObject("sprites");
            String sprite = sprites == null ? "" : sprites.optString("front_default", "");
            List<String> habilidades = extraerHabilidades(json.getJSONArray("abilities"));

            return new Pokemon(
                    json.getInt("id"),
                    json.getString("name"),
                    tipos,
                    sprite,
                    stats.getInt("hp"),
                    stats.getInt("attack"),
                    stats.getInt("defense"),
                    stats.getInt("special-attack"),
                    stats.getInt("special-defense"),
                    stats.getInt("speed"),
                    json.getInt("weight"),
                    json.getInt("height"),
                    habilidades
            );
        } catch (RuntimeException exception) {
            throw new PokeApiException("La respuesta de PokeAPI no tiene el formato esperado", exception);
        }
    }

    private List<String> extraerHabilidades(JSONArray abilitiesArray) {
        List<String> habilidades = new ArrayList<>();
        for (int index = 0; index < abilitiesArray.length(); index++) {
            habilidades.add(abilitiesArray.getJSONObject(index)
                    .getJSONObject("ability").getString("name"));
        }
        return habilidades;
    }

    private JSONObject extraerEstadisticas(JSONArray statsArray) {
        JSONObject stats = new JSONObject(new java.util.HashMap<>());
        for (int index = 0; index < statsArray.length(); index++) {
            JSONObject statEntry = statsArray.getJSONObject(index);
            String name = statEntry.getJSONObject("stat").getString("name");
            if (name.equals("hp") || name.equals("attack") || name.equals("defense")
                    || name.equals("special-attack") || name.equals("special-defense")
                    || name.equals("speed")) {
                stats.put(name, statEntry.getInt("base_stat"));
            }
        }
        return stats;
    }

    /** Implementacion minima para evitar una dependencia externa de org.json. */
    private static final class JSONException extends RuntimeException {
        JSONException(String message) { super(message); }
    }

    private static final class JSONObject {
        private final java.util.Map<String, Object> values;

        JSONObject(String source) { this.values = new Parser(source).object(); }
        private JSONObject(java.util.Map<String, Object> values) { this.values = values; }

        JSONArray getJSONArray(String key) { return (JSONArray) required(key); }
        JSONObject getJSONObject(String key) { return (JSONObject) required(key); }
        String getString(String key) { return (String) required(key); }
        int getInt(String key) { return ((Number) required(key)).intValue(); }
        String optString(String key, String fallback) {
            Object value = values.get(key);
            return value instanceof String ? (String) value : fallback;
        }
        JSONObject optJSONObject(String key) {
            Object value = values.get(key);
            return value instanceof JSONObject ? (JSONObject) value : null;
        }
        void put(String key, Object value) { values.put(key, value); }
        private Object required(String key) {
            if (!values.containsKey(key) || values.get(key) == null) throw new JSONException("Missing key: " + key);
            return values.get(key);
        }
    }

    private static final class JSONArray {
        private final List<Object> values;
        JSONArray(List<Object> values) { this.values = values; }
        int length() { return values.size(); }
        JSONObject getJSONObject(int index) { return (JSONObject) values.get(index); }
    }

    private static final class Parser {
        private final String text;
        private int position;
        Parser(String text) { this.text = text; }
        java.util.Map<String, Object> object() {
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            expect('{'); skip();
            if (peek('}')) { position++; return result; }
            while (true) {
                String key = string(); skip(); expect(':'); result.put(key, value()); skip();
                if (peek('}')) { position++; return result; }
                expect(','); skip();
            }
        }
        private Object value() {
            skip();
            if (peek('{')) return new JSONObject(object());
            if (peek('[')) { position++; List<Object> list = new ArrayList<>(); skip();
                if (!peek(']')) { while (true) { list.add(value()); skip(); if (peek(']')) break; expect(','); } }
                expect(']'); return new JSONArray(list); }
            if (peek('"')) return string();
            int start = position; while (position < text.length() && " ,-}\n\r\t".indexOf(text.charAt(position)) < 0) position++;
            String token = text.substring(start, position);
            if ("null".equals(token)) return null;
            if ("true".equals(token)) return Boolean.TRUE;
            if ("false".equals(token)) return Boolean.FALSE;
            try { return Integer.valueOf(token); } catch (NumberFormatException e) { throw new JSONException("Invalid value"); }
        }
        private String string() {
            expect('"'); StringBuilder result = new StringBuilder();
            while (position < text.length()) { char c = text.charAt(position++); if (c == '"') return result.toString();
                if (c == '\\' && position < text.length()) { char escaped = text.charAt(position++); result.append(escaped == 'n' ? '\n' : escaped); } else result.append(c); }
            throw new JSONException("Unterminated string");
        }
        private void skip() { while (position < text.length() && Character.isWhitespace(text.charAt(position))) position++; }
        private boolean peek(char c) { return position < text.length() && text.charAt(position) == c; }
        private void expect(char c) { if (!peek(c)) throw new JSONException("Invalid JSON"); position++; }
    }
}
