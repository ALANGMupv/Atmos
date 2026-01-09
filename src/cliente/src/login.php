<!doctype html>
<html lang="es">
<head>
  <!--
  /**
   * @file login.php
   * @brief Vista de inicio de sesión del sistema ATMOS.
   *
   * Esta página permite a los usuarios autenticarse en la plataforma ATMOS.
   * Contiene:
   *  - Formulario de inicio de sesión (correo y contraseña).
   *  - Enlace de recuperación de contraseña.
   *  - Enlace al registro de nuevos usuarios.
   *
   * La autenticación real se gestiona en el frontend mediante Firebase
   * (login.js) y se valida posteriormente con el backend.
   *
   * Dependencias:
   *  - partials/header.php
   *  - partials/footer.php
   *  - js/login.js
   *  - css/estilos.css
   *  - css/login.css
   *
   * @author Equipo ATMOS
   * @version 1.0
   */
  -->

  <meta charset="UTF-8">
  <meta name="viewport"
        content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <meta http-equiv="X-UA-Compatible" content="ie=edge">
  <title>Iniciar Sesión - Atmos</title>

  <!--
  /**
   * @section Estilos
   * @brief Hojas de estilo globales y específicas del login.
   */
  -->
  <link rel="stylesheet" href="css/estilos.css">
  <link rel="stylesheet" href="css/login.css">

  <!--
  /**
   * @section Tipografía
   * @brief Fuente Roboto utilizada en la vista de login.
   */
  -->
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Roboto:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">

</head>
<body>

<?php
/**
 * @brief Inclusión del header público.
 *
 * Cabecera común para usuarios no autenticados.
 */
include __DIR__ . '/partials/header.php';
?>

<main>

    <!--
    /**
     * @section Contenedor principal de login
     * @brief Layout dividido en imagen lateral y formulario.
     */
    -->
    <section class="login-container login-solo">

        <!--
        /**
         * @section Panel visual izquierdo
         * @brief Muestra el logo y mensaje motivacional.
         */
        -->
        <div class="login-imagen">

            <div class="contenido-imagen">
                <img src="img/logoAtmosFull_Blanco.PNG"
                     alt="Logo Atmos Blanco"
                     class="logo-login-desk">

                <h2 class="texto-login-imagen">
                    Solo un paso más… inicia sesión y empieza.
                </h2>
            </div>
        </div>

        <!--
        /**
         * @section Formulario de autenticación
         * @brief Formulario de inicio de sesión del usuario.
         *
         * El envío del formulario es interceptado por login.js,
         * donde se realiza:
         *  - Autenticación con Firebase
         *  - Validación con backend
         *  - Creación de sesión PHP
         */
        -->
        <div class="formulario-login">

            <form class="formulario-login-pagLogin">
                <h2>Iniciar sesión</h2>

                <!-- Campo correo -->
                <div class="campo">
                    <label for="correo">Correo *</label>
                    <input type="email" id="correo" class="input-base">
                </div>

                <!-- Campo contraseña -->
                <div class="campo">
                    <label for="contrasena">Contraseña *</label>
                    <input type="password" id="contrasena" class="input-base">
                </div>

                <!-- Acciones finales -->
                <div class="grupo-final">

                    <a href="restContrasenya.php"
                       class="enlace-secundario">
                        ¿Olvidaste tu contraseña?
                    </a>

                    <button type="submit" class="btn">
                        Iniciar sesión
                    </button>

                    <p class="texto-secundario">
                        ¿No tienes una cuenta?
                        <a href="registro.php"
                           class="enlace-secundario"
                           id="go-register">
                            Regístrate
                        </a>
                    </p>

                </div>
            </form>
        </div>
    </section>
</main>

<?php
/**
 * @brief Inclusión del footer común.
 */
include __DIR__ . '/partials/footer.php';
?>

<!--
/**
 * @section Scripts
 * @brief Script de autenticación con Firebase.
 *
 * login.js gestiona:
 *  - Autenticación con Firebase
 *  - Obtención del ID Token
 *  - Validación con backend
 *  - Creación de sesión
 */
-->
<script type="module" src="js/login.js"></script>

</body>
</html>
