/* ============================================================================
   MAPA ATMOS – IDW interpolado con colores vibrantes del índice
   ============================================================================ */

/* ============================================================================
   0. Plugin CanvasOverlay (para dibujar sobre canvas)
   ============================================================================ */

L.CanvasOverlay = (L.Layer ? L.Layer : L.Class).extend({
    initialize: function (userDrawFunc, options) {
        this._userDrawFunc = userDrawFunc;
        L.setOptions(this, options);
    },

    drawing: function (userDrawFunc) {
        this._userDrawFunc = userDrawFunc;
        return this;
    },

    canvas: function () { return this._canvas; },

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

    onRemove: function (map) {
        const pane = map.getPanes().overlayPane;
        if (this._canvas && pane.contains(this._canvas))
            pane.removeChild(this._canvas);

        map.off("moveend zoomend resize", this._reset, this);
    },

    addTo: function (map) {
        map.addLayer(this);
        return this;
    },

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

L.canvasOverlay = (fn, options) => new L.CanvasOverlay(fn, options);





/* ============================================================================
   1. RANGOS OFICIALES POR GAS
   ============================================================================ */

const RANGOS = {
    12: { bueno: 1.7,    moderado: 4.4,    insalubre: 8.7   }, // CO
    11: { bueno: 0.021,  moderado: 0.053,  insalubre: 0.106 }, // NO₂
    13: { bueno: 0.031,  moderado: 0.061,  insalubre: 0.092 }, // O₃
    14: { bueno: 0.0076, moderado: 0.019,  insalubre: 0.038 }  // SO₂
};


/* ============================================================================
   2. FUNCIÓN NORMALIZADA POR GAS
   ============================================================================ */

function normalizarGas(tipo, valor) {
    const r = RANGOS[tipo];
    if (!r || valor == null) return 0;

    if (valor <= r.bueno)     return 0.10;    // Verde
    if (valor <= r.moderado)  return 0.45;    // Amarillo
    if (valor <= r.insalubre) return 0.75;    // Naranja
    return 1.0;                               // Rojo
}


/* ============================================================================
   3. COLORES VIBRANTES EXACTOS DEL ÍNDICE
   ============================================================================ */

function colorPorNivel(n) {
    if (n <= 0.10)  return [36, 255, 84];   // Verde
    if (n <= 0.45)  return [255, 240, 0];   // Amarillo
    if (n <= 0.75)  return [255, 144, 0];   // Naranja
    return [255, 48, 48];                  // Rojo
}


/* ============================================================================
   4. MAPA
   ============================================================================ */

// ===============================
// MAPA CON VISTA INICIAL OBLIGATORIA
// ===============================
let mapa = L.map("map", { zoomControl: false })
    .setView([39.47, -0.38], 12); // Vista temporal

L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19
}).addTo(mapa);


// ===============================
// GEOLOCALIZACIÓN AUTOMÁTICA
// ===============================
mapa.whenReady(() => {
    if (!navigator.geolocation) {
        console.warn("Geolocalización no soportada.");
        cargarMapa("ALL");
        return;
    }

    navigator.geolocation.getCurrentPosition(
        pos => {
            const lat = pos.coords.latitude;
            const lng = pos.coords.longitude;

            mapa.setView([lat, lng], 15);

            L.circleMarker([lat, lng], {
                radius: 8,
                fillColor: "#18707e",
                color: "#18707e",
                fillOpacity: 1
            }).addTo(mapa);

            cargarMapa("ALL");
        },
        err => {
            console.warn("No se pudo obtener ubicación:", err);
            cargarMapa("ALL");
        },
        { enableHighAccuracy: true, timeout: 5000, maximumAge: 0 }
    );
});





/* ============================================================================
   5. CAPA CANVAS + IDW
   ============================================================================ */

let puntosActuales = [];
const canvasLayer = L.canvasOverlay(dibujarIDW).addTo(mapa);


