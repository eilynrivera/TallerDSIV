package taller.owasp.shared;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import taller.owasp.domain.User;
import taller.owasp.domain.UserRepository;

/**
 * Carga de usuarios de prueba.
 *
 * NOTA (unificacion a JPA - Jose/Persona C, acordado con el equipo):
 * reescrito de JdbcTemplate + DDL manual a Spring Data JPA (UserRepository + entidad
 * User). Antes vivia en el paquete demo/ con un DataSource propio (demoDataSource);
 * ahora usa el unico datasource de la app y la tabla "users" la crea la entidad User
 * (con la columna email). Esto evita el choque de datasources/tablas que habia al
 * mezclar. La logica es la misma: vulnerable guarda en claro, seguro con BCrypt.
 */
@Component
public class UserSeed {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserSeed(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void inicializarVulnerable() {
        userRepository.deleteAll();
        userRepository.save(new User("admin", "admin123", null, "ADMIN"));
        userRepository.save(new User("maria", "sunshine", null, "USER"));
        userRepository.save(new User("juan", "qwerty2024", null, "USER"));
    }

    public void inicializarSeguro() {
        userRepository.deleteAll();
        userRepository.save(new User("admin", passwordEncoder.encode("admin123"), null, "ADMIN"));
        userRepository.save(new User("maria", passwordEncoder.encode("sunshine"), null, "USER"));
        userRepository.save(new User("juan", passwordEncoder.encode("qwerty2024"), null, "USER"));
    }
}
