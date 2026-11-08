package taller.owasp.patched;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_INTENTOS = 5;
    private static final long VENTANA_MS = 60_000;

    private final ConcurrentHashMap<String, AttemptWindow> intentos = new ConcurrentHashMap<>();

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

    private static class AttemptWindow {
        private long inicio = System.currentTimeMillis();
        private int cantidad = 0;
    }
}
