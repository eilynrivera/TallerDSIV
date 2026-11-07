package taller.owasp.entity;

import jakarta.persistence.*;

// Tabla escondida del reto CTF. Ninguna pantalla la muestra:
// solo se puede leer con inyección SQL en la versión vulnerable.
@Entity
@Table(name = "secreto_ctf")
public class SecretoCtf {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String flag;

    public SecretoCtf() {}

    public SecretoCtf(String flag) {
        this.flag = flag;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }
}
