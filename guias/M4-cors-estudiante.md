# Guía del participante · M4 — CORS y cabeceras de seguridad

> **Formato:** parchéalo tú (hands-on).
> **La idea:** la tienda **vulnerable** (`:3000`) deja que otro sitio te robe datos. Vas a **arreglar esa misma tienda** con tus manos y comprobar que al terminar se comporta **igual que la versión segura** (`:3001`).
> **Tiempo estimado:** ~8 min. El fix se escribe en menos de 3 min.

**Guías del taller:** **M4 — CORS (estás aquí)** · [M5 — CSRF](M5-csrf-estudiante.md)

---

## 0. El escenario (3 puertos)

| Puerto | Qué es | Tu rol |
|---|---|---|
| **`localhost:3000`** | La tienda **vulnerable** | **Es la que vas a arreglar** |
| **`localhost:3001`** | La tienda **segura** (ya resuelta) · **(TallerDSIV)** | Es tu **referencia**: así debe quedar la 3000 |
| **`127.0.0.1:4000`** | El **sitio atacante** | Intenta leer tus datos de `:3000` |

> **(TallerDSIV)** La tienda segura **`:3001`**, la carpeta **`patched/`** y el marcador **`dashboard.html`** pertenecen al proyecto completo **TallerDSIV**. En esta guía se usan solo como **referencia** de cómo debe quedar la tienda `:3000`.

### Levantar los tres servicios (una terminal por perfil)

Desde la carpeta del proyecto, en PowerShell, abrí **una terminal para cada perfil** y ejecutá:

```powershell
mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=vulnerable"   # tienda vulnerable -> localhost:3000
mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=patched"      # tienda segura     -> localhost:3001
mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=evil"         # sitio atacante    -> 127.0.0.1:4000
```

> Cada comando queda corriendo en su terminal; no los cierres durante el taller. Reiniciar un perfil restaura su base (H2 en memoria).

**La vulnerabilidad:** la tienda `:3000` responde a CORS reflejando *cualquier* origen y además permite credenciales (`allowCredentials(true)`). Si tenés sesión iniciada, un sitio ajeno (`:4000`) puede hacer un `fetch` *con tu cookie* y leer tu perfil y tus comentarios.

---

## 1. Primero: mirá el ataque funcionando

1. Iniciá sesión en la tienda **`http://localhost:3000`**.
2. En otra pestaña abrí el sitio atacante **`http://127.0.0.1:4000`** y pulsá el botón del ataque.
3. En la página del atacante aparece:
   > **Datos robados por el atacante:** `{ "perfil": {...}, "comentarios": [...] }`
4. Abrí **DevTools (F12) → Network** y mirá la respuesta de `/api/profile`: trae `Access-Control-Allow-Origin` reflejando al atacante.

> Ese es el "antes". Ahora vas a arreglar la tienda `:3000` para que ese botón **deje de robar datos**, igual que ya pasa en `:3001`.

---

## 2. El archivo a modificar

```
src/main/java/taller/owasp/vulnerable/SecurityConfig.java
```

> **Ojo:** es el de la carpeta **`vulnerable/`** (la tienda `:3000`), **no** el de `patched/` **(TallerDSIV)**. La de `patched/` ya está resuelta y te sirve de referencia.

Dentro hay **dos puntos** que tocar: el método de CORS (al final del archivo) y la línea de `headers`.

---

## 3. Cambio A — CORS: de "cualquier origen" a lista blanca  *(el corazón de M4)*

En el **método `corsPermisivo()`** (al final del archivo), buscá estas dos líneas:

```java
// ANTES (vulnerable)
config.setAllowedOriginPatterns(List.of("*"));   // refleja CUALQUIER origen
config.setAllowedMethods(List.of("*"));
```

y reemplazalas por:

```java
// DESPUÉS (corregido) — copiá y pegá
config.setAllowedOrigins(List.of("http://localhost:3000", "http://127.0.0.1:3000"));
config.setAllowedMethods(List.of("GET", "POST"));
```

**Por qué:** con una lista blanca, el origen `http://127.0.0.1:4000` ya **no** está permitido. El navegador no recibe el `Access-Control-Allow-Origin` que lo autorice y **bloquea la lectura** de la respuesta.

> **Fijate:** acá ponés los orígenes de **tu propia tienda** (`:3000`). La versión segura de referencia (`patched/` **(TallerDSIV)**) hace exactamente lo mismo pero con los suyos (`:3001` **(TallerDSIV)**). Cada tienda permite **su propio origen**, y a nadie más.

