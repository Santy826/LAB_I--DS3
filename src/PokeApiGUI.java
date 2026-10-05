import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.MediaTracker;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class PokeApiGUI implements BattleListener {
    private final PokeApiClient apiClient = new PokeApiClient();

    private JPanel mainPanel;
    private JTextField campoNombreUno;
    private JTextField campoNombreDos;
    private JButton botonLoadUno;
    private JButton botonLoadDos;
    private JButton botonRandomUno;
    private JButton botonRandomDos;
    private JButton botonFight;
    private JLabel nombreUno;
    private JLabel nombreDos;
    private JLabel spriteUno;
    private JLabel spriteDos;
    private JLabel tiposUno;
    private JLabel tiposDos;
    private JLabel hpUno;
    private JLabel hpDos;
    private JLabel ataqueUno;
    private JLabel ataqueDos;
    private JLabel defensaUno;
    private JLabel defensaDos;
    private JLabel velocidadUno;
    private JLabel velocidadDos;
    private JProgressBar barraHpUno;
    private JProgressBar barraHpDos;
    private JTextArea areaRegistro;
    private JLabel estadoLabel;

    private Pokemon pokemonUno;
    private Pokemon pokemonDos;
    private Battle battle;
    private boolean cargaEnCurso;
    private boolean batallaEnCurso;
    private boolean primerTurnoRegistrado;

    public PokeApiGUI() {
        crearInterfaz();
        configurarEventos();
        actualizarEstadoFight();
    }

    private void crearInterfaz() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelJugadores = new JPanel(new GridLayout(1, 2, 10, 0));
        panelJugadores.add(crearPanelJugador(1));
        panelJugadores.add(crearPanelJugador(2));

        botonFight = new JButton("Fight");
        botonFight.setEnabled(false);

        areaRegistro = new JTextArea(10, 70);
        areaRegistro.setEditable(false);
        areaRegistro.setLineWrap(true);
        areaRegistro.setWrapStyleWord(true);

        estadoLabel = new JLabel("Carga un Pokemon para cada jugador.");

        JPanel panelCombate = new JPanel(new BorderLayout(5, 5));
        panelCombate.add(botonFight, BorderLayout.NORTH);
        panelCombate.add(new JScrollPane(areaRegistro), BorderLayout.CENTER);

        mainPanel.add(panelJugadores, BorderLayout.CENTER);
        mainPanel.add(panelCombate, BorderLayout.SOUTH);
        mainPanel.add(estadoLabel, BorderLayout.NORTH);
    }

    private JPanel crearPanelJugador(int jugador) {
        boolean primero = jugador == 1;
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Jugador " + jugador));

        JTextField campoNombre = new JTextField();
        JButton botonLoad = new JButton("Load");
        JButton botonRandom = new JButton("Random");

        JPanel panelBusqueda = new JPanel(new BorderLayout(5, 5));
        panelBusqueda.add(campoNombre, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new GridLayout(1, 2, 5, 0));
        panelBotones.add(botonLoad);
        panelBotones.add(botonRandom);
        panelBusqueda.add(panelBotones, BorderLayout.SOUTH);

        JLabel nombre = new JLabel("Nombre: -");
        JLabel sprite = new JLabel("Imagen no disponible", JLabel.CENTER);
        sprite.setPreferredSize(new Dimension(150, 150));
        JLabel tipos = new JLabel("Tipos: -");
        JLabel hp = new JLabel("HP: -");
        JLabel ataque = new JLabel("Ataque: -");
        JLabel defensa = new JLabel("Defensa: -");
        JLabel velocidad = new JLabel("Velocidad: -");
        JProgressBar barraHp = new JProgressBar();
        barraHp.setStringPainted(true);

        JPanel estadisticas = new JPanel(new GridLayout(0, 1));
        estadisticas.add(nombre);
        estadisticas.add(tipos);
        estadisticas.add(hp);
        estadisticas.add(barraHp);
        estadisticas.add(ataque);
        estadisticas.add(defensa);
        estadisticas.add(velocidad);

        panel.add(panelBusqueda, BorderLayout.NORTH);
        panel.add(sprite, BorderLayout.CENTER);
        panel.add(estadisticas, BorderLayout.SOUTH);

        if (primero) {
            campoNombreUno = campoNombre;
            botonLoadUno = botonLoad;
            botonRandomUno = botonRandom;
            nombreUno = nombre;
            spriteUno = sprite;
            tiposUno = tipos;
            hpUno = hp;
            ataqueUno = ataque;
            defensaUno = defensa;
            velocidadUno = velocidad;
            barraHpUno = barraHp;
        } else {
            campoNombreDos = campoNombre;
            botonLoadDos = botonLoad;
            botonRandomDos = botonRandom;
            nombreDos = nombre;
            spriteDos = sprite;
            tiposDos = tipos;
            hpDos = hp;
            ataqueDos = ataque;
            defensaDos = defensa;
            velocidadDos = velocidad;
            barraHpDos = barraHp;
        }

        return panel;
    }

    private void configurarEventos() {
        botonLoadUno.addActionListener(event -> cargarPorNombre(1));
        botonLoadDos.addActionListener(event -> cargarPorNombre(2));
        botonRandomUno.addActionListener(event -> cargarAleatorio(1));
        botonRandomDos.addActionListener(event -> cargarAleatorio(2));
        botonFight.addActionListener(event -> iniciarCombate());
    }

    private void cargarPorNombre(int jugador) {
        String nombre = obtenerCampoNombre(jugador).getText().trim();
        if (nombre.isEmpty()) {
            mostrarEstado("Escribe el nombre del Pokemon del jugador " + jugador + ".");
            return;
        }
        consultar(jugador, () -> apiClient.buscarPorNombre(nombre));
    }

    private void cargarAleatorio(int jugador) {
        consultar(jugador, apiClient::obtenerAleatorio);
    }

    private void consultar(int jugador, ConsultaPokemon consulta) {
        if (cargaEnCurso) {
            return;
        }

        cambiarControlesDuranteCarga(true);
        mostrarEstado("Consultando PokeAPI...");

        SwingWorker<PokemonCargado, Void> worker = new SwingWorker<>() {
            @Override
            protected PokemonCargado doInBackground() throws PokeApiException {
                Pokemon pokemon = consulta.ejecutar();
                return new PokemonCargado(pokemon, cargarSprite(pokemon));
            }

            @Override
            protected void done() {
                try {
                    PokemonCargado resultado = get();
                    mostrarPokemon(jugador, resultado.pokemon, resultado.sprite);
                    mostrarEstado("Pokemon cargado correctamente.");
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    mostrarEstado("La consulta fue interrumpida.");
                } catch (ExecutionException exception) {
                    Throwable causa = exception.getCause();
                    mostrarEstado(causa == null
                            ? "No se pudo cargar el Pokemon."
                            : causa.getMessage());
                } finally {
                    cambiarControlesDuranteCarga(false);
                }
            }
        };
        worker.execute();
    }

    private ImageIcon cargarSprite(Pokemon pokemon) {
        if (pokemon.getSpriteUrl().isEmpty()) {
            return null;
        }

        try {
            ImageIcon sprite = new ImageIcon(new URL(pokemon.getSpriteUrl()));
            return sprite.getImageLoadStatus() == MediaTracker.COMPLETE
                    ? sprite
                    : null;
        } catch (MalformedURLException exception) {
            return null;
        }
    }

    private void mostrarPokemon(int jugador, Pokemon pokemon, ImageIcon spriteIcon) {
        JTextField campoNombre = obtenerCampoNombre(jugador);
        JLabel nombre = jugador == 1 ? nombreUno : nombreDos;
        JLabel sprite = jugador == 1 ? spriteUno : spriteDos;
        JLabel tipos = jugador == 1 ? tiposUno : tiposDos;
        JLabel hp = jugador == 1 ? hpUno : hpDos;
        JLabel ataque = jugador == 1 ? ataqueUno : ataqueDos;
        JLabel defensa = jugador == 1 ? defensaUno : defensaDos;
        JLabel velocidad = jugador == 1 ? velocidadUno : velocidadDos;
        JProgressBar barraHp = jugador == 1 ? barraHpUno : barraHpDos;

        campoNombre.setText(pokemon.getNombre());
        nombre.setText("Nombre: " + pokemon.getNombre());
        tipos.setText("Tipos: " + String.join(", ", pokemon.getTipos()));
        hp.setText("HP: " + pokemon.getHpActual() + "/" + pokemon.getHpMaximo());
        ataque.setText("Ataque: " + pokemon.getAtaque());
        defensa.setText("Defensa: " + pokemon.getDefensa());
        velocidad.setText("Velocidad: " + pokemon.getVelocidad());
        barraHp.setMaximum(pokemon.getHpMaximo());
        barraHp.setValue(pokemon.getHpActual());
        barraHp.setString(pokemon.getHpActual() + "/" + pokemon.getHpMaximo());

        if (spriteIcon == null) {
            sprite.setIcon(null);
            sprite.setText("Imagen no disponible");
        } else {
            sprite.setText("");
            sprite.setIcon(spriteIcon);
        }

        if (jugador == 1) {
            pokemonUno = pokemon;
        } else {
            pokemonDos = pokemon;
        }
        battle = null;
        batallaEnCurso = false;
        actualizarEstadoFight();
    }

    private void iniciarCombate() {
        if (cargaEnCurso || batallaEnCurso || pokemonUno == null || pokemonDos == null) {
            return;
        }
        if (pokemonUno.getNombre().equalsIgnoreCase(pokemonDos.getNombre())) {
            mostrarEstado("Selecciona dos Pokemon diferentes para comenzar.");
            return;
        }

        batallaEnCurso = true;
        botonFight.setEnabled(false);
        cambiarBotonesSeleccion(false);
        areaRegistro.setText("");
        primerTurnoRegistrado = false;
        mostrarEstado("La batalla comenzo.");

        battle = new Battle(pokemonUno, pokemonDos);
        battle.setListener(this);

        SwingWorker<Pokemon, Void> worker = new SwingWorker<>() {
            @Override
            protected Pokemon doInBackground() {
                return battle.iniciar();
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    mostrarEstado("La batalla fue interrumpida.");
                    finalizarEstadoDeBatalla();
                } catch (ExecutionException exception) {
                    Throwable causa = exception.getCause();
                    mostrarEstado(causa == null
                            ? "Ocurrio un error durante la batalla."
                            : causa.getMessage());
                    finalizarEstadoDeBatalla();
                }
            }
        };
        worker.execute();
    }

    private JTextField obtenerCampoNombre(int jugador) {
        return jugador == 1 ? campoNombreUno : campoNombreDos;
    }

    private void cambiarControlesDuranteCarga(boolean cargando) {
        cargaEnCurso = cargando;
        cambiarBotonesSeleccion(!cargando && !batallaEnCurso);
        actualizarEstadoFight();
    }

    private void cambiarBotonesSeleccion(boolean habilitados) {
        botonLoadUno.setEnabled(habilitados);
        botonLoadDos.setEnabled(habilitados);
        botonRandomUno.setEnabled(habilitados);
        botonRandomDos.setEnabled(habilitados);
    }

    private void actualizarEstadoFight() {
        if (botonFight != null) {
            botonFight.setEnabled(!cargaEnCurso
                    && !batallaEnCurso
                    && pokemonUno != null
                    && pokemonDos != null);
        }
    }

    private void mostrarEstado(String mensaje) {
        estadoLabel.setText(mensaje == null || mensaje.isBlank()
                ? "Ocurrio un error al consultar PokeAPI."
                : mensaje);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    @Override
    public void onTurn(String attacker, String defender, int damage,
                       boolean critical, double modifier) {
        SwingUtilities.invokeLater(() -> {
            if (!primerTurnoRegistrado) {
                String motivoInicio = determinarMotivoInicio(attacker);
                areaRegistro.append("Inicia " + attacker + " (" + motivoInicio + ").\n");
                primerTurnoRegistrado = true;
            }

            String efectividad = modifier > 1.0
                    ? "Super efectivo"
                    : modifier < 1.0
                    ? "Poco efectivo"
                    : "Efectividad normal";
            String mensaje = attacker + " ataca a " + defender
                    + " y causa " + damage + " de dano. "
                    + efectividad + ".";
            if (critical) {
                mensaje += " Golpe critico.";
            }
            areaRegistro.append(mensaje + "\n");
            desplazarRegistroAlFinal();
        });
    }

    private String determinarMotivoInicio(String atacante) {
        if (pokemonUno == null || pokemonDos == null) {
            return "primer turno";
        }
        if (pokemonUno.getVelocidad() == pokemonDos.getVelocidad()) {
            return "desempate aleatorio de velocidad";
        }
        return "mayor velocidad";
    }

    @Override
    public void onHpChanged(String pokemon, int hpActual) {
        SwingUtilities.invokeLater(() -> {
            int hpSeguro = Math.max(0, hpActual);
            if (pokemonUno != null && pokemonUno.getNombre().equalsIgnoreCase(pokemon)) {
                actualizarHpVisual(hpUno, barraHpUno, hpSeguro, pokemonUno.getHpMaximo());
                areaRegistro.append(pokemon + " queda con "
                        + Math.min(hpSeguro, pokemonUno.getHpMaximo())
                        + "/" + pokemonUno.getHpMaximo() + " HP.\n");
            }
            if (pokemonDos != null && pokemonDos.getNombre().equalsIgnoreCase(pokemon)) {
                actualizarHpVisual(hpDos, barraHpDos, hpSeguro, pokemonDos.getHpMaximo());
                if (pokemonUno == null
                        || !pokemonUno.getNombre().equalsIgnoreCase(pokemon)) {
                    areaRegistro.append(pokemon + " queda con "
                            + Math.min(hpSeguro, pokemonDos.getHpMaximo())
                            + "/" + pokemonDos.getHpMaximo() + " HP.\n");
                }
            }
            desplazarRegistroAlFinal();
        });
    }

    private void actualizarHpVisual(JLabel etiqueta, JProgressBar barra,
                                    int hpActual, int hpMaximo) {
        int hpVisual = Math.min(hpActual, hpMaximo);
        etiqueta.setText("HP: " + hpVisual + "/" + hpMaximo);
        barra.setMaximum(hpMaximo);
        barra.setValue(hpVisual);
        barra.setString(hpVisual + "/" + hpMaximo);
    }

    @Override
    public void onBattleEnded(String winner) {
        SwingUtilities.invokeLater(() -> {
            areaRegistro.append("\nLa batalla finalizo.\n");
            areaRegistro.append("Ganador: " + winner + "\n");
            desplazarRegistroAlFinal();
            mostrarEstado("Ganador: " + winner + ". Selecciona nuevos Pokemon para otra batalla.");
            finalizarEstadoDeBatalla();
        });
    }

    private void finalizarEstadoDeBatalla() {
        batallaEnCurso = false;
        cambiarBotonesSeleccion(true);
        actualizarEstadoFight();
    }

    private void desplazarRegistroAlFinal() {
        areaRegistro.setCaretPosition(areaRegistro.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Pokemon Stadium Lite");
            frame.setContentPane(new PokeApiGUI().getMainPanel());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    @FunctionalInterface
    private interface ConsultaPokemon {
        Pokemon ejecutar() throws PokeApiException;
    }

    private static final class PokemonCargado {
        private final Pokemon pokemon;
        private final ImageIcon sprite;

        private PokemonCargado(Pokemon pokemon, ImageIcon sprite) {
            this.pokemon = pokemon;
            this.sprite = sprite;
        }
    }
}
