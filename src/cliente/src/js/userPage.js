/**
 * userPage.js
 * -------------------------
 * Script de interacción de la página del usuario.
 *
 * Funcionalidades:
 *  - Mostrar/ocultar popups informativos.
 *  - Gestionar el selector de gas.
 *  - Consultar al backend la última medición y el promedio del día
 *    dependiendo del gas seleccionado.
 *
 * Autor: Santiago Fuenmayor Ruiz
 */

// ================================================================
// 1. POPUPS
// ================================================================

// Abrir popup
document.querySelectorAll("[data-popup]").forEach(boton => {
    boton.addEventListener("click", () => {
        const popup = document.getElementById(boton.dataset.popup);
        if (popup) popup.style.display = "flex";
    });
});

// Cerrar popup con la X
document.querySelectorAll(".cerrar-popup").forEach(btn => {
    btn.addEventListener("click", () => {
        const popup = btn.closest(".popup-info-container");
        if (popup) popup.style.display = "none";
    });
});

// Cerrar popup haciendo clic en el fondo
document.querySelectorAll(".popup-info-container").forEach(popup => {
    popup.addEventListener("click", e => {
        if (e.target === popup) popup.style.display = "none";
    });
});


// ================================================================
// 2. Selector de gas
// ================================================================

const gasSelector   = document.getElementById("gasSelector");
const gasUltima     = document.getElementById("gasUltima");
const gasPromedio   = document.getElementById("gasPromedio");
const ultimaValor   = document.querySelector(".medicion-medicion");
const promedioValor = document.querySelector(".medicion-promedio");

// Mapeo gas → tipo numérico
const MAPA_GASES = {
    "NO₂": 11,
    "CO": 12,
    "O₃": 13,
    "SO₂": 14
};

// ID del usuario (inyectado desde PHP en el HTML)
const ID_USUARIO = window.ID_USUARIO;

if (!ID_USUARIO) {
    console.warn("ID_USUARIO no está disponible.");
}

// Acción al seleccionar un gas
if (gasSelector) {
    gasSelector.addEventListener("change", async () => {

        const gasElegido = gasSelector.value;
        const tipo = MAPA_GASES[gasElegido];

        gasUltima.textContent   = gasElegido;
        gasPromedio.textContent = gasElegido;

        if (!tipo) return;

        await cargarDatosDeGas(tipo);
    });
}


// ================================================================
// 3. Clasificación de la calidad del aire
// ================================================================

function clasificarMedida(valor, tipoGas) {

    // Si no hay valor válido → Sin datos
    if (valor === null || valor === undefined || isNaN(valor)) {
        return {
            texto: "Sin datos",
            color: "#C0C0C0" // gris neutro
        };
    }

    // Rangos por gas (ppm)
    const rangos = {
        12: { bueno: 1.7,    moderado: 4.4,   insalubre: 8.7   },   // CO
        11: { bueno: 0.021,  moderado: 0.053, insalubre: 0.106 },   // NO₂
        13: { bueno: 0.031,  moderado: 0.061, insalubre: 0.092 },   // O₃
        14: { bueno: 0.0076, moderado: 0.019, insalubre: 0.038 }    // SO₂
    };

    const r = rangos[tipoGas];
    if (!r) return { texto: "Sin datos", color: "#C0C0C0" };

    if (valor <= r.bueno)    return { texto: "Buena",     color: "#55E249" };
    if (valor <= r.moderado) return { texto: "Moderada",  color: "#FFF71B" };
    if (valor <= r.insalubre)return { texto: "Insalubre", color: "#FF9D00" };
    return { texto: "Mala",  color: "#FF0004" };
}


// ================================================================
// 4. Consulta al backend
// ================================================================

