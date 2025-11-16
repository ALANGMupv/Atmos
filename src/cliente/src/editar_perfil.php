<?php
/**
 * --------------------------------------------------------------
 *  Fichero: editar_perfil.php
 *  Autor: Alan Guevara Martínez
 *  Descripción: Página de editar perfil del usuario.
 *  Fecha: 16/11/2025
 * --------------------------------------------------------------
 */

session_start();

/*
 * Comprobación de sesión:
 * Este bloque normalmente redirige al usuario si no ha iniciado sesión.
 * Está comentado temporalmente para permitir acceso en local.
 */
/*
if (!isset($_SESSION['usuario'])) {
    header("Location: mapas.php");
    exit;
}
*/

$usuario = $_SESSION['usuario'] ?? [];
$nombre = htmlspecialchars($usuario['nombre'] ?? '');
$apellidos = htmlspecialchars($usuario['apellidos'] ?? '');
$email = htmlspecialchars($usuario['email'] ?? '');
?>

<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>Editar Perfil - ATMOS</title>

    <link rel="stylesheet" href="css/estilos.css">
    <link rel="stylesheet" href="css/header.css">
    <link rel="stylesheet" href="css/editar_perfil.css">

    <script src="js/editar_perfil.js" defer></script>
</head>

<body>

<?php include __DIR__ . '/partials/header.php'; ?>

<main class="ep-main-container">

    <section class="ep-container">

        <!-- TÍTULO -->
        <h2 class="ep-titulo">Editar Perfil</h2>

        <p class="ep-intro">
            Modifica tus datos personales. Tu correo no puede cambiarse por motivos de seguridad.
        </p>

        <!-- FORMULARIO -->
        <form class="formulario-editar" id="form-editar">

            <!-- Nombre -->
            <div class="campo">
                <label for="nombre">Nombre</label>
                <input type="text" id="nombre" name="nombre"
                       value="<?= $nombre ?>" class="input-base">
            </div>

            <!-- Apellidos -->
            <div class="campo">
                <label for="apellidos">Apellidos</label>
                <input type="text" id="apellidos" name="apellidos"
                       value="<?= $apellidos ?>" class="input-base">
            </div>

            <!-- Correo - SOLO EN MODO LECTURA-->
            <div class="campo">
                <label for="correo">Correo electrónico</label>
                <input type="email" id="correo" name="correo"
                       value="<?= $email ?>" class="input-base" readonly>
            </div>

            <!-- Cambiar contraseña -->
            <a href="#" class="ep-enlace-pass" id="btn-cambiar-pass">Cambiar contraseña</a>

            <!-- Botón -->
            <button type="submit" class="ep-btn-guardar">Guardar cambios</button>
        </form>
    </section>

</main>


<!-- POPUP A — Cambiar contraseña -->
<div class="popup-overlay" id="popup-pass">
    <div class="popup-box">
        <h2 class="popup-title">Cambiar contraseña</h2>
        <p class="popup-text">Se cerrará tu sesión y serás redirigid@ para restablecerla.</p>

        <div class="popup-buttons">
            <a href="restContrasenya.php" class="popup-confirm popup-red">Continuar</a>
            <button class="popup-cancel" id="popup-cancel-pass">Cancelar</button>
        </div>
    </div>
</div>


<!-- POPUP B — Confirmar cambios -->
<div class="popup-overlay" id="popup-confirmar">
    <div class="popup-box">
        <h2 class="popup-title">Confirmar cambios</h2>
        <p class="popup-text">Introduce tu contraseña actual para guardar los cambios.</p>

        <div class="campo campo-popup">
            <label for="popup-pass-confirm">Contraseña actual</label>

            <div class="input-password">
                <input type="password" id="popup-pass-confirm" class="input-base popup-input-linea">
                <span class="toggle-pass" data-target="popup-pass-confirm"></span>
            </div>
        </div>

        <div class="popup-buttons">
            <button class="popup-confirm" id="popup-confirmar-btn">Confirmar</button>
            <button class="popup-cancel" id="popup-cancel-confirm">Cancelar</button>
        </div>
    </div>
</div>

</body>
</html>
