import java.util.concurrent.ExecutionException;
import javax.swing.*;

public class PokeApiGUI
{
    private final PokeApiClient apiClient = new PokeApiClient();

    private JPanel mainPanel;
    private JTextField campoId;
    private JTextField campoNombre;
    private JTextField campoPeso;
    private JTextField campoAltura;
    private JTextField campoHp;
    private JTextField campoAtk;
    private JTextField campoDef;
    private JTextField campoAtkEsp;
    private JTextField campoDefEsp;
    private JTextField campoVelocidad;
    private JTextArea areaHabilidades;
    private JButton buscarPokemonButton;
    private JLabel textoFoto;


    public PokeApiGUI()
    {
        buscarPokemonButton.addActionListener(event -> consultarPokemon());
    }

    public void consultarPokemon()
    {
        String nombrePokemon = campoNombre.getText().trim();
        if (nombrePokemon.isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, "Escribe el nombre de un Pokemon");
            return;
        }

        buscarPokemonButton.setEnabled(false);
        SwingWorker<Pokemon, Void> worker = new SwingWorker<>() {
            @Override
            protected Pokemon doInBackground() throws PokeApiException {
                return apiClient.buscarPorNombre(nombrePokemon);
            }

            @Override
            protected void done() {
                buscarPokemonButton.setEnabled(true);
                try {
                    mostrarPokemon(get());
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    mostrarError("La consulta fue interrumpida");
                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause();
                    mostrarError(cause == null ? exception.getMessage() : cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void mostrarPokemon(Pokemon pokemon) {
        campoId.setText(String.valueOf(pokemon.getId()));
        campoNombre.setText(pokemon.getNombre());
        campoPeso.setText(String.valueOf(pokemon.getPeso()));
        campoAltura.setText(String.valueOf(pokemon.getAltura()));
        campoHp.setText(String.valueOf(pokemon.getHpMaximo()));
        campoAtk.setText(String.valueOf(pokemon.getAtaque()));
        campoDef.setText(String.valueOf(pokemon.getDefensa()));
        campoAtkEsp.setText(String.valueOf(pokemon.getAtaqueEspecial()));
        campoDefEsp.setText(String.valueOf(pokemon.getDefensaEspecial()));
        campoVelocidad.setText(String.valueOf(pokemon.getVelocidad()));
        areaHabilidades.setText(String.join("\n", pokemon.getHabilidades()));

        if (pokemon.getSpriteUrl().isEmpty()) {
            textoFoto.setIcon(null);
            textoFoto.setText("Imagen no disponible");
            return;
        }
        textoFoto.setText("");
        textoFoto.setIcon(new ImageIcon(pokemon.getSpriteUrl()));
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(mainPanel, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() ->
        {
            JFrame frame = new JFrame("PokeApi");
            frame.setContentPane(new PokeApiGUI().mainPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }


}
