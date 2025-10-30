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
</head>
<body>

<main>
    <section class="login-container">

        <!-- Logo -->
        <img src="img/logoAtmos.png" alt="Logo Atmos" class="logo-login">

        <!-- Texto de introducción -->
        <p class="texto-intro">
            Introduce tus credenciales de <strong>inicio de sesión</strong>
        </p>

        <!-- Formulario -->
        <form class="formulario-login" action="verificarLogin.php" method="post">

            <div class="campo">
                <label for="correo">Correo electrónico</label>
                <input type="email" id="correo" name="correo" class="input-base" required>
            </div>

            <div class="campo">
                <label for="contrasena">Contraseña</label>
                <input type="password" id="contrasena" name="contrasena" class="input-base" required>
            </div>

            <a href="#" id="contrasena" class="enlace-secundario">Olvidé mi contraseña</a>

            <button type="submit" class="btn btn-login">Iniciar Sesión</button>

            <p class="enlace-secundario">
                <a href="registro.php">No tengo una cuenta. Registrarme</a>
            </p>
        </form>
    </section>
</main>

</body>
</html>
