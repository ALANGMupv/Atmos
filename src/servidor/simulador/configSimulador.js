/**
 * @file configSimulador.js
 * @brief Configuración global del simulador de nodos Atmos.
 *
 * Este archivo centraliza:
 *  - URL de la API REST
 *  - Intervalos de envío
 *  - Tipos de gas soportados
 *
 * @author Alan Guevara Martínez
 * @date 22/12/2025
 */

module.exports = {

    /** URL base del backend REST */
    API_URL: "https://nagufor.upv.edu.es",

    /** Intervalo de envío de medidas (ms) */
    INTERVALO_ENVIO_MS: 10000,

    /**
     * Tiempo máximo de simulación (ms)
     * 1 minuto = 60000 ms
     */
    TIEMPO_MAXIMO_MS: 60000,

    /** Tipos de gas definidos en la BBDD */
    TIPOS_GAS: {
        NO2: 11,
        CO: 12,
        O3: 13,
        SO2: 14
    }
};