/* ============================================================================
   6. FUNCIÓN IDW
   ============================================================================ */

function dibujarIDW(layer, params) {
    const canvas = params.canvas;
    const ctx = canvas.getContext("2d");

    ctx.clearRect(0, 0, canvas.width, canvas.height);
    if (!puntosActuales.length) return;

    const zoom = mapa.getZoom();

    // ⭐ Tamaño de influencia por punto
    const RADIO =
        zoom < 11 ? 0.017 :
            zoom < 14 ? 0.010 :
                0.008;

    // ⭐ Resolución del grid → menos = más suave
    const GRID =
        zoom < 12 ? 55 :
            zoom < 14 ? 65 :
                75;

    const stepX = canvas.width / GRID;
    const stepY = canvas.height / GRID;

    const EXP = 2; // Inverse Distance Weighting

    // ⭐ opacidad general de cada círculo
    const ALPHA = 0.07; // <--- AJUSTABLE

    // ⭐ tamaño relativo del círculo (reduce panal)
    const RATIO = 1.1; // <--- AJUSTABLE

    for (let ix = 0; ix < GRID; ix++) {
        for (let iy = 0; iy < GRID; iy++) {

            const px = ix * stepX + stepX / 2;
            const py = iy * stepY + stepY / 2;

            const ll = mapa.containerPointToLatLng([px, py]);
            const lat = ll.lat;
            const lng = ll.lng;

            let sumW = 0, sumR = 0, sumG = 0, sumB = 0;

            for (let p of puntosActuales) {
                const dx = lng - p.lng;
                const dy = lat - p.lat;
                const d = Math.sqrt(dx*dx + dy*dy);

                if (d > RADIO) continue;

                const w = d === 0 ? 1 : 1 / Math.pow(d, EXP);

                sumW += w;
                sumR += w * p.r;
                sumG += w * p.g;
                sumB += w * p.b;
            }

            if (sumW === 0) continue;

            const r = sumR / sumW;
            const g = sumG / sumW;
            const b = sumB / sumW;

            ctx.fillStyle = `rgba(${r}, ${g}, ${b}, ${ALPHA})`;

            ctx.beginPath();
            ctx.arc(
                ix * stepX + stepX / 2,
                iy * stepY + stepY / 2,
                stepX * RATIO,
                0,
                Math.PI * 2
            );
            ctx.fill();
        }
    }
}








/* ============================================================================
   7. CARGA DE DATOS DEL BACKEND
   ============================================================================ */

async function cargarMapa(tipo = "ALL") {
    puntosActuales = [];

    if (tipo === "ALL") {
        const res = await fetch("/mapa/medidas/todos");
        const data = await res.json();
        if (data.status !== "ok") return;

        puntosActuales = data.placas
            .filter(p => p.latitud && p.longitud)
            .map(p => {

                let peor = 0;

                for (const t of [11,12,13,14]) {
                    const v = p[t === 11 ? "NO2" :
                        t === 12 ? "CO"  :
                            t === 13 ? "O3"  : "SO2"];
                    peor = Math.max(peor, normalizarGas(t, v));
                }

                const [r, g, b] = colorPorNivel(peor);

                return { lat: p.latitud, lng: p.longitud, r, g, b };
            });
    }

    else {
        const res = await fetch(`/mapa/medidas/gas?tipo=${tipo}`);
        const data = await res.json();
        if (data.status !== "ok") return;

        puntosActuales = data.medidas.map(m => {
            const norm = normalizarGas(Number(tipo), m.valor);
            const [r, g, b] = colorPorNivel(norm);
            return { lat: m.latitud, lng: m.longitud, r, g, b };
        });
    }

    canvasLayer._reset();
}

// ==============================
// CONTROLES DE ZOOM
// ==============================
const zoomInBtn  = document.getElementById("zoom-in");
const zoomOutBtn = document.getElementById("zoom-out");

if (zoomInBtn) {
    zoomInBtn.addEventListener("click", () => mapa.zoomIn());
}

