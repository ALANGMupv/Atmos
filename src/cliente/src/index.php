<!doctype html>
<html lang="es">
<head>
    <!--
    /**
     * @file index.php
     * @brief Página principal (Landing Page) del proyecto ATMOS.
     *
     * Esta página actúa como punto de entrada público a la plataforma ATMOS.
     * Presenta el proyecto, su propósito, funcionalidades principales
     * y enlaces de acceso al registro y al mapa público.
     *
     * Secciones principales:
     *  - Hero de bienvenida
     *  - Explicación del funcionamiento
     *  - Funcionalidades según tipo de usuario
     *  - CTA de compra de sensor
     *
     * Dependencias:
     *  - partials/header.php
     *  - partials/footer.php
     *  - CSS globales y específicos
     *
     * @author Equipo ATMOS
     * @version 1.0
     */
    -->

    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>ATMOS — Respira con datos reales</title>

    <!--
    /**
     * @section Fuentes
     * @brief Fuentes tipográficas utilizadas en la landing.
     */
    -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Open+Sans:wght@300;400;600&family=Roboto:wght@600&display=swap" rel="stylesheet">

    <!--
    /**
     * @section Estilos
     * @brief Hojas de estilo globales y componentes reutilizables.
     */
    -->
    <link rel="stylesheet" href="css/index.css?v=1.0.6">
    <link rel="stylesheet" href="css/buttons.css?v=1.0.3">
    <link rel="stylesheet" href="css/header.css?v=1.0.5">
</head>

<body>

<?php
/**
 * @brief Inclusión del header público.
 *
 * Contiene el menú de navegación principal visible para usuarios
 * no autenticados.
 */
include __DIR__ . '/partials/header.php';
?>

<main>

    <!--
    /**
     * @section Hero
     * @brief Sección principal de bienvenida.
     *
     * Presenta el mensaje principal del proyecto ATMOS
     * y llamadas a la acción (registro y mapa público).
     */
    -->
    <section id="hero" class="section section-hero">
        <div class="wrap hero-content">
            <h1 class="hero-title">
                Visualiza el aire que<br>
                <span class="line2">respiras en todo momento</span>
            </h1>

            <p class="hero-sub">
                Únete a una comunidad colaborativa que mide la calidad del aire en tiempo real.
                Tus datos ayudan a crear ciudades más saludables.
            </p>

            <div class="cta-row centered">
                <a href="registro.php" class="btn btn-primary btn-lg">Comienza ahora</a>
                <a href="mapa.php" class="btn btn-outline btn-lg">Ver mapa público</a>
            </div>
        </div>

        <!-- Scroll hacia la siguiente sección -->
        <a href="#funciona" class="scroll-down">
            <img src="img/ArrowDownCircle.svg" alt="Desplázate hacia abajo" class="scroll-icon">
        </a>
    </section>

    <!--
    /**
     * @section Funciona
     * @brief Explicación del funcionamiento de ATMOS.
     *
     * Describe el flujo general del sistema y la participación
     * ciudadana en la recolección y visualización de datos.
     */
    -->
    <section id="funciona" class="section section-funciona">
        <div class="wrap funciona-content">

            <!-- Bloque: Cómo funciona -->
            <h2 class="funciona-title">¿Cómo funciona Atmos?</h2>
            <p class="funciona-sub">
                Un sistema simple y efectivo basado en la participación ciudadana
            </p>

            <div class="funciona-grid">
                <img src="img/Tarjeta1Landing.png" alt="App móvil ATMOS" class="funciona-icon">
                <img src="img/Recolecta Datos.png" alt="Recolección de datos ATMOS" class="funciona-icon">
                <img src="img/Ver Mapas.png" alt="Ver mapas ATMOS" class="funciona-icon">
            </div>

            <div class="section-divider"></div>

            <!-- Bloque: Funcionalidades -->
            <h2 class="funcionalidades-title">Funcionalidades para cada usuario</h2>
            <p class="funcionalidades-sub">
                Diferentes niveles de acceso según tu participación
            </p>

            <div class="funcionalidades-grid">
                <img src="img/Usuario Visitante.png" alt="Usuario visitante ATMOS" class="funcionalidades-icon">
                <img src="img/Usuario Registrado.png" alt="Usuario registrado ATMOS" class="funcionalidades-icon">
            </div>
        </div>
    </section>

    <!--
    /**
     * @section CTA Sensor
     * @brief Llamada a la acción para la compra del sensor.
     *
     * Invita al usuario a adquirir un sensor físico
     * para contribuir activamente a la red ATMOS.
     */
    -->
    <section id="compra-sensor" class="section-cta">
        <div class="wrap">
            <h2 class="cta-title">Comienza a monitorizar el aire hoy con la compra de tu sensor</h2>
            <p class="cta-sub">
                Únete a cientos de personas que ya están contribuyendo a un futuro más saludable
            </p>

            <div class="cta-actions">
                <a href="solucion.php" class="btn btn-secondary btn-lg">Compra tu sensor</a>
            </div>
        </div>
    </section>

</main>

<?php
/**
 * @brief Inclusión del footer público.
 *
 * Contiene enlaces legales, información de contacto
 * y créditos del proyecto.
 */
include __DIR__ . '/partials/footer.php';
?>

<!--
/**
 * @section Scroll Suave
 * @brief Implementa desplazamiento suave entre anclas internas.
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
