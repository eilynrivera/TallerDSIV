package taller.owasp.controller.rest.dto;

// Cuerpo de POST /api/login: {"username": "...", "password": "..."}
// Lo comparten la versión vulnerable y la segura.
public record LoginRequest(String username, String password) {}
