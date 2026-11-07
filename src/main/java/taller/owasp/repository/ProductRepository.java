package taller.owasp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import taller.owasp.domain.Product;

public interface ProductRepository extends JpaRepository<Product, Long>{
    List<Product> findByNameContainingIgnoreCase(String name);
}
