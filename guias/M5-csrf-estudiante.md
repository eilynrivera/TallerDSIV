# Guía del participante · M5 — CSRF (Cross-Site Request Forgery)

> **Formato:** demo guiada (+ "parchéalo tú" opcional).
> **La idea:** un sitio atacante le cambia el correo a tu cuenta **sin tu permiso**, solo porque tenías la sesión abierta. Vas a ver el ataque en la tienda **vulnerable** (`:3000`) y comprobar que la **segura** (`:3001`) lo bloquea con **403**.
> **Tiempo estimado:** ~6 min.

**Guías del taller:** [M4 — CORS](M4-cors-estudiante.md) · **M5 — CSRF (estás aquí)**

---

## Contenido

- [0. El escenario (3 puertos)](#0-el-escenario-3-puertos)
- [1. Mirá el ataque funcionando](#1-mirá-el-ataque-funcionando)
- [2. Errores comunes (leé esto antes)](#2-errores-comunes-leé-esto-antes)
- [3. Cómo se defiende la :3001](#3-cómo-se-defiende-la-3001)
- [4. Repetir el ataque contra la :3001](#4-repetir-el-ataque-contra-la-3001)
- [5. Qué debés ver](#5-qué-debés-ver)
- [Si algo sale raro](#si-algo-sale-raro)

---

## 0. El escenario (3 puertos)

| Puerto | Qué es | Su papel en M5 |
|---|---|---|
| **`localhost:3000`** | La tienda **vulnerable** | Donde el ataque **funciona** |
| **`localhost:3001`** | La tienda **segura** (ya resuelta) · **(TallerDSIV)** | Tu **referencia**: acá el ataque da **403** |
| **`127.0.0.1:4000`** | El **sitio atacante** | Hospeda `premio.html`, que dispara el CSRF |

> **(TallerDSIV)** La tienda segura `:3001`, la carpeta `patched/` y el marcador `dashboard.html` pertenecen al proyecto completo **TallerDSIV**. En esta guía se usan solo como **referencia**.

**La vulnerabilidad:** `POST /api/account/email` en `:3000` solo comprueba que **haya sesión**; no exige **token CSRF**, y la cookie va con `SameSite=None`. Por eso un formulario oculto en `premio.html` (`:4000`) puede enviar esa petición **con tu cookie** y cambiar tu correo sin que vos hagas nada.

[↑ Contenido](#contenido)

---

## 1. Mirá el ataque funcionando

1. Iniciá sesión en la tienda **`http://localhost:3000`** (`admin` / `admin123`).
2. Andá a **"Mi cuenta"** y mirá el **"Correo actual"**. Con el usuario `admin` arranca en **`(vacío)`**. **No escribas nada acá.**
3. En otra pestaña abrí el **sitio atacante**. Dos caminos, los dos terminan igual:
   - **Ruta A (mejor para narrar):** abrí la portada **`http://127.0.0.1:4000`** y hacé clic en el enlace **"Abrí el regalo"** → te lleva a `premio.html`.
   - **Ruta B (atajo):** abrí directo **`http://127.0.0.1:4000/premio.html`**.
4. `premio.html` **se autoenvía al cargar** (no hay botón que tocar *dentro* de ella): dispara un `POST /api/account/email` a `:3000` llevando tu cookie de sesión.
5. Volvé a la pestaña de `:3000` y **recargá "Mi cuenta" (F5)**: el "Correo actual" pasó **solo** de `(vacío)` a **`atacante@evil.test`**.
6. El **marcador** registra `M5 · Cambio de correo sin consentimiento (CSRF)` como **éxito** en `:3000`.

> **Narralo en voz alta:** la víctima solo hizo clic en algo inocente ("Abrí el regalo") y, sin saberlo, le cambiaron el correo. Funciona porque la cookie va forzada a `SameSite=None` — hoy Chrome usa `Lax` por defecto; el taller fuerza `None` **a propósito** para que el ataque sea posible.

[↑ Contenido](#contenido)

---

## 2. Errores comunes (leé esto antes)

> **El ataque NO se escribe a mano. Se dispara solo.** Este es el malentendido #1.

- **No tipees el correo en "Mi cuenta".** Si escribís `atacante@evil.test` ahí y le das "Cambiar correo", es un cambio **legítimo del mismo sitio** (`Origin = localhost:3000`) → el marcador **no** cuenta. El ataque tiene que venir de `:4000`.
- **"Mi cuenta" no es donde se lanza el ataque.** Es solo el **verificador**: ahí mirás el correo antes y después. El ataque vive en **`:4000/premio.html`**.
- **El clic en "Abrí el regalo" no es el ataque.** Es solo **navegar** desde la portada del atacante hacia `premio.html`. El ataque es el `POST` que `premio.html` dispara **solo al cargar**.
- **Que el correo esté en `(vacío)` al inicio es normal.** El usuario `admin` se siembra sin correo. La prueba del ataque es que pase de `(vacío)` a `atacante@evil.test` **sin que vos escribas nada**.

> **Por qué el marcador distingue:** el servidor solo registra M5 cuando la cabecera `Origin` de la petición **no** coincide con su propio origen (lo compara `AtaquesTaller.esCruzada`). El POST desde tu formulario trae `Origin: http://localhost:3000` (mismo sitio, no cuenta); el POST desde `premio.html` trae `Origin: http://127.0.0.1:4000` (cruzado, **sí** cuenta).

[↑ Contenido](#contenido)

---

## 3. Cómo se defiende la :3001

La tienda segura `:3001` **(TallerDSIV)** bloquea el ataque con **tres piezas** (ya aplicadas en `patched/`):

**a) Activar CSRF solo para `/api/account/**`** — `patched/SecurityConfig.java`

```java
// ✗ Antes (vulnerable): sin token, cualquier POST con la cookie se acepta
.csrf(csrf -> csrf.disable());
```

```java
// ✓ Después (corregido)
.csrf(csrf -> csrf
        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
        .ignoringRequestMatchers("/api/login", "/api/logout",
                "/api/comments", "/h2-console/**"));
```

Con CSRF activo, `POST /api/account/email` exige el token. El formulario del atacante no lo tiene → **403**. Se **ignora** login, logout, comentarios y H2 para no romper M1, M3 y M6, que mandan POST sin token a propósito.

**b) Entregar el token al frontend legítimo** — `patched/CsrfController.java`

```java
@GetMapping("/api/csrf")
public CsrfToken csrf(CsrfToken token) {
    return token; // escribe la cookie XSRF-TOKEN; el front la reenvía en X-XSRF-TOKEN
}
```

El frontend legítimo pide el token con `GET /api/csrf` y lo manda en la cabecera `X-XSRF-TOKEN`; por eso su petición **sí** pasa (200) y la del atacante no.

**c) Cookie `SameSite=Lax` en vez de `None`** — `application-patched.properties`

```properties
server.servlet.session.cookie.same-site=lax
```

`SameSite=Lax` hace que el navegador **no** envíe la cookie de sesión en un POST que nace de otro sitio. Es una **segunda barrera** además del token.

[↑ Contenido](#contenido)

---

## 4. Repetir el ataque contra la :3001

> **Ojo:** el `action` de `premio.html` está **fijo en `localhost:3000`**. Para atacar la `:3001` necesitás una **copia** de `premio.html` cuyo `action` apunte a `http://localhost:3001/api/account/email` (o cambiar esa línea temporalmente).

1. Iniciá sesión en **`http://localhost:3001`** y mirá el correo en "Mi cuenta".
2. Abrí la copia de `premio.html` que apunta a `:3001`.
3. El `POST` del atacante recibe **403** (no trae el token) y el correo **no cambia**. El frontend legítimo **sí** sigue funcionando (manda `X-XSRF-TOKEN`).
4. El marcador registra la defensa como **bloqueado** en `:3001`.

[↑ Contenido](#contenido)

---

## 5. Qué debés ver

| Ataque | `:3000` (vulnerable) | `:3001` (segura) **(TallerDSIV)** |
|---|---|---|
| `premio.html` hace `POST /api/account/email` | El correo **cambia** a `atacante@evil.test` (200) | **403**: sin token CSRF, se rechaza |
| Cambio de correo desde el frontend legítimo | Funciona | Funciona (manda `X-XSRF-TOKEN`) |
| Marcador M5 | **✗ éxito** (ataque logrado) | **✓ bloqueado** |

**La idea clave de M5:** sin token CSRF, **cualquier** POST que lleve la cookie se acepta. La defensa es el **token** (que el atacante no puede leer desde otro sitio) + la cookie **`SameSite=Lax`**.

[↑ Contenido](#contenido)

---

## Si algo sale raro

- **El correo sigue en `(vacío)` tras abrir `premio.html`:** ¿está corriendo el perfil **evil** (`:4000`)? ¿Estás en **Chrome normal**? La cookie es `SameSite=None`; si el navegador no la manda cross-site, el POST llega sin sesión y no cambia nada.
- **No aparece el marcador M5:** casi seguro escribiste el correo **a mano** (mismo sitio, no cuenta). Reintentá abriendo **`:4000/premio.html`** y recargando "Mi cuenta" sin tocar el campo.
- **El `403` no aparece en `:3001`:** la copia de `premio.html` sigue apuntando a `:3000`; revisá que el `action` diga `localhost:3001`.
- **Dejaste el correo cambiado:** reiniciar el perfil restaura los datos (están en memoria), o cambialo de vuelta desde "Mi cuenta".

---

Taller OWASP · Módulo M5 (CSRF) · Guía del participante
