<!--
==========================================================
Nombre del archivo: editar_perfil.php
Descripción: Página de edición de perfil de usuario en la aplicación Atmos.
Permite modificar los datos personales como nombre, apellidos, correo
y contraseña, mostrando los campos en un formulario editable.
Fecha: 30/10/2025
Autor: Nerea Aguilar Forés
==========================================================
-->

<!doctype html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport"
        content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <meta http-equiv="X-UA-Compatible" content="ie=edge">
  <title>Editar Perfil - Atmos</title>

  <!-- CSS principal -->
  <link rel="stylesheet" href="css/estilos.css">

  <!-- Estilos específicos para la página de edición de perfil -->
  <link rel="stylesheet" href="css/editar_perfil.css">
</head>
<body>

<main>
  <section class="editar-container">

    <!-- Icono de cierre (volver o salir del perfil) -->
    <a href="index.php">
      <img src="icons/cerrar.svg" alt="Cerrar" class="icono-cerrar">
    </a>

    <!-- Icono del usuario -->
    <img src="img/usuario.png" alt="Usuario" class="icono-usuario">

    <!-- Título principal de la página -->
    <h1 class="titulo-editar">Editar Perfil</h1>

    <!-- Formulario para editar los datos personales del usuario -->
    <form class="formulario-editar">

      <!-- Campo de nombre -->
      <div class="campo">
        <label for="nombre">Nombre</label>
        <input type="text" id="nombre" name="nombre" class="input-base">
      </div>

      <!-- Campo de apellidos -->
      <div class="campo">
        <label for="apellidos">Apellido/s</label>
        <input type="text" id="apellidos" name="apellidos" class="input-base">
      </div>

      <!-- Campo de correo electrónico -->
      <div class="campo">
        <label for="correo">Correo electrónico</label>
        <input type="email" id="correo" name="correo" class="input-base">
      </div>

      <!-- Campo de contraseña -->
      <div class="campo">
        <label for="contrasena">Contraseña</label>
        <input type="password" id="contrasena" name="contrasena" class="input-base">
      </div>

      <!-- Botón para guardar los cambios realizados -->
      <button type="submit" class="btn">Guardar Cambios</button>
    </form>

  </section>
</main>

</body>
</html>
