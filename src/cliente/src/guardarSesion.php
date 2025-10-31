<?php
session_start();

$input = file_get_contents("php://input");
$data = json_decode($input, true);

if (!$data || !isset($data['id_Usuario'])) {
    http_response_code(400);
    echo json_encode(['status' => 'error', 'mensaje' => 'Datos inválidos']);
    exit;
}

$_SESSION['usuario'] = [
    'id'        => $data['id_usuario'],
    'nombre'    => $data['nombre'] ?? '',
    'apellidos' => $data['apellidos'] ?? '',
    'email'     => $data['email'] ?? '',
];


echo json_encode(['status' => 'ok']);
