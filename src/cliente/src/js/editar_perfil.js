/**
 * ------------------------------------------------------------------
 *  Archivo JS: editar_perfil.js
 *  Autor: Alan Guevara Martínez
 *  Fecha: 16/11/2025
 *
 *  Descripción general:
 *  --------------------
 *  Este script controla:
 *     1. La apertura y cierre del popup para *Cambiar contraseña*.
 *     2. La apertura y cierre del popup para *Confirmar cambios*.
 *     3. La funcionalidad de mostrar/ocultar contraseña mediante
 *        el icono del ojo, igual que en la página de registro.
 * ------------------------------------------------------------------
 */

// --- POPUP A — CAMBIAR CONTRASEÑA ---

// Botón que abre el popup A
const btnPass = document.getElementById("btn-cambiar-pass");

// Popup A completo
const popupPass = document.getElementById("popup-pass");

// Botón cancelar dentro del popup A
const cancelPass = document.getElementById("popup-cancel-pass");

// Mostrar popup al pulsar "Cambiar contraseña"
if (btnPass) {
  btnPass.addEventListener("click", e => {
    e.preventDefault();               // Evita recargar la página
    popupPass.classList.add("active"); // Muestra el popup
  });
}

// Cerrar popup al pulsar "Cancelar"
if (cancelPass) {
  cancelPass.addEventListener("click", () => {
    popupPass.classList.remove("active"); // Oculta el popup
  });
}


// --- POPUP B — CONFIRMAR CAMBIOS ---

// Formulario principal de edición
const form = document.getElementById("form-editar");

// Popup B completo (confirmar cambios)
const popupConfirm = document.getElementById("popup-confirmar");

// Botón cancelar dentro del popup B
const popupCancelConfirm = document.getElementById("popup-cancel-confirm");

// Abrir popup B cuando se intenta enviar el formulario
if (form) {
  form.addEventListener("submit", e => {
    e.preventDefault();                  // Bloquea el envío real
    popupConfirm.classList.add("active"); // Muestra el popup
  });
}

// Cerrar popup B y limpiar el campo contraseña
if (popupCancelConfirm) {
  popupCancelConfirm.addEventListener("click", () => {
    popupConfirm.classList.remove("active");  // Oculta popup
    document.getElementById("popup-pass-confirm").value = ""; // Limpia input
  });
}


// --- OJO DE CONTRASEÑA (MISMA LÓGICA QUE REGISTRO) ---

// Selecciona todos los iconos de ojo con atributo data-target
document.querySelectorAll(".toggle-pass").forEach(icon => {

  icon.addEventListener("click", () => {

    // ID del input asociado al ojo
    const targetId = icon.dataset.target;

    // Input real de contraseña
    const input = document.getElementById(targetId);

    if (!input) return; // Seguridad por si no existe el input

    // Alternar visibilidad del texto
    if (input.type === "password") {
      input.type = "text";         // Muestra la contraseña
      icon.classList.add("active"); // Icono de ojo abierto
    } else {
      input.type = "password";      // Oculta la contraseña
      icon.classList.remove("active"); // Icono de ojo cerrado
    }
  });
});
