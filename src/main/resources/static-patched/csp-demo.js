// Demo M3 "CSP sola" (Persona A). Perfil patched, puerto 3001.
//
// A PROPÓSITO esta página usa innerHTML (como la versión vulnerable) para quitar la
// capa 1. Con lab.demo.csp-solo=true el servidor tampoco sanitiza (capa 2), así que
// el único freno es la CSP: script-src 'self' bloquea onerror="..." y cualquier
// <script> en línea. No se usa en la tienda normal (index.html sigue con textContent).

const api = (path, options) =>
    fetch(path, { credentials: "same-origin", ...options });

async function cargarComentarios() {
    const cont = document.getElementById("comments");
    const resp = await api("/api/comments");
    const comentarios = await resp.json();
    cont.innerHTML = "";
    let hayHtml = false;
    for (const c of comentarios) {
        if (c.content && c.content.includes("<")) {
            hayHtml = true;
        }
        const div = document.createElement("div");
        div.className = "comment";
        // Sin escapar, a propósito: aquí la CSP es la que tiene que detener el ataque.
        div.innerHTML = '<span class="author">' + c.author + "</span>: " + c.content;
        cont.appendChild(div);
    }
    // Si el HTML de un comentario llegó intacto, el sanitizer está apagado.
    document.getElementById("estado").textContent = hayHtml
        ? "APAGADO (lab.demo.csp-solo=true): solo protege la CSP"
        : "sin HTML guardado todavía (o sanitizer encendido)";
}

async function comentar(event) {
    event.preventDefault();
    await api("/api/comments", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            author: document.getElementById("comment-author").value,
            content: document.getElementById("comment-content").value,
        }),
    });
    document.getElementById("comment-content").value = "";
    cargarComentarios();
}

document.getElementById("form-comment").addEventListener("submit", comentar);
cargarComentarios();
