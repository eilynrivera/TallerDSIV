# M4 · CORS y cabeceras HTTP

**Ataque:** desde el sitio atacante (`http://127.0.0.1:4000`), un `fetch` con `credentials: 'include'` lee `/api/profile` y `/api/comments` de la víctima usando su cookie de sesión; además faltan todas las cabeceras de seguridad (CSP, X-Frame-Options, etc.).
**Cómo probarlo:** por navegador (no hay `.http`). Abrir `http://127.0.0.1:4000` en Chrome con sesión iniciada en `localhost:3000`, pulsar el botón del ataque y mirar **DevTools → Console / Network**. Comparar contra `localhost:3001`.
**Marcador:** registra `M4 · Lectura de datos desde otro sitio (CORS)` con `exito` en 3000 y `bloqueado` en 3001 (lo detectan `vulnerable/MarcadorCorsFilter` y `patched/MarcadorCorsFilter` cuando llega una petición cross-site a `/api/profile` o `/api/comments`).

## 1. CORS: de reflejar cualquier origen a una lista blanca

**Archivo:** `src/main/java/taller/owasp/patched/SecurityConfig.java`

Antes (vulnerable — `vulnerable/SecurityConfig.java`):

```java
CorsConfiguration config = new CorsConfiguration();
config.setAllowedOriginPatterns(List.of("*")); // refleja CUALQUIER origen
config.setAllowCredentials(true);              // ...y con la cookie de sesión
```

Después (corregido — `patched/SecurityConfig.java`):

```java
CorsConfiguration config = new CorsConfiguration();
config.setAllowedOrigins(List.of("http://localhost:3001", "http://127.0.0.1:3001"));
config.setAllowedMethods(List.of("GET", "POST"));
config.setAllowCredentials(true);
```

**Por qué:** `allowedOriginPatterns("*")` junto con `allowCredentials(true)` hace que el servidor devuelva `Access-Control-Allow-Origin` reflejando al atacante, así que el navegador le deja leer la respuesta. Con una lista blanca, el origen `:4000` no está permitido y el navegador bloquea la lectura.

## 2. Cabeceras de seguridad: de apagadas a activas (+ CSP)

**Archivo:** `src/main/java/taller/owasp/patched/SecurityConfig.java`

Antes (vulnerable):

```java
.headers(headers -> headers.disable()); // sin CSP, sin X-Frame-Options, sin nada
```

Después (corregido):

```java
.headers(headers -> headers
        .frameOptions(frame -> frame.sameOrigin())
        .addHeaderWriter(new DelegatingRequestMatcherHeaderWriter(
                new NegatedRequestMatcher(new AntPathRequestMatcher("/h2-console/**")),
                new StaticHeadersWriter("Content-Security-Policy", CSP))));
```

**Por qué:** al quitar `headers.disable()` vuelven las cabeceras por defecto de Spring Security y se añade la CSP de M3. `frameOptions = sameOrigin` deja viva la consola H2 (M2) sin permitir que otros sitios incrusten la app.

## Resultado esperado

| Ataque | 3000 (vulnerable) | 3001 (corregido) |
|---|---|---|
| `fetch` cross-site a `/api/profile` y `/api/comments` | El atacante (`:4000`) lee el perfil y los comentarios de la víctima | **Error de CORS** en la consola; no lee la respuesta |
| Inspección de cabeceras en Network | Faltan CSP, X-Frame-Options, etc. | Cabeceras de seguridad presentes + CSP |

## Guía rápida para "parchéalo tú" (`git checkout modulo-4`)

1. Abrir `http://127.0.0.1:4000` con sesión en `:3000`: el ataque **funciona** (lee datos).
2. En `patched/SecurityConfig.java`: cambiar `allowedOriginPatterns("*")` por la **lista blanca** del paso 1, y `headers.disable()` por el bloque de cabeceras + CSP del paso 2.
3. Re-levantar el perfil `patched` y repetir el ataque apuntando a `:3001`: ahora el navegador lo **bloquea** (error de CORS en la consola).
