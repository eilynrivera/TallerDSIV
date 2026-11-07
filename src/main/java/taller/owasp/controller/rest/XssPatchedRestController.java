package taller.owasp.controller.rest;

import java.util.List;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import taller.owasp.entity.Comment;
import taller.owasp.service.CommentService;

@RestController
@Profile("patched")
@RequestMapping("/api/comments")
public class XssPatchedRestController {

    private static final PolicyFactory SIN_ETIQUETAS = new HtmlPolicyBuilder().toFactory();

    private final CommentService commentService;
    private final boolean cspSolo;

    public XssPatchedRestController(CommentService commentService,
                         @Value("${lab.demo.csp-solo:false}") boolean cspSolo) {
        this.commentService = commentService;
        this.cspSolo = cspSolo;
    }

    @GetMapping
    public List<Comment> listar() {
        return commentService.findAllByOrderByIdDesc();
    }

    // Mismo límite que la columna content (@Column(length = 2000)).
    private static final int MAX_CONTENIDO = 2000;

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody ComentarioRequest datos) {
        // Validar la entrada antes de guardarla: vacío o demasiado largo se rechaza con 400
        // en lugar de terminar en un error 500 de la base.
        if (datos.content() == null || datos.content().isBlank()
                || datos.content().length() > MAX_CONTENIDO) {
            return ResponseEntity.badRequest().body("El comentario debe tener entre 1 y " + MAX_CONTENIDO + " caracteres");
        }
        String contenido = cspSolo ? datos.content() : SIN_ETIQUETAS.sanitize(datos.content());
        String autor = SIN_ETIQUETAS.sanitize(datos.author());
        return ResponseEntity.ok(commentService.guardar(autor, contenido));
    }

    // Cuerpo de POST /api/comments: {"author": "...", "content": "..."}
    public record ComentarioRequest(String author, String content) {}
}
