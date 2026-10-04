import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public final class Battle {
    private static final double CRITICAL_CHANCE = 0.10;
    private static final double CRITICAL_MULTIPLIER = 1.5;
    private static final double EFFECTIVE_MULTIPLIER = 1.3;
    private static final double NOT_VERY_EFFECTIVE_MULTIPLIER = 0.7;

    private final Pokemon pokemonUno;
    private final Pokemon pokemonDos;
    private final Random random;
    private BattleListener listener;
    private Pokemon atacante;
    private boolean iniciada;
    private boolean finalizada;

    public Battle(Pokemon pokemonUno, Pokemon pokemonDos) {
        this(pokemonUno, pokemonDos, new Random());
    }

    Battle(Pokemon pokemonUno, Pokemon pokemonDos, Random random) {
        if (pokemonUno == null || pokemonDos == null || pokemonUno == pokemonDos) {
            throw new IllegalArgumentException("La batalla requiere dos Pokemon diferentes");
        }
        this.pokemonUno = pokemonUno;
        this.pokemonDos = pokemonDos;
        this.random = random;
    }

    public void setListener(BattleListener listener) {
        this.listener = listener;
    }

    public Pokemon getAtacante() {
        return atacante;
    }

    public boolean estaFinalizada() {
        return finalizada;
    }

    public Pokemon iniciar() {
        if (iniciada) {
            throw new IllegalStateException("La batalla ya fue iniciada");
        }
        iniciada = true;
        pokemonUno.restaurar();
        pokemonDos.restaurar();
        atacante = elegirPrimerAtacante();

        while (!finalizada) {
            ejecutarTurno();
        }
        return ganador();
    }

    public void ejecutarTurno() {
        if (!iniciada || finalizada) {
            throw new IllegalStateException("La batalla no esta disponible para otro turno");
        }

        Pokemon defensor = atacante == pokemonUno ? pokemonDos : pokemonUno;
        boolean critico = random.nextDouble() < CRITICAL_CHANCE;
        double modificadorTipo = calcularModificadorTipo(atacante, defensor);
        int dano = calcularDano(atacante, defensor, critico, modificadorTipo);

        defensor.recibirDano(dano);
        notificarTurno(atacante, defensor, dano, critico, modificadorTipo);
        notificarHp(defensor);

        if (defensor.estaDerrotado()) {
            finalizada = true;
            if (listener != null) {
                listener.onBattleEnded(atacante.getNombre());
            }
        } else {
            atacante = defensor;
        }
    }

    private Pokemon elegirPrimerAtacante() {
        if (pokemonUno.getVelocidad() > pokemonDos.getVelocidad()) {
            return pokemonUno;
        }
        if (pokemonDos.getVelocidad() > pokemonUno.getVelocidad()) {
            return pokemonDos;
        }
        return random.nextBoolean() ? pokemonUno : pokemonDos;
    }

    private int calcularDano(Pokemon atacante, Pokemon defensor, boolean critico, double modificadorTipo) {
        double danoBase = atacante.getAtaque() * random.nextDouble()
                - defensor.getDefensa() * random.nextDouble();
        double danoFinal = Math.max(0.0, danoBase * modificadorTipo
                * (critico ? CRITICAL_MULTIPLIER : 1.0));
        return (int) Math.round(danoFinal);
    }

    private double calcularModificadorTipo(Pokemon atacante, Pokemon defensor) {
        String tipoAtacante = primerTipo(atacante);
        String tipoDefensor = primerTipo(defensor);
        if (tipoAtacante.equals("agua") && tipoDefensor.equals("fuego")
                || tipoAtacante.equals("fuego") && tipoDefensor.equals("planta")
                || tipoAtacante.equals("planta") && tipoDefensor.equals("agua")) {
            return EFFECTIVE_MULTIPLIER;
        }
        if (tipoAtacante.equals("fuego") && tipoDefensor.equals("agua")
                || tipoAtacante.equals("planta") && tipoDefensor.equals("fuego")
                || tipoAtacante.equals("agua") && tipoDefensor.equals("planta")) {
            return NOT_VERY_EFFECTIVE_MULTIPLIER;
        }
        return 1.0;
    }

    /** PokeAPI entrega los tipos en ingles; se traducen los tres que usa la efectividad simple. */
    private String primerTipo(Pokemon pokemon) {
        String tipo = pokemon.getTipos().get(0).toLowerCase(Locale.ROOT);
        switch (tipo) {
            case "water": return "agua";
            case "fire": return "fuego";
            case "grass": return "planta";
            default: return tipo;
        }
    }

    private void notificarTurno(Pokemon atacante, Pokemon defensor, int dano,
                                boolean critico, double modificadorTipo) {
        if (listener != null) {
            listener.onTurn(atacante.getNombre(), defensor.getNombre(), dano, critico, modificadorTipo);
        }
    }

    private void notificarHp(Pokemon pokemon) {
        if (listener != null) {
            listener.onHpChanged(pokemon.getNombre(), pokemon.getHpActual());
        }
    }

    private Pokemon ganador() {
        return pokemonUno.estaDerrotado() ? pokemonDos : pokemonUno;
    }

    public List<Pokemon> getPokemon() {
        return Collections.unmodifiableList(List.of(pokemonUno, pokemonDos));
    }
}
