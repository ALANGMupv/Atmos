<?php

/**
 * --------------------------------------------------------------
 *  Fichero: headerLogueado.php
 *  Autor: Alan Guevara Martínez
 *  Descripción: Header para las páginas del usuario logueado (como un include).
 *  Fecha: 16/11/2025
 * --------------------------------------------------------------
 */

if (!isset($active))  { $active = null; }
?>

<link rel="stylesheet" href="css/headerLogueado.css?v=1.0.0">
<link href="https://fonts.googleapis.com/css2?family=Open+Sans:wght@400;500;600&display=swap" rel="stylesheet">

<div id="atm-header">
    <header class="site-header">
        <nav class="navbar" aria-label="Navegación principal">

            <!-- LOGO -->
            <a href="index.php" class="brand">
                <img src="img/logoHeader.svg" alt="ATMOS" class="brand-logo">
            </a>

            <!-- Botón Hamburguesa -->
            <button class="menu-toggle" aria-label="Abrir menú">
                <span></span><span></span><span></span>
            </button>

            <!-- Menú -->
            <div class="nav-right">

                <a href="userPage.php"
                   class="nav-link <?php echo $active === 'portal' ? 'is-active' : ''; ?>"
                    <?php echo $active === 'portal' ? 'aria-current="page"' : ''; ?>>
                    MI PORTAL
                </a>

                <a href="incidencias.php"
                   class="nav-link <?php echo $active === 'incidencias' ? 'is-active' : ''; ?>"
                    <?php echo $active === 'incidencias' ? 'aria-current="page"' : ''; ?>>
                    INCIDENCIAS
                </a>

                <a href="mapaUser.php"
                   class="nav-link <?php echo $active === 'mapas' ? 'is-active' : ''; ?>"
                    <?php echo $active === 'mapas' ? 'aria-current="page"' : ''; ?>>
                    MAPA CONTAMINACIÓN
                </a>

                <!-- ICONO NOTIFICACIONES -->
                <a href="#" class="icon-btn" title="Notificaciones" aria-label="Notificaciones">
                    <img src="img/notificaciones.svg" class="icon" alt="Notificaciones">
                </a>

                <!-- ICONO PERFIL -->
                <a href="perfil.php"
                   class="icon-btn account <?php echo $active === 'perfil' ? 'is-active' : ''; ?>"
                   title="Mi perfil" aria-label="Mi perfil">
                    <img src="img/perfilLogueado.svg" class="icon" alt="Perfil ATMOS">
                </a>

            </div>
        </nav>
    </header>
</div>

<!-- Script menu hamburguesa (copiado y pegado de header.php, funciona) -->
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
