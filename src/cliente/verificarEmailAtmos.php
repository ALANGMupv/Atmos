<?php
/**
 * @file verificarCorreo.php
 * @brief Página encargada de validar el parámetro oobCode enviado por Firebase
 *        para completar la verificación del correo electrónico.
 *
 * Este script recibe un parámetro GET `oobCode` que Firebase envía por correo.
 * Si no existe, se detiene la ejecución mostrando un mensaje de error.
 *
 * @author: Alan Guevara Martínez
 * @date: 02/12/2025
 */

// Obtiene el código de verificación enviado por Firebase
$oobCode = $_GET["oobCode"] ?? null;

/**
 * Si no se recibe el código, se informa al usuario y se detiene el proceso.
 */
if (!$oobCode) {
    echo "Código inválido.";
    exit;
}
?>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">

    <!--
        @section estilos
        Página minimalista que muestra un loader mientras se valida el correo en Firebase.
    -->
    <title>Verificando correo…</title>

    <link rel="stylesheet" href="css/estilos.css">

    <style>
        /**
         * @brief Estilos principales de la pantalla de verificación.
         *
         * Centra el contenido y muestra un cuadro con un spinner animado
         * para indicar que la operación está en proceso.
         */
        body {
            display:flex;
            justify-content:center;
            align-items:center;
            min-height:100vh;
            font-family:'Open Sans', sans-serif;
            background:#F9FAFB;
        }
        .box {
            background:white;
            padding:40px;
            border-radius:16px;
            text-align:center;
            box-shadow:0 10px 25px rgba(0,0,0,0.1);
            max-width:420px;
        }
        .loader {
            margin:20px auto;
            border:4px solid #ddd;
            border-top-color:#059669;
            border-radius:50%;
            width:40px;
            height:40px;
            animation:spin 1s linear infinite;
        }
        @keyframes spin {
            to { transform: rotate(360deg); }
        }

        /* ------------------------------
           ESTILOS AÑADIDOS PARA EL MENSAJE
           ------------------------------ */
        #mensaje {
            display:none;
            margin-top:20px;
            padding:15px;
            border-radius:10px;
            font-size:16px;
            background:#D1FAE5;
            color:#065F46;
        }
        #mensaje.error {
            background:#FEE2E2;
            color:#991B1B;
        }
    </style>
</head>

<body>

<!--
    @section contenido
    Cuadro mostrado al usuario mientras se realiza la verificación.
-->
<div class="box">
    <h2>Verificando tu correo…</h2>
    <div class="loader"></div>
    <p>Un momento por favor.</p>

    <!-- MENSAJE DE RESULTADO (OCULTO AL PRINCIPIO) -->
    <div id="mensaje"></div>
</div>

<script type="module">
    /**
     * @section firebase
     * Inicializa Firebase y aplica el código de verificación (oobCode) recibido
     * desde el servidor PHP.
     */

    import { initializeApp } from "https://www.gstatic.com/firebasejs/11.0.1/firebase-app.js";
    import { getAuth, applyActionCode } from "https://www.gstatic.com/firebasejs/11.0.1/firebase-auth.js";

    /** @brief Configuración básica del proyecto Firebase. */
    const firebaseConfig = {
        apiKey: "AIzaSyBQ8T4ECyaDpybvoL6M6XbmfaipYfFeEXM",
        authDomain: "atmos-e3f6c.firebaseapp.com",
        projectId: "atmos-e3f6c"
    };

    /** Inicializa la app y obtiene el módulo de autenticación. */
    const app = initializeApp(firebaseConfig);
    const auth = getAuth(app);

    /** Código de verificación recibido desde PHP. */
    const oobCode = "<?php echo $oobCode; ?>";

    /**
     * @function verificarCorreo
     * @brief Intenta aplicar el código de verificación a través de Firebase.
     *
     * Proceso:
     *  - Firebase valida el oobCode.
     *  - (Opcional) Se notifica al backend para registrar que el usuario verificó su correo.
     *  - Si es correcto: notifica y redirige a login.php.
     *  - Si falla: informa del error y también redirige.
     *
     * @return void
     */
    async function verificarCorreo() {

        const mensajeDiv = document.getElementById("mensaje");

        try {
            // Aplica el código de verificación en Firebase
            await applyActionCode(auth, oobCode);

            /**
             * Notificación opcional al backend (MySQL u otro sistema)
             * para marcar el correo como verificado en la base de datos.
             */
            fetch("https://nagufor.upv.edu.es/verificarEmailAtmos", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ oobCode })
            });

            // MOSTRAR MENSAJE EN PANTALLA (sin alert)
            mensajeDiv.textContent = "Tu correo ha sido verificado con éxito. Redirigiendo…";
            mensajeDiv.classList.remove("error");
            mensajeDiv.style.display = "block";

            // Espera 2 segundos y redirige
            setTimeout(() => {
                window.location.href = "login.php";
            }, 2000);

        } catch (e) {
            console.error(e);

            // MENSAJE DE ERROR EN PANTALLA
            mensajeDiv.textContent = "El enlace ha expirado o no es válido. Redirigiendo…";
            mensajeDiv.classList.add("error");
            mensajeDiv.style.display = "block";

            setTimeout(() => {
                window.location.href = "login.php";
            }, 2500);
        }
    }

    /** Ejecuta la verificación automáticamente al cargar la página. */
    verificarCorreo();
</script>

</body>
</html>
