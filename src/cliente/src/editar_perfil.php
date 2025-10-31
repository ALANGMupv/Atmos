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

<!-- Popup de verificación -->
<div id="popup-overlay" style="
  display:none; position:fixed; top:0; left:0; width:100%; height:100%;
  background:rgba(0,0,0,0.5); z-index:10000; justify-content:center; align-items:center;">
  <div id="popup" style="
    background:white; padding:25px 30px; border-radius:12px; width:300px;
    text-align:center; box-shadow:0 0 20px rgba(0,0,0,0.3); font-family:sans-serif;">
    <h3 style="margin-bottom:15px;">Confirmar cambios</h3>
    <p style="margin-bottom:10px;">Introduce tu contraseña actual para guardar los cambios.</p>
    <input type="password" id="popup-pass" placeholder="Contraseña" style="
      width:100%; padding:10px; margin-bottom:15px; border:1px solid #ccc;
      border-radius:6px; font-size:14px;">
    <div>
      <button id="popup-confirm" style="
        background:#0f2940; color:white; border:none; padding:8px 14px;
        border-radius:6px; cursor:pointer; margin-right:8px;">
        Confirmar
      </button>
      <button id="popup-cancel" style="
        background:#ccc; color:#333; border:none; padding:8px 14px;
        border-radius:6px; cursor:pointer;">
        Cancelar
      </button>
    </div>
  </div>
</div>

<script>
  // Variable global accesible desde editar_perfil.js
  const id_usuario = <?php echo json_encode($usuario['id'] ?? null); ?>;
  console.log("ID de usuario cargado desde PHP:", id_usuario);
</script>

<script type="module" src="js/editar_perfil.js?v=1"></script>
</body>
</html>
