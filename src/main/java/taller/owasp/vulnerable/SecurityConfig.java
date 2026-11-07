package taller.owasp.vulnerable;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig del perfil VULNERABLE (puerto 3000).
 *
 * Base del taller: Spring Security deja pasar todo (anyRequest().permitAll()),
 * sin login de formulario ni httpBasic ni logout por defecto; la sesion la maneja
 * la app a mano (atributo "user" en la HttpSession). CSRF se deja desactivado para
 * no romper los POST de login/comentarios. Las cabeceras de seguridad se desactivan
 * aqui a proposito: esta es la version insegura.
 *
 * Los bloques marcados son los puntos donde cada modulo ajusta su configuracion.
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

                // --- headers --- (M4 / C6: cabeceras de seguridad apagadas en la
                // version insegura; ademas frameOptions off para que cargue la consola H2)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                // --- CORS --- (M4 / C6: bean CorsConfigurationSource permisivo, se
                // agrega al llegar a ese modulo)
                // http.cors(cors -> cors.configurationSource(...));

                // --- sesion/cookies --- (M4/M5: SameSite=None se fija por propiedades
                // y/o un CookieSerializer cuando se trabaje CORS/CSRF)
                ;

        return http.build();
    }
}
