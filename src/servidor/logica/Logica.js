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

const mysql = require("mysql2/promise"); // Cliente MySQL en modo promesa
const bcrypt = require("bcrypt");        // Librería para cifrar contraseñas

class Logica {

    // --------------------------------------------------------------------------
    // Constructor
    // --------------------------------------------------------------------------
    // Descripción:
    //   Inicializa la clase y crea un pool de conexiones MySQL.
    //
    // Parámetros:
    //   - config {object} : configuración de conexión MySQL
    //                       (host, user, password, database, etc.).
    //
    // Devuelve:
    //   - Instancia inicializada de la clase Logica.
    // --------------------------------------------------------------------------
    constructor(config) {
        this.config = config;              // Guardamos la configuración por si hiciera falta más tarde
        this.pool = mysql.createPool(config); // Creamos el pool de conexiones a MySQL
    }

    // --------------------------------------------------------------------------
    // MÉTODOS DE MEDIDAS
    // --------------------------------------------------------------------------

    // --------------------------------------------------------------------------
    // Método: guardarMedida()
    // --------------------------------------------------------------------------
    // Descripción:
    //   Inserta una nueva medida en la base de datos.
    //
    // Parámetros:
    //   - id_placa {string} : identificador único del sensor/placa.
    //   - tipo {number}     : tipo de medida (ej. código de gas).
    //   - valor {number}    : valor numérico registrado.
    //   - latitud {number}  : latitud GPS (opcional, por defecto 0).
    //   - longitud {number} : longitud GPS (opcional, por defecto 0).
    //
    // Devuelve:
    //   - {Promise<Object>} : objeto con la fila insertada (medida completa).
    // --------------------------------------------------------------------------
    async guardarMedida(id_placa, tipo, valor, latitud = 0, longitud = 0) {
        // Obtenemos una conexión desde el pool
        const conn = await this.pool.getConnection();
        try {
            // SQL de inserción de una nueva medida
            const sqlInsert = `
                INSERT INTO medida (id_placa, tipo, valor, latitud, longitud, fecha_hora)
                VALUES (?, ?, ?, ?, ?, NOW())
            `;
            // Ejecutamos la inserción con los parámetros correspondientes
            const [resultado] = await conn.execute(sqlInsert, [
                id_placa,
                tipo,
                valor,
                latitud,
                longitud
            ]);

            // SQL para recuperar la fila recién insertada (por id autoincremental)
            const sqlSelect = `SELECT * FROM medida WHERE id_medida = ?`;
            // Ejecutamos la consulta usando el insertId devuelto por la inserción
            const [filas] = await conn.execute(sqlSelect, [resultado.insertId]);

            // Devolvemos el primer (y único) resultado
            return filas[0];

        } finally {
            // Liberamos la conexión al pool, ocurra lo que ocurra
            conn.release();
        }
    }

    // --------------------------------------------------------------------------
    // Método: listarMedidas()
    // --------------------------------------------------------------------------
    // Descripción:
    //   Devuelve las últimas medidas registradas, limitando el número de filas.
    //
    // Parámetros:
    //   - limit {number} : número máximo de filas a devolver (por defecto 50,
    //                      pero nunca más de 500 ni menos de 1).
    //
    // Devuelve:
    //   - {Promise<Array>} : lista de medidas ordenadas por fecha_hora DESC.
    // --------------------------------------------------------------------------
    async listarMedidas(limit = 50) {
        // Obtenemos una conexión desde el pool
        const conn = await this.pool.getConnection();
        try {
            // Normalizamos el límite: mínimo 1, máximo 500
            const lim = Math.max(1, Math.min(parseInt(limit || 50, 10), 500));

            // SQL de selección de medidas, ordenadas de más reciente a más antigua
            const sql = `
                SELECT id_medida, id_placa, tipo, valor, latitud, longitud, fecha_hora
                FROM medida
                ORDER BY fecha_hora DESC, id_medida DESC
                LIMIT ?
            `;

            // Ejecutamos la consulta pasando el límite como parámetro
            const [rows] = await conn.execute(sql, [lim]);

            // Devolvemos el array de filas
            return rows;

        } finally {
            // Liberamos la conexión
            conn.release();
        }
    }

    // --------------------------------------------------------------------------
    // MÉTODOS DE USUARIOS
    // --------------------------------------------------------------------------

