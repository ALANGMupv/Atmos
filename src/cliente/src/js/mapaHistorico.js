/* ============================================================================
   MAPA ATMOS – IDW interpolado con colores vibrantes (MODO HISTÓRICO)
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
    12: { bueno: 1.7,    moderado: 4.4,    insalubre: 8.7   },
    11: { bueno: 0.021,  moderado: 0.053,  insalubre: 0.106 },
    13: { bueno: 0.031,  moderado: 0.061,  insalubre: 0.092 },
    14: { bueno: 0.0076, moderado: 0.019,  insalubre: 0.038 }
};


/* ============================================================================
   2. NORMALIZAR GAS
   ============================================================================ */

function normalizarGas(tipo, valor) {
    const r = RANGOS[tipo];
    if (!r || valor == null) return 0;
    if (valor <= r.bueno) return 0.10;
    if (valor <= r.moderado) return 0.45;
    if (valor <= r.insalubre) return 0.75;
    return 1.0;
}


/* ============================================================================
   3. COLORES
   ============================================================================ */

function colorPorNivel(n) {
    if (n <= 0.10) return [36, 255, 84];
    if (n <= 0.45) return [255, 240, 0];
    if (n <= 0.75) return [255, 144, 0];
    return [255, 48, 48];
}

function clasificarPorColor(r, g, b) {
    if (g > 200 && r < 80) return "buena";
    if (r > 200 && g > 200) return "moderada";
    if (r > 200 && g < 200 && g > 80) return "insalubre";
    return "mala";
}


/* ============================================================================
   4. ÍNDICE
   ============================================================================ */

let contadorNiveles = { buena: 0, moderada: 0, insalubre: 0, mala: 0 };
let totalCeldas = 0;

function registrarNivel(n) {
    contadorNiveles[n]++;
    totalCeldas++;
}

function actualizarIndice() {
    if (totalCeldas === 0) {
        document.querySelector(".porcentaje-buena").textContent = "0%";
        document.querySelector(".porcentaje-moderada").textContent = "0%";
        document.querySelector(".porcentaje-insalubre").textContent = "0%";
        document.querySelector(".porcentaje-mala").textContent = "0%";
        return;
    }
    const pct = v => ((v / totalCeldas) * 100).toFixed(0) + "%";
    document.querySelector(".porcentaje-buena").textContent = pct(contadorNiveles.buena);
    document.querySelector(".porcentaje-moderada").textContent = pct(contadorNiveles.moderada);
    document.querySelector(".porcentaje-insalubre").textContent = pct(contadorNiveles.insalubre);
    document.querySelector(".porcentaje-mala").textContent = pct(contadorNiveles.mala);
}


/* ============================================================================
   5. MAPA
   ============================================================================ */

let mapa = L.map("map", { zoomControl: false }).setView([39.47, -0.38], 12);

L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19
}).addTo(mapa);

mapa.createPane("estacionesPane");
mapa.getPane("estacionesPane").style.zIndex = 650;


/* ============================================================================
   6. ESTACIONES + GEOLOCALIZACIÓN
   ============================================================================ */

mapa.whenReady(() => {
    cargarEstacionesOficiales(mapa);
});


/* ============================================================================
   7. CAPA IDW
   ============================================================================ */

let puntosActuales = [];
const canvasLayer = L.canvasOverlay(dibujarIDW).addTo(mapa);


/* ============================================================================
   8. IDW
   ============================================================================ */

