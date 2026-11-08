package taller.owasp.shared;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Etiquetas y detectores del marcador para los modulos de C (M4 CORS, M5 CSRF) y de
 * B (M2 datos sensibles, M6 fuerza bruta). Es el equivalente de {@link AtaquesInyeccion}
 * (modulos de A) para el resto del taller.
 *
 * Vulnerable y patched usan EXACTAMENTE los mismos textos de modulo+ataque, para que el
 * dashboard empareje la fila de 3000 (exito) con la de 3001 (bloqueado):
 *
 *   Scoreboard.registrar(modulo, ataque, AtaquesInyeccion.EXITO)      -> funciono (3000)
 *   Scoreboard.registrar(modulo, ataque, AtaquesInyeccion.BLOQUEADO)  -> fallo    (3001)
 */
public final class AtaquesTaller {

    private AtaquesTaller() {}

    // M2 · Datos sensibles (la contrasena viaja en la respuesta del login)
    public static final String M2 = "M2";
    public static final String M2_PASSWORD = "Contraseña expuesta en el login";

    // M4 · CORS (otro sitio lee datos de la victima con su sesion)
    public static final String M4 = "M4";
    public static final String M4_CORS = "Lectura de datos desde otro sitio (CORS)";

    // M5 · CSRF (otro sitio cambia el correo de la victima)
    public static final String M5 = "M5";
    public static final String M5_CSRF = "Cambio de correo sin consentimiento (CSRF)";

    // M6 · Fuerza bruta (login sin limite de intentos)
    public static final String M6 = "M6";
    public static final String M6_FUERZA = "Fuerza bruta al login (sin límite)";

    /**
     * True si la peticion viene de OTRO sitio: trae cabecera Origin y no coincide con
     * el origen del propio servidor. Es la senal de un ataque cross-site (CORS / CSRF);
     * el uso normal de la tienda es del mismo origen y no ensucia el marcador. Los
     * archivos .http (REST Client) no mandan Origin, asi que tampoco cuentan.
     */
    public static boolean esCruzada(HttpServletRequest req) {
        String origin = req.getHeader("Origin");
        if (origin == null || origin.isBlank()) {
            return false;
        }
        String propio = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();
        return !origin.equals(propio);
    }

    /** True si es una lectura cross-site de los datos que el sitio atacante roba (M4). */
    public static boolean esLecturaCruzadaDeDatos(HttpServletRequest req) {
        String path = req.getServletPath();
        boolean endpointDeDatos = "/api/profile".equals(path) || "/api/comments".equals(path);
        return endpointDeDatos && esCruzada(req);
    }
}