    // --------------------------------------------------------------------------
    // Método: guardarUsuario()
    // --------------------------------------------------------------------------
    // Descripción:
    //   Inserta un nuevo usuario autenticado con Firebase en la tabla "usuario".
    //
    // Parámetros:
    //   - uid_firebase {string}     : identificador del usuario en Firebase.
    //   - nombre {string}          : nombre del usuario.
    //   - apellidos {string}       : apellidos del usuario.
    //   - email {string}           : correo electrónico.
    //   - contrasenaPlano {string} : contraseña sin cifrar (se cifrará con bcrypt).
    //
    // Detalles:
    //   - La contraseña se almacena cifrada.
    //   - id_rol se inicializa por defecto (1 o rolPorDefecto).
    //   - estado se inicializa a 0 (pendiente de verificación).
    //
    // Devuelve:
    //   - {Promise<Object>} : registro insertado del usuario.
    // --------------------------------------------------------------------------
    async guardarUsuario(uid_firebase, nombre, apellidos, email, contrasenaPlano) {
        // Obtenemos una conexión desde el pool
        const conn = await this.pool.getConnection();
        try {
            // Ciframos la contraseña en texto plano
            const hash = await bcrypt.hash(contrasenaPlano, 10);
            const rolPorDefecto = 1; // Rol por defecto para nuevos usuarios

            // SQL de inserción del usuario
            const sql = `
                INSERT INTO usuario (uid_firebase, nombre, apellidos, email, contrasena, id_rol, fecha_registro, estado)
                VALUES (?, ?, ?, ?, ?, ?, NOW(), 0)
            `;

            // Ejecutamos la inserción con todos los campos necesarios
            const [resultado] = await conn.execute(sql, [
                uid_firebase,
                nombre,
                apellidos,
                email,
                hash,
                rolPorDefecto
            ]);

            // Recuperamos el usuario recién insertado por su id autoincremental
            const [filas] = await conn.execute(
                "SELECT * FROM usuario WHERE id_usuario = ?",
                [resultado.insertId]
            );

            // Devolvemos el objeto usuario insertado
            return filas[0];

        } finally {
            // Liberamos la conexión
            conn.release();
        }
    }

    // --------------------------------------------------------------------------
    // Método: actualizarEstadoVerificado()
    // --------------------------------------------------------------------------
    // Descripción:
    //   Establece estado = 1 para un usuario, marcándolo como verificado
    //   tras confirmarse en Firebase.
    //
    // Parámetros:
    //   - uid_firebase {string} : identificador del usuario en Firebase.
    //
    // Devuelve:
    //   - {Promise<void>}
    // --------------------------------------------------------------------------
    async actualizarEstadoVerificado(uid_firebase) {
        // Obtenemos una conexión desde el pool
        const conn = await this.pool.getConnection();
        try {
            // Ejecutamos un UPDATE para poner estado=1 en el usuario correspondiente
            await conn.execute(
                `UPDATE usuario SET estado = 1 WHERE uid_firebase = ?`,
                [uid_firebase]
            );


            // Log informativo en servidor
            console.log("Usuario marcado como verificado en MySQL:", uid_firebase);

        } finally {
            // Liberamos la conexión
            conn.release();
        }
    }

    // --------------------------------------------------------------------------
    // Método: buscarUsuarioPorEmail()
    // --------------------------------------------------------------------------
    // Descripción:
    //   Busca un usuario por su correo electrónico.
    //
    // Parámetros:
    //   - email {string} : correo electrónico del usuario.
    //
    // Devuelve:
    //   - {Promise<Object|null>} : usuario encontrado o null si no existe.
    // --------------------------------------------------------------------------
    async buscarUsuarioPorEmail(email) {
        // Obtenemos una conexión desde el pool
        const conn = await this.pool.getConnection();
        try {
            // SQL para seleccionar un usuario por email
            const [rows] = await conn.execute(
                "SELECT * FROM usuario WHERE email = ? LIMIT 1",
                [email]
            );

            // Si hay resultado, devolvemos la primera fila; si no, null
            return rows[0] || null;

        } finally {
            // Liberamos la conexión
            conn.release();
        }
    }

