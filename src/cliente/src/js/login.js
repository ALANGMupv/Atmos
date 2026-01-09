/**
 * @file login.js
 * @brief Gestión del inicio de sesión de la aplicación.
 *
 * Este módulo del frontend se encarga de:
 * - Autenticar usuarios mediante Firebase Authentication.
 * - Obtener el ID Token emitido por Firebase.
 * - Validar la sesión con el backend (Node.js + MySQL).
 * - Crear sesión local en el servidor (PHP).
 * - Sincronizar la contraseña actual de Firebase con MySQL
 *   tras un restablecimiento de contraseña.
 *
 * @author Alejandro Vazquez Remes
 * @note Modificaciones por Santiago Fuenmayor Ruiz
 *       (sincronización de contraseña Firebase ↔ MySQL)
 */

// ==========================================================
// IMPORTACIÓN DE MÓDULOS FIREBASE
// ==========================================================

import { initializeApp } from "https://www.gstatic.com/firebasejs/11.0.1/firebase-app.js";

import {
  getAuth,
  signInWithEmailAndPassword,
  setPersistence,
  browserLocalPersistence
} from "https://www.gstatic.com/firebasejs/11.0.1/firebase-auth.js";

// ==========================================================
// CONFIGURACIÓN E INICIALIZACIÓN DE FIREBASE
// ==========================================================

/** Configuración pública del proyecto Firebase */
const firebaseConfig = {
  apiKey: "AIzaSyBQ8T4ECyaDpybvoL6M6XbmfaipYfFeEXM",
  authDomain: "atmos-e3f6c.firebaseapp.com",
  projectId: "atmos-e3f6c"
};

/** Inicialización de Firebase */
const app = initializeApp(firebaseConfig);

/** Servicio de autenticación Firebase */
const auth = getAuth(app);

/**
 * @brief Habilita la persistencia del login.
 *
 * Permite que el usuario permanezca autenticado
 * aunque navegue entre páginas.
 */
setPersistence(auth, browserLocalPersistence)
  .then(() => {
    console.log("Persistencia habilitada: el login se mantiene al cambiar de página.");
  })
  .catch(err => {
    console.error("Error aplicando la persistencia:", err);
  });

// ==========================================================
// UTILIDADES
// ==========================================================

/**
 * @brief Traduce los códigos de error de Firebase a mensajes en español.
 *
 * @param {string} code Código de error devuelto por Firebase.
 * @return {string} Mensaje traducido y comprensible para el usuario.
 */
function traducirErrorFirebase(code) {
  switch (code) {
    case "auth/user-not-found":
    case "auth/invalid-credential":
      return "Correo o contraseña incorrectos.";

    case "auth/wrong-password":
      return "La contraseña es incorrecta.";

    case "auth/invalid-email":
      return "El formato del correo electrónico no es válido.";

    case "auth/user-disabled":
      return "Esta cuenta ha sido deshabilitada.";

    case "auth/too-many-requests":
      return "Demasiados intentos fallidos. Inténtalo más tarde.";

    default:
      return "Error al iniciar sesión. Inténtalo de nuevo.";
  }
}

// ==========================================================
// MANEJADOR PRINCIPAL DE INICIO DE SESIÓN
// ==========================================================

/**
 * @brief Inicializa el formulario de login al cargar el DOM.
 */
document.addEventListener("DOMContentLoaded", () => {

  /** Formulario de inicio de sesión */
  const form = document.querySelector(".formulario-login-pagLogin");

  form.addEventListener("submit", async (evt) => {
    evt.preventDefault();

    // ======================================================
    // 1) LECTURA Y VALIDACIÓN DEL FORMULARIO
    // ======================================================

    const correo = document.getElementById("correo").value.trim();
    const contrasena = document.getElementById("contrasena").value;

    if (!correo || !contrasena) {
      alert("Por favor, completa todos los campos.");
      return;
    }

    try {
      // ====================================================
      // 2) AUTENTICACIÓN CON FIREBASE
      // ====================================================

      const cred = await signInWithEmailAndPassword(auth, correo, contrasena);
      const user = cred.user;

      // ====================================================
      // 3) VERIFICACIÓN DE CORREO
      // ====================================================

      if (!user.emailVerified) {
        alert("Por favor, verifica tu correo electrónico antes de iniciar sesión.");
        return;
      }

      // ====================================================
      // 4) OBTENCIÓN DEL ID TOKEN
      // ====================================================

      const idToken = await user.getIdToken();

      // ====================================================
      // 5) VALIDACIÓN CON BACKEND (NODE)
      // ====================================================

      const response = await fetch("https://nagufor.upv.edu.es/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": "Bearer " + idToken
        }
      });

      const data = await response.json();

      // ====================================================
      // 6) PROCESAMIENTO DE RESPUESTA
      // ====================================================

      if (data.status === "ok") {

        // --------------------------------------------------
        // 6.1) CREACIÓN DE SESIÓN PHP
        // --------------------------------------------------

        console.log("JSON enviado a guardarSesion.php:", {
          id_usuario: data.usuario.id_usuario,
          nombre: data.usuario.nombre,
          apellidos: data.usuario.apellidos,
          email: data.usuario.email,
          id_rol: data.usuario.id_rol
        });

        await fetch("guardarSesion.php", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            id_usuario: data.usuario.id_usuario,
            nombre: data.usuario.nombre,
            apellidos: data.usuario.apellidos,
            email: data.usuario.email,
            id_rol: data.usuario.id_rol
          })
        });

        // --------------------------------------------------
        // 6.2) SINCRONIZACIÓN DE CONTRASEÑA CON MYSQL
        // --------------------------------------------------

        try {
          /**
           * Flujo de sincronización:
           * - El usuario puede haber restablecido la contraseña en Firebase.
           * - Al iniciar sesión correctamente, se actualiza también
           *   la contraseña almacenada en MySQL.
           */
          const user = auth.currentUser;
          const nuevaContrasena = contrasena;
          const idToken = await user.getIdToken();

          const { id_usuario, nombre, apellidos, email } = data.usuario;

          const respuestaSync = await fetch("https://nagufor.upv.edu.es/usuario", {
            method: "PUT",
            headers: {
              "Content-Type": "application/json",
              "Authorization": "Bearer " + idToken
            },
            body: JSON.stringify({
              id_usuario: id_usuario,
              nombre: nombre,
              apellidos: apellidos,
              email: email,
              contrasena_actual: nuevaContrasena,
              nueva_contrasena: nuevaContrasena
            })
          });

          if (respuestaSync.ok) {
            console.log("Contraseña sincronizada correctamente con MySQL.");
          } else {
            console.warn("No se pudo sincronizar la contraseña con MySQL.");
          }

        } catch (syncError) {
          console.error("Error sincronizando contraseña con MySQL:", syncError);
        }

        // --------------------------------------------------
        // 6.3) REDIRECCIÓN SEGÚN ROL
        // --------------------------------------------------

        if (data.usuario.id_rol === 2) {
          window.location.href = "informe_nodos.php";
        } else {
          window.location.href = "userPage.php";
        }

      } else {
        alert("Error: " + (data.error || "Error desconocido"));
      }

    } catch (err) {
      console.error("Error en login:", err);
      alert(traducirErrorFirebase(err.code));
    }
  });
});
