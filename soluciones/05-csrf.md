# M5 · CSRF (Cross-Site Request Forgery)

**Ataque:** la página `premio.html` del sitio atacante (`http://127.0.0.1:4000`) tiene un formulario oculto que hace `POST /api/account/email` a `localhost:3000`. Como la cookie es `SameSite=None`, el navegador la envía aunque la petición venga de otro sitio, y el servidor cambia el correo de la víctima sin su consentimiento.
**Cómo probarlo:** por navegador (no hay `.http`). Con sesión iniciada en `localhost:3000`, abrir `http://127.0.0.1:4000/premio.html` y verificar en "Mi cuenta" que el correo cambió. Repetir contra `localhost:3001`.
**Marcador:** registra `M5 · Cambio de correo sin consentimiento (CSRF)` con `exito` en 3000 (lo detecta `vulnerable/CsrfController` cuando el POST es cross-site) y `bloqueado` en 3001 (lo detecta `patched/CsrfMarcadorAccessDeniedHandler` cuando Spring Security rechaza el POST sin token).

## 1. Activar la protección CSRF solo para `/api/account/**`

**Archivo:** `src/main/java/taller/owasp/patched/SecurityConfig.java`

Antes (vulnerable — `vulnerable/SecurityConfig.java`):

```java
.csrf(csrf -> csrf.disable()); // sin token: cualquier POST con la cookie se acepta
```

Después (corregido — `patched/SecurityConfig.java`):

```java
.csrf(csrf -> csrf
        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
        .ignoringRequestMatchers("/api/login", "/api/logout",
                "/api/comments", "/h2-console/**"));
```

**Por qué:** con CSRF activo, `POST /api/account/email` exige el token. El formulario del atacante no lo tiene, así que recibe **403**. Se **ignora** login, logout, comentarios y la consola H2 para no romper M1, M3 y M6, que usan POST sin token a propósito.

## 2. Entregar el token al frontend legítimo

**Archivo:** `src/main/java/taller/owasp/patched/CsrfController.java`

```java
@GetMapping("/api/csrf")
public CsrfToken csrf(CsrfToken token) {
    return token; // escribe la cookie XSRF-TOKEN; el front la reenvía en X-XSRF-TOKEN
}
```

**Por qué:** el frontend legítimo pide el token con `GET /api/csrf` y lo manda en la cabecera `X-XSRF-TOKEN` al cambiar el correo; por eso su petición sí pasa (200) y la del atacante no.

## 3. Cookie `SameSite=Lax` en vez de `None`

**Archivo:** `src/main/resources/application-patched.properties`

```properties
server.servlet.session.cookie.same-site=lax
```

**Por qué:** `SameSite=Lax` (hoy el valor por defecto de Chrome) hace que el navegador **no** envíe la cookie de sesión en un POST que nace de otro sitio. Es una segunda barrera además del token. *(La versión vulnerable fuerza `SameSite=None` justo para que el ataque sea posible — decirlo en voz alta al narrar.)*

## Resultado esperado

| Ataque | 3000 (vulnerable) | 3001 (corregido) |
|---|---|---|
| `premio.html` hace `POST /api/account/email` | El correo de la víctima **cambia** (200) | **403**: sin token CSRF, la petición se rechaza |
| Cambio de correo desde el frontend legítimo | Funciona | Funciona (manda `X-XSRF-TOKEN`) |

## Guía rápida para demo / "parchéalo tú" (`git checkout modulo-5`)

1. Con sesión en `:3000`, abrir `premio.html` del sitio atacante: el correo **cambia**.
2. En `patched/SecurityConfig.java` activar CSRF para `/api/account/**` (paso 1) y poner la cookie en `SameSite=Lax` (paso 3).
3. Re-levantar `patched` y repetir contra `:3001`: el POST del atacante da **403**; el frontend legítimo sigue funcionando con su token.
