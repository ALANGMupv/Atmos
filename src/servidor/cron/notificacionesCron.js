/**
 * @file cron_notificaciones.js
 * @brief CRON automático para la generación de notificaciones en ATMOS.
 *
 * Este script se ejecuta periódicamente (CRON) para:
 *  - Obtener todas las placas asociadas a usuarios.
 *  - Evaluar reglas de notificación según las lecturas recientes.
 *  - Generar e insertar notificaciones en la base de datos.
 *
 * El proceso finaliza automáticamente tras completar la ejecución.
 *
 * Dependencias:
 *  - Logica: acceso a base de datos MySQL.
 *  - ReglasNotificaciones: reglas de negocio para generar notificaciones.
 *
 * @author —
 * @date 2025
 * @version 1.0
 */

const Logica = require("../logica/Logica");
const ReglasNotificaciones = require("../logica/ReglasNotificaciones");

/**
 * @brief Configuración de la conexión a la base de datos MySQL.
 *
 * Se utiliza para acceder a placas, usuarios y notificaciones.
 *  Sustituir TU_PASSWORD por la contraseña real en producción.
 */
const logica = new Logica({
    host: "localhost",
    user: "root",
    password: "TU_PASSWORD",
    database: "atmos_db"
});

/**
 * @brief Instancia del gestor de reglas de notificación.
 *
 * Encapsula la lógica necesaria para decidir cuándo
 * se debe generar una notificación para una placa.
 */
const reglas = new ReglasNotificaciones(logica);

/**
 * @async
 * @function ejecutarCron
 * @brief Función principal del CRON de notificaciones.
 *
 * Flujo de ejecución:
 *  1. Obtiene todas las placas registradas en el sistema.
 *  2. Evalúa reglas de notificación por placa y usuario.
 *  3. Inserta las nuevas notificaciones generadas en la base de datos.
 *  4. Muestra trazas por consola.
 *  5. Finaliza el proceso.
 *
 * @returns {Promise<void>} No devuelve valor.
 */
async function ejecutarCron() {

    console.log("\n=== CRON NOTIFICACIONES ===");

    /**
     * @brief Obtención de todas las placas registradas.
     * @type {Array<{id_usuario:number, id_placa:number}>}
     */
    const placas = await logica.obtenerTodasLasPlacas();

    for (const { id_usuario, id_placa } of placas) {

        /**
         * @brief Generación de nuevas notificaciones para una placa concreta.
         * @type {Array<Object>}
         */
        const nuevas = await reglas.generarNotificacionesParaPlaca(
            id_usuario,
            id_placa
        );

        for (const n of nuevas) {

            /**
             * @brief Inserción de la notificación en la base de datos.
             */
            await logica.insertarNotificacion({
                id_usuario,
                id_placa,
                tipo: n.tipo,
                titulo: n.titulo,
                mensaje: n.texto,
                nivel: n.nivel || "info",
                icono: n.icono || null
            });

            console.log(
                ` Notificación generada para usuario ${id_usuario}: ${n.tipo}`
            );
        }
    }

    console.log("=== CRON FINALIZADO ===\n");

    /**
     * @brief Finaliza explícitamente el proceso Node.js.
     */
    process.exit();
}

/**
 * @brief Ejecución inmediata del CRON.
 */
ejecutarCron();
