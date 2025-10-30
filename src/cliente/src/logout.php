<?php
/**
 * logout.php
 * -----------------
 * Cierra la sesión PHP y redirige al login.
 */
session_start();

// Elimina todos los datos de sesión
session_unset();
session_destroy();

// Redirige al login
header("Location: login.php");
exit;
