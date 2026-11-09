package taller.owasp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de arranque de la aplicacion Spring Boot del taller OWASP.
 *
 * La misma app se levanta con tres perfiles distintos (se elige con
 * spring.profiles.active, ver application-*.properties):
 *   - "vulnerable" (puerto 3000): la tienda con los huecos a proposito.
 *   - "patched"    (puerto 3001): la misma tienda con las correcciones.
 *   - "evil"       (puerto 4000): el sitio atacante (solo archivos estaticos).
 * Cada bean esta anotado con @Profile para cargarse solo en el perfil que le toca.
 */
@SpringBootApplication
public class OwaspApplication {

	// Arranca el contenedor de Spring: crea los beans, levanta el servidor web
	// embebido (Tomcat) y deja la app escuchando segun el perfil activo.
	public static void main(String[] args) {
		SpringApplication.run(OwaspApplication.class, args);
	}

}