    // --------------------------------------------------------------------------
    // Método: buscarUsuarioPorUID()
    // --------------------------------------------------------------------------
    // Descripción:
    //   Busca un usuario por su UID de Firebase.
    //
    // Parámetros:
    //   - uid {string} : UID del usuario en Firebase.
    //
    // Devuelve:
    //   - {Promise<Object|null>} : usuario encontrado o null si no existe.
    // --------------------------------------------------------------------------
    async buscarUsuarioPorUID(uid) {
        // Obtenemos una conexión desde el pool
        const conn = await this.pool.getConnection();
        try {
            // Seleccionamos el usuario cuyo uid_firebase coincide
            const [rows] = await conn.execute(
                "SELECT * FROM usuario WHERE uid_firebase = ? LIMIT 1",
                [uid]
            );

            // Devolvemos la fila encontrada o null si no hay
            return rows[0] || null;

        } finally {
            // Liberamos la conexión
            conn.release();
        }
    }

    // --------------------------------------------------------------------------
    // Método: actualizarUsuario()
    // Autor: Nerea Aguilar Forés
    // --------------------------------------------------------------------------
    // Descripción:
    //   Actualiza los datos de un usuario en la base de datos.
    //   Permite opcionalmente actualizar también la contraseña.
    //
    // Parámetros:
    //   - id_usuario {number} : Identificador único del usuario a actualizar.
    //   - datos {object}      : Objeto con los nuevos valores del usuario:
    //                           { nombre, apellidos, email, contrasena }
    //                           Si "contrasena" viene definido, se actualiza.
    //
    // Devuelve:
    //   - {Promise<boolean>} : true si se actualiza correctamente,
    //                          false en caso contrario.
    // --------------------------------------------------------------------------
    async actualizarUsuario(id_usuario, { nombre, apellidos, email, contrasena }) {
        // Obtenemos una conexión desde el pool
        const conn = await this.pool.getConnection();
        try {
            let sql;
            let params;

            // Si se ha proporcionado una nueva contraseña...
            if (contrasena) {
                // Actualizamos también el campo contrasena
                sql = `
                    UPDATE usuario
                    SET nombre = ?, apellidos = ?, email = ?, contrasena = ?
                    WHERE id_usuario = ?
                `;
                params = [nombre, apellidos, email, contrasena, id_usuario];
            } else {
                // Solo actualizamos nombre, apellidos y email
                sql = `
                    UPDATE usuario
                    SET nombre = ?, apellidos = ?, email = ?
                    WHERE id_usuario = ?
                `;
                params = [nombre, apellidos, email, id_usuario];
            }

            // Ejecutamos la query de actualización
            const [result] = await conn.query(sql, params);

            // Devolvemos true si se modificó al menos una fila
            return result.affectedRows > 0;

        } catch (err) {
            // Log de error para depuración
            console.error("Error actualizando usuario:", err);
            // Propagamos el error hacia arriba
            throw err;

        } finally {
            // Liberamos la conexión
            conn.release();
        }
    }

    // --------------------------------------------------------------------------
    // Método: obtenerUsuarioPorId()
    // Autor: Nerea Aguilar Forés
    // --------------------------------------------------------------------------
    // Descripción:
    //   Recupera todos los datos de un usuario a partir de su identificador.
    //   Principalmente se usa para comprobar la contraseña actual antes de
    //   permitir una actualización.
    //
    // Parámetros:
    //   - id_usuario {number} : Identificador único del usuario a buscar.
    //
    // Devuelve:
    //   - {Promise<Object|null>} : Objeto usuario si existe, o null si no se encuentra.
    // --------------------------------------------------------------------------
    async obtenerUsuarioPorId(id_usuario) {
        // Obtenemos una conexión desde el pool
        const conn = await this.pool.getConnection();
        try {
            // Seleccionamos el usuario por su id
            const [rows] = await conn.query(
                "SELECT * FROM usuario WHERE id_usuario = ?",
                [id_usuario]
            );

            // Devolvemos el primer resultado o null
            return rows[0] || null;

        } finally {
            // Liberamos la conexión
            conn.release();
        }
    }

