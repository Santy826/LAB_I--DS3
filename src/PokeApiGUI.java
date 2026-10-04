import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PokeApiGUI
{

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
        buscarPokemonButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                consultarPokemon();
            }
        });
    }

    public void consultarPokemon()
    {
        String nombrePokemon = campoNombre.getText().trim().toLowerCase();

        if (nombrePokemon.isEmpty())
        {
            JOptionPane.showMessageDialog(null, "Ingrese el nombre de un pokemon");
            return;
        }

        areaHabilidades.setText("");

        try
        {
            //se crea un cliente http para realizar la peticion
            HttpClient client = HttpClient.newHttpClient();

            //se crea un objeto de tipo request para realizar la peticion
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://pokeapi.co/api/v2/pokemon/"+nombrePokemon))
                    .build();

            //ejecutamos la solicitud
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            //se verifica el codigo de respuesta, 200 para ejecucion exitosa
            if (response.statusCode() == 200)
            {
                //Creamos el objeto JSON
                JSONObject json = new JSONObject(response.body());

                campoId.setText(String.valueOf(json.getInt("id")));
                campoPeso.setText(String.valueOf(json.getInt("weight")));
                campoAltura.setText(String.valueOf(json.getInt("height")));

                System.out.println("Habilidades:");
                //Accedemos al array de habilidades
                json.getJSONArray("abilities").forEach(ability ->
                {
                    //accedemos a cada habilidad donde esta contenido el objeto de habilidad
                    JSONObject abilityJson = (JSONObject) ability;
                    //accedemos al objeto de habilidad
                    JSONObject nameJson = (JSONObject) abilityJson.get("ability");

                    //mostramos el nombre de la habilidad
                    areaHabilidades.append(nameJson.getString("name")+"\n");

                });

                System.out.println("Estadisticas:");
                //Accedemos al array de estaditicas
                json.getJSONArray("stats").forEach(stat ->
                {
                    //accedemos a cada estadistica donde esta contenido el objeto de habilidad
                    JSONObject statJson = (JSONObject) stat;
                    //accedemos al objeto de habilidad
                    JSONObject nameJson = (JSONObject) statJson.get("stat");

                    //mostramos el nombre de la habilidad
                    System.out.println(nameJson.getString("name")+": "+statJson.getInt("base_stat"));

                    String nombre = nameJson.getString("name");
                    int valor = statJson.getInt("base_stat");

                    if (nombre.equals("hp"))
                        campoHp.setText(String.valueOf(valor));
                    else if (nombre.equals("attack"))
                        campoAtk.setText(String.valueOf(valor));
                    else if (nombre.equals("defense"))
                        campoDef.setText(String.valueOf(valor));
                    else if (nombre.equals("special-attack"))
                        campoAtkEsp.setText(String.valueOf(valor));
                    else if (nombre.equals("special-defense"))
                        campoDefEsp.setText(String.valueOf(valor));
                    else if (nombre.equals("speed"))
                        campoVelocidad.setText(String.valueOf(valor));
                });

                System.out.println("Imagen");
                //accedemos al objeto de imagen
                JSONObject imageJson = (JSONObject) json.get("sprites");
                //accedemos al objeto de imagen
                System.out.println(imageJson.getString("front_default"));

                try
                {
                    java.net.URL urlImagen = new java.net.URL(imageJson.getString("front_default"));
                    ImageIcon icono = new ImageIcon(urlImagen);
                    Image image = icono.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                    textoFoto.setText("");
                    textoFoto.setIcon(new ImageIcon(image));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    textoFoto.setText("No se pudo cargar la imagen");
                }

                System.out.println("Sonido");
                //accedemos al objeto de sonido
                JSONObject soundJson = (JSONObject) json.get("cries");
                //accedemos al objeto de sonido
                System.out.println(soundJson.getString("latest"));

            }
            else
            {
                JOptionPane.showMessageDialog(null, "El pokemon no existe");
            }
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
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
