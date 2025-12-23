<?php

/**
 * @file estacionesOficiales.php
 * @brief Proxy backend para obtener estaciones oficiales OpenAQ
 *        (idéntico al comportamiento de Android).
 *
 * Estrategia:
 *  1) Descarga locations (bbox Comunitat Valenciana)
 *  2) Para cada location → descarga sensors
 *  3) Devuelve JSON unificado listo para Leaflet
 *
 * @author Alan Guevara Martínez
 * @date 23/12/2025
 */

header('Content-Type: application/json');

/* =========================================================
 * CACHE SIMPLE (fichero) Necesario para que no tarde tanto al entrar
 * ========================================================= */

$cacheFile = __DIR__ . '/cache_estaciones.json';
$cacheTTL  = 900; // segundos (15 minutos de caché - la comparten todos los usuarios)

// Si existe caché válida → devolver y salir
if (file_exists($cacheFile) && (time() - filemtime($cacheFile)) < $cacheTTL) {
    readfile($cacheFile);
    exit;
}

/* API KEY */
const API_KEY = '3dd56585357ae0bd5b39f7c77852e61d63b0d3ac21f6da1b8befedd154d58e0a';

/**
 * Realiza una petición GET a OpenAQ.
 *
 * @param string $url
 * @return array|null
 */
function fetchOpenAQ(string $url): ?array
{
    $ch = curl_init($url);

    curl_setopt_array($ch, [
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_HTTPHEADER => [
            'Accept: application/json',
            'X-API-Key: ' . API_KEY
        ]
    ]);

    $res  = curl_exec($ch);
    $code = curl_getinfo($ch, CURLINFO_HTTP_CODE);

    curl_close($ch);

    if ($code >= 400 || !$res) {
        return null;
    }

    return json_decode($res, true);
}

/* =========================================================
 * PASO 1: LOCATIONS
 * ========================================================= */

$locUrl =
    'https://api.openaq.org/v3/locations'
    . '?bbox=-2.0,37.7,0.8,40.8'
    . '&limit=40';

$locations = fetchOpenAQ($locUrl);

if (!$locations) {
    echo json_encode(['status' => 'error']);
    exit;
}

$resultado = [];

/* =========================================================
 * PASO 2: SENSORS
 * ========================================================= */

foreach ($locations['results'] as $loc) {

    $estacion = [
        'id'     => $loc['id'],
        'nombre' => $loc['name'] ?? ('Estación ' . $loc['id']),
        'lat'    => $loc['coordinates']['latitude'],
        'lon'    => $loc['coordinates']['longitude'],

        'no2' => null,
        'o3'  => null,
        'co'  => null,
        'so2' => null,

        'u_no2' => null,
        'u_o3'  => null,
        'u_co'  => null,
        'u_so2' => null
    ];

    $sensUrl  = "https://api.openaq.org/v3/locations/{$loc['id']}/sensors";
    $sensores = fetchOpenAQ($sensUrl);

    if (!$sensores) {
        continue;
    }

    foreach ($sensores['results'] as $s) {

        if (!isset($s['latest'])) {
            continue;
        }

        $param = strtolower($s['parameter']['name']);
        $unit  = $s['parameter']['units'] ?? 'µg/m³';
        $value = $s['latest']['value'];

        if (str_contains($param, 'no2')) {
            $estacion['no2']   = $value;
            $estacion['u_no2'] = $unit;

        } elseif (str_contains($param, 'o3')) {
            $estacion['o3']   = $value;
            $estacion['u_o3'] = $unit;

        } elseif (str_contains($param, 'co')) {
            $estacion['co']   = $value;
            $estacion['u_co'] = $unit;

        } elseif (str_contains($param, 'so2')) {
            $estacion['so2']   = $value;
            $estacion['u_so2'] = $unit;
        }
    }

    $resultado[] = $estacion;
}

$output = json_encode([
    'status'     => 'ok',
    'estaciones' => $resultado
]);

file_put_contents($cacheFile, $output);
echo $output;
