# M3 · XSS almacenado

**Ataque:** un comentario con `<img src=x onerror=alert(document.cookie)>` se guarda
tal cual y el frontend lo pinta con `innerHTML`; el navegador de **cada visitante**
ejecuta el JavaScript y muestra (o podría robar) la cookie de sesión `sid_vuln`.
**Cómo probarlo:** navegador en `http://localhost:3000` (se ve el `alert`) y
`requests/03-xss.http` (qué guarda el servidor y qué cabeceras responde).
**Marcador:** `M3` · `XSS almacenado (<img onerror>)`, con `exito` en 3000 y `bloqueado` en 3001.

La corrección es **defensa en profundidad**: tres capas independientes. Cualquiera
de ellas sola ya detiene este payload.

## Capa 1. Pintar con `textContent`, no con `innerHTML` (frontend, C)

**Archivo:** `src/main/resources/static-patched/app.js`
(antes: `static-vulnerable/app.js`)

Antes (vulnerable):

```js
div.innerHTML = '<span class="author">' + c.author + "</span>: " + c.content;
```

Después (corregido):

```js
const autor = document.createElement("span");
autor.textContent = c.author;
const texto = document.createTextNode(": " + c.content);
div.append(autor, texto);
```

**Por qué:** `textContent` muestra el HTML como texto; el navegador nunca lo interpreta.

## Capa 2. Sanitizar al guardar (servidor)

**Archivo:** `src/main/java/taller/owasp/patched/XssPatchedRestController.java`
(antes: `vulnerable/XssVulnerableRestController.java`)

Antes (vulnerable):

```java
return commentService.guardar(datos.author(), datos.content());
```

Después (corregido):

```java
private static final PolicyFactory SIN_ETIQUETAS = new HtmlPolicyBuilder().toFactory();

String contenido = SIN_ETIQUETAS.sanitize(datos.content());
String autor = SIN_ETIQUETAS.sanitize(datos.author());
return ResponseEntity.ok(commentService.guardar(autor, contenido));
```

**Por qué:** una política vacía del OWASP Java HTML Sanitizer elimina **todas** las
etiquetas; aunque otro cliente (una app móvil, otra página) pinte con `innerHTML`,
en la base ya no hay HTML. También se valida la longitud (1–2000) y se responde 400.

## Capa 3. CSP + cookie `HttpOnly` (configuración)

**Archivos:** `src/main/java/taller/owasp/patched/SecurityConfig.java` y
`src/main/resources/application-patched.properties`

Antes (vulnerable, `application-vulnerable.properties`):

```properties
server.servlet.session.cookie.http-only=false
# y sin cabecera Content-Security-Policy
```

Después (corregido):

```java
private static final String CSP = "default-src 'self'; script-src 'self'; style-src 'self'; "
                                + "img-src 'self'; object-src 'none'";
// .headers(... new StaticHeadersWriter("Content-Security-Policy", CSP) ...)
```

```properties
server.servlet.session.cookie.http-only=true
```

**Por qué:** `script-src 'self'` prohíbe el JavaScript en línea (`onerror="..."`,
`<script>...</script>`): solo corren los `.js` servidos por la tienda. Y con
`HttpOnly`, aunque algo se ejecutara, `document.cookie` no puede leer `sid_safe`.

## Demo: la CSP sola también detiene el ataque

Para mostrar la capa 3 aislada se apagan las otras dos:

1. Arrancar `patched` con el sanitizer apagado (capa 2):

   ```powershell
   .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=patched" "-Dspring-boot.run.arguments=--lab.demo.csp-solo=true"
   ```

2. Abrir `http://localhost:3001/csp-demo.html`, que pinta con `innerHTML` (capa 1 apagada).
3. Publicar `<img src=x onerror=alert(document.cookie)>`.
4. **No aparece el `alert`.** En DevTools → Console se ve:
   *Refused to execute inline event handler because it violates the following
   Content Security Policy directive: "script-src 'self'"*. En Elements, el `<img>`
   está en el DOM: el HTML llegó, pero la CSP no dejó ejecutar el código.

Sin la propiedad (arranque normal), la página de la demo no muestra nada peligroso
porque el sanitizer ya quitó las etiquetas al guardar.

## Resultado esperado

| Prueba | 3000 (vulnerable) | 3001 (corregido) |
|---|---|---|
| Publicar `<img src=x onerror=alert(document.cookie)>` | `alert` con `sid_vuln=...` | se ve el texto sin etiquetas, no hay `alert` |
| `GET /api/comments` | el `<img onerror>` intacto | el contenido sin HTML |
| Cabecera `Content-Security-Policy` | no está | `default-src 'self'; script-src 'self'; ...` |
| Cookie de sesión | `sid_vuln` sin `HttpOnly` | `sid_safe; HttpOnly` |
