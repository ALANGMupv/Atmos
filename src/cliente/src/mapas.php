<?php
session_start();

$active = 'mapas';
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
  <title>ATMOS - Mapas</title>

  <!-- ESTILOS -->
  <link rel="stylesheet" href="css/index.css">
  <link rel="stylesheet" href="css/header.css">
  <link rel="stylesheet" href="css/mapa.css">
</head>

<body>



<main class="container">
  <section class="panel">
    <div class="panel-header">
      <h2>Vista de mapas</h2>
    </div>

    <div class="mapa-simulado">
      <img src="img/mapa-simulado.png" alt="Mapa simulado" class="mapa-img">
    </div>
  </section>
</main>
</body>
</html>
