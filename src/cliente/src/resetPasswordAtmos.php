<?php
/**
 * @file resetPasswordAtmos.php
 * @brief Página donde el usuario establece una nueva contraseña tras seguir
 *        un enlace de restablecimiento enviado por Firebase.
 *
 * @details
 *  - Este archivo muestra un formulario donde el usuario puede introducir
 *    una nueva contraseña y confirmarla.
 *  - El procesamiento real del cambio de contraseña se realiza mediante
 *    el script JS: `js/resetPasswordAtmos.js`, el cual utiliza Firebase Auth.
 *  - El diseño sigue la estética de las páginas de registro/login de Atmos.
 *
 * @author
 *  Alan Guevara Martínez
 *
 * @date
 *  01/12/2025
 */
?>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Atmos - Nueva contraseña</title>

    <link rel="stylesheet" href="css/estilos.css">
    <link rel="stylesheet" href="css/registro.css">

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500&family=Open+Sans:wght@300;400;500&display=swap" rel="stylesheet">

    <style>
        .reset-container {
            display:flex;
            justify-content:center;
            align-items:center;
            min-height:100vh;
            background:#F9FAFB;
        }

        .reset-box {
            background:white;
            width:380px;
            padding:32px;
            border-radius:16px;
            box-shadow:0 10px 25px rgba(0,0,0,0.10);
            font-family:'Open Sans', sans-serif;
        }

        .reset-box h2 {
            text-align:center;
            margin-bottom:10px;
            font-family:"Roboto", sans-serif;
            color:#111827;
        }

        .texto-info {
            text-align:center;
            font-size:0.9rem;
            color:#6b7280;
            margin-bottom:25px;
        }

        .input-password {
            position:relative;
            width:100%;
            display:flex;
            align-items:center;
            margin-bottom: 0.5em;
        }

        .input-base {
            all:unset;
            width:100%;
            border-bottom:1px solid rgba(17, 24, 39, 0.29);
            font-size:1rem;
            padding:8px 0;
        }

        .toggle-pass {
            position:absolute;
            right:0;
            cursor:pointer;
            width:22px;
            height:22px;
            background-image:url('img/icono-ojo.svg');
            background-size:contain;
            background-repeat:no-repeat;
            opacity:0.6;
            transition:0.2s;
        }

        .toggle-pass.active {
            background-image:url('img/icono-ojo-abierto.svg');
        }

        button.btn-reset {
            margin-top:25px;
            width:100%;
            height:45px;
            background:#059669;
            border:none;
            border-radius:15px;
            font-size:1rem;
            color:white;
            cursor:pointer;
            font-family:'Open Sans', sans-serif;
        }

        button.btn-reset:hover {
            background:#058d63;
        }

        .fila-label {
            display: flex;
            flex-direction: column;
            align-items: flex-start;
            gap: 5px;
        }

        .info-contrasena {
            font-family: 'Open Sans', sans-serif;
            font-weight: 300;
            font-size: 0.75rem;
            color: #6b7280;
            margin-left: 0;
            max-width: 100%;
            line-height: 1.3;
        }

    </style>
</head>

<body>

<div class="reset-container">
    <div class="reset-box">

        <h2>Restablecer contraseña</h2>
        <p class="texto-info">Introduce tu <strong>nueva</strong> contraseña</p>

        <form id="form-reset">

            <!-- Campo contraseña 1 -->
            <div class="campo">
                <div class="fila-label">
                    <label for="pass1">Contraseña</label>
                    <span class="info-contrasena">
                        Debe contener al menos 8 caracteres e incluir números o símbolos
                    </span>
                </div>

                <div class="input-password">
                    <input type="password" id="pass1" class="input-base" required>
                    <span class="toggle-pass" data-target="pass1"></span>
                </div>
            </div>

            <!-- Campo contraseña 2 -->
            <div class="campo">
                <label for="pass2">Repetir contraseña</label>
                <div class="input-password">
                    <input type="password" id="pass2" class="input-base" required>
                    <span class="toggle-pass" data-target="pass2"></span>
                </div>
            </div>

            <button type="submit" class="btn-reset">Guardar contraseña</button>

        </form>

    </div>
</div>

<script type="module" src="js/resetPasswordAtmos.js"></script>

</body>
</html>
