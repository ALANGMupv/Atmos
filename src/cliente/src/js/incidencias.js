
const API_BASE_URL = "https://nagufor.upv.edu.es";

document.addEventListener('DOMContentLoaded', () => {
    cargarIncidencias();

    // Event listener para el formulario
    document.getElementById('formEnviar').addEventListener('submit', (e) => {
        e.preventDefault();
        enviarIncidencia();
    });
});

async function cargarIncidencias() {
    const contenedor = document.getElementById('incidenceList');

    try {
        const idUsuario = window.ID_USUARIO;
        const response = await fetch(`${API_BASE_URL}/incidencias?id_usuario=${idUsuario}`);
        const data = await response.json();

        if (data.status === 'ok') {
            renderizarLista(data.incidencias);
        } else {
            console.error("Error al cargar incidencias:", data);
            contenedor.innerHTML = `<div style="padding:20px; text-align:center;">Error al cargar datos.</div>`;
        }

    } catch (error) {
        console.error("Error de red:", error);
        contenedor.innerHTML = `<div style="padding:20px; text-align:center;">Error de conexión.</div>`;
    }
}

function renderizarLista(incidencias) {
    const contenedor = document.getElementById('incidenceList');
    contenedor.innerHTML = '';

    if (incidencias.length === 0) {
        contenedor.innerHTML = `<div style="padding:20px; text-align:center; color:#888;">No tienes incidencias registradas.</div>`;
        return;
    }

    // Ordenar por fecha descendente (la más nueva arriba)
    // El formato de fecha suele venir ISO: 2025-12-29 00:15:59
    incidencias.sort((a, b) => new Date(b.fecha) - new Date(a.fecha));

    incidencias.forEach(inc => {
        // Mapear estado
        // 1: Solicitada (Pendiente)
        // 2: En proceso (Pendiente)
        // 3: Resuelta
        // 4: Rechazada
        let estadoUI = 'pending';
        let claseDot = 'yellow';
        let textoEstado = 'Pendiente';

        if (inc.id_estado == 3) {
            estadoUI = 'resolved';
            claseDot = 'green';
            textoEstado = 'Resuelta';
        } else if (inc.id_estado == 4) {
            estadoUI = 'rejected';
            claseDot = 'red';
            textoEstado = 'Rechazada';
        }

        const fechaFormateada = formatearFecha(inc.fecha);
        const item = document.createElement('div');
        item.className = 'incidence-item';

        // Guardamos datos en el objeto del DOM para usarlos al hacer click
        item.dataset.json = JSON.stringify(inc);

        item.innerHTML = `
            <div class="incidence-header">
                <h4 class="incidence-title-item">${escapeHtml(inc.asunto)}</h4>
                <div class="incidence-meta">
                    <span class="incidence-time">${fechaFormateada}</span>
                    <span class="status-dot ${claseDot}" title="${textoEstado}"></span>
                </div>
            </div>
            <p class="incidence-preview">${escapeHtml(inc.mensaje_enviado)}</p>
        `;

        item.addEventListener('click', () => selectIncidencia(item, inc));
        contenedor.appendChild(item);
    });
}

function selectIncidencia(element, data) {
    // 1. Gestionar clases active
    document.querySelectorAll('.incidence-item').forEach(i => i.classList.remove('active'));
    element.classList.add('active');

    // 2. Mostrar contenedor de detalle y ocultar empty state
    document.getElementById('emptyStateDetail').style.display = 'none';
    document.getElementById('contentDetailWrapper').style.display = 'block';

    // 3. Rellenar datos
    document.getElementById('detailTitle').textContent = data.asunto;
    document.getElementById('detailDesc').innerText = data.mensaje_enviado; // innerText respeta saltos de línea basicos

    // 4. Actualizar Estado (Status Hero)
    const resContainer = document.getElementById('resolutionContent');
    const idEstado = parseInt(data.id_estado);
    const respuestaAdmin = data.respuesta;

    if (idEstado === 3) { // RESUELTA
        resContainer.innerHTML = `
            <div class="status-hero resolved">
                <div class="status-icon-large">
                    <i class="fa-solid fa-check"></i>
                </div>
                <h4 class="status-title">Incidencia Resuelta</h4>
                <p class="status-desc">${respuestaAdmin ? escapeHtml(respuestaAdmin) : 'Tu incidencia ha sido resuelta.'}</p>
            </div>
        `;
    } else if (idEstado === 4) { // RECHAZADA
        resContainer.innerHTML = `
            <div class="status-hero rejected" style="border-color: #FECACA;">
                <div class="status-icon-large" style="background: #FEF2F2; color: #EF4444;">
                    <i class="fa-solid fa-xmark"></i>
                </div>
                <h4 class="status-title" style="color: #EF4444;">Incidencia Rechazada</h4>
                <p class="status-desc">${respuestaAdmin ? escapeHtml(respuestaAdmin) : 'La incidencia no procede o ha sido desestimada.'}</p>
            </div>
        `;
    } else { // PENDIENTE O EN PROCESO
        resContainer.innerHTML = `
            <div class="status-hero pending">
                <div class="status-icon-large">
                    <i class="fa-regular fa-clock"></i>
                </div>
                <h4 class="status-title">Incidencia en revisión</h4>
                <p class="status-desc">
                    Nuestro equipo está analizando el caso reportado.<br>
                    Te notificaremos en cuanto haya novedades.
                </p>
            </div>
        `;
    }

    // 5. Marcar como leída si no lo estaba
    // El campo puede venir como string "0" o int 0
    if (data.leida_usuario == 0 && data.respuesta) {
        marcarComoLeida(data.id_incidencia);
        // Actualizamos localmente para no llamar otra vez
        data.leida_usuario = 1;
        element.dataset.json = JSON.stringify(data);
    }
}

async function enviarIncidencia() {
    const asunto = document.getElementById('asuntoInput').value.trim();
    const mensaje = document.getElementById('mensajeInput').value.trim();
    const btn = document.querySelector('#formEnviar button');

    if (!asunto || !mensaje) {
        alert("Por favor, rellena todos los campos.");
        return;
    }

    btn.disabled = true;
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Enviando...';

    try {
        const payload = {
            id_usuario: window.ID_USUARIO,
            asunto: asunto,
            mensaje_enviado: mensaje
            // id_placa: null (opcional, no lo tenemos en este form simplificado)
        };

        const response = await fetch(`${API_BASE_URL}/incidencia`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await response.json();

        if (data.status === 'ok') {
            // Limpiar form
            document.getElementById('asuntoInput').value = '';
            document.getElementById('mensajeInput').value = '';
            // Recargar lista
            cargarIncidencias();
            alert("Incidencia enviada correctamente.");
        } else {
            alert("Error al enviar: " + (data.mensaje || 'Error desconocido'));
        }

    } catch (error) {
        console.error(error);
        alert("Error de conexión al enviar la incidencia.");
    } finally {
        btn.disabled = false;
        btn.innerText = 'Enviar Incidencia';
    }
}

function marcarComoLeida(idIncidencia) {
    fetch(`${API_BASE_URL}/incidencia/leida`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ id_incidencia: idIncidencia })
    }).catch(err => console.error("Error marcando leída", err));
}

function formatearFecha(fechaString) {
    if (!fechaString) return '';
    // fechaString suele ser "YYYY-MM-DD HH:mm:ss" o ISO
    const d = new Date(fechaString);
    if (isNaN(d.getTime())) return fechaString;

    // Formato: HH:mm - DD/MM/YYYY
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
