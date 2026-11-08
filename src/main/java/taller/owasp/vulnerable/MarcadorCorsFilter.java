package taller.owasp.vulnerable;

import java.io.IOException;

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
 * Marcador de M4 (CORS) en el perfil VULNERABLE. Como el CORS refleja cualquier origen,
 * el sitio atacante (127.0.0.1:4000) SI puede leer /api/profile y /api/comments de la
 * victima: se registra "exito". Solo cuenta cuando la peticion viene de otro sitio
 * (cabecera Origin distinta), no el uso normal de la tienda.
 */
@Component
@Profile("vulnerable")
public class MarcadorCorsFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (AtaquesTaller.esLecturaCruzadaDeDatos(request)) {
            Scoreboard.registrar(AtaquesTaller.M4, AtaquesTaller.M4_CORS, AtaquesInyeccion.EXITO);
        }
        filterChain.doFilter(request, response);
    }
}
