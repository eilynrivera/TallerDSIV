package taller.owasp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import taller.owasp.domain.Comment;

// Repositorio de comentarios (Spring Data JPA).
public interface CommentRepository extends JpaRepository<Comment, Long>{
    // Devuelve los comentarios del mas reciente al mas antiguo (id descendente).
    List<Comment> findAllByOrderByIdDesc();
}
