/**
 * ------------------------------------------------------------------
 *  Archivo JS: editar_perfil.js
 *  Autor: Alan Guevara Martínez
 *  Fecha: 16/11/2025
 *
 * Descripción general:
 * --------------------
 * Este script controla:
 *    1. La apertura y cierre de los popups (cambiar contraseña / confirmar cambios).
 *    2. La visibilidad de la contraseña mediante el icono de ojo.
 *    3. La validación de la contraseña en Firebase antes de actualizar los datos.
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


// --- CONFIRMAR CAMBIOS → VALIDACIÓN CON FIREBASE ---

// Importamos los módulos necesarios de Firebase desde su CDN.
// *** IMPORTANTE ***
// Unificamos TODOS los imports de firebase-auth en un solo bloque
// porque usar varios imports separados del mismo archivo rompe los módulos.
import { initializeApp } from "https://www.gstatic.com/firebasejs/11.0.1/firebase-app.js";

import {
  getAuth,
  signInWithEmailAndPassword,
  EmailAuthProvider,
  reauthenticateWithCredential,
  onAuthStateChanged,
  setPersistence,           // <-- añadido en el import unificado
  browserLocalPersistence   // <-- añadido también
} from "https://www.gstatic.com/firebasejs/11.0.1/firebase-auth.js";


// -------- CONFIG FIREBASE --------
// Configuración del proyecto Firebase (claves públicas del frontend).
const firebaseConfig = {
  apiKey: "AIzaSyBQ8T4ECyaDpybvoL6M6XbmfaipYfFeEXM",
  authDomain: "atmos-e3f6c.firebaseapp.com",
  projectId: "atmos-e3f6c"
};

// Inicializamos Firebase con la configuración anterior.
const app = initializeApp(firebaseConfig);

// Obtenemos el servicio de autenticación.
const auth = getAuth(app);


// --- HABILITAR PERSISTENCIA DEL LOGIN ---
// Esto permite que Firebase mantenga al usuario logueado al cambiar de página.
setPersistence(auth, browserLocalPersistence)
    .catch(err => console.error("Error en persistencia:", err));


// Detectamos si hay sesión activa de Firebase
let firebaseUser = null;

onAuthStateChanged(auth, (user) => {
  firebaseUser = user;
});


// --------------------------------------------------------
// Botón del popup de confirmación.
const popupConfirmBtn = document.getElementById("popup-confirmar-btn");

// Si el botón existe en la página...
if (popupConfirmBtn) {

  // Escuchamos el clic del botón para confirmar cambios.
  popupConfirmBtn.addEventListener("click", async () => {

    // Leemos los valores del formulario que el usuario ha editado.
    const nombre = document.getElementById("nombre").value.trim();
    const apellidos = document.getElementById("apellidos").value.trim();
    const email = document.getElementById("correo").value.trim();
    const contrasena = document.getElementById("popup-pass-confirm").value;

    // Si el usuario no ha escrito la contraseña, no continuamos.
    if (!contrasena) {
      alert("Introduce tu contraseña.");
      return;
    }

    // Obtenemos el ID del usuario (inyectado desde PHP).
    const id_usuario = document.getElementById("id_usuario").value;

    // ---------- (A) SESIÓN EXPIRADA EN FIREBASE ----------
    if (!firebaseUser) {
      alert("Tu sesión ha caducado. Vuelve a iniciar sesión para continuar.");
      window.location.href = "login.php";
      return;
    }

    // ---------- (B) VALIDAR CONTRASEÑA CON REAUTHENTICATE ----------
    try {
      const credential = EmailAuthProvider.credential(email, contrasena);

      await reauthenticateWithCredential(firebaseUser, credential);

      // Obtenemos token actualizado
      const token = await firebaseUser.getIdToken();

      // ---------- (C) LLAMAR AL BACKEND PARA ACTUALIZAR DATOS ----------
      const resp = await fetch("https://nagufor.upv.edu.es/usuario", {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          "Authorization": "Bearer " + token
        },
        body: JSON.stringify({
          id_usuario: id_usuario,
          nombre: nombre,
          apellidos: apellidos,
          email: email,
          contrasena_actual: contrasena,
          nueva_contrasena: ""             // cadena vacía porque aquí no la cambiamos
        })
      });

      const data = await resp.json();

      if (resp.ok) {
        // Actualiza la sesión PHP con los nuevos datos
        await fetch("guardarSesion.php", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            id_usuario: id_usuario,
            nombre: nombre,
            apellidos: apellidos,
            email: email
          })
        });

        alert("Cambios guardados correctamente.");
        window.location.href = "perfil.php";
      } else {
        alert("Error al actualizar: " + (data.error || "Error desconocido"));
      }

    } catch (err) {
      console.error(err);
      alert("Contraseña incorrecta.");
    }
  });
}
