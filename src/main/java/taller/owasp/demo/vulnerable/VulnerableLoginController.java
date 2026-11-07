package taller.owasp.demo.vulnerable;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vulnerable")
@Profile("vulnerable")
public class VulnerableLoginController {

    private final JdbcTemplate jdbcTemplate;

    public VulnerableLoginController(
            @org.springframework.beans.factory.annotation.Qualifier("demoJdbcTemplate")
            JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        List<Map<String, Object>> usuarios = jdbcTemplate.queryForList(
                """
                SELECT id, username, password, role
                FROM users
                WHERE username = ? AND password = ?
                """,
                request.username(),
                request.password()
        );

        if (usuarios.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "Credenciales incorrectas"));
        }

        // Vulnerabilidad: devuelve todos los datos, incluida la contraseña.
        return ResponseEntity.ok(usuarios.get(0));
    }

    public record LoginRequest(String username, String password) {
    }
}