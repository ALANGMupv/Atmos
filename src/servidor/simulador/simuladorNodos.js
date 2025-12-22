/**
 * @file simuladorNodos.js
 * @brief Simulador de nodos sensores Atmos con parada automática y manual.
 *
 * Este script simula múltiples nodos físicos enviando medidas periódicas
 * a la API REST del sistema Atmos.
 *
 * Características:
 *  - Simulación continua mediante setInterval
 *  - Parada automática tras un tiempo máximo configurable
 *  - Parada manual limpia mediante CTRL + C (SIGINT)
 *
 * Este archivo NO accede directamente a la base de datos.
 * Toda la comunicación se realiza vía API REST.
 *
 * @author Alan Guevara Martínez
 * @date 22/12/2025
 */

const axios = require("axios");
const placas = require("./placasMadrid");
const config = require("./configSimulador");


/** Referencia al intervalo de envío */
let intervaloEnvio = null;

/**
 * Genera un valor aleatorio coherente según el tipo de gas.
 *
 * @param {number} tipoGas Código del gas
 * @returns {number} Valor simulado
 */
function generarValor(tipoGas) {
    switch (tipoGas) {
        case 11: return Math.random() * 100 + 20;   // NO2
        case 12: return Math.random() * 5 + 0.5;    // CO
        case 13: return Math.random() * 120 + 30;   // O3
        case 14: return Math.random() * 20 + 5;     // SO2
        default: return 0;
    }
}

/**
 * Genera un RSSI aleatorio realista.
 *
 * @returns {number} RSSI simulado
 */
function generarRSSI() {
    return Math.floor(Math.random() * (-40 + 95) - 95);
}

/**
 * Envía una medida simulada correspondiente a una placa concreta.
 *
 * @param {Object} placa Objeto con datos de la placa
 */
async function enviarMedida(placa) {
    try {
        await axios.post(`${config.API_URL}/medida`, {
            id_placa: placa.id,
            tipo: placa.tipo,
            valor: generarValor(placa.tipo),
            latitud: placa.lat,
            longitud: placa.lon,
            rssi: generarRSSI()
        });

        console.log(`[OK] Medida enviada por ${placa.id}`);

    } catch (err) {
        console.error(`[ERROR] Fallo enviando medida (${placa.id}):`, err.message);
    }
}

/**
 * Inicia el bucle periódico de simulación.
 */
function iniciarSimulacion() {

    console.log("===========================================");
    console.log(" SIMULADOR DE NODOS ATMOS INICIADO");
    console.log(` Placas simuladas : ${placas.length}`);
    console.log(` Intervalo envío  : ${config.INTERVALO_ENVIO_MS} ms`);
    console.log(` Duración máxima  : ${config.TIEMPO_MAXIMO_MS / 1000} s`);
    console.log("===========================================");

    // Iniciar envío periódico
    intervaloEnvio = setInterval(() => {
        placas.forEach(enviarMedida);
    }, config.INTERVALO_ENVIO_MS);

    // Programar parada automática
    setTimeout(finalizarSimulacion, config.TIEMPO_MAXIMO_MS);
}

/**
 * Finaliza la simulación de forma controlada.
 * Detiene el intervalo y termina el proceso Node.js.
 */
function finalizarSimulacion() {

    if (intervaloEnvio) {
        clearInterval(intervaloEnvio);
        intervaloEnvio = null;
    }

    console.log("\n===========================================");
    console.log(" SIMULACIÓN FINALIZADA (PARADA AUTOMÁTICA)");
    console.log("===========================================");

    process.exit(0);
}

// Arranque del simulador
iniciarSimulacion();
