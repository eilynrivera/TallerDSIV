package taller.owasp.vulnerable;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * SecurityConfig del perfil VULNERABLE (puerto 3000).
 *
 * Spring Security deja pasar todo; la sesion la maneja la app (atributo "user").
 * CSRF desactivado, cabeceras de seguridad apagadas y CORS permisivo: esta es la
 * version insegura, a proposito.
 */
@Configuration
@EnableWebSecurity
@Profile("vulnerable")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable())

                // --- CSRF --- (M5 / C8: en vulnerable va desactivado)
                .csrf(csrf -> csrf.disable())

                // --- CORS --- (M4 / C6): refleja CUALQUIER origen y permite credenciales,
                // asi que un sitio ajeno (127.0.0.1:4000) puede leer datos con la sesion
                // de la victima.
                .cors(cors -> cors.configurationSource(corsPermisivo()))

                // --- headers --- (M4 / C6): todas las cabeceras de seguridad apagadas.
                // Esto tambien quita X-Frame-Options, asi que la consola H2 (frames) carga.
                .headers(headers -> headers.disable());

        return http.build();
    }

    // CORS que refleja el origen solicitante (allowedOriginPatterns + credenciales).
    // Expuesto como @Bean (nombre "corsTaller") para que /dashboard/estado pueda
    // consultarlo en vivo. El nombre lo distingue del CorsConfigurationSource que
    // Spring MVC tambien registra (mvcHandlerMappingIntrospector).
    @Bean("corsTaller")
    CorsConfigurationSource corsPermisivo() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
