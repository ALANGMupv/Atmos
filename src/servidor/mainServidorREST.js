/**
 * mainServidorREST.js
 * -------------------------
 * Punto de entrada principal del servidor REST basado en Express.
 *
 * Responsabilidades:
 *   - Configurar y lanzar el servidor HTTP.
 *   - Verificar la conexión con MySQL antes del arranque.
 *   - Registrar las rutas REST definidas en `ReglasREST.js`.
 *   - Instanciar la capa de lógica (`Logica.js`).
 *   - Gestionar los logs de ejecución en archivo `app.log`.
 *
 * Esta versión simplificada no depende de Firebase.
 *
 * Autores: Alan Guevara Martínez y Santiago Fuenmayor Ruiz
 */

/* Para que node.js lea las variables de entorno desde el archivo .env */
const path = require("path");
require("dotenv").config({
    path: path.join(__dirname, ".env")
});


const fs = require("fs");
const express = require("express");
const cors = require("cors");
const mysql = require("mysql2/promise");
const Logica = require("./logica/Logica");
const reglasREST = require("./apiREST/ReglasREST");


// --------------------------------------------------------------------------
//  Configuración de logging
// --------------------------------------------------------------------------
/**
 * Redefine las funciones estándar de consola (`console.log` y `console.error`)
 * para que los mensajes se registren tanto en el archivo `app.log` como en la
 * salida estándar del proceso.
 */
const logStream = fs.createWriteStream(__dirname + "/app.log", { flags: "a" });
console.log = function (msg) {
    const formatted = `[${new Date().toISOString()}] ${msg}\n`;
    logStream.write(formatted);
    process.stdout.write(msg + "\n");
};
console.error = console.log;

// --------------------------------------------------------------------------
//  Configuración general del servidor
// --------------------------------------------------------------------------
/**
 * Puerto en el que escuchará el servidor y configuración de conexión MySQL.
 * En producción (Plesk), la variable `process.env.PORT` puede establecerse
 * automáticamente por el entorno.
 */
const PORT = process.env.PORT || 3000;

const DB_CONFIG = {
    host: "localhost",
    port: 3306,
    user: "nagufor_user",
    password: "Atmos2025Aura",
    database: "atmos_db"
};

// --------------------------------------------------------------------------
//  Inicialización de Express
// --------------------------------------------------------------------------
/**
 * Se configura el servidor Express con:
 *   - Soporte para JSON en las peticiones.
 *   - Middleware CORS para permitir peticiones externas.
 */
const app = express();
app.use(express.json());
app.use(cors());

// --------------------------------------------------------------------------
//  Comprobación de conexión MySQL y arranque del servidor
// --------------------------------------------------------------------------
/**
 * Antes de iniciar el servidor, se comprueba la conexión con la base de datos.
 * Si la conexión es correcta:
 *   - Se instancia la capa de lógica.
 *   - Se montan las rutas REST.
 *   - Se lanza el servidor HTTP.
 *
 * En caso de error, el servidor no arranca y el error se registra en `app.log`.
 */
(async () => {
    try {
        // Verificación de la conexión a la base de datos
        const conn = await mysql.createConnection(DB_CONFIG);
        await conn.query("SELECT 1");
        await conn.end();
        console.log("Conexión a MySQL verificada correctamente.");

        // Inicialización de la capa de lógica
        const logica = new Logica(DB_CONFIG);

        // Registro de las rutas REST principales
        app.use("/", reglasREST(logica));

        // Ruta raíz de diagnóstico
        app.get("/", (req, res) => {
            res.status(200).json({ status: "ok", message: "Servidor vivo" });
        });

        // Arranque del servidor
        app.listen(PORT, () => {
            console.log(`Servidor REST escuchando en el puerto ${PORT}`);
        });

    } catch (err) {
        console.error("Error al arrancar el servidor:", err);
    }
})();
