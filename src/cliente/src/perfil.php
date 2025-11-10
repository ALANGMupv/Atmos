<?php
session_start();

// Solo usuarios logueados pueden acceder
if (!isset($_SESSION['usuario'])) {
    header("Location: mapas.php");
    exit;
}

// Datos del usuario desde la sesión
$usuario   = $_SESSION['usuario'] ?? [];
$nombre    = htmlspecialchars($usuario['nombre'] ?? '—');
$apellidos = htmlspecialchars($usuario['apellidos'] ?? '');
$email     = htmlspecialchars($usuario['email']  ?? '—');

$isGuest = false;   // ya sabemos que está logueado
$active  = null;    // ninguna pestaña activa
?>
<!doctype html>
<html lang="es">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Mi Perfil – ATMOS</title>

    <!-- Estilos -->
    <link rel="stylesheet" href="css/index.css" />
    <link rel="stylesheet" href="css/header.css" />
    <link rel="stylesheet" href="css/perfil.css" />
</head>
<body>

<?php include __DIR__ . '/partials/header.php'; ?>

<main class="container">
    <section class="perfil-card">
        <div class="perfil-top">
            <img src="img/usuario.png" alt="Avatar del usuario" class="perfil-avatar" />
            <div class="perfil-identidad">
                <h1 class="perfil-nombre"><?php echo trim($nombre . ' ' . $apellidos); ?></h1>
                <p class="perfil-email"><?php echo $email; ?></p>
            </div>
        </div>

        <div class="perfil-info">
            <div class="info-row">
                <span class="info-label">Nombre</span>
                <span class="info-value"><?php echo trim($nombre . ' ' . $apellidos); ?></span>
            </div>

            <div class="info-row">
                <span class="info-label">Correo</span>
                <span class="info-value"><?php echo $email; ?></span>
            </div>

            <!-- Si quieres mostrar más campos guardados en la sesión, agrégalos aquí -->
            <!--
      <div class="info-row">
        <span class="info-label">ID de usuario</span>
        <span class="info-value"><?php // echo htmlspecialchars($usuario['id'] ?? '—'); ?></span>
      </div>
      -->
        </div>

        <div class="perfil-actions">
            <a class="btn btn-primario" href="editar_perfil.php">Editar perfil</a>
            <a class="btn btn-secundario" href="logout.php">Cerrar sesión</a>
        </div>
    </section>
</main>

</body>
</html>
