/**
 * mainServidorREST.js
 * -------------------------
 * Punto de entrada principal para levantar el servidor REST con Express.
 * - Configura Express (servidor - framework de node) y su middleware.
 * - Instancia la lógica de negocio (conexión a la base de datos MySQL en Plesk).
 * - Carga las reglas/endpoints REST definidos.
 * - Arranca el servidor escuchando en el puerto configurado.
 */

// @author: Alan Guevara Martínez

require("dotenv").config(); // Cargar variables desde .env
const express = require("express");
const cors = require("cors");

const Logica = require("./logica/Logica");
const reglasREST = require("./apiREST/ReglasREST");


// Configuración del servidor
const DB_CONFIG = {
  host: process.env.DB_HOST,
  user: process.env.DB_USER,
  password: process.env.DB_PASS,
  database: process.env.DB_NAME,
  port: process.env.DB_PORT || 3306
};

// Puerto (en local o en Plesk)
const PORT = process.env.PORT || 3000;


// Inicialización del servidor
const app = express();
app.use(express.json());
app.use(cors());


// Conexión con la capa lógica
const logica = new Logica(DB_CONFIG);
app.use("/", reglasREST(logica));

//  Ruta raíz (ping)
app.get("/", (req, res) => {
  res.status(200).json({
    status: "ok",
    message: "Servidor Atmos Biometría activo 🚀"
  });
});


// Exportar app para tests
module.exports = app;

// Arrancar servidor solo si se ejecuta directamente
if (require.main === module) {
  app.listen(PORT, () => {
    console.log(`✅ Servidor REST escuchando en http://localhost:${PORT}`);
  });
}