/**
 * @file index.js
 * @brief Carga medidas desde la API y actualiza la tabla del front-end.
 *
 * Este archivo es el punto de entrada del front-end y se encarga de:
 * - Conectarse a la API del servidor Node/Express.
 * - Obtener datos de medidas (gas, temperatura, etc.).
 * - Pintar las medidas en una tabla HTML.
 * - Manejar errores de conexión o de respuesta del servidor.
 */

// ==========================================================
// CONFIGURACIÓN GLOBAL
// ==========================================================

/**
 * @brief URL base de la API.
 *
 * Si cambia la ruta del servidor, debe modificarse aquí.
 */
window.API_BASE = "https://nagufor.upv.edu.es";

// ==========================================================
// REFERENCIAS AL DOM
// ==========================================================

/** <tbody> de la tabla donde se pintan las filas de medidas */
const tbody = document.getElementById("tbody-medidas");

/** Caja de error para mostrar mensajes al usuario */
const errorBox = document.getElementById("error");

/** Selector que define el número máximo de filas solicitadas */
const limitSel = document.getElementById("limit");

// ==========================================================
// CONSTRUCCIÓN DE URL
// ==========================================================

/**
 * @brief Construye la URL de la API con el límite seleccionado.
 *
 * Ejemplo:
 * - limitSel.value = 100 →
 *   https://nagufor.upv.edu.es/medidas?limit=100
 *
 * @return {string} URL lista para realizar la petición fetch.
 */
function construirURL() {
    const limit = encodeURIComponent(limitSel.value || 1);
    return `${window.API_BASE}/medidas?limit=${limit}`;
}

// ==========================================================
// CARGA DE MEDIDAS
// ==========================================================

/**
 * @brief Carga las medidas desde la API y actualiza la tabla.
 *
 * Realiza una petición GET, procesa la respuesta JSON
 * y pinta los datos en la tabla HTML.
 */
async function cargarMedidas() {
    try {
        errorBox.hidden = true;

        const res = await fetch(construirURL(), {
            headers: { "Accept": "application/json" }
        });

        if (!res.ok) {
            throw new Error(`HTTP ${res.status}`);
        }

        const data = await res.json();
        pintarFilas(data.medidas || []);

    } catch (e) {
        console.error(e);

        mostrarError("No se pudieron cargar las medidas. ¿Servidor Node activo?");
        tbody.innerHTML = `<tr><td colspan="7" class="muted">Sin datos</td></tr>`;
    }
}

// ==========================================================
// RENDERIZADO DE TABLA
// ==========================================================

/**
 * @brief Genera las filas de la tabla HTML a partir de los datos recibidos.
 *
 * @param {Array<Object>} filas Array de medidas con los campos:
 * {id_medida, id_placa, tipo, valor, latitud, longitud, fecha}
 */
function pintarFilas(filas) {
    if (!filas.length) {
        tbody.innerHTML =
            `<tr><td colspan="7" class="muted">No hay medidas</td></tr>`;
        return;
    }

    tbody.innerHTML = filas.map(r => {
        const [fecha, hora] = formatearFecha(r.fecha);
        return `
        <tr>
          <td>${r.id_medida}</td>
          <td>${escaparHTML(r.id_placa)}</td>
          <td>${gasATexto(r.tipo)}</td>
          <td>${r.valor}</td>
          <td>${r.id_contador ?? "-"}</td>
          <td>${r.latitud}, ${r.longitud}</td>
          <td>${fecha}</td>
          <td>${hora}</td>
        </tr>
      `;
    }).join("");
}

// ==========================================================
// MANEJO DE ERRORES
// ==========================================================

/**
 * @brief Muestra un mensaje de error en la interfaz.
 *
 * @param {string} msg Mensaje de error a mostrar.
 */
function mostrarError(msg) {
    errorBox.textContent = msg;
    errorBox.hidden = false;
}

// ==========================================================
// UTILIDADES
// ==========================================================

/**
 * @brief Convierte el código numérico de gas a texto descriptivo.
 *
 * @param {number|string} gas Código del gas.
 * @return {string} Texto descriptivo del gas.
 */
function gasATexto(gas) {
    console.log("Valor recibido en gasATexto:", gas, typeof gas);
    const num = parseInt(gas, 10);
    if (num === 11) return "CO₂";
    if (num === 12) return "Temperatura";
    return gas;
}

/**
 * @brief Formatea una fecha ISO en formato legible.
 *
 * @param {string} iso Fecha en formato ISO o "YYYY-MM-DD HH:mm:ss".
 * @return {Array<string>} Array con [fecha, hora].
 */
function formatearFecha(iso) {
    try {
        let d = new Date(iso);
        if (isNaN(d)) {
            const [fecha, hora] = iso.split(" ");
            return [fecha || iso, hora || ""];
        }

        const fecha = d.toLocaleDateString("es-ES", {
            day: "2-digit",
            month: "2-digit",
            year: "numeric"
        });
        const hora = d.toLocaleTimeString("es-ES", {
            hour: "2-digit",
            minute: "2-digit",
            second: "2-digit"
        });
        return [fecha, hora];

    } catch {
        return [iso, ""];
    }
}

/**
 * @brief Escapa caracteres HTML peligrosos para evitar XSS.
 *
 * @param {string} s Texto a escapar.
 * @return {string} Texto escapado.
 */
function escaparHTML(s) {
    return String(s).replace(/[&<>"']/g, m => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        "\"": "&quot;",
        "'": "&#39;"
    }[m]));
}

// ==========================================================
// EVENTOS Y AUTO-REFRESH
// ==========================================================

/**
 * @brief Recarga las medidas al cambiar el límite seleccionado.
 */
limitSel.addEventListener("change", cargarMedidas);

/**
 * @brief Auto-refresh de datos cada 3 segundos.
 */
setInterval(cargarMedidas, 3000);

/**
 * @brief Primera carga de datos al abrir la página.
 */
cargarMedidas();
