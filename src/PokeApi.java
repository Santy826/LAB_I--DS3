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

                //Accedemos al array de habilidades
                json.getJSONArray("abilities").forEach(ability ->
                {
                    //accedemos a cada habilidad donde esta contenido el objeto de habilidad
                    JSONObject abilityJson = (JSONObject) ability;
                    //accedemos al objeto de habilidad
                    JSONObject nameJson = (JSONObject) abilityJson.get("ability");

                    //mostramos el nombre de la habilidad
                    System.out.println("Habilidad: "+nameJson.getString("name"));

                });

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
    static void main()
    {
        PokeApi pokeApi = new PokeApi();
        pokeApi.consultarPokemon();
    }

}
