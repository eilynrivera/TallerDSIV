package taller.owasp.patched;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import taller.owasp.shared.UserSeed;

/**
 * Carga los usuarios de prueba (con BCrypt) al arrancar el perfil patched.
 *
 * NOTA (unificacion - Jose/Persona C): viene de CorregidoDemoConfig (demo/corregido).
 * Cambia el paquete y el perfil quedo en "patched"; usa el UserSeed ya migrado a JPA.
 */
@Configuration
@Profile("patched")
public class DatosPatchedConfig {

    @Bean
    public CommandLineRunner cargarUsuariosPatched(UserSeed userSeed) {
        return args -> userSeed.inicializarSeguro();
    }
}
