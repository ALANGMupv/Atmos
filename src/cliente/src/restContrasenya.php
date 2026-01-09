<!--
/**
 * @file restContrasenya.php
 * @brief Página de restablecimiento de contraseña de la plataforma ATMOS.
 *
 * Esta vista permite a los usuarios solicitar el envío de un correo
 * electrónico para recuperar el acceso a su cuenta mediante Firebase.
 *
 * Flujo general:
 *  - El usuario introduce su correo electrónico
 *  - Se envía la solicitud al script JavaScript correspondiente
 *  - Firebase gestiona el envío del email de recuperación
 *
 * @author —
 * @date 2025
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
    <title>Reestablecer Contraseña - Atmos</title>

    <!--
    /**
     * @brief Hojas de estilo globales y específicas de la vista
     *        de restablecimiento de contraseña.
     */
    -->
    <link rel="stylesheet" href="css/estilos.css">
    <link rel="stylesheet" href="css/restContrasenya.css">
</head>
<body>

<!--
/**
 * @brief Cabecera común de la aplicación (usuarios no autenticados).
 */
-->
<?php
include __DIR__ . '/partials/header.php';
?>

<main>
    <!--
    /**
     * @section Contenedor principal
     * @brief Estructura central de la página de recuperación.
     */
    -->
    <section class="rc-main-container">
        <section class="rc-container">

            <!--
            /**
             * @brief Título principal de la página.
             */
            -->
            <h2 class="texto-titulo">
                Restablecer Contraseña
            </h2>

            <!--
            /**
             * @brief Texto introductorio explicando el proceso de recuperación.
             */
            -->
            <p class="texto-intro">
                Introduce el correo electrónico asociado a tu cuenta y te enviaremos un email de recuperación.
            </p>

            <p class="texto-intro2">
                Si no lo recibes, revisa la carpeta de spam.
            </p>

            <!--
            /**
             * @section Formulario de recuperación
             * @brief Permite al usuario solicitar el envío del correo
             *        de restablecimiento de contraseña.
             */
            -->
            <form class="formulario-restContrasenya" action="" method="post">

                <!--
                /**
                 * @brief Campo de entrada del correo electrónico del usuario.
                 */
                -->
                <label for="correo">Correo electrónico *</label>

                <input
                    type="email"
                    id="correo"
                    name="correo"
                    class="input-base"
                    required
                >

                <!--
                /**
                 * @brief Botón para enviar la solicitud de restablecimiento.
                 */
                -->
                <button type="submit" class="btn btn-login">
                    Restablecer contraseña
                </button>

                <!--
                /**
                 * @brief Enlace de retorno a la página de inicio de sesión.
                 */
                -->
                <p class="enlace-secundario">
                    Volver a
                    <a href="login.php">Iniciar Sesión</a>
                </p>
            </form>

        </section>
    </section>
</main>

<!--
/**
 * @brief Pie de página común de la aplicación.
 */
-->
<?php
include __DIR__ . '/partials/footer.php';
?>

<!--
/**
 * @brief Script encargado de la lógica de recuperación de contraseña.
 *        Gestiona la comunicación con Firebase Authentication.
 */
-->
<script type="module" src="js/restContrasenya.js"></script>

</body>
</html>
