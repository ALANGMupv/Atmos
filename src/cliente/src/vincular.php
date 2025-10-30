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

        <!-- Icono X (cerrar) -->
        <img src="icons/cerrar.svg" alt="Cerrar" class="icono-cerrar">

        <!-- Título -->
        <h2 class="titulo-vincular">Vincular Dispositivo</h2>

        <!-- Subtexto -->
        <p class="texto-secundario">
            Introduce el código del dispositivo y un nombre identificativo
        </p>

        <!-- Formulario -->
        <form class="formulario-vincular" action="vincularDispositivo.php" method="post">
            <div class="campo">
                <label for="codigo">Código del dispositivo</label>
                <input type="text" id="codigo" name="codigo" class="input-base" required>
            </div>

            <div class="campo">
                <label for="nombre">Nombre</label>
                <input type="text" id="nombre" name="nombre" class="input-base" required>
            </div>

            <button type="submit" class="btn btn-vincular">Vincular</button>
        </form>
    </section>
</main>

</body>
</html>
