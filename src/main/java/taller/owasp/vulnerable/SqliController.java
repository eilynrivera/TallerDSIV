package taller.owasp.vulnerable;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
 * Login de la version VULNERABLE (POST /api/login, perfil vulnerable).
 *
 * NOTA (unificacion - Jose/Persona C, acordado con el equipo): este archivo viene del
 * VulnerableLoginController de B (paquete demo/vulnerable). Cambios:
 *   - Ruta /api/vulnerable/login -> /api/login (convencion del plan).
 *   - JdbcTemplate -> Spring Data JPA (entidad User + UserRepository).
 *   - Guarda el SessionUser en la HttpSession (lo lee GET /api/profile).
 *
 * NOTA PARA A (M1 - SQLi): este es el UNICO POST /api/login del perfil vulnerable;
 * NO crees otro controller con esa ruta o Spring falla por mapeo ambiguo. Para la
 * demo de inyeccion, reemplaza la busqueda de abajo por una consulta NATIVA
 * concatenada (entityManager.createNativeQuery("... '" + username + "' ...")) y
 * agrega aqui tambien GET /api/products?q=. Hoy la busqueda es parametrizada, asi que
 * todavia no es inyectable: solo demuestra la fuga de datos (M2).
 */
@RestController
@Profile("vulnerable")
public class SqliController {

    private final UserRepository userRepository;

    public SqliController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/api/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest http) {

        Optional<User> encontrado =
                userRepository.findByUsernameAndPassword(request.username(), request.password());

        if (encontrado.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "Credenciales incorrectas"));
        }

        User usuario = encontrado.get();

        http.getSession(true).setAttribute(
                SessionUser.ATRIBUTO_SESION,
                new SessionUser(usuario.getId(), usuario.getUsername(), usuario.getRole()));

        // Vulnerabilidad (M2): se devuelve la entidad completa, incluida la contrasena.
        return ResponseEntity.ok(usuario);
    }

    public record LoginRequest(String username, String password) {
    }
}
