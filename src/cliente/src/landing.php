<!doctype html>
<html lang="es">
<head>
    <!--
    /**
     * @file landing.php
     * @brief Landing page inicial de acceso al sistema ATMOS.
     *
     * Esta página sirve como pantalla de entrada previa al login,
     * ofreciendo al usuario tres opciones:
     *  - Iniciar sesión.
     *  - Registrarse como nuevo usuario.
     *  - Acceder como invitado (redirige a index.php).
     *
     * Incluye:
     *  - Identidad visual del proyecto (logo).
     *  - Mensaje principal de valor.
     *  - Llamadas a la acción (CTA).
     *
     * Dependencias:
     *  - partials/header.php
     *  - css/landing.css
     *  - Google Fonts (Poppins)
     *
     * @author Equipo ATMOS
     * @version 1.0
     */
    -->

    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Landing Page - Atmos</title>

    <!--
    /**
     * @section Fuentes tipográficas
     * @brief Fuente principal utilizada en la landing.
     */
    -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link
        href="https://fonts.googleapis.com/css2?family=Poppins:ital,wght@0,100;0,200;0,300;0,400;0,500;0,600;0,700;0,800;0,900;1,100;1,200;1,300;1,400;1,500;1,600;1,700;1,800;1,900&display=swap"
        rel="stylesheet">

    <!--
    /**
     * @section Estilos
     * @brief Hoja de estilos específica para la landing page.
     */
    -->
    <link rel="stylesheet" href="css/landing.css">
</head>

<body>

<?php
/**
 * @brief Inclusión del header público.
 *
 * Muestra la cabecera común para usuarios no autenticados.
 */
include __DIR__ . '/partials/header.php';
?>

<main>

    <!--
    /**
     * @section Contenedor principal
     * @brief Estructura principal de la landing.
     *
     * Contiene:
     *  - Identidad visual (logo).
     *  - Mensaje principal del proyecto.
     *  - Botones de acceso y registro.
     */
    -->
    <section class="landing-container">

        <div class="Container-info">

            <!--
            /**
             * @brief Logo principal del proyecto ATMOS.
             */
            -->
            <img src="img/LogoAtmosFull.png" alt="Logo Atmos" class="logo">

            <!--
            /**
             * @brief Texto introductorio del proyecto.
             */
            -->
            <h1 class="texto-intro">
                Visualiza el aire que respiras <br> en todo momento
            </h1>
        </div>

        <!--
        /**
         * @section Acciones de usuario
         * @brief Botones de acceso al sistema.
         *
         * Permite:
         *  - Login de usuario existente.
         *  - Registro de nuevo usuario.
         *  - Acceso como invitado (modo lectura).
         */
        -->
        <div class="Container-ctas">
            <button class="btn-login" onclick="window.location.href='login.php'">
                INICIAR SESÓN
            </button>

            <button class="btn-registro" onclick="window.location.href='registro.php'">
                REGISTRARME
            </button>

            <a href="index.php" class="acceso-invitado">
                Acceder como invitado
            </a>
        </div>

    </section>
</main>

</body>
</html>
