package taller.owasp.patched;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.Optional;

import taller.owasp.domain.User;
import taller.owasp.domain.UserRepository;
import taller.owasp.shared.SessionUser;

/**
 * Login de la version SEGURA (POST /api/login, perfil patched).
 *
 * NOTA (unificacion - Jose/Persona C, acordado con el equipo): este archivo viene del
 * LoginSeguroController de B (paquete demo/corregido). Cambios:
 *   - Ruta /api/corregido/login -> /api/login (convencion del plan).
 *   - JdbcTemplate -> Spring Data JPA (findByUsername + passwordEncoder.matches).
 *   - Responde con un DTO {id, username, role} (sin contrasena) y guarda el SessionUser.
 * Es la tarea B4 del plan tal cual.
 *
 * NOTA PARA A (M1): este es el UNICO POST /api/login del perfil patched; no crees otro
 * con esa ruta. El parche de M1 es usar findBy... parametrizado como aqui.
 */
@RestController
@Profile("patched")
public class SqliController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SqliController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/api/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest http) {

        Optional<User> encontrado = userRepository.findByUsername(request.username());

        if (encontrado.isEmpty()
                || !passwordEncoder.matches(request.password(), encontrado.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "Credenciales incorrectas"));
        }

        User usuario = encontrado.get();

        http.getSession(true).setAttribute(
                SessionUser.ATRIBUTO_SESION,
                new SessionUser(usuario.getId(), usuario.getUsername(), usuario.getRole()));

        return ResponseEntity.ok(new LoginResponse(
                usuario.getId(), usuario.getUsername(), usuario.getRole()));
    }

    public record LoginRequest(String username, String password) {
    }

    public record LoginResponse(Long id, String username, String role) {
    }
}
