// Vista del marcador del taller OWASP.
// Lee el MISMO endpoint JSON (GET /dashboard) y lo pinta en verde/rojo.
// Se usa textContent (no innerHTML) porque las etiquetas de ataque contienen
// HTML real (p. ej. "<img onerror>" de M3): pintarlas como HTML seria un auto-XSS.

const ORDEN = ["M1", "M2", "M3", "M4", "M5", "M6", "IDOR"];
const REFRESCO_MS = 2000;

function pill(tipo, port, texto) {
  const span = document.createElement("span");
  span.className = "pill " + tipo;
  const p = document.createElement("span");
  p.className = "port";
  p.textContent = ":" + port + "  ";
  span.appendChild(p);
  span.appendChild(document.createTextNode(texto));
  return span;
}

function render(datos) {
  const cont = document.getElementById("modulos");
  cont.textContent = "";

  if (!Array.isArray(datos) || datos.length === 0) {
    const vacio = document.createElement("div");
    vacio.className = "empty";
    vacio.textContent = "Todavia no hay ataques registrados. Lanzen un ataque y el marcador se llena solo.";
    cont.appendChild(vacio);
    actualizarProgreso(0, 0);
    return;
  }

  const grupos = {};
  for (const it of datos) {
    (grupos[it.modulo] = grupos[it.modulo] || []).push(it);
  }

  const claves = Object.keys(grupos).sort((a, b) => {
    const ia = ORDEN.indexOf(a), ib = ORDEN.indexOf(b);
    return (ia < 0 ? 99 : ia) - (ib < 0 ? 99 : ib);
  });

  let total = 0, verdes = 0;

  for (const k of claves) {
    const card = document.createElement("section");
    card.className = "card";

    const h = document.createElement("h2");
    h.textContent = k;
    card.appendChild(h);

    for (const it of grupos[k]) {
      total++;
      const exito = it.vulnerable === "exito";
      const bloqueado = it.patched === "bloqueado";
      if (bloqueado) verdes++;

      const row = document.createElement("div");
      row.className = "row";

      const nombre = document.createElement("div");
      nombre.className = "ataque";
      nombre.textContent = it.ataque;
      row.appendChild(nombre);

      row.appendChild(exito
        ? pill("exito", "3000", "✗ exito")
        : pill("vacio", "3000", "—"));

      row.appendChild(bloqueado
        ? pill("bloqueado", "3001", "✓ bloqueado")
        : pill("vacio", "3001", "—"));

      card.appendChild(row);
    }
    cont.appendChild(card);
  }

  actualizarProgreso(verdes, total);
}

function actualizarProgreso(verdes, total) {
  const box = document.getElementById("progreso");
  const fill = document.getElementById("fill");
  const label = document.getElementById("progreso-label");
  const pct = total === 0 ? 0 : Math.round((verdes / total) * 100);
  fill.style.width = pct + "%";
  label.innerHTML = "";
  const b = document.createElement("b");
  b.textContent = verdes + " / " + total;
  label.appendChild(b);
  label.appendChild(document.createTextNode(" ataques bloqueados (" + pct + "%)"));
  box.classList.toggle("allgreen", total > 0 && verdes === total);
}

function marcarEstado(ok) {
  const dot = document.getElementById("estado-dot");
  const txt = document.getElementById("estado-txt");
  dot.className = "dot " + (ok ? "ok" : "off");
  txt.textContent = ok
    ? "actualizado " + new Date().toLocaleTimeString()
    : "sin conexion con /dashboard";
}

async function cargar() {
  try {
    const r = await fetch("/dashboard", { headers: { "Accept": "application/json" } });
    if (!r.ok) throw new Error("HTTP " + r.status);
    render(await r.json());
    marcarEstado(true);
  } catch (e) {
    marcarEstado(false);
  }
}

cargar();
setInterval(cargar, REFRESCO_MS);
