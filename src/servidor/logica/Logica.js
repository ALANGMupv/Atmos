/**
 * Logica.js
 * -------------------------
 * Capa de LÓGICA DE NEGOCIO.
 * Aquí se define cómo la aplicación habla con la base de datos MySQL.
 * - Se encarga de abrir la conexión (pool de conexiones).
 * - Ejecutar consultas SQL.
 * - Devolver resultados a la capa REST (ReglasREST.js).
 */
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

const mysql = require("mysql2/promise"); // Cliente MySQL con soporte de promesas
const bcrypt = require("bcrypt");        // Encriptación segura de contraseñas

class Logica {
    // --------------------------------------------------------------------------
    //  Constructor
    // --------------------------------------------------------------------------
    /**
     * Inicializa la clase con la configuración de conexión a la base de datos.
     * Crea un pool de conexiones para optimizar el rendimiento y la concurrencia.
     *
     * @param {object} config - Objeto con la configuración de conexión a MySQL.
     */
    constructor(config) {
        this.config = config;
        this.pool = mysql.createPool(config);
    }

    // --------------------------------------------------------------------------
    //  Método: guardarMedida()
    // --------------------------------------------------------------------------
    /**
     * Inserta una nueva medida en la tabla `medida`.
     *
     * @param {string} id_placa   - Identificador único del sensor (UUID del beacon).
     * @param {number} tipo       - Tipo de medida (ejemplo: 11 = CO₂, 12 = temperatura...).
     * @param {number} valor      - Valor numérico medido.
     * @param {number} latitud    - Coordenada de latitud (por defecto 0.0).
     * @param {number} longitud   - Coordenada de longitud (por defecto 0.0).
     *
     * @returns {Promise<Object>} Objeto con la fila insertada en la base de datos.
     */
    async guardarMedida(id_placa, tipo, valor, latitud = 0.0, longitud = 0.0) {
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

            // Recuperar la fila recién insertada
            const sqlSelect = `SELECT * FROM medida WHERE id_medida = ?`;
            const [filas] = await conn.execute(sqlSelect, [resultado.insertId]);
            return filas[0];
        } finally {
            conn.release();
        }
    }

    // --------------------------------------------------------------------------
    //  Método: listarMedidas()
    // --------------------------------------------------------------------------
    /**
     * Devuelve una lista de las últimas medidas registradas en la base de datos.
     * Limita el número máximo de resultados a 500 para evitar sobrecargas.
     *
     * @param {number} [limit=50] - Número máximo de filas a devolver (máximo 500).
     * @returns {Promise<Array>} Array de objetos que representan las medidas.
     */
    async listarMedidas(limit = 50) {
        const conn = await this.pool.getConnection();
        try {
            const lim = Math.max(1, Math.min(parseInt(limit || 50, 10), 500));
            console.log("SQL LIMIT calculado =", lim);

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
    //  Método: guardarUsuario()
    // --------------------------------------------------------------------------
    /**
     * Inserta un nuevo usuario autenticado con Firebase en la tabla `usuario`.
     *
     * Guarda además un hash bcrypt de su contraseña para tener respaldo local.
     * El rol por defecto asignado es 1 (usuario estándar).
     *
     * @param {string} uid_firebase   - UID único del usuario en Firebase Authentication.
     * @param {string} nombre         - Nombre del usuario.
     * @param {string} apellidos      - Apellidos del usuario.
     * @param {string} email          - Correo electrónico del usuario.
     * @param {string} contrasenaPlano - Contraseña en texto plano (para generar el hash).
     *
     * @returns {Promise<Object>} Objeto con los datos del usuario insertado.
     */
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
                "SELECT * FROM usuario WHERE id_Usuario = ?",
                [resultado.insertId]
            );
            return filas[0];
        } finally {
            conn.release();
        }
    }

    // --------------------------------------------------------------------------
    //  Método: actualizarEstadoVerificado()
    // --------------------------------------------------------------------------
    /**
     * Marca al usuario como verificado (estado = 1) en MySQL
     * a partir de su UID de Firebase.
     *
     * @param {string} uid_firebase - UID de Firebase del usuario.
     */
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

    // --------------------------------------------------------------------------
    //  Método: buscarUsuarioPorEmail()
    // --------------------------------------------------------------------------
    /**
     * Busca un usuario existente en la base de datos por su correo electrónico.
     *
     * @param {string} email - Correo electrónico del usuario a buscar.
     * @returns {Promise<Object|undefined>} Objeto usuario si existe, o undefined si no.
     */
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
}

// --------------------------------------------------------------------------
//  Exportación de la clase
// --------------------------------------------------------------------------
/**
 * Exporta la clase `Logica` para ser utilizada por `mainServidorREST.js`
 * y otros módulos que requieran acceso a las operaciones de base de datos.
 */
module.exports = Logica;

// @authors: Alan Guevara Martínez y Santiago Fuenmayor Ruiz


// Importamos la librería mysql2 con soporte de promesas
const mysql = require("mysql2/promise");

// Importamos la librería bcrypt para generar y comparar los hashes que encriptan las contraseñas de los usuarios
const bcrypt = require("bcrypt");


class Logica {
    /**
     * Constructor de la clase Logica.
     * @param {object} config - Objeto con la configuración de conexión a MySQL.
     */
    constructor(config) {
        this.config = config;                 // Guardamos la configuración
        this.pool = mysql.createPool(config); // Creamos el pool de conexiones
    }

