<?php
/**
 * @file guardarSesion.php
 * @brief Crea y almacena la sesión PHP del usuario autenticado.
 *
 * Este script recibe datos del usuario desde el frontend (login.js),
 * normalmente tras una autenticación válida con Firebase y el backend Node.js.
 *
 * Funciones principales:
 *  - Leer datos JSON enviados por POST.
 *  - Validar la estructura de los datos recibidos.
 *  - Crear la sesión PHP del usuario.
 *  - Devolver una respuesta JSON de confirmación.
 *
 * El script es compatible con dos formatos de entrada:
 *  - Datos planos (id_usuario, nombre, etc.).
 *  - Datos anidados dentro de la clave "usuario".
 */

session_start();

/**
 * ------------------------------------------------------------------
 * 1. Leer JSON enviado desde login.js
 * ------------------------------------------------------------------
 * Se obtiene el contenido bruto del body de la petición HTTP
 * y se decodifica a un array asociativo.
 */
$input = file_get_contents("php://input");
$data = json_decode($input, true);

/**
 * ------------------------------------------------------------------
 * 2. Validar que el backend haya enviado los datos correctos
 * ------------------------------------------------------------------
 * En el formato actual, los datos pueden venir:
 *  - Directamente en el array raíz
 *  - O anidados dentro de la clave "usuario"
 */
if (!$data || !isset($data['id_usuario'])) {

    // Caso: los datos vienen dentro de la clave "usuario"
    if (isset($data['usuario'])) {
        $data = $data['usuario']; // Reescribimos para unificar el acceso
    } else {
        // Datos inválidos: no se puede crear la sesión
        http_response_code(400);
        echo json_encode([
            'status'  => 'error',
            'mensaje' => 'Datos inválidos'
        ]);
        exit;
    }
}

/**
 * ------------------------------------------------------------------
 * 3. Crear la sesión PHP con los datos del usuario
 * ------------------------------------------------------------------
 * Se almacenan los datos mínimos necesarios para la sesión
 * del usuario autenticado.
 */
$_SESSION['usuario'] = [
    'id_usuario' => $data['id_usuario'],
    'nombre'     => $data['nombre']     ?? '',
    'apellidos'  => $data['apellidos']  ?? '',
    'email'      => $data['email']      ?? '',
    'id_rol'     => $data['id_rol']     ?? ''
];

/**
 * ------------------------------------------------------------------
 * 4. Devolver OK al frontend
 * ------------------------------------------------------------------
 * Respuesta JSON simple indicando que la sesión se ha creado correctamente.
 */
echo json_encode(['status' => 'ok']);
