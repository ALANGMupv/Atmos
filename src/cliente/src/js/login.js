/**
 * login.js
 * -------------------------
 * Módulo del frontend encargado de gestionar el inicio de sesión de la APP.
 *
 * Funcionalidades principales:
 *   - Autenticar usuarios mediante Firebase Authentication.
 *   - Obtener el ID Token emitido por Firebase.
 *   - Validar sesión con el backend (Node.js + MySQL).
 *   - Crear sesión local (PHP) en el servidor.
 *   - Sincronizar la contraseña actual de Firebase con MySQL (tras restablecimiento).
 *
 * Autor: Alejandro Vazquez Remes
 * Modificaciones: Santiago Fuenmayor Ruiz (sincronización de contraseña)
 */

// --------------------------------------------------------------------------
//  Importación de módulos de Firebase
// --------------------------------------------------------------------------
import { initializeApp } from "https://www.gstatic.com/firebasejs/11.0.1/firebase-app.js";

import {
  getAuth,
  signInWithEmailAndPassword,
  setPersistence,
  browserLocalPersistence
} from "https://www.gstatic.com/firebasejs/11.0.1/firebase-auth.js";

// --------------------------------------------------------------------------
//  Configuración e inicialización de Firebase
// --------------------------------------------------------------------------
const firebaseConfig = {
  apiKey: "AIzaSyBQ8T4ECyaDpybvoL6M6XbmfaipYfFeEXM",
  authDomain: "atmos-e3f6c.firebaseapp.com",
  projectId: "atmos-e3f6c"
};

// Inicializar Firebase
const app = initializeApp(firebaseConfig);
const auth = getAuth(app);

// --- HABILITAR PERSISTENCIA DEL LOGIN (OBLIGATORIO)
setPersistence(auth, browserLocalPersistence)
    .then(() => {
      console.log("Persistencia habilitada: el login se mantiene al cambiar de página.");
    })
    .catch(err => {
      console.error("Error aplicando la persistencia:", err);
    });


// --------------------------------------------------------------------------
//  Manejador principal de inicio de sesión
// --------------------------------------------------------------------------
document.addEventListener("DOMContentLoaded", () => {
  const form = document.querySelector(".formulario-login-pagLogin");


  form.addEventListener("submit", async (evt) => {
    evt.preventDefault();

    // ----------------------------------------------------------------------
    //  1) Lectura y validación de los datos del formulario
    // ----------------------------------------------------------------------
    const correo = document.getElementById("correo").value.trim();
    const contrasena = document.getElementById("contrasena").value;

    if (!correo || !contrasena) {
      alert("Por favor, completa todos los campos.");
      return;
    }

    try {
      // ----------------------------------------------------------------------
      //  2) Iniciar sesión con Firebase
      // ----------------------------------------------------------------------
      const cred = await signInWithEmailAndPassword(auth, correo, contrasena);
      const user = cred.user;

      // ----------------------------------------------------------------------
      //  3) Verificar si el correo está confirmado
      // ----------------------------------------------------------------------
      if (!user.emailVerified) {
        alert("Por favor, verifica tu correo electrónico antes de iniciar sesión.");
        return;
      }

      // ----------------------------------------------------------------------
      //  4) Obtener el ID Token emitido por Firebase
      // ----------------------------------------------------------------------
      const idToken = await user.getIdToken();

      // ----------------------------------------------------------------------
      //  5) Enviar token al backend (Node) para validar y obtener datos del usuario
      // ----------------------------------------------------------------------
      const response = await fetch("https://nagufor.upv.edu.es/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": "Bearer " + idToken
        }
      });

      const data = await response.json();

      // ----------------------------------------------------------------------
      //  6) Validar respuesta del backend
      // ----------------------------------------------------------------------
      if (data.status === "ok") {
        // ------------------------------------------------------------------
        //  6.1) Crear sesión PHP local con los datos del usuario
        // ------------------------------------------------------------------
        console.log("JSON QUE VOY A ENVIAR A guardarSesion.php:", {
          id_usuario: data.usuario.id,
          nombre: data.usuario.nombre,
          apellidos: data.usuario.apellidos,
          email: data.usuario.email
        });

        await fetch("guardarSesion.php", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            id_usuario: data.usuario.id_usuario,
            nombre: data.usuario.nombre,
            apellidos: data.usuario.apellidos,
            email: data.usuario.email
          })
        });

        // ------------------------------------------------------------------
        //  6.2) Sincronizar la contraseña nueva en MySQL
        //       (en caso de haber sido restablecida en Firebase)
        // ------------------------------------------------------------------
        try {
          /**
           * Flujo:
           *   - Tras restablecer la contraseña en Firebase, el usuario inicia sesión con la nueva.
           *   - En este punto, aprovechamos para actualizar también el campo "contrasena"
           *     en la base de datos MySQL, manteniendo ambas fuentes sincronizadas.
           */
          const user = auth.currentUser;
          const nuevaContrasena = contrasena; // contraseña usada en este login
          const idToken = await user.getIdToken(); // token actualizado

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
            console.log(" Contraseña sincronizada correctamente con MySQL.");
          } else {
            console.warn(" No se pudo sincronizar la contraseña con MySQL.");
          }

        } catch (syncError) {
          console.error("Error sincronizando contraseña con MySQL:", syncError);
        }

        // ------------------------------------------------------------------
        //  6.3) Redirigir al home principal
        // ------------------------------------------------------------------
        window.location.href = "index.php";

      } else {
        alert("Error: " + (data.error || "Error desconocido"));
      }

    } catch (err) {
      console.error("Error en login:", err);
      alert("Error: " + err.message);
    }
  });
});
