public final class PokeApi {
    private final PokeApiClient client;

    public PokeApi() {
        this.client = new PokeApiClient();
    }

    public Pokemon buscarPokemon(String nombre) throws PokeApiException {
        return client.buscarPorNombre(nombre);
    }

    public Pokemon obtenerPokemonAleatorio() throws PokeApiException {
        return client.obtenerAleatorio();
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java PokeApi <nombre-pokemon>");
            return;
        }

        try {
            System.out.println(new PokeApi().buscarPokemon(args[0]));
        } catch (PokeApiException exception) {
            System.err.println(exception.getMessage());
        }
    }
}
