<?php
/**
 * Página: resetPasswordAtmos.php
 * ---------------------------------
 * Página donde el usuario introduce dos veces la nueva contraseña.
 * Recibe los parámetros de Firebase:
 *   - mode=resetPassword
 *   - oobCode=XXXX
 *
 * Autor: Alan Guevara Martínez
 * Fecha: 01/12/2025
 */
?>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Atmos - Nueva contraseña</title>
    <link rel="stylesheet" href="css/estilos.css">
    <style>
        .box {
            max-width:420px;margin:40px auto;background:white;
            padding:30px;border-radius:14px;box-shadow:0 6px 20px rgba(0,0,0,0.1);
            font-family:Arial;
        }
        input, button { width:100%;padding:12px;margin-top:10px;border-radius:8px; }
        button { background:#10B981;color:white;font-weight:bold;border:none;cursor:pointer; }
    </style>
</head>
<body>

<div class="box">
    <h2>Nueva contraseña</h2>
    <p>Introduce tu nueva contraseña dos veces:</p>

    <form id="form-reset">
        <input type="password" id="pass1" placeholder="Nueva contraseña" required>
        <input type="password" id="pass2" placeholder="Repetir contraseña" required>
        <button type="submit">Guardar contraseña</button>
    </form>
</div>

<script type="module" src="js/resetPasswordAtmos.js"></script>

</body>
</html>
