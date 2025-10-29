// ReglasREST.js
// Define los endpoints REST de la API.
// Importante: aquí NO se hacen consultas SQL directamente.
// Este archivo solo:
// - Recibe peticiones HTTP (POST, GET, etc.).
// - Valida datos de entrada.
// - Llama a la capa de lógica (Logica.js), que es la que realmente accede a la BD.

// @author: Alan Guevara Martínez

const express = require("express");

// logica: Logica → reglasREST() → router : Router
function reglasREST(logica) {
  // Creamos un router, un mini-servidor de Express donde definiremos las rutas
  const router = express.Router();

  // Endpoint: POST /medida
  // Espera un JSON en el body con esta estructura:
  // { id_placa, tipo, valor, latitud, longitud }
  //
  // - id_placa: identificador de la placa (clave foránea)
  // - tipo: tipo de medida (11=CO₂, 12=Temperatura, etc.)
  // - valor: valor numérico de la medida
  // - latitud y longitud: opcionales, se pueden omitir
  //
  // Respuesta:
  // - Si falta algún campo obligatorio → error 400
  // - Si todo va bien → status: "ok" + medida guardada en la BD
  router.post("/medida", async (req, res) => {
    try {
      const { id_placa, tipo, valor, latitud, longitud } = req.body;

      // Validación de campos obligatorios
      if (!id_placa || tipo === undefined || valor === undefined) {
        return res.status(400).json({
          status: "error",
          mensaje: "Faltan campos obligatorios: id_placa, tipo o valor",
        });
      }

      // Llamamos a la lógica de negocio para guardar la medida
      const medidaInsertada = await logica.guardarMedida(
        id_placa,
        tipo,
        valor,
        latitud ?? 0,
        longitud ?? 0
      );

      // Respondemos al cliente confirmando el guardado
      res.json({
        status: "ok",
        medida: medidaInsertada,
      });
    } catch (err) {
      console.error("Error en POST /medida:", err);
      res.status(500).json({
        status: "error",
        mensaje: "Error interno en el servidor",
        detalle: err.message,
      });
    }
  });

  // Endpoint: GET /medidas
  // Devuelve las últimas medidas guardadas en la BD.
  // Ejemplo: GET /medidas?limit=20
  router.get("/medidas", async (req, res) => {
    try {
      const { limit } = req.query; // recogemos el query param ?limit=...
      console.log("GET /medidas con limit =", limit);
      const filas = await logica.listarMedidas(limit);

      res.json({
        status: "ok",
        medidas: filas,
      });
    } catch (err) {
      console.error("Error en GET /medidas:", err);
      res.status(500).json({
        status: "error",
        mensaje: "Error interno al obtener medidas",
      });
    }
  });

  // Devolvemos el router con todas las rutas definidas
  return router;
}

// Exportamos la función para que mainServidorREST.js pueda usarla
module.exports = reglasREST;
