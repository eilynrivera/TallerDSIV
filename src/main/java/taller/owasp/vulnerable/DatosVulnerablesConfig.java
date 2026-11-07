package taller.owasp.vulnerable;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import taller.owasp.shared.UserSeed;

/**
 * Carga los usuarios de prueba (en claro) al arrancar el perfil vulnerable.
 *
 * NOTA (unificacion - Jose/Persona C): viene de VulnerableDemoConfig (demo/vulnerable).
 * Solo cambio el paquete; usa el UserSeed ya migrado a JPA.
 */
@Configuration
@Profile("vulnerable")
public class DatosVulnerablesConfig {

    @Bean
    public CommandLineRunner cargarUsuariosVulnerables(UserSeed userSeed) {
        return args -> userSeed.inicializarVulnerable();
    }
}
