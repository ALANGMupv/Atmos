<!doctype html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport"
        content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <meta http-equiv="X-UA-Compatible" content="ie=edge">
  <title>Editar Perfil - Atmos</title>

  <!-- CSS -->
  <link rel="stylesheet" href="css/estilos.css">
  <link rel="stylesheet" href="css/editar_perfil.css">
</head>
<body>

<main>
  <section class="editar-container">

    <!-- Icono de cierre -->
    <img src="icons/cerrar.svg" alt="Cerrar" class="icono-cerrar">

    <!-- Icono de usuario -->
    <img src="img/usuario.png" alt="Usuario" class="icono-usuario">

    <!-- Título -->
    <h1 class="titulo-editar">Editar Perfil</h1>

    <!-- Formulario -->
    <form class="formulario-editar">

  <div class="campo">
    <label for="nombre">Nombre</label>
    <input type="text" id="nombre" name="nombre" class="input-base">
  </div>

  <div class="campo">
    <label for="apellidos">Apellido/s</label>
    <input type="text" id="apellidos" name="apellidos" class="input-base">
  </div>

  <div class="campo">
    <label for="correo">Correo electrónico</label>
    <input type="email" id="correo" name="correo" class="input-base">
  </div>

  <div class="campo">
    <label for="contrasena">Contraseña</label>
    <input type="password" id="contrasena" name="contrasena" class="input-base">
  </div>

  <button type="submit" class="btn">Guardar Cambios</button>
</form>


  </section>
</main>

</body>
</html>
