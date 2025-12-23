/* ============================================================================
   MAPA ATMOS – IDW interpolado con colores vibrantes (versión corregida)
   ============================================================================ */


/* ============================================================================
   0. Plugin CanvasOverlay
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
        if (this._canvas && pane.contains(this._canvas)) pane.removeChild(this._canvas);
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
   2. NORMALIZAR GAS (0–1)
   ============================================================================ */

function normalizarGas(tipo, valor) {
    const r = RANGOS[tipo];
    if (!r || valor == null) return 0;

    if (valor <= r.bueno)     return 0.10;
    if (valor <= r.moderado)  return 0.45;
    if (valor <= r.insalubre) return 0.75;
    return 1.0;
}



/* ============================================================================
   3. COLORES VIBRANTES EXACTOS DEL ÍNDICE
   ============================================================================ */

function colorPorNivel(n) {
    if (n <= 0.10) return [36, 255, 84];     // Verde
    if (n <= 0.45) return [255, 240, 0];     // Amarillo
    if (n <= 0.75) return [255, 144, 0];     // Naranja
    return [255, 48, 48];                   // Rojo
}

/* ============================================================================
   4. CLASIFICAR COLOR PARA EL ÍNDICE
   ============================================================================ */

function clasificarPorColor(r, g, b) {
    if (g > 200 && r < 80) return "buena";                 // Verde
    if (r > 200 && g > 200) return "moderada";             // Amarillo
    if (r > 200 && g < 200 && g > 80) return "insalubre";  // Naranja
    return "mala";                                         // Rojo
}


/* ============================================================================
   5. CONTADORES PARA EL ÍNDICE
   ============================================================================ */

let contadorNiveles = {
    buena: 0,
    moderada: 0,
    insalubre: 0,
    mala: 0
};

let totalCeldas = 0;

function registrarNivel(nombre) {
    contadorNiveles[nombre]++;
    totalCeldas++;
}

function actualizarIndice() {

    //  Si NO se ha pintado ninguna celda visible → reset total
    if (totalCeldas === 0) {
        document.querySelector(".porcentaje-buena").textContent = "0%";
        document.querySelector(".porcentaje-moderada").textContent = "0%";
        document.querySelector(".porcentaje-insalubre").textContent = "0%";
        document.querySelector(".porcentaje-mala").textContent = "0%";
        return;
    }

    //  Si hay celdas visibles, calcular normalmente
    const pct = f => ((f / totalCeldas) * 100).toFixed(0) + "%";

    document.querySelector(".porcentaje-buena").textContent = pct(contadorNiveles.buena);
    document.querySelector(".porcentaje-moderada").textContent = pct(contadorNiveles.moderada);
    document.querySelector(".porcentaje-insalubre").textContent = pct(contadorNiveles.insalubre);
    document.querySelector(".porcentaje-mala").textContent = pct(contadorNiveles.mala);
}




/* ============================================================================
   6. MAPA
   ============================================================================ */

let mapa = L.map("map", { zoomControl: false })
    .setView([39.47, -0.38], 12);

L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19
}).addTo(mapa);

/* ============================================================================
   PANE PARA ESTACIONES OFICIALES (encima del IDW)
   ============================================================================ */

mapa.createPane("estacionesPane");
mapa.getPane("estacionesPane").style.zIndex = 650;

/* ============================================================================
   7. GEOLOCALIZACIÓN AUTOMÁTICA
   ============================================================================ */

mapa.whenReady(() => {

    /* ============================================================================
       ESTACIONES OFICIALES (OpenAQ)
       ============================================================================ */

    /**
     * Las estaciones oficiales:
     *  - Se cargan UNA sola vez
     *  - No dependen del gas seleccionado
     *  - No dependen del IDW
     *  - Son marcadores independientes (igual que Android)
     */
    cargarEstacionesOficiales(mapa);

    if (!navigator.geolocation) {
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
            console.warn("Geoloc falló:", err);
            cargarMapa("ALL");
        },
        { enableHighAccuracy: true, timeout: 5000 }
    );
});



/* ============================================================================
   8. CAPA CANVAS + IDW
   ============================================================================ */

