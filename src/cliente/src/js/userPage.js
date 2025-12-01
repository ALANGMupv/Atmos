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

// ================================================================
// 2.1. Estado global para la gráfica
// ================================================================

// Tipo de gas actualmente seleccionado (11, 12, 13, 14)
let tipoGasActual = null;

// Modo de visualización de la gráfica: "D" (semana) o "H" (horas)
let modoGraficaActual = "D";

// Instancia de Chart.js para poder destruirla y recrearla
let graficaCalidadChart = null;

// Control del intervalo de actualización de la señal del sensor
let intervaloSenal = null;

// Variable global: indica si el sensor está activo o no
let sensorActivo = true;


// Referencias a elementos de la gráfica
const ctxGrafica = document.getElementById("graficaCalidad")
    ? document.getElementById("graficaCalidad").getContext("2d")
    : null;

const botonesModoGrafica = document.querySelectorAll(".selector-modo-grafica .selector-opcion");
const caritaGraficaImg   = document.querySelector(".carita-grafica img");
const graficaRangoTexto  = document.getElementById("graficaRangoTexto");


// Acción al seleccionar un gas
if (gasSelector) {
    gasSelector.addEventListener("change", async () => {

        const gasElegido = gasSelector.value;
        const tipo = MAPA_GASES[gasElegido];

        gasUltima.textContent   = gasElegido;
        gasPromedio.textContent = gasElegido;

        if (!tipo) return;

        // Guardamos el tipo de gas actual para la gráfica
        tipoGasActual = tipo;

        // Actualizamos tarjetas de última medida / promedio diario
        await cargarDatosDeGas(tipo);

        // Actualizamos las graficas segun el tipo de gas
        await cargarGraficaSegunModo();

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

function nivelCategoriaValor(valor, tipoGas) {
    const categ = clasificarMedida(valor, tipoGas).texto;

    switch (categ) {
        case "Buena":     return 1;
        case "Moderada":  return 2;
        case "Insalubre": return 3;
        case "Mala":      return 4;
        default:          return 0; // Sin datos
    }
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

// ======================================================================
// 5. Función: renderizarGraficaCalidad()
// ----------------------------------------------------------------------
// Descripción:
//   Recrea la gráfica con los datos proporcionados.
//   Incluye:
//      - PPM reales en tooltip
//      - Fechas reales en tooltip
//      - Fechas debajo de cada barra (doble línea)
//      - Esquinas redondeadas
//      - Sin líneas verticales de separación
// ======================================================================
function renderizarGraficaCalidad(labels, valores) {

    // Eliminar width y height inline que pone Chart.js
    const canvas = document.getElementById("graficaCalidad");
    canvas.removeAttribute("width");
    canvas.removeAttribute("height");

    const ctx = canvas.getContext("2d");

    if (graficaCalidadChart) {
        graficaCalidadChart.destroy();
    }

    // Convertir ppm → categorías (1,2,3,4)
    const valoresConvertidos = valores.map(v => nivelCategoriaValor(v, tipoGasActual));

    graficaCalidadChart = new Chart(ctx, {
        type: "bar",

        data: {
            labels: labels,
            datasets: [{
                label: "ppm",
                data: valoresConvertidos,
                originalValues: valores,
                backgroundColor: valores.map(v => clasificarMedida(v, tipoGasActual).color),

                // esquinas redondeadas
                borderRadius: {
                    topLeft: 5,
                    topRight: 5,
                    bottomLeft: 0,
                    bottomRight: 0
                },

                borderSkipped: false
            }]
        },

        options: {
            responsive: true,
            maintainAspectRatio: false,
            animation: false,

            plugins: {
                legend: { display: false },

                tooltip: {
                    callbacks: {

                        // ======================================================
                        //  TÍTULO DEL TOOLTIP → FECHA REAL SEGÚN MODO
                        // ======================================================
                        title: function (context) {

                            const idx = context[0].dataIndex;
                            const hoy = new Date();

                            // ---------- MODO SEMANA ----------
                            if (modoGraficaActual === "D") {

                                const fecha = new Date(hoy);
                                fecha.setDate(hoy.getDate() - (labels.length - 1 - idx));

                                const dia = fecha.toLocaleDateString("es-ES", {
                                    weekday: "long"
                                });

                                const fechaStr = fecha.toLocaleDateString("es-ES");

                                return `${dia} (${fechaStr})`;
                            }

                            // ---------- MODO HORAS ----------
                            if (modoGraficaActual === "H") {

                                const fecha = new Date(
                                    hoy.getTime() - (labels.length - 1 - idx) * 3600000
                                );

                                const hora = fecha.getHours().toString().padStart(2, "0");
                                const fechaStr = fecha.toLocaleDateString("es-ES");

                                return `${hora}:00 (${fechaStr})`;
                            }

                            return "";
                        },

                        // ======================================================
                        //  LÍNEA DEL TOOLTIP → PPM REAL
                        // ======================================================
                        label: function (context) {
                            const ppm = context.dataset.originalValues[context.dataIndex];
                            return ppm.toFixed(3) + " ppm";
                        }
                    }
                }
            },

            // ==============================================================
            //  EJES
            // ==============================================================

            scales: {
                x: {
                    ticks: {
                        font: { size: 10 },

                        // Mostrar debajo del label la fecha real
                        callback: function (value, index, ticks) {

                            const label = this.getLabelForValue(value);
                            const hoy = new Date();

                            // ---------- MODO SEMANA ----------
                            if (modoGraficaActual === "D") {

                                const fecha = new Date(hoy);
                                fecha.setDate(hoy.getDate() - (ticks.length - 1 - index));

                                const fechaStr = fecha.toLocaleDateString("es-ES", {
                                    day: "2-digit",
                                    month: "2-digit"
                                });

                                // multi-line label
                                return [label, fechaStr];
                            }

                            // ---------- MODO HORAS ----------
                            if (modoGraficaActual === "H") {

                                const fecha = new Date(
                                    hoy.getTime() - (ticks.length - 1 - index) * 3600000
                                );

                                const fechaStr = fecha.toLocaleDateString("es-ES", {
                                    day: "2-digit",
                                    month: "2-digit"
                                });

                                return [label, fechaStr];
                            }

                            return label;
                        }
                    },

                    // 🚫 Quitar líneas verticales
                    grid: {
                        display: false
                    }
                },

                y: {
                    min: 0,
                    max: 4,
                    ticks: {
                        callback: function (value) {
                            switch (value) {
                                case 1: return "Buena";
                                case 2: return "Moderada";
                                case 3: return "Insalubre";
                                case 4: return "Mala";
                                default: return "";
                            }
                        }
                    }
                }
            }
        }
    });
}


// ======================================================================
// 6. Función: actualizarCaritaGrafica(promedio)
// ----------------------------------------------------------------------
// Actualiza la carita de la gráfica según el promedio del período
// ======================================================================
function actualizarCaritaGrafica(promedio) {

    window.promedioGraficaActual = promedio;

    if (!caritaGraficaImg) return;

    // Si no hay datos → carita neutra
    if (promedio === null || isNaN(promedio)) {
        caritaGraficaImg.src = "img/estadoAireIcono.svg";
        return;
    }

    // Clasificar según los rangos del gas seleccionado
    const { texto } = clasificarMedida(promedio, tipoGasActual);

    // Selección de imagen según categoría
    switch (texto) {
        case "Buena":
            caritaGraficaImg.src = "img/caritaGraficaBuena.svg";
            break;

        case "Moderada":
            caritaGraficaImg.src = "img/caritaGraficaModerada.svg";
            break;

        case "Insalubre":
            caritaGraficaImg.src = "img/caritaGraficaInsalubre.svg";
            break;

        case "Mala":
            caritaGraficaImg.src = "img/caritaGraficaMala.svg";
            break;

        default:
            caritaGraficaImg.src = "img/estadoAireIcono.svg";
            break;
    }

    // ======================================================================
    //  Mensajes para cada categoría de la carita
    // ======================================================================
    const mensajesCarita = {
        "Buena": "En promedio, la calidad del aire es buena.",
        "Moderada": "En promedio, la calidad del aire es moderada. Las personas sensibles pueden notar molestias.",
        "Insalubre": "En promedio, la calidad del aire es insalubre con varios picos de contaminación.",
        "Mala": "En promedio, la calidad del aire es mala. Evita la exposición prolongada.",
        "Sin datos": "Aún no hay suficiente información para evaluar la calidad del aire."
    };

    // ======================================================================
    // Tooltip dinámico en la carita
    // ======================================================================
    const tooltipCarita = document.getElementById("tooltipCarita");

    if (caritaGraficaImg && tooltipCarita) {

        caritaGraficaImg.addEventListener("mouseenter", () => {

            // Determinar categoría actual a partir del promedio
            const promedio = window.promedioGraficaActual || null;
            let categoria = "Sin datos";

            if (promedio !== null && !isNaN(promedio)) {
                categoria = clasificarMedida(promedio, tipoGasActual).texto;
            }

            // Establecer texto del tooltip
            tooltipCarita.textContent = mensajesCarita[categoria] || "Sin datos";

            // Mostrar tooltip
            tooltipCarita.classList.remove("hidden");
        });

        caritaGraficaImg.addEventListener("mouseleave", () => {
            tooltipCarita.classList.add("hidden");
        });
    }

    caritaGraficaImg.addEventListener("mouseenter", () => {
        console.log("HOVER DETECTADO en la carita");
    });
}


// ======================================================================
// 7. Función: cargarGraficaSegunModo()
// ----------------------------------------------------------------------
// Soluciona el desfase de barras: se rotan labels pero NO valores.
// ======================================================================
async function cargarGraficaSegunModo() {

    // Si no hay gas seleccionado → mostrar mensaje en el canvas
    if (!tipoGasActual) {

        // Si existe una gráfica previa → destruirla
        if (graficaCalidadChart) {
            graficaCalidadChart.destroy();
            graficaCalidadChart = null;
        }

        const canvas = document.getElementById("graficaCalidad");

        // Asegurar tamaño correcto antes de dibujar
        canvas.style.width = "100%";
        canvas.style.height = "100%";

        const ctx = canvas.getContext("2d");

        // Limpiar el canvas real (no el CSS)
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        // Texto
        ctx.font = "16px Poppins";
        ctx.fillStyle = "#999";
        ctx.textAlign = "center";

        ctx.fillText(
            "Selecciona un gas para ver la gráfica",
            canvas.width / 2,
            canvas.height / 2
        );

        return;
    }



    const API_BASE = "https://nagufor.upv.edu.es";
    const endpoint = (modoGraficaActual === "D") ? "/resumen7Dias" : "/resumen8Horas";

    const url = API_BASE + endpoint +
        "?id_usuario=" + ID_USUARIO +
        "&tipo=" + tipoGasActual;

    console.log("URL que se está llamando:", url);

    const resp = await fetch(url);
    const data = await resp.json();

    if (data.status === "sin_placa") return;

    // -----------------------------------------------
    // El backend devuelve:
    // labels  = ["Vie","Sab","Dom","Lun","Mar","Mie","Jue"] (cronológico)
    // valores = [ 0 , 0 ,  0  ,  0 , 0.48 , 0.01 , 0 ]     (cronológico)
    //
    // Si hoy es JUEVES, "Jue" ya está en la última posición,
    // así que NO hay que invertir valores.
    //
    // Solo rotamos labels si HOY no coincide con la última etiqueta.
    // -----------------------------------------------
    const labels = data.labels.slice();
    const valores = data.valores.slice();

    const diasSemana = ["Dom","Lun","Mar","Mie","Jue","Vie","Sab"];
    const hoy = diasSemana[new Date().getDay()];

    // SOLO rotamos si el modo es D (dias)
    if (modoGraficaActual === "D") {

        const diasSemana = ["Dom","Lun","Mar","Mie","Jue","Vie","Sab"];
        const hoy = diasSemana[new Date().getDay()];

        if (labels[labels.length - 1] !== hoy) {
            let seguridad = 20; // evitamos bucles infinitos

            while (labels[labels.length - 1] !== hoy && seguridad > 0) {
                const primero = labels.shift();
                labels.push(primero);

                const primeroVal = valores.shift();
                valores.push(primeroVal);

                seguridad--;
            }
        }
    }

    // ===============================
    //  ACTUALIZAR TEXTO DEL RANGO
    // ===============================
    if (graficaRangoTexto) {

        if (modoGraficaActual === "D") {

            graficaRangoTexto.textContent = "Últimos 7 días";

        } else if (modoGraficaActual === "H") {

            graficaRangoTexto.textContent = "Últimas 8 horas";
        }
    }


    // Render final con el orden correcto
    renderizarGraficaCalidad(labels, valores);

    actualizarCaritaGrafica(data.promedio);
}




// ======================================================================
// 8. Botones de modo (D / H)
// ----------------------------------------------------------------------
botonesModoGrafica.forEach(btn => {
    btn.addEventListener("click", () => {

        botonesModoGrafica.forEach(b => b.classList.remove("activo"));
        btn.classList.add("activo");

        modoGraficaActual = btn.dataset.modo;

        cargarGraficaSegunModo();
    });
});

// -----------------------------------------------------------------------------
//  Función: actualizarEstadoSensor()
// -----------------------------------------------------------------------------
//  Consulta al backend si la placa está encendida o apagada.
//  Si está apagada, INMEDIATAMENTE fuerza la tarjeta "Distancia al sensor"
//  a mostrar "Sin datos", sin esperar los 5 segundos del intervalo.
// -----------------------------------------------------------------------------
async function actualizarEstadoSensor() {

    try {
        const url = `https://nagufor.upv.edu.es/estadoPlaca?id_usuario=${ID_USUARIO}`;
        const resp = await fetch(url);
        const data = await resp.json();

        const texto = document.getElementById("estadoSensorTexto");
        const icono = document.getElementById("estadoSensorIcono");

        if (!texto || !icono) {
            console.warn(" Elementos del DOM del estado del sensor no encontrados.");
            return;
        }

        // ========= SENSOR ACTIVO =========
        if (data.estado === "activo") {
            sensorActivo = true;

            texto.textContent = "Sensor activo";
            texto.style.color = "#000";
            icono.src = "img/estadoActivoSensorIcono.svg";
            return;
        }

        // ========= SENSOR INACTIVO =========
        if (data.estado === "inactivo") {
            sensorActivo = false;

            texto.textContent = "Sensor inactivo";
            texto.style.color = "#000";
            icono.src = "img/estadoInactivoSensorIcono.svg";

            //  FORZAR que "Distancia al sensor" muestre SIN DATOS inmediatamente
            const iconoSenal = document.getElementById("iconoSenal");
            const textoSenal = document.getElementById("textoSenal");

            if (iconoSenal && textoSenal) {
                iconoSenal.src = "img/sinSeñalDistanciaIcono.svg";
                textoSenal.textContent = "Sin datos";
            }

            return;
        }

        // ========= SIN PLACA =========
        sensorActivo = false;

        texto.textContent = "Sin placa asociada";
        texto.style.color = "#777";
        icono.src = "img/estadoInactivoSensorIcono.svg";

        const iconoSenal = document.getElementById("iconoSenal");
        const textoSenal = document.getElementById("textoSenal");

        if (iconoSenal && textoSenal) {
            iconoSenal.src = "img/sinSeñalDistanciaIcono.svg";
            textoSenal.textContent = "Sin datos";
        }

    } catch (err) {
        console.error("Error consultando estado del sensor:", err);
    }
}



// ======================================================================
// Función: resetearEstadoSenal()
// ----------------------------------------------------------------------
// Se llama cuando la placa está INACTIVA o NO hay placa asociada.
// Pone inmediatamente la tarjeta de señal como "Sin datos" sin esperar.
// ======================================================================
function resetearEstadoSenal() {
    const icono = document.getElementById("iconoSenal");
    const texto = document.getElementById("textoSenal");

    if (icono) icono.src = "img/sinSeñalDistanciaIcono.svg";
    if (texto) texto.textContent = "Sin datos";
}


// ======================================================================
// Función: actualizarEstadoSenal()
// ----------------------------------------------------------------------
// Solo consulta señal si el sensor está ACTIVO.
// Si está inactivo → muestra "Sin datos" inmediatamente (sin esperar).
// ======================================================================
async function actualizarEstadoSenal() {

    // Si el sensor está apagado → no pedir al backend, mostrar “Sin datos”
    if (!sensorActivo) {

        const icono = document.getElementById("iconoSenal");
        const texto = document.getElementById("textoSenal");

        if (icono && texto) {
            icono.src = "img/sinSeñalDistanciaIcono.svg";
            texto.textContent = "Sin datos";
        }

        return;
    }

    // Sensor activo → SÍ consultar señal
    const resp = await fetch(`/estadoSenal?id_usuario=${ID_USUARIO}`);
    const data = await resp.json();

    if (data.status !== "ok") return;

    const icono = document.getElementById("iconoSenal");
    const texto = document.getElementById("textoSenal");

    switch (data.nivel) {
        case "fuerte":
            icono.src = "img/señalAltaDistanciaIcono.svg";
            texto.textContent = "Señal alta";
            break;

        case "media":
            icono.src = "img/señalRegularDistanciaIcono.svg";
            texto.textContent = "Señal regular";
            break;

        case "baja":
            icono.src = "img/señalBajaDistanciaIcono.svg";
            texto.textContent = "Señal baja";
            break;

        case "mala":
            icono.src = "img/sinSeñalDistanciaIcono.svg";
            texto.textContent = "Señal muy baja";
            break;

        default:
            icono.src = "img/sinSeñalDistanciaIcono.svg";
            texto.textContent = "Sin datos";
    }
}


// ======================================================================
//  9. Inicialización automática al cargar la página
// ======================================================================
window.addEventListener("DOMContentLoaded", async () => {

    if (!gasSelector || gasSelector.options.length === 0) return;

    gasSelector.selectedIndex = 2;

    const gasInicial = gasSelector.value;
    const tipoInicial = MAPA_GASES[gasInicial];

    tipoGasActual = tipoInicial;

    gasUltima.textContent   = gasInicial;
    gasPromedio.textContent = gasInicial;

    await cargarDatosDeGas(tipoInicial);
    await cargarGraficaSegunModo();

    // --- Estado del sensor ---
    actualizarEstadoSensor();
    setInterval(actualizarEstadoSensor, 500);

    // --- Estado de señal ---
    actualizarEstadoSenal();                       // primera llamada
    intervaloSenal = setInterval(actualizarEstadoSenal, 500);  // autorefresco
});







