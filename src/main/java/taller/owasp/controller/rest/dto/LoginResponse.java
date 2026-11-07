package taller.owasp.controller.rest.dto;

// Respuesta del login seguro (M2 / B4): solo lo necesario, nunca la contrasena.
public record LoginResponse(Long id, String username, String role) {}
