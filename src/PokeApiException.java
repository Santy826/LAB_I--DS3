public class PokeApiException extends Exception {
    private static final long serialVersionUID = 1L;

    public PokeApiException(String message) {
        super(message);
    }

    public PokeApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
