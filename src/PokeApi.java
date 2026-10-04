import org.json.JSONObject;

import javax.swing.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PokeApi
{
    public void consultarPokemon()
    {
        String nombrePokemon = JOptionPane.showInputDialog("Ingrese el nombre del pokemon");

        if (nombrePokemon == null || nombrePokemon.trim().isEmpty())
        {
            return;
        }

        nombrePokemon = nombrePokemon.trim().toLowerCase();

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

                System.out.println("ID Pokemon: "+json.getInt("id"));
                System.out.println("Nombre: "+json.getString("name"));
                System.out.println("Peso: "+json.getInt("weight"));
                System.out.println("altura: "+json.getInt("height"));

                System.out.println("Habilidades:");
                //Accedemos al array de habilidades
                json.getJSONArray("abilities").forEach(ability ->
                {
                    //accedemos a cada habilidad donde esta contenido el objeto de habilidad
                    JSONObject abilityJson = (JSONObject) ability;
                    //accedemos al objeto de habilidad
                    JSONObject nameJson = (JSONObject) abilityJson.get("ability");

                    //mostramos el nombre de la habilidad
                    System.out.println(nameJson.getString("name"));

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
                });

                System.out.println("Imagen");
                //accedemos al objeto de imagen
                JSONObject imageJson = (JSONObject) json.get("sprites");
                //accedemos al objeto de imagen
                System.out.println(imageJson.getString("front_default"));

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

    //psvm
    public static void main(String[] args)
    {
        PokeApi pokeApi = new PokeApi();
        pokeApi.consultarPokemon();
    }

}
