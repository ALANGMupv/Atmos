/**
 * @file ReglasNotificaciones.js
 * @brief Generador de notificaciones automáticas a partir de medidas ambientales.
 *
 * Esta clase encapsula todas las reglas de negocio necesarias para:
 *  - Detectar niveles críticos de ozono.
 *  - Detectar sensores inactivos.
 *  - Identificar lecturas erróneas.
 *  - Generar resúmenes diarios.
 *  - Avisar por pérdida de proximidad (RSSI).
 *
 * No realiza acceso directo a base de datos, delega esa responsabilidad
 * en la clase Logica.
 *
 * @author —
 * @date 2025
 * @version 1.0
 */

class ReglasNotificaciones {

    /**
     * @constructor
     * @brief Crea una instancia del gestor de reglas de notificación.
     *
     * @param {Object} logica
     *        Instancia de la clase Logica que proporciona acceso a la base de datos.
     */
    constructor(logica) {
        this.logica = logica;
    }

    /**
     * @async
     * @function generarNotificacionesParaPlaca
     * @brief Genera notificaciones automáticas para una placa concreta.
     *
     * Evalúa diferentes reglas basadas en:
     *  - Valores de ozono (O₃).
     *  - Inactividad del sensor.
     *  - Lecturas fuera de rango.
     *  - Resumen diario de exposición.
     *  - Distancia estimada mediante RSSI.
     *
     * @param {number} id_usuario Identificador del usuario propietario de la placa.
     * @param {number} id_placa   Identificador de la placa/sensor.
     *
     * @returns {Promise<Array<Object>>}
     *          Array de notificaciones generadas (puede estar vacío).
     */
    async generarNotificacionesParaPlaca(id_usuario, id_placa) {

        /**
         * @brief Lista acumulada de notificaciones generadas.
         * @type {Array<Object>}
         */
        const notificaciones = [];

        /**
         * @name Configuración de umbrales y constantes
         * @brief Valores de referencia usados por las reglas.
         */
        const TIPO_O3 = 13;
        const UMBRAL_O3_CRITICO   = 0.9;
        const UMBRAL_O3_PICO_ALTO = 0.4;
        const MIN_INACTIVO_MIN   = 10;
        const MIN_O3_ESPERADO    = 0;
        const MAX_O3_ESPERADO    = 100;
        const UMBRAL_RSSI_LEJOS  = -85;

        // ============================================================
        // 1) NIVEL CRÍTICO DE OZONO
        // ============================================================

        /**
         * @brief Última medida de ozono registrada para la placa.
         */
        const ultimaO3 = await this.logica.obtenerUltimaMedidaO3(id_placa);

        if (ultimaO3 && ultimaO3.valor > UMBRAL_O3_CRITICO) {
            notificaciones.push({
                tipo: "O3_CRITICO",
                titulo: "Nivel crítico de ozono",
                texto: `Nivel de O₃ crítico: ${Math.round(ultimaO3.valor)} ppm.`,
                icono: "alerta",
                nivel: "critico"
            });
        }

        // ============================================================
        // 2) SENSOR INACTIVO
        // ============================================================

        /**
         * @brief Minutos transcurridos desde la última medida recibida.
         */
        const minutosSinDatos =
            await this.logica.obtenerMinutosDesdeUltimaMedida(id_placa);

        if (minutosSinDatos !== null && minutosSinDatos > MIN_INACTIVO_MIN) {

            let fechaEstable = new Date();
            if (ultimaO3?.fecha_hora) {
                const fechaUl = new Date(ultimaO3.fecha_hora);
                fechaEstable = new Date(
                    fechaUl.getTime() + MIN_INACTIVO_MIN * 60000
                );
            }

            notificaciones.push({
                tipo: "SENSOR_INACTIVO",
                titulo: "Sensor inactivo",
                texto: "Tu sensor dejó de enviar datos. Revisa si está encendido.",
                icono: "desconexion",
                nivel: "warning"
            });
        }

        // ============================================================
        // 3) LECTURAS ERRÓNEAS
        // ============================================================

        /**
         * @brief Conteo de lecturas fuera de rango esperado.
         */
        const lecturasFuera =
            await this.logica.contarLecturasFueraDeRango(
                id_placa,
                TIPO_O3,
                MIN_O3_ESPERADO,
                MAX_O3_ESPERADO,
                0.17
            );

        if (lecturasFuera >= 3) {
            notificaciones.push({
                tipo: "LECTURAS_ERRONEAS",
                titulo: "Lecturas inconsistentes",
                texto: "Detectamos lecturas inusuales. Puede haber un fallo en el sensor.",
                icono: "warning",
                nivel: "warning"
            });
        }

        // ============================================================
        // 4) RESUMEN DIARIO (22:00–23:59)
        // ============================================================

        const ahora = new Date();
        const horaActual = ahora.getHours();

        if (horaActual >= 22 && horaActual <= 23) {

            const promedioHoy =
                await this.logica.obtenerPromedioPorGasHoy(id_placa, TIPO_O3);

            const numPicos =
                await this.logica.contarPicosAltosHoy(
                    id_placa,
                    TIPO_O3,
                    UMBRAL_O3_PICO_ALTO
                );

            if (promedioHoy !== null) {

                let nivel = "baja";
                if (promedioHoy > 0.7) nivel = "alta";
                else if (promedioHoy > 0.4) nivel = "moderada";

                const fechaResumen = new Date();
                fechaResumen.setHours(23, 0, 0, 0);

                notificaciones.push({
                    tipo: "RESUMEN_DIARIO",
                    titulo: "Resumen del día",
                    texto: `Tu exposición al ozono hoy fue ${nivel}. ${numPicos} picos detectados.`,
                    icono: "resumen",
                    nivel: "info"
                });
            }
        }

        // ============================================================
        // 5) DISTANCIA DEL SENSOR (RSSI)
        // ============================================================

        /**
         * @brief Valor RSSI estimado para la placa.
         */
        const rssi = await this.logica.obtenerDistanciaPlaca(id_placa);

        if (rssi !== null && rssi < UMBRAL_RSSI_LEJOS) {

            const fechaNormalizada = new Date();
            fechaNormalizada.setSeconds(0, 0);

            notificaciones.push({
                tipo: "DISTANCIA_SENSOR",
                titulo: "Te estás alejando",
                texto: `La señal es muy débil (${rssi} dBm). ¿Llevas el sensor contigo?`,
                icono: "ubicacion",
                nivel: "warning"
            });
        }

        return notificaciones;
    }
}

module.exports = ReglasNotificaciones;
