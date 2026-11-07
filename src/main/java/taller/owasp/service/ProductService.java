package taller.owasp.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import taller.owasp.domain.Product;
import taller.owasp.repository.ProductRepository;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final EntityManager entityManager;

    public ProductService(ProductRepository productRepository, EntityManager entityManager) {
        this.productRepository = productRepository;
        this.entityManager = entityManager;
    }

    // SEGURO (M1 corregido): Spring Data genera una consulta parametrizada.
    public List<Product> buscar(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    // VULNERABLE (M1): el texto del usuario se pega dentro del SQL.
    // Con q = ' UNION SELECT ... se pueden leer otras tablas.
    // Las columnas se escriben en orden fijo para que el UNION tenga 4 columnas.
    // ILIKE (H2) ignora mayúsculas, igual que la versión segura (IgnoreCase), y no
    // agrega paréntesis: los payloads del plan (' UNION ... --) funcionan tal cual.
    public List<Map<String, Object>> buscarInseguro(String q) {
        String sql = "SELECT id, name, description, price FROM products "
                   + "WHERE name ILIKE '%" + q + "%'";

        List<?> filas = entityManager.createNativeQuery(sql).getResultList();

        // Cada fila (Object[]) se convierte en un mapa para devolverla como JSON.
        // No se usa Product.class porque Hibernate confundiría las filas del UNION
        // con productos que tengan el mismo id y se perdería el resultado del ataque.
        List<Map<String, Object>> productos = new ArrayList<>();
        for (Object fila : filas) {
            Object[] col = (Object[]) fila;
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("id", col[0]);
            p.put("name", col[1]);
            p.put("description", col[2]);
            p.put("price", col[3]);
            productos.add(p);
        }
        return productos;
    }
}
