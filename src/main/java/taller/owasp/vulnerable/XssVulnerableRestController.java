package taller.owasp.vulnerable;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import taller.owasp.domain.Comment;
import taller.owasp.service.CommentService;
import taller.owasp.shared.Scoreboard;

import static taller.owasp.shared.AtaquesInyeccion.*;

// M3 VULNERABLE: los comentarios se guardan sin filtrar (XSS almacenado).
@RestController
@Profile("vulnerable")
@RequestMapping("/api/comments")
public class XssVulnerableRestController {

    private final CommentService commentService;

    public XssVulnerableRestController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public List<Comment> listar() {
        return commentService.findAllByOrderByIdDesc();
    }

    @PostMapping
    public Comment crear(@RequestBody ComentarioRequest datos) {
        // ERROR A PROPÓSITO: se guarda el contenido tal cual, con <script>, <img onerror>, etc.
        Comment guardado = commentService.guardar(datos.author(), datos.content());
        if (esXss(datos.content()) || esXss(datos.author())) {
            // Marcador: el HTML quedo guardado intacto y el frontend lo pintara con innerHTML.
            Scoreboard.registrar(M3, M3_XSS, EXITO);
        }
        return guardado;
    }

    // Cuerpo de POST /api/comments: {"author": "...", "content": "..."}
    public record ComentarioRequest(String author, String content) {}
}
