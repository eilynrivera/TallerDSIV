package taller.owasp.controller.rest;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import taller.owasp.entity.User;
import taller.owasp.service.ProductService;
import taller.owasp.service.UserService;
import taller.owasp.shared.SessionUser;

@RestController
@Profile("vulnerable")
@RequestMapping("/api")
public class SqliVulnerableRestController {

    private final UserService userService;
    private final ProductService productService;

    public SqliVulnerableRestController(UserService userService, ProductService productService) {
        this.userService = userService;
        this.productService = productService;
    }

    public record LoginRequest(String username, String password) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest datos, HttpSession session) {
        try {
            List<User> usuarios = userService.loginInseguro(datos.username(), datos.password());
            if (usuarios.isEmpty()) {
                return ResponseEntity.status(401).body("Usuario o contraseña incorrectos");
            }
            User u = usuarios.get(0);
            session.setAttribute("user", new SessionUser(u.getId(), u.getUsername(), u.getRole()));
            // ERROR A PROPÓSITO (M2): se devuelve la entidad completa, contraseña incluida.
            return ResponseEntity.ok(u);
        } catch (Exception e) {
            // ERROR A PROPÓSITO: se muestra el error de la base al cliente.
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/products")
    public ResponseEntity<?> buscar(@RequestParam(defaultValue = "") String q) {
        try {
            return ResponseEntity.ok(productService.buscarInseguro(q));
        } catch (Exception e) {
            // ERROR A PROPÓSITO: con una comilla sola se ve el error de SQL.
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}
