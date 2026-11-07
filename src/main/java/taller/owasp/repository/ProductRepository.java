package taller.owasp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import taller.owasp.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Integer>{
    List<Product> findByNameContainingIgnoreCase(String name);
}
