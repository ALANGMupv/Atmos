/**
 * Logica.js
 * -------------------------
 * Capa de LÓGICA DE NEGOCIO.
 * Define cómo la aplicación interactúa con la base de datos MySQL.
 *
 * Autores: Alan Guevara Martínez y Santiago Fuenmayor Ruiz
 */

const mysql = require("mysql2/promise");
const bcrypt = require("bcrypt");

class Logica {
    // --------------------------------------------------------------------------
    // Constructor
    // --------------------------------------------------------------------------
    constructor(config) {
        this.config = config;
        this.pool = mysql.createPool(config);
    }

    // --------------------------------------------------------------------------
    // MÉTODOS DE MEDIDAS
    // --------------------------------------------------------------------------
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
                longitud
            ]);

            const sqlSelect = `SELECT * FROM medida WHERE id_medida = ?`;
            const [filas] = await conn.execute(sqlSelect, [resultado.insertId]);
            return filas[0];
        } finally {
            conn.release();
        }
    }

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

    // --------------------------------------------------------------------------
    // MÉTODOS DE USUARIOS
    // --------------------------------------------------------------------------
    async guardarUsuario(uid_firebase, nombre, apellidos, email, contrasenaPlano) {
        const conn = await this.pool.getConnection();
        try {
            const hash = await bcrypt.hash(contrasenaPlano, 10);
            const rolPorDefecto = 1;

            const sql = `
        INSERT INTO usuario (uid_firebase, nombre, apellidos, email, contrasena, id_rol, fecha_registro, estado)
        VALUES (?, ?, ?, ?, ?, ?, NOW(), 0)
      `;
            const [resultado] = await conn.execute(sql, [
                uid_firebase,
                nombre,
                apellidos,
                email,
                hash,
                rolPorDefecto
            ]);

            const [filas] = await conn.execute(
                "SELECT * FROM usuario WHERE id_usuario = ?",
                [resultado.insertId]
            );
            return filas[0];
        } finally {
            conn.release();
        }
    }

    async actualizarEstadoVerificado(uid_firebase) {
        const conn = await this.pool.getConnection();
        try {
            const sql = `
        UPDATE usuario
        SET estado = 1
        WHERE uid_firebase = ?
      `;
            await conn.execute(sql, [uid_firebase]);
            console.log("Usuario marcado como verificado en MySQL:", uid_firebase);
        } finally {
            conn.release();
        }
    }

    async buscarUsuarioPorEmail(email) {
        const conn = await this.pool.getConnection();
        try {
            const [filas] = await conn.execute(
                "SELECT * FROM usuario WHERE email = ? LIMIT 1",
                [email]
            );
            return filas[0];
        } finally {
            conn.release();
        }
    }

    // --------------------------------------------------------------------------
    // MÉTODOS NUEVOS: actualización y verificación de contraseña
    // --------------------------------------------------------------------------
    async actualizarUsuario(id_usuario, { nombre, apellidos, email, contrasena }) {
        const conn = await this.pool.getConnection();
        try {
            let sql, params;
            if (contrasena) {
                sql =
                    "UPDATE usuario SET nombre=?, apellidos=?, email=?, contrasena=? WHERE id_usuario=?";
                params = [nombre, apellidos, email, contrasena, id_usuario];
            } else {
                sql =
                    "UPDATE usuario SET nombre=?, apellidos=?, email=? WHERE id_usuario=?";
                params = [nombre, apellidos, email, id_usuario];
            }

            const [result] = await conn.query(sql, params);
            return result.affectedRows > 0;
        } catch (err) {
            console.error("Error actualizando usuario:", err);
            throw err;
        } finally {
            conn.release();
        }
    }

    async obtenerUsuarioPorId(id_usuario) {
        const conn = await this.pool.getConnection();
        try {
            const [rows] = await conn.query(
                "SELECT * FROM usuario WHERE id_usuario = ?",
                [id_usuario]
            );
            return rows[0] || null;
        } finally {
            conn.release();
        }
    }
}

module.exports = Logica;
