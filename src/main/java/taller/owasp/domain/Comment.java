package taller.owasp.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;

/**
 * Entidad JPA de comentario (tabla "comments"). Es el vector de M3 (XSS almacenado):
 * el content se guarda tal cual en el perfil vulnerable y el frontend lo pinta, asi que
 * un contenido con etiquetas HTML/JS se ejecuta al mostrarlo.
 */
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String author;
    // Limite de 2000 caracteres; el controlador patched valida contra este mismo tope.
    @Column(length = 2000)
    private String content;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Comment() {}

    public Comment(String author, String content) {
        this.author = author;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
