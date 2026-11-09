package taller.owasp.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import taller.owasp.domain.Comment;
import taller.owasp.repository.CommentRepository;

/**
 * Logica de comentarios (M3 XSS almacenado). Aqui no se filtra ni se sanea: solo
 * persiste y lee. El saneo y la defensa de XSS viven en los controladores (el vulnerable
 * guarda tal cual; el patched sanea antes de llamar a guardar(...)) y en el frontend.
 */
@Service
@Transactional
public class CommentService {
    private final CommentRepository repository;

    public CommentService(CommentRepository repository) {
        this.repository = repository;
    }

    // Lista los comentarios del mas nuevo al mas viejo (orden que espera la vista).
    public List<Comment> findAllByOrderByIdDesc() {
        return repository.findAllByOrderByIdDesc();
    }

    // Crea y guarda un comentario con el autor y contenido ya recibidos del controlador.
    public Comment guardar(String author, String content) {
        return repository.save(new Comment(author, content));
    }
}
