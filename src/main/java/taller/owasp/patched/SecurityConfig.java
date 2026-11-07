package taller.owasp.patched;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;

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

    // M3 capa 3 (A): solo se ejecutan scripts servidos por la propia app; un
    // <img onerror=...> o un <script> inyectado en un comentario no corre.
    private static final String CSP = "default-src 'self'; script-src 'self'; style-src 'self'; "
                                    + "img-src 'self'; object-src 'none'";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable())

                // --- CSRF --- (M5 / C9: reactivar solo para /api/account/**)
                .csrf(csrf -> csrf.disable())

                // --- headers --- (M3 / A: solo la CSP. M4 / C7: quitar defaultsDisabled()
                // para activar el resto de cabeceras, dejando la CSP.)
                // La CSP no se aplica a /h2-console/** porque la consola H2 (M2) usa
                // JavaScript en linea y dejaria de funcionar.
                .headers(headers -> headers
                        .defaultsDisabled()
                        .addHeaderWriter(new DelegatingRequestMatcherHeaderWriter(
                                new NegatedRequestMatcher(new AntPathRequestMatcher("/h2-console/**")),
                                new StaticHeadersWriter("Content-Security-Policy", CSP))))

                // --- CORS --- (M4 / C7: lista explicita de origenes)
                // http.cors(cors -> cors.configurationSource(...));

                // --- sesion/cookies --- (M5: SameSite=Lax en la version segura)
                ;

        return http.build();
    }
}
