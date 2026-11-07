package taller.owasp;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

// M1 y M3: los mismos ataques deben FALLAR en el perfil patched (3001).
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:test-safe;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@ActiveProfiles("patched")
class AtaquesPatchedTest {

    @Autowired MockMvc mvc;

    @Test
    void loginConAdminComillaGuionesResponde401() throws Exception {
        mvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin' --\",\"password\":\"x\"}"))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void comillaSolaNoDaError() throws Exception {
        mvc.perform(get("/api/products").param("q", "'"))
           .andExpect(status().isOk())
           .andExpect(content().json("[]"));
    }

    @Test
    void unionNoDevuelveNada() throws Exception {
        mvc.perform(get("/api/products").param("q", "' UNION SELECT id, username, password, 0 FROM users --"))
           .andExpect(status().isOk())
           .andExpect(content().json("[]"));
    }

    @Test
    void comentarioSeGuardaSinEtiquetas() throws Exception {
        mvc.perform(post("/api/comments").contentType(MediaType.APPLICATION_JSON)
                .content("{\"author\":\"x\",\"content\":\"hola <img src=x onerror=alert(1)>\"}"))
           .andExpect(jsonPath("$.content").value(not(containsString("<img"))));
    }

    @Test
    void respuestasLlevanCspSalvoLaConsolaH2() throws Exception {
        mvc.perform(get("/api/comments"))
           .andExpect(header().string("Content-Security-Policy", containsString("script-src 'self'")));
        mvc.perform(get("/h2-console/"))
           .andExpect(header().doesNotExist("Content-Security-Policy"));
    }
}
