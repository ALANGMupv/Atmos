<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Reestablecer Contraseña - Atmos</title>

    <!-- CSS -->
    <link rel="stylesheet" href="css/estilos.css">
    <link rel="stylesheet" href="css/restContrasenya.css">
</head>
<body>

<?php
include __DIR__ . '/partials/header.php';
?>

<main>
    <section class="rc-main-container">
        <section class="rc-container">

            <h2 class="texto-titulo">
                Restablecer Contraseña
            </h2>

            <!-- Texto de introducción -->
            <p class="texto-intro">
                Introduce el correo electrónico asociado a tu cuenta y te enviaremos un email de recuperación.
            </p>

            <p class="texto-intro2">
                Si no lo recibes, revisa la carpeta de spam.
            </p>

            <!-- Formulario -->
            <form class="formulario-restContrasenya" action="" method="post">

                <label for="correo">Correo electrónico *</label>

                <input type="email" id="correo" name="correo" class="input-base" required>

                <button type="submit" class="btn btn-login">Restablecer contraseña</button>

                <p class="enlace-secundario">
                    Volver a
                    <a href="login.php">Iniciar Sesión</a>
                </p>
            </form>

        </section>

    </section>
</main>

<?php
include __DIR__ . '/partials/footer.php';
?>

<script type="module" src="js/restContrasenya.js"></script>


</body>
</html>
