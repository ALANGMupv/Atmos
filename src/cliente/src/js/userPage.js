// --------------------------------------------------------------------------
// popup_info.js
// --------------------------------------------------------------------------
// Script encargado de mostrar y ocultar múltiples POPUPS simples
// dentro de la interfaz de usuario.
//
// Flujo general:
//    - Cada botón lleva el atributo data-popup con el ID del popup a abrir.
//    - Al pulsarlo, se muestra el popup correspondiente.
//    - Cada popup tiene un botón con la clase .cerrar-popup para ocultarlo.
//    - También se cierra haciendo click fuera de la caja.
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
        popup.style.display = "none";
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