let puntosActuales = [];
const canvasLayer = L.canvasOverlay(dibujarIDW).addTo(mapa);



/* ============================================================================
   9. FUNCIÓN IDW (versión corregida)
   ============================================================================ */

function dibujarIDW(layer, params) {
    const canvas = params.canvas;
    const ctx = canvas.getContext("2d");

    // Reiniciar indicador
    contadorNiveles = { buena: 0, moderada: 0, insalubre: 0, mala: 0 };
    totalCeldas = 0;

    ctx.clearRect(0, 0, canvas.width, canvas.height);
    if (!puntosActuales.length) return;

    const zoom = mapa.getZoom();
    const bounds = mapa.getBounds();

    const RADIO = zoom < 11 ? 0.017 : zoom < 14 ? 0.010 : 0.008;
    const GRID  = zoom < 12 ? 55   : zoom < 14 ? 65   : 75;

    const stepX = canvas.width / GRID;
    const stepY = canvas.height / GRID;

    const EXP   = 2;
    const ALPHA = 0.09;
    const RATIO = 1.1;

    for (let ix = 0; ix < GRID; ix++) {
        for (let iy = 0; iy < GRID; iy++) {

            const px = ix * stepX + stepX / 2;
            const py = iy * stepY + stepY / 2;

            const ll = mapa.containerPointToLatLng([px, py]);
            const lat = ll.lat;
            const lng = ll.lng;

            // ❗ Solo contar lo visible en pantalla
            if (!bounds.contains([lat, lng])) continue;

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

            // Clasificar color
            const nivel = clasificarPorColor(r, g, b);
            registrarNivel(nivel);

            ctx.fillStyle = `rgba(${r},${g},${b},${ALPHA})`;

            ctx.beginPath();
            ctx.arc(
                ix * stepX + stepX / 2,
                iy * stepY + stepY / 2,
                stepX * RATIO,
                0, Math.PI * 2
            );
            ctx.fill();
        }
    }

    actualizarIndice();
}



/* ============================================================================
   10. CARGAR DATOS DEL BACKEND
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



/* ============================================================================
   11. BOTONES DE ZOOM
   ============================================================================ */

document.getElementById("zoom-in")?.addEventListener("click", () => mapa.zoomIn());
document.getElementById("zoom-out")?.addEventListener("click", () => mapa.zoomOut());



/* ============================================================================
   12. BOTÓN DE GEOLOCALIZACIÓN
   ============================================================================ */

document.getElementById("btn-geoloc")?.addEventListener("click", () => {
    if (!navigator.geolocation) return;

    navigator.geolocation.getCurrentPosition(pos => {
        const lat = pos.coords.latitude;
        const lng = pos.coords.longitude;
        mapa.setView([lat, lng], 15);

        L.circleMarker([lat, lng], {
            radius: 8,
            fillColor: "#18707e",
            color: "#18707e",
            fillOpacity: 1,
        }).addTo(mapa);
    });
});



/* ============================================================================
   13. AUTOCOMPLETADO (Nominatim)
   ============================================================================ */

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

function showSuggestions(list) {
    if (!list.length) return clearSuggestions();

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

if (searchInput) {
    searchInput.addEventListener("input", () => {
        const query = searchInput.value.trim();
        if (!query) return clearSuggestions();

        clearTimeout(suggestionTimeout);
        suggestionTimeout = setTimeout(async () => {
            const data = await fetchSuggestions(query);
            showSuggestions(data);
        }, 300);
    });

    searchInput.addEventListener("keydown", async e => {
        if (e.key !== "Enter") return;

        const data = await fetchSuggestions(searchInput.value.trim());
        if (data.length) {
            moveToLocation(data[0]);
            clearSuggestions();
        }
    });
}

document.addEventListener("click", e => {
    if (!e.target.closest(".search-bar") &&
        !e.target.closest("#search-suggestions")) {
        clearSuggestions();
    }
});



/* ============================================================================
   14. SELECTOR DE GASES
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


/* ============================================================================
   15. PRIMERA CARGA
   ============================================================================ */

cargarMapa("ALL");