El método completo queda así:

```java
private CorsConfigurationSource corsPermisivo() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:3000", "http://127.0.0.1:3000"));
    config.setAllowedMethods(List.of("GET", "POST"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

> **Ojo con la firma:** dejá el método tal cual está en el archivo (`private CorsConfigurationSource corsPermisivo()`). Solo cambiás las **dos líneas** de arriba; la firma del método no se toca.

---

## 4. Cambio B — Cabeceras de seguridad + CSP

En la cadena de `http`, buscá:

```java
// ANTES (vulnerable): todas las cabeceras de seguridad apagadas
.headers(headers -> headers.disable());
```

y reemplazala por:

```java
// DESPUÉS (corregido) — copiá y pegá
.headers(headers -> headers
        .frameOptions(frame -> frame.sameOrigin())
        .addHeaderWriter(new DelegatingRequestMatcherHeaderWriter(
                new NegatedRequestMatcher(new AntPathRequestMatcher("/h2-console/**")),
                new StaticHeadersWriter("Content-Security-Policy", CSP))));
```

Ese bloque usa una constante `CSP`. Agregala **justo después** de la línea `public class SecurityConfig {`:

```java
private static final String CSP = "default-src 'self'; script-src 'self'; style-src 'self'; "
                                + "img-src 'self'; object-src 'none'";
```

Y agregá estos **4 imports** arriba del archivo (junto a los demás `import`):

```java
import org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
```

**Por qué:** al quitar `headers.disable()` vuelven las cabeceras de seguridad por defecto de Spring Security y se añade la CSP. `frameOptions = sameOrigin` deja viva la consola H2 sin permitir que otros sitios incrusten la app en un `<iframe>`.

---

## 5. Re-levantar y volver a atacar

1. Reiniciá la tienda **vulnerable** (`:3000`) para que tome los cambios.
2. Volvé al sitio atacante **`http://127.0.0.1:4000`** y pulsá otra vez el botón del ataque.

---

## 6. Qué debés ver una vez aplicado el parche ✓

La meta: que la tienda `:3000` ahora bloquee el robo **igual que la `:3001`** **(TallerDSIV)**.

| Dónde mirar | Antes (tu `:3000` sin parchar) | Después (tu `:3000` parchado) |
|---|---|---|
| **Página del atacante (`:4000`)** | *"Datos robados por el atacante: {perfil…, comentarios…}"* | *"El navegador bloqueó la lectura (CORS)…"* — **igual que contra `:3001`** **(TallerDSIV)** |
| **DevTools → Console** | El `fetch` trae los datos | **Error de CORS** rojo: *"has been blocked by CORS policy… No 'Access-Control-Allow-Origin'…"* |
| **DevTools → Network** (respuesta) | `Access-Control-Allow-Origin: http://127.0.0.1:4000` reflejado | Ya **no** se refleja el origen atacante; la lectura se bloquea |
| **DevTools → Network** (cabeceras) | Faltan CSP, X-Frame-Options… | Aparecen **`Content-Security-Policy`** y **`X-Frame-Options`** |
| **La tienda `:3000`** | Funciona | **Sigue funcionando** normal (su propio origen sí está en la lista) |
| **Consola H2** | Carga | **Sigue cargando** (`frameOptions=sameOrigin`) |

**La idea clave de M4:** `allowedOriginPatterns("*")` + `allowCredentials(true)` es la combinación peligrosa. La defensa es una **lista blanca de orígenes** (solo el tuyo).

> **Sobre el marcador `/dashboard.html` (TallerDSIV):** muestra la comparación general 3000 (✗ exito) vs 3001 (✓ bloqueado) **(TallerDSIV)**. La prueba concreta de que **tu** parche sirvió es que el atacante ya **no** puede leer los datos (el mensaje de CORS en la página del atacante y en la consola).

---

## Si algo sale raro

- **No compila:** revisá que agregaste los 4 `import` y la constante `CSP`.
- **El ataque sigue robando datos:** no reiniciaste la tienda `:3000`, o editaste el archivo equivocado (debe ser el de **`vulnerable/`**, no `patched/` **(TallerDSIV)**).
- **Dejaste la base rara:** reiniciar el perfil restaura los datos (están en memoria).
- **Volver a dejar la tienda vulnerable** (para el siguiente grupo): `git checkout -- src/main/java/taller/owasp/vulnerable/SecurityConfig.java`.
