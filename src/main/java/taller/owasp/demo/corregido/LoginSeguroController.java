package taller.owasp.demo.corregido;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/corregido")
@Profile("corregido")
public class LoginSeguroController {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public LoginSeguroController(
            @Qualifier("demoJdbcTemplate") JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder) {

        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        List<Map<String, Object>> usuarios = jdbcTemplate.queryForList(
                """
                SELECT id, username, password, role
                FROM users
                WHERE username = ?
                """,
                request.username()
        );

        if (usuarios.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "Credenciales incorrectas"));
        }

        Map<String, Object> usuario = usuarios.get(0);

        String passwordGuardada = (String) usuario.get("PASSWORD");

        if (!passwordEncoder.matches(request.password(), passwordGuardada)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "Credenciales incorrectas"));
        }

        return ResponseEntity.ok(
                new LoginResponse(
                        ((Number) usuario.get("ID")).longValue(),
                        (String) usuario.get("USERNAME"),
                        (String) usuario.get("ROLE")
                )
        );
    }

    public record LoginRequest(String username, String password) {
    }

    public record LoginResponse(Long id, String username, String role) {
    }
}