package taller.owasp.shared;

// Lo que se guarda en la sesión (atributo "user") después de un login correcto.
// Convención del equipo: todo el que necesite saber quién tiene sesión lee este atributo.
public record SessionUser(Long id, String username, String role) {}
