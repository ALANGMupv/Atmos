/**
 * @file placasMadrid.js
 * @brief Definición estática de las placas simuladas en la Comunidad de Madrid.
 *
 * Cada objeto representa un nodo físico:
 *  - id_placa debe existir en la tabla placa
 *  - tipoGas corresponde con los códigos del sistema
 *  - latitud / longitud son reales
 *
 * @author Alan Guevara Martínez
 * @date 22/12/2025
 */

const { TIPOS_GAS } = require("./configSimulador");

module.exports = [
    { id: "MADRID_01_NO2", tipo: TIPOS_GAS.NO2, lat: 40.4168, lon: -3.7038 },
    { id: "MADRID_02_CO", tipo: TIPOS_GAS.CO, lat: 40.3319, lon: -3.7686 },
    { id: "MADRID_03_O3", tipo: TIPOS_GAS.O3, lat: 40.4818, lon: -3.3641 },
    { id: "MADRID_04_SO2", tipo: TIPOS_GAS.SO2, lat: 40.3459, lon: -3.8249 },
    { id: "MADRID_05_NO2", tipo: TIPOS_GAS.NO2, lat: 40.4894, lon: -3.7122 },
    { id: "MADRID_06_CO", tipo: TIPOS_GAS.CO, lat: 40.4531, lon: -3.6883 },
    { id: "MADRID_07_O3", tipo: TIPOS_GAS.O3, lat: 40.4090, lon: -3.6922 },
    { id: "MADRID_08_SO2", tipo: TIPOS_GAS.SO2, lat: 40.3928, lon: -3.6982 },
    { id: "MADRID_09_NO2", tipo: TIPOS_GAS.NO2, lat: 40.4280, lon: -3.7000 },
    { id: "MADRID_10_CO", tipo: TIPOS_GAS.CO, lat: 40.4210, lon: -3.6690 }
];
