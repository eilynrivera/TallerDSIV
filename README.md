# Taller OWASP Top 10 — Tienda de ejemplo (Spring Boot)

Tienda de ejemplo con Spring Boot en **dos versiones lado a lado** —`vulnerable` (puerto 3000) y `patched` (puerto 3001)— más un **sitio atacante** (`evil`, puerto 4000), para un taller sobre seis vulnerabilidades del OWASP Top 10. El patrón de cada módulo es **atacar → comparar → entender el parche**: se lanza el ataque en 3000 (funciona), se repite en 3001 (falla) y se mira la diferencia en el código, en las cabeceras (DevTools) o en los archivos `.http`.

Java 21 · Spring Boot 3.4.2 · Spring Data JPA (H2 en memoria) · Spring Security.

## Aviso ético

Este proyecto tiene fines **educativos**: demuestra vulnerabilidades y sus protecciones. Las técnicas aquí mostradas solo deben aplicarse al **entorno local de este proyecto** o a sistemas con **autorización expresa y por escrito**. No se deben usar contra sistemas reales sin permiso.

## Instalación y ejecución

### Requisitos

- **Java 21** (Temurin recomendado).
- **Git**.
- **VS Code** con la extensión *REST Client* (`humao.rest-client`) para los `.http`.
- **Google Chrome** normal (no incógnito): M4 y M5 dependen de cómo Chrome trata las cookies.
- Maven Wrapper (`mvnw.cmd`) ya viene en el proyecto; no hay que instalar Maven.

### Clonar y verificar

```bash
git clone https://github.com/eilynrivera/TallerDSIV.git
cd TallerDSIV
```

```powershell
.\scripts\check.bat
```

`check.bat` comprueba Java 21 y los puertos 3000/3001/4000 e imprime `Listo para el taller`. Para correr las pruebas: `.\mvnw.cmd clean test`.

### Levantar los perfiles (una terminal por perfil)

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=vulnerable"
```

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=patched"
```

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=evil"
```

La base H2 es en memoria y se **rellena sola al arrancar**: reiniciar un servidor restaura su base. Usuarios de prueba: `admin/admin123`, `maria/sunshine`, `juan/qwerty2024`.

## Perfiles, puertos y cookies

| Perfil | Puerto | Cookie de sesión | Frontend | Notas de cookie |
|---|---|---|---|---|
| `vulnerable` | 3000 | `sid_vuln` | `static-vulnerable/` | `SameSite=None; Secure`, `HttpOnly=false` (a propósito) |
| `patched` | 3001 | `sid_safe` | `static-patched/` | `SameSite=Lax`, `HttpOnly=true` |
| `evil` | 4000 | — | `static-evil/` | sitio atacante; sin `Seed` |

La tienda vive en `localhost` y el atacante en `127.0.0.1:4000`, para que Chrome los trate como **sitios distintos** (clave para M4 y M5). Todo escucha solo en `127.0.0.1`.

**Convención de frontend:** nada de `onclick="..."` ni `<script>` con código dentro; todo el JavaScript va en archivos `.js`, para que la CSP de la versión segura no rompa la app.

## Módulos

| Módulo | Vulnerabilidad | Material |
|---|---|---|
| **M1** | Inyección SQL | `requests/01-sqli.http`, `soluciones/01-sqli.md` |
| **M2** | Datos sensibles y hashing | `requests/02-datos-sensibles.http`, `soluciones/02-datos-sensibles.md` |
| **M3** | XSS almacenado | `requests/03-xss.http`, `soluciones/03-xss.md` |
| **M4** | CORS y cabeceras HTTP | `soluciones/04-cors-cabeceras.md` (por navegador) |
| **M5** | CSRF | `soluciones/05-csrf.md` (por navegador) |
| **M6** | Fuerza bruta y rate limit | `requests/06-rate-limit.http`, `soluciones/06-rate-limit.md` |
| Extra | IDOR (opcional) | `requests/07-idor.http`, `soluciones/07-idor.md` |

### M1 — Inyección SQL

Hibernate parametriza por defecto; para la lección, la versión vulnerable arma a propósito una consulta nativa concatenando el texto del usuario.

- **Vulnerable (3000):** `POST /api/login` y `GET /api/products?q=` pegan la entrada en el SQL y devuelven el error de la base al cliente.
  - `admin' --` como usuario (cualquier contraseña) → entra como admin.
  - `'` en el buscador → error de SQL visible.
  - `' UNION SELECT id, username, password, 0 FROM users --` → usuarios y contraseñas en la lista de productos.
