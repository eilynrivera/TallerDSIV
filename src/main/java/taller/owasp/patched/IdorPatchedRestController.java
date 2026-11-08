package taller.owasp.patched;

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

// EXTRA (A11) CORREGIDO: el id de la URL debe coincidir con el id de la sesion.
// Si no coincide responde 403, y nunca devuelve la contrasena (DTO).
@RestController
@Profile("patched")
@RequestMapping("/api/users")
public class IdorPatchedRestController {

    private final UserRepository userRepository;

    public IdorPatchedRestController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> verUsuario(@PathVariable Long id, HttpSession session) {
        if (!(session.getAttribute(SessionUser.ATRIBUTO_SESION) instanceof SessionUser actual)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "Inicia sesión primero"));
        }
        // LA CORRECCIÓN: autorizacion por objeto, no solo autenticacion.
        if (!id.equals(actual.id())) {
            Scoreboard.registrar(IDOR, IDOR_PERFIL, BLOQUEADO);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("mensaje", "No puedes ver los datos de otro usuario"));
        }
        return userRepository.findById(id)
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(
                        new UsuarioResponse(u.getId(), u.getUsername(), u.getEmail(), u.getRole())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Solo lo necesario, nunca la contrasena.
    public record UsuarioResponse(Long id, String username, String email, String role) {}
}
