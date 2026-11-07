package taller.owasp.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /** Usado por la version segura de M1: busqueda parametrizada, sin concatenar SQL. */
    List<Product> findByNameContainingIgnoreCase(String name);
}
