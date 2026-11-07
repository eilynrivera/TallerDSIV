package taller.owasp.controller.rest;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import taller.owasp.entity.Comment;
import taller.owasp.service.CommentService;

// M3 VULNERABLE: los comentarios se guardan sin filtrar (XSS almacenado).
@RestController
@Profile("vulnerable")
@RequestMapping("/api/comments")
public class XssVulnerableRestController {

    private final CommentService commentService;

    public XssVulnerableRestController(CommentService commentService) {
        this.commentService = commentService;
    }

    // Cuerpo del comentario: {"author": "...", "content": "..."}
    public record ComentarioRequest(String author, String content) {}

    @GetMapping
    public List<Comment> listar() {
        return commentService.findAllByOrderByIdDesc();
    }

    @PostMapping
    public Comment crear(@RequestBody ComentarioRequest datos) {
        // ERROR A PROPÓSITO: se guarda el contenido tal cual, con <script>, <img onerror>, etc.
        return commentService.guardar(datos.author(), datos.content());
    }
}
