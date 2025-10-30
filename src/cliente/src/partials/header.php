<?php
/**
 * HEADER PRINCIPAL DE ATMOS
 * 
 * Instrucciones de uso:
 * ---------------------
 * 1️⃣ Incluye este archivo justo después de abrir la etiqueta <body>:
 *     include __DIR__ . '/partials/header.php';
 * 
 * 2️⃣ Si quieres marcar una pestaña como activa:
 *     $active = 'mapas';    // o 'vincular'
 *     Debe ir antes de incluir el header.
 * 
 * Ejemplo:
 * <?php
 *   $active = 'vincular';
 *   include __DIR__ . '/partials/header.php';
 * ?>
 * 
 * Autor: Alejandro Vázquez Remes
 */

if (!isset($active)) { $active = null; }
?>

<!-- Vincula el CSS del header -->
<link rel="stylesheet" href="css/header.css">

<header class="site-header">
  <nav class="navbar" aria-label="Navegación principal">

    <!-- Izquierda: LOGO + texto ATMOS -->
    <a href="index.php" class="brand" aria-label="Inicio ATMOS">
      <img src="img/logoAtmosBlanco.png" alt="ATMOS" class="brand-logo">
      <span class="brand-text">ATMOS</span>
    </a>

    <!-- Derecha: Pestañas + Foto usuario -->
    <div class="nav-right">

      <a href="mapas.php"
         class="nav-link <?php echo $active === 'mapas' ? 'is-active' : ''; ?>"
         <?php echo $active === 'mapas' ? 'aria-current="page"' : ''; ?>>
         Mapas
      </a>

      <a href="vincular.php"
         class="nav-link <?php echo $active === 'vincular' ? 'is-active' : ''; ?>"
         <?php echo $active === 'vincular' ? 'aria-current="page"' : ''; ?>>
         Vincular sensor
      </a>

      <a href="editar_perfil.php" class="account" title="Mi cuenta" aria-label="Mi cuenta">
        <img src="img/usuario.png" alt="Usuario" class="account-avatar">
      </a>

    </div>
  </nav>
</header>
