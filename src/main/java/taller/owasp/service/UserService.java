package taller.owasp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import taller.owasp.domain.User;
import taller.owasp.repository.UserRepository;

/**
 * Logica de acceso a usuarios para el login (M1 SQLi / M2 datos sensibles). Convive a
 * proposito un metodo seguro y uno inseguro para contrastarlos en el taller: el perfil
 * vulnerable llama a loginInseguro(...) y el patched a findByUsername(...).
 */
@Service
@Transactional
public class UserService {
    private final UserRepository repository;
    // EntityManager solo lo usa la version vulnerable para armar SQL nativo a mano.
    private final EntityManager entityManager;

    public UserService(UserRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    // SEGURO (M1 + M2): consulta parametrizada; la contrasena se verifica con BCrypt
    // en el controlador patched.
    public Optional<User> findByUsername(String username) {
        return repository.findByUsername(username);
    }

    // VULNERABLE (M1): EL HUECO. Concatena el username y el password directo dentro del
    // SQL, sin parametros. Con username = admin' -- el resto del WHERE (la comprobacion
    // de la contrasena) queda comentado y se entra sin conocerla. Una comilla sola
    // rompe la consulta y expone el error de SQL. La correccion es findByUsername(...),
    // que deja que JPA parametrice y trate el texto como dato, no como codigo.
    @SuppressWarnings("unchecked")
    public List<User> loginInseguro(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = '" + username
                   + "' AND password = '" + password + "'";
        return entityManager.createNativeQuery(sql, User.class).getResultList();
    }
}
