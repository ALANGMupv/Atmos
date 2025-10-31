<?php
session_start();

// Si el usuario no ha iniciado sesión, redirige a mapas
if (!isset($_SESSION['usuario'])) {
  header("Location: mapas.php");
  exit;
}

$usuario = $_SESSION['usuario'];
$isGuest = false;
$active  = 'perfil'; // activa el icono del perfil
?>
<!doctype html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport"
        content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <meta http-equiv="X-UA-Compatible" content="ie=edge">
  <title>Editar Perfil - ATMOS</title>

  <!-- CSS -->
  <link rel="stylesheet" href="css/estilos.css">
  <link rel="stylesheet" href="css/header.css">
  <link rel="stylesheet" href="css/editar_perfil.css">
</head>
<body>

<?php include __DIR__ . '/partials/header.php'; ?>

<main>
  <section class="editar-container">

    <!-- Título -->
    <h1 class="titulo-editar">Editar perfil</h1>

    <!-- Formulario -->
    <form class="formulario-editar" method="post" action="actualizarPerfil.php">

      <div class="campo">
        <label for="nombre">Nombre</label>
        <input type="text" id="nombre" name="nombre" class="input-base"
               value="<?php echo htmlspecialchars($usuario['nombre']); ?>" required>
      </div>

      <div class="campo">
        <label for="apellidos">Apellido/s</label>
        <input type="text" id="apellidos" name="apellidos" class="input-base"
               value="<?php echo htmlspecialchars($usuario['apellidos'] ?? ''); ?>">
      </div>

      <div class="campo">
        <label for="correo">Correo electrónico</label>
        <input type="email" id="correo" name="correo" class="input-base"
               value="<?php echo htmlspecialchars($usuario['email']); ?>" readonly>
      </div>

      <div class="campo">
        <label for="contrasena">Contraseña</label>
        <input type="password" id="contrasena" name="contrasena" class="input-base" placeholder="Opcional">
      </div>

      <button type="submit" class="btn">Guardar cambios</button>
    </form>

  </section>
</main>

<!-- Popup visual -->
<div id="popup" style="display:none; position:fixed; top:20px; right:20px;
  background:#333; color:#fff; padding:15px 20px; border-radius:10px;
  font-family:sans-serif; box-shadow:0 0 10px rgba(0,0,0,0.3);
  z-index:10000; opacity:0; transition:opacity 0.3s;">
</div>

<script type="module" src="js/editar_perfil.js?v=1"></script>
</body>
</html>
