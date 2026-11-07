package taller.owasp.shared;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Endpoints base de sesion, comunes a vulnerable y patched (Persona C):
 *
 *  - GET  /api/profile : devuelve el SessionUser si hay sesion, 401 si no.
 *  - POST /api/logout  : invalida la sesion.
 *
 * El atributo "user" (un SessionUser) lo escriben los controladores de login de
 * M1 / M2. Este controlador solo lo lee; no activa el login de Spring Security.
 */
@RestController
@Profile({"vulnerable", "patched"})
public class SessionController {

    static final String ATRIBUTO_USUARIO = "user";

    @GetMapping("/api/profile")
    public ResponseEntity<?> perfil(HttpServletRequest request) {

        HttpSession sesion = request.getSession(false);
        Object usuario = (sesion != null) ? sesion.getAttribute(ATRIBUTO_USUARIO) : null;

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(usuario);
    }

    @PostMapping("/api/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {

        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }

        return ResponseEntity.noContent().build();
    }
}
