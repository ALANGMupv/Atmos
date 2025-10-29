/**
 * Logica.js
 * -------------------------
 * Capa de LÓGICA DE NEGOCIO.
 * Aquí se define cómo la aplicación habla con la base de datos MySQL.
 * - Se encarga de abrir la conexión (pool de conexiones).
 * - Ejecutar consultas SQL.
 * - Devolver resultados a la capa REST (ReglasREST.js).
 */

// @author: Alan Guevara Martínez

// Importamos la librería mysql2 con soporte de promesas
const mysql = require("mysql2/promise");

class Logica {
  /**
   * @param {object} config Configuración de conexión (viene del .env)
   */
  constructor(config) {
    this.config = config;
    this.pool = mysql.createPool(config);
  }

  /**
   * Guarda una nueva medida en la tabla `medida`.
   * 
   * @param {number} id_placa - ID de la placa (FK)
   * @param {number} tipo - Tipo de medida (11=CO₂, 12=Temperatura, etc.)
   * @param {number} valor - Valor numérico de la medida
   * @param {number} latitud - Coordenada (puede ser 0 por ahora)
   * @param {number} longitud - Coordenada (puede ser 0 por ahora)
   * @returns {Promise<Object>} La fila insertada
   */
  async guardarMedida(id_placa, tipo, valor, latitud = 0, longitud = 0) {
    const conn = await this.pool.getConnection();
    try {
      const sqlInsert = `
        INSERT INTO medida (id_placa, tipo, valor, latitud, longitud, fecha_hora)
        VALUES (?, ?, ?, ?, ?, NOW())
      `;
      const [resultado] = await conn.execute(sqlInsert, [
        id_placa,
        tipo,
        valor,
        latitud,
        longitud,
      ]);

      const sqlSelect = `SELECT * FROM medida WHERE id_medida = ?`;
      const [filas] = await conn.execute(sqlSelect, [resultado.insertId]);
      return filas[0];
    } finally {
      conn.release();
    }
  }

  /**
   * Devuelve las últimas medidas almacenadas.
   * @param {number} limit - número máximo de filas a devolver (por defecto 50)
   * @returns {Promise<Array>} array de medidas
   */
  async listarMedidas(limit = 50) {
    const conn = await this.pool.getConnection();
    try {
      const lim = Math.max(1, Math.min(parseInt(limit || 50, 10), 500));
      const sql = `
        SELECT id_medida, id_placa, tipo, valor, latitud, longitud, fecha_hora
        FROM medida
        ORDER BY fecha_hora DESC, id_medida DESC
        LIMIT ?
      `;
      const [filas] = await conn.execute(sql, [lim]);
      return filas;
    } finally {
      conn.release();
    }
  }
}

module.exports = Logica;