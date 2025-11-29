<!--
* --------------------------------------------------------------
*  Fichero: registro.php
*  Autor: Alan Guevara Martínez
*  Descripción: Página de registro del usuario.
*  Fecha: 14/11/2025
* --------------------------------------------------------------
-->


<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Registro - Atmos</title>

    <!-- CSS -->
    <link rel="stylesheet" href="css/estilos.css">
    <link rel="stylesheet" href="css/registro.css">

    <!-- Font -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500&family=Open+Sans:wght@300;400;500&display=swap" rel="stylesheet">

</head>
<body>

<?php include __DIR__ . '/partials/header.php'; ?>

<main>
    <section class="registro-container registro-solo">

        <!-- FORMULARIO (IZQUIERDA) -->
        <div class="registro-formulario">

            <form class="formulario-registro-pag" action="registrarUsuario.php" method="post">

                <h2>Registro</h2>

                <!-- Nombre + Apellidos -->
                <div class="campo-doble">
                    <div class="campo mitad">
                        <label for="nombre">Nombre *</label>
                        <input type="text" id="nombre" name="nombre" class="input-base" required>
                    </div>

                    <div class="campo mitad">
                        <label for="apellidos">Apellidos *</label>
                        <input type="text" id="apellidos" name="apellidos" class="input-base" required>
                    </div>
                </div>

                <!-- Correo -->
                <div class="campo">
                    <label for="correo">Correo electrónico *</label>
                    <input type="email" id="correo" name="correo" class="input-base" required>
                </div>

                <!-- Contraseña -->
                <div class="campo">
                    <div class="fila-label">
                        <label for="contrasena">Contraseña *</label>
                        <span class="info-contrasena">Debe contener al menos 8 caracteres e incluir números o símbolos</span>
                    </div>

                    <div class="input-password">
                        <input type="password" id="contrasena" name="contrasena" class="input-base" required>
                        <span class="toggle-pass" data-target="contrasena"></span>
                    </div>
                </div>

                <!-- Repetir contraseña -->
                <div class="campo">
                    <label for="repetir">Repetir contraseña *</label>

                    <div class="input-password">
                        <input type="password" id="repetir" name="repetir" class="input-base" required>
                        <span class="toggle-pass" data-target="repetir"></span>
                    </div>
                </div>

                <!-- Checkbox política -->
                <div class="checkbox-politica">
                    <input type="checkbox" id="politica" required>

                    <span class="texto-politica">
                        Aceptas nuestros
                        <a href="privacidad.php">términos de servicio y política de privacidad</a>
                    </span>
                </div>

                <!-- Botón -->
                <button type="submit" class="btn btn-registro">Registrarse</button>

                <!-- Enlace login -->
                <p class="texto-secundario">
                    ¿Ya tienes una cuenta?
                    <a href="login.php" class="enlace-secundario" id="go-login">Iniciar sesión</a>
                </p>

            </form>

        </div>

        <!-- IMAGEN DERECHA -->
        <div class="registro-imagen">
            <div class="contenido-imagen">
                <img src="img/logoAtmosFull_Blanco.PNG" alt="Logo Atmos Blanco" class="logo-registro-desk">

                <h2 class="texto-registro-imagen">
                    Empieza tu experiencia ATMOS… desbloquea todo con tu sensor
                </h2>
            </div>
        </div>

    </section>
</main>

<?php include __DIR__ . '/partials/footer.php'; ?>

<script type="module" src="js/registro.js"></script>

</body>
</html>
