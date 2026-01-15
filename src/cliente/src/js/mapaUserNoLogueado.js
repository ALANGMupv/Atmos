/**
 * @file mapaUserNoLogueado.js
 * @brief Lógica del mapa para usuarios NO logueados.
 *
 * Funciones bloqueadas:
 *  - Selección de contaminante individual
 *  - Cálculo dinámico de índice de calidad del aire
 *
 * Se muestra únicamente la interpolación IDW general ("Todos").
 */

/* =============================================================================
 * @section CanvasOverlay - Capa personalizada para dibujar en Canvas
 * ========================================================================== */

/**
 * @class L.CanvasOverlay
 * @brief Capa Leaflet personalizada que permite dibujar libremente en canvas.
 */
L.CanvasOverlay = (L.Layer ? L.Layer : L.Class).extend({

    /**
     * @brief Inicializa la capa.
     * @param {Function} userDrawFunc Función que dibuja en el canvas.
     * @param {Object} options Opciones Leaflet.
     */
    initialize: function (userDrawFunc, options) {
        this._userDrawFunc = userDrawFunc;
        L.setOptions(this, options);
    },

    /**
     * @brief Asigna nueva función de dibujo.
     */
    drawing: function (userDrawFunc) {
        this._userDrawFunc = userDrawFunc;
        return this;
    },

    /** @brief Devuelve el objeto canvas. */
    canvas: function () { return this._canvas; },

    /**
     * @brief Añade la capa al mapa.
     */
    onAdd: function (map) {
        this._map = map;
        this._canvas = L.DomUtil.create("canvas", "leaflet-canvas-overlay");

        const size = map.getSize();
        this._canvas.width = size.x;
        this._canvas.height = size.y;

        map.getPanes().overlayPane.appendChild(this._canvas);

        map.on("moveend zoomend resize", this._reset, this);
        this._reset();
    },

    /**
     * @brief Elimina la capa del mapa.
     */
    onRemove: function (map) {
        const pane = map.getPanes().overlayPane;
        if (this._canvas && pane.contains(this._canvas)) {
            pane.removeChild(this._canvas);
        }
        map.off("moveend zoomend resize", this._reset, this);
    },

    /**
     * @brief Recalcula posición y redibuja.
     */
    _reset: function () {
        if (!this._map || !this._canvas || !this._userDrawFunc) return;

        const topLeft = this._map.containerPointToLayerPoint([0, 0]);
        L.DomUtil.setPosition(this._canvas, topLeft);

        const size = this._map.getSize();
        this._canvas.width = size.x;
        this._canvas.height = size.y;

        this._userDrawFunc(this, {
            canvas: this._canvas,
            bounds: this._map.getBounds(),
            size: size,
            zoom: this._map.getZoom()
        });
    }
});

/** Helper para crear la capa */
L.canvasOverlay = (fn, options) => new L.CanvasOverlay(fn, options);



/* =============================================================================
 * @section Normalización y colores
 * ========================================================================== */

const RANGOS = {
    12: { bueno: 1.7, moderado: 4.4, insalubre: 8.7 },
    11: { bueno: 0.021, moderado: 0.053, insalubre: 0.106 },
    13: { bueno: 0.031, moderado: 0.061, insalubre: 0.092 },
    14: { bueno: 0.0076, moderado: 0.019, insalubre: 0.038 }
};

/**
 * @brief Normaliza valores según rangos oficiales.
 */
function normalizarGas(tipo, valor) {
    const r = RANGOS[tipo];
    if (!r || valor == null) return 0;
    if (valor <= r.bueno) return 0.10;
    if (valor <= r.moderado) return 0.45;
    if (valor <= r.insalubre) return 0.75;
    return 1.0;
}

/**
 * @brief Devuelve color RGB según nivel normalizado.
 */
function colorPorNivel(n) {
    if (n <= 0.10) return [36, 255, 84];
    if (n <= 0.45) return [255, 240, 0];
    if (n <= 0.75) return [255, 144, 0];
    return [255, 48, 48];
}



/* =============================================================================
 * @section Índice bloqueado (solo muestra guiones)
 * ========================================================================== */

/**
 * @brief Desactiva el índice dinámico y coloca guiones.
 */
