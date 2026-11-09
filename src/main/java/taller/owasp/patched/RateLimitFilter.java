package taller.owasp.patched;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import taller.owasp.shared.AtaquesInyeccion;
import taller.owasp.shared.AtaquesTaller;
import taller.owasp.shared.Scoreboard;

/**
 * M6 CORREGIDO (B7): limite de intentos de login por IP (defensa contra fuerza bruta).
 *
 * Se registra en patched/SecurityConfig con addFilterBefore(..., Usuario...Filter), asi
 * que corre antes del login. Lleva una ventana deslizante en memoria por IP: permite
 * MAX_INTENTOS (5) POST /api/login por minuto; a partir del 6.o responde 429 (Too Many
 * Requests) con Retry-After y ya no deja probar mas contrasenas en esa ventana. Esto es
 * lo que detiene al DictionaryAttack, que en el perfil vulnerable recorreria la lista
 * entera. Al no ser @Component no se registra solo: lo instancia SecurityConfig.
 *
 * La ventana vive en memoria (ConcurrentHashMap), asi que reiniciar el servidor la
 * reinicia; en produccion esto iria en un almacen compartido (p. ej. Redis).
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_INTENTOS = 5;      // intentos permitidos por ventana
    private static final long VENTANA_MS = 60_000;  // duracion de la ventana: 1 minuto

    // Ventana de intentos por IP. ConcurrentHashMap porque varias peticiones entran a la vez.
    private final ConcurrentHashMap<String, AttemptWindow> intentos = new ConcurrentHashMap<>();

    // Por cada POST /api/login: cuenta el intento de esa IP, publica las cabeceras
    // RateLimit-* y, si ya paso del limite dentro del minuto, corta con 429 en vez de
    // dejar pasar la peticion al login.
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // El limite solo se aplica al endpoint de inicio de sesion.
        if (!"POST".equalsIgnoreCase(request.getMethod())
                || !"/api/login".equals(request.getServletPath())) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = request.getRemoteAddr();
        AttemptWindow ventana = intentos.computeIfAbsent(ip, key -> new AttemptWindow());

        synchronized (ventana) {
            long ahora = System.currentTimeMillis();

            if (ahora - ventana.inicio >= VENTANA_MS) {
                ventana.inicio = ahora;
                ventana.cantidad = 0;
            }

            long segundosRestantes = Math.max(
                    1, (VENTANA_MS - (ahora - ventana.inicio) + 999) / 1000);

            response.setHeader("RateLimit-Limit", String.valueOf(MAX_INTENTOS));
            response.setHeader("RateLimit-Remaining",
                    String.valueOf(Math.max(0, MAX_INTENTOS - ventana.cantidad)));
            response.setHeader("RateLimit-Reset", String.valueOf(segundosRestantes));

            if (ventana.cantidad >= MAX_INTENTOS) {
                // Marcador M6: el limite corto la fuerza bruta en este intento.
                Scoreboard.registrar(AtaquesTaller.M6, AtaquesTaller.M6_FUERZA, AtaquesInyeccion.BLOQUEADO);
                response.setHeader("Retry-After", String.valueOf(segundosRestantes));
                response.setStatus(429);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(
                        "{\"mensaje\":\"Demasiados intentos. Intenta de nuevo más tarde.\"}");
                return;
            }

            ventana.cantidad++;

            response.setHeader("RateLimit-Remaining",
                    String.valueOf(MAX_INTENTOS - ventana.cantidad));
        }

        filterChain.doFilter(request, response);
    }

    // Estado por IP: cuando empezo la ventana actual y cuantos intentos van en ella.
    private static class AttemptWindow {
        private long inicio = System.currentTimeMillis();
        private int cantidad = 0;
    }
}