function dibujarIDW(layer, params) {
    const ctx = params.canvas.getContext("2d");
    contadorNiveles = { buena: 0, moderada: 0, insalubre: 0, mala: 0 };
    totalCeldas = 0;

    ctx.clearRect(0, 0, params.canvas.width, params.canvas.height);
    if (!puntosActuales.length) return;

    const bounds = mapa.getBounds();
    const GRID = 65;
    const stepX = params.canvas.width / GRID;
    const stepY = params.canvas.height / GRID;

    for (let ix = 0; ix < GRID; ix++) {
        for (let iy = 0; iy < GRID; iy++) {
            const ll = mapa.containerPointToLatLng([
                ix * stepX + stepX / 2,
                iy * stepY + stepY / 2
            ]);

            if (!bounds.contains(ll)) continue;

            let sumW = 0, r = 0, g = 0, b = 0;

            for (let p of puntosActuales) {
                const d = Math.hypot(ll.lat - p.lat, ll.lng - p.lng);
                if (d > 0.01) continue;
                const w = 1 / (d || 0.0001);
                sumW += w;
                r += w * p.r;
                g += w * p.g;
                b += w * p.b;
            }

            if (!sumW) continue;

            r /= sumW; g /= sumW; b /= sumW;
            registrarNivel(clasificarPorColor(r, g, b));

            ctx.fillStyle = `rgba(${r},${g},${b},0.09)`;
            ctx.beginPath();
            ctx.arc(ix * stepX, iy * stepY, stepX, 0, Math.PI * 2);
            ctx.fill();
        }
    }

    actualizarIndice();
}


/* ============================================================================
   9. CARGA HISTÓRICA (ÚNICO CAMBIO REAL)
   ============================================================================ */

async function cargarMapaHistorico(tipo = "ALL") {
    const fecha = document.getElementById("fecha-input")?.value;
    const hora  = document.getElementById("hora-input")?.value;

    if (!fecha || !hora) {
        alert("Selecciona fecha y hora");
        return;
    }

    puntosActuales = [];

    let url = tipo === "ALL"
        ? `/mapa/medidas/todos/historico?fecha=${fecha}&hora=${hora}`
        : `/mapa/medidas/gas/historico?tipo=${tipo}&fecha=${fecha}&hora=${hora}`;

    console.log(" Filtro histórico aplicado");
    console.log("➡ URL:", url);

    const res = await fetch(url);
    const data = await res.json();

    console.log(" Respuesta backend:", data);

    if (data.status !== "ok") {
        console.error(" Error en backend");
        return;
    }

    const sinDatos =
        (tipo === "ALL" && (!data.placas || data.placas.length === 0)) ||
        (tipo !== "ALL" && (!data.medidas || data.medidas.length === 0));

    if (sinDatos) {
        puntosActuales = [];
        canvasLayer._reset();   // limpia el mapa
        alert("No hay medidas para la fecha y hora seleccionadas");
        return;
    }

    if (tipo === "ALL") {
        console.log(" Placas recibidas:", data.placas.length);
    } else {
        console.log(" Medidas recibidas:", data.medidas.length);
    }


    if (tipo === "ALL") {
        puntosActuales = data.placas.map(p => {
            let peor = 0;
            for (const t of [11,12,13,14]) {
                const v = p[t === 11 ? "NO2" : t === 12 ? "CO" : t === 13 ? "O3" : "SO2"];
                peor = Math.max(peor, normalizarGas(t, v));
            }
            const [r,g,b] = colorPorNivel(peor);
            return { lat: p.latitud, lng: p.longitud, r, g, b };
        });
    } else {
        puntosActuales = data.medidas.map(m => {
            const [r,g,b] = colorPorNivel(normalizarGas(tipo, m.valor));
            return { lat: m.latitud, lng: m.longitud, r, g, b };
        });
    }

    canvasLayer._reset();
}


/* ============================================================================
   10. SELECTOR DE GASES
   ============================================================================ */

document.querySelectorAll(".contaminante-option").forEach(btn => {
    btn.addEventListener("click", () => {
        document.querySelectorAll(".contaminante-option")
            .forEach(b => b.classList.remove("active"));
        btn.classList.add("active");
        cargarMapaHistorico(btn.dataset.tipo);
    });
});


/* ============================================================================
   11. BOTÓN APLICAR FECHA
   ============================================================================ */

document.getElementById("btn-aplicar-fecha")
    ?.addEventListener("click", () => {
        const activo = document.querySelector(".contaminante-option.active");
        cargarMapaHistorico(activo?.dataset.tipo || "ALL");
    });


mapa.on("moveend zoomend", () => canvasLayer._reset());
