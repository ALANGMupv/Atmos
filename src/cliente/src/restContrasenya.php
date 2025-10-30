<!--
==========================================================
Nombre del archivo: restContrasenya.php
Descripción: Página de restablecimiento de contraseña para la aplicación Atmos.
Permite al usuario ingresar el correo electrónico asociado a su cuenta
para recibir un enlace o instrucciones de recuperación.
Fecha: 30/10/2025
Autor: Alejandro Vázquez Remes
==========================================================
-->

<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Reestablecer Contraseña - Atmos</title>

    <!-- CSS principal del proyecto -->
    <link rel="stylesheet" href="css/estilos.css">

    <!-- Estilos específicos para login y reestablecer -->
    <link rel="stylesheet" href="css/login.css">
</head>
<body>

<main>
    <section class="login-container">

        <!-- Icono X (cerrar): permite volver a la página de login -->
        <a href="login.php">
            <img src="icons/cerrar.svg" alt="Cerrar" class="icono-cerrar">
        </a>

        <!-- Logo principal de la aplicación -->
        <img src="img/logoAtmos.png" alt="Logo Atmos" class="logo-login">

        <!-- Texto de introducción -->
        <p class="texto-intro">
            Introduce el correo electrónico asociado a tu cuenta para recuperar tu contraseña
        </p>

        <!-- Formulario de restablecimiento de contraseña -->
        <!-- En el backend se procesará el correo y se enviará el enlace de recuperación -->
        <form class="formulario-reestContrasenya" action="restContrasenya.php" method="post">

            <div class="campo">
                <label for="correo">Correo electrónico</label>
                <input type="email" id="correo" name="correo" class="input-base" required>
            </div>

            <!-- Botón para enviar la solicitud de restablecimiento -->
            <button type="submit" class="btn btn-login">Reestablecer</button>

            <!-- Enlace para acceder a la página de registro -->
            <p class="enlace-secundario">
                <a href="registro.php">No tengo una cuenta. Registrarme</a>
            </p>
        </form>
    </section>
</main>

</body>
</html>
