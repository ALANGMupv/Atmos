// -------------------------------------------------------------
// CRON: Generador automático de notificaciones ATMOS
// -------------------------------------------------------------

const Logica = require("../logica/Logica");
const ReglasNotificaciones = require("../logica/ReglasNotificaciones");

// CONFIGURA TU CONEXIÓN A MYSQL
const logica = new Logica({
    host: "localhost",
    user: "root",
    password: "TU_PASSWORD",
    database: "atmos_db"
});

const reglas = new ReglasNotificaciones(logica);

async function ejecutarCron() {
    console.log("\n=== CRON NOTIFICACIONES ===");

    const placas = await logica.obtenerTodasLasPlacas();

    for (const { id_usuario, id_placa } of placas) {

        const nuevas = await reglas.generarNotificacionesParaPlaca(id_usuario, id_placa);

        for (const n of nuevas) {

            await logica.insertarNotificacion({
                id_usuario,
                id_placa,
                tipo: n.tipo,
                titulo: n.titulo,
                mensaje: n.texto,
                nivel: n.nivel || "info",
                icono: n.icono || null
            });

            console.log(`🟢 Notificación generada para usuario ${id_usuario}: ${n.tipo}`);
        }
    }

    console.log("=== CRON FINALIZADO ===\n");
    process.exit();
}

ejecutarCron();
