/**
 * --------------------------------------------------------------
 *  Fichero: perfil.js
 *  Autor: Alan Guevara Martínez
 *  Descripción: Controla la apertura y cierre del popup de cierre
 *  de sesión en la página de perfil.
 *  Fecha: 16/11/2025
 * --------------------------------------------------------------
 */

// --------------------------------------------------------------
//  TU CÓDIGO ORIGINAL (NO SE TOCA NADA)
// --------------------------------------------------------------

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



/* ========================================================================
 *  BLOQUE AÑADIDO — CARGA DE FIREBASE EN perfil.php
 * ------------------------------------------------------------------------
 *  Este bloque:
 *    Carga Firebase (app + auth)
 *    Habilita persistencia del login
 *    Recupera al usuario logueado desde Firebase
 *    Lo expone como window.firebaseUser para editar_perfil.js
 * ======================================================================== */

// Esperamos a que Firebase cargue correctamente
import { initializeApp } from "https://www.gstatic.com/firebasejs/11.0.1/firebase-app.js";

import {
    getAuth,
    onAuthStateChanged,
    setPersistence,
    browserLocalPersistence
} from "https://www.gstatic.com/firebasejs/11.0.1/firebase-auth.js";

// Configuración Firebase
const firebaseConfig = {
    apiKey: "AIzaSyBQ8T4ECyaDpybvoL6M6XbmfaipYfFeEXM",
    authDomain: "atmos-e3f6c.firebaseapp.com",
    projectId: "atmos-e3f6c"
};

// Inicializamos Firebase
const app = initializeApp(firebaseConfig);
const auth = getAuth(app);

// Activar persistencia
setPersistence(auth, browserLocalPersistence)
    .then(() => console.log("Persistencia habilitada en perfil.php"))
    .catch(err => console.error("Error persistencia:", err));

// Restaurar sesión real
onAuthStateChanged(auth, (user) => {
    window.firebaseUser = user;

    if (user) {
        console.log("Usuario activo en Firebase:", user.email);
    } else {
        console.warn("NO hay sesión Firebase.");
    }
});
