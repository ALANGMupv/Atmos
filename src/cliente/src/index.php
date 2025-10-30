<?php
/*
==========================================================
Nombre del archivo: index.php
Descripción: Archivo principal de la interfaz web del Proyecto Biometría.
Muestra las medidas obtenidas en tiempo real desde la API en formato tabla.
Contiene el selector de número de filas y los elementos visuales principales
de la interfaz del Sprint 0.
Fecha: 30/10/2025
Autor: Alan Guevara Martínez
==========================================================
*/

// Aunque tiene extensión .php, este archivo no ejecuta código PHP;
// únicamente genera el HTML de la aplicación web.
?>
<!doctype html>
<html lang="es">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Proyecto Biometría – Medidas</title>

    <!-- Hoja de estilos principal -->
    <link rel="stylesheet" href="css/index.css" />

    <!-- Script principal de la app -->
    <script defer src="js/index.js?v=3"></script>
    <!-- Comentario temporal: prueba de commit -->
</head>
<body>

<!-- Barra superior con título del proyecto -->
<header class="topbar">
    <div class="container">
        <h1>Proyecto Biometría - Alan Guevara Martínez</h1>
        <p class="subtitle">Sprint 0 – Medidas en tiempo real</p>
    </div>
</header>

<main class="container">

    <!-- Panel principal donde se muestran las medidas -->
    <section class="panel">

        <!-- Cabecera del panel con título y selector de filas -->
        <div class="panel-header">
            <h2>Últimas medidas</h2>

            <div class="controls">
                <!-- Selector para definir el número de medidas mostradas -->
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

        <!-- Contenedor de la tabla de medidas -->
        <div class="table-wrap">
            <!-- Tabla dinámica cargada desde la API -->
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
                    <!-- Mensaje inicial mientras se cargan los datos -->
                    <tr><td colspan="7" class="muted">Cargando…</td></tr>
                </tbody>
            </table>
        </div>

        <!-- Mensaje de error (oculto por defecto) -->
        <p id="error" class="error" hidden></p>
    </section>
</main>

<!-- Pie de página con créditos -->
<footer class="footer">
    <div class="container">
        <small>© 2025 Proyecto Biometría</small>
    </div>
</footer>

</body>
</html>
