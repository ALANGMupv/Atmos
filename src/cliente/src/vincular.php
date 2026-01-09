<?php
/**
 * @file vincular.php
 * @brief Página para vincular un dispositivo físico a un usuario.
 *
 * Esta vista permite asociar un dispositivo al usuario autenticado
 * mediante un código único. Si no existe sesión activa, el usuario
 * se considera invitado y no se inyecta ningún ID de usuario.
 *
 * El identificador del usuario se expone al frontend mediante
 * una variable global JavaScript.
 *
 * @author —
 * @date 2025
 * @version 1.0
 */

session_start();

/**
 * @brief Comprobación del estado de la sesión.
 *
 * Si existe un usuario logueado y contiene un identificador,
 * se recupera su ID. En caso contrario, se marca como invitado.
 */
if (isset($_SESSION['usuario']) && isset($_SESSION['usuario']['id'])) {

    /**
     * @var int $id_usuario
     * @brief Identificador del usuario autenticado.
     */
    $id_usuario = $_SESSION['usuario']['id'];

    /**
     * @var bool $isGuest
     * @brief Indica que el usuario NO es invitado.
     */
    $isGuest = false;

} else {

    /**
     * @var null $id_usuario
     * @brief No hay sesión activa, no existe identificador de usuario.
     */
    $id_usuario = null;

    /**
     * @var bool $isGuest
     * @brief Indica que el usuario es invitado.
     */
    $isGuest = true;
}

/**
 * @var string $active
 * @brief Sección activa del menú de navegación.
 */
$active = 'vincular';
?>

<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Vincular Dispositivo - Atmos</title>

    <!--
    /**
     * @brief Hojas de estilo necesarias para la vista de vinculación.
     */
    -->
    <link rel="stylesheet" href="css/header.css">
    <link rel="stylesheet" href="css/estilos.css">
    <link rel="stylesheet" href="css/vincular.css">

    <!--
    /**
     * @brief Inyección del ID del usuario desde PHP a JavaScript.
     *
     * Se utiliza para asociar el dispositivo al usuario autenticado
     * desde el frontend.
     */
    -->
    <script>
        window.ID_USUARIO = <?php echo json_encode($id_usuario); ?>;
        console.log("ID de usuario inyectado desde PHP:", window.ID_USUARIO);
    </script>

    <!--
    /**
     * @brief Script principal de la funcionalidad de vinculación.
     *
     * Gestiona el envío del código del dispositivo y la comunicación
     * con el backend.
     */
    -->
    <script defer src="js/vincular.js"></script>
</head>
<body>

<!--
/**
 * @brief Inclusión del header común de la aplicación.
 */
-->
<?php include __DIR__ . '/partials/header.php'; ?>

<main>

    <!-- ==================================================
         SECCIÓN DE VINCULACIÓN DE DISPOSITIVOS
    ================================================== -->
    <!--
    /**
     * @section VincularDispositivo
     * @brief Formulario para introducir el código del dispositivo.
     */
    -->
    <section class="vincular-container">

        <h2 class="titulo-vincular">Vincular Dispositivo</h2>
        <p class="texto-secundario">
            Introduce el código del dispositivo que deseas vincular
        </p>

        <!-- Formulario de vinculación -->
        <form class="formulario-vincular">

            <div class="campo">
                <label for="codigo">Código del dispositivo</label>
                <input
                    type="text"
                    id="codigo"
                    name="codigo"
                    class="input-base"
                    required
                >
            </div>

            <button type="submit" class="btn btn-vincular">
                Vincular
            </button>

        </form>
    </section>
</main>

</body>
</html>