async function cargarDatosDeGas(tipoGas) {
    try {
        const fechaUltimaDOM   = document.querySelector(".medicion-container .medicion-titulo-container p");
        const fechaPromedioDOM = document.querySelector(".promedio-container .promedio-titulo-container p");
        const cuadraditoMedicion = document.querySelector(".categoria-medicion-container .color-medicion");
        const textoMedicion      = document.querySelector(".categoria-medicion-container .texto-medicion");
        const cuadraditoPromedio = document.querySelector(".categoria-promedio-container .color-promedio");
        const textoPromedio      = document.querySelector(".categoria-promedio-container .texto-promedio");

        const url  = `https://nagufor.upv.edu.es/resumenUsuarioPorGas?id_usuario=${ID_USUARIO}&tipo=${tipoGas}`;
        const resp = await fetch(url);
        const data = await resp.json();

        // Si NO hay placa → todo vacío
        if (data.status === "sin_placa") {
            ultimaValor.textContent   = "0.00";
            promedioValor.textContent = "0.00";

            if (fechaUltimaDOM)   fechaUltimaDOM.textContent   = "--:--";
            if (fechaPromedioDOM) fechaPromedioDOM.textContent = "--/--/----";

            if (cuadraditoMedicion) cuadraditoMedicion.style.backgroundColor = "#C0C0C0";
            if (textoMedicion)      textoMedicion.textContent = "Sin datos";

            if (cuadraditoPromedio) cuadraditoPromedio.style.backgroundColor = "#C0C0C0";
            if (textoPromedio)      textoPromedio.textContent = "Sin datos";

            return;
        }

        // FECHA ÚLTIMA MEDIDA
        if (data.ultima_medida && data.ultima_medida.fecha_hora) {
            const fechaJS = new Date(data.ultima_medida.fecha_hora);
            const hora  = fechaJS.toLocaleTimeString("es-ES", { hour: "2-digit", minute: "2-digit" });
            const fecha = fechaJS.toLocaleDateString("es-ES");
            if (fechaUltimaDOM) fechaUltimaDOM.textContent = `${hora} - ${fecha}`;
        } else if (fechaUltimaDOM) {
            fechaUltimaDOM.textContent = "--:--";
        }

        // FECHA PROMEDIO = HOY
        if (fechaPromedioDOM) {
            fechaPromedioDOM.textContent = new Date().toLocaleDateString("es-ES");
        }

        // VALORES
        const valorUltimo = data.ultima_medida
            ? Number(data.ultima_medida.valor)
            : null;

        ultimaValor.textContent   = valorUltimo !== null ? valorUltimo.toFixed(2) : "0.00";
        promedioValor.textContent = Number(data.promedio || 0).toFixed(2);

        // *** CORRECCIÓN IMPORTANTE ***
        // Si NO hay última medida → también promedio = "Sin datos"
        if (valorUltimo === null) {
            if (cuadraditoMedicion) cuadraditoMedicion.style.backgroundColor = "#C0C0C0";
            if (textoMedicion)      textoMedicion.textContent = "Sin datos";

            if (cuadraditoPromedio) cuadraditoPromedio.style.backgroundColor = "#C0C0C0";
            if (textoPromedio)      textoPromedio.textContent = "Sin datos";

            return;
        }

        // CLASIFICACIÓN PARA ÚLTIMA MEDIDA
        const categoriaUltima = clasificarMedida(valorUltimo, tipoGas);

        if (cuadraditoMedicion) cuadraditoMedicion.style.backgroundColor = categoriaUltima.color;
        if (textoMedicion)      textoMedicion.textContent = categoriaUltima.texto;


// CLASIFICACIÓN PARA PROMEDIO
        const valorProm = Number(data.promedio || 0);

// Si NO hay última medida → también promedio = "Sin datos"
        if (valorUltimo === null) {
            if (cuadraditoPromedio) cuadraditoPromedio.style.backgroundColor = "#C0C0C0";
            if (textoPromedio)      textoPromedio.textContent = "Sin datos";
        } else {
            const categoriaProm = clasificarMedida(valorProm, tipoGas);

            if (cuadraditoPromedio) cuadraditoPromedio.style.backgroundColor = categoriaProm.color;
            if (textoPromedio)      textoPromedio.textContent = categoriaProm.texto;
        }


    } catch (err) {
        console.error("Error consultando datos:", err);
    }
}
