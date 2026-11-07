package taller.owasp.patched;

import java.util.Map;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import taller.owasp.domain.User;
import taller.owasp.repository.UserRepository;
import taller.owasp.shared.SessionUser;

/**
 * M5 SEGURO (perfil patched). Cambio de correo protegido con CSRF.
 *
 * Spring Security exige el token CSRF para POST /api/account/** (ver SecurityConfig),
 * y la cookie de sesion es SameSite=Lax, asi que el formulario oculto de premio.html
 * (otro sitio) no lleva token ni cookie util y recibe 403.
 *
 * GET /api/csrf entrega el token para que el frontend legitimo lo mande en la
 * cabecera X-XSRF-TOKEN.
 */
@RestController
@Profile("patched")
public class CsrfController {

    private final UserRepository userRepository;

    public CsrfController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Devuelve el token CSRF y hace que se escriba la cookie XSRF-TOKEN.
    @GetMapping("/api/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }

    @GetMapping("/api/account/email")
    public ResponseEntity<?> verEmail(HttpServletRequest http) {
        User user = usuarioDeLaSesion(http);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(Map.of("username", user.getUsername(),
                "email", user.getEmail() == null ? "" : user.getEmail()));
    }

    @PostMapping("/api/account/email")
    public ResponseEntity<?> cambiarEmail(@RequestParam String email, HttpServletRequest http) {
        User user = usuarioDeLaSesion(http);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        user.setEmail(email);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("mensaje", "Correo actualizado", "email", email));
    }

    private User usuarioDeLaSesion(HttpServletRequest http) {
        HttpSession sesion = http.getSession(false);
        if (sesion == null) {
            return null;
        }
        Object attr = sesion.getAttribute(SessionUser.ATRIBUTO_SESION);
        if (!(attr instanceof SessionUser u)) {
            return null;
        }
        Optional<User> encontrado = userRepository.findById(u.id());
        return encontrado.orElse(null);
    }
}
