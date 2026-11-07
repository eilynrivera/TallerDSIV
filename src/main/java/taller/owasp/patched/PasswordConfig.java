package taller.owasp.patched;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Bean PasswordEncoder (BCrypt) para el login seguro de M2.
 *
 * NOTA (unificacion - Jose/Persona C): viene de demo/corregido/PasswordConfig.
 * Solo cambio el paquete y lo gatee a @Profile("patched"), que es donde se usa.
 */
@Configuration
@Profile("patched")
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
