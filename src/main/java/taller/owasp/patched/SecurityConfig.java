package taller.owasp.patched;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig del perfil PATCHED (puerto 3001).
 *
 * Mismo esqueleto que la version vulnerable (permitAll, sin formLogin/httpBasic/logout,
 * sesion a mano). Aqui es donde se endurece la configuracion al avanzar los modulos:
 *
 *  - M3 / A: la CSP se agrega en el bloque "headers" con headers.defaultsDisabled()
 *    + contentSecurityPolicy(...). Avisar a C antes de tocar este archivo.
 *  - M4 / C7: se quita defaultsDisabled() para activar todas las cabeceras y se deja
 *    la CSP; el CORS pasa a lista explicita (solo http://localhost:3001).
 *  - M5 / C9: se activa CSRF SOLO para /api/account/** (si se activa global rompe
 *    login y comentarios).
 *
 * Por ahora el baseline deja todo pasar y CSRF desactivado para no romper los .http.
 */
@Configuration
@EnableWebSecurity
@Profile("patched")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable())

                // --- CSRF --- (M5 / C9: reactivar solo para /api/account/**)
                .csrf(csrf -> csrf.disable())

                // --- headers --- (M3 / A: CSP; M4 / C7: cabeceras completas.
                // frameOptions off por ahora para la consola H2 de M2)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                // --- CORS --- (M4 / C7: lista explicita de origenes)
                // http.cors(cors -> cors.configurationSource(...));

                // --- sesion/cookies --- (M5: SameSite=Lax en la version segura)
                ;

        return http.build();
    }
}
