<?php
session_start();

// Verificar si el usuario tiene sesión
if (!isset($_SESSION['usuario'])) {
    // Si no tiene sesión → redirigir a mapas.php
    header("Location: mapas.php");
    exit;
}

$isGuest = false; // ya sabemos que está logueado
$active  = 'medidas';
include __DIR__ . '/partials/header.php';
?>
<!doctype html>
<html lang="es">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Proyecto Biometría – Medidas</title>

    <!-- Hoja de estilos principal -->
    <link rel="stylesheet" href="css/index.css" />
    <link rel="stylesheet" href="css/header.css" />

    <!-- Script JS de la app -->
    <script defer src="js/index.js"></script>
</head>
<body>

<main class="container">
    <section class="panel">
        <div class="panel-header">
            <h2>Últimas medidas</h2>
            <div class="controls">
                <label for="limit">Filas:</label>
                <select id="limit">
                    <option value="1" selected>Última medida</option>
                    <option value="10">10</option>
                    <option value="50">50</option>
                    <option value="100">100</option>
                    <option value="300">300</option>
                </select>
            </div>
        </div>

        <div class="table-wrap">
            <table class="table" id="tabla-medidas">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Placa</th>
                    <th>Tipo</th>
                    <th>Valor</th>
                    <th>Contador</th>
                    <th>Lat, Long</th>
                    <th>Fecha</th>
                    <th>Hora</th>
                </tr>
                </thead>
                <tbody id="tbody-medidas">
                <tr><td colspan="7" class="muted">Cargando…</td></tr>
                </tbody>
            </table>
        </div>

        <p id="error" class="error" hidden></p>
    </section>
</main>

<footer class="footer">
    <div class="container">
        <small>© 2025 Proyecto Biometría</small>
    </div>
</footer>

</body>
</html>
