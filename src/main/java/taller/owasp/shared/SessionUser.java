package taller.owasp.shared;

/**
 * Lo que se guarda en la HttpSession tras un login correcto, bajo el atributo "user".
 * Convencion acordada por los tres: cualquier endpoint que necesite saber quien tiene
 * sesion lee este atributo. Lo escriben los controladores de login (M1 / M2).
 */
public record SessionUser(Long id, String username, String role) {

    /** Nombre del atributo en la HttpSession. Usarlo siempre en vez de un literal. */
    public static final String ATRIBUTO_SESION = "user";
}
