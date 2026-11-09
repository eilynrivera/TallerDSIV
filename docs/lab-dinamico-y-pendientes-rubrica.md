# Taller OWASP — Pendientes de la rúbrica y lab guiado dinámico

Dos partes: **(A)** lo que falta para cumplir la rúbrica, como checklist accionable, y
**(B)** ideas para ejecutar el laboratorio de forma dinámica (que interactúen con el
código, no solo miren el frontend) más la actividad final tipo Kahoot.

Estado base al 7 nov 2026: en `main` están M1, M2, M3, M4 y M5. Falta M6, el
marcador cableado, las soluciones, el README y los tags `modulo-1`…`modulo-6`.

---

## A. Pendientes para cumplir la rúbrica

Leyenda de dueño: **C** = Jose (navegador/infra), **A** = eilynrivera, **B** = Kimaru17, **Equipo** = los tres.
Prioridad: **Alta** = sin esto baja la nota del taller guiado; **Media** = mejora clara.

### Parte I — Valoración del taller (25 pts)

**1. Componente teórico (conexión con el ámbito empresarial)**
- [ ] Añadir 1 caso empresarial por bloque de exposición (dónde ocurrió, impacto). **Media** — A (O2), B (O1/O3), **C (O4/O5)**.
- [ ] En O4/O5, cerrar cada módulo con "qué significa esto en una empresa" (fuga de datos de clientes por CORS, cambio de datos por CSRF). **Media** — C.

**2. Taller guiado (práctica) — el criterio central**
- [ ] **M6 fuerza bruta**: `RateLimitFilter` (parche) + ataque HTTP completo. Hoy solo existe `DictionaryAttack`. Sin esto, un módulo del lab no corre. **Alta** — B.
- [ ] **Cablear el marcador**: cada controlador debe llamar `Scoreboard.registrar(modulo, ataque, resultado)`; hoy **nadie lo llama**. **Alta** — Equipo (cada quien en sus controladores).
- [ ] **Dashboard** `/dashboard` que lea `scoreboard.jsonl` (B9). Sin esto el cierre con el marcador no funciona. **Alta** — B.
- [ ] **`soluciones/*.md`** (carpeta vacía): `00-ctf`, `01-sqli`, `02-datos-sensibles`, `03-xss`, `06-rate-limit`, y agregar `04-cors` y `05-csrf`. **Alta** — A (01, 03, CTF), B (02, 06), **C (04, 05)**.
- [ ] **Archivos `.http`** faltantes: hoy solo `01-sqli.http` y `03-xss.http`. Falta M2 y M6 (M4/M5 son por navegador). **Media** — A/B.
- [ ] **Guía/handout del participante** (ver parte B): no existe; hoy el lab se sigue solo de palabra. **Alta** — Equipo (C puede liderarlo).

**3. Requerimientos técnicos (guía de software y configuración)**
- [ ] **README** con instalación, convenciones (puertos, cookies, frontend sin JS en línea), aviso ético y guía por módulo (B10). Hoy **no hay README**. **Alta** — B (C ya entregó su párrafo en `docs/README-parrafo-C.md`).
- [ ] **Instructivo de instalación para estudiantes** enviado el día anterior: JDK 21, VS Code + REST Client, Chrome, clonar y correr `check`. **Alta** — B.
- [x] `scripts/check.bat` (verifica Java 21 + puertos). Hecho — C.

**4. Profesionalismo y didáctica**
- [ ] **Ensayos** (Fase 6): ensayo 1 individual y ensayo 2 los tres juntos con cronómetro; cada quien capaz de destrabar a un estudiante. **Alta** — Equipo.
- [ ] Preparar las **estrategias didácticas dinámicas** (parte B) y la actividad Kahoot. **Alta** — Equipo.

**5. Valoración general del profesor** — no accionable directamente; se cubre con todo lo anterior.

### Parte IV — Informe oral (15 pts)
- [ ] **Diapositivas O1–O5 con plantilla uniforme** (la comparte B); integrar el contenido de cada quien. **Media** — Equipo.
- [ ] **Ensayar la narración** sin leer, con participación equitativa y reparto de preguntas. **Alta** — Equipo.
- [ ] **Casos de uso empresariales** y fragmentos de código legibles en las láminas. **Media** — Equipo.
- [x] Diapositivas O4/O5 con diagrama de 3 actores y lámina de herramientas. Hecho — C.

### Lo tuyo (Persona C), estado
- [x] M4 y M5 funcionando y verificados; diapositivas O4/O5; tag `modulo-0`; párrafo del README.
- [ ] `soluciones/04-cors.md` y `soluciones/05-csrf.md`.
- [ ] Tags `modulo-1`…`modulo-5` (ya posibles) y `modulo-6` (cuando B termine M6).
- [ ] C11 videos de respaldo (M4/M5).
- [ ] 1 caso empresarial en O4/O5.

---

## B. Cómo ejecutar el lab guiado de forma dinámica

