<?php
session_start();

// ---------------------------------------------------------------
// 1. Leer JSON enviado desde login.js
// ---------------------------------------------------------------
$input = file_get_contents("php://input");
$data = json_decode($input, true);

// ---------------------------------------------------------------
// 2. Validar que el backend haya enviado los datos correctos
//    AHORA vienen dentro de $data['usuario']
// ---------------------------------------------------------------
if (!$data || !isset($data['id_usuario'])) {

    // SI VIENE EN LA CLAVE "usuario" (nuevo formato)
    if (isset($data['usuario'])) {
        $data = $data['usuario']; // reescribir para usarlo igual
    } else {
        http_response_code(400);
        echo json_encode(['status' => 'error', 'mensaje' => 'Datos inválidos']);
        exit;
    }
}

// ---------------------------------------------------------------
// 3. Crear la sesión PHP con los datos del usuario
// ---------------------------------------------------------------
$_SESSION['usuario'] = [
    'id_usuario' => $data['id_usuario'],
    'nombre'     => $data['nombre'] ?? '',
    'apellidos'  => $data['apellidos'] ?? '',
    'email'      => $data['email'] ?? '',
     'id_rol'     => $data['id_rol'] ?? ''
];

// ---------------------------------------------------------------
// 4. Devolver OK al frontend
// ---------------------------------------------------------------
echo json_encode(['status' => 'ok']);
