package taller.owasp.patched;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import taller.owasp.domain.Product;
import taller.owasp.domain.User;
import taller.owasp.service.ProductService;
import taller.owasp.service.UserService;
import taller.owasp.shared.SessionUser;

// M1 CORREGIDO: consultas parametrizadas (Spring Data) y errores genericos al cliente.
// M2 CORREGIDO (B4): contrasena verificada con BCrypt y respuesta sin contrasena.
// Es el UNICO POST /api/login del perfil patched.
@RestController
@Profile("patched")
@RequestMapping("/api")
public class SqliPatchedRestController {

    private static final Logger log = LoggerFactory.getLogger(SqliPatchedRestController.class);

    private final UserService userService;
    private final ProductService productService;
    private final PasswordEncoder passwordEncoder;   // bean de patched/PasswordConfig (B)

    public SqliPatchedRestController(UserService userService, ProductService productService,
                                     PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.productService = productService;
        this.passwordEncoder = passwordEncoder;
    }

    // Metodo de B (M2): findByUsername + passwordEncoder.matches + DTO.
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest datos, HttpSession session) {
        Optional<User> encontrado = userService.findByUsername(datos.username());

        if (encontrado.isEmpty()
                || !passwordEncoder.matches(datos.password(), encontrado.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "Credenciales incorrectas"));
        }

        User u = encontrado.get();
        session.setAttribute(SessionUser.ATRIBUTO_SESION,
                new SessionUser(u.getId(), u.getUsername(), u.getRole()));
        return ResponseEntity.ok(new LoginResponse(u.getId(), u.getUsername(), u.getRole()));
    }

    @GetMapping("/products")
    public List<Product> buscar(@RequestParam(defaultValue = "") String q) {
        return productService.buscar(q);
    }

    // Si algo falla, el detalle va al log y el cliente solo recibe un mensaje generico.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> manejarError(Exception e) {
        log.error("Error en SqliPatchedRestController", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ocurrió un error. Intenta de nuevo.");
    }

    // Cuerpo de POST /api/login: {"username": "...", "password": "..."}
    public record LoginRequest(String username, String password) {}

    // Respuesta del login seguro (M2): solo lo necesario, nunca la contrasena.
    public record LoginResponse(Long id, String username, String role) {}
}
