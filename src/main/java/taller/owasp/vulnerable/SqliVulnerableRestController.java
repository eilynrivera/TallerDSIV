package taller.owasp.vulnerable;

import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import taller.owasp.domain.User;
import taller.owasp.service.ProductService;
import taller.owasp.service.UserService;
import taller.owasp.shared.Scoreboard;
import taller.owasp.shared.SessionUser;

import static taller.owasp.shared.AtaquesInyeccion.*;

// M1 VULNERABLE: inyeccion SQL en el login y en la busqueda de productos.
// M2 VULNERABLE: el login responde con la entidad completa (contrasena incluida).
// Es el UNICO POST /api/login del perfil vulnerable.
// Marcador: cada ataque que funciona se registra como "exito" (ver shared/AtaquesInyeccion).
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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest datos, HttpSession session) {
        try {
            List<User> usuarios = userService.loginInseguro(datos.username(), datos.password());
            if (usuarios.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("mensaje", "Credenciales incorrectas"));
            }
            User u = usuarios.get(0);
            session.setAttribute(SessionUser.ATRIBUTO_SESION,
                    new SessionUser(u.getId(), u.getUsername(), u.getRole()));
            if (esInyeccionSql(datos.username()) || esInyeccionSql(datos.password())) {
                // Marcador: se entro con un payload, no con la contrasena.
                Scoreboard.registrar(M1, M1_LOGIN, EXITO);
            }
            // ERROR A PROPÓSITO (M2): se devuelve la entidad completa, contraseña incluida.
            return ResponseEntity.ok(u);
        } catch (Exception e) {
            // ERROR A PROPÓSITO (M1): se muestra el error de la base al cliente.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/products")
    public ResponseEntity<?> buscar(@RequestParam(defaultValue = "") String q) {
        String ataque = ataqueDeBusqueda(q);
        try {
            List<Map<String, Object>> filas = productService.buscarInseguro(q);
            if ((M1_UNION.equals(ataque) || M1_CTF.equals(ataque)) && !filas.isEmpty()) {
                // Marcador: el UNION trajo datos de otra tabla.
                Scoreboard.registrar(M1, ataque, EXITO);
            }
            return ResponseEntity.ok(filas);
        } catch (Exception e) {
            if (ataque != null) {
                // Marcador: la comilla rompio la consulta y el error llega al cliente.
                Scoreboard.registrar(M1, M1_COMILLA, EXITO);
            }
            // ERROR A PROPÓSITO (M1): con una comilla sola se ve el error de SQL.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // Cuerpo de POST /api/login: {"username": "...", "password": "..."}
    public record LoginRequest(String username, String password) {}
}
