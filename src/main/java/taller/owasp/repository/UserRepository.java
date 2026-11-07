package taller.owasp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import taller.owasp.domain.User;

// username es unico (ver User), por eso las busquedas devuelven Optional.
public interface UserRepository extends JpaRepository<User, Long> {

    // M2 corregido (B4): se busca solo por usuario y la contrasena se compara con BCrypt.
    Optional<User> findByUsername(String username);

    // M1 corregido, paso intermedio: consulta parametrizada pero con contrasena en claro.
    // Hoy no la usa ningun controlador (M2 la reemplazo); se deja para soluciones/01-sqli.md.
    Optional<User> findByUsernameAndPassword(String username, String password);
}