function actualizarIndice() {
    document.querySelector(".porcentaje-buena").textContent = "–";
    document.querySelector(".porcentaje-moderada").textContent = "–";
    document.querySelector(".porcentaje-insalubre").textContent = "–";
    document.querySelector(".porcentaje-mala").textContent = "–";
}



/* =============================================================================
 * @section Mapa base
 * ========================================================================== */

let mapa = L.map("map", { zoomControl: false }).setView([39.47, -0.38], 12);

// Pane inferior para el IDW (para que NO tape al icono del usuario)
mapa.createPane("idwPane");
mapa.getPane("idwPane").style.zIndex = 300;


L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19
}).addTo(mapa);



/* =============================================================================
 * @section Geolocalización (modo no logueado)
 * @brief Centra el mapa y muestra un icono permanente de ubicación.
 * ========================================================================== */

/**
 * @brief Pane especial para que el icono de ubicación siempre quede encima del IDW.
 */
mapa.createPane("ubicacionPane");
mapa.getPane("ubicacionPane").style.zIndex = 650;  // por encima del canvas

/** @brief Variable global del marcador de usuario */
let marcadorUsuario = null;

/**
 * @brief Centra el mapa en la ubicación del usuario y crea/actualiza el marcador.
 */
function centrarEnUsuario(lat, lng) {

    // Mover mapa al usuario
    mapa.setView([lat, lng], 15);

    // Si ya existe un marcador, lo movemos
    if (marcadorUsuario) {
        marcadorUsuario.setLatLng([lat, lng]);
        return;
    }

    /**
     * @brief Marcador de ubicación del usuario
     */
    marcadorUsuario = L.circleMarker([lat, lng], {
        radius: 9,
        fillColor: "#0F6E8C",
        color: "#ffffff",
        weight: 2,
        fillOpacity: 1,
        pane: "ubicacionPane"
    }).addTo(mapa);
}

/**
 * @brief Solicita geolocalización y ejecuta el centrado.
 */
mapa.whenReady(() => {
    console.log("¿Geolocation disponible?:", navigator.geolocation ? "SÍ" : "NO");
    if (navigator.geolocation) {

        navigator.geolocation.getCurrentPosition(
            pos => {
                console.log("GEO OK", pos.coords);
                centrarEnUsuario(pos.coords.latitude, pos.coords.longitude);
                cargarMapa("ALL");
            },
            err => {
                console.warn("GEO ERROR:", err);
                cargarMapa("ALL");
            }
        );
    } else {
        cargarMapa("ALL");
    }
});

/**
 * @brief Botón manual de ubicación
 */
document.getElementById("btn-geoloc")?.addEventListener("click", () => {
    if (!navigator.geolocation) return;

    navigator.geolocation.getCurrentPosition(
        pos => centrarEnUsuario(pos.coords.latitude, pos.coords.longitude)
    );
});


/* =============================================================================
 * @section IDW - Interpolación de calor
 * ========================================================================== */

let puntosActuales = [];
const canvasLayer = L.canvasOverlay(dibujarIDW, {
    pane: "idwPane"
}).addTo(mapa);


/**
 * @brief Dibuja interpolación IDW sobre el canvas.
 */
function dibujarIDW(layer, params) {

    const canvas = params.canvas;
    const ctx = canvas.getContext("2d");

    ctx.clearRect(0, 0, canvas.width, canvas.height);
    if (!puntosActuales.length) return;

    const zoom = mapa.getZoom();
    const bounds = mapa.getBounds();

    const RADIO = zoom < 11 ? 0.017 : zoom < 14 ? 0.010 : 0.008;
    const GRID  = zoom < 12 ? 55 : zoom < 14 ? 65 : 75;

    const stepX = canvas.width / GRID;
    const stepY = canvas.height / GRID;

    for (let ix = 0; ix < GRID; ix++) {
        for (let iy = 0; iy < GRID; iy++) {

            const px = ix * stepX + stepX / 2;
            const py = iy * stepY + stepY / 2;

            const ll = mapa.containerPointToLatLng([px, py]);
            if (!bounds.contains([ll.lat, ll.lng])) continue;

            let sumW = 0;
            let peorNivel = 0;

            for (let p of puntosActuales) {
                const dx = ll.lng - p.lng;
                const dy = ll.lat - p.lat;
                const d = Math.sqrt(dx * dx + dy * dy);
                if (d > RADIO) continue;

                const w = d === 0 ? 1 : 1 / (d * d);
                sumW += w;
                peorNivel = Math.max(peorNivel, p.nivel);
            }

            if (sumW === 0) continue;

            const [r, g, b] = colorPorNivel(peorNivel);

            ctx.fillStyle = `rgba(${r},${g},${b},0.09)`;
            ctx.beginPath();
            ctx.arc(px, py, stepX * 1.1, 0, Math.PI * 2);
            ctx.fill();
        }
    }

    actualizarIndice();  // índice bloqueado
}



