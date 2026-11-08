package taller.owasp.vulnerable;

import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import taller.owasp.repository.UserRepository;
import taller.owasp.shared.Scoreboard;
import taller.owasp.shared.SessionUser;

import static taller.owasp.shared.AtaquesInyeccion.*;

// EXTRA (A11) VULNERABLE: IDOR (Insecure Direct Object Reference).
// Comprueba que HAYA sesion, pero no que el id pedido sea el del usuario de la sesion:
// maria (id 2) cambia la URL a /api/users/1 y lee los datos de admin.
@RestController
@Profile("vulnerable")
@RequestMapping("/api/users")
public class IdorVulnerableRestController {

    private final UserRepository userRepository;

    public IdorVulnerableRestController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> verUsuario(@PathVariable Long id, HttpSession session) {
        if (!(session.getAttribute(SessionUser.ATRIBUTO_SESION) instanceof SessionUser actual)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "Inicia sesión primero"));
        }
        // ERROR A PROPÓSITO: falta comprobar que id == actual.id().
        return userRepository.findById(id)
                .<ResponseEntity<?>>map(u -> {
                    if (!u.getId().equals(actual.id())) {
                        Scoreboard.registrar(IDOR, IDOR_PERFIL, EXITO);
                    }
                    return ResponseEntity.ok(u);   // entidad completa, como el login de M1/M2
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