if (zoomOutBtn) {
    zoomOutBtn.addEventListener("click", () => mapa.zoomOut());
}


// ==============================
// BOTÓN DE GEOLOCALIZACIÓN
// ==============================
const geoBtn = document.getElementById("btn-geoloc");

if (geoBtn) {
    geoBtn.addEventListener("click", () => {
        if (!navigator.geolocation) {
            console.error("Geoloc no soportada");
            return;
        }

        navigator.geolocation.getCurrentPosition(
            pos => {
                const lat = pos.coords.latitude;
                const lng = pos.coords.longitude;
                mapa.setView([lat, lng], 15);

                L.circleMarker([lat, lng], {
                    radius: 8,
                    fillColor: "#18707e",
                    color: "#18707e",
                    fillOpacity: 1,
                }).addTo(mapa);
            },
            err => console.error("Error geoloc:", err)
        );
    });
}

// ======================================================
// AUTOCOMPLETADO + MENÚ DESPLEGABLE (NOMINATIM)
// ======================================================

const searchInput = document.getElementById("search-input");
const suggestionBox = document.getElementById("search-suggestions");

let suggestionTimeout = null;

function clearSuggestions() {
    suggestionBox.innerHTML = "";
    suggestionBox.style.display = "none";
}

async function fetchSuggestions(query) {
    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&addressdetails=1&limit=5`;

    const res = await fetch(url);
    return res.json();
}

// Mostrar sugerencias
function showSuggestions(list) {
    if (!list.length) {
        clearSuggestions();
        return;
    }

    suggestionBox.innerHTML = "";

    list.forEach(item => {
        const div = document.createElement("div");
        div.classList.add("suggestion-item");
        div.textContent = item.display_name;

        div.addEventListener("click", () => {
            moveToLocation(item);
            searchInput.value = item.display_name;
            clearSuggestions();
        });

        suggestionBox.appendChild(div);
    });

    suggestionBox.style.display = "block";
}

// Mover mapa + añadir marcador
function moveToLocation(item) {
    const lat = parseFloat(item.lat);
    const lon = parseFloat(item.lon);

    mapa.setView([lat, lon], 15);

    if (window.searchMarker) mapa.removeLayer(window.searchMarker);

    window.searchMarker = L.marker([lat, lon])
        .addTo(mapa)
        .bindPopup(item.display_name)
        .openPopup();
}

// Evento al escribir → autocompletar
if (searchInput) {
    searchInput.addEventListener("input", () => {
        const query = searchInput.value.trim();

        if (!query) {
            clearSuggestions();
            return;
        }

        // Evitar spam a la API → retraso de 300ms
        clearTimeout(suggestionTimeout);
        suggestionTimeout = setTimeout(async () => {
            const data = await fetchSuggestions(query);
            showSuggestions(data);
        }, 300);
    });

    // Buscar con ENTER
    searchInput.addEventListener("keydown", async (e) => {
        if (e.key !== "Enter") return;

        const query = searchInput.value.trim();
        if (!query) return;

        const data = await fetchSuggestions(query);

        if (!data.length) {
            alert("No se encontró ninguna ubicación.");
            return;
        }

        moveToLocation(data[0]);
        clearSuggestions();
    });
}

// Cerrar menú si clic afuera
document.addEventListener("click", (e) => {
    if (!e.target.closest(".search-bar") && !e.target.closest("#search-suggestions")) {
        clearSuggestions();
    }
});






/* ============================================================================
   8. SELECTOR DE GASES
   ============================================================================ */

document.querySelectorAll(".contaminante-option").forEach(btn => {
    btn.addEventListener("click", () => {
        document.querySelectorAll(".contaminante-option")
            .forEach(b => b.classList.remove("active"));

        btn.classList.add("active");
        cargarMapa(btn.dataset.tipo);
    });
});

mapa.on("moveend zoomend", () => canvasLayer._reset());

cargarMapa("ALL");
