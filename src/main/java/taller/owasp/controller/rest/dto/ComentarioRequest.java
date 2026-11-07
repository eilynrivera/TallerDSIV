package taller.owasp.controller.rest.dto;

// Cuerpo de POST /api/comments: {"author": "...", "content": "..."}
// Lo comparten la versión vulnerable y la segura.
public record ComentarioRequest(String author, String content) {}