/* =============================================================================
 * @section Carga de datos desde backend
 * ========================================================================== */

/**
 * @brief Carga todas las medidas y selecciona el peor valor por placa.
 */
async function cargarMapa(tipo = "ALL") {

    puntosActuales = [];

    const res = await fetch("/mapa/medidas/todos");
    const data = await res.json();
    if (data.status !== "ok") return;

    puntosActuales = data.placas
        .filter(p => p.latitud && p.longitud)
        .map(p => {

            let peor = 0;
            for (const t of [11,12,13,14]) {
                const v = p[
                    t === 11 ? "NO2" :
                        t === 12 ? "CO" :
                            t === 13 ? "O3" :
                                "SO2"
                    ];
                peor = Math.max(peor, normalizarGas(t, v));
            }

            return {
                lat: p.latitud,
                lng: p.longitud,
                nivel: peor
            };
        });

    canvasLayer._reset();
}



/* =============================================================================
 * @section Botones UI
 * ========================================================================== */

/** @brief Zoom */
document.getElementById("zoom-in")?.addEventListener("click", () => mapa.zoomIn());
document.getElementById("zoom-out")?.addEventListener("click", () => mapa.zoomOut());

/* =============================================================================
 * @section Autocompletado de búsqueda
 * ========================================================================== */

const searchInput = document.getElementById("search-input");
const suggestionBox = document.getElementById("search-suggestions");

let suggestionTimeout = null;

/**
 * @brief Limpia la lista de sugerencias.
 */
function clearSuggestions() {
    suggestionBox.innerHTML = "";
    suggestionBox.style.display = "none";
}

/**
 * @brief Llama a Nominatim para obtener sugerencias.
 */
async function fetchSuggestions(query) {
    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&addressdetails=1&limit=5`;
    return (await fetch(url)).json();
}

/**
 * @brief Mueve el mapa a la selección del usuario.
 */
function moveToLocation(item) {
    mapa.setView([parseFloat(item.lat), parseFloat(item.lon)], 15);
}

if (searchInput) {
    searchInput.addEventListener("input", () => {
        const q = searchInput.value.trim();
        if (!q) return clearSuggestions();

        clearTimeout(suggestionTimeout);
        suggestionTimeout = setTimeout(async () => {

            const data = await fetchSuggestions(q);
            suggestionBox.innerHTML = "";

            data.forEach(item => {
                const div = document.createElement("div");
                div.classList.add("suggestion-item");
                div.textContent = item.display_name;

                div.onclick = () => {
                    moveToLocation(item);
                    clearSuggestions();
                };

                suggestionBox.appendChild(div);
            });

            suggestionBox.style.display = "block";

        }, 300);
    });
}

document.addEventListener("click", e => {
    if (!e.target.closest(".search-bar") &&
        !e.target.closest("#search-suggestions")) {
        clearSuggestions();
    }
});



/* =============================================================================
 * @section Selector contaminantes (bloqueado)
 * ========================================================================== */

/**
 * @brief Bloquea selección de gases excepto "Todos".
 */
document.querySelectorAll(".contaminante-option").forEach(btn => {

    const tipo = btn.dataset.tipo;

    if (tipo !== "ALL") {
        btn.classList.add("disabled");
        btn.addEventListener("click", () =>
            alert("Inicia sesión para ver contaminantes específicos.")
        );
    } else {
        btn.addEventListener("click", () => cargarMapa("ALL"));
    }

});



/* =============================================================================
 * @section Primera carga
 * ========================================================================== */

cargarMapa("ALL");
