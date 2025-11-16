/**
 * --------------------------------------------------------------
 *  Fichero: perfil.js
 *  Autor: Alan Guevara Martínez
 *  Descripción: Controla la apertura y cierre del popup de cierre
 *               de sesión en la página de perfil.
 *  Fecha: 16/11/2025
 * --------------------------------------------------------------
 */

// Obtiene el botón que abre el popup de cierre de sesión
const btnOpen = document.getElementById("btn-open-logout");

// Obtiene el contenedor del popup (ventana emergente)
const popup = document.getElementById("popup-logout");

// Obtiene el botón de cancelar dentro del popup
const btnCancel = document.getElementById("popup-cancel");

// Verifica si el botón de abrir existe en el DOM
// para evitar errores si el elemento no está presente.
if (btnOpen) {
    btnOpen.addEventListener("click", (e) => {
        e.preventDefault();          // Evita que el botón recargue o navegue
        popup.classList.add("active"); // Muestra el popup añadiendo la clase 'active'
    });
}

// Verifica si el botón cancelar del popup existe
if (btnCancel) {
    btnCancel.addEventListener("click", () => {
        popup.classList.remove("active"); // Oculta el popup removiendo la clase 'active'
    });
}
