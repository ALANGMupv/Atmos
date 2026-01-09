<!doctype html>
<html lang="es">
<head>
    <!--
    /**
     * @file mapaUser.php
     * @brief Vista del mapa de calidad del aire para usuarios autenticados.
     *
     * Esta página muestra el mapa completo de calidad del aire con todas
     * las funcionalidades habilitadas:
     *  - Selección individual de contaminantes (NO₂, CO, O₃, SO₂).
     *  - Índice de calidad del aire dinámico.
     *  - Interpolación IDW en canvas.
     *  - Timeline temporal de medidas.
     *  - Geolocalización.
     *  - Estaciones oficiales superpuestas.
     *
     * Diferencias respecto a la versión pública:
     *  - No hay restricciones de acceso.
     *  - El índice de calidad se calcula y actualiza.
     *  - Timeline funcional.
     *
     * Tecnologías utilizadas:
     *  - Leaflet.js
     *  - CanvasOverlay personalizado
     *  - Turf.js
     *  - OpenStreetMap
     *
     * Scripts asociados:
     *  - js/mapaUser.js
     *  - js/estacionesOficiales.js
     *
     * @author Equipo ATMOS
     * @version 1.0
     */
    -->

    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">

    <title>Mapa de Calidad de Aire - Atmos</title>

    <!--
    /**
     * @section Estilos Leaflet
     * @brief Hojas de estilo necesarias para Leaflet.
     */
    -->
    <link
            rel="stylesheet"
            href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
            integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY="
            crossorigin=""
    />

    <!--
    /**
     * @section Estilos propios
     * @brief Estilos globales y específicos del mapa para usuarios logueados.
     */
    -->
    <link rel="stylesheet" href="css/estilos.css">
    <link rel="stylesheet" href="css/mapaUser.css">
    <link rel="stylesheet" href="css/popupContaminantes.css">

    <!--
    /**
     * @section Tipografía
     * @brief Fuente principal de la interfaz.
     */
    -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500;700&display=swap" rel="stylesheet">
</head>

<body>

<?php
/**
 * @brief Inclusión del header para usuarios autenticados.
 *
 * Marca la sección "mapas" como activa.
 */
$active = 'mapas';
include __DIR__ . '/partials/headerLogueado.php'; ?>

<main>

    <!--
    /**
     * @section Contenedor principal del mapa
     * @brief Estructura general de la vista.
     */
    -->
    <section class="main-container">

        <!-- Título -->
        <section class="titulo-container">
            <h2>Mapa de calidad del aire</h2>
        </section>

        <section class="mapa-main-container">

            <!--
            /**
             * @section Mapa Leaflet
             * @brief Contenedor del mapa interactivo.
             */
            -->
            <div id="map" class="mapa"></div>

            <!--
            /**
             * @section Barra de búsqueda
             * @brief Autocompletado geográfico con Nominatim.
             */
            -->
            <div class="search-bar">
                <span class="search-icon">
                    <img src="img/busquedaIcono.svg" alt="">
                </span>
                <input
                        type="text"
                        id="search-input"
                        placeholder="Buscar ubicación..."
                        autocomplete="off"
                >
            </div>

            <!-- Contenedor dinámico de sugerencias -->
            <div id="search-suggestions" class="search-suggestions"></div>

            <!--
            /**
             * @section Selector de contaminantes
             * @brief Permite seleccionar gas específico o vista global.
             */
            -->
            <div class="contaminantes-panel">

                <div class="contaminantes-header">
                    <h3>Contaminantes</h3>
                    <button class="info-btn-contaminantes" data-popup="popupContaminantes">
                        <img src="img/informacionIcon.svg" alt="">
                    </button>
                </div>

                <div class="contaminantes-selector">

                    <div class="contaminante-option active"
                         data-gas="ALL"
                         data-tipo="ALL"
                         id="TodosOpcionSelector">
                        <img src="img/TODOSIcono.svg" alt="Todos">
                        <span>Todos</span>
                    </div>

                    <div class="contaminante-option" data-tipo="11">
                        <img src="img/NO2icono.svg" alt="NO2">
                        <span>NO₂</span>
                    </div>

                    <div class="contaminante-option" data-tipo="12">
                        <img src="img/COicono.svg" alt="CO">
                        <span>CO</span>
                    </div>

                    <div class="contaminante-option" data-tipo="13">
                        <img src="img/O3icono.svg" alt="O3">
                        <span>O₃</span>
                    </div>

                    <div class="contaminante-option" data-tipo="14">
                        <img src="img/SO2icono.svg" alt="SO2">
                        <span>SO₂</span>
                    </div>

                </div>

            </div>

            <!--
            /**
             * @section Índice de calidad del aire
             * @brief Muestra el porcentaje de celdas por nivel.
             */
            -->
            <div class="mapa-indice-container">
                <h4>Índice de Calidad</h4>

                <div class="leyendas_indices-container">

                    <div class="fila-indice">
                        <span class="color-buena"></span>
                        <span class="texto-indice">Buena</span>
                        <span class="porcentaje-buena">0%</span>
                    </div>

                    <div class="fila-indice">
                        <span class="color-moderada"></span>
                        <span class="texto-indice">Moderada</span>
                        <span class="porcentaje-moderada">0%</span>
                    </div>

                    <div class="fila-indice">
                        <span class="color-insalubre"></span>
                        <span class="texto-indice">Insalubre</span>
                        <span class="porcentaje-insalubre">0%</span>
                    </div>

                    <div class="fila-indice">
                        <span class="color-mala"></span>
                        <span class="texto-indice">Mala</span>
                        <span class="porcentaje-mala">0%</span>
                    </div>

                </div>
            </div>

            <!-- Controles de zoom -->
            <div class="map-zoom-controls">
                <button id="zoom-in"><img src="img/acercarIcono.svg" alt=""></button>
                <button id="zoom-out"><img src="img/alejarIcono.svg" alt=""></button>
            </div>

            <!-- Botón geolocalización -->
            <button id="btn-geoloc" class="map-location-btn">
                <img src="img/ubicacionIcono.svg" alt="">
            </button>

            <!--
            /**
             * @section Timeline temporal
             * @brief Control de navegación histórica de medidas.
             */
            -->
            <div class="timeline-box">

                <div class="timeline-controls">
                    <button id="tl-back"><img src="img/atrasarIcono.svg" alt=""></button>
                    <button id="tl-pause"><img src="img/pauseIcono.svg" alt=""></button>
                    <button id="tl-play"><img src="img/playIcon.svg" alt=""></button>
                    <button id="tl-forward"><img src="img/avanzarIcono.svg" alt=""></button>
                </div>

                <input
                        type="range"
                        id="timeline-slider"
                        min="0"
                        max="12"
                        value="12"
                >

                <div class="timeline-labels" id="timeline-labels"></div>
            </div>

        </section>
    </section>

</main>

<!--
/**
 * @section Librerías externas
 * @brief Dependencias necesarias para el mapa.
 */
-->
<script
        src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
        integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo="
        crossorigin=""
></script>

<script src="https://unpkg.com/leaflet.heat/dist/leaflet-heat.js"></script>
<script src="https://unpkg.com/@turf/turf@6/turf.min.js"></script>

<!-- Lógica de estaciones oficiales -->
<script src="js/estacionesOficiales.js"></script>

<!-- Script principal del mapa -->
<script src="js/mapaUser.js"></script>

<?php
/**
 * @brief Popup informativo de contaminantes.
 */
include __DIR__ . '/popupContaminantes.php'; ?>

</body>
</html>
