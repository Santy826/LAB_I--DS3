import java.util.List;
import java.util.Random;

/** Pruebas ejecutables sin dependencias externas: java -ea BattleLogicTest. */
public final class BattleLogicTest {
    public static void main(String[] args) {
        testCreacionDePokemon();
        testPokemonNoPermiteHpNegativo();
        testPokemonRestauraHp();
        testVelocidadDefinePrimerTurno();
        testDesempateDeVelocidad();
        testDanoMinimoCero();
        testGolpeCritico();
        testBatallaNotificaEventosYFinaliza();
        testEfectividadDeTipo();
        testEfectividadConTiposDePokeApi();
        System.out.println("BattleLogicTest: todas las pruebas pasaron");
    }

    private static void testCreacionDePokemon() {
        Pokemon pokemon = pokemon("Pikachu", "electric", 35, 55, 40, 90);
        assert pokemon.getNombre().equals("pikachu");
        assert pokemon.getHpMaximo() == 35;
        assert pokemon.getHpActual() == 35;
        assert pokemon.getTipos().equals(List.of("electric"));
    }

    private static void testPokemonNoPermiteHpNegativo() {
        Pokemon pokemon = pokemon("test", "normal", 20, 50, 10, 10);
        pokemon.recibirDano(999);
        assert pokemon.getHpActual() == 0;
        assert pokemon.estaDerrotado();
    }

    private static void testPokemonRestauraHp() {
        Pokemon pokemon = pokemon("test", "normal", 20, 50, 10, 10);
        pokemon.recibirDano(8);
        pokemon.restaurar();
        assert pokemon.getHpActual() == pokemon.getHpMaximo();
    }

    private static void testVelocidadDefinePrimerTurno() {
        Pokemon rapido = pokemon("rapido", "normal", 50, 10, 10, 80);
        Pokemon lento = pokemon("lento", "normal", 50, 10, 10, 20);
        Battle battle = new Battle(rapido, lento, new Random(1));
        RecordingListener listener = new RecordingListener();
        battle.setListener(listener);
        battle.iniciar();
        assert listener.primerAtacante.equals("rapido");
    }

    private static void testDesempateDeVelocidad() {
        Pokemon primero = pokemon("primero", "normal", 50, 10, 10, 50);
        Pokemon segundo = pokemon("segundo", "normal", 50, 10, 10, 50);
        RecordingListener listener = new RecordingListener();
        Battle battle = new Battle(primero, segundo, new FixedRandom(false, 0.5, 0.99, 0.0));
        battle.setListener(listener);
        battle.iniciar();
        assert listener.primerAtacante.equals("segundo");
    }

    private static void testDanoMinimoCero() {
        Pokemon atacante = pokemon("atacante", "normal", 50, 1, 0, 100);
        Pokemon defensor = pokemon("defensor", "normal", 1, 1, 100, 1);
        RecordingListener listener = new RecordingListener();
        Battle battle = new Battle(atacante, defensor,
            new FixedRandom(0.5, 0.0, 0.99, 0.5, 0.0, 0.99, 0.5, 0.99, 0.0));
        battle.setListener(listener);
        battle.iniciar();
        assert listener.primerDano == 0;
    }

    private static void testGolpeCritico() {
        Pokemon atacante = pokemon("atacante", "normal", 50, 10, 0, 100);
        Pokemon defensor = pokemon("defensor", "normal", 13, 1, 0, 1);
        RecordingListener listener = new RecordingListener();
        Battle battle = new Battle(atacante, defensor, new FixedRandom(0.0, 0.9, 0.0));
        battle.setListener(listener);
        battle.iniciar();
        assert listener.critico;
        assert listener.primerDano == 14;
    }

    private static void testBatallaNotificaEventosYFinaliza() {
        Pokemon atacante = pokemon("atacante", "normal", 30, 100, 0, 100);
        Pokemon defensor = pokemon("defensor", "normal", 10, 1, 0, 1);
        RecordingListener listener = new RecordingListener();
        Battle battle = new Battle(atacante, defensor, new Random(2));
        battle.setListener(listener);
        Pokemon ganador = battle.iniciar();
        assert battle.estaFinalizada();
        assert ganador == atacante;
        assert listener.turnos > 0;
        assert listener.ganador.equals("atacante");
        assert defensor.getHpActual() == 0;
    }

    private static void testEfectividadDeTipo() {
        Pokemon agua = pokemon("agua", "agua", 100, 100, 0, 100);
        Pokemon fuego = pokemon("fuego", "fuego", 100, 1, 0, 1);
        RecordingListener listener = new RecordingListener();
        Battle battle = new Battle(agua, fuego, new Random(4));
        battle.setListener(listener);
        battle.iniciar();
        assert listener.modificador == 1.3;
    }

    /** PokeAPI devuelve los tipos en ingles ("water", "fire"): la efectividad debe aplicarse igual. */
    private static void testEfectividadConTiposDePokeApi() {
        Pokemon agua = pokemon("squirtle", "water", 100, 100, 0, 100);
        Pokemon fuego = pokemon("charmander", "fire", 100, 1, 0, 1);
        RecordingListener listener = new RecordingListener();
        Battle battle = new Battle(agua, fuego, new Random(4));
        battle.setListener(listener);
        battle.iniciar();
        assert listener.modificador == 1.3;
    }

    private static Pokemon pokemon(String nombre, String tipo, int hp, int ataque, int defensa, int velocidad) {
        return new Pokemon(1, nombre, List.of(tipo), "", hp, ataque, defensa, velocidad);
    }

    private static final class RecordingListener implements BattleListener {
        private int turnos;
        private String ganador;
        private double modificador;
        private String primerAtacante;
        private int primerDano;
        private boolean critico;

        @Override
        public void onTurn(String attacker, String defender, int damage, boolean critical, double modifier) {
            turnos++;
            if (turnos == 1) {
                primerAtacante = attacker;
                primerDano = damage;
                critico = critical;
            }
            modificador = modifier;
        }

        @Override
        public void onHpChanged(String pokemon, int hpActual) {
            assert hpActual >= 0;
        }

        @Override
        public void onBattleEnded(String winner) {
            ganador = winner;
        }
    }

    private static final class FixedRandom extends Random {
        private final double[] values;
        private int index;
        private final Boolean booleanValue;

        FixedRandom(double... values) {
            this.values = values;
            this.booleanValue = null;
        }

        FixedRandom(boolean booleanValue, double... values) {
            this.values = values;
            this.booleanValue = booleanValue;
        }

        @Override
        public double nextDouble() {
            return values[index++ % values.length];
        }

        @Override
        public boolean nextBoolean() {
            return booleanValue;
        }
    }
}
