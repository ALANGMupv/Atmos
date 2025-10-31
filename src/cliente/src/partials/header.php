<?php
/**
 * HEADER PRINCIPAL DE ATMOS
 * ---------------------------------------------------
 * Controla el header según si el usuario está logueado
 * o entra como invitado.
 *
 * Uso:
 * ---------------------------------------------------
 * session_start();
 * $isGuest = !isset($_SESSION['usuario']);
 * $active  = 'mapas' | 'medidas' | 'vincular' | 'perfil' | null;
 * include __DIR__ . '/partials/header.php';
 *
 * Autor: Alejandro Vázquez Remes
 */

if (!isset($active))  { $active = null; }
if (!isset($isGuest)) { $isGuest = true; } // seguridad
?>

<!-- Vincula el CSS del header -->
<link rel="stylesheet" href="css/header.css">

<header class="site-header">
  <nav class="navbar" aria-label="Navegación principal">

    <!-- Izquierda: LOGO + texto ATMOS (no clickeable) -->
    <div class="brand no-link">
      <img src="img/logoAtmosBlanco.png" alt="ATMOS" class="brand-logo">
      <span class="brand-text">ATMOS</span>
    </div>

    <!-- Derecha: Navegación -->
    <div class="nav-right">

      <!-- Siempre visible -->
      <a href="mapas.php"
         class="nav-link <?php echo $active === 'mapas' ? 'is-active' : ''; ?>"
         <?php echo $active === 'mapas' ? 'aria-current="page"' : ''; ?>>
         Mapas
      </a>

      <?php if (!$isGuest): ?>
        <!-- Solo usuarios logueados -->
        <a href="index.php"
           class="nav-link <?php echo $active === 'medidas' ? 'is-active' : ''; ?>"
           <?php echo $active === 'medidas' ? 'aria-current="page"' : ''; ?>>
           Medidas
        </a>

        <a href="vincular.php"
           class="nav-link <?php echo $active === 'vincular' ? 'is-active' : ''; ?>"
           <?php echo $active === 'vincular' ? 'aria-current="page"' : ''; ?>>
           Vincular sensor
        </a>

        <a href="perfil.php"
           class="account <?php echo $active === 'perfil' ? 'is-active' : ''; ?>"
           title="Mi perfil" aria-label="Mi perfil"
           <?php echo $active === 'perfil' ? 'aria-current="page"' : ''; ?>>
          <img src="img/UserBlanco.png" alt="Usuario ATMOS" class="account-avatar">
        </a>

      <?php else: ?>
        <!-- Invitado -->
        <a href="login.php" class="nav-link">Iniciar sesión</a>
      <?php endif; ?>

    </div>

  </nav>
</header>
