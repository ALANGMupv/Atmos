
const API_BASE_URL = "https://nagufor.upv.edu.es";
// Endpoint propuestos (A confirmar por usuario/backend):
// GET  /incidencias/todas      -> Devuelve todas las incidencias
// POST /incidencia/resolver    -> { id_incidencia, respuesta, estado }

document.addEventListener('DOMContentLoaded', () => {
    cargarIncidenciasAdmin();

    // Listener para filtros
    document.querySelectorAll('.btn-filter').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.btn-filter').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');

            // Re-aplicar filtro sobre los datos guardados en memoria
            const filter = btn.dataset.filter || 'all';
            aplicarFiltroLocal(filter);
        });
    });

    // NUEVO: Event listeners para los botones de acción
    document.getElementById('btnResolve').addEventListener('click', () => {
        enviarRespuestaAdmin('resolved');
    });

    document.getElementById('btnReject').addEventListener('click', () => {
        enviarRespuestaAdmin('rejected');
    });
});

let incidenciasCache = []; // Guardamos aquí para filtrar sin re-pedir

async function cargarIncidenciasAdmin() {
    const container = document.getElementById('adminIncidenceList');

    // Mostrar loading si estaba vacío
    if (!incidenciasCache.length) {
        container.innerHTML = `
            <div class="loading-state" style="padding: 20px; text-align: center; color: #666;">
                <i class="fa-solid fa-spinner fa-spin"></i> Cargando incidencias...
            </div>`;
    }

    try {
        // Usamos el endpoint específico para admin creado por el usuario: GET /incidencias/todas
        const response = await fetch(`${API_BASE_URL}/incidencias/todas`);

        // Manejo de error si el endpoint no existe (404)
        if (!response.ok) {
            throw new Error(`Error HTTP: ${response.status}`);
        }

        const data = await response.json();

        if (data.status === 'ok') {
            incidenciasCache = data.incidencias || [];
            // Ordenar por defecto: fecha descendente
            incidenciasCache.sort((a, b) => new Date(b.fecha) - new Date(a.fecha));

            // Renderizar todo al inicio
            renderList(incidenciasCache);
        } else {
            console.error("Error API:", data);
            container.innerHTML = `<div style="padding:20px; text-align:center;">Error al cargar datos del servidor.</div>`;
        }
    } catch (error) {
        console.error("Fetch error:", error);
        // Fallback a UI de error
        container.innerHTML = `
            <div class="empty-inbox">
                 <div class="empty-icon-wrapper" style="color:#EF4444; background:#FEF2F2;">
                    <i class="fa-solid fa-triangle-exclamation"></i>
                 </div>
                 <h3>Error de conexión</h3>
                 <p>No se pudo conectar con el servidor de incidencias.</p>
                 <button class="btn btn-primary" onclick="cargarIncidenciasAdmin()" style="margin-top:10px; font-size:12px; padding: 6px 12px;">Reintentar</button>
            </div>
        `;
    }
}

function aplicarFiltroLocal(filter) {
    if (filter === 'all') {
        renderList(incidenciasCache);
    } else {
        // Mapeo status: 'pending' -> id_estado 1 o 2. 'resolved' -> 3.
        const filtered = incidenciasCache.filter(inc => {
            if (filter === 'pending') return (inc.id_estado == 1 || inc.id_estado == 2);
            if (filter === 'resolved') return (inc.id_estado == 3);
            return true;
        });
        renderList(filtered);
    }
}

function renderList(data) {
    const container = document.getElementById('adminIncidenceList');
    container.innerHTML = '';

    if (data.length === 0) {
        // EMPTY STATE
        container.innerHTML = `
            <div class="empty-inbox">
                <div class="empty-icon-wrapper">
                    <i class="fa-regular fa-folder-open"></i>
                </div>
                <h3>Bandeja vacía</h3>
                <p>No hay incidencias en esta vista.</p>
            </div>
        `;
        return;
    }

    data.forEach(inc => {
        const item = document.createElement('div');
        item.className = 'incidence-item';
        // Guardamos todo el objeto en dataset
        item.dataset.json = JSON.stringify(inc);

        // Lógica de visualización de estado
        let dotClass = 'yellow';
        let statusTitle = 'Pendiente';

        // Asumiendo IDs: 1=Enviada, 2=En revisión, 3=Resuelta, 4=Rechazada
        if (inc.id_estado == 3) {
            dotClass = 'green';
            statusTitle = 'Resuelta';
        } else if (inc.id_estado == 4) {
            dotClass = 'red';
            statusTitle = 'Rechazada';
        }

        // Nombre de usuario (si viene en el JSON, sino "Usuario")
        const userName = inc.nombre_usuario || "Usuario " + inc.id_usuario;

        item.innerHTML = `
            <div class="incidence-header">
                <div class="incidence-user-info"><i class="fa-solid fa-user"></i> ${escapeHtml(userName)}</div>
                <h4 class="incidence-title-item">${escapeHtml(inc.asunto)}</h4>
                <div class="incidence-meta">
                    <span class="incidence-time">${formatearFecha(inc.fecha)}</span>
                    <span class="status-dot ${dotClass}" title="${statusTitle}"></span>
                </div>
            </div>
            <p class="incidence-preview">${escapeHtml(inc.mensaje_enviado)}</p>
        `;

        item.addEventListener('click', () => selectIncidencia(item, inc));
        container.appendChild(item);
    });
}

