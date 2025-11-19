/**
 * Logica.js
 * -------------------------
 * Capa de LÓGICA DE NEGOCIO.
 *
 * Define cómo la aplicación interactúa con la base de datos MySQL.
 * Sus responsabilidades principales son:
 *   - Crear y gestionar el pool de conexiones.
 *   - Ejecutar las consultas SQL necesarias.
 *   - Proveer métodos reutilizables para la capa REST (ReglasREST.js).
 *
 * Autores: Alan Guevara Martínez y Santiago Fuenmayor Ruiz
 */

const mysql = require("mysql2/promise");
const bcrypt = require("bcrypt");

class Logica {

    // --------------------------------------------------------------------------
    // Constructor
    // --------------------------------------------------------------------------
    // Descripción:
    //   Inicializa la clase y crea un pool de conexiones MySQL.
    //
    // Parámetros:
    //   - config {object} : configuración de conexión MySQL.
    //
    // Devuelve:
    //   - Instancia inicializada de la clase Logica.
    // --------------------------------------------------------------------------
    constructor(config) {
        this.config = config;
        this.pool = mysql.createPool(config);
    }

    // --------------------------------------------------------------------------
    // Método: guardarMedida()
    // --------------------------------------------------------------------------
    // Descripción:
    //   Inserta una nueva medida en la base de datos.
    //
    // Parámetros:
    //   - id_placa {string}   : identificador único del sensor.
    //   - tipo {number}       : tipo de medida.
    //   - valor {number}      : valor numérico registrado.
    //   - latitud {number}    : latitud GPS (opcional).
    //   - longitud {number}   : longitud GPS (opcional).
    //
    // Devuelve:
    //   - {Promise<Object>} : objeto con la fila insertada.
    // --------------------------------------------------------------------------
    async guardarMedida(id_placa, tipo, valor, latitud = 0.0, longitud = 0.0) {
        const conn = await this.pool.getConnection();
        try {
            // Insertar en base de datos
            const sqlInsert = `
                INSERT INTO medida (id_placa, tipo, valor, latitud, longitud, fecha_hora)
                VALUES (?, ?, ?, ?, ?, NOW())
            `;
            const [resultado] = await conn.execute(sqlInsert, [
                id_placa, tipo, valor, latitud, longitud
            ]);

            // Recuperar la fila recién insertada
            const sqlSelect = `SELECT * FROM medida WHERE id_medida = ?`;
            const [filas] = await conn.execute(sqlSelect, [resultado.insertId]);

            return filas[0];

        } finally {
            conn.release();
        }
    }

    // --------------------------------------------------------------------------
    // Método: listarMedidas()
    // --------------------------------------------------------------------------
    // Descripción:
    //   Devuelve las últimas medidas registradas.
    //
    // Parámetros:
    //   - limit {number} : número máximo de filas a devolver (máx. 500).
    //
    // Devuelve:
    //   - {Promise<Array>} : lista de medidas.
    // --------------------------------------------------------------------------
    async listarMedidas(limit = 50) {
        const conn = await this.pool.getConnection();
        try {
            const lim = Math.max(1, Math.min(parseInt(limit) || 50, 500));

            const sql = `
                SELECT id_medida, id_placa, tipo, valor, latitud, longitud, fecha_hora
                FROM medida
                ORDER BY fecha_hora DESC, id_medida DESC
                LIMIT ?
            `;
            const [rows] = await conn.execute(sql, [lim]);

            return rows;

        } finally {
            conn.release();
        }
    }

