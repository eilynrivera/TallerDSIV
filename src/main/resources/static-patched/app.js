// Frontend SEGURO (perfil patched, puerto 3001).
// Mismo flujo que la version vulnerable, pero los comentarios se pintan con
// textContent (capa 1 de defensa de M3): aunque llegara HTML, no se ejecuta.

const api = (path, options) =>
    fetch(path, { credentials: "same-origin", ...options });

const json = (body) => ({
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
});

// ---------- Login (M1 / M2) ----------
async function login(event) {
    event.preventDefault();
    const salida = document.getElementById("login-result");
    const resp = await api("/api/login", {
        method: "POST",
        ...json({
            username: document.getElementById("login-user").value,
            password: document.getElementById("login-pass").value,
        }),
    });
    const texto = await resp.text();
    // En patched la respuesta es un DTO sin contrasena.
    salida.textContent = "HTTP " + resp.status + "\n" + texto;
    cargarEmail();
}

// ---------- Mi cuenta (M5 CSRF) ----------
function leerCookie(nombre) {
    const par = document.cookie.split("; ").find((c) => c.startsWith(nombre + "="));
    return par ? decodeURIComponent(par.split("=")[1]) : "";
}

async function cargarEmail() {
    const el = document.getElementById("email-actual");
    const resp = await api("/api/account/email");
    el.textContent = resp.ok ? ((await resp.json()).email || "(vacío)") : "(iniciá sesión)";
}

async function cambiarEmail(event) {
    event.preventDefault();
    const email = document.getElementById("email-nuevo").value;
    // DEFENSA (M5): se pide el token CSRF y se envia en la cabecera X-XSRF-TOKEN.
    await api("/api/csrf");
    const token = leerCookie("XSRF-TOKEN");
    const resp = await api("/api/account/email", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            "X-XSRF-TOKEN": token,
        },
        body: "email=" + encodeURIComponent(email),
    });
    document.getElementById("email-result").textContent =
        "HTTP " + resp.status + "\n" + (await resp.text());
    cargarEmail();
}

// ---------- Perfil / sesion ----------
async function verPerfil() {
    const salida = document.getElementById("profile-result");
    const resp = await api("/api/profile");
    salida.textContent = resp.status === 200
        ? await resp.text()
        : "Sin sesion (HTTP " + resp.status + ")";
}

async function cerrarSesion() {
    await api("/api/logout", { method: "POST" });
    document.getElementById("profile-result").textContent = "Sesion cerrada.";
}

// ---------- Productos (M1) ----------
async function buscar(event) {
    event.preventDefault();
    const q = document.getElementById("search-q").value;
    const cont = document.getElementById("products");
    const err = document.getElementById("products-error");
    cont.innerHTML = "";
    err.textContent = "";

    const resp = await api("/api/products?q=" + encodeURIComponent(q));
    if (!resp.ok) {
        // En patched el error es generico (sin detalle de la base).
        err.textContent = "HTTP " + resp.status + "\n" + (await resp.text());
        return;
    }
    const productos = await resp.json();
    if (productos.length === 0) {
        cont.textContent = "Sin resultados.";
        return;
    }
    for (const p of productos) {
        const div = document.createElement("div");
        div.className = "product";
        const nombre = document.createElement("span");
        nombre.className = "name";
        nombre.textContent = p.name;
        const desc = document.createElement("span");
        desc.textContent = " — " + (p.description ?? "");
        const precio = document.createElement("span");
        precio.className = "price";
        precio.textContent = "  $" + p.price;
        div.append(nombre, desc, precio);
        cont.appendChild(div);
    }
}

// ---------- Comentarios (M3) ----------
async function cargarComentarios() {
    const cont = document.getElementById("comments");
    const resp = await api("/api/comments");
    const comentarios = await resp.json();
    cont.innerHTML = "";
    for (const c of comentarios) {
        const div = document.createElement("div");
        div.className = "comment";
        // DEFENSA (M3 capa 1): textContent nunca interpreta HTML, solo lo muestra.
        const autor = document.createElement("span");
        autor.className = "author";
        autor.textContent = c.author;
        const texto = document.createTextNode(": " + c.content);
        div.append(autor, texto);
        cont.appendChild(div);
    }
}

async function comentar(event) {
    event.preventDefault();
    await api("/api/comments", {
        method: "POST",
        ...json({
            author: document.getElementById("comment-author").value,
            content: document.getElementById("comment-content").value,
        }),
    });
    document.getElementById("comment-content").value = "";
    cargarComentarios();
}

// ---------- Conexion de eventos ----------
document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("form-login").addEventListener("submit", login);
    document.getElementById("btn-profile").addEventListener("click", verPerfil);
    document.getElementById("btn-logout").addEventListener("click", cerrarSesion);
    document.getElementById("form-search").addEventListener("submit", buscar);
    document.getElementById("form-comment").addEventListener("submit", comentar);
    document.getElementById("form-email").addEventListener("submit", cambiarEmail);
    cargarComentarios();
    cargarEmail();
});
