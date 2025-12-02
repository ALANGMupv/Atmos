<?php
/**
 * @file redirectFirebase.php
 * @brief Gestiona las redirecciones necesarias según el parámetro `mode`
 *        enviado por Firebase en los enlaces de acción.
 *
 * @details
 * Este script actúa como intermediario entre los enlaces generados por Firebase
 * para:
 *   - Restablecimiento de contraseña (resetPassword)
 *   - Verificación de correo electrónico (verifyEmail)
 *
 * Firebase envía las siguientes variables en la URL:
 *   - mode: indica la acción solicitada
 *   - oobCode: código temporal generado por Firebase
 *
 * El script verifica estos parámetros y redirige hacia el archivo PHP
 * correspondiente, propagando el `oobCode` para que el archivo de destino
 * realice la operación adecuada.
 *
 * @author Alan Guevara Martínez
 * @date 01/12/2025
 */

/**
 * @var string|null $mode
 * @brief Modo de operación enviado por Firebase.
 * @details Puede ser:
 *   - resetPassword
 *   - verifyEmail
 */
$mode = $_GET["mode"] ?? null;

/**
 * @var string|null $code
 * @brief Código temporal (One-Time Code) utilizado por Firebase.
 */
$code = $_GET["oobCode"] ?? null;

$apiKey = "AIzaSyDAyM_UUwr5uE76Mf5tsNbfk6P3-2HKfJQ"; //API key pública


/**
 * @brief Validación inicial de parámetros.
 * @details Si falta alguno de los parámetros esenciales (`mode` o `oobCode`),
 *          el script detiene su ejecución mostrando un mensaje de error.
 */
if (!$mode || !$code) {
    echo "Parámetros inválidos.";
    exit;
}

/**
 * @brief Selecciona la acción adecuada según el valor de `mode`.
 */
switch ($mode) {

    /**
     * @brief Caso: Restablecer contraseña.
     * @details Redirige al formulario de restablecimiento propio de Atmos.
     */
    case "resetPassword":
        header("Location: resetPasswordAtmos.php?oobCode=$code");
        exit;

    /**
     * @brief Caso: Verificar correo electrónico.
     * @details Redirige al script de verificación de correo propio de Atmos.
     */
    case "verifyEmail":
        header("Location: verificarEmailAtmos.php?oobCode=$code");
        exit;

    /**
     * @brief Cualquier otro valor no contemplado.
     * @details Muestra mensaje genérico de error.
     */
    default:
        echo "Acción no reconocida.";
        exit;
}
?>
