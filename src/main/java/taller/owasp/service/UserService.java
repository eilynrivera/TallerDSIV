package taller.owasp.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import taller.owasp.entity.User;
import taller.owasp.repository.UserRepository;

@Service
@Transactional
public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    List<User> findByUsernameAndPassword(String username, String password){
        return repository.findByUsernameAndPassword(username, password);
    }
    
    List<User> findByUsername(String username){
        return repository.findByUsername(username);
    }
}
