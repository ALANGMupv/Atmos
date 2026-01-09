<!--
/**
 * @file registro.php
 * @brief Página de registro de usuarios de la plataforma ATMOS.
 *
 * Esta vista permite a un usuario crear una cuenta proporcionando
 * sus datos personales básicos y aceptando la política de privacidad.
 *
 * Funcionalidades principales:
 *  - Introducción de nombre, apellidos, correo y contraseña
 *  - Validación básica mediante atributos HTML
 *  - Envío del formulario al backend PHP
 *  - Enlace a política de privacidad y login
 *
 * @author Alan Guevara Martínez
 * @date 14/11/2025
 * @version 1.0
 */
-->
<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Registro - Atmos</title>

    <!--
    /**
     * @brief Hojas de estilo globales y específicas de la vista de registro.
     */
    -->
    <link rel="stylesheet" href="css/estilos.css">
    <link rel="stylesheet" href="css/registro.css">

    <!--
    /**
     * @brief Fuentes utilizadas en la interfaz de registro.
     */
    -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500&family=Open+Sans:wght@300;400;500&display=swap" rel="stylesheet">
</head>
<body>

<!--
/**
 * @brief Cabecera común para usuarios no autenticados.
 */
-->
<?php include __DIR__ . '/partials/header.php'; ?>

<main>
    <section class="registro-container registro-solo">

        <!--
        /**
         * @section Formulario de registro
         * @brief Formulario principal para crear una cuenta de usuario.
         */
        -->
        <div class="registro-formulario">

            <form class="formulario-registro-pag" action="registrarUsuario.php" method="post">

                <h2>Registro</h2>

                <!--
                /**
                 * @subsection Datos personales
                 * @brief Campos de nombre y apellidos del usuario.
                 */
                -->
                <div class="campo-doble">
                    <div class="campo mitad">
                        <label for="nombre">Nombre *</label>
                        <input type="text" id="nombre" name="nombre" class="input-base" required>
                    </div>

                    <div class="campo mitad">
                        <label for="apellidos">Apellidos *</label>
                        <input type="text" id="apellidos" name="apellidos" class="input-base" required>
                    </div>
                </div>

                <!--
                /**
                 * @brief Campo de correo electrónico del usuario.
                 */
                -->
                <div class="campo">
                    <label for="correo">Correo electrónico *</label>
                    <input type="email" id="correo" name="correo" class="input-base" required>
                </div>

                <!--
                /**
                 * @brief Campo de contraseña con ayuda visual y requisitos mínimos.
                 */
                -->
                <div class="campo">
                    <div class="fila-label">
                        <label for="contrasena">Contraseña *</label>
                        <span class="info-contrasena">
                            Debe contener al menos 8 caracteres e incluir números o símbolos
                        </span>
                    </div>

                    <div class="input-password">
                        <input type="password" id="contrasena" name="contrasena" class="input-base" required>
                        <span class="toggle-pass" data-target="contrasena"></span>
                    </div>
                </div>

                <!--
                /**
                 * @brief Campo para repetir la contraseña y confirmar coincidencia.
                 */
                -->
                <div class="campo">
                    <label for="repetir">Repetir contraseña *</label>

                    <div class="input-password">
                        <input type="password" id="repetir" name="repetir" class="input-base" required>
                        <span class="toggle-pass" data-target="repetir"></span>
                    </div>
                </div>

                <!--
                /**
                 * @brief Aceptación de términos y política de privacidad.
                 */
                -->
                <div class="checkbox-politica">
                    <input type="checkbox" id="politica" required>

                    <span class="texto-politica">
                        Aceptas nuestros
                        <a href="privacidad.php">términos de servicio y política de privacidad</a>
                    </span>
                </div>

                <!--
                /**
                 * @brief Botón de envío del formulario de registro.
                 */
                -->
                <button type="submit" class="btn btn-registro">Registrarse</button>

                <!--
                /**
                 * @brief Enlace a la página de inicio de sesión.
                 */
                -->
                <p class="texto-secundario">
                    ¿Ya tienes una cuenta?
                    <a href="login.php" class="enlace-secundario" id="go-login">Iniciar sesión</a>
                </p>

            </form>
        </div>

        <!--
        /**
         * @section Panel visual
         * @brief Imagen y mensaje motivacional lateral para escritorio.
         */
        -->
        <div class="registro-imagen">
            <div class="contenido-imagen">
                <img src="img/logoAtmosFull_Blanco.PNG"
                     alt="Logo Atmos Blanco"
                     class="logo-registro-desk">

                <h2 class="texto-registro-imagen">
                    Empieza tu experiencia ATMOS… desbloquea todo con tu sensor
                </h2>
            </div>
        </div>

    </section>
</main>

<!--
/**
 * @brief Pie de página común de la aplicación.
 */
-->
<?php include __DIR__ . '/partials/footer.php'; ?>

<!--
/**
 * @brief Script de lógica del formulario de registro.
 * Maneja validaciones de cliente y comportamientos interactivos.
 */
-->
<script type="module" src="js/registro.js"></script>

</body>
</html>
