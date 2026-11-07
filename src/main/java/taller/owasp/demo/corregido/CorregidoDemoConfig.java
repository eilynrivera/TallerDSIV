package taller.owasp.demo.corregido;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import taller.owasp.demo.UserSeed;

@Configuration
// NOTA (adaptacion a la convencion del plan - Jose/Persona C):
// perfil "corregido" -> "patched". El plan acordado fija los perfiles
// vulnerable/patched/evil; sin este cambio tus beans seguros no se activan
// junto con el SecurityConfig de patched (C2). Antes: @Profile("corregido").
@Profile("patched")
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