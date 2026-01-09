<?php
/**
 * @file medidas.php
 * @brief Vista de consulta de las últimas medidas registradas en el sistema.
 *
 * Esta página muestra una tabla con las medidas ambientales más recientes
 * asociadas a los nodos del sistema. El acceso está restringido a usuarios
 * autenticados; los usuarios sin sesión activa son redirigidos al mapa público.
 *
 * @author —
 * @date 2025
 * @version 1.0
 */

session_start();

/**
 * @brief Verifica si el usuario tiene una sesión activa.
 *
 * Si el usuario no está autenticado, se redirige automáticamente
 * a la página pública de mapas.
 */
if (!isset($_SESSION['usuario'])) {
    header("Location: mapas.php");
    exit;
}

/**
 * @var bool $isGuest
 * @brief Indica que el usuario no es invitado (está logueado).
 */
$isGuest = false;

/**
 * @var string $active
 * @brief Sección activa del menú de navegación.
 */
$active  = 'medidas';

/**
 * @brief Inclusión del header común de la aplicación.
 */
include __DIR__ . '/partials/header.php';
?>

<!doctype html>
<html lang="es">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Proyecto Biometría – Medidas</title>

    <!--
    /**
     * @brief Hojas de estilo principales de la aplicación.
     */
    -->
    <link rel="stylesheet" href="css/index.css" />
    <link rel="stylesheet" href="css/header.css" />

    <!--
    /**
     * @brief Script principal de la aplicación.
     *
     * Gestiona la carga dinámica de datos y la actualización
     * de la tabla de medidas.
     */
    -->
    <script defer src="js/index.js?v=3"></script>
</head>
<body>

<main class="container">

    <!-- ==================================================
         PANEL DE MEDIDAS
    ================================================== -->
    <!--
    /**
     * @section PanelMedidas
     * @brief Panel principal que contiene la tabla de medidas.
     */
    -->
    <section class="panel">

        <!-- Cabecera del panel -->
        <div class="panel-header">
            <h2>Últimas medidas</h2>

            <!--
            /**
             * @brief Controles de visualización de filas.
             */
            -->
            <div class="controls">
                <label for="limit">Filas:</label>
                <select id="limit">
                    <option value="1" selected>Última medida</option>
                    <option value="10">10</option>
                    <option value="50">50</option>
                    <option value="100">100</option>
                    <option value="300">300</option>
                </select>
            </div>
        </div>

        <!-- Contenedor de la tabla -->
        <div class="table-wrap">
            <table class="table" id="tabla-medidas">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Placa</th>
                        <th>Tipo</th>
                        <th>Valor</th>
                        <th>Contador</th>
                        <th>Lat, Long</th>
                        <th>Fecha</th>
                        <th>Hora</th>
                    </tr>
                </thead>

                <!--
                /**
                 * @brief Cuerpo de la tabla de medidas.
                 *
                 * Se rellena dinámicamente mediante JavaScript
                 * a partir de los datos obtenidos del backend.
                 */
                -->
                <tbody id="tbody-medidas">
                    <tr>
                        <td colspan="7" class="muted">Cargando…</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!--
        /**
         * @brief Contenedor de mensajes de error.
         *
         * Se muestra únicamente cuando ocurre un fallo
         * en la carga de datos.
         */
        -->
        <p id="error" class="error" hidden></p>

    </section>
</main>

<!-- ==================================================
     FOOTER
================================================== -->
<!--
/**
 * @brief Pie de página informativo del proyecto.
 */
-->
<footer class="footer">
    <div class="container">
        <small>© 2025 Proyecto Biometría</small>
    </div>
</footer>

</body>
</html>
