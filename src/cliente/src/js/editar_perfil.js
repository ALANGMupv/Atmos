// --------------------------------------------------------------------------
// editar_perfil.js
// --------------------------------------------------------------------------
// Script encargado de gestionar la edición de datos del usuario actual.
//
// Flujo general:
//    El usuario modifica campos en el formulario de perfil.
//    Al pulsar "Guardar cambios", se abre un POPUP pidiendo su contraseña actual.
//    Si confirma, se envía la solicitud PUT /usuario al servidor con los datos nuevos.
//    El backend valida la contraseña y actualiza los datos.
//    Se muestra un mensaje con el resultado.
//
// Autor: Nerea Aguilar Forés
// --------------------------------------------------------------------------

document.addEventListener("DOMContentLoaded", () => {
  // Seleccionar el formulario principal de edición
  const form = document.querySelector(".formulario-editar");
  if (!form) return; // Si no existe, no hacemos nada

  // ------------------------------------------------------------------------
  // POPUP de confirmación de contraseña
  // ------------------------------------------------------------------------
  // (Ya está en el HTML, solo lo referenciamos)
  const overlay = document.getElementById("popup-overlay");
  const confirmBtn = document.getElementById("popup-confirm");
  const cancelBtn = document.getElementById("popup-cancel");

  // ------------------------------------------------------------------------
  //  Evento principal: click en "Guardar cambios"
  // ------------------------------------------------------------------------
  // Este evento NO manda los datos todavía. Primero abre el popup
  // para que el usuario verifique su identidad con su contraseña actual.
  form.addEventListener("submit", (e) => {
    e.preventDefault();

    const nombre = document.getElementById("nombre").value.trim();
    const apellidos = document.getElementById("apellidos").value.trim();
    const email = document.getElementById("correo").value.trim();

    // Si no hay ningún campo cambiado, no abre el popup
    if (!nombre && !apellidos && !email) {
      mostrarToast("No hay cambios que guardar.", "info");
      return;
    }

    // Mostrar el popup de verificación
    overlay.style.display = "flex";

    // Guardar temporalmente los datos a modificar
    overlay.dataset.nombre = nombre;
    overlay.dataset.apellidos = apellidos;
    overlay.dataset.email = email;
  });

  // ------------------------------------------------------------------------
  // Botón "Confirmar" del popup
  // ------------------------------------------------------------------------
  confirmBtn.addEventListener("click", async () => {
    // Obtener la contraseña introducida en el popup
    const contrasena_actual = document.getElementById("popup-pass").value.trim();

    // Recuperamos el ID del usuario logueado.
    const id_usuario = localStorage.getItem("id_usuario");

    // Validaciones básicas
    if (!id_usuario) {
      mostrarToast("No se encontró el usuario actual.", "error");
      overlay.style.display = "none";
      return;
    }

    if (!contrasena_actual) {
      mostrarToast("Debes introducir tu contraseña.", "warning");
      return;
    }

    // Recuperamos los datos almacenados temporalmente en el overlay
    const nombre = overlay.dataset.nombre;
    const apellidos = overlay.dataset.apellidos;
    const email = overlay.dataset.email;

    // Creamos un objeto con solo los campos que el usuario haya editado
    const datosActualizados = { id_usuario, contrasena_actual };
    if (nombre) datosActualizados.nombre = nombre;
    if (apellidos) datosActualizados.apellidos = apellidos;
    if (email) datosActualizados.email = email;

    // ----------------------------------------------------------------------
    // Envío de la petición PUT al servidor REST
    // ----------------------------------------------------------------------
    try {
      const resp = await fetch("https://nagufor.upv.edu.es/usuario", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(datosActualizados)
      });

      const data = await resp.json();

      // Interpretamos la respuesta del backend
      if (data.status === "ok") {
        mostrarToast("Datos actualizados correctamente ✅", "success");
        form.reset(); // Limpia el formulario tras guardar
      } else {
        mostrarToast(data.error || "Error al actualizar los datos.", "error");
      }
    } catch (err) {
      console.error("Error en PUT /usuario:", err);
      mostrarToast("No se pudo conectar con el servidor.", "error");
    }

    // Cerrar el popup y limpiar el input de contraseña
    overlay.style.display = "none";
    document.getElementById("popup-pass").value = "";
  });

  // ------------------------------------------------------------------------
  // Botón "Cancelar" del popup.
  // ------------------------------------------------------------------------
  cancelBtn.addEventListener("click", () => {
    overlay.style.display = "none"; // Oculta el popup
    document.getElementById("popup-pass").value = ""; // Limpia el campo
  });
});

// Mostrar notificaciones toast
function mostrarToast(mensaje, tipo = "info", duracion = 4000) {
  let box = document.getElementById("toast");
  if (!box) {
    box = document.createElement("div");
    box.id = "toast";
    box.style.cssText = `
      position:fixed; bottom:20px; right:20px; padding:15px 20px;
      border-radius:10px; color:white; font-family:sans-serif; z-index:9999;
      box-shadow:0 0 10px rgba(0,0,0,0.3); opacity:0; transition:opacity 0.3s;
    `;
    document.body.appendChild(box);
  }

  // Colores según el tipo de mensaje
  const colores = {
    success: "#4CAF50", // verde
    error: "#E53935",   // rojo
    warning: "#FFC107", // amarillo
    info: "#333"        // gris oscuro
  };

  // Personalizar el mensaje y color
  box.style.background = colores[tipo] || "#333";
  box.textContent = mensaje;
  box.style.opacity = "1";

  // Ocultar el toast.
  setTimeout(() => { box.style.opacity = "0"; }, duracion);
}
