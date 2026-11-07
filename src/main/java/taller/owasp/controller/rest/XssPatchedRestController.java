package taller.owasp.controller.rest;

import java.util.List;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
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

    public record ComentarioRequest(String author, String content) {}

    @GetMapping
    public List<Comment> listar() {
        return commentService.findAllByOrderByIdDesc();
    }

    @PostMapping
    public Comment crear(@RequestBody ComentarioRequest datos) {
        String contenido = cspSolo ? datos.content() : SIN_ETIQUETAS.sanitize(datos.content());
        String autor = SIN_ETIQUETAS.sanitize(datos.author());
        return commentService.guardar(autor, contenido);
    }
}
