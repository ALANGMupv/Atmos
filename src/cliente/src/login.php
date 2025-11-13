<!doctype html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport"
        content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <meta http-equiv="X-UA-Compatible" content="ie=edge">
  <title>Iniciar Sesión - Atmos</title>

    <!-- CSS -->
    <link rel="stylesheet" href="css/estilos.css">
    <link rel="stylesheet" href="css/login.css">

    <!-- Font -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Roboto:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">

</head>
<body>
<?php
include __DIR__ . '/partials/header.php';
?>

<main>
    <section class="login-container login-solo">

        <div class="login-imagen">

            <div class="contenido-imagen">
                <img src="img/logoAtmosFull_Blanco.PNG" alt="Logo Atmos Blanco" class="logo-login-desk">

                <h2 class="texto-login-imagen">Solo un paso más… inicia sesión y empieza.</h2>
            </div>
        </div>

        <!-- Parte derecha login - FORMULARIO-->
        <div class="formulario-login">

            <form class="formulario-login-pagLogin">
                <h2>Iniciar sesión</h2>

                <div class="campo">
                    <label for="correo">Correo *</label>
                    <input type="email" id="correo" class="input-base">
                </div>

                <div class="campo">
                    <label for="contrasena">Contraseña *</label>
                    <input type="password" id="contrasena" class="input-base">
                </div>

                <div class="grupo-final">

                    <a href="restContrasenya.php" class="enlace-secundario">¿Olvidaste tu contraseña?</a>

                    <button type="submit" class="btn">Iniciar sesión</button>

                    <p class="texto-secundario">
                        ¿No tienes una cuenta?
                        <a href="registro.php" class="enlace-secundario">Regístrate</a>
                    </p>

                </div>
            </form>
        </div>
    </section>
</main>

<?php
include __DIR__ . '/partials/footer.php';
?>

<!-- ✅ Carga del script de login -->
<script type="module" src="js/login.js"></script>

</body>
</html>
