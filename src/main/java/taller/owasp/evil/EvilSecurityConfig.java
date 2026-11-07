package taller.owasp.evil;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig del perfil EVIL (sitio atacante, puerto 4000).
 *
 * Solo sirve archivos estaticos (static-evil/). Sin esta config, Spring Security
 * bloquearia todo con su login por defecto. Todo permitido y sin CSRF.
 */
@Configuration
@EnableWebSecurity
@Profile("evil")
public class EvilSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable());

        return http.build();
    }
}
