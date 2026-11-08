package taller.owasp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

// Los tests de A disparan Scoreboard.registrar(...), que escribe en scoreboard.jsonl
// (la misma ruta que lee el dashboard). Para no ensuciar el marcador real, se guarda
// una copia antes de los tests y se restaura al terminar.
final class MarcadorDePrueba {

    private static final Path ARCHIVO = Path.of("scoreboard.jsonl");
    private static byte[] copia;   // null = el archivo no existia

    private MarcadorDePrueba() {}

    static void guardarCopia() throws IOException {
        copia = Files.exists(ARCHIVO) ? Files.readAllBytes(ARCHIVO) : null;
    }

    static void restaurar() throws IOException {
        if (copia == null) {
            Files.deleteIfExists(ARCHIVO);
        } else {
            Files.write(ARCHIVO, copia);
        }
    }

    static String contenido() throws IOException {
        return Files.exists(ARCHIVO) ? Files.readString(ARCHIVO, StandardCharsets.UTF_8) : "";
    }

    // Linea JSON exacta que escribe Scoreboard (sin caracteres que haya que escapar).
    static String linea(String modulo, String ataque, String resultado) {
        return "{\"modulo\":\"" + modulo + "\",\"ataque\":\"" + ataque
                + "\",\"resultado\":\"" + resultado + "\"}";
    }
}
