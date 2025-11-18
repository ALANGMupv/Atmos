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

        <section class="saludo-container">
            <h2>¡Hola, <?= htmlspecialchars($usuario['nombre']); ?>!</h2>
            <p>así se ve el aire que respiras hoy</p>
        </section>

        <section class="panel-container">
            <div class="left-container">
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
                <div class="miSensor-container">
                    <div class="miSensor-titulo-container">
                        <h3>Mi Sensor</h3>
                        <button class="informacion-icono"><img src="img/informacionIcon.svg" alt=""></button>
                    </div>

                    <div class="info-miSensor-mobile">
                        <div class="distancia_estado-container">
                            <div class="distancia-container">
                                <div class="distancia-titulo-container"><h4>Distancia al sensor</h4></div>
                                <img class="distancia-icono" src="img/distanciaIcono.svg" alt="">
                                <p>Señal alta</p>
                            </div>

                            <div class="estado-container">
                                <div class="estado-titulo-container"><h4>Estado del aire</h4></div>
                                <img class="estado-icono" src="img/estadoAireIcono.svg" alt="">
                                <p>Calidad regular</p>
                            </div>
                        </div>

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
                                        <p class="tipo-medicion">NO2</p>
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
                                        <p class="tipo-promedio">NO2</p>
                                        <p class="unidad-promedio">ppm</p>
                                    </div>
                                </div>
                                <div class="categoria-promedio-container">
                                    <span class="color-promedio"></span> <span class="texto-promedio">Insalubre</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="grafica-container"></div>
                </div>
            </div>
        </section>

    </section>
</main>

</body>
</html>
