package taller.owasp.patched;

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
 * Marcador de M4 (CORS) en el perfil PATCHED. El CORS solo permite el propio origen de
 * la tienda, asi que el sitio atacante (127.0.0.1:4000) NO esta en la lista: aunque el
 * servidor procese la peticion, el navegador bloquea la lectura de la respuesta. Se
 * registra "bloqueado" para la misma lectura cross-site que en vulnerable da "exito".
 */
@Component
@Profile("patched")
public class MarcadorCorsFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (AtaquesTaller.esLecturaCruzadaDeDatos(request)) {
            Scoreboard.registrar(AtaquesTaller.M4, AtaquesTaller.M4_CORS, AtaquesInyeccion.BLOQUEADO);
        }
        filterChain.doFilter(request, response);
    }
}
