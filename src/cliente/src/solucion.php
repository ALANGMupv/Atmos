<?php
session_start();

$active = 'solucion';
$isGuest = !isset($_SESSION['usuario']);

if ($isGuest) {
    include __DIR__ . '/partials/header.php';
} else {
    include __DIR__ . '/partials/headerLogueado.php';
}
?>

<!doctype html>
<html lang="es">

<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>ATMOS - Nuestra Solución</title>

    <!-- Fuentes -->
    <link
        href="https://fonts.googleapis.com/css2?family=Open+Sans:wght@300;400;600&family=Roboto:wght@500;700&display=swap"
        rel="stylesheet">

    <!-- Iconos -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

    <!-- Estilos -->
    <link rel="stylesheet" href="css/index.css?v=1.0.6">
    <link rel="stylesheet" href="css/header.css">
    <link rel="stylesheet" href="css/solucion.css?v=1.0.0">
</head>

<body>

    <main>

        <!-- ====== HERO ====== -->
        <section class="solucion-hero">
            <div class="wrap">
                <h1 class="solucion-title">Cómo funciona Atmos</h1>
                <p class="solucion-subtitle">
                    Una plataforma colaborativa que transforma datos ciudadanos en información valiosa sobre la calidad
                    del aire que respiramos a través de nuestro sensor de ozono
                </p>

            </div>
        </section>

        <!-- ====== SECCIÓN: EL PROBLEMA ====== -->
        <section id="problema" class="section-problem">
            <div class="wrap problem-grid">

                <!-- Columna Izquierda: Texto -->
                <div class="problem-text">
                    <h2 class="section-title-left">El problema</h2>
                    <p>
                        La contaminación del aire es un problema invisible pero crítico que afecta la salud de millones
                        de personas. Las estaciones de monitoreo tradicionales son costosas, escasas y no proporcionan
                        cobertura detallada.
                    </p>
                    <p>
                        Necesitamos una red densa de sensores para entender realmente la calidad del aire en nuestras
                        ciudades y tomar decisiones informadas.
                    </p>
                </div>

                <!-- Columna Derecha: Tarjeta Naranja -->
                <div class="problem-card-orange">
                    <div class="stat-item">
                        <span class="stat-number">7 millones</span>
                        <p class="stat-desc">Muertes anuales por contaminación del aire</p>
                    </div>
                    <div class="stat-divider"></div>
                    <div class="stat-item">
                        <span class="stat-number">91%</span>
                        <p class="stat-desc">De la población mundial respira aire contaminado</p>
                    </div>
                </div>

            </div>
        </section>

        <!-- ====== SECCIÓN: NUESTRA SOLUCIÓN ====== -->
        <section class="section-solution-details">
            <div class="wrap problem-grid">

                <!-- Columna Izquierda: Tarjeta Verde (Estilo Atmos) -->
                <div class="solution-card-atmos">
                    <div class="feature-item">
                        <h3>Red Colaborativa</h3>
                        <p>Cientos de sensores personales trabajando juntos</p>
                    </div>
                    <div class="stat-divider dark"></div>
                    <div class="feature-item">
                        <h3>Datos en Tiempo Real</h3>
                        <p>Información actualizada cada minuto</p>
                    </div>
                    <div class="stat-divider dark"></div>
                    <div class="feature-item">
                        <h3>Acceso Abierto</h3>
                        <p>Información disponible para todos</p>
                    </div>
                </div>

                <!-- Columna Derecha: Texto -->
                <div class="problem-text">
                    <h2 class="section-title-left">Nuestra solución</h2>
                    <p>
                        Atmos utiliza sensores personales conectados por Bluetooth a una aplicación móvil. Los datos se
                        envían a un servidor central que genera mapas de contaminación accesibles para todos.
                    </p>
                    <p>
                        Cada persona con un sensor se convierte en un nodo de nuestra red, contribuyendo a crear el mapa
                        más detallado de calidad del aire jamás creado.
                    </p>
                    <a href="#sensor" class="scroll-down big-arrow">
                        <img src="img/ArrowDownCircle.svg" alt="Desplázate hacia abajo" class="scroll-icon">
                    </a>
                </div>

            </div>
        </section>

        <!-- ====== SECCIÓN: NUESTRO SENSOR ====== -->
        <section id="sensor" class="section-sensor">
            <div class="wrap problem-grid">

                <!-- Columna Izquierda: Texto -->
                <div class="problem-text">
                    <h2 class="section-title-left">Nuestro Sensor de <span class="highlight-green">Ozono</span></h2>
                    <p>
                        Dispositivo compacto y preciso para monitorizar la calidad del aire en tiempo real
                    </p>
                </div>

                <!-- Columna Derecha: Imagen -->
                <div class="sensor-image-container">
                    <img src="img/fotosensoratmos.png" alt="Sensor Atmos" class="sensor-img-display">
                </div>

            </div>
        </section>

        <!-- ====== SECCIÓN: ESPECIFICACIONES Y CARACTERÍSTICAS ====== -->
        <section class="section-details-list">
            <div class="wrap problem-grid">

                <!-- Columna Izquierda: Especificaciones -->
                <div class="details-column">
                    <h3 class="details-title">Especificaciones Técnicas</h3>
                    <ul class="custom-list specs">
                        <li><strong>Sensor:</strong> ULPSM-03 de alta precisión</li>
                        <li><strong>Conectividad:</strong> Bluetooth 5.0 de bajo consumo</li>
                        <li><strong>Rango de medición:</strong> 0–500 ppb (partes por billón)</li>
                        <li><strong>Precisión:</strong> ±5% o ±5 ppb</li>
                        <li><strong>Batería:</strong> Recargable, hasta 48 h de autonomía</li>
                        <li><strong>Dimensiones:</strong> 45 mm × 25 mm × 8 mm</li>
                        <li><strong>Peso:</strong> 15 gramos</li>
                    </ul>
                </div>

                <!-- Columna Derecha: Características -->
                <div class="details-column">
                    <h3 class="details-title">Características Principales</h3>
                    <ul class="custom-list features">
                        <li><strong>Portátil y ligero:</strong> Llévalo contigo a cualquier lugar</li>
                        <li><strong>Conexión automática:</strong> Se sincroniza con tu smartphone sin esfuerzo</li>
                        <li><strong>Geolocalización:</strong> Cada medida incluye tu ubicación GPS</li>
                        <li><strong>Historial personal:</strong> Accede a tus mediciones anteriores</li>
                        <li><strong>Alertas inteligentes:</strong> Notificaciones cuando detecta alta contaminación</li>
                        <li><strong>Contribución social:</strong> Tus datos ayudan a toda la comunidad</li>
                    </ul>
                </div>

                <!-- Flecha hacia Únete -->
                <div
                    style="display: flex; justify-content: center; width: 100%; margin-top: 40px; grid-column: 1 / -1;">
                    <a href="#unete" class="scroll-down big-arrow" style="position: static;">
                        <img src="img/ArrowDownCircle.svg" alt="Desplázate hacia abajo" class="scroll-icon">
                    </a>
                </div>

            </div>
        </section>

        <!-- ====== SECCIÓN: ÚNETE A LA RED ====== -->
        <section id="unete" class="section-join">
            <div class="wrap">
                <h2 class="join-title">Únete a la red Atmos</h2>

                <div class="join-grid">

                    <!-- Columna Izquierda: 4 Pasos (Cudrantes) -->
                    <div class="join-steps-grid">

                        <!-- Paso 1 -->
                        <div class="step-box">
                            <div class="step-circle">1</div>
                            <div class="step-content">
                                <h4>Conecta</h4>
                                <p>Vincula el sensor con tu smartphone via Bluetooth</p>
                            </div>
                        </div>

                        <!-- Paso 2 -->
                        <div class="step-box">
                            <div class="step-circle">2</div>
                            <div class="step-content">
                                <h4>Mide</h4>
                                <p>El sensor recopila datos de ozono automáticamente</p>
                            </div>
                        </div>

                        <!-- Paso 3 -->
                        <div class="step-box">
                            <div class="step-circle">3</div>
                            <div class="step-content">
                                <h4>Comparte</h4>
                                <p>Los datos se envian al servidor central de forma anónima</p>
                            </div>
                        </div>

                        <!-- Paso 4 -->
                        <div class="step-box">
                            <div class="step-circle">4</div>
                            <div class="step-content">
                                <h4>Visualiza</h4>
                                <p>Consulta el mapa de contaminación en tiempo real</p>
                            </div>
                        </div>

                    </div>

                    <!-- Columna Derecha: Tarjeta Precio -->
                    <div class="join-right-col">
                        <div class="pricing-card">
                            <div class="pricing-header">
                                <h3>Usuario con Sensor</h3>
                                <p>Contribuye y accede a funciones premium</p>
                            </div>
                            <div class="pricing-price">
                                <span class="amount">89 €</span>
                                <span class="period">Pago único</span>
                            </div>
                            <ul class="pricing-features">
                                <li>Todo lo del plan gratuito</li>
                                <li>Sensor de ozono personal</li>
                                <li>Historial personal de mediciones</li>
                                <li>Alertas personalizadas</li>
                                <li>Contribuye a la red colaborativa</li>
                            </ul>
                            <div class="pricing-action">
                                <a href="registro.php" class="btn btn-light btn-block">Registrarse</a>
                            </div>
                        </div>
                    </div>

                </div>
            </div>
        </section>



    </main>

    <!-- FOOTER -->
    <?php include __DIR__ . '/partials/footer.php'; ?>

    <!-- Scroll suave -->
    <script>
        document.addEventListener('click', e => {
            const a = e.target.closest('a[href^="#"]');
            if (!a) return;
            const el = document.querySelector(a.getAttribute('href'));
            if (!el) return;
            e.preventDefault();
            el.scrollIntoView({ behavior: 'smooth', block: 'start' });
        });
    </script>
</body>

</html>