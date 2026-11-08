package taller.owasp.shared;

/**
 * Textos y detectores de los ataques de la Persona A (M1, M3 y el extra IDOR) para
 * el marcador. Vulnerable y patched usan EXACTAMENTE los mismos textos, asi el
 * dashboard empareja la fila de 3000 con la de 3001:
 *
 *   Scoreboard.registrar(modulo, ataque, EXITO)      -> el ataque funciono (3000)
 *   Scoreboard.registrar(modulo, ataque, BLOQUEADO)  -> el ataque fallo    (3001)
 *
 * Se registra solo cuando la peticion trae un payload (comilla, UNION, HTML...):
 * el uso normal de la tienda no ensucia el marcador.
 */
public final class AtaquesInyeccion {

    private AtaquesInyeccion() {}

    // Resultados
    public static final String EXITO = "exito";
    public static final String BLOQUEADO = "bloqueado";

    // M1 · Inyeccion SQL
    public static final String M1 = "M1";
    public static final String M1_LOGIN = "Login sin contraseña (admin' --)";
    public static final String M1_COMILLA = "Error SQL visible (comilla)";
    public static final String M1_UNION = "UNION SELECT a users";
    public static final String M1_CTF = "Reto CTF (secreto_ctf)";

    // M3 · XSS
    public static final String M3 = "M3";
    public static final String M3_XSS = "XSS almacenado (<img onerror>)";

    // Extra (A11) · IDOR
    public static final String IDOR = "IDOR";
    public static final String IDOR_PERFIL = "Leer el perfil de otro usuario";

    /** Un texto con comilla simple es un intento de inyeccion SQL. */
    public static boolean esInyeccionSql(String texto) {
        return texto != null && texto.contains("'");
    }

    /**
     * Clasifica una busqueda de productos: el ataque que corresponde, o null si es una
     * busqueda normal. UNION a secreto_ctf o INFORMATION_SCHEMA es el reto CTF.
     */
    public static String ataqueDeBusqueda(String q) {
        if (!esInyeccionSql(q)) {
            return null;
        }
        String mayus = q.toUpperCase();
        if (!mayus.contains("UNION")) {
            return M1_COMILLA;
        }
        return mayus.contains("SECRETO_CTF") || mayus.contains("INFORMATION_SCHEMA")
                ? M1_CTF : M1_UNION;
    }

    /** Un comentario con etiquetas HTML es un intento de XSS. */
    public static boolean esXss(String texto) {
        return texto != null && texto.contains("<");
    }
}
