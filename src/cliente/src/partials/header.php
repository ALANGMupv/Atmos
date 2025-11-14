<?php
if (!isset($active))  { $active = null; }
if (!isset($isGuest)) { $isGuest = true; }
?>
<link href="https://fonts.googleapis.com/css2?family=Open+Sans:wght@400;500;600&display=swap" rel="stylesheet">
<link rel="stylesheet" href="css/header.css?v=1.2.0">

<div id="atm-header">
  <header class="site-header">
    <nav class="navbar" aria-label="Navegación principal">

      <!-- LOGO -->
      <div class="brand no-link">
        <img src="img/logoHeader.svg" alt="ATMOS" class="brand-logo">
      </div>

      <!-- Botón Hamburguesa -->
      <button class="menu-toggle" aria-label="Abrir menú">
        <span></span><span></span><span></span>
      </button>

      <!-- Menú -->
      <div class="nav-right">
        <a href="mapas.php"
           class="nav-link <?php echo $active === 'mapa' ? 'is-active' : ''; ?>"
           <?php echo $active === 'mapa' ? 'aria-current="page"' : ''; ?>>
           MAPA CONTAMINACIÓN
        </a>

        <a href="solucion.php"
           class="nav-link <?php echo $active === 'solucion' ? 'is-active' : ''; ?>"
           <?php echo $active === 'solucion' ? 'aria-current="page"' : ''; ?>>
           NUESTRA SOLUCIÓN
        </a>

        <?php if ($isGuest): ?>
          <a href="login.php" class="btn btn-outline">Iniciar sesión</a>
          <a href="registro.php" class="btn btn-primary">Registrarse</a>
        <?php else: ?>
          <a href="perfil.php"
             class="account <?php echo $active === 'perfil' ? 'is-active' : ''; ?>"
             title="Mi perfil" aria-label="Mi perfil">
            <img src="img/UserBlanco.png" alt="Usuario ATMOS" class="account-avatar">
          </a>
        <?php endif; ?>
      </div>
    </nav>
  </header>
</div>

<!-- Script -->
<script>
document.addEventListener("DOMContentLoaded", function() {
  const toggle = document.querySelector(".menu-toggle");
  const nav = document.querySelector(".nav-right");

  toggle.addEventListener("click", () => {
    nav.classList.toggle("open");
    toggle.classList.toggle("open");
  });
});
</script>
