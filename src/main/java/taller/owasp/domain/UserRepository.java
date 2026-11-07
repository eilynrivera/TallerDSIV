package taller.owasp.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    /** Usado por la version segura de M2 (login con BCrypt): busca por usuario y luego compara el hash. */
    Optional<User> findByUsername(String username);

    /** Usado por la version segura de M1: consulta parametrizada en vez de concatenar SQL. */
    Optional<User> findByUsernameAndPassword(String username, String password);
}
