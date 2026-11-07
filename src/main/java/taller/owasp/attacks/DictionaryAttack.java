package taller.owasp.attacks;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DictionaryAttack {

    public List<String> cargarContraseñas() throws Exception {

        List<String> contraseñas = new ArrayList<>();

        InputStream archivo = getClass()
                .getClassLoader()
                .getResourceAsStream("attacks/passwords.txt");

        if (archivo == null) {
            throw new Exception("No se encontró el archivo de contraseñas");
        }

        try (BufferedReader lector = new BufferedReader(
                new InputStreamReader(archivo))) {

            String linea;

            while ((linea = lector.readLine()) != null) {
                if (!linea.isBlank()) {
                    contraseñas.add(linea.trim());
                }
            }
        }

        return contraseñas;
    }

    public void probarCarga() throws Exception {
        List<String> contraseñas = cargarContraseñas();

        System.out.println("Total de contraseñas cargadas: " + contraseñas.size());
        System.out.println("Contraseña en posición 37: " + contraseñas.get(36));
    }
}