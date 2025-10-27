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
</head>
<body>

<main>
    <section class="registro-container">

        <!-- Icono X (cerrar) -->
        <img src="icons/cerrar.svg" alt="Cerrar" class="icono-cerrar">

        <!-- Logo -->
        <img src="img/logoAtmos.png" alt="Logo Atmos" class="logo-registro">

        <!-- Texto de introducción -->
        <p class="texto-intro">
            Introduce tus datos personales para crear una cuenta
        </p>

        <!-- Formulario -->
        <form class="formulario-registro" action="registrarUsuario.php" method="post">

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

            <div class="campo">
                <label for="correo">Correo electrónico</label>
                <input type="email" id="correo" name="correo" class="input-base" required>
            </div>

            <div class="campo">
                <label for="contrasena">Contraseña</label>
                <input type="password" id="contrasena" name="contrasena" class="input-base" required>
            </div>

            <div class="campo">
                <label for="repetir">Repetir contraseña</label>
                <input type="password" id="repetir" name="repetir" class="input-base" required>
            </div>

            <button type="submit" class="btn btn-registrar">Registrarme</button>

            <p class="enlace-login">
                <a href="login.php">Ya tengo una cuenta. Iniciar Sesión</a>
            </p>
        </form>
    </section>
</main>

</body>
</html>
