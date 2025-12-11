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

                <a href="informe_nodos.php"
                   class="nav-link <?php echo $active === 'nodos' ? 'is-active' : ''; ?>"
                    <?php echo $active === 'nodos' ? 'aria-current="page"' : ''; ?>>
                    NODOS
                </a>

                <!-- ICONO PERFIL -->
                <a href="perfilEmpresario.php"
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
