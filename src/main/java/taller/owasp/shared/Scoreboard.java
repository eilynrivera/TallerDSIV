
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

public class Scoreboard {

    private static final Path ARCHIVO = Path.of("scoreboard.jsonl");

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

    private static String escapar(String texto) {
        return texto
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
