package taller.owasp.patched;

import java.io.IOException;

import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
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
 * Marcador de los ataques cross-site (M4 CORS y M5 CSRF) en el perfil PATCHED.
 *
 * Debe correr ANTES de Spring Security: su CorsFilter rechaza el origen no permitido
 * (el sitio atacante, 4000) con 403 y corta la cadena, asi que un filtro normal nunca
 * veria la peticion. Con @Order(HIGHEST_PRECEDENCE) se registra el "bloqueado" y luego
 * la cadena sigue hasta que CORS la rechaza. Ambos ataques se bloquean en el servidor:
 *
 *   - M4: lectura cross-site de /api/profile o /api/comments.
 *   - M5: POST cross-site a /api/account/email (el cambio de correo de premio.html).
 *
 * El uso normal de la tienda es del mismo origen (sin cabecera Origin cruzada) y no
 * cuenta; los .http (REST Client) tampoco mandan Origin.
 */
@Component
@Profile("patched")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MarcadorCorsFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (AtaquesTaller.esLecturaCruzadaDeDatos(request)) {
            Scoreboard.registrar(AtaquesTaller.M4, AtaquesTaller.M4_CORS, AtaquesInyeccion.BLOQUEADO);
        } else if ("POST".equalsIgnoreCase(request.getMethod())
                && "/api/account/email".equals(request.getServletPath())
                && AtaquesTaller.esCruzada(request)) {
            Scoreboard.registrar(AtaquesTaller.M5, AtaquesTaller.M5_CSRF, AtaquesInyeccion.BLOQUEADO);
        }
        filterChain.doFilter(request, response);
    }
}