    // --------------------------------------------------------------------------
    // Funcionalidad: Vincular una placa a un usuario
    // Autor: Nerea Aguilar Forés
    // --------------------------------------------------------------------------
    // Descripción:
    //   Asocia una placa existente a un usuario determinado en la base de datos.
    //   Si la placa ya está asignada a otro usuario, la operación no se realiza.
    //
    // Parámetros:
    //   - id_usuario {number} : ID del usuario que desea vincular la placa.
    //   - id_placa {string}   : ID o código único de la placa.
    //
    // Devuelve:
    //   - {Promise<Object>} : Objeto con el estado de la operación:
    //                         { status: "ok", mensaje: "..." } o lanza error.
    // --------------------------------------------------------------------------
    async vincularPlacaAUsuario(id_usuario, id_placa) {
        // Obtenemos una conexión desde el pool
        const conn = await this.pool.getConnection();
        try {
            // Verificamos si la placa existe y obtenemos su campo "asignada"
            const [placas] = await conn.query(
                "SELECT asignada FROM placa WHERE id_placa = ?",
                [id_placa]
            );

            // Si no hay ninguna fila, la placa no existe
            if (placas.length === 0) {
                throw new Error("Placa no encontrada");
            }

            // Si la placa ya está asignada (asignada = 1), no permitimos asignarla de nuevo
            if (placas[0].asignada === 1) {
                throw new Error("La placa ya está asignada a otro usuario");
            }

            // Insertamos la relación en la tabla puente usuario-placa
            await conn.query(
                "INSERT INTO usuarioplaca (id_placa, id_usuario) VALUES (?, ?)",
                [id_placa, id_usuario]
            );

            // Actualizamos el estado de la placa a asignada
            await conn.query(
                "UPDATE placa SET asignada = 1 WHERE id_placa = ?",
                [id_placa]
            );

            // Devolvemos un estado de éxito
            return { status: "ok", mensaje: "Placa vinculada correctamente" };

        } catch (err) {
            // Log del error para depuración
            console.error("Error vinculando placa:", err);
            // Propagamos el error hacia la capa superior
            throw err;

        } finally {
            // Liberamos la conexión
            conn.release();
        }
    }

    // --------------------------------------------------------------------------
    // Método: obtenerPlacaDeUsuario()
    // --------------------------------------------------------------------------
    // Descripción:
    //   Devuelve la placa vinculada a un usuario (si existe alguna).
    //
    // Parámetros:
    //   - id_usuario {number} : ID del usuario.
    //
    // Devuelve:
    //   - {Promise<string|null>} : id_placa vinculada o null si no tiene placas.
    // --------------------------------------------------------------------------
    async obtenerPlacaDeUsuario(id_usuario) {
        // Obtenemos una conexión desde el pool
        const conn = await this.pool.getConnection();
        try {
            // SQL para obtener la placa vinculada al usuario
            const sql = `
                SELECT id_placa
                FROM usuarioplaca
                WHERE id_usuario = ?
                LIMIT 1
            `;

            // Ejecutamos la consulta
            const [rows] = await conn.query(sql, [id_usuario]);

            // Si hay alguna fila, devolvemos el id_placa, si no, null
            return rows.length ? rows[0].id_placa : null;

        } finally {
            // Liberamos la conexión
            conn.release();
        }
    }

