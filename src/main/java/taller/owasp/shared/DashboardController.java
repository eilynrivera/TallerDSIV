package taller.owasp.shared;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lee el marcador (scoreboard.jsonl que escribe {@link Scoreboard}) y lo resume para el
 * dashboard: una fila por cada ataque, con el estado en vulnerable (exito) y en patched
 * (bloqueado) lado a lado. Es el reporte historico de ataques ya lanzados; el estado de
 * las defensas en vivo lo da aparte {@link EstadoDefensasController}.
 */
@RestController
@Profile({"vulnerable", "patched"})
public class DashboardController {

    private static final Path ARCHIVO = Path.of("scoreboard.jsonl");

    private final ObjectMapper objectMapper;

    public DashboardController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * GET /dashboard: lee todas las lineas del marcador y las agrupa por modulo+ataque.
     * Por cada clave guarda si hubo "exito" (columna vulnerable) y/o "bloqueado"
     * (columna patched). Devuelve la lista para que el frontend pinte la tabla; si el
     * archivo aun no existe, devuelve lista vacia.
     */
    @GetMapping("/dashboard")
    public List<ResultadoDashboard> obtenerDashboard() throws IOException {

        Map<String, EstadoAtaque> resultados = new LinkedHashMap<>();

        if (!Files.exists(ARCHIVO)) {
            return List.of();
        }

        List<String> registros = Files.readAllLines(
                ARCHIVO, StandardCharsets.UTF_8
        );

        for (String registro : registros) {

            if (registro.isBlank()) {
                continue;
            }

            JsonNode datos;

            try {
                datos = objectMapper.readTree(registro);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(
                        "Registro inválido en scoreboard.jsonl.", e
                );
            }

            String modulo = datos.path("modulo").asText();
            String ataque = datos.path("ataque").asText();
            String resultado = datos.path("resultado").asText();

            String clave = modulo + "|" + ataque;

            EstadoAtaque estado = resultados.computeIfAbsent(
                    clave, k -> new EstadoAtaque(modulo, ataque)
            );

            if (AtaquesInyeccion.EXITO.equals(resultado)) {
                estado.vulnerable = resultado;
            } else if (AtaquesInyeccion.BLOQUEADO.equals(resultado)) {
                estado.patched = resultado;
            }
        }

        List<ResultadoDashboard> respuesta = new ArrayList<>();

        for (EstadoAtaque estado : resultados.values()) {
            respuesta.add(new ResultadoDashboard(
                    estado.modulo,
                    estado.ataque,
                    estado.vulnerable,
                    estado.patched
            ));
        }

        return respuesta;
    }

    // Acumulador interno mientras se recorre el archivo: junta en una sola entrada el
    // "exito" (vulnerable) y el "bloqueado" (patched) del mismo ataque.
    private static class EstadoAtaque {

        private final String modulo;
        private final String ataque;
        private String vulnerable;
        private String patched;

        private EstadoAtaque(String modulo, String ataque) {
            this.modulo = modulo;
            this.ataque = ataque;
        }
    }

    // Fila final que se serializa a JSON para el frontend (una por ataque).
    public record ResultadoDashboard(
            String modulo,
            String ataque,
            String vulnerable,
            String patched
    ) {}
}