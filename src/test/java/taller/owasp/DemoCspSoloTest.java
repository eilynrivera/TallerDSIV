package taller.owasp;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

// M3, demo "CSP sola" (A7): con lab.demo.csp-solo=true el sanitizer se apaga, el HTML
// se guarda tal cual y la unica defensa es la CSP que acompana a la pagina de la demo.
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:test-csp-solo;DB_CLOSE_DELAY=-1",
        "lab.demo.csp-solo=true"})
@AutoConfigureMockMvc
@ActiveProfiles("patched")
class DemoCspSoloTest {

    @Autowired MockMvc mvc;

    @BeforeAll
    static void guardarMarcador() throws Exception {
        MarcadorDePrueba.guardarCopia();
    }

    @AfterAll
    static void restaurarMarcador() throws Exception {
        MarcadorDePrueba.restaurar();
    }

    @Test
    void conCspSoloElHtmlSeGuardaSinSanitizar() throws Exception {
        mvc.perform(post("/api/comments").contentType(MediaType.APPLICATION_JSON)
                .content("{\"author\":\"x\",\"content\":\"<img src=x onerror=alert(1)>\"}"))
           .andExpect(jsonPath("$.content").value("<img src=x onerror=alert(1)>"));
    }

    @Test
    void laPaginaDeLaDemoSeSirveConCsp() throws Exception {
        mvc.perform(get("/csp-demo.html"))
           .andExpect(status().isOk())
           .andExpect(header().string("Content-Security-Policy", containsString("script-src 'self'")))
           .andExpect(content().string(containsString("csp-demo.js")));
    }
}