    // --------------------------------------------------------------------------
    // Método: guardarUsuario()
    // --------------------------------------------------------------------------
    // Descripción:
    //   Inserta un nuevo usuario autenticado con Firebase.
    //
    // Parámetros:
    //   - uid_firebase {string} : identificador del usuario en Firebase.
    //   - nombre {string}       : nombre del usuario.
    //   - apellidos {string}    : apellidos del usuario.
    //   - email {string}        : correo electrónico.
    //   - contrasenaPlano {string} : contraseña sin cifrar.
    //
    // Devuelve:
    //   - {Promise<Object>} : registro insertado del usuario.
    // --------------------------------------------------------------------------
    async guardarUsuario(uid_firebase, nombre, apellidos, email, contrasenaPlano) {

        const conn = await this.pool.getConnection();
        try {
            const hash = await bcrypt.hash(contrasenaPlano, 10);

            const sql = `
                INSERT INTO usuario (uid_firebase, nombre, apellidos, email, contrasena, id_rol, fecha_registro, estado)
                VALUES (?, ?, ?, ?, ?, 1, NOW(), 0)
            `;

            const [resultado] = await conn.execute(sql, [
                uid_firebase, nombre, apellidos, email, hash
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

    // --------------------------------------------------------------------------
    // Método: actualizarEstadoVerificado()
    // --------------------------------------------------------------------------
    // Descripción:
    //   Establece estado = 1 para un usuario verificado.
    //
    // Parámetros:
    //   - uid_firebase {string} : identificador del usuario en Firebase.
    //
    // Devuelve:
    //   - {Promise<void>}
    // --------------------------------------------------------------------------
    async actualizarEstadoVerificado(uid_firebase) {
        const conn = await this.pool.getConnection();
        try {
            await conn.execute(
                `UPDATE usuario SET estado = 1 WHERE uid_firebase = ?`,
                [uid_firebase]
            );
        } finally {
            conn.release();
        }
    }

    // --------------------------------------------------------------------------
    // Método: buscarUsuarioPorEmail()
    // --------------------------------------------------------------------------
    // Descripción:
    //   Busca un usuario por su email.
    //
    // Parámetros:
    //   - email {string}
    //
    // Devuelve:
    //   - {Promise<Object|null>} : usuario o null.
    // --------------------------------------------------------------------------
    async buscarUsuarioPorEmail(email) {
        const conn = await this.pool.getConnection();
        try {
            const [rows] = await conn.execute(
                "SELECT * FROM usuario WHERE email = ? LIMIT 1",
                [email]
            );

            return rows[0] || null;

        } finally {
            conn.release();
        }
    }

    // --------------------------------------------------------------------------
    // Método: buscarUsuarioPorUID()
    // --------------------------------------------------------------------------
    // Descripción:
    //   Busca un usuario por UID de Firebase.
    //
    // Parámetros:
    //   - uid {string}
    //
    // Devuelve:
    //   - {Promise<Object|null>}
    // --------------------------------------------------------------------------
    async buscarUsuarioPorUID(uid) {
        const conn = await this.pool.getConnection();
        try {
            const [rows] = await conn.execute(
                "SELECT * FROM usuario WHERE uid_firebase = ? LIMIT 1",
                [uid]
            );

            return rows[0] || null;

        } finally {
            conn.release();
        }
    }

    // --------------------------------------------------------------------------
    // Método: actualizarUsuario()
    // --------------------------------------------------------------------------
    // Descripción:
    //   Actualiza campos de un usuario.
    //
    // Parámetros:
    //   - id_usuario {number}
    //   - datos {object} : { nombre, apellidos, email }
    //
    // Devuelve:
    //   - {Promise<boolean>} : true si se actualizó correctamente.
    // --------------------------------------------------------------------------
    async actualizarUsuario(id_usuario, { nombre, apellidos, email }) {
        const conn = await this.pool.getConnection();
        try {
            const sql = `
                UPDATE usuario
                SET nombre = ?, apellidos = ?, email = ?
                WHERE id_usuario = ?
            `;

            const [result] = await conn.execute(sql, [
                nombre, apellidos, email, id_usuario
            ]);

            return result.affectedRows > 0;

        } finally {
            conn.release();
        }
    }

    // --------------------------------------------------------------------------
    // Método: obtenerUsuarioPorId()
    // --------------------------------------------------------------------------
    // Descripción:
    //   Recupera un usuario por su ID.
    //
    // Parámetros:
    //   - id_usuario {number}
    //
    // Devuelve:
    //   - {Promise<Object|null>}
    // --------------------------------------------------------------------------
    async obtenerUsuarioPorId(id_usuario) {
        const conn = await this.pool.getConnection();
        try {
            const [rows] = await conn.execute(
                "SELECT * FROM usuario WHERE id_usuario = ?",
                [id_usuario]
            );

            return rows[0] || null;

        } finally {
            conn.release();
        }
    }

    // --------------------------------------------------------------------------
    // Método: vincularPlacaAUsuario()
    // --------------------------------------------------------------------------
    // Descripción:
    //   Asocia una placa a un usuario.
    //
    // Parámetros:
    //   - id_usuario {number}
    //   - id_placa {string}
    //
    // Devuelve:
    //   - {Promise<Object>} : { status, mensaje }
    // --------------------------------------------------------------------------
    async vincularPlacaAUsuario(id_usuario, id_placa) {

        const conn = await this.pool.getConnection();
        try {
            // Verificar placa
            const [placas] = await conn.query(
                "SELECT asignada FROM placa WHERE id_placa = ?",
                [id_placa]
            );

            if (placas.length === 0) throw new Error("Placa no encontrada");
            if (placas[0].asignada === 1)
                throw new Error("La placa ya está asignada");

            // Insertar relación
            await conn.query(
                "INSERT INTO usuarioplaca (id_placa, id_usuario) VALUES (?, ?)",
                [id_placa, id_usuario]
            );

            // Marcar placa como asignada
            await conn.query(
                "UPDATE placa SET asignada = 1 WHERE id_placa = ?",
                [id_placa]
            );

            return { status: "ok", mensaje: "Placa vinculada correctamente" };

        } finally {
            conn.release();
        }
    }

    // --------------------------------------------------------------------------
    // Método: obtenerPlacaDeUsuario()
    // --------------------------------------------------------------------------
    // Descripción:
    //   Devuelve la placa vinculada a un usuario.
    //
    // Parámetros:
    //   - id_usuario {number}
    //
    // Devuelve:
    //   - {Promise<string|null>} : id de la placa o null.
    // --------------------------------------------------------------------------
    async obtenerPlacaDeUsuario(id_usuario) {
        const conn = await this.pool.getConnection();
        try {
            const sql = `
                SELECT id_placa
                FROM usuarioplaca
                WHERE id_usuario = ?
                LIMIT 1
            `;

            const [rows] = await conn.query(sql, [id_usuario]);

            return rows.length ? rows[0].id_placa : null;

        } finally {
            conn.release();
        }
    }

    // --------------------------------------------------------------------------
    // Método: obtenerUltimaMedidaPorGas()
    // --------------------------------------------------------------------------
    // Descripción:
    //   Recupera la última medición registrada de un TIPO DE GAS concreto para
    //   una placa determinada.
    //
    // Parámetros:
    //   - id_placa {string} : identificador único de la placa.
    //   - tipo     {number} : código del tipo de gas (11,12,13,14).
    //
    // Devuelve:
    //   - {Promise<Object|null>} : objeto con valor y fecha_hora,
    //                              o null si no existen mediciones.
    // --------------------------------------------------------------------------
        async obtenerUltimaMedidaPorGas(id_placa, tipo) {
            const conn = await this.pool.getConnection();
            try {
                const sql = `
                SELECT valor, fecha_hora
                  FROM medida
                 WHERE id_placa = ? 
                   AND tipo = ?
              ORDER BY fecha_hora DESC, id_medida DESC
                 LIMIT 1
            `;

                const [rows] = await conn.query(sql, [id_placa, tipo]);

                return rows.length ? rows[0] : null;
            } finally {
                conn.release();
            }
        }

    // --------------------------------------------------------------------------
    // Método: obtenerPromedioPorGasHoy()
    // --------------------------------------------------------------------------
    // Descripción:
    //   Calcula el promedio de las mediciones de un gas concreto (por tipo)
    //   registradas HOY para una placa determinada.
    //
    // Parámetros:
    //   - id_placa {string} : identificador único de la placa.
    //   - tipo     {number} : código del tipo de gas (11,12,13,14).
    //
    // Devuelve:
    //   - {Promise<number|null>} : promedio numérico o null si no hay datos hoy.
    // --------------------------------------------------------------------------
        async obtenerPromedioPorGasHoy(id_placa, tipo) {
            const conn = await this.pool.getConnection();
            try {
                const sql = `
                SELECT AVG(valor) AS promedio
                  FROM medida
                 WHERE id_placa = ?
                   AND tipo = ?
                   AND DATE(fecha_hora) = CURDATE()
            `;

                const [rows] = await conn.query(sql, [id_placa, tipo]);

                return rows[0].promedio !== null ? Number(rows[0].promedio) : null;
            } finally {
                conn.release();
            }
        }

    }

// --------------------------------------------------------------------------
// Exportación de la clase
// --------------------------------------------------------------------------
module.exports = Logica;
