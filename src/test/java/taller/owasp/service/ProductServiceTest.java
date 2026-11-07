package taller.owasp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import jakarta.persistence.PersistenceException;
import taller.owasp.entity.Product;
import taller.owasp.entity.SecretoCtf;
import taller.owasp.entity.User;
import taller.owasp.repository.ProductRepository;
import taller.owasp.repository.SecretoCtfRepository;
import taller.owasp.repository.UserRepository;

// @DataJpaTest levanta solo JPA + una base H2 en memoria vacia (sin Seed ni controladores).
// Cada test corre en una transaccion que se deshace al terminar.
@DataJpaTest
@Import(ProductService.class)
class ProductServiceTest {

    private static final String UNION_USUARIOS =
            "' UNION SELECT id, username, password, 0 FROM users --";

    @Autowired ProductService productService;
    @Autowired ProductRepository productRepository;
    @Autowired UserRepository userRepository;
    @Autowired SecretoCtfRepository secretoCtfRepository;

    @BeforeEach
    void datos() {
        productRepository.saveAllAndFlush(List.of(
                new Product("Laptop", "Laptop de 14 pulgadas", 750.0),
                new Product("Mouse", "Mouse inalámbrico", 15.0),
                new Product("Teclado", "Teclado mecánico", 60.0)));
        userRepository.saveAndFlush(new User("admin", "admin123", null, "ADMIN"));
        secretoCtfRepository.saveAndFlush(new SecretoCtf("FLAG{test}"));
    }

    @Nested
    @DisplayName("buscar (version segura, consulta parametrizada)")
    class Seguro {

        @Test
        void encuentraPorParteDelNombreSinImportarMayusculas() {
            assertThat(productService.buscar("LAP"))
                    .extracting(Product::getName)
                    .containsExactly("Laptop");
        }

        @Test
        void textoVacioDevuelveTodos() {
            assertThat(productService.buscar("")).hasSize(3);
        }

        @Test
        void unaComillaNoRompeLaConsulta() {
            assertThat(productService.buscar("'")).isEmpty();
        }

        @Test
        void elUnionSeTrataComoTextoYNoDevuelveUsuarios() {
            assertThat(productService.buscar(UNION_USUARIOS)).isEmpty();
        }
    }

    @Nested
    @DisplayName("buscarInseguro (version vulnerable, SQL concatenado)")
    class Vulnerable {

        @Test
        void busquedaNormalFuncionaIgualQueLaSegura() {
            List<Map<String, Object>> filas = productService.buscarInseguro("LAP");

            assertThat(filas).hasSize(1);
            assertThat(filas.get(0))
                    .containsEntry("name", "Laptop")
                    .containsKeys("id", "description", "price");
        }

        @Test
        void unaComillaProvocaErrorDeSqlConLaConsultaEnElMensaje() {
            assertThatThrownBy(() -> productService.buscarInseguro("'"))
                    .isInstanceOf(PersistenceException.class)
                    .hasMessageContaining("ILIKE '%'%'");
        }

        @Test
        void elUnionFiltraUsuariosYContrasenas() {
            List<Map<String, Object>> filas = productService.buscarInseguro(UNION_USUARIOS);

            // El username aparece en "name" y la contrasena en "description".
            assertThat(filas).anySatisfy(fila -> assertThat(fila)
                    .containsEntry("name", "admin")
                    .containsEntry("description", "admin123"));
        }

        @Test
        void retoCtfPaso1ListaLasTablasDesdeInformationSchema() {
            List<Map<String, Object>> filas = productService.buscarInseguro(
                    "' UNION SELECT 1, TABLE_NAME, '', 0 FROM INFORMATION_SCHEMA.TABLES "
                    + "WHERE TABLE_SCHEMA='PUBLIC' --");

            assertThat(filas).extracting(f -> f.get("name"))
                    .contains("USERS", "PRODUCTS", "COMMENTS", "SECRETO_CTF");
        }

        @Test
        void retoCtfPaso2LeeLaBandera() {
            List<Map<String, Object>> filas = productService.buscarInseguro(
                    "' UNION SELECT id, flag, '', 0 FROM secreto_ctf --");

            assertThat(filas).extracting(f -> f.get("name")).contains("FLAG{test}");
        }
    }
}
