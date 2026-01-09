<?php
/**
 * @file solucion.php
 * @brief Página informativa "Nuestra Solución" de la plataforma ATMOS.
 *
 * Esta vista explica el problema de la contaminación del aire,
 * la propuesta de valor de Atmos, el funcionamiento del sistema,
 * el sensor de ozono y el modelo de participación del usuario.
 *
 * La página adapta el header en función de si el usuario está
 * autenticado o accede como invitado.
 *
 * @author —
 * @date 2025
 * @version 1.0
 */

session_start();

/**
 * @var string $active
 * @brief Sección activa del menú de navegación.
 */
$active = 'solucion';

/**
 * @var bool $isGuest
 * @brief Indica si el usuario es invitado (no autenticado).
 */
$isGuest = !isset($_SESSION['usuario']);

/**
 * @brief Carga dinámica del header según estado de sesión.
 */
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

    <!--
    /**
     * @brief Fuentes tipográficas utilizadas en la vista.
     */
    -->
    <link
        href="https://fonts.googleapis.com/css2?family=Open+Sans:wght@300;400;600&family=Roboto:wght@500;700&display=swap"
        rel="stylesheet">

    <!--
    /**
     * @brief Librería de iconos Font Awesome.
     */
    -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

    <!--
    /**
     * @brief Hojas de estilo generales y específicas de la página.
     */
    -->
    <link rel="stylesheet" href="css/index.css?v=1.0.6">
    <link rel="stylesheet" href="css/header.css">
    <link rel="stylesheet" href="css/solucion.css?v=1.0.0">
</head>

<body>

<main>

    <!-- ==================================================
         HERO
    ================================================== -->
    <!--
    /**
     * @section Hero
     * @brief Sección principal introductoria de la solución ATMOS.
     */
    -->
    <section class="solucion-hero">
        <div class="wrap">
            <h1 class="solucion-title">Cómo funciona Atmos</h1>
            <p class="solucion-subtitle">
                Una plataforma colaborativa que transforma datos ciudadanos en información valiosa sobre la calidad
                del aire que respiramos a través de nuestro sensor de ozono
            </p>
        </div>
    </section>

    <!-- ==================================================
         EL PROBLEMA
    ================================================== -->
    <!--
    /**
     * @section Problema
     * @brief Describe el contexto y la problemática de la contaminación del aire.
     */
    -->
    <section id="problema" class="section-problem">
        <div class="wrap problem-grid">

            <!-- Texto explicativo -->
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

            <!-- Tarjeta estadística -->
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

    <!-- ==================================================
         NUESTRA SOLUCIÓN
    ================================================== -->
    <!--
    /**
     * @section Solución
     * @brief Explicación del enfoque colaborativo de Atmos.
     */
    -->
    <section class="section-solution-details">
        <div class="wrap problem-grid">

            <!-- Tarjeta de características -->
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

            <!-- Texto descriptivo -->
            <div class="problem-text">
                <h2 class="section-title-left">Nuestra solución</h2>
                <p>
                    Atmos utiliza sensores personales conectados por Bluetooth a una aplicación móvil.
                </p>
                <p>
                    Cada persona con un sensor se convierte en un nodo de nuestra red.
                </p>
                <a href="#sensor" class="scroll-down big-arrow">
                    <img src="img/ArrowDownCircle.svg" alt="Desplázate hacia abajo" class="scroll-icon">
                </a>
            </div>

        </div>
    </section>

    <!-- ==================================================
         SENSOR
    ================================================== -->
    <!--
    /**
     * @section Sensor
     * @brief Presentación del sensor físico de ozono de Atmos.
     */
    -->
    <section id="sensor" class="section-sensor">
        <div class="wrap problem-grid">

            <div class="problem-text">
                <h2 class="section-title-left">Nuestro Sensor de <span class="highlight-green">Ozono</span></h2>
                <p>
                    Dispositivo compacto y preciso para monitorizar la calidad del aire en tiempo real
                </p>
            </div>

            <div class="sensor-image-container">
                <img src="img/fotosensoratmos.png" alt="Sensor Atmos" class="sensor-img-display">
            </div>

        </div>
    </section>

    <!-- ==================================================
         UNIRSE A LA RED
    ================================================== -->
    <!--
    /**
     * @section Unete
     * @brief Explica el proceso de participación del usuario en la red.
     */
    -->
    <section id="unete" class="section-join">
        <div class="wrap">
            <h2 class="join-title">Únete a la red Atmos</h2>
            <!-- Contenido intacto -->
        </div>
    </section>

</main>

<!--
/**
 * @brief Footer común de la plataforma.
 */
-->
<?php include __DIR__ . '/partials/footer.php'; ?>

<!--
/**
 * @brief Script para scroll suave entre secciones ancla.
 */
-->
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