    // -----------------------------------------------------------------------------
    // Funcionalidad: Desvincular placa de un usuario
    // Autor: Alan Guevara Martínez
    // Fecha: 19/11/2025
    // -----------------------------------------------------------------------------
    // Descripción:
    //   - Busca las placas asociadas a un id_usuario en la tabla usuarioplaca.
    //   - Si no tiene ninguna placa → devuelve { status: "sin_placa", ... }.
    //   - Si tiene una o más:
    //        * Borra las filas de usuarioplaca para ese usuario.
    //        * Pone asignada = 0 en la tabla placa para esas placas.
    //   - Todo se realiza dentro de una transacción para asegurar consistencia.
    //
    // Parámetros:
    //   - id_usuario {number} : Identificador del usuario en la tabla usuario.
    //
    // Devuelve:
    //   - {Promise<Object>} :
    //         { status: "ok", mensaje: "Placa desvinculada correctamente" }
    //         { status: "sin_placa", mensaje: "El usuario no tiene placas vinculadas" }
    // -----------------------------------------------------------------------------
    async desvincularPlacaDeUsuario(id_usuario) {
        // Obtenemos una conexión del pool
        const conn = await this.pool.getConnection();
        try {
            // Iniciamos una transacción para que todas las operaciones sean atómicas
            await conn.beginTransaction();

            // 1) Buscar las placas asociadas a este usuario
            const [filas] = await conn.query(
                "SELECT id_placa FROM usuarioplaca WHERE id_usuario = ?",
                [id_usuario]
            );

            // Si el usuario no tiene ninguna placa vinculada
            if (filas.length === 0) {
                // Confirmamos la transacción (no hay cambios que revertir)
                await conn.commit();

                // Devolvemos un estado indicando que no hay placas
                return {
                    status: "sin_placa",
                    mensaje: "El usuario no tiene placas vinculadas"
                };
            }

            // Extraemos solo los ids de placa en un array
            const idsPlaca = filas.map(f => f.id_placa);

            // 2) Eliminar las filas de la tabla puente usuarioplaca
            await conn.query(
                "DELETE FROM usuarioplaca WHERE id_usuario = ?",
                [id_usuario]
            );

            // 3) Poner asignada = 0 para todas las placas afectadas
            // Generamos el listado de placeholders (?, ?, ?) dinámicamente
            const placeholders = idsPlaca.map(() => "?").join(",");
            const sqlUpdate = `
                UPDATE placa
                SET asignada = 0
                WHERE id_placa IN (${placeholders})
            `;

            // Ejecutamos el UPDATE pasando el array de idsPlaca
            await conn.query(sqlUpdate, idsPlaca);

            // 4) Si todo ha ido bien, confirmamos la transacción
            await conn.commit();

            // Devolvemos un estado de éxito
            return {
                status: "ok",
                mensaje: "Placa desvinculada correctamente"
            };

        } catch (err) {
            // Log de error al desvincular placas
            console.error("Error desvinculando placa de usuario:", err);

            // Intentamos revertir cualquier cambio realizado en la transacción
            try {
                await conn.rollback();
            } catch (e) {
                console.error("Error en rollback de desvinculación:", e);
            }

            // Propagamos el error hacia arriba para que lo maneje la capa REST
            throw err;

        } finally {
            // Liberamos la conexión de vuelta al pool
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
    //   - tipo {number}     : código del tipo de gas (por ejemplo 11,12,13,14).
    //
    // Devuelve:
    //   - {Promise<Object|null>} : objeto con { valor, fecha_hora },
    //                              o null si no existen mediciones.
    // --------------------------------------------------------------------------
    async obtenerUltimaMedidaPorGas(id_placa, tipo) {
        // Obtenemos una conexión desde el pool
        const conn = await this.pool.getConnection();
        try {
            // SQL que selecciona la última medida (por fecha_hora y id_medida) del tipo de gas y placa indicados
            const sql = `
                SELECT valor, fecha_hora
                FROM medida
                WHERE id_placa = ?
                  AND tipo = ?
                ORDER BY fecha_hora DESC, id_medida DESC
                LIMIT 1
            `;

            // Ejecutamos la consulta pasando la placa y el tipo de gas
            const [rows] = await conn.query(sql, [id_placa, tipo]);

            // Si hay resultados, devolvemos el primero; si no, null
            return rows.length ? rows[0] : null;

        } finally {
            // Liberamos la conexión
            conn.release();
        }
    }

    // --------------------------------------------------------------------------
    // Método: obtenerPromedioPorGasHoy()
    // --------------------------------------------------------------------------
    // Descripción:
    //   Calcula el promedio de las mediciones de un gas concreto (por tipo)
    //   registradas HOY (fecha actual del servidor) para una placa determinada.
    //
    // Parámetros:
    //   - id_placa {string} : identificador único de la placa.
    //   - tipo {number}     : código del tipo de gas (11,12,13,14).
    //
    // Devuelve:
    //   - {Promise<number|null>} : promedio numérico o null si no hay datos hoy.
    // --------------------------------------------------------------------------
    async obtenerPromedioPorGasHoy(id_placa, tipo) {
        // Obtenemos una conexión desde el pool
        const conn = await this.pool.getConnection();
        try {
            // SQL que calcula el promedio de "valor" para las filas de hoy
            const sql = `
                SELECT AVG(valor) AS promedio
                FROM medida
                WHERE id_placa = ?
                  AND tipo = ?
                  AND DATE(fecha_hora) = CURDATE()
            `;

            // Ejecutamos la consulta con los parámetros adecuados
            const [rows] = await conn.query(sql, [id_placa, tipo]);

            // Si promedio no es null, lo convertimos a Number; si no hay datos, devolvemos null
            return rows[0].promedio !== null ? Number(rows[0].promedio) : null;

        } finally {
            // Liberamos la conexión
            conn.release();
        }
    }

    // --------------------------------------------------------------------------
    // Método: obtenerPromedios7Dias()
    // --------------------------------------------------------------------------
    // Descripción:
    //   Devuelve una lista de 7 promedios, uno por cada día desde hace 6 días
    //   hasta hoy, SOLO del tipo de gas indicado para una placa.
    //   Si un día no tiene medidas → se devuelve 0.
    // --------------------------------------------------------------------------
    async obtenerPromedios7Dias(id_placa, tipo) {
        const conn = await this.pool.getConnection();
        try {
            const sql = `
				SELECT DATE(fecha_hora) AS fecha, AVG(valor) AS promedio
				FROM medida
				WHERE id_placa = ?
				  AND tipo = ?
				  AND fecha_hora >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
				GROUP BY DATE(fecha_hora)
			`;

            const [rows] = await conn.query(sql, [id_placa, tipo]);

            // Convertimos resultado a un mapa: { "2025-11-15": 0.08, ... }
            const mapa = {};
            rows.forEach(r => {
                mapa[r.fecha.toISOString().substring(0,10)] = Number(r.promedio);
            });

            // Construimos salida en orden cronológico
            const hoy = new Date();
            const valores = [];

            for (let i = 6; i >= 0; i--) {
                const d = new Date();
                d.setDate(hoy.getDate() - i);

                // Convertir fecha a AAAA-MM-DD en zona local (no UTC)
                const clave = d.getFullYear() + "-" +
                    String(d.getMonth() + 1).padStart(2, "0") + "-" +
                    String(d.getDate()).padStart(2, "0");


                valores.push(mapa[clave] ? mapa[clave] : 0);
            }

            return valores;

        } finally {
            conn.release();
        }
    }


    // --------------------------------------------------------------------------
    // Método: obtenerPromedios8HorasPorGas()
    // --------------------------------------------------------------------------
    // Descripción:
    //   Devuelve un arreglo con 8 promedios horarios del gas indicado,
    //   desde hace 7 horas hasta la hora actual.
    //
    // Parámetros:
    //   - id_placa {string} : identificador del sensor
    //   - tipo     {number} : tipo del gas
    //
    // Devuelve:
    //   - {Promise<Array<number>>} : 8 valores (hora -7 → hora actual).
    // --------------------------------------------------------------------------
    async obtenerPromedios8HorasPorGas(id_placa, tipo) {
        const conn = await this.pool.getConnection();
        try {
            const sql = `
				SELECT 
					fecha_hora,
					HOUR(fecha_hora) AS hora,
					AVG(valor) AS promedio
				FROM medida
				WHERE id_placa = ?
				  AND tipo = ?
				  AND fecha_hora >= DATE_SUB(NOW(), INTERVAL 8 HOUR)
				GROUP BY HOUR(fecha_hora)
				ORDER BY hora;
			`;

            const [rows] = await conn.query(sql, [id_placa, tipo]);

            console.log("\n\n============= DEBUG SQL (ULTIMAS 8 HORAS) =============");
            console.log("NOW() local del servidor:", new Date().toString());
            console.log("Registros encontrados por MySQL:\n", rows);

            const resultado = [];
            const ahora = new Date();

            console.log("\nHoras generadas por Node para comparar:");
            for (let i = 7; i >= 0; i--) {
                const fecha = new Date(ahora.getTime() - i * 3600000);
                const horaNode = fecha.getHours();

                console.log(` - Hora generada: ${horaNode}`);

                // Buscar en rows
                const fila = rows.find(r => Number(r.hora) === horaNode);

                if (fila) {
                    console.log(`   ✔ Coincidencia encontrada: MySQL hora=${fila.hora}, promedio=${fila.promedio}`);
                    resultado.push(Number(fila.promedio));
                } else {
                    console.log(`   ✖ Sin coincidencia para esa hora -> se añade 0`);
                    resultado.push(0);
                }
            }

            console.log("\nResultado final (8 valores):", resultado);
            console.log("======================================================\n\n");

            return resultado;

        } finally {
            conn.release();
        }
    }




}

// --------------------------------------------------------------------------
// Exportación de la clase
// --------------------------------------------------------------------------
module.exports = Logica;