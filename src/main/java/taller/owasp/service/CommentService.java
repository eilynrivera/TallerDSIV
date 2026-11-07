package taller.owasp.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import taller.owasp.entity.Comment;
import taller.owasp.repository.CommentRepository;

@Service
@Transactional
public class CommentService {
    public final CommentRepository repository;

    public CommentService(CommentRepository repository) {
        this.repository = repository;
    }

    List<Comment> findAllByOrderByIdDesc(){
        return repository.findAllByOrderByIdDesc();
    }
    
}
