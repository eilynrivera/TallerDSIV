package taller.owasp.shared;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Estado de las defensas EN VIVO, calculado desde la configuracion actual del servidor
 * (no desde el log de ataques). Sirve para el panel "Defensas en vivo" del dashboard:
 * si un participante edita la carpeta vulnerable y re-levanta, este endpoint refleja el
 * cambio al instante, sin que nadie tenga que lanzar el ataque.
 *
 * A diferencia del marcador de ataques (scoreboard.jsonl, append-only), esto es un
 * chequeo deterministico: para M4 se le pregunta al propio CORS si dejaria entrar al
 * sitio atacante (127.0.0.1:4000). Si lo rechaza, la defensa esta activa.
 */
@RestController
@Profile({"vulnerable", "patched"})
public class EstadoDefensasController {

    private static final String ORIGEN_ATACANTE = "http://127.0.0.1:4000";

    private final CorsConfigurationSource corsSource;

    public EstadoDefensasController(@Qualifier("corsTaller") CorsConfigurationSource corsSource) {
        this.corsSource = corsSource;
    }

    @GetMapping("/dashboard/estado")
    public List<EstadoDefensa> estado(HttpServletRequest request) {
        List<EstadoDefensa> estados = new ArrayList<>();
        estados.add(corsM4(request));
        return estados;
    }

    /**
     * M4 (CORS): la defensa esta activa si la configuracion actual NO autoriza al origen
     * atacante. checkOrigin devuelve el origen permitido, o null si no esta en la lista.
     */
    private EstadoDefensa corsM4(HttpServletRequest request) {
        CorsConfiguration config = corsSource.getCorsConfiguration(request);
        boolean permiteAtacante = config != null && config.checkOrigin(ORIGEN_ATACANTE) != null;
        boolean activa = !permiteAtacante;
        String detalle = activa
                ? "El CORS actual rechaza " + ORIGEN_ATACANTE
                : "El CORS actual permite " + ORIGEN_ATACANTE + " (falta la lista blanca)";
        return new EstadoDefensa(AtaquesTaller.M4, "CORS: rechaza origenes ajenos", activa, detalle);
    }

    public record EstadoDefensa(String modulo, String defensa, boolean activa, String detalle) {}
}
