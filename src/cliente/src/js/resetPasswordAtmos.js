/**
 * @file resetPassword.js
 * @brief Maneja la lógica del formulario de restablecimiento de contraseña
 *        usando Firebase Authentication.
 *
 * @details
 * Este script:
 *  - Inicializa Firebase en el cliente.
 *  - Obtiene el parámetro `oobCode` enviado por Firebase en el enlace del correo.
 *  - Verifica que el código de restablecimiento sea válido.
 *  - Permite al usuario introducir y confirmar una nueva contraseña.
 *  - Envía la nueva contraseña a Firebase mediante confirmPasswordReset().
 *  - Redirige al login después de actualizarla.
 *
 * @author Alan Guevara Martínez
 * @date 01/12/2025
 */

// -----------------------------------------------------------------------------
// Imports de Firebase SDK
// -----------------------------------------------------------------------------

import { initializeApp } from "https://www.gstatic.com/firebasejs/11.0.1/firebase-app.js";
import {
    getAuth,
    verifyPasswordResetCode,
    confirmPasswordReset
} from "https://www.gstatic.com/firebasejs/11.0.1/firebase-auth.js";

// -----------------------------------------------------------------------------
// Configuración de Firebase usada en este módulo.
// -----------------------------------------------------------------------------

/**
 * @brief Configuración del proyecto Firebase.
 * @details Contiene las claves necesarias para inicializar el proyecto.
 */
const firebaseConfig = {
    apiKey: "AIzaSyBQ8T4ECyaDpybvoL6M6XbmfaipYfFeEXM",
    authDomain: "atmos-e3f6c.firebaseapp.com",
    projectId: "atmos-e3f6c"
};

// -----------------------------------------------------------------------------
// Inicialización de Firebase
// -----------------------------------------------------------------------------

/** @brief Inicializa la app de Firebase. */
const app = initializeApp(firebaseConfig);

/** @brief Instancia del servicio de autenticación de Firebase. */
const auth = getAuth(app);

// -----------------------------------------------------------------------------
// Lógica principal: se ejecuta cuando el DOM está cargado.
// -----------------------------------------------------------------------------

/**
 * @brief Evento principal que controla el flujo de restablecimiento
 *        de contraseña una vez que el documento esté listo.
 */
document.addEventListener("DOMContentLoaded", () => {

    // -------------------------------------------------------------------------
    // Obtención del parámetro oobCode enviado por el enlace de Firebase.
    // -------------------------------------------------------------------------

    /**
     * @brief Objeto para leer parámetros GET de la URL.
     */
    const params = new URLSearchParams(window.location.search);

    /**
     * @brief Código temporal enviado por Firebase para el reset de contraseña.
     */
    const oobCode = params.get("oobCode");

    /**
     * @brief Si no existe el código, el enlace no es válido.
     */
    if (!oobCode) {
        alert("Enlace de restablecimiento inválido.");
        return;
    }

    /**
     * @brief Referencia al formulario de restablecimiento.
     */
    const form = document.getElementById("form-reset");

    // -------------------------------------------------------------------------
    // Evento submit del formulario.
    // -------------------------------------------------------------------------

    /**
     * @brief Maneja el envío del formulario y realiza la validación previa.
     */
    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        //! Nueva contraseña ingresada por el usuario.
        const pass1 = document.getElementById("pass1").value.trim();

        //! Confirmación de la contraseña.
        const pass2 = document.getElementById("pass2").value.trim();

        // ---------------------------------------------------------------------
        // Validaciones del lado del cliente
        // ---------------------------------------------------------------------

        /** @brief Comprueba que ambas contraseñas coinciden. */
        if (pass1 !== pass2) {
            alert("Las contraseñas no coinciden.");
            return;
        }

        /** @brief Valida longitud mínima recomendada por Firebase. */
        if (pass1.length < 6) {
            alert("La nueva contraseña debe tener al menos 6 caracteres.");
            return;
        }

        // ---------------------------------------------------------------------
        // Flujo Firebase: verificar código y actualizar contraseña.
        // ---------------------------------------------------------------------

        try {
            /**
             * @brief Verifica que el enlace sea válido y no haya expirado.
             */
            await verifyPasswordResetCode(auth, oobCode);

            /**
             * @brief Envía la nueva contraseña a Firebase.
             */
            await confirmPasswordReset(auth, oobCode, pass1);

            alert("Contraseña actualizada correctamente.");

            /**
             * @brief Redirige al login una vez completado el proceso.
             */
            window.location.href = "login.php";

        } catch (err) {
            console.error(err);
            alert("El enlace ha expirado o es inválido.");
        }
    });

});