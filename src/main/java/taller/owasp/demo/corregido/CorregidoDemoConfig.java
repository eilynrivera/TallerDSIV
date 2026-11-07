package taller.owasp.demo.corregido;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import taller.owasp.demo.UserSeed;

@Configuration
@Profile("corregido")
public class CorregidoDemoConfig {

    @Bean
    public CommandLineRunner cargarUsuariosCorregidos(UserSeed userSeed) {

        return new CommandLineRunner() {
            @Override
            public void run(String... args) {
                userSeed.inicializarSeguro();
            }
        };
    }
}