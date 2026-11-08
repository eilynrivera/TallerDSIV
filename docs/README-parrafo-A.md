# Párrafo de la Persona A para el README

Contenido que A aporta al README (lo integra B): M1 Inyección SQL, M3 XSS, el reto
CTF, el extra IDOR y la convención del marcador. Pegar estas secciones donde corresponda.

---

## M1 — Inyección SQL

Hibernate parametriza las consultas por defecto, así que **no** es vulnerable si se
usan repositorios. Para la lección, la versión vulnerable arma a propósito una
consulta nativa concatenando el texto del usuario.

- **Vulnerable (3000):** `POST /api/login` y `GET /api/products?q=` pegan la entrada
  dentro del SQL y devuelven al cliente el mensaje de error de la base.
  - `admin' --` como usuario (cualquier contraseña) → entra como admin.
  - `'` en el buscador → error de SQL visible.
  - `' UNION SELECT id, username, password, 0 FROM users --` → usuarios y
    contraseñas en la lista de productos.
- **Seguro (3001):** consultas parametrizadas con Spring Data
  (`findByNameContainingIgnoreCase`, `findByUsername`) y un `@ExceptionHandler` que
  manda el detalle al log y al cliente solo un mensaje genérico. Los tres ataques
  fallan: 401, `[]` y `[]`.
- **Cuidado:** los payloads no deben llevar `:` ni `?`, porque Hibernate los
  interpreta como parámetros dentro de la consulta nativa.
- Material: `requests/01-sqli.http`, `soluciones/01-sqli.md`.

## M3 — XSS almacenado

- **Vulnerable (3000):** los comentarios se guardan sin filtrar y el frontend los
  pinta con `innerHTML`. `<img src=x onerror=alert(document.cookie)>` muestra la
  cookie `sid_vuln`, que es legible porque la cookie no es `HttpOnly`.
- **Seguro (3001), tres capas:**
  1. `textContent` en el frontend.
  2. OWASP Java HTML Sanitizer al guardar (elimina todas las etiquetas).
  3. Cabecera `Content-Security-Policy` (`script-src 'self'`) y cookie `HttpOnly`.
- **Demo de la CSP sola:** arrancar `patched` con
  `"-Dspring-boot.run.arguments=--lab.demo.csp-solo=true"` (apaga el sanitizer) y
  abrir `http://localhost:3001/csp-demo.html` (pinta con `innerHTML`). El payload
  llega al DOM, pero no hay `alert`: la consola de Chrome muestra que la CSP lo bloqueó.
- Material: `requests/03-xss.http`, `soluciones/03-xss.md`.

## Reto CTF (tarea para la casa)

En la base hay una tabla, `secreto_ctf`, que ninguna pantalla muestra. El reto es
leer su bandera con la inyección SQL del buscador de la versión vulnerable
(`git checkout modulo-0` y el perfil `vulnerable`).

**Pista:** la base guarda un catálogo de sus propias tablas, `INFORMATION_SCHEMA`
(H2 no tiene `sqlite_master`).

La solución completa está en `soluciones/00-ctf.md`: no la abras antes de intentarlo.

## Extra opcional — IDOR

`GET /api/users/{id}`: en 3000 cualquier usuario con sesión lee los datos de otro
cambiando el número de la URL; en 3001 se comprueba que el id sea el de la sesión
(403 si no). No está en la agenda de 60 minutos. Material:
`requests/07-idor.http`, `soluciones/07-idor.md`.

## Marcador: qué registran M1, M3 e IDOR

Los controladores llaman a `Scoreboard.registrar(modulo, ataque, resultado)` solo
cuando la petición trae un payload (comilla, `UNION`, HTML), así que el uso normal
de la tienda no ensucia el marcador. El `resultado` es `exito` en 3000 (el ataque
funcionó) y `bloqueado` en 3001 (el ataque falló). Los textos de `ataque` son
idénticos en las dos versiones (`shared/AtaquesInyeccion.java`), para que el
dashboard empareje las filas:

| Módulo | Ataque |
|---|---|
| `M1` | `Login sin contraseña (admin' --)` |
| `M1` | `Error SQL visible (comilla)` |
| `M1` | `UNION SELECT a users` |
| `M1` | `Reto CTF (secreto_ctf)` |
| `M3` | `XSS almacenado (<img onerror>)` |
| `IDOR` | `Leer el perfil de otro usuario` |

## Plantillas del material

- `requests/_plantilla.http`: variables `@vuln` y `@safe` arriba, un bloque por
  ataque en parejas `Xa` (3000) / `Xb` (3001) con el resultado esperado en el título.
- `soluciones/_plantilla.md`: para cada cambio, el archivo, el código antes y
  después, y una o dos líneas de por qué.
