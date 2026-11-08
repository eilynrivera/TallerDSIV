package taller.owasp.patched;

import java.io.IOException;

import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import taller.owasp.shared.AtaquesInyeccion;
import taller.owasp.shared.AtaquesTaller;
import taller.owasp.shared.Scoreboard;

/**
 * Marcador de M5 (CSRF) en el perfil PATCHED. Cuando Spring Security rechaza un POST a
 * /api/account/** por falta del token CSRF (el formulario del sitio atacante no lo
 * lleva), registra "bloqueado" y luego responde el 403 habitual. El cambio de correo
 * legitimo del frontend si trae token, no pasa por aqui y no ensucia el marcador.
 */
public class CsrfMarcadorAccessDeniedHandler implements AccessDeniedHandler {

    private final AccessDeniedHandler delegado = new AccessDeniedHandlerImpl();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException ex)
            throws IOException, ServletException {

        String path = request.getServletPath();
        if ("POST".equalsIgnoreCase(request.getMethod())
                && path != null && path.startsWith("/api/account")) {
            Scoreboard.registrar(AtaquesTaller.M5, AtaquesTaller.M5_CSRF, AtaquesInyeccion.BLOQUEADO);
        }
        delegado.handle(request, response, ex);
    }
}