- **Seguro (3001):** consultas parametrizadas con Spring Data y un `@ExceptionHandler` que manda el detalle al log y al cliente un mensaje genérico. Los tres ataques fallan (401, `[]`, `[]`).
- **Cuidado:** los payloads no deben llevar `:` ni `?` (Hibernate los toma como parámetros).

### M2 — Datos sensibles y hashing

- **Vulnerable (3000):** las contraseñas se guardan en claro y un login válido responde con la **entidad `User` completa** (incluye `password`). Visible también en la consola H2 (`http://localhost:3000/h2-console`, JDBC `jdbc:h2:mem:tienda`, `sa` sin contraseña).
- **Seguro (3001):** contraseñas con **BCrypt**, verificación con `PasswordEncoder.matches(...)` y respuesta mediante un **DTO** `{id, username, role}` sin contraseña.

### M3 — XSS almacenado

- **Vulnerable (3000):** los comentarios se guardan sin filtrar y el frontend los pinta con `innerHTML`. `<img src=x onerror=alert(document.cookie)>` muestra la cookie `sid_vuln` (legible porque no es `HttpOnly`).
- **Seguro (3001), tres capas:** `textContent` en el frontend; OWASP Java HTML Sanitizer al guardar; cabecera `Content-Security-Policy` (`script-src 'self'`) y cookie `HttpOnly`.
- **Demo de la CSP sola:** arrancar `patched` con `"-Dspring-boot.run.arguments=--lab.demo.csp-solo=true"` y abrir `http://localhost:3001/csp-demo.html`: el payload llega al DOM, pero la CSP bloquea el script (visible en la consola de Chrome).

### M4 — CORS y cabeceras HTTP

- **Vulnerable (3000):** CORS permisivo (`allowedOriginPatterns("*")` + `allowCredentials(true)`) y cabeceras de seguridad apagadas (`headers.disable()`). El sitio atacante (`127.0.0.1:4000`) lee con `fetch(..., { credentials: "include" })` el perfil y los comentarios de la víctima.
- **Seguro (3001):** CORS con **lista explícita** de orígenes, cabeceras de seguridad completas y CSP. El `fetch` del atacante queda bloqueado por el navegador.

### M5 — CSRF (cambio de correo)

- **Vulnerable (3000):** `POST /api/account/email` solo comprueba la sesión; sin token CSRF y con la cookie `SameSite=None`, el formulario oculto de `premio.html` cambia el correo de la víctima desde otro sitio.
- **Seguro (3001):** CSRF activo para `/api/account/**` con `CookieCsrfTokenRepository.withHttpOnlyFalse()`; el frontend pide el token en `GET /api/csrf` y lo reenvía en `X-XSRF-TOKEN`. El ataque sin token recibe **403**.

### M6 — Fuerza bruta y rate limit

- **Vulnerable (3000):** sin límite de intentos; un ataque de diccionario (`attacks/DictionaryAttack`) prueba contraseñas hasta acertar.
- **Seguro (3001):** 5 intentos por IP cada 60 s; el sexto responde **HTTP 429** con cabeceras `RateLimit-Limit`, `RateLimit-Remaining`, `RateLimit-Reset` y `Retry-After`.

### Extra — IDOR (opcional)

`GET /api/users/{id}`: en 3000 cualquier usuario con sesión lee los datos de otro cambiando el número de la URL; en 3001 se comprueba que el id sea el de la sesión (403 si no). No está en la agenda de 60 minutos.

### Reto CTF (tarea para la casa)

La base tiene una tabla `secreto_ctf` que ninguna pantalla muestra. El reto es leer su bandera con la inyección SQL del buscador vulnerable (`git checkout modulo-0`, perfil `vulnerable`). **Pista:** `INFORMATION_SCHEMA` (H2 no tiene `sqlite_master`). Bandera: `FLAG{union_select_information_schema}`. Solución en `soluciones/00-ctf.md`.

