<!--
==========================================================
Nombre del archivo: login.php
Descripción: Página de inicio de sesión de la aplicación Atmos.
Permite a los usuarios autenticarse ingresando su correo y contraseña.
Incluye enlace para recuperación de contraseña y registro de nuevos usuarios.
Fecha: 30/10/2025
Autor: Alan Guevara Martínez
==========================================================
-->

<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Iniciar Sesión - Atmos</title>

    <!-- CSS principal -->
    <link rel="stylesheet" href="css/estilos.css">
    <link rel="stylesheet" href="css/login.css">
</head>
<body>

<main>
    <section class="login-container">

        <!-- Icono X (cerrar): permite volver a la landing page -->
        <a href="landing.php">
            <img src="icons/cerrar.svg" alt="Cerrar" class="icono-cerrar">
        </a>

        <!-- Logo de la aplicación -->
        <img src="img/logoAtmos.png" alt="Logo Atmos" class="logo-login">

        <!-- Texto introductorio -->
        <p class="texto-intro">
            Introduce tus credenciales de inicio de sesión
        </p>

        <!-- Formulario de inicio de sesión -->
        <!-- Envía los datos al backend (verificarLogin.php) mediante POST -->
        <form class="formulario-login" action="verificarLogin.php" method="post">

            <!-- Campo para el correo electrónico -->
            <div class="campo">
                <label for="correo">Correo electrónico</label>
                <input type="email" id="correo" name="correo" class="input-base" required>
            </div>

            <!-- Campo para la contraseña -->
            <div class="campo">
                <label for="contrasena">Contraseña</label>
                <input type="password" id="contrasena" name="contrasena" class="input-base" required>
            </div>

            <!-- Enlace para restablecer la contraseña -->
            <a href="restContrasenya.php" class="enlace-secundario">Olvidé mi contraseña</a>

            <!-- Botón para enviar el formulario -->
            <button type="submit" class="btn btn-login">Iniciar Sesión</button>

            <!-- Enlace para acceder a la página de registro -->
            <p class="enlace-secundario">
                <a href="registro.php">No tengo una cuenta. Registrarme</a>
            </p>
        </form>
    </section>
</main>

</body>
</html>
