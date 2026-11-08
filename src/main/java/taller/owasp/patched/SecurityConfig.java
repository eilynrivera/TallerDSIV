
package taller.owasp.patched;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * SecurityConfig del perfil PATCHED (puerto 3001).
 *
 *  - M3 / A: CSP que solo permite recursos del propio origen.
 *  - M4 / C7: CORS restringido a una lista explicita (solo la propia tienda) y
 *    todas las cabeceras de seguridad por defecto activas (ya no defaultsDisabled()),
 *    conservando la CSP. frameOptions queda en sameOrigin para no romper la consola H2.
 *  - M5 / C9: CSRF se reactivara solo para /api/account/**.
 *  - B7: limite de cinco intentos de login por IP cada minuto.
 */
@Configuration
@EnableWebSecurity
@Profile("patched")
public class SecurityConfig {

    // M3 capa 3 (A): solo se ejecutan scripts servidos por la propia app.
    private static final String CSP = "default-src 'self'; script-src 'self'; style-src 'self'; "
                                    + "img-src 'self'; object-src 'none'";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable())

                // --- CSRF --- (M5 / C9): activo SOLO para /api/account/**. Se ignoran
                // login, logout, comentarios y la consola H2 para no romperlos. El token
                // va en la cookie XSRF-TOKEN (legible por JS) y el frontend lo reenvia en
                // la cabecera X-XSRF-TOKEN. El form del atacante no lo tiene -> 403.
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers("/api/login", "/api/logout",
                                "/api/comments", "/h2-console/**"))

                // --- CORS --- (M4 / C7): lista explicita; el sitio atacante (4000) no
                // esta permitido, asi que el navegador bloquea la lectura de la respuesta.
                .cors(cors -> cors.configurationSource(corsRestringido()))

                // --- headers --- (M4 / C7): cabeceras por defecto activas + CSP (M3).
                // La CSP no se aplica a /h2-console/** (la consola usa JS en linea).
                // frameOptions=sameOrigin para que la consola H2 (M2) siga cargando.
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                        .addHeaderWriter(new DelegatingRequestMatcherHeaderWriter(
                                new NegatedRequestMatcher(new AntPathRequestMatcher("/h2-console/**")),
                                new StaticHeadersWriter("Content-Security-Policy", CSP))))

                // Marcador M5 (C9): al rechazar el POST sin token CSRF, se registra
                // "bloqueado" antes de devolver el 403 (ver CsrfMarcadorAccessDeniedHandler).
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(new CsrfMarcadorAccessDeniedHandler()));

        // --- B7: limitar intentos de inicio de sesion ---
        http.addFilterBefore(
                new RateLimitFilter(),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CORS seguro: solo el propio origen de la tienda; nada de reflejar cualquiera.
    private CorsConfigurationSource corsRestringido() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3001", "http://127.0.0.1:3001"));
        config.setAllowedMethods(List.of("GET", "POST"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
