package taller.owasp.attacks;

import org.junit.jupiter.api.Test;

import taller.owasp.attacks.DictionaryAttack;

import static org.junit.jupiter.api.Assertions.*;

class DictionaryAttackTest {

    @Test
    void probarCargaDeContraseñas() throws Exception {

        DictionaryAttack ataque = new DictionaryAttack();

        var contraseñas = ataque.cargarContraseñas();

        assertEquals(100, contraseñas.size());
        assertEquals("sunshine", contraseñas.get(36));
    }
}