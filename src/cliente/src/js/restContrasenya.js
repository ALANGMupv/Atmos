/**
 * restContrasenya.js
 * -------------------------
 * Módulo del frontend encargado de gestionar el restablecimiento
 * de contraseñas mediante Firebase Authentication.
 *
 * Funcionalidades principales:
 *   - Captura el correo electrónico ingresado por el usuario.
 *   - Envía un email automático de restablecimiento de contraseña.
 *   - Muestra mensajes informativos y maneja errores comunes.
 *   - Redirige opcionalmente al login tras el envío.
 *
 * Autor: Santiago Fuenmayor Ruiz
 * Basado en la estructura de: Alejandro Vazquez Remes
 */

// --------------------------------------------------------------------------
//  Importación de módulos de Firebase
// --------------------------------------------------------------------------
import { initializeApp } from "https://www.gstatic.com/firebasejs/11.0.1/firebase-app.js";
import { getAuth} from "https://www.gstatic.com/firebasejs/11.0.1/firebase-auth.js";

// --------------------------------------------------------------------------
//  Configuración e inicialización de Firebase
// --------------------------------------------------------------------------
/**
 * Configura la aplicación Firebase en el cliente.
 * Los valores corresponden al proyecto registrado en la consola Firebase.
 */
const firebaseConfig = {
    apiKey: "AIzaSyBQ8T4ECyaDpybvoL6M6XbmfaipYfFeEXM",
    authDomain: "atmos-e3f6c.firebaseapp.com",
    projectId: "atmos-e3f6c"
};

// Inicialización del cliente Firebase
const app = initializeApp(firebaseConfig);
const auth = getAuth(app);

// --------------------------------------------------------------------------
//  Manejo del evento de envío del formulario
// --------------------------------------------------------------------------
/**
 * Captura el envío del formulario de restablecimiento de contraseña
 * y ejecuta el flujo principal:
 *
 *   1. Validar el correo ingresado.
 *   2. Enviar email de restablecimiento usando Firebase.
 *   3. Mostrar mensajes informativos según el resultado.
 *   4. (Opcional) Redirigir al login después del éxito.
 */
document.addEventListener("DOMContentLoaded", () => {
    const form = document.querySelector(".formulario-restContrasenya");

    form.addEventListener("submit", async (evt) => {
        evt.preventDefault();

        // ----------------------------------------------------------------------
        //  1) Lectura y validación del correo electrónico
        // ----------------------------------------------------------------------
        const correo = document.getElementById("correo").value.trim();

        if (!correo) {
            alert("Por favor, introduce tu correo electrónico.");
            return;
        }

        try {

            // ----------------------------------------------------------------------
            //  2) Enviar correo de restablecimiento de contraseña
            // ----------------------------------------------------------------------
            // Enviar solicitud al backend personalizado
            const respuesta = await fetch("https://nagufor.upv.edu.es/resetPasswordAtmos", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ correo })
            });

            const datos = await respuesta.json();

            if (datos.status !== "ok") {
                alert("No se ha podido enviar el correo de recuperación.");
                return;
            }

            // ----------------------------------------------------------------------
            //  3) Mostrar mensaje de confirmación al usuario
            // ----------------------------------------------------------------------
            alert(
                "Se ha enviado un correo para restablecer tu contraseña.\n" +
                "Revisa tu bandeja de entrada o la carpeta de spam."
            );

            // Limpieza del formulario tras el envío
            form.reset();

            // ----------------------------------------------------------------------
            //  4) Redirigir opcionalmente al login
            // ----------------------------------------------------------------------
            // window.location.href = "login.php";

        } catch (error) {
            // ----------------------------------------------------------------------
            //  5) Manejo de errores comunes de Firebase Authentication
            // ----------------------------------------------------------------------
            console.error("Error al enviar correo de restablecimiento:", error);

            let mensaje = "Error al enviar el correo de restablecimiento.";

            switch (error.code) {
                case "auth/user-not-found":
                    mensaje = "No existe ninguna cuenta registrada con ese correo.";
                    break;
                case "auth/invalid-email":
                    mensaje = "El formato del correo electrónico no es válido.";
                    break;
                case "auth/missing-email":
                    mensaje = "Debes introducir un correo electrónico válido.";
                    break;
                default:
                    mensaje = "Se ha producido un error inesperado.";
                    break;
            }

            alert(mensaje);
        }
    });
});
