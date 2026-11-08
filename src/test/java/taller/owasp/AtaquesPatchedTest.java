package taller.owasp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
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

// M1, M3 y el extra IDOR: los mismos ataques deben FALLAR en el perfil patched (3001)
// y quedar en el marcador como "bloqueado".
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:test-safe;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@ActiveProfiles("patched")
class AtaquesPatchedTest {

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
    void loginConAdminComillaGuionesResponde401() throws Exception {
        mvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin' --\",\"password\":\"x\"}"))
           .andExpect(status().isUnauthorized());
        assertThat(MarcadorDePrueba.contenido()).contains(MarcadorDePrueba.linea(M1, M1_LOGIN, BLOQUEADO));
    }

    @Test
    void comillaSolaNoDaError() throws Exception {
        mvc.perform(get("/api/products").param("q", "'"))
           .andExpect(status().isOk())
           .andExpect(content().json("[]"));
        assertThat(MarcadorDePrueba.contenido()).contains(MarcadorDePrueba.linea(M1, M1_COMILLA, BLOQUEADO));
    }

    @Test
    void unionNoDevuelveNada() throws Exception {
        mvc.perform(get("/api/products").param("q", "' UNION SELECT id, username, password, 0 FROM users --"))
           .andExpect(status().isOk())
           .andExpect(content().json("[]"));
        assertThat(MarcadorDePrueba.contenido()).contains(MarcadorDePrueba.linea(M1, M1_UNION, BLOQUEADO));
    }

    @Test
    void retoCtfNoLeeLaBandera() throws Exception {
        mvc.perform(get("/api/products").param("q", "' UNION SELECT id, flag, '', 0 FROM secreto_ctf --"))
           .andExpect(status().isOk())
           .andExpect(content().json("[]"));
        assertThat(MarcadorDePrueba.contenido()).contains(MarcadorDePrueba.linea(M1, M1_CTF, BLOQUEADO));
    }

    @Test
    void busquedaNormalNoSeAnotaEnElMarcador() throws Exception {
        String antes = MarcadorDePrueba.contenido();
        mvc.perform(get("/api/products").param("q", "lap"))
           .andExpect(status().isOk());
        assertThat(MarcadorDePrueba.contenido()).isEqualTo(antes);
    }

    @Test
    void comentarioSeGuardaSinEtiquetas() throws Exception {
        mvc.perform(post("/api/comments").contentType(MediaType.APPLICATION_JSON)
                .content("{\"author\":\"x\",\"content\":\"hola <img src=x onerror=alert(1)>\"}"))
           .andExpect(jsonPath("$.content").value(not(containsString("<img"))));
        assertThat(MarcadorDePrueba.contenido()).contains(MarcadorDePrueba.linea(M3, M3_XSS, BLOQUEADO));
    }

    @Test
    void respuestasLlevanCspSalvoLaConsolaH2() throws Exception {
        mvc.perform(get("/api/comments"))
           .andExpect(header().string("Content-Security-Policy", containsString("script-src 'self'")));
        mvc.perform(get("/h2-console/"))
           .andExpect(header().doesNotExist("Content-Security-Policy"));
    }

    // ---------- Extra A11: IDOR ----------

    @Test
    void idorVerOtroUsuarioResponde403() throws Exception {
        MockHttpSession maria = iniciarSesion("maria", "sunshine");
        Long idAdmin = userRepository.findByUsername("admin").orElseThrow().getId();

        mvc.perform(get("/api/users/" + idAdmin).session(maria))
           .andExpect(status().isForbidden());
        assertThat(MarcadorDePrueba.contenido()).contains(MarcadorDePrueba.linea(IDOR, IDOR_PERFIL, BLOQUEADO));
    }

    @Test
    void idorVerSuPropioPerfilFuncionaSinContrasena() throws Exception {
        MockHttpSession maria = iniciarSesion("maria", "sunshine");
        Long idMaria = userRepository.findByUsername("maria").orElseThrow().getId();

        mvc.perform(get("/api/users/" + idMaria).session(maria))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.username").value("maria"))
           .andExpect(jsonPath("$.password").doesNotExist());
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
