package taller.owasp.vulnerable;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import taller.owasp.shared.AtaquesInyeccion;
import taller.owasp.shared.AtaquesTaller;
import taller.owasp.shared.Scoreboard;

/**
 * Marcador de M6 (fuerza bruta) en el perfil VULNERABLE. No bloquea nada: solo cuenta
 * los POST /api/login por IP y, al llegar al intento que el RateLimitFilter de patched
 * habria cortado (el 6.o dentro de un minuto), registra "exito" una vez. Es la contra-
 * parte del "bloqueado" que patched escribe al responder 429: el ataque de diccionario
 * dispara ambos. Reiniciar el servidor reinicia el contador (la ventana es en memoria).
 */
@Component
@Profile("vulnerable")
public class BruteForceMarcadorFilter extends OncePerRequestFilter {

    private static final int UMBRAL = 6;          // mismo punto en que patched devuelve 429
    private static final long VENTANA_MS = 60_000;

    private final ConcurrentHashMap<String, Ventana> intentos = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if ("POST".equalsIgnoreCase(request.getMethod())
                && "/api/login".equals(request.getServletPath())) {

            String ip = request.getRemoteAddr();
            Ventana ventana = intentos.computeIfAbsent(ip, key -> new Ventana());

            synchronized (ventana) {
                long ahora = System.currentTimeMillis();
                if (ahora - ventana.inicio >= VENTANA_MS) {
                    ventana.inicio = ahora;
                    ventana.cantidad = 0;
                    ventana.registrado = false;
                }
                ventana.cantidad++;
                if (ventana.cantidad >= UMBRAL && !ventana.registrado) {
                    ventana.registrado = true;
                    Scoreboard.registrar(AtaquesTaller.M6, AtaquesTaller.M6_FUERZA, AtaquesInyeccion.EXITO);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private static class Ventana {
        private long inicio = System.currentTimeMillis();
        private int cantidad = 0;
        private boolean registrado = false;
    }
}
