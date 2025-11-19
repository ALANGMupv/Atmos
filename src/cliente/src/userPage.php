<?php

session_start();

// -------------------------------
// 1. Comprobar si hay sesión activa
// -------------------------------
if (!isset($_SESSION['usuario'])) {
    header("Location: login.php");
    exit();
}

// 2. Guardar datos del usuario
$usuario = $_SESSION['usuario'];

?>

<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">

    <title>Home Page - Atmos</title>

    <!-- CSS -->
    <link rel="stylesheet" href="css/estilos.css">
    <link rel="stylesheet" href="css/userPage.css">

    <!-- Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500;700&display=swap" rel="stylesheet">
</head>
<body>

<?php
include __DIR__ . '/partials/headerLogueado.php';
?>

<main>

    <section class="home-container">

        <!--Sección de saludo al usuario -->
        <section class="saludo-container">
            <h2>¡Hola, <?= htmlspecialchars($usuario['nombre']); ?>!</h2>
            <p>así se ve el aire que respiras hoy</p>
        </section>

        <!--Sección de Paneles informativos -->
        <section class="panel-container">
            <div class="left-container">

                <!--Sección de Mapas -->
                <div class="mapa-container">
                    <h3>Mapa</h3>
                    <div class="mapa">
                        <div class="mapa-indice-container">
                            <h4>Índice de Calidad</h4>
                            <div class="leyendas_indices-container">
                                <div class="fila-indice"><span class="color-buena"></span> <span class="texto-indice">Buena</span> <span class="porcentaje-buena">0%</span></div>
                                <div class="fila-indice"><span class="color-moderada"></span> <span class="texto-indice">Moderada</span> <span class="porcentaje-moderada">0%</span></div>
                                <div class="fila-indice"><span class="color-insalubre"></span> <span class="texto-indice">Insalubre</span> <span class="porcentaje-insalubre">0%</span></div>
                                <div class="fila-indice"><span class="color-mala"></span> <span class="texto-indice">Mala</span> <span class="porcentaje-mala">0%</span></div>
                            </div>
                        </div>
                    </div>
                    <button>Ver Mapa</button>
                </div>

                <!--Sección de Menú de acciones -->
                <div class="menu-container">
                    <h3>Menú de acciones</h3>
                    <div class="botones-menu-container">
                        <button class="menu-btn"><img src="img/manualUsuarioBoton.svg" alt=""></button>
                        <button class="menu-btn"><img src="img/incidenciasMenuBoton.svg" alt=""></button>
                        <button class="desvincular-btn"><img src="img/desvincularMenuBoton.svg" alt=""></button>
                    </div>
                </div>
            </div>

            <div class="right-container">

                <!--Sección de panel de Mi Sensor -->
                <div class="miSensor-container">
                    <div class="miSensor-titulo-container">
                        <h3>Mi Sensor</h3>
                        <button data-popup="popupMiSensor" class="informacion-icono"><img src="img/informacionIcon.svg" alt=""></button>
                    </div>

                    <div class="selector-gases-container">
                            <select id="gasSelector">
                                <option value="" disabled selected hidden>Selecciona un gas contaminante</option>
                                <option value="NO₂">NO₂</option>
                                <option value="CO">CO</option>
                                <option value="O₃">O₃</option>
                                <option value="SO₂">SO₂</option>
                            </select>
                    </div>

                    <div class="info-miSensor-mobile">

                        <!--Sección de contenedor de distancia y estado del sensor -->
                        <div class="distancia_estado-container">
                            <div class="estado-container">
                                <div class="estado-titulo-container"><h4>Estado del Sensor</h4></div>
                                <img class="estado-icono" src="img/estadoActivoSensorIcono.svg" alt="">
                                <p>Sensor activo</p>
                            </div>

                            <div class="distancia-container">
                                <div class="distancia-titulo-container"><h4>Distancia al sensor</h4></div>
                                <img class="distancia-icono" src="img/distanciaIcono.svg" alt="">
                                <p>Señal alta</p>
                            </div>
                        </div>

                        <!--Sección de contenedor de última medicion del sensor y promedio de contaminación del día -->
                        <div class="medicion_promedio-container">

                            <div class="medicion-container">
                                <div class="medicion-titulo-container">
                                    <div class="titulo-medicion">
                                        <h4>Última medición</h4>
                                    </div>
                                    <p>9:41 - 08/09</p>
                                </div>
                                <div class="medicion-medio-container">
                                    <p class="medicion-medicion">0.02</p>
                                    <div class="detalles-medicion">
                                        <p class="tipo-medicion" id="gasUltima">NO2</p>
                                        <p class="unidad-medicion">ppm</p>
                                    </div>
                                </div>
                                <div class="categoria-medicion-container">
                                    <span class="color-medicion"></span> <span class="texto-medicion">Buena</span>
                                </div>
                            </div>

                            <div class="promedio-container">
                                <div class="promedio-titulo-container">
                                    <div class="titulo-promedio">
                                        <h4>Promedio del día</h4>
                                    </div>
                                    <p>08/09/2025</p>
                                </div>
                                <div class="promedio-medio-container">
                                    <p class="medicion-promedio">0.10</p>
                                    <div class="detalles-medicion-promedio">
                                        <p class="tipo-promedio" id="gasPromedio">NO2</p>
                                        <p class="unidad-promedio">ppm</p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!--Sección de contenedor de gráfica de calidad del aire -->
                    <div class="grafica-container"></div>
                </div>
            </div>
        </section>

        <!--POPUP DE INFORMACIÓN DE MI SENSOR -->
        <section id="popupMiSensor" class="popup-info-container">
            <div class="popup-container">
                <button class="cerrar-popup">
                    <img  src="img/cerrarIcono.svg" alt="">
                </button>
                <div class="top-containers">
                    <div class="estado-info-container">
                        <h2>Estado del Sensor</h2>
                        <img src="img/estadoSensorInfoPopup.svg" alt="">
                        <p>Muestra el estado actual del nodo sensor. Este puede estar conectado y <b>activo</b> o desconectado e <b>inactivo</b>.</p>
                    </div>
                    <div class="distancia-info-container">
                        <h2>Distancia del Sensor</h2>
                        <img src="img/distanciaSensorInfoPopup.svg" alt="">
                        <p>Indica la potencia de la señal de tu sensor. Una señal <b>alta</b> significa que el sensor está cerca de tu móvil o bien conectado; una señal <b>baja</b> puede indicar más distancia o interferencias.</p>
                    </div>
                </div>

                <div class="bottom-containers">
                    <div class="medicion-info-container">
                        <h2>Última medición</h2>
                        <img src="img/medicionInfoPopup.svg" alt="">
                        <p>Es la medición más reciente de NO₂ captada por tu sensor. Este valor se actualiza periódicamente y permite saber en tiempo real cómo está la calidad del aire justo ahora.</p>
                    </div>
                    <div class="promedio-info-container">
                        <h2>Promedio del día</h2>
                        <img src="img/promedioInfoPopup.svg" alt="">
                        <p>Es el valor medio de NO₂ registrado por el sensor a lo largo del día. Ayuda a entender cómo ha sido la calidad del aire durante la jornada, más allá de picos puntuales.</p>
                    </div>
                </div>
            </div>
        </section>

        <!--POPUP DE INFORMACIÓN GRÁFICA DE CALIDAD DEL AIRE -->

    </section>
</main>

<!-- Carga del script de User Page -->
<script type="module" src="js/userPage.js"></script>

</body>
</html>
