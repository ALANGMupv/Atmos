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
 * Autor: Santiago Fuenmayor Ruiz y Alan Guevara Martínez
 */

// --------------------------------------------------------------------------
//  Importación de módulos de Firebase
// --------------------------------------------------------------------------
import { initializeApp } from "https://www.gstatic.com/firebasejs/11.0.1/firebase-app.js";
import { getAuth, createUserWithEmailAndPassword, sendEmailVerification } from "https://www.gstatic.com/firebasejs/11.0.1/firebase-auth.js";


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
  const form = document.querySelector(".formulario-registro-pag");

  // ----------------------------------------------------------------------
  //  Validación visual en tiempo real
  // ----------------------------------------------------------------------
  const inputContrasena = document.getElementById("contrasena");
  const inputRepetir = document.getElementById("repetir");

  // Patrones de validación (6 caracteres y almenos un número)
  const regexPassword =   /^(?=.*[A-Za-z])(?=.*[0-9\W]).{8,}$/;

  function validarInput(input, condicion) {
    if (condicion) {
      input.classList.add("valid");
      input.classList.remove("invalid");
    } else {
      input.classList.add("invalid");
      input.classList.remove("valid");
    }
  }

  // Contraseña
  inputContrasena.addEventListener("input", () => {
    validarInput(inputContrasena, regexPassword.test(inputContrasena.value));
  });

  // Repetir contraseña
  inputRepetir.addEventListener("input", () => {
    validarInput(inputRepetir, inputRepetir.value === inputContrasena.value);
  });

  // Mostrar / ocultar contraseña icono ojo
  document.querySelectorAll(".toggle-pass").forEach(icon => {
    icon.addEventListener("click", () => {
      const input = document.getElementById(icon.dataset.target);

      if (input.type === "password") {
        input.type = "text";
        icon.classList.add("active");
      } else {
        input.type = "password";
        icon.classList.remove("active");
      }
    });
  });

  form.addEventListener("submit", async (evt) => {
    evt.preventDefault();

    // Lectura y validación de datos del formulario
    const nombre = document.getElementById("nombre").value.trim();
    const apellidos = document.getElementById("apellidos").value.trim();
    const correo = document.getElementById("correo").value.trim();
    const contrasena = document.getElementById("contrasena").value;
    const repetir = document.getElementById("repetir").value;
    const politica = document.getElementById("politica");

    if (!nombre || !correo || !contrasena) {
      alert("Por favor, completa todos los campos.");
      return;
    }

    if (!regexPassword.test(contrasena)) {
      alert("La contraseña debe tener mínimo 8 caracteres e incluir letras y al menos un número o un símbolo.");
      return;
    }

    if (contrasena !== repetir) {
      alert("Las contraseñas no coinciden.");
      return;
    }

    if (!politica.checked) {
      alert("Debes aceptar los términos de servicio y la política de privacidad.");
      return;
    }

    // Flujo principal de registro
    try {
      // Crear usuario en Firebase
      const cred = await createUserWithEmailAndPassword(auth, correo, contrasena);

      // Enviar email de verificación
      await sendEmailVerification(cred.user);

      // Obtener token JWT
      const idToken = await cred.user.getIdToken();

      // Guardar usuario en backend
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

      const data = await response.json();

      if (data.status === "ok") {
        alert("Usuario registrado correctamente. Valida tu correo para poder inciar sesión. Revisa tu carpeta de spam.");
        window.location.href = "login.php"; // Redirección añadida
      } else {
        alert("Error en el registro: " + (data.error || "Error desconocido"));
      }

    } catch (err) {
      console.error("Error durante el registro:", err);
      alert("Error: " + err.message);
    }
  });
});
