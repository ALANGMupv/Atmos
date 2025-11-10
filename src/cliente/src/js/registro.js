/**
 * registro.js
 * -------------------------
 * Módulo del frontend encargado de gestionar el registro de usuarios.
 *
 * Funcionalidades principales:
 *   - Inicializa Firebase Authentication en el cliente.
 *   - Crea un nuevo usuario mediante Firebase (email y contraseña).
 *   - Obtiene el token JWT emitido por Firebase.
 *   - Envía los datos del usuario al servidor REST (Node.js + MySQL).
 *
 * Este módulo reemplaza el uso de formularios PHP tradicionales.
 *
 * Autor: Santiago Fuenmayor Ruiz
 */

// --------------------------------------------------------------------------
//  Importación de módulos de Firebase
// --------------------------------------------------------------------------
import { initializeApp } from "https://www.gstatic.com/firebasejs/11.0.1/firebase-app.js";
import { getAuth, createUserWithEmailAndPassword, sendEmailVerification } 
  from "https://www.gstatic.com/firebasejs/11.0.1/firebase-auth.js";

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
//  Manejo del evento de registro
// --------------------------------------------------------------------------
/**
 * Captura el envío del formulario de registro y realiza las siguientes acciones:
 *   1. Valida los datos ingresados por el usuario.
 *   2. Crea la cuenta en Firebase Authentication.
 *   3. Obtiene el token JWT (ID Token) de Firebase.
 *   4. Envía los datos del usuario al backend mediante fetch() POST /usuario.
 *
 * Si la operación tiene éxito, el backend registrará el usuario en MySQL.
 */
document.addEventListener("DOMContentLoaded", () => {
  const form = document.querySelector(".formulario-registro");

  form.addEventListener("submit", async (evt) => {
    evt.preventDefault();

    // ----------------------------------------------------------------------
    //  Lectura y validación de datos del formulario
    // ----------------------------------------------------------------------
    const nombre = document.getElementById("nombre").value.trim();
    const apellidos = document.getElementById("apellidos").value.trim();
    const correo = document.getElementById("correo").value.trim();
    const contrasena = document.getElementById("contrasena").value;
    const repetir = document.getElementById("repetir").value;

    if (!nombre || !correo || !contrasena) {
      alert("Por favor, completa todos los campos.");
      return;
    }

    if (contrasena !== repetir) {
      alert("Las contraseñas no coinciden.");
      return;
    }

    // ----------------------------------------------------------------------
    //  Flujo principal de registro
    // ----------------------------------------------------------------------
    try {
      // 1) Crear usuario en Firebase Authentication
      const cred = await createUserWithEmailAndPassword(auth, correo, contrasena);

      // 1.5) Enviar correo de verificación
      await sendEmailVerification(cred.user);

      // 2) Obtener el token JWT emitido por Firebase
      const idToken = await cred.user.getIdToken();

      // 3) Enviar los datos del usuario al backend (Node.js /usuario)
      const response = await fetch("https://nagufor.upv.edu.es/usuario", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": "Bearer " + idToken
        },
        body: JSON.stringify({
          nombre: nombre,
          apellidos: apellidos,
          contrasena: contrasena
        })
      });

      // 4) Interpretar la respuesta del servidor REST
      const data = await response.json();

      if (data.status === "ok") {
        alert("Se ha enviado una verificación a tu email. Podría haber llegado a SPAM");
        // Si se desea, redirigir al login:
        // window.location.href = "login.php";
      } else {
        alert("Error en el registro: " + (data.error || "Error desconocido"));
      }

    } catch (err) {
      console.error("Error durante el registro:", err);
      alert("Error: " + err.message);
    }
  });
});
