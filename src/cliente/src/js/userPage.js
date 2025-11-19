// --------------------------------------------------------------------------
// userPage.js
// --------------------------------------------------------------------------
// Script que gestiona diferentes interacciones de la interfaz del usuario
// dentro de la sección "Mi Sensor" de la aplicación Atmos.
//
// Funcionalidades incluidas:
//    1. Mostrar y ocultar múltiples POPUPS informativos mediante atributos
//       data-popup.
//    2. Cerrar los popups desde sus botones de cierre o pulsando sobre el
//       fondo oscuro que los rodea.
//    3. Gestionar el selector de gas contaminante, actualizando dinámicamente
//       el tipo de gas mostrado en las tarjetas de "Última medición" y
//       "Promedio del día".
//
// Autor: Santiago Fuenmayor Ruiz
// --------------------------------------------------------------------------


// --------------------------------------------------------------------------
// 1. Mostrar el popup correspondiente al botón pulsado
// --------------------------------------------------------------------------
document.querySelectorAll("[data-popup]").forEach(boton => {
    boton.addEventListener("click", () => {
        const idPopup = boton.dataset.popup;            // ID del popup a abrir
        document.getElementById(idPopup).style.display = "flex";
    });
});

// --------------------------------------------------------------------------
// 2. Cerrar el popup cuando se pulsa su botón de cerrar
// --------------------------------------------------------------------------
document.querySelectorAll(".cerrar-popup").forEach(botonCerrar => {
    botonCerrar.addEventListener("click", () => {
        const popup = botonCerrar.closest(".popup-info-container");    // popup actual
        if (popup) popup.style.display = "none";
    });
});

// --------------------------------------------------------------------------
// 3. Cerrar el popup si el usuario hace click fuera de la caja
// --------------------------------------------------------------------------
document.querySelectorAll(".popup-info-container").forEach(popup => {
    popup.addEventListener("click", (e) => {
        // Si haces click directamente en el fondo (sección), NO en el cuadro blanco
        if (e.target === popup) {
            popup.style.display = "none";
        }
    });
});


// --------------------------------------------------------------------------
// 4. Selector de gas
// --------------------------------------------------------------------------
// Cambia dinámicamente el tipo de gas mostrado en:
//
//    - Última medición
//    - Promedio del día
//
// Cada vez que se selecciona un gas distinto en el <select>,
// este bloque actualiza los textos de ambas tarjetas.
//
// --------------------------------------------------------------------------

const gasSelector = document.getElementById("gasSelector");
const gasUltima = document.getElementById("gasUltima");
const gasPromedio = document.getElementById("gasPromedio");

if (gasSelector && gasUltima && gasPromedio) {
    gasSelector.addEventListener("change", () => {
        const gasSeleccionado = gasSelector.value;      // Ej: "NO₂", "CO", "O₃", "SO₂"

        gasUltima.textContent = gasSeleccionado;        // Actualiza Última medición
        gasPromedio.textContent = gasSeleccionado;      // Actualiza Promedio del día
    });
}
