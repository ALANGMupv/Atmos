<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">

    <title>Mapa de Calidad de Aire - Atmos</title>

    <!-- Leaflet CSS -->
    <link rel="stylesheet"
          href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
          crossorigin=""
    />

    <link rel="stylesheet" href="css/estilos.css">
    <link rel="stylesheet" href="css/mapaUserNoLogueado.css">

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500;700&display=swap" rel="stylesheet">
</head>

<body>

<?php
$active = 'mapas';
include __DIR__ . '/partials/header.php';
?>

<main>

    <section class="main-container">

        <section class="titulo-container">
            <h2>Mapa de calidad del aire</h2>
        </section>

        <section class="mapa-main-container">

            <!-- Mapa -->
            <div id="map" class="mapa"></div>

            <!-- Barra de búsqueda -->
            <div class="search-bar">
                <span class="search-icon"><img src="img/busquedaIcono.svg" alt=""></span>
                <input type="text" id="search-input" placeholder="Buscar ubicación..." autocomplete="off">
            </div>

            <div id="search-suggestions" class="search-suggestions"></div>


            <!-- Panel contaminantes (modo no logueado → bloqueado) -->
            <div class="contaminantes-panel">

                <div class="contaminantes-header">
                    <h3>Contaminantes</h3>
                    <button class="info-btn-contaminantes">
                        <img src="img/informacionIcon.svg" alt="">
                    </button>
                </div>

                <div class="contaminantes-selector">

                    <!-- SOLO ESTO ESTÁ ACTIVO -->
                    <div class="contaminante-option active" data-tipo="ALL" id="TodosOpcionSelector">
                        <img src="img/TODOSIcono.svg" alt="Todos">
                        <span>Todos</span>
                    </div>

                    <!-- LOS DEMÁS ESTÁN BLOQUEADOS -->
                    <div class="contaminante-option disabled" data-tipo="11">
                        <img src="img/NO2icono.svg" alt="NO2">
                        <span>NO₂</span>
                    </div>

                    <div class="contaminante-option disabled" data-tipo="12">
                        <img src="img/COicono.svg" alt="CO">
                        <span>CO</span>
                    </div>

                    <div class="contaminante-option disabled" data-tipo="13">
                        <img src="img/O3icono.svg" alt="O3">
                        <span>O₃</span>
                    </div>

                    <div class="contaminante-option disabled" data-tipo="14">
                        <img src="img/SO2icono.svg" alt="SO2">
                        <span>SO₂</span>
                    </div>

                </div>
            </div>


            <!-- Índice de Calidad → versión limitada -->
            <div class="mapa-indice-container">
                <h4>Índice de Calidad</h4>

                <p style="text-align:center; font-size:12px; color:#666; margin-top:-5px; margin-bottom:5px;">
                    Disponible solo para usuarios registrados
                </p>

                <div class="leyendas_indices-container">

                    <div class="fila-indice">
                        <span class="color-buena"></span>
                        <span class="texto-indice">Buena</span>
                        <span class="porcentaje-buena">–</span>
                    </div>

                    <div class="fila-indice">
                        <span class="color-moderada"></span>
                        <span class="texto-indice">Moderada</span>
                        <span class="porcentaje-moderada">–</span>
                    </div>

                    <div class="fila-indice">
                        <span class="color-insalubre"></span>
                        <span class="texto-indice">Insalubre</span>
                        <span class="porcentaje-insalubre">–</span>
                    </div>

                    <div class="fila-indice">
                        <span class="color-mala"></span>
                        <span class="texto-indice">Mala</span>
                        <span class="porcentaje-mala">–</span>
                    </div>

                </div>
            </div>


            <!-- Zoom -->
            <div class="map-zoom-controls">
                <button id="zoom-in"><img src="img/acercarIcono.svg" alt=""></button>
                <button id="zoom-out"><img src="img/alejarIcono.svg" alt=""></button>
            </div>

            <!-- Mi ubicación -->
            <button id="btn-geoloc" class="map-location-btn">
                <img src="img/ubicacionIcono.svg" alt="">
            </button>

            <!-- Timeline (opcional bloquear) -->
            <div class="timeline-box">
                <div class="timeline-controls">
                    <button id="tl-back"><img src="img/atrasarIcono.svg" alt=""></button>
                    <button id="tl-pause"><img src="img/pauseIcono.svg" alt=""></button>
                    <button id="tl-play"><img src="img/playIcon.svg" alt=""></button>
                    <button id="tl-forward"><img src="img/avanzarIcono.svg" alt=""></button>
                </div>

                <input type="range" id="timeline-slider" min="0" max="12" value="12">

                <div class="timeline-labels" id="timeline-labels"></div>
            </div>

        </section>
    </section>

</main>

<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
<script src="https://unpkg.com/leaflet.heat/dist/leaflet-heat.js"></script>
<script src="https://unpkg.com/@turf/turf@6/turf.min.js"></script>

<script src="js/mapaUserNoLogueado.js" defer></script>

</body>
</html>
