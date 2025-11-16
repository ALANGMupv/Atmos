<?php
/**
 * --------------------------------------------------------------
 *  Fichero: perfil.php
 *  Autor: Alan Guevara Martínez
 *  Descripción: Página de perfil del usuario. Muestra los datos
 *               almacenados en sesión y ofrece enlaces para
 *               editar el perfil o cerrar sesión.
 *  Fecha: 16/11/2025
 * --------------------------------------------------------------
 */

session_start();

// Para poder mostrar el hover del header.
$active = 'perfil';

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

// Recuperación segura de los datos del usuario almacenados en sesión.
// Se utiliza el operador ?? para evitar errores si no existen claves,
// y htmlspecialchars() para prevenir inyecciones XSS.
$usuario   = $_SESSION['usuario'] ?? [];
$nombre    = htmlspecialchars($usuario['nombre'] ?? 'Nombre');
$apellidos = htmlspecialchars($usuario['apellidos'] ?? 'de Usuario');
$email     = htmlspecialchars($usuario['email'] ?? 'Correo electrónico de Usuario');
?>
<!doctype html>
<html lang="es">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Mi Perfil – ATMOS</title>

    <!-- Hojas de estilo -->
    <link rel="stylesheet" href="css/index.css" />
    <link rel="stylesheet" href="css/header.css" />
    <link rel="stylesheet" href="css/perfil.css" />

    <!-- JS -->
    <script type="module" src="js/perfil.js"></script>

</head>
<body>

<?php
include __DIR__ . '/partials/headerLogueado.php';
?>

<main class="perfil-wrapper"> <!-- Contenedor principal de la página de perfil -->

    <!-- COLUMNA IZQUIERDA: Título + imagen ilustrativa -->
    <section class="perfil-left">
        <div class="left-image-container">
            <!-- Título principal de la página -->
            <h1 class="perfil-titulo">Mi Perfil</h1>

            <!-- Imagen ilustrativa del perfil -->
            <img src="img/imgPerfil.png" class="perfil-ilustracion" alt="Imagen Perfil">
        </div>
    </section>

    <!-- Línea vertical separadora entre izquierda y derecha -->
    <div class="perfil-divider"></div>

    <!-- COLUMNA DERECHA: Información del usuario + botones -->
    <section class="perfil-right">

        <!-- Avatar o ícono representativo del usuario -->
        <img src="img/icnPagPerfil.svg" class="perfil-avatar" alt="Avatar">

        <!-- Bloque que muestra los datos del usuario -->
        <div class="perfil-info">
            <h3>Usuario</h3>
            <!-- Nombre completo obtenido desde PHP -->
            <p class="info-value"><?php echo $nombre . " " . $apellidos; ?></p>

            <h3>Correo electrónico</h3>
            <!-- Correo del usuario -->
            <p class="info-value"><?php echo $email; ?></p>
        </div>

        <!-- BOTONES -->
        <div class="perfil-btns">
            <a href="editar_perfil.php" class="btn-edit">Editar Perfil</a>

            <!-- Botón que abre el popup -->
            <a href="#" class="btn-logout" id="btn-open-logout">Cerrar sesión</a>
        </div>

    </section>

</main>

<!-- region --- POPUP LOGOUT --- -->
<div class="popup-overlay" id="popup-logout">
    <div class="popup-box">
        <h2 class="popup-title">¿Estás segur@ que quieres cerrar sesión?</h2>

        <div class="popup-buttons">
            <a href="logout.php" class="popup-confirm">Cerrar sesión</a>
            <button class="popup-cancel" id="popup-cancel">Cancelar</button>
        </div>
    </div>
</div>
<!-- endregion -->

</body>
</html>