Objetivo: que los participantes **toquen el código y la herramienta**, no solo miren
las dos tiendas. El patrón por módulo sigue siendo **atacar → comparar → entender el
parche**, pero con la persona participante haciendo, no observando.

### La pieza clave: los tags `modulo-N`
`git checkout modulo-N` deja el repo en la **versión vulnerable con los módulos
anteriores ya corregidos**. Es lo que habilita el ejercicio "parchéalo tú": el
estudiante parte de un estado conocido, escribe el arreglo del módulo N y verifica
que el ataque deja de funcionar. **Para esto hay que tener creados `modulo-1`…`modulo-6`**
(hoy solo existe `modulo-0`). Es una buena razón para cerrar C10 antes del taller.

### Cinco formatos dinámicos (de menos a más interacción)

1. **Atacar ellos mismos (REST Client / navegador).** El participante lanza el payload
   desde los `.http` en VS Code contra `:3000` (funciona) y `:3001` (falla), y lee el
   código de estado. Rápido, sin edición de código. Base para todos los módulos.

2. **Inspección con DevTools (reto de observación).** "Abran DevTools, encuentren la
   cookie de sesión, miren su `SameSite`, y provoquen el error de CORS en la consola."
   Interactivo y sin tocar código; ideal para M4/M5.

3. **Flip de una propiedad (romper/arreglar sin git).** Con una bandera en
   `application-*.properties` (p. ej. `lab.demo.csp-solo=true`) el participante activa o
   desactiva una defensa y re-levanta el perfil para ver el cambio. Menos fricción que
   editar código.

4. **"Encuentra la línea vulnerable" (diff en vivo).** Se muestran lado a lado el
   controlador vulnerable y el parcheado y se pregunta cuál es **la línea** que cambia
   el resultado (concatenación vs. parametrizado; `innerHTML` vs. `textContent`;
   `allowedOriginPatterns("*")` vs. lista blanca). Dinámico y corto.

5. **"Parchéalo tú" guiado con tags (el más hands-on).** `git checkout modulo-N`, el
   participante escribe el fix siguiendo una consigna breve, re-levanta y confirma que
   el ataque ahora da 401/403/bloqueado. Reservar para **1 o 2 módulos** por tiempo.

### Qué módulos conviene parchear: los más frecuentes e interesantes hoy

El ejercicio "parchéalo tú" se reserva para las vulnerabilidades **más frecuentes y
vigentes hoy** (según el OWASP Top 10 2021/2025), que además tengan un parche chico y
claro. Cada una la **guía su dueño**, y el participante lo sigue.

| Módulo | Relevancia hoy | Parche apto para hands-on | Guía |
|--------|----------------|----------------------------|------|
| **M3 XSS** | **Muy alta** — de lo más común en la web | Sí, limpio y visual (`innerHTML` → `textContent`) | A |
| **M4 CORS / misconfiguración** | **Alta** — era de APIs y SPAs | Sí (config: `*` → lista blanca) | **C (Jose)** |
| **M1 SQLi** | Alta, clásico e icónico | Sí (concatenación → parametrizado) | A |
| M2 Datos sensibles (hashing) | Alta, alto impacto | Medio (BCrypt toca verificación/seed) | B |
| M6 Fuerza bruta / credential stuffing | Frecuente | Pesado (agregar filtro) y aún sin implementar | B |
| M5 CSRF | **Baja hoy** — `SameSite` por defecto lo mitiga; salió del Top 10 | Config pequeña, concepto histórico | C (Jose) |

### Recomendación de formato (para 35 min y 6 módulos)
No alcanza el tiempo para "parchéalo tú" en los seis. Se hace hands-on en los más
frecuentes y se deja el resto como atacar-y-comparar o demo:

| Módulo | Formato dinámico | Guía | Por qué |
|--------|------------------|------|---------|
| **M3 XSS** | **Parchéalo tú** (`modulo-3`): `innerHTML` → `textContent` (+ sanitizer) | A | La más frecuente hoy; parche limpio y de efecto visible |
| **M4 CORS** | **Parchéalo tú** (`modulo-4`): `*` → lista blanca | **C (Jose)** | Muy vigente (APIs/SPA); edit chico, lo guiás bien |
| M1 SQLi | **Parchéalo tú si da tiempo** (`modulo-1`): concatenación → parametrizado; si no, atacar + "encuentra la línea" | A | Clásico icónico; parche didáctico |
| M2 Datos sensibles | Atacar ellos + consola H2 (claro vs. hash); **hands-on opcional** (BCrypt) para que **B** también guíe uno | B | Alto impacto; parche algo más grande |
| M6 Fuerza bruta | DevTools + ataque de diccionario proyectado | B | Depende de que B termine el `RateLimitFilter` |
| M5 CSRF | **Demo / atacar-comparar** (no hands-on) | C (Jose) | Es el menos frecuente hoy; mejor como demostración |

