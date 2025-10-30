<!--
==========================================================
Nombre del archivo: vincular.php
Descripción: Página para vincular un nuevo dispositivo a la cuenta del usuario
en la aplicación Atmos. Permite introducir el código del dispositivo y enviarlo
al servidor mediante un formulario.
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
    <title>Vincular Dispositivo - Atmos</title>

    <!-- CSS -->
    <link rel="stylesheet" href="css/estilos.css">
    <link rel="stylesheet" href="css/vincular.css">
</head>
<body>

<main>
    <section class="vincular-container">

        <!-- Icono X (cerrar): al hacer clic redirige a la landing page -->
        <a href="index.php">
            <img src="icons/cerrar.svg" alt="Cerrar" class="icono-cerrar">
        </a>

        <!-- Título principal de la página -->
        <h2 class="titulo-vincular">Vincular Dispositivo</h2>

        <!-- Subtexto con instrucciones -->
        <p class="texto-secundario">
            Introduce el código del dispositivo y un nombre identificativo
        </p>

        <!-- Formulario para introducir el código del dispositivo -->
        <!-- Envia los datos al archivo PHP vincularDispositivo.php mediante POST -->
        <form class="formulario-vincular" action="vincularDispositivo.php" method="post">
            <div class="campo">
                <label for="codigo">Código del dispositivo</label>
                <input type="text" id="codigo" name="codigo" class="input-base" required>
            </div>

            <!-- Botón que envía el formulario -->
            <button type="submit" class="btn btn-vincular">Vincular</button>
        </form>
    </section>
</main>

</body>
</html>
