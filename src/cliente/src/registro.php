<!--
==========================================================
Nombre del archivo: registro.php
Descripción: Página de registro de usuario para la aplicación Atmos.
Permite crear una nueva cuenta ingresando datos personales como nombre,
apellidos, correo y contraseña. Incluye un botón de cierre (X) que
redirige a la landing page.
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
    <title>Registro - Atmos</title>

    <!-- CSS principal del proyecto -->
    <link rel="stylesheet" href="css/estilos.css">

    <!-- Estilos específicos de la página de registro -->
    <link rel="stylesheet" href="css/registro.css">
</head>
<body>

<main>
    <section class="registro-container">

        <!-- Icono X (cerrar): redirige a la página principal (landing) -->
        <a href="landing.php">
            <img src="icons/cerrar.svg" alt="Cerrar" class="icono-cerrar">
        </a>

        <!-- Logo principal de la aplicación -->
        <img src="img/logoAtmos.png" alt="Logo Atmos" class="logo-registro">

        <!-- Texto introductorio del formulario -->
        <p class="texto-intro">
            Introduce tus datos personales para crear una cuenta
        </p>

        <!-- Formulario de registro -->
        <!-- Envia los datos al backend (registrarUsuario.php) mediante POST -->
        <form class="formulario-registro" action="registrarUsuario.php" method="post">

            <!-- Campos de nombre y apellidos (distribuidos en dos columnas) -->
            <div class="campo-doble">
                <div class="mitad">
                    <div class="campo">
                        <label for="nombre">Nombre</label>
                        <input type="text" id="nombre" name="nombre" class="input-base" required>
                    </div>
                </div>

                <div class="mitad">
                    <div class="campo">
                        <label for="apellidos">Apellido/s</label>
                        <input type="text" id="apellidos" name="apellidos" class="input-base" required>
                    </div>
                </div>
            </div>

            <!-- Campo de correo electrónico -->
            <div class="campo">
                <label for="correo">Correo electrónico</label>
                <input type="email" id="correo" name="correo" class="input-base" required>
            </div>

            <!-- Campo de contraseña -->
            <div class="campo">
                <label for="contrasena">Contraseña</label>
                <input type="password" id="contrasena" name="contrasena" class="input-base" required>
            </div>

            <!-- Campo para repetir contraseña -->
            <div class="campo">
                <label for="repetir">Repetir contraseña</label>
                <input type="password" id="repetir" name="repetir" class="input-base" required>
            </div>

            <!-- Botón de envío del formulario -->
            <button type="submit" class="btn btn-registrar">Registrarme</button>

            <!-- Enlace alternativo para usuarios ya registrados -->
            <p class="enlace-login">
                <a href="login.php">Ya tengo una cuenta. Iniciar Sesión</a>
            </p>
        </form>
    </section>
</main>

<!-- Script JS asociado a la lógica del registro -->
<script type="module" src="js/registro.js"></script>

</body>
</html>
