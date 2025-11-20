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
    <title>ATMOS - Cómo Funciona (Próximamente)</title>

    <!-- Fuentes (Asumo que ya las cargas en el header, pero por si acaso) -->
    <link href="https://fonts.googleapis.com/css2?family=Open+Sans:wght@300;400;600&family=Roboto:wght@500;700&display=swap" rel="stylesheet">

    <!-- Iconos (FontAwesome para el icono del mapa) -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

    <!-- ESTILOS -->
    <!-- Asumo que index.css tiene tus variables :root, si no, el mapa.css las incluye de respaldo -->
    <link rel="stylesheet" href="css/index.css">
    <link rel="stylesheet" href="css/header.css">
    <link rel="stylesheet" href="css/mapa.css">
</head>

<body>

<!-- Contenedor principal de la página de "Próximamente" -->
<main class="coming-soon-section">
    <div class="wrap">

        <div class="coming-soon-card">
            <!-- Icono animado -->
            <div class="icon-container">
                <i class="fa-solid fa-gear fa-spin"></i>
            </div>

            <!-- Textos -->
            <h1 class="cs-title">Estamos mejorando Atmos</h1>
            <p class="cs-subtitle">
                Nuestra sección de <strong>Cómo Funciona</strong> está casi lista.
                 En breve podrás entender paso a paso cómo Atmos monitoriza la calidad del aire.
            </p>

            <!-- Barra de progreso decorativa -->
            <div class="progress-bar-container">
                <div class="progress-bar"></div>
            </div>
            <p class="cs-status">En desarollo</p>
        </div>

    </div>
</main>

</body>
</html>