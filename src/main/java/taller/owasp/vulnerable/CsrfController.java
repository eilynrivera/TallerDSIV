package taller.owasp.vulnerable;

import java.util.Map;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import taller.owasp.domain.User;
import taller.owasp.repository.UserRepository;
import taller.owasp.shared.SessionUser;

/**
 * M5 VULNERABLE (perfil vulnerable). Cambio de correo de la cuenta.
 *
 * Solo comprueba que haya sesion: no exige token CSRF. Como la cookie es
 * SameSite=None (M4), el navegador la envia aunque la peticion venga de otro sitio,
 * asi que el formulario oculto de premio.html (127.0.0.1:4000) cambia el correo de
 * la victima sin su consentimiento.
 *
 * Recibe @RequestParam (form-urlencoded), no JSON, porque el form del atacante no
 * puede enviar JSON.
 */
@RestController
@Profile("vulnerable")
@RequestMapping("/api/account")
public class CsrfController {

    private final UserRepository userRepository;

    public CsrfController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/email")
    public ResponseEntity<?> verEmail(HttpServletRequest http) {
        User user = usuarioDeLaSesion(http);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(Map.of("username", user.getUsername(),
                "email", user.getEmail() == null ? "" : user.getEmail()));
    }

    @PostMapping("/email")
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
