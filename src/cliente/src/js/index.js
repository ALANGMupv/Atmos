// index.js: carga medidas desde la API y actualiza la tabla
// -----------------------------------------------------------------------------
// Este archivo es el punto de entrada del front-end que se encarga de:
// - Conectarse a la API en el servidor Node/Express (backend).
// - Obtener los datos de medidas (mediciones de gas, temperatura, etc.).
// - Manejar errores si el servidor no responde o devuelve problemas.
// -----------------------------------------------------------------------------

// Si cambia la ruta en el servidor, modificar aquí.
window.API_BASE = "https://nagufor.upv.edu.es";

// -----------------------------------------------------------------------------
// Referencias a elementos del DOM (HTML)
// -----------------------------------------------------------------------------
const tbody = document.getElementById("tbody-medidas"); // <tbody> de la tabla donde se pintan las filas de medidas
const errorBox = document.getElementById("error");      // Caja de error (div o span) para mostrar mensajes de error al usuario
const limitSel = document.getElementById("limit");      // Selector de límite (input/select) que define cuántas filas pide el front

// -----------------------------------------------------------------------------
// Construye la URL de la API con el límite seleccionado
// Ejemplo: si limitSel.value = 100 → "https://aguemar.upv.edu.es/medidas?limit=100"
// -----------------------------------------------------------------------------
function construirURL() {
    const limit = encodeURIComponent(limitSel.value || 1); // encodeURIComponent evita caracteres raros; si no hay valor, usa 1
    return `${window.API_BASE}/medidas?limit=${limit}`;    // Devuelve la URL lista para el fetch
}

// -----------------------------------------------------------------------------
// Carga las medidas desde la API y actualiza la tabla en la página
// - Hace la petición GET con fetch()
// - Convierte la respuesta en JSON
// - Pinta los datos en la tabla (o muestra error si algo falla)
// -----------------------------------------------------------------------------
async function cargarMedidas() {
    try {
        errorBox.hidden = true; // Antes de empezar, oculta cualquier error previo

        // Petición HTTP → fetch por defecto usa método GET si no se indica "method"
        const res = await fetch(construirURL(), { headers: { "Accept": "application/json" } });

        // Si la respuesta de la API no está en el rango 200–299, lanzamos un error
        if (!res.ok) throw new Error(`HTTP ${res.status}`);

        // Convertimos el body de la respuesta en objeto JSON
        const data = await res.json();

        // Pinta las filas en la tabla (si no hay medidas, pasa [])
        pintarFilas(data.medidas || []);
    } catch (e) {
        // Si algo falla en el fetch o en el parseo JSON:
        console.error(e); // log en consola para debug

        // Mensaje visible para el usuario
        mostrarError("No se pudieron cargar las medidas. ¿Servidor Node activo?");

        // fallback visual: muestra fila vacía con texto "Sin datos"
        tbody.innerHTML = `<tr><td colspan="7" class="muted">Sin datos</td></tr>`;
    }
}

// -----------------------------------------------------------------------------
// Genera las filas de la tabla HTML a partir de los datos recibidos de la API
// Recibe: filas (array de objetos con {id_medida, id_placa, tipo, valor, latitud, longitud, fecha})
// -----------------------------------------------------------------------------
function pintarFilas(filas) {
    if (!filas.length) {
        // Si no hay filas, mensaje "No hay medidas"
        tbody.innerHTML = `<tr><td colspan="7" class="muted">No hay medidas</td></tr>`;
        return;
    }

    // Map transforma cada objeto en una <tr> (string con HTML)
    // join("") une todas las filas en un único string de HTML
    // Se llama escaparHTML() para evitar inyección de HTML en el UUID
    tbody.innerHTML = filas.map(r => {
        const [fecha, hora] = formatearFecha(r.fecha_hora || r.fecha || r.created_at);
        const valorFormateado = parseFloat(r.valor).toFixed(3); // fuerza 3 decimales
        return `
        <tr>
          <td>${r.id_medida}</td>
          <td>${escaparHTML(r.id_placa)}</td>
          <td>${gasATexto(r.tipo)}</td>
          <td>${valorFormateado}</td>
          <td>${r.id_contador ?? "-"}</td>
          <td>${r.latitud}, ${r.longitud}</td>
          <td>${fecha}</td>
          <td>${hora}</td>
        </tr>
      `;
    }).join("");
}
// -----------------------------------------------------------------------------
// Muestra un mensaje de error en la caja de error (errorBox)
// -----------------------------------------------------------------------------
function mostrarError(msg) {
    errorBox.textContent = msg; // Escribe el texto del error
    errorBox.hidden = false;    // Hace visible la caja
}

// -----------------------------------------------------------------------------
// Convierte el código numérico de gas a un texto descriptivo
// Ej: 11 → "CO₂", 12 → "Temperatura"
// -----------------------------------------------------------------------------
function gasATexto(gas) {
    console.log("Valor recibido en gasATexto:", gas, typeof gas); // Log para depuración
    const num = parseInt(gas, 10); // parseo a número entero (decimal base 10)
    if (num === 11) return "CO₂";
    if (num === 12) return "Temperatura";
    return gas; // si no coincide, devuelve el valor tal cual
}

// -----------------------------------------------------------------------------
// Formatea una fecha ISO a un array [fecha, hora]
// - Entrada típica: "2025-09-30T20:41:03.000Z" (ISO) o "2025-09-30 20:41:03"
// - Salida: ["30/09/2025", "20:41:03"]
// -----------------------------------------------------------------------------
function formatearFecha(iso) {
    try {
        let d = new Date(iso); // Intentamos crear objeto Date a partir del string
        if (isNaN(d)) {
            // Caso: el formato no es reconocido (ejemplo con espacio en lugar de "T")
            const [fecha, hora] = iso.split(" ");
            return [fecha || iso, hora || ""];
        }
        // Localización: formato español (dd/mm/yyyy, hh:mm:ss)
        const fecha = d.toLocaleDateString("es-ES", { day: "2-digit", month: "2-digit", year: "numeric" });
        const hora = d.toLocaleTimeString("es-ES", { hour: "2-digit", minute: "2-digit", second: "2-digit" });
        return [fecha, hora];
    } catch {
        // fallback si todo falla: devuelve el string original y hora vacía
        return [iso, ""];
    }
}

// -----------------------------------------------------------------------------
// Escapa caracteres peligrosos en strings dinámicos para evitar XSS
// Sustituye &, <, >, ", ' por sus entidades HTML
// -----------------------------------------------------------------------------
function escaparHTML(s) {
    return String(s).replace(/[&<>"']/g, m => ({
        "&":"&amp;",
        "<":"&lt;",
        ">":"&gt;",
        "\"":"&quot;",
        "'":"&#39;"
    }[m]));
}

// -----------------------------------------------------------------------------
// Eventos de usuario y auto-refresh
// -----------------------------------------------------------------------------
// 1. Cuando el usuario cambia el valor del selector de límite, recarga datos
limitSel.addEventListener("change", cargarMedidas);

// 2. Auto-refresh: cada 5 segundos recarga los datos de la API automáticamente
setInterval(cargarMedidas, 3000);

// 3. Primera carga de datos al abrir la página
cargarMedidas();