## Marcador y `/dashboard`

Los controladores y filtros llaman a `Scoreboard.registrar(modulo, ataque, resultado)` **solo cuando hay un ataque** (payload, petición cross-site, exceso de intentos), así que el uso normal de la tienda no ensucia el marcador. Cada llamada agrega una línea a `scoreboard.jsonl`; `GET /dashboard` la lee y empareja la fila de 3000 (`exito`) con la de 3001 (`bloqueado`). Los textos de `ataque` son idénticos en ambas versiones (`shared/AtaquesInyeccion.java` y `shared/AtaquesTaller.java`).

| Módulo | Ataque | 3000 | 3001 |
|---|---|---|---|
| `M1` | `Login sin contraseña (admin' --)` / `Error SQL visible (comilla)` / `UNION SELECT a users` / `Reto CTF (secreto_ctf)` | `exito` | `bloqueado` |
| `M2` | `Contraseña expuesta en el login` | `exito` | `bloqueado` |
| `M3` | `XSS almacenado (<img onerror>)` | `exito` | `bloqueado` |
| `M4` | `Lectura de datos desde otro sitio (CORS)` | `exito` | `bloqueado` |
| `M5` | `Cambio de correo sin consentimiento (CSRF)` | `exito` | `bloqueado` |
| `M6` | `Fuerza bruta al login (sin límite)` | `exito` | `bloqueado` |
| `IDOR` | `Leer el perfil de otro usuario` | `exito` | `bloqueado` |

## Tags de git (puntos de rescate)

El repositorio incluye tags `modulo-0` … `modulo-6`: puntos de rescate para la demo en vivo. Como el proyecto mantiene **las dos versiones a la vez**, cada tag marca el punto de la historia donde ese módulo quedó terminado; `modulo-N` contiene los módulos `0..N` ya completos (vulnerable y corregido lado a lado).

| Tag | Contenido |
|-----|-----------|
| `modulo-0` | Base: esqueleto, los tres perfiles, estructura de paquetes y datos sembrados. Todo vulnerable. Punto de rescate del **reto CTF**. |
| `modulo-1` | + M1 Inyección SQL corregida. |
| `modulo-2` | + M2 Datos sensibles (BCrypt y respuesta sin contraseña). |
| `modulo-3` | + M3 XSS (sanitizado en servidor y `textContent` en cliente). |
| `modulo-4` | + M4 CORS y cabeceras (lista blanca, cabeceras de seguridad y CSP). |
| `modulo-5` | + M5 CSRF (token y cookie `SameSite=Lax`). |
| `modulo-6` | + M6 Fuerza bruta (límite de intentos). |

Los tags los crea una sola persona, al final y en orden. Para el lab guiado "parchéalo tú" se parte de `git checkout modulo-N` (estado vulnerable con lo anterior ya corregido), se escribe el fix siguiendo la `soluciones/0N-*.md` y se re-levanta el perfil.

### Nota sobre `SameSite`

El taller fuerza `SameSite=None` en la versión vulnerable **a propósito**: los navegadores actuales ya usan `SameSite=Lax` por defecto, que por sí solo mitigaría buena parte de CSRF. Chrome solo acepta `SameSite=None` si la cookie es `Secure`; en `localhost`/`127.0.0.1` lo trata como contexto seguro, por eso funciona sin HTTPS en la demo.

## Estructura del proyecto

- `vulnerable/`, `patched/`, `evil/`: controladores y configuración por perfil (`@Profile`).
- `shared/`: componentes comunes (`Seed`, `Scoreboard`, `DashboardController`, sesión, etiquetas del marcador).
- `domain/`, `repository/`, `service/`: entidades JPA, repositorios y servicios.
- `requests/`: peticiones `.http` (REST Client) por módulo, en parejas 3000/3001.
- `soluciones/`: explicación de cada módulo (archivo, código antes/después, por qué).
- `attacks/`: `DictionaryAttack` (M6) y `passwords.txt`.
- `scripts/`: `check.bat`.
