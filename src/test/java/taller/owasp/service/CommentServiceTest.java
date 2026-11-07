package taller.owasp.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import taller.owasp.entity.Comment;

@DataJpaTest
@Import(CommentService.class)
class CommentServiceTest {

    @Autowired CommentService commentService;

    @Test
    void guardarAsignaIdYFecha() {
        Comment c = commentService.guardar("maria", "Muy buena tienda");

        assertThat(c.getId()).isNotNull();
        assertThat(c.getCreatedAt()).isNotNull();
        assertThat(c.getAuthor()).isEqualTo("maria");
        assertThat(c.getContent()).isEqualTo("Muy buena tienda");
    }

    @Test
    void listaDelMasNuevoAlMasViejo() {
        commentService.guardar("a", "primero");
        commentService.guardar("b", "segundo");
        commentService.guardar("c", "tercero");

        assertThat(commentService.findAllByOrderByIdDesc())
                .extracting(Comment::getContent)
                .containsExactly("tercero", "segundo", "primero");
    }

    // El servicio NO limpia el HTML: guarda lo que le pasan. La limpieza (capa 2 de M3)
    // es responsabilidad del controlador patched, antes de llamar a guardar.
    @Test
    void guardaElHtmlTalCualLlega() {
        String payload = "<img src=x onerror=alert(document.cookie)>";

        commentService.guardar("atacante", payload);

        assertThat(commentService.findAllByOrderByIdDesc())
                .extracting(Comment::getContent)
                .containsExactly(payload);
    }
}
