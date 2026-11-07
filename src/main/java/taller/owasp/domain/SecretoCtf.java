package taller.owasp.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Reto CTF: tabla que no enlaza ninguna pantalla.
 * Se descubre con UNION SELECT e INFORMATION_SCHEMA en la version vulnerable.
 */
@Entity
@Table(name = "secreto_ctf")
public class SecretoCtf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String flag;

    public SecretoCtf() {
    }

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
