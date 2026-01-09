/**
 * @file editar_perfil.js
 * @author Alan Guevara Martínez
 * @date 16/11/2025
 * @brief Gestión de edición de perfil de usuario.
 *
 * Este script se encarga de:
 * 1. Abrir y cerrar los popups de cambio de contraseña y confirmación.
 * 2. Alternar la visibilidad de las contraseñas mediante el icono de ojo.
 * 3. Validar la contraseña del usuario con Firebase antes de actualizar datos.
 * 4. Enviar los cambios al backend autenticado.
 */

// ==========================================================
// POPUP A — CAMBIAR CONTRASEÑA
// ==========================================================

/** Botón que abre el popup de cambio de contraseña */
const btnPass = document.getElementById("btn-cambiar-pass");

/** Contenedor completo del popup de cambio de contraseña */
const popupPass = document.getElementById("popup-pass");

/** Botón cancelar del popup de cambio de contraseña */
const cancelPass = document.getElementById("popup-cancel-pass");

/**
 * @brief Muestra el popup de cambio de contraseña.
 */
if (btnPass) {
  btnPass.addEventListener("click", e => {
    e.preventDefault();
    popupPass.classList.add("active");
  });
}

/**
 * @brief Cierra el popup de cambio de contraseña.
 */
if (cancelPass) {
  cancelPass.addEventListener("click", () => {
    popupPass.classList.remove("active");
  });
}


// ==========================================================
// POPUP B — CONFIRMAR CAMBIOS
// ==========================================================

/** Formulario principal de edición de perfil */
const form = document.getElementById("form-editar");

/** Contenedor del popup de confirmación de cambios */
const popupConfirm = document.getElementById("popup-confirmar");

/** Botón cancelar del popup de confirmación */
const popupCancelConfirm = document.getElementById("popup-cancel-confirm");

/**
 * @brief Abre el popup de confirmación al intentar enviar el formulario.
 */
if (form) {
  form.addEventListener("submit", e => {
    e.preventDefault();
    popupConfirm.classList.add("active");
  });
}

/**
 * @brief Cierra el popup de confirmación y limpia el campo de contraseña.
 */
if (popupCancelConfirm) {
  popupCancelConfirm.addEventListener("click", () => {
    popupConfirm.classList.remove("active");
    document.getElementById("popup-pass-confirm").value = "";
  });
}


// ==========================================================
// VISIBILIDAD DE CONTRASEÑA (ICONO OJO)
// ==========================================================

/**
 * @brief Alterna la visibilidad de los campos de contraseña.
 *
 * Cada icono usa el atributo data-target para indicar
 * el input de contraseña asociado.
 */
document.querySelectorAll(".toggle-pass").forEach(icon => {

  icon.addEventListener("click", () => {

    const targetId = icon.dataset.target;
    const input = document.getElementById(targetId);

    if (!input) return;

    if (input.type === "password") {
      input.type = "text";
      icon.classList.add("active");
    } else {
      input.type = "password";
      icon.classList.remove("active");
    }
  });
});


// ==========================================================
// VALIDACIÓN DE CAMBIOS CON FIREBASE
// ==========================================================

/**
 * @brief Inicialización de Firebase.
 *
 * Todos los imports de firebase-auth se unifican
 * para evitar errores en módulos ES.
 */
import { initializeApp } from "https://www.gstatic.com/firebasejs/11.0.1/firebase-app.js";

import {
  getAuth,
  signInWithEmailAndPassword,
  EmailAuthProvider,
  reauthenticateWithCredential,
  onAuthStateChanged,
  setPersistence,
  browserLocalPersistence
} from "https://www.gstatic.com/firebasejs/11.0.1/firebase-auth.js";

/** Configuración pública del proyecto Firebase */
const firebaseConfig = {
  apiKey: "AIzaSyBQ8T4ECyaDpybvoL6M6XbmfaipYfFeEXM",
  authDomain: "atmos-e3f6c.firebaseapp.com",
  projectId: "atmos-e3f6c"
};

/** Aplicación Firebase inicializada */
const app = initializeApp(firebaseConfig);

/** Servicio de autenticación Firebase */
const auth = getAuth(app);

/**
 * @brief Habilita la persistencia de sesión del usuario.
 */
setPersistence(auth, browserLocalPersistence)
  .catch(err => console.error("Error en persistencia:", err));


// ==========================================================
// GESTIÓN DE SESIÓN FIREBASE
// ==========================================================

/** Usuario autenticado actualmente en Firebase */
let firebaseUser = null;

/**
 * @brief Detecta cambios en el estado de autenticación.
 *
 * @param {Object|null} user Usuario autenticado o null.
 */
onAuthStateChanged(auth, (user) => {
  firebaseUser = user;
});


// ==========================================================
// CONFIRMACIÓN DE CAMBIOS Y BACKEND
// ==========================================================

/** Botón de confirmación del popup */
const popupConfirmBtn = document.getElementById("popup-confirmar-btn");

/**
 * @brief Valida la contraseña del usuario y actualiza los datos del perfil.
 */
if (popupConfirmBtn) {

  popupConfirmBtn.addEventListener("click", async () => {

    const nombre = document.getElementById("nombre").value.trim();
    const apellidos = document.getElementById("apellidos").value.trim();
    const email = document.getElementById("correo").value.trim();
    const contrasena = document.getElementById("popup-pass-confirm").value;

    if (!contrasena) {
      alert("Introduce tu contraseña.");
      return;
    }

    const id_usuario = document.getElementById("id_usuario").value;

    if (!firebaseUser) {
      alert("Tu sesión ha caducado. Vuelve a iniciar sesión para continuar.");
      window.location.href = "login.php";
      return;
    }

    try {
      const credential = EmailAuthProvider.credential(email, contrasena);
      await reauthenticateWithCredential(firebaseUser, credential);

      const token = await firebaseUser.getIdToken();

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
          nueva_contrasena: ""
        })
      });

      const data = await resp.json();

      if (resp.ok) {

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
