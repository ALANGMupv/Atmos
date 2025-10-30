<?php
/**
 * HEADER PRINCIPAL DE ATMOS
 * 
 * Controla el header según si el usuario es invitado ($isGuest = true)
 * o está logueado ($isGuest = false).
 *
 * Instrucciones de uso:
 * ---------------------
 * Incluye este archivo justo después de abrir la etiqueta <body>:
 * 
 *     session_start();
 *     $isGuest = !isset($_SESSION['usuario']);
 *     $active = 'mapas'; // o 'vincular'
 *     include __DIR__ . '/partials/header.php';
 * 
 * Autor: Alejandro Vázquez Remes
 */

if (!isset($active)) { $active = null; }
if (!isset($isGuest)) { $isGuest = true; } // seguridad
?>

<!-- Vincula el CSS del header -->
<link rel="stylesheet" href="css/header.css">

<header class="site-header">
  <nav class="navbar" aria-label="Navegación principal">

    <!-- Izquierda: LOGO + texto ATMOS -->
    <?php if ($isGuest): ?>
      <!-- Invitado: logo no clickeable -->
      <div class="brand no-link">
        <img src="img/logoAtmosBlanco.png" alt="ATMOS" class="brand-logo">
        <span class="brand-text">ATMOS</span>
      </div>
    <?php else: ?>
      <!-- Usuario logueado: logo clickeable -->
      <a href="index.php" class="brand" aria-label="Inicio ATMOS">
        <img src="img/logoAtmosBlanco.png" alt="ATMOS" class="brand-logo">
        <span class="brand-text">ATMOS</span>
      </a>
    <?php endif; ?>

    <!-- Derecha: Navegación -->
    <div class="nav-right">
      <a href="mapas.php"
         class="nav-link <?php echo $active === 'mapas' ? 'is-active' : ''; ?>"
         <?php echo $active === 'mapas' ? 'aria-current=\"page\"' : ''; ?>>
         Mapas
      </a>

      <?php if ($isGuest): ?>
        <!-- Invitado -->
        <a href="login.php" class="nav-link">Iniciar sesión</a>
      <?php else: ?>
        <!-- Usuario logueado -->
        <a href="vincular.php"
           class="nav-link <?php echo $active === 'vincular' ? 'is-active' : ''; ?>"
           <?php echo $active === 'vincular' ? 'aria-current=\"page\"' : ''; ?>>
           Vincular sensor
        </a>

        <a href="editar_perfil.php" class="account" title="Mi cuenta" aria-label="Mi cuenta">
          <img src="img/usuario.png" alt="Usuario" class="account-avatar">
        </a>
      <?php endif; ?>
    </div>

  </nav>
</header>
