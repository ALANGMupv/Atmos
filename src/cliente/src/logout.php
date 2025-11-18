<?php
/**
 * --------------------------------------------------------------------
 *  Fichero: logout.php
 *  Autor: Alan Guevara Martínez
 *
 *  Descripción:
 * ------------
 *  Este script cierra completamente la sesión PHP del usuario.
 *  Eliminamos todos los datos de sesión y destruimos la sesión actual.
 *
 *  Además, permite redirigir a una página específica después del logout.
 *  Si en la URL se pasa el parámetro GET ?redir=destino.php,
 *  el usuario será enviado a dicha página tras cerrar sesión.
 *
 *  Ejemplo de uso desde un enlace:
 *      <a href="logout.php?redir=restContrasenya.php">Continuar</a>
 *
 *  Si NO se recibe ningún parámetro 'redir', el usuario se enviará
 *  de forma predeterminada a login.php.
 * --------------------------------------------------------------------
 */

session_start(); // Inicia o reanuda la sesión para poder destruirla

// ------------------------------------------------------------------
// 1. ELIMINAR TODAS LAS VARIABLES DE SESIÓN
// ------------------------------------------------------------------
session_unset();   // Borra todas las variables de la sesión

// ------------------------------------------------------------------
// 2. DESTRUIR LA SESIÓN COMPLETA
// ------------------------------------------------------------------
session_destroy(); // Elimina por completo la sesión en el servidor


// ------------------------------------------------------------------
// 3. DETERMINAR A DÓNDE REDIRIGIR DESPUÉS DEL LOGOUT
// ------------------------------------------------------------------

// Si se proporciona un destino personalizado (ej: restContrasenya.php),
// redirigimos allí.
if (isset($_GET['redir']) && !empty($_GET['redir'])) {
    $destino = $_GET['redir'];
    header("Location: " . $destino);
    exit;
}

// Si NO se especificó una redirección → mandamos al login
header("Location: login.php");
exit;
