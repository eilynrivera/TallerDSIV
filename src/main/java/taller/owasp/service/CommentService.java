package taller.owasp.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import taller.owasp.domain.Comment;
import taller.owasp.repository.CommentRepository;

@Service
@Transactional
public class CommentService {
    private final CommentRepository repository;

    public CommentService(CommentRepository repository) {
        this.repository = repository;
    }

    public List<Comment> findAllByOrderByIdDesc() {
        return repository.findAllByOrderByIdDesc();
    }

    public Comment guardar(String author, String content) {
        return repository.save(new Comment(author, content));
    }
}
