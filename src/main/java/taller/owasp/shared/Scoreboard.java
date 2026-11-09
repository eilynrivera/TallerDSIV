
package taller.owasp.shared;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Marcador del taller: escribe cada ataque intentado en scoreboard.jsonl (una linea
 * JSON por intento, formato JSON Lines, solo se agrega al final). Lo llaman tanto el
 * perfil vulnerable (registra "exito") como el patched (registra "bloqueado"), con los
 * mismos textos de modulo/ataque, y luego DashboardController empareja ambas filas.
 *
 * Es estatico a proposito: lo invocan filtros y controladores sueltos sin inyeccion.
 * El archivo es compartido por los dos servidores (3000 y 3001), que corren en la misma
 * carpeta; por eso cada escritura toma un FileLock para no entrelazar lineas.
 */
public class Scoreboard {

    // Archivo compartido (en la carpeta de trabajo). Borrarlo reinicia el marcador.
    private static final Path ARCHIVO = Path.of("scoreboard.jsonl");

    /**
     * Agrega una linea al marcador con el modulo (M1..M6, IDOR), el nombre del ataque y
     * el resultado ("exito" o "bloqueado"). Serializa el JSON a mano y lo escribe con un
     * bloqueo de archivo (FileLock) para que las escrituras concurrentes de ambos
     * perfiles no se mezclen. Si falla el disco, relanza como UncheckedIOException.
     */
    public static void registrar(String modulo, String ataque, String resultado) {

        String registro = String.format(
                "{\"modulo\":\"%s\",\"ataque\":\"%s\",\"resultado\":\"%s\"}%n",
                escapar(modulo),
                escapar(ataque),
                escapar(resultado)
        );

        byte[] datos = registro.getBytes(StandardCharsets.UTF_8);

        try {
            Files.createDirectories(ARCHIVO.toAbsolutePath().getParent());

            try (FileChannel canal = FileChannel.open(
                    ARCHIVO,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND
            );
                 FileLock bloqueo = canal.lock()) {

                ByteBuffer buffer = ByteBuffer.wrap(datos);

                while (buffer.hasRemaining()) {
                    canal.write(buffer);
                }
            }

        } catch (IOException e) {
            throw new UncheckedIOException(
                    "No se pudo registrar el resultado en el scoreboard.", e
            );
        }
    }

    // Escapa los caracteres especiales de JSON para que los textos de ataque (que
    // pueden traer comillas o backslashes del propio payload) no rompan la linea JSON.
    private static String escapar(String texto) {
        return texto
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
