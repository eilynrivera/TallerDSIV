package taller.owasp.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import taller.owasp.entity.User;
import taller.owasp.repository.UserRepository;

@Service
@Transactional
public class UserService {
    private final UserRepository repository;
    private final EntityManager entityManager;

    public UserService(UserRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    // SEGURO (M1 corregido): consulta parametrizada.
    public List<User> findByUsernameAndPassword(String username, String password) {
        return repository.findByUsernameAndPassword(username, password);
    }

    // Lo usará B en M2 (login con BCrypt).
    public List<User> findByUsername(String username) {
        return repository.findByUsername(username);
    }

    // VULNERABLE (M1): con username = admin' -- el resto del WHERE queda comentado
    // y se entra sin saber la contraseña.
    @SuppressWarnings("unchecked")
    public List<User> loginInseguro(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = '" + username
                   + "' AND password = '" + password + "'";
        return entityManager.createNativeQuery(sql, User.class).getResultList();
    }
}
