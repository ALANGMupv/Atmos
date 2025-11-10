<?php
/**
 * HEADER PRINCIPAL DE ATMOS (versión blanca)
 * ---------------------------------------------------
 * Controla el header según si el usuario está logueado
 * o entra como invitado.
 */

if (!isset($active))  { $active = null; }
if (!isset($isGuest)) { $isGuest = true; }
?>
<link href="https://fonts.googleapis.com/css2?family=Open+Sans:wght@400;500;600&display=swap" rel="stylesheet">

<!-- Vincula el CSS del header -->
<link rel="stylesheet" href="css/header.css">

<header class="site-header">
  <nav class="navbar" aria-label="Navegación principal">

    <!-- Izquierda: LOGO -->
    <div class="brand no-link">
      <img src="img/logoHeader.svg" alt="ATMOS" class="brand-logo">
    </div>

    <!-- Derecha: Navegación -->
    <div class="nav-right">

      <a href="mapas.php"
         class="nav-link <?php echo $active === 'mapa' ? 'is-active' : ''; ?>"
         <?php echo $active === 'mapa' ? 'aria-current="page"' : ''; ?>>
         Mapa Contaminación
      </a>

      <a href="solucion.php"
         class="nav-link <?php echo $active === 'solucion' ? 'is-active' : ''; ?>"
         <?php echo $active === 'solucion' ? 'aria-current="page"' : ''; ?>>
         Nuestra Solución
      </a>

      <?php if ($isGuest): ?>
        <!-- Invitado -->
        <a href="login.php" class="btn btn-outline">Iniciar sesión</a>
        <a href="registro.php" class="btn btn-primary">Registrarse</a>
      <?php else: ?>
        <!-- Usuario logueado -->
        <a href="perfil.php"
           class="account <?php echo $active === 'perfil' ? 'is-active' : ''; ?>"
           title="Mi perfil" aria-label="Mi perfil">
          <img src="img/UserBlanco.png" alt="Usuario ATMOS" class="account-avatar">
        </a>
      <?php endif; ?>

    </div>

  </nav>

</header>
