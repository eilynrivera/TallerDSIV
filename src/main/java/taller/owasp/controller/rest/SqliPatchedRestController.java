package taller.owasp.controller.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import taller.owasp.entity.Product;
import taller.owasp.entity.User;
import taller.owasp.service.ProductService;
import taller.owasp.service.UserService;
import taller.owasp.shared.SessionUser;


@RestController
@Profile("patched")
@RequestMapping("/api")
public class SqliPatchedRestController {

    private static final Logger log = LoggerFactory.getLogger(SqliPatchedRestController.class);

    private final UserService userService;
    private final ProductService productService;

    public SqliPatchedRestController(UserService userService, ProductService productService) {
        this.userService = userService;
        this.productService = productService;
    }

    public record LoginRequest(String username, String password) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest datos, HttpSession session) {
        List<User> usuarios = userService.findByUsernameAndPassword(datos.username(), datos.password());
        if (usuarios.isEmpty()) {
            return ResponseEntity.status(401).body("Usuario o contraseña incorrectos");
        }
        User u = usuarios.get(0);
        session.setAttribute("user", new SessionUser(u.getId(), u.getUsername(), u.getRole()));
        return ResponseEntity.ok(u);
    }

    @GetMapping("/products")
    public List<Product> buscar(@RequestParam(defaultValue = "") String q) {
        return productService.buscar(q);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> manejarError(Exception e) {
        log.error("Error en SqliPatchedRestController", e);
        return ResponseEntity.status(500).body("Ocurrió un error. Intenta de nuevo.");
    }
}
