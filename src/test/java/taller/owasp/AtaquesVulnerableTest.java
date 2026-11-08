package taller.owasp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static taller.owasp.shared.AtaquesInyeccion.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import taller.owasp.repository.UserRepository;

// M1, M3 y el extra IDOR: cada ataque del plan debe FUNCIONAR en el perfil vulnerable
// (3000) y quedar en el marcador como "exito".
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:test-vuln;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@ActiveProfiles("vulnerable")
class AtaquesVulnerableTest {

    @Autowired MockMvc mvc;
    @Autowired UserRepository userRepository;

    @BeforeAll
    static void guardarMarcador() throws Exception {
        MarcadorDePrueba.guardarCopia();
    }

    @AfterAll
    static void restaurarMarcador() throws Exception {
        MarcadorDePrueba.restaurar();
    }

    @Test
    void loginConAdminComillaGuionesEntraSinContrasena() throws Exception {
        mvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin' --\",\"password\":\"x\"}"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.password").value("admin123"));
        assertThat(MarcadorDePrueba.contenido()).contains(MarcadorDePrueba.linea(M1, M1_LOGIN, EXITO));
    }

    @Test
    void comillaSolaMuestraElErrorDeLaBase() throws Exception {
        mvc.perform(get("/api/products").param("q", "'"))
           .andExpect(status().isInternalServerError())
           .andExpect(content().string(containsString("SQL")));
        assertThat(MarcadorDePrueba.contenido()).contains(MarcadorDePrueba.linea(M1, M1_COMILLA, EXITO));
    }

    @Test
    void unionDevuelveUsuariosYContrasenas() throws Exception {
        mvc.perform(get("/api/products").param("q", "' UNION SELECT id, username, password, 0 FROM users --"))
           .andExpect(content().string(containsString("admin123")));
        assertThat(MarcadorDePrueba.contenido()).contains(MarcadorDePrueba.linea(M1, M1_UNION, EXITO));
    }

    @Test
    void retoCtfListaLasTablasConInformationSchema() throws Exception {
        mvc.perform(get("/api/products").param("q",
                "' UNION SELECT 1, TABLE_NAME, '', 0 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC' --"))
           .andExpect(status().isOk())
           .andExpect(content().string(containsString("SECRETO_CTF")));
    }

    @Test
    void retoCtfLeeLaBandera() throws Exception {
        mvc.perform(get("/api/products").param("q", "' UNION SELECT id, flag, '', 0 FROM secreto_ctf --"))
           .andExpect(content().string(containsString("FLAG{")));
        assertThat(MarcadorDePrueba.contenido()).contains(MarcadorDePrueba.linea(M1, M1_CTF, EXITO));
    }

    @Test
    void busquedaNormalNoSeAnotaEnElMarcador() throws Exception {
        String antes = MarcadorDePrueba.contenido();
        mvc.perform(get("/api/products").param("q", "lap"))
           .andExpect(status().isOk());
        assertThat(MarcadorDePrueba.contenido()).isEqualTo(antes);
    }

    @Test
    void comentarioGuardaElHtmlSinFiltrar() throws Exception {
        mvc.perform(post("/api/comments").contentType(MediaType.APPLICATION_JSON)
                .content("{\"author\":\"x\",\"content\":\"<img src=x onerror=alert(1)>\"}"))
           .andExpect(jsonPath("$.content").value("<img src=x onerror=alert(1)>"));
        assertThat(MarcadorDePrueba.contenido()).contains(MarcadorDePrueba.linea(M3, M3_XSS, EXITO));
    }

    @Test
    void respuestasNoLlevanCsp() throws Exception {
        mvc.perform(get("/api/comments"))
           .andExpect(header().doesNotExist("Content-Security-Policy"));
    }

    // ---------- Extra A11: IDOR ----------

    @Test
    void idorMariaLeeLosDatosDeAdmin() throws Exception {
        MockHttpSession maria = iniciarSesion("maria", "sunshine");
        Long idAdmin = userRepository.findByUsername("admin").orElseThrow().getId();

        mvc.perform(get("/api/users/" + idAdmin).session(maria))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.username").value("admin"))
           .andExpect(jsonPath("$.password").value("admin123"));
        assertThat(MarcadorDePrueba.contenido()).contains(MarcadorDePrueba.linea(IDOR, IDOR_PERFIL, EXITO));
    }

    @Test
    void idorSinSesionResponde401() throws Exception {
        mvc.perform(get("/api/users/1"))
           .andExpect(status().isUnauthorized());
    }

    private MockHttpSession iniciarSesion(String usuario, String clave) throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + usuario + "\",\"password\":\"" + clave + "\"}"))
           .andExpect(status().isOk())
           .andReturn().getRequest().getSession();
    }
}
