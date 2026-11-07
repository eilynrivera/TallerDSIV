package taller.owasp.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import taller.owasp.entity.Product;
import taller.owasp.repository.ProductRepository;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> buscar(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }
}
