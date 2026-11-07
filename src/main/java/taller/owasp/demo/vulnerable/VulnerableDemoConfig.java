package taller.owasp.demo.vulnerable;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import taller.owasp.demo.UserSeed;

@Configuration
@Profile("vulnerable")
public class VulnerableDemoConfig {

    @Bean
    public CommandLineRunner cargarUsuariosVulnerables(UserSeed userSeed) {

        return new CommandLineRunner() {
            @Override
            public void run(String... args) {
                userSeed.inicializarVulnerable();
            }
        };
    }
}