package taller.owasp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import taller.owasp.domain.Product;

// Repositorio de productos (Spring Data JPA).
public interface ProductRepository extends JpaRepository<Product, Long>{
    // Busqueda SEGURA de M1: Spring Data genera una consulta parametrizada
    // (WHERE name LIKE ? ignorando mayusculas); el texto del usuario viaja como
    // parametro, nunca concatenado. La usa el perfil patched.
    List<Product> findByNameContainingIgnoreCase(String name);
}