    /**
     * Guardar una medida en la tabla `medidas`.
     * 
     * @param {string} uuid - Identificador único del dispositivo/sensor.
     * @param {number} gas - Tipo de gas medido (ej. CO2, CH4, etc).
     * @param {number} valor - Valor numérico de la medida.
     * @param {number} contador - Número de medida (contador del dispositivo).
     * 
     * @return {Promise<Object>} - Devuelve la fila insertada.
     */

    // (uuid: Texto, gas: Z, valor: Z, contador: Z) → guardarMedida() → { id: Z, uuid: Texto, gas: Z, valor: Z, contador: Z, fecha: Texto }
    async guardarMedida(uuid, gas, valor, contador) {
        // Conexión desde el pool
        const conn = await this.pool.getConnection();
        try {
            // Insertar medida
            const sqlInsert = `
                INSERT INTO medidas (uuid, gas, valor, contador, fecha)
                VALUES (?, ?, ?, ?, NOW())
            `;
            const [resultado] = await conn.execute(sqlInsert, [uuid, gas, valor, contador]);

            // Recuperar la fila recién insertada (usamos el id autoincremental)
            const sqlSelect = `SELECT * FROM medidas WHERE id = ?`;
            const [filas] = await conn.execute(sqlSelect, [resultado.insertId]);

            return filas[0]; // Devolvemos el objeto con los datos insertados, array de objetos. Es un feedback inmediato para el cliente de la API.
        } finally {
            conn.release(); // Liberamos la conexión al pool
        }
    }

    /**
     * Listar medidas desde la BD.
     * 
     * @param {number} limit - número máximo de filas a devolver (por defecto 50).
     * @return {Promise<Array>} - Devuelve un array de filas con las medidas.
     */

    // (limit: Z) → listarMedidas() → [ { id: Z, uuid: Texto, gas: Z, valor: Z, contador: Z, fecha: Texto } ]
    async listarMedidas(limit = 50) { // Si alguien llama a /api/medidas sin parámetro, el backend responde con 50
        const conn = await this.pool.getConnection();
        try {
            // Seguridad: limit entre 1 y 500 (no le vamos a cumplir el deseo al iluminado que pida 1000 en el front)
            const lim = Math.max(1, Math.min(parseInt(limit || 50, 10), 500));
            console.log("SQL LIMIT calculado =", lim);

            const sql = `
                SELECT id, uuid, gas, valor, contador, fecha
                  FROM medidas
              ORDER BY fecha DESC, id DESC
                 LIMIT ?
            `;
            const [filas] = await conn.execute(sql, [lim]);
            return filas;
        } finally {
            conn.release();
        }
    }

    /**
    * Guardar usuario con modelo híbrido (Firebase + copia local)
    * -----------------------------------------------------------
    * Guarda en la tabla `Usuario` un usuario autenticado por Firebase,
    * junto con un hash bcrypt local de su contraseña para resiliencia.
    *
    * @param {string} uid_firebase - UID único de Firebase Authentication
    * @param {string} nombre - Nombre del usuario
    * @param {string} email - Correo del usuario
    * @param {string} contrasenaPlano - Contraseña en texto plano para generar el hash local
    * @return {Promise<Object>} - Objeto con los datos del usuario insertado
    */
    async guardarUsuario(uid_firebase, nombre, apellidos, email, contrasenaPlano) {
        const conn = await this.pool.getConnection();
        try {
            // Generamos hash local de la contraseña (bcrypt con factor 10)
            const hash = await bcrypt.hash(contrasenaPlano, 10);
            const rolPorDefecto = 1;

            const sql = `
                INSERT INTO Usuario (uid_firebase, nombre, apellidos, email, contrasena, id_rol, fecha_registro, estado)
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
                "SELECT * FROM Usuario WHERE id_Usuario = ?",
                [resultado.insertId]
            );

            return filas[0];
        } finally {
            conn.release();
        }
    }

    /**
     * actualizarEstadoVerificado(uid_firebase)
     * -----------------------------------------
     * Marca el usuario como verificado (estado = 1) en MySQL
     * según su UID de Firebase.
     */
    async actualizarEstadoVerificado(uid_firebase) {
        const conn = await this.pool.getConnection();
        try {
            const sql = `
            UPDATE Usuario
               SET estado = 1
             WHERE uid_firebase = ?
        `;
            await conn.execute(sql, [uid_firebase]);
            console.log("Usuario marcado como verificado en MySQL:", uid_firebase);
        } finally {
            conn.release();
        }
    }


    /**
     * Buscar usuario por email
     * -----------------------------------------------------------
     * Permite comprobar si un usuario ya existe en la base de datos.
     *
     * @param {string} email - Correo electrónico del usuario
     * @return {Promise<Object|undefined>} - Objeto usuario si existe, o undefined si no.
     */
    async buscarUsuarioPorEmail(email) {
        const conn = await this.pool.getConnection();
        try {
            const [filas] = await conn.execute(
                "SELECT * FROM Usuario WHERE email = ? LIMIT 1",
                [email]
            );
            return filas[0];
        } finally {
            conn.release();
        }
    }


}

// Exportamos la clase para usarla en mainServidorREST.js
module.exports = Logica;