**En corto:** hands-on obligatorio en **M3 (A) y M4 (Jose)**; **M1 (A)** si el reloj
lo permite; **M2 (B)** como hands-on opcional para equilibrar quién guía. M5 pasa a
demostración por ser el de menor vigencia actual, aunque sea de Jose.

### Gamificación con el marcador
Si se cablea `Scoreboard.registrar(...)` y el `/dashboard`, cada ataque exitoso y cada
parche pasa de ✗ a ✓ en pantalla en vivo. Convertirlo en "pongan todo en verde" da
ritmo y motiva. (Depende de B9 + el cableado.)

### Mini-CTF en vivo (opcional, 5 min)
`git checkout modulo-0`, y el participante extrae la bandera con
`' UNION SELECT id, flag, '', 0 FROM secreto_ctf --` tras descubrir la tabla por
`INFORMATION_SCHEMA`. Buen gancho antes del cierre, o queda de tarea para la casa.

### Checklist para que el "parchéalo tú" funcione el día del taller
- [ ] Tags de los módulos elegidos creados y **pusheados** (C10): al menos `modulo-3` y `modulo-4`; `modulo-1` y `modulo-2` si se suman. (Conviene crear igual la serie completa `modulo-1`…`modulo-6` al cierre.)
- [ ] Consigna breve por módulo en `soluciones/0N-*.md` (qué archivo y qué línea tocar, resultado esperado).
- [ ] Probado que, partiendo de `modulo-N`, el fix se escribe en < 3 min y el perfil re-levanta.
- [ ] Un integrante "circula" para destrabar (rol ya previsto en el plan).
- [ ] Recordar: reiniciar el perfil restaura la base (en memoria), por si alguien la deja rara.

---

## C. Actividad final tipo Kahoot (banco de preguntas)

10–12 preguntas, una ronda de ~8 min al cierre, para reforzar lo visto. Respuesta
correcta marcada con **(✓)**. Ajustar al formato de Kahoot/Quizizz/Mentimeter.

1. ¿Qué logra el payload `admin' --` en un login vulnerable a SQLi?
   - Comenta el resto de la consulta y entra sin contraseña **(✓)**
   - Borra la tabla de usuarios
   - Cifra la contraseña
   - No hace nada

2. ¿Cuál es la defensa correcta contra la inyección SQL?
   - Consultas parametrizadas **(✓)**
   - Ocultar los mensajes de error
   - Usar HTTPS
   - Cambiar el puerto

3. En XSS almacenado, ¿por qué `textContent` es seguro y `innerHTML` no?
   - `textContent` muestra el texto sin interpretarlo como HTML **(✓)**
   - `textContent` cifra el contenido
   - `innerHTML` es más lento
   - No hay diferencia

4. ¿Para qué sirve CORS?
   - Controla qué otros orígenes pueden leer las respuestas del servidor **(✓)**
   - Cifra las cookies
   - Acelera las peticiones
   - Bloquea el SQL

5. ¿Qué combinación de CORS es peligrosa?
   - `allowedOrigins("*")` junto con `allowCredentials(true)` **(✓)**
   - Una lista blanca de orígenes
   - Deshabilitar CORS
   - Permitir solo GET

6. ¿Qué busca un ataque CSRF?
   - Que el navegador ejecute una acción con la cookie de la víctima **(✓)**
   - Leer la base de datos
   - Descifrar contraseñas
   - Saturar el servidor

7. ¿Cómo se defiende CSRF en el taller?
   - Token CSRF (`X-XSRF-TOKEN`) y cookie `SameSite=Lax` **(✓)**
   - Deshabilitando las cookies
   - Con un captcha
   - Cambiando el puerto

8. ¿Por qué el taller fuerza `SameSite=None` en la versión vulnerable?
   - Porque hoy el valor por defecto `Lax` ya mitigaría parte del CSRF **(✓)**
   - Para que sea más rápido
   - Porque lo exige HTTPS
   - Por error

9. ¿Qué cabecera ayuda a frenar XSS limitando de dónde se cargan scripts?
   - Content-Security-Policy (CSP) **(✓)**
   - Content-Type
   - Accept-Language
   - Cache-Control

10. ¿Cómo se almacenan las contraseñas en la versión segura (M2)?
    - Con un hash BCrypt **(✓)**
    - En texto claro
    - Cifradas con el puerto
    - No se guardan

11. ¿Qué hace la defensa de fuerza bruta (M6)?
    - Limita los intentos de login por minuto y responde 429 **(✓)**
    - Borra la cuenta
    - Cambia la contraseña
    - Ignora el login

12. En el reto CTF, ¿qué se consulta para descubrir las tablas de la base?
    - `INFORMATION_SCHEMA` **(✓)**
    - `sqlite_master`
    - El archivo `.env`
    - Las cookies

**Idea de cierre:** mostrar el marcador `/dashboard` en verde + el podio de Kahoot;
dejar el CTF como tarea para la casa.
