<?php
session_start();

// Comprobamos si la sesión contiene el usuario logueado
if (isset($_SESSION['usuario']) && isset($_SESSION['usuario']['id'])) {
    $id_usuario = $_SESSION['usuario']['id'];
    $isGuest = false;
} else {
    $id_usuario = null; // No hay sesión activa
    $isGuest = true;
}

$active = 'vincular';
?>
<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Vincular Dispositivo - Atmos</title>

    <!-- CSS -->
    <link rel="stylesheet" href="css/header.css">
    <link rel="stylesheet" href="css/estilos.css">
    <link rel="stylesheet" href="css/vincular.css">


    <!-- Inyectamos el ID del usuario desde PHP -->
    <script>
        window.ID_USUARIO = <?php echo json_encode($id_usuario); ?>;
        console.log("ID de usuario inyectado desde PHP:", window.ID_USUARIO);
    </script>

    <!-- Script de la app -->
    <script defer src="js/vincular.js"></script>
</head>
<body>

<?php include __DIR__ . '/partials/header.php'; ?>

<main>
    <section class="vincular-container">
        <h2 class="titulo-vincular"><strong>¡Empieza a ver datos!</strong></h2>
        <p class="texto-secundario">Introduce el código del dispositivo que deseas vincular</p>

        <form class="formulario-vincular">
            <div class="campo">
                <label for="codigo">Código del dispositivo</label>
                <input type="text" id="codigo" name="codigo" class="input-base" required>
            </div>

            <button type="submit" class="btn btn-vincular">Vincular</button>
        </form>
    </section>
</main>

</body>
</html>