let selectedIncidenceId = null;

function selectIncidencia(element, data) {
    // 1. UI Active
    document.querySelectorAll('.incidence-item').forEach(i => i.classList.remove('active'));
    element.classList.add('active');

    // 2. Variables globales para la acción
    selectedIncidenceId = data.id_incidencia;

    // 3. Render Panel Derecho
    const userName = data.nombre_usuario || "Usuario " + data.id_usuario;
    document.getElementById('detailUser').innerHTML = `<i class="fa-solid fa-user"></i> ${escapeHtml(userName)}`;
    document.getElementById('detailTitle').textContent = data.asunto;
    document.getElementById('detailDesc').innerText = data.mensaje_enviado;

    // 4. Formulario
    const textarea = document.getElementById('responseInput');
    textarea.value = data.respuesta || "";

    // Reset visual de botones
    document.querySelectorAll('.btn-action').forEach(b => b.classList.remove('active'));

    // Si ya está resuelta/rechazada, marcar visualmente
    const idEstado = parseInt(data.id_estado);
    if (idEstado === 3) {
        document.querySelector('.btn-resolve').classList.add('active');
    } else if (idEstado === 4) {
        document.querySelector('.btn-reject').classList.add('active');
    }
}

async function enviarRespuestaAdmin(accion) {
    if (!selectedIncidenceId) {
        alert("Selecciona una incidencia primero.");
        return;
    }

    const respuesta = document.getElementById('responseInput').value.trim();

    if (!respuesta) {
        alert("Por favor escribe una respuesta para el usuario.");
        return;
    }

    // Determinar el botón presionado para mostrar loading
    const btn = (accion === 'resolved') ? document.getElementById('btnResolve') : document.getElementById('btnReject');
    const originalHTML = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Procesando...';

    try {
        // Mapear accion a ID estado: resolved -> 3, rejected -> 4
        const nuevoEstadoId = (accion === 'resolved') ? 3 : 4;

        const payload = {
            id_incidencia: selectedIncidenceId,
            respuesta: respuesta,
            id_estado: nuevoEstadoId
        };

        const response = await fetch(`${API_BASE_URL}/incidencia/resolver`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await response.json();

        if (data.status === 'ok') {
            const accionTexto = (accion === 'resolved') ? 'resuelta' : 'rechazada';
            alert(`Incidencia ${accionTexto} correctamente.`);

            // Recargar lista
            cargarIncidenciasAdmin();

            // Limpiar selección
            document.getElementById('detailTitle').textContent = '';
            document.getElementById('detailDesc').textContent = '';
            document.getElementById('responseInput').value = '';
            selectedIncidenceId = null;
            document.querySelectorAll('.btn-action').forEach(b => b.classList.remove('active'));
        } else {
            alert("Error del servidor: " + (data.mensaje || 'Desconocido'));
        }

    } catch (error) {
        console.error("Error enviando respuesta:", error);
        alert("Error de conexión al enviar la respuesta.");
    } finally {
        btn.disabled = false;
        btn.innerHTML = originalHTML;
    }
}

function formatearFecha(fechaString) {
    if (!fechaString) return '';
    const d = new Date(fechaString);
    if (isNaN(d.getTime())) return fechaString;
    const hours = d.getHours().toString().padStart(2, '0');
    const mins = d.getMinutes().toString().padStart(2, '0');
    const day = d.getDate().toString().padStart(2, '0');
    const month = (d.getMonth() + 1).toString().padStart(2, '0');
    const year = d.getFullYear();
    return `${hours}:${mins} - ${day}/${month}/${year}`;
}

function escapeHtml(text) {
    if (!text) return '';
    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}
