package taller.owasp.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import taller.owasp.domain.User;
import taller.owasp.domain.UserRepository;

@DataJpaTest
@Import(UserService.class)
class UserServiceTest {

    @Autowired UserService userService;
    @Autowired UserRepository userRepository;

    @BeforeEach
    void datos() {
        userRepository.saveAllAndFlush(List.of(
                new User("admin", "admin123", null, "ADMIN"),
                new User("maria", "sunshine", null, "USER"),
                new User("juan", "qwerty2024", null, "USER")));
    }

    @Nested
    @DisplayName("findByUsername (version segura)")
    class Seguro {

        @Test
        void encuentraAlUsuarioExistente() {
            assertThat(userService.findByUsername("maria"))
                    .get()
                    .extracting(User::getRole)
                    .isEqualTo("USER");
        }

        @Test
        void usuarioInexistenteDevuelveVacio() {
            assertThat(userService.findByUsername("nadie")).isEmpty();
        }

        @Test
        void elPayloadDeInyeccionSeBuscaComoNombreLiteral() {
            assertThat(userService.findByUsername("admin' --")).isEmpty();
        }
    }

    @Nested
    @DisplayName("loginInseguro (version vulnerable, SQL concatenado)")
    class Vulnerable {

        @Test
        void credencialesCorrectasDevuelvenAlUsuario() {
            assertThat(userService.loginInseguro("maria", "sunshine"))
                    .extracting(User::getUsername)
                    .containsExactly("maria");
        }

        @Test
        void contrasenaIncorrectaNoDevuelveNada() {
            assertThat(userService.loginInseguro("maria", "mala")).isEmpty();
        }

        @Test
        void comentarioSqlEntraComoAdminSinContrasena() {
            List<User> resultado = userService.loginInseguro("admin' --", "cualquiera");

            assertThat(resultado).extracting(User::getUsername).containsExactly("admin");
            // M2: la entidad trae la contrasena en claro.
            assertThat(resultado.get(0).getPassword()).isEqualTo("admin123");
        }

        @Test
        void orSiempreVerdaderoDevuelveTodosLosUsuarios() {
            assertThat(userService.loginInseguro("x' OR '1'='1' --", "cualquiera"))
                    .hasSize(3);
        }
    }
}
