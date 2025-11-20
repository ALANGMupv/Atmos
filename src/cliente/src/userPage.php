<?php

session_start();

// Comprobar sesión activa
if (!isset($_SESSION['usuario'])) {
    header("Location: login.php");
    exit();
}

// Datos del usuario en sesión
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

    <!-- Fuente -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500;700&display=swap" rel="stylesheet">
</head>

<!-- data-id-usuario permite al JS saber quién está logueado -->
<body data-id-usuario="<?= htmlspecialchars($usuario['id_usuario']); ?>">

<?php include __DIR__ . '/partials/headerLogueado.php'; ?>

<main>

    <section class="home-container">

        <!-- Saludo-->
        <section class="saludo-container">
            <h2>¡Hola, <?= htmlspecialchars($usuario['nombre']); ?>!</h2>
            <p>así se ve el aire que respiras hoy</p>
        </section>

        <!-- Panel general -->
        <section class="panel-container">
            <div class="left-container">

                <!-- Mapa -->
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

                <!-- Menú de acciones -->
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

                <!-- Mi Sensor -->
                <div class="miSensor-container">
                    <div class="miSensor-titulo-container">
                        <h3>Mi Sensor</h3>

                        <!-- Botón de info del popup -->
                        <button data-popup="popupMiSensor" class="informacion-icono">
                            <img src="img/informacionIcon.svg" alt="">
                        </button>
                    </div>

                    <!-- Selector de gas -->
                    <div class="selector-gases-container">
                        <p>Selecciona un gas contaminante:</p>
                        <select id="gasSelector">
                            <option value="NO₂" selected >NO₂</option>
                            <option value="CO">CO</option>
                            <option value="O₃">O₃</option>
                            <option value="SO₂">SO₂</option>
                        </select>
                    </div>

                    <div class="info-miSensor-mobile">

                        <!-- Estado y distancia -->
                        <div class="distancia_estado-container">
                            <div class="estado-container">
                                <div class="estado-titulo-container"><h4>Estado del Sensor</h4></div>
                                <img class="estado-icono" id="estadoSensorIcono" src="img/estadoActivoSensorIcono.svg" alt="">
                                <p id="estadoSensorTexto">Sensor activo</p>
                            </div>

                            <div class="distancia-container">
                                <div class="distancia-titulo-container"><h4>Distancia al sensor</h4></div>
                                <img class="distancia-icono" id="iconoSenal" src="img/sinSeñalDistanciaIcono.svg" alt="">
                                <p id="textoSenal">Señal alta</p>
                            </div>
                        </div>

                        <!-- Última medición + promedio -->
                        <div class="medicion_promedio-container">

                            <!-- Última medición -->
                            <div class="medicion-container">
                                <div class="medicion-titulo-container">
                                    <h4>Última medición</h4>
                                    <p class="hora-ultima" id="fechaUltima">--:--</p>
                                </div>

                                <!-- Valor que se actualizará -->
                                <div class="medicion-medio-container">
                                    <p class="medicion-medicion">--</p>

                                    <div class="detalles-medicion">
                                        <p class="tipo-medicion" id="gasUltima">--</p>
                                        <p class="unidad-medicion">ppm</p>
                                    </div>
                                </div>

                                <div class="categoria-medicion-container">
                                    <span class="color-medicion"></span>
                                    <span class="texto-medicion">--</span>
                                </div>
                            </div>

                            <!-- Promedio -->
                            <div class="promedio-container">
                                <div class="promedio-titulo-container">
                                    <h4>Promedio del día</h4>
                                    <p class="fecha-promedio" id="fechaPromedio">--/--/----</p>
                                </div>

                                <!-- Valor que se actualizará -->
                                <div class="promedio-medio-container">
                                    <p class="medicion-promedio">--</p>

                                    <div class="detalles-medicion-promedio">
                                        <p class="tipo-promedio" id="gasPromedio">--</p>
                                        <p class="unidad-promedio">ppm</p>
                                    </div>
                                </div>

                                <div class="categoria-promedio-container">
                                    <span class="color-promedio"></span>
                                    <span class="texto-promedio">--</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Gráfica -->
                    <div class="grafica-container">
                        <div class="top-container">
                            <div class="titulo-grafica-container">
                                <div class="titulo-grafica">
                                    <h4>Gráfica de Calidad del Aire</h4>
                                    <!-- Botón de info del popup -->
                                    <button data-popup="popupGrafica" class="informacion-icono">
                                        <img src="img/informacionIcon.svg" alt="">
                                    </button>
                                </div>
                                <p id="graficaRangoTexto">--/-- al --/--</p>
                            </div>
                            <div class="selector-carita-container">
                                <div class="selector-grafica-container">
                                    <div class="selector-modo-grafica">
                                        <button class="selector-opcion activo" data-modo="D">D</button>
                                        <button class="selector-opcion" data-modo="H">H</button>
                                    </div>
                                </div>
                                <div class="carita-grafica-container">

                                    <div class="carita-grafica">
                                        <img src="img/estadoAireIcono.svg" alt="">
                                    </div>
                                </div>
                            </div>

                        </div>
                        <div class="bottom-container">
                            <canvas id="graficaCalidad"></canvas>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- POPUP de info -->
        <section id="popupMiSensor" class="popup-info-container">
            <div class="popup-container">
                <button class="cerrar-popup"><img src="img/cerrarIcono.svg" alt=""></button>

                <div class="top-containers">
                    <div class="estado-info-container">
                        <h2>Estado del Sensor</h2>
                        <img src="img/estadoSensorInfoPopup.svg" alt="">
                        <p>Muestra el estado actual del nodo sensor. Este puede estar conectado y activo o desconectado e inactivo.</p>
                    </div>

                    <div class="distancia-info-container">
                        <h2>Distancia del Sensor</h2>
                        <img src="img/distanciaSensorInfoPopup.svg" alt="">
                        <p>Indica la intensidad de la señal entre tu móvil y el sensor Atmos midiendo la potencia de la señal.  Una señal alta significa que el sensor está cerca o bien conectado; una señal baja puede indicar más distancia o interferencias.</p>
                    </div>
                </div>

                <div class="bottom-containers">
                    <div class="medicion-info-container">
                        <h2>Última medición</h2>
                        <img src="img/medicionInfoPopup.svg" alt="">
                        <p>Es la medición más reciente captada por tu sensor. Este valor se actualiza periódicamente y permite saber en tiempo real cómo está la calidad del aire justo ahora.</p>
                    </div>

                    <div class="promedio-info-container">
                        <h2>Promedio del día</h2>
                        <img src="img/promedioInfoPopup.svg" alt="">
                        <p>Es el valor medio de las mediciones registradas por el sensor a lo largo del día. Ayuda a entender cómo ha sido la calidad del aire durante la jornada, más allá de picos puntuales.</p>
                    </div>
                </div>
            </div>
        </section>

    </section>
</main>

<script>
    window.ID_USUARIO = <?= json_encode($usuario['id_usuario']); ?>;
</script>

<!-- Script de la página -->
<script type="module" src="js/userPage.js"></script>

<!-- Script para la gráfica de calidad de aire -->
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>



</body>
</html>
