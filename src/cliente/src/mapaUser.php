<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">

    <title>Mapa de Calidad de Aire - Atmos</title>

    <!-- Leaflet CSS -->
    <link
            rel="stylesheet"
            href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
            integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY="
            crossorigin=""
    />


    <!-- CSS -->
    <link rel="stylesheet" href="css/estilos.css">
    <link rel="stylesheet" href="css/mapaUser.css">

    <!-- Fuente -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500;700&display=swap" rel="stylesheet">
</head>

<body>

<?php
$active = 'mapas';
include __DIR__ . '/partials/headerLogueado.php'; ?>

<main>

    <section class="main-container">
        <!-- Titulo -->
        <section class="titulo-container">
            <h2>Mapa de calidad del aire</h2>
        </section>

        <section class="mapa-main-container">

            <!-- Contenedor del Mapa -->
            <div id="map" class="mapa"></div>

            <!-- Barra de búsqueda -->
            <div class="search-bar">
                <span class="search-icon"> <img src="img/busquedaIcono.svg" alt=""></span>
                <input
                        type="text"
                        id="search-input"
                        placeholder="Buscar ubicación..."
                        autocomplete="off"
                >
            </div>

            <!-- Contenedor de sugerencias (se muestra dinámicamente con JS) -->
            <div id="search-suggestions" class="search-suggestions"></div>


            <!-- Selector de Gases -->
            <div class="contaminantes-panel">

                <div class="contaminantes-header">
                    <h3>Contaminantes</h3>
                    <button class="info-btn-contaminantes">
                        <img src="img/informacionIcon.svg" alt="">
                    </button>
                </div>

                <div class="contaminantes-selector">

                    <div class="contaminante-option active" data-gas="ALL" data-tipo="ALL" id="TodosOpcionSelector">
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

            <!-- Indice de Contaminantes -->

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



            <!-- Botones de Zoom -->
            <div class="map-zoom-controls">
                <button id="zoom-in"><img src="img/acercarIcono.svg" alt=""></button>
                <button id="zoom-out"><img src="img/alejarIcono.svg" alt=""></button>
            </div>

            <!-- Botón de Centrar Mi ubicacion -->
            <button id="btn-geoloc" class="map-location-btn">
                <img src="img/ubicacionIcono.svg" alt="">
            </button>

            <!-- Cotenedr del Timline -->
            <div class="timeline-box">

                <!-- Controles -->
                <div class="timeline-controls">
                    <button id="tl-back"><img src="img/atrasarIcono.svg" alt=""></button>
                    <button id="tl-pause"><img src="img/pauseIcono.svg" alt=""></button>
                    <button id="tl-play"><img src="img/playIcon.svg" alt=""></button>
                    <button id="tl-forward"><img src="img/avanzarIcono.svg" alt=""></button>
                </div>

                <!-- Slider -->
                <input
                        type="range"
                        id="timeline-slider"
                        min="0"
                        max="12"
                        value="12"
                >

                <!-- Eje dinámico -->
                <div class="timeline-labels" id="timeline-labels">
                    <!-- Se rellena dinámicamente con JS -->
                    <!-- Ejemplo visual:
                    <span>00:00</span>
                    <span>2</span>
                    <span>4</span>
                    <span>6</span>
                    <span>8</span>
                    <span>10</span>
                    <span>Ahora</span>
                    -->
                </div>
            </div>


        </section>
    </section>

</main>

</main>

<!-- Leaflet JS -->
<script
        src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
        integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo="
        crossorigin=""
></script>

<script src="https://unpkg.com/leaflet.heat/dist/leaflet-heat.js"></script>

<!-- Turf.js (para interpolaciones si las necesitas) -->
<script src="https://unpkg.com/@turf/turf@6/turf.min.js"></script>

<!-- Lógica de estaciones oficiales (define cargarEstacionesOficiales) -->
<script src="js/estacionesOficiales.js"></script>

<!-- Tu script del mapa -->
<script src="js/mapaUser.js"></script>

</body>
</html>


</body>
</html>
