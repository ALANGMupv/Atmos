/**
 * login.js
 * -------------------------
 * Módulo del frontend encargado de gestionar el inicio de sesión de la APP.
 *
 * Autor: Alejandro Vazquez Remes
 */

import { initializeApp } from "https://www.gstatic.com/firebasejs/11.0.1/firebase-app.js";
import { getAuth, signInWithEmailAndPassword } from "https://www.gstatic.com/firebasejs/11.0.1/firebase-auth.js";

const firebaseConfig = {
  apiKey: "AIzaSyBQ8T4ECyaDpybvoL6M6XbmfaipYfFeEXM",
  authDomain: "atmos-e3f6c.firebaseapp.com",
  projectId: "atmos-e3f6c"
};

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);

document.addEventListener("DOMContentLoaded", () => {
  const form = document.querySelector(".formulario-login");

  form.addEventListener("submit", async (evt) => {
    evt.preventDefault();

    const correo = document.getElementById("correo").value.trim();
    const contrasena = document.getElementById("contrasena").value;

    if (!correo || !contrasena) {
      alert("Por favor, completa todos los campos.");
      return;
    }

    try {
      //Iniciar sesión con Firebase
      const cred = await signInWithEmailAndPassword(auth, correo, contrasena);
      const user = cred.user;

      // Verificar si el correo está confirmado
      if (!user.emailVerified) {
        alert("Por favor, verifica tu correo electrónico antes de iniciar sesión.");
        return;
      }

      // ¡Obtener ID Token
      const idToken = await user.getIdToken();

      // Enviar token al backend (Node) para validar y obtener los datos del usuario
      const response = await fetch("https://nagufor.upv.edu.es/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": "Bearer " + idToken
        }
      });

      const data = await response.json();
	  console.log("Respuesta cruda del backend:", data);

      if (data.status === "ok") {
        // Crear la sesión PHP local con los datos del usuario
		console.log("Datos recibidos del backend /login:", data);
        await fetch("guardarSesion.php", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(data.usuario)
        });

        // 6Redirigir al home (index.php)
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
