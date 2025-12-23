/**
 * @file estacionesOficiales.js
 * @brief Carga y dibuja estaciones oficiales OpenAQ en Leaflet.
 *
 * Procedimiento:
 *  - Llama al backend PHP
 *  - Convierte unidades
 *  - Calcula peor contaminante
 *  - Pinta marcadores independientes del IDW
 *
 *  @author Alan Guevara Martínez
 *  @date 23/12/2025
 */

/**
 * =========================================================
 * CONVERSIONES A ppm
 * =========================================================
 */

/**
 * Convierte NO₂ a ppm si la unidad está en µg/m³.
 *
 * @param {number} v Valor numérico del contaminante.
 * @param {string|null} u Unidad del valor ("µg/m³" o ppm).
 * @returns {number} Valor en ppm.
 */
function convNO2(v, u) {
    return u?.includes("µg") ? v / 1880 : v;
}

/**
 * Convierte O₃ a ppm si la unidad está en µg/m³.
 *
 * @param {number} v Valor numérico del contaminante.
 * @param {string|null} u Unidad del valor.
 * @returns {number} Valor en ppm.
 */
function convO3(v, u) {
    return u?.includes("µg") ? v / 2000 : v;
}

/**
 * Convierte SO₂ a ppm si la unidad está en µg/m³.
 *
 * @param {number} v Valor numérico del contaminante.
 * @param {string|null} u Unidad del valor.
 * @returns {number} Valor en ppm.
 */
function convSO2(v, u) {
    return u?.includes("µg") ? v / 2620 : v;
}

/**
 * Convierte CO a ppm si la unidad está en µg/m³.
 *
 * @param {number} v Valor numérico del contaminante.
 * @param {string|null} u Unidad del valor.
 * @returns {number} Valor en ppm.
 */
function convCO(v, u) {
    return u?.includes("µg") ? v / 1145 : v;
}

/**
 * =========================================================
 * NORMALIZACIÓN (MISMO CRITERIO QUE ANDROID)
 * =========================================================
 */

/**
 * Normaliza un valor de contaminante a un nivel de riesgo
 * entre 0 y 1, según el tipo de contaminante.
 *
 * @param {number} tipo Identificador del contaminante:
 *  - 11: NO₂
 *  - 12: CO
 *  - 13: O₃
 *  - 14: SO₂
 * @param {number|null} valor Valor en ppm.
 * @returns {number} Nivel normalizado (0, 0.1, 0.45, 0.75 o 1).
 */
function normal(tipo, valor) {

    if (valor == null) return 0;

    switch (tipo) {
        case 12: // CO
            if (valor <= 1.7) return 0.1;
            if (valor <= 4.4) return 0.45;
            if (valor <= 8.7) return 0.75;
            return 1;

        case 11: // NO₂
            if (valor <= 0.021) return 0.1;
            if (valor <= 0.053) return 0.45;
            if (valor <= 0.106) return 0.75;
            return 1;

        case 13: // O₃
            if (valor <= 0.031) return 0.1;
            if (valor <= 0.061) return 0.45;
            if (valor <= 0.092) return 0.75;
            return 1;

        case 14: // SO₂
            if (valor <= 0.0076) return 0.1;
            if (valor <= 0.019) return 0.45;
            if (valor <= 0.038) return 0.75;
            return 1;
    }

    return 0;
}

/**
 * =========================================================
 * CARGA PRINCIPAL DE ESTACIONES OFICIALES
 * =========================================================
 */

/**
 * Carga las estaciones oficiales desde el backend,
 * calcula el peor contaminante por estación y las
 * representa en el mapa mediante marcadores circulares.
 *
 * @async
 * @param {Object} mapa Instancia del mapa Leaflet.
 * @returns {Promise<void>}
 */
async function cargarEstacionesOficiales(mapa) {

    const res = await fetch("/servidor/estacionesOficiales.php");
    const json = await res.json();

    if (json.status !== "ok") return;

    json.estaciones.forEach(e => {

        let peor = 0;

        if (e.no2 != null)
            peor = Math.max(peor, normal(11, convNO2(e.no2, e.u_no2)));
        if (e.o3 != null)
            peor = Math.max(peor, normal(13, convO3(e.o3, e.u_o3)));
        if (e.so2 != null)
            peor = Math.max(peor, normal(14, convSO2(e.so2, e.u_so2)));
        if (e.co != null)
            peor = Math.max(peor, normal(12, convCO(e.co, e.u_co)));

        let color;
        if (peor <= 0) {
            // Definimos el gris oscuro para estaciones sin datos
            color = "#6B7280";
        } else {
            // Llamamos a la función global de mapaUser.js
            color = colorPorNivel(peor);
        }
        const colorCSS = Array.isArray(color) ? `rgb(${color.join(',')})` : color;

        // Quitamos stroke (borde) y usamos los nuevos colores oscuros
        const iconHTML = `
<svg width="28" height="28" viewBox="0 0 24 24" style="display:block" xmlns="http://www.w3.org/2000/svg">
  <path
    d="M12 2C8.1 2 5 5.1 5 9c0 5.2 7 13 7 13s7-7.8 7-13c0-3.9-3.1-7-7-7z"
    style="fill: ${colorCSS} !important; stroke: none !important;"
  />
  <circle cx="12" cy="9" r="3" style="fill: #ffffff !important;" />
</svg>`;

        const marker = L.marker([e.lat, e.lon], {
            pane: "estacionesPane",
            icon: L.divIcon({
                className: "estacion-marker",
                html: iconHTML,
                iconSize: [28, 28],
                iconAnchor: [14, 28],
                popupAnchor: [0, -28]
            })
        });

        marker.bindPopup(
            `<strong>${e.nombre}</strong><br>
             NO₂: ${e.no2 ?? "—"} ${e.u_no2 ?? ""}<br>
             O₃: ${e.o3 ?? "—"} ${e.u_o3 ?? ""}<br>
             CO: ${e.co ?? "—"} ${e.u_co ?? ""}<br>
             SO₂: ${e.so2 ?? "—"} ${e.u_so2 ?? ""}`
        );

        marker.addTo(mapa);
    });
}