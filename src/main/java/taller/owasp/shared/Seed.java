package taller.owasp.shared;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import taller.owasp.domain.Comment;
import taller.owasp.domain.Product;
import taller.owasp.domain.SecretoCtf;
import taller.owasp.domain.CommentRepository;
import taller.owasp.domain.ProductRepository;
import taller.owasp.domain.SecretoCtfRepository;

// Llena la base H2 en memoria cada vez que arranca el servidor.
// Reiniciar el servidor equivale a restaurar la base.
@Component
@Profile({"vulnerable", "patched"})   // en "evil" no se llena la base
public class Seed implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final CommentRepository commentRepository;
    private final SecretoCtfRepository secretoCtfRepository;

    public Seed(ProductRepository productRepository,
                CommentRepository commentRepository,
                SecretoCtfRepository secretoCtfRepository) {
        this.productRepository = productRepository;
        this.commentRepository = commentRepository;
        this.secretoCtfRepository = secretoCtfRepository;
    }

    @Override
    public void run(String... args) {
        // --- Productos (A) ---
        productRepository.saveAll(List.of(
            new Product("Laptop", "Laptop de 14 pulgadas", 750.0),
            new Product("Mouse", "Mouse inalámbrico", 15.0),
            new Product("Teclado", "Teclado mecánico", 60.0),
            new Product("Monitor", "Monitor de 24 pulgadas", 180.0),
            new Product("Audífonos", "Audífonos con micrófono", 35.0),
            new Product("Cámara web", "Cámara HD 1080p", 40.0),
            new Product("Memoria USB", "USB de 64 GB", 12.0),
            new Product("Disco SSD", "SSD de 1 TB", 90.0)
        ));

        // --- Comentarios (A) ---
        commentRepository.saveAll(List.of(
            new Comment("maria", "¡Muy buena tienda!"),
            new Comment("juan", "El envío fue rápido."),
            new Comment("admin", "Bienvenidos al taller.")
        ));

        // --- Reto CTF (A) ---
        // Pista: la base tiene un catálogo de sus propias tablas, INFORMATION_SCHEMA.
        secretoCtfRepository.save(new SecretoCtf("FLAG{union_select_information_schema}"));

        // --- Usuarios (B) ---
        // Esta sección la escribe B: texto plano en vulnerable, BCrypt en patched.
    }
}
