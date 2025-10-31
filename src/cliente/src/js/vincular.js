/**
 * vincular.js
 * ---------------------------------------------------------
 * Módulo del frontend encargado de gestionar la vinculación
 * de un dispositivo (placa) con un usuario registrado.
 *
 * Funcionalidad:
 *  - Captura el código del dispositivo desde el formulario.
 *  - Obtiene el id_usuario activo desde la sesión (PHP o sessionStorage).
 *  - Envía una petición POST al backend (API REST /vincular).
 *  - Muestra mensajes de éxito o error según la respuesta.
 *
 * Autor: Alan Guevara Martínez
 * Fecha: 31 / 10 / 2025
 * ---------------------------------------------------------
 */

document.addEventListener("DOMContentLoaded", () => {
    // ---------------------------------------------------------------------------
    // Seleccionamos el formulario HTML por su clase
    // ---------------------------------------------------------------------------
    const form = document.querySelector(".formulario-vincular");

    // ---------------------------------------------------------------------------
    // Capturamos el evento "submit" (cuando el usuario pulsa "Vincular")
    // ---------------------------------------------------------------------------
    form.addEventListener("submit", async (evt) => {
        evt.preventDefault(); // Evita el comportamiento por defecto del formulario

        // -------------------------------------------------------------------------
        // Capturar el valor introducido por el usuario
        // -------------------------------------------------------------------------
        const codigo = document.getElementById("codigo").value.trim();

        // ID del usuario actual (se guarda tras el login)
        // Puede venir desde PHP (inyectado en window.ID_USUARIO)
        // o desde sessionStorage (guardado tras el login exitoso)
        const id_usuario = window.ID_USUARIO || sessionStorage.getItem("id_usuario");

        // Validar campos obligatorios
        if (!codigo) {
            alert("Por favor, introduce el código del dispositivo.");
            return;
        }

        if (!id_usuario) {
            alert("Error: No se ha encontrado el usuario activo en la sesión.");
            return;
        }

        // -------------------------------------------------------------------------
        // Preparar los datos que se enviarán al backend (payload)
        // -------------------------------------------------------------------------
        const payload = {
            id_usuario: id_usuario,
            id_placa: codigo
        };

        // -------------------------------------------------------------------------
        // Enviar la solicitud al backend (API REST)
        // Endpoint definido en ReglasREST.js → POST /vincular
        // -------------------------------------------------------------------------
        try {
            const response = await fetch("https://nagufor.upv.edu.es/vincular", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(payload)
            });

            // Intentamos convertir la respuesta del servidor a JSON
            const data = await response.json();

            // -----------------------------------------------------------------------
            // Analizar la respuesta y mostrar mensajes al usuario
            // -----------------------------------------------------------------------
            if (response.ok && data.status === "ok") {
                alert("Dispositivo vinculado correctamente.");

                // Guardamos el ID del dispositivo vinculado por si se usa más adelante
                sessionStorage.setItem("placa_vinculada", codigo);

                // Redirigimos al panel principal o a la página de inicio
                window.location.href = "index.php";
            } else {
                // En caso de error, mostramos el mensaje devuelto por el backend
                const mensajeError = data.error || data.mensaje || "Error desconocido.";
                alert("No se pudo vincular el dispositivo: " + mensajeError);
            }
        } catch (err) {
            // -----------------------------------------------------------------------
            // Manejo de errores de red o servidor
            // -----------------------------------------------------------------------
            console.error("Error al vincular el dispositivo:", err);
            alert("Error de conexión: no se pudo contactar con el servidor.");
        }
    });
});
