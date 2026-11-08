# Párrafo de José (Persona C) para el README

Contenido que C aporta al README (lo integra B): los tags de git, los módulos
M4 y M5, y la nota sobre `SameSite`. Pegar estas secciones donde corresponda.

---

## Tags de git (puntos de rescate)

El repositorio incluye tags `modulo-0` … `modulo-6`. Sirven como **puntos de
rescate** durante la demostración en vivo: si un módulo se complica, se hace
`git checkout modulo-N` para retomar desde un estado conocido y seguir.

Como este proyecto mantiene **las dos versiones a la vez** (perfil `vulnerable`
en el puerto 3000 y `patched` en el 3001), cada tag marca el punto de la
historia en el que ese módulo quedó terminado. Es decir, `modulo-N` contiene los
módulos `0..N` ya completos (vulnerable y corregido lado a lado), listos para
comparar.

| Tag | Contenido |
|-----|-----------|
| `modulo-0` | Base del taller: esqueleto, los tres perfiles (`vulnerable`/`patched`/`evil`), estructura de paquetes y datos sembrados. Todo vulnerable, nada corregido. Es el punto de rescate del **reto CTF**. |
| `modulo-1` | + M1 Inyección SQL corregida (consultas parametrizadas vía JPA en la versión segura). |
| `modulo-2` | + M2 Datos sensibles (contraseñas con BCrypt y respuesta sin contraseña en la versión segura). |
| `modulo-3` | + M3 XSS corregido (sanitizado en el servidor y `textContent` en el cliente). |
| `modulo-4` | + M4 CORS y cabeceras (lista blanca de orígenes, cabeceras de seguridad y CSP en la versión segura). |
| `modulo-5` | + M5 CSRF (token con `CookieCsrfTokenRepository`, cookie `SameSite=Lax` en la versión segura). |
| `modulo-6` | + M6 Fuerza bruta (límite de intentos de inicio de sesión en la versión segura). |

Los tags los crea una sola persona, al final y en orden, para que la numeración
quede consistente.

### Verificar el reto CTF con `modulo-0`

```bash
git checkout modulo-0
# levantar el perfil vulnerable (puerto 3000)
```

Con el perfil vulnerable arriba, el buscador de productos concatena la entrada
en la consulta SQL, así que un `UNION SELECT` contra `INFORMATION_SCHEMA`
descubre las tablas de la base (pista del reto), entre ellas `secreto_ctf`, de
donde se extrae la bandera `FLAG{union_select_information_schema}`.

---

## M4 — CORS y cabeceras HTTP

- **Vulnerable (3000):** CORS permisivo (`allowedOriginPatterns("*")` con
  `allowCredentials(true)`) y cabeceras de seguridad desactivadas
  (`headers.disable()`). Un sitio atacante (`127.0.0.1:4000`) puede leer con
  `fetch(..., { credentials: "include" })` el perfil y los comentarios de la
  víctima, porque el navegador adjunta la cookie de sesión.
- **Seguro (3001):** CORS con **lista explícita** de orígenes (solo el propio
  frontend), más las cabeceras de seguridad completas y una Content-Security
  -Policy. El mismo `fetch` del atacante queda bloqueado por el navegador.

## M5 — CSRF (cambio de correo)

- **Vulnerable (3000):** `POST /api/account/email` solo comprueba que haya
  sesión; no exige token CSRF. Combinado con la cookie `SameSite=None` de M4, el
  formulario oculto de `premio.html` (otro origen) cambia el correo de la
  víctima sin su consentimiento.
- **Seguro (3001):** CSRF activado para `/api/account/**` con
  `CookieCsrfTokenRepository.withHttpOnlyFalse()`; el frontend pide el token en
  `GET /api/csrf` y lo reenvía en la cabecera `X-XSRF-TOKEN`. El ataque sin token
  recibe **403**.

## Nota sobre `SameSite`

El taller fuerza `SameSite=None` en la versión vulnerable **a propósito**: los
navegadores actuales (Chrome incluido) ya usan `SameSite=Lax` por defecto, que
por sí solo mitigaría buena parte de CSRF. Al ponerlo en `None` reproducimos el
escenario clásico para que el ataque sea observable; la versión segura vuelve a
`Lax`. Chrome solo acepta `SameSite=None` si la cookie también es `Secure`; en
`localhost`/`127.0.0.1` lo trata como contexto seguro, por eso funciona sin
HTTPS en la demo.
