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
                mapa[r.fecha.toISOString().substring(0, 10)] = Number(r.promedio);
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

    // --------------------------------------------------------------------------
    // Autor: Alan Guevara Martínez
    // Fecha: 20/11/2025
    // Método: guardarMedidaYActualizarDistancia()
    // --------------------------------------------------------------------------
    // Descripción:
    //   Inserta una nueva medida en la tabla "medida" (como guardarMedida),
    //   y además actualiza el campo "distancia" de la tabla "placa"
    //   usando el RSSI recibido (ahora se guarda el valor crudo).
    //
    // Parámetros:
    //   - id_placa {string} : identificador de la placa.
    //   - tipo     {number} : tipo de gas.
    //   - valor    {number} : valor medido.
    //   - latitud  {number} : latitud registrada (opcional).
    //   - longitud {number} : longitud registrada (opcional).
    //   - rssi     {number} : intensidad de señal del beacon.
    //
    // Devuelve:
    //   - {Promise<Object>} : fila insertada en "medida".
    // --------------------------------------------------------------------------
    async guardarMedidaYActualizarDistancia(
        id_placa,
        tipo,
        valor,
        latitud = 0,
        longitud = 0,
        rssi
    ) {
        const conn = await this.pool.getConnection();
        try {
            // Iniciamos transacción
            await conn.beginTransaction();

            // 1) Insertar la medida
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
            const medidaInsertada = filas[0];

            // 2) Guardar directamente el RSSI en placa.distancia
            const sqlUpdatePlaca = `
            UPDATE placa
            SET distancia = ?
            WHERE id_placa = ?
        `;
            await conn.execute(sqlUpdatePlaca, [rssi, id_placa]);

            // 3) Commit
            await conn.commit();

            return medidaInsertada;

        } catch (err) {
            // Rollback si falla
            try {
                await conn.rollback();
            } catch (e) {
                console.error("Error en rollback de guardarMedidaYActualizarDistancia:", e);
            }
            console.error("Error en guardarMedidaYActualizarDistancia:", err);
            throw err;

        } finally {
            conn.release();
        }
    }

    // --------------------------------------------------------------------------
    // Método: actualizarEstadoPlaca
    // Autor: Alan Guevara Martínez
    // Fecha: 23/11/2025
    // --------------------------------------------------------------------------
    // Descripción:
    //   Actualiza el campo "encendida" de la tabla placa según si el sensor
    //   está enviando beacons (1) o no (0).
    //
    // Parámetros:
    //   - id_placa {string}
    //   - encendida {number} → 1 o 0
    //
    // Devuelve:
    //   - {Promise<void>}
    // --------------------------------------------------------------------------
    async actualizarEstadoPlaca(id_placa, encendida) {
        const conn = await this.pool.getConnection();
        try {
            const sql = `
                UPDATE placa
                SET encendida = ?
                WHERE id_placa = ?
            `;
            await conn.execute(sql, [encendida, id_placa]);

        } catch (err) {
            console.error("Error en actualizarEstadoPlaca:", err);
            throw err;

        } finally {
            conn.release();
        }
    }

    // -----------------------------------------------------------------------------
    // Método: obtenerEstadoPlaca()
    // -----------------------------------------------------------------------------
    // Descripción:
    //     Consulta en la base de datos si una placa (sensor) está encendida o no.
    //     La tabla `placa` contiene un campo llamado `encendida` que:
    //         • vale 1 → la placa está activa / encendida
    //         • vale 0 → la placa está apagada / inactiva
    //
    //  Parámetros:
    //     - id_placa {string} : ID de la placa que queremos comprobar
    //
    //  Devuelve:
    //     - true  → si la placa está encendida (1)
    //     - false → si la placa está apagada (0)
    //     - null  → si no existe la placa
    //
    //  Notas:
    //     • Solo consulta 1 campo, así que es rápido.
    //     • Se usa en el endpoint /estadoPlaca.
    // -----------------------------------------------------------------------------
    async obtenerEstadoPlaca(id_placa) {

        const conn = await this.pool.getConnection();

        try {
            const sql = `
                SELECT encendida 
                FROM placa
                WHERE id_placa = ?
                LIMIT 1
            `;

            const [rows] = await conn.query(sql, [id_placa]);

            if (rows.length === 0) return null;

            // Devuelve true si encendida = 1, false si encendida = 0
            return rows[0].encendida === 1;

        } finally {
            conn.release();
        }
    }

    // ==========================================================================
    // Método: obtenerEstadoSenal( idUsuario )
    // --------------------------------------------------------------------------
    // Descripción:
    //   Devuelve el nivel de señal del sensor del usuario.
    //
    //   Para calcularlo:
    //      - Se obtiene la placa asignada al usuario.
    //      - Se lee el valor del campo "distancia" de la tabla placa (RSSI).
    //      - Se clasifica según tus rangos:
    //
    //          -40  a -55  → señal fuerte
    //          -56  a -70  → señal media
    //          -71  a -85  → señal baja
    //          -86  a -95  → mala señal (casi perdido)
    //
    // Parámetros:
    //   - idUsuario: ID del usuario autenticado.
    //
    // Respuesta:
    //   { rssi: number, nivel: "fuerte" | "media" | "baja" | "mala" | "sin_datos" }
    // ==========================================================================
    async obtenerEstadoSenal(idUsuario) {

        const conn = await this.pool.getConnection();

        try {
            // 1) Buscar qué placa tiene este usuario
            const sqlPlaca = `
                SELECT p.distancia
                FROM usuarioplaca up
                JOIN placa p ON p.id_placa = up.id_placa
                WHERE up.id_usuario = ?
                LIMIT 1
            `;

            const [rows] = await conn.query(sqlPlaca, [idUsuario]);

            if (rows.length === 0) {
                return { nivel: "sin_datos" };
            }

            const rssi = rows[0].distancia;

            // 2) Clasificar señal según rangos
            let nivel = "sin_datos";

            if (rssi <= -40 && rssi >= -55) {
                nivel = "fuerte";
            } else if (rssi <= -56 && rssi >= -70) {
                nivel = "media";
            } else if (rssi <= -71 && rssi >= -85) {
                nivel = "baja";
            } else if (rssi <= -86 && rssi >= -95) {
                nivel = "mala";
            }

            return { rssi, nivel };

        } finally {
            conn.release();
        }
    }

    //A partir de aqui agrego notificaciones --alex

    /* --------------------------------------------------------------------------
     * Método: obtenerUltimaMedidaPlaca()
     * --------------------------------------------------------------------------
     * Descripción:
     *   Devuelve la última medida registrada (de cualquier tipo) asociada a una
     *   placa. Útil para evaluar actividad reciente e inactividad del sensor.
     *
     * Parámetros:
     *   - id_placa {number} : identificador de la placa
     *
     * Devuelve:
     *   - {Promise<Object|null>} : la fila completa de la tabla medida o null
     * -------------------------------------------------------------------------- */
    async obtenerUltimaMedidaPlaca(id_placa) {
        const conn = await this.pool.getConnection();
        try {
            const sql = `
                SELECT *
                FROM medida
                WHERE id_placa = ?
                ORDER BY fecha_hora DESC, id_medida DESC
                LIMIT 1
            `;
            const [rows] = await conn.query(sql, [id_placa]);
            return rows.length ? rows[0] : null;
        } finally {
            conn.release();
        }
    }

    /* --------------------------------------------------------------------------
 * Método: obtenerUltimaMedidaO3()
 * --------------------------------------------------------------------------
 * Descripción:
 *   Devuelve la última medida de tipo O₃ (tipo = 13) para una placa.
 * -------------------------------------------------------------------------- */
    async obtenerUltimaMedidaO3(id_placa) {
        const TIPO_O3 = 13;
        return this.obtenerUltimaMedidaPorGas(id_placa, TIPO_O3);
    }

    /* --------------------------------------------------------------------------
     * Método: obtenerMinutosDesdeUltimaMedida()
     * --------------------------------------------------------------------------
     * Descripción:
     *   Calcula cuántos minutos han pasado desde la última medida registrada de
     *   una placa. Sirve para detectar sensores inactivos.
     *
     * Parámetros:
     *   - id_placa {number}
     *
     * Devuelve:
     *   - {Promise<number|null>} : minutos transcurridos o null si no hay datos
     * -------------------------------------------------------------------------- */
    async obtenerMinutosDesdeUltimaMedida(id_placa) {
        const conn = await this.pool.getConnection();
        try {
            const sql = `
                SELECT TIMESTAMPDIFF(MINUTE, MAX(fecha_hora), NOW()) AS minutos
                FROM medida
                WHERE id_placa = ?
            `;
            const [rows] = await conn.query(sql, [id_placa]);
            return rows[0].minutos !== null ? Number(rows[0].minutos) : null;
        } finally {
            conn.release();
        }
    }

    /* --------------------------------------------------------------------------
     * Método: contarLecturasFueraDeRango()
     * --------------------------------------------------------------------------
     * Descripción:
     *   Cuenta cuántas lecturas de un gas están fuera de un rango esperado dentro
     *   de una ventana de tiempo determinada.
     *
     * Parámetros:
     *   - id_placa     {number}
     *   - tipo         {number} : tipo de gas
     *   - minValor     {number}
     *   - maxValor     {number}
     *   - horasVentana {number} : horas hacia atrás a evaluar
     *
     * Devuelve:
     *   - {Promise<number>} : número de lecturas fuera de rango
     * -------------------------------------------------------------------------- */
    async contarLecturasFueraDeRango(id_placa, tipo, minValor, maxValor, horasVentana) {
        const conn = await this.pool.getConnection();
        try {
            const sql = `
                SELECT COUNT(*) AS num
                FROM medida
                WHERE id_placa = ?
                  AND tipo = ?
                  AND (valor < ? OR valor > ?)
                  AND fecha_hora >= DATE_SUB(NOW(), INTERVAL ? HOUR)
            `;
            const [rows] = await conn.query(sql, [
                id_placa,
                tipo,
                minValor,
                maxValor,
                horasVentana
            ]);
            return Number(rows[0].num);
        } finally {
            conn.release();
        }
    }

    /* --------------------------------------------------------------------------
     * Método: contarPicosAltosHoy()
     * --------------------------------------------------------------------------
     * Descripción:
     *   Cuenta cuántas veces un gas supera cierto umbral dentro del día actual.
     *
     * Parámetros:
     *   - id_placa     {number}
     *   - tipo         {number}
     *   - umbralAlto   {number}
     *
     * Devuelve:
     *   - {Promise<number>}
     * -------------------------------------------------------------------------- */
    async contarPicosAltosHoy(id_placa, tipo, umbralAlto) {
        const conn = await this.pool.getConnection();
        try {
            const sql = `
                SELECT COUNT(*) AS num
                FROM medida
                WHERE id_placa = ?
                  AND tipo = ?
                  AND valor > ?
                  AND DATE(fecha_hora) = CURDATE()
            `;
            const [rows] = await conn.query(sql, [id_placa, tipo, umbralAlto]);
            return Number(rows[0].num);
        } finally {
            conn.release();
        }
    }

    /* --------------------------------------------------------------------------
     * Método: obtenerDistanciaPlaca()
     * --------------------------------------------------------------------------
     * Descripción:
     *   Devuelve la última distancia registrada para una placa (si existe), usada
     *   para generar notificaciones de proximidad entre sensor y teléfono.
     *
     * Parámetros:
     *   - id_placa {number}
     *
     * Devuelve:
     *   - {Promise<string|null>}
     * -------------------------------------------------------------------------- */
    async obtenerDistanciaPlaca(id_placa) {
        const conn = await this.pool.getConnection();
        try {
            const sql = `SELECT distancia FROM placa WHERE id_placa = ? LIMIT 1`;
            const [rows] = await conn.query(sql, [id_placa]);
            return rows.length ? rows[0].distancia : null;
        } finally {
            conn.release();
        }
    }


    // --------------------------------------------------------------------------
    // Método: obtenerUltimasMedidasGlobalPorGas()
    // --------------------------------------------------------------------------
    // Descripción:
    //   Devuelve, para un TIPO DE GAS concreto, la ÚLTIMA medición registrada
    //   por cada placa en la tabla "medida".
    //
    //   Es decir, si hay 50 placas repartidas por varias ciudades, este método
    //   devuelve como máximo 50 filas: la medida más reciente de ese gas para
    //   cada id_placa, con sus coordenadas (latitud / longitud).
    //
    //   Este método está pensado para alimentar el MAPA GLOBAL de calidad de
    //   aire, donde se pintan todos los puntos de medición disponibles,
    //   independientemente del usuario que los haya generado.
    //
    // Parámetros:
    //   - tipo {number} : código del gas (11 = NO2, 12 = CO, 13 = O3, 14 = SO2).
    //
    // Devuelve:
    //   - {Promise<Array>} : lista de filas con el formato:
    //         [
    //           {
    //             id_medida,
    //             id_placa,
    //             tipo,
    //             valor,
    //             latitud,
    //             longitud,
    //             fecha_hora
    //           },
    //           ...
    //         ]
    //
    // Detalles de implementación:
    //   - Se usa una subconsulta para obtener, por cada placa, el MAX(id_medida)
    //     del tipo de gas indicado.
    //   - Después, se hace un JOIN con la tabla medida para recuperar los datos
    //     completos de esas filas "últimas".
    //   - Se ordena por fecha_hora DESC para tener primero las más recientes.
    // --------------------------------------------------------------------------
    async obtenerUltimasMedidasGlobalPorGas(tipo) {
        // Obtenemos una conexión desde el pool
        const conn = await this.pool.getConnection();
        try {
            const sql = `
                SELECT 
                    m.id_medida,
                    m.id_placa,
                    m.tipo,
                    m.valor,
                    m.latitud,
                    m.longitud,
                    m.fecha_hora
                FROM medida m
                INNER JOIN (
                    SELECT 
                        id_placa,
                        MAX(id_medida) AS max_id
                    FROM medida
                    WHERE tipo = ?
                    GROUP BY id_placa
                ) ult
                ON m.id_medida = ult.max_id
                ORDER BY m.fecha_hora DESC, m.id_medida DESC
            `;

            // Ejecutamos la consulta pasando el tipo de gas como parámetro
            const [rows] = await conn.query(sql, [tipo]);

            // Devolvemos directamente el array de filas
            return rows;

        } finally {
            // Liberamos siempre la conexión al pool
            conn.release();
        }
    }

    // --------------------------------------------------------------------------
    // Método: obtenerUltimasMedidasGlobalTodasLasPlacas()
    // Autor: Santiago Fuenmayor Ruiz
    // Fecha: 05/12/2025 (revisado)
    // --------------------------------------------------------------------------
    // Descripción:
    //   Devuelve, para CADA placa que tenga mediciones en la tabla "medida",
    //   las últimas coordenadas y el último valor de cada gas (11,12,13,14)
    //   en UNA única fila por placa.
    //
    //   Solo salen placas que tengan al menos una medida y lat/long no nulas.
    // --------------------------------------------------------------------------
    async obtenerUltimasMedidasGlobalTodasLasPlacas() {
        const conn = await this.pool.getConnection();
        try {

            const sql = `
            SELECT *
            FROM (
                SELECT 
                    p.id_placa,

                    -- Última posición registrada (cualquier tipo)
                    (
                        SELECT latitud
                        FROM medida
                        WHERE id_placa = p.id_placa
                        ORDER BY fecha_hora DESC, id_medida DESC
                        LIMIT 1
                    ) AS latitud,

                    (
                        SELECT longitud
                        FROM medida
                        WHERE id_placa = p.id_placa
                        ORDER BY fecha_hora DESC, id_medida DESC
                        LIMIT 1
                    ) AS longitud,

                    -- Último NO2 (tipo 11)
                    (
                        SELECT valor
                        FROM medida
                        WHERE id_placa = p.id_placa AND tipo = 11
                        ORDER BY fecha_hora DESC, id_medida DESC
                        LIMIT 1
                    ) AS NO2,

                    -- Último CO (tipo 12)
                    (
                        SELECT valor
                        FROM medida
                        WHERE id_placa = p.id_placa AND tipo = 12
                        ORDER BY fecha_hora DESC, id_medida DESC
                        LIMIT 1
                    ) AS CO,

                    -- Último O3 (tipo 13)
                    (
                        SELECT valor
                        FROM medida
                        WHERE id_placa = p.id_placa AND tipo = 13
                        ORDER BY fecha_hora DESC, id_medida DESC
                        LIMIT 1
                    ) AS O3,

                    -- Último SO2 (tipo 14)
                    (
                        SELECT valor
                        FROM medida
                        WHERE id_placa = p.id_placa AND tipo = 14
                        ORDER BY fecha_hora DESC, id_medida DESC
                        LIMIT 1
                    ) AS SO2

                -- ATENCIÓN: ahora partimos de MEDIDA, no de placa
                FROM (
                    SELECT DISTINCT id_placa
                    FROM medida
                ) p
            ) t
            -- Eliminamos placas sin coordenadas válidas
            WHERE t.latitud IS NOT NULL
              AND t.longitud IS NOT NULL;
        `;

            const [rows] = await conn.query(sql);
            return rows;

        } finally {
            conn.release();
        }
    }



    // --------------------------------------------------------------------------
    // Método: obtenerEstadoNodos()
    // --------------------------------------------------------------------------
    // Devuelve el estado de TODOS los nodos (placas) para el informe T019.
    // Agregado por: Alejandro V.
    // --------------------------------------------------------------------------
    async obtenerEstadoNodos(umbralInactivoMin = 5, horasError = 4, limit = 100) {
        const conn = await this.pool.getConnection();
        try {
            const lim = Math.max(1, Math.min(parseInt(limit || 100, 10), 500));

            const sqlPlacas = `
                SELECT 
                    p.id_placa,
                    p.encendida,
                    MAX(m.fecha_hora) AS ultima_medida,
                    TIMESTAMPDIFF(
                        MINUTE,
                        MAX(m.fecha_hora),
                        NOW()
                    ) AS minutos_desde_ultima
                FROM placa p
                LEFT JOIN medida m
                    ON p.id_placa = m.id_placa
                GROUP BY p.id_placa, p.encendida
                ORDER BY ultima_medida DESC
                LIMIT ?
            `;

            const [rows] = await conn.query(sqlPlacas, [lim]);
            if (!rows.length) return [];

            const sqlErrores = `
                SELECT 
                    id_placa,
                    COUNT(*) AS num_fuera_rango
                FROM medida
                WHERE fecha_hora >= DATE_SUB(NOW(), INTERVAL ? HOUR)
                  AND valor > 2300
                GROUP BY id_placa
            `;
            const [rowsErrores] = await conn.query(sqlErrores, [horasError]);

            const mapaErrores = {};
            rowsErrores.forEach(r => mapaErrores[r.id_placa] = r.num_fuera_rango);

            return rows.map(r => {
                const minutosDesdeUltima = r.ultima_medida ? Number(r.minutos_desde_ultima) : null;
                const tieneErrores = mapaErrores[r.id_placa] > 0;

                let estado = "activo";
                let tiempoProblemaMin = null;

                if (!r.ultima_medida || minutosDesdeUltima > umbralInactivoMin || r.encendida === 0) {
                    estado = "inactivo";
                    tiempoProblemaMin = minutosDesdeUltima;
                }

                if (tieneErrores) {
                    estado = "error";
                    tiempoProblemaMin = horasError * 60;
                }

                return {
                    id_placa: r.id_placa,
                    ultima_medida: r.ultima_medida,
                    minutos_desde_ultima: minutosDesdeUltima,
                    estado,
                    tiempo_problema_min: tiempoProblemaMin
                };
            });

        } finally {
            conn.release();
        }
    }
    //* --------------------------------------------------------------------------
    //* NOTIFICACIONES
    //* --------------------------------------------------------------------------
    /* --------------------------------------------------------------------------
     * Método: obtenerNotificacionesUsuarioDesdeBD()
     * Lee las notificaciones reales desde la tabla `notificacion`
     * -------------------------------------------------------------------------- */
    async obtenerNotificacionesUsuarioDesdeBD(id_usuario) {
        const conn = await this.pool.getConnection();
        try {
            const sql = `
            SELECT
                id_notificacion,
                tipo,
                titulo,
                mensaje,
                icono,
                fecha_creacion,
                estado
            FROM notificacion
            WHERE id_usuario = ?
            ORDER BY fecha_creacion DESC
            LIMIT 100
        `;
            const [rows] = await conn.query(sql, [id_usuario]);

            // Adaptamos al formato que Android ya maneja
            return rows.map(row => ({
                id_notificacion: row.id_notificacion,
                tipo: row.tipo,
                titulo: row.titulo,
                texto: row.mensaje,
                icono: row.icono,
                fecha_hora: row.fecha_creacion,
                leido: row.estado === 1
            }));

        } finally {
            conn.release();
        }
    }
    /* --------------------------------------------------------------------------
     * Método: obtenerNotificacionesUsuario()
     * --------------------------------------------------------------------------
     *
     * Simplemente devuelve las notificaciones persistidas en la tabla `notificacion`
     * para ese usuario.
     * -------------------------------------------------------------------------- */
    async obtenerNotificacionesUsuario(id_usuario) {
        // Delegamos en el método que lee directamente de la BBDD
        return await this.obtenerNotificacionesUsuarioDesdeBD(id_usuario);
    }
    /* --------------------------------------------------------------------------
     * Método: insertarNotificacion()
     * --------------------------------------------------------------------------
     * Inserta una notificación en la tabla `notificacion`.
     *
     * Params (obj):
     *  - id_usuario {number}      (requerido)
     *  - id_placa   {string|null} (opcional)
     *  - tipo       {string}      (p.ej. 'O3_CRITICO', 'SENSOR_INACTIVO')
     *  - titulo     {string}
     *  - mensaje    {string}
     *  - nivel      {string}      ('info', 'warning', 'critico')
     *  - icono      {string|null} (p.ej. 'alerta', 'desconexion')
     * -------------------------------------------------------------------------- */
    async insertarNotificacion({
        id_usuario,
        id_placa = null,
        tipo,
        titulo,
        mensaje,
        nivel = "info",
        icono = null,
        estado = 0
    }) {
        const conn = await this.pool.getConnection();
        try {
            const sql = `
                INSERT INTO notificacion
                (id_usuario, id_placa, tipo, titulo, mensaje, nivel, icono, estado, fecha_creacion)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())
            `;
            const [result] = await conn.execute(sql, [
                id_usuario,
                id_placa,
                tipo,
                titulo,
                mensaje,
                nivel,
                icono,
                estado
            ]);

            return result.insertId; // por si lo quieres usar
        } finally {
            conn.release();
        }
    }
    /* --------------------------------------------------------------------------
     * Método: obtenerNotificacionesUsuarioDesdeBD()
     * --------------------------------------------------------------------------
     * Lee las notificaciones persistidas en `notificacion` para un usuario.
     * Devuelve en el formato que espera Android.
     * -------------------------------------------------------------------------- */
    async obtenerNotificacionesUsuarioDesdeBD(id_usuario) {
        const conn = await this.pool.getConnection();
        try {
            const sql = `
                SELECT
                    id_notificacion,
                    tipo,
                    titulo,
                    mensaje,
                    icono,
                    fecha_creacion,
                    estado
                FROM notificacion
                WHERE id_usuario = ?
                ORDER BY fecha_creacion DESC, id_notificacion DESC
                LIMIT 100
            `;
            const [rows] = await conn.query(sql, [id_usuario]);

            return rows.map(row => ({
                id_notificacion: row.id_notificacion,
                tipo: row.tipo,
                titulo: row.titulo,
                texto: row.mensaje,
                icono: row.icono,
                fecha_hora: row.fecha_creacion,
                leido: row.estado === 1
            }));
        } finally {
            conn.release();
        }
    }
    /* --------------------------------------------------------------------------
     * Método: marcarNotificacionLeida()
     * --------------------------------------------------------------------------
     * Marca una notificación concreta como leída para un usuario.
     * -------------------------------------------------------------------------- */
    async marcarNotificacionLeida(id_notificacion, id_usuario) {
        const conn = await this.pool.getConnection();
        try {
            const sql = `
                UPDATE notificacion
                SET estado = 1,
                    fecha_leida = NOW()
                WHERE id_notificacion = ? AND id_usuario = ?
            `;
            const [result] = await conn.execute(sql, [id_notificacion, id_usuario]);
            return result.affectedRows > 0;
        } finally {
            conn.release();
        }
    }
    /* --------------------------------------------------------------------------
     * Método: marcarTodasNotificacionesLeidas()
     * --------------------------------------------------------------------------
     * Marca como leídas todas las notificaciones de un usuario que estén sin leer.
     * -------------------------------------------------------------------------- */
    async marcarTodasNotificacionesLeidas(id_usuario) {
        const conn = await this.pool.getConnection();
        try {
            const sql = `
                UPDATE notificacion
                SET estado = 1,
                    fecha_leida = IF(fecha_leida IS NULL, NOW(), fecha_leida)
                WHERE id_usuario = ? AND estado = 0
            `;
            const [result] = await conn.execute(sql, [id_usuario]);
            return result.affectedRows;
        } finally {
            conn.release();
        }
    }
    async borrarNotificacion(id_notificacion, id_usuario) {
        const conn = await this.pool.getConnection();
        try {
            const sql = `
                DELETE FROM notificacion
                WHERE id_notificacion = ? AND id_usuario = ?
            `;
            const [result] = await conn.execute(sql, [id_notificacion, id_usuario]);
            return result.affectedRows > 0;
        } finally {
            conn.release();
        }
    }

    async borrarNotificacionesUsuario(id_usuario) {
        const conn = await this.pool.getConnection();
        try {
            const sql = `DELETE FROM notificacion WHERE id_usuario = ?`;
            const [result] = await conn.execute(sql, [id_usuario]);
            return result.affectedRows;
        } finally {
            conn.release();
        }
    }

    /**
     * @brief Guarda o acumula la distancia diaria de un usuario.
     *
     * Inserta un nuevo registro de recorrido diario para el usuario
     * o, si ya existe un registro para la misma fecha, acumula
     * la distancia sumando los metros recorridos.
     *
     * Se utiliza principalmente al finalizar un recorrido.
     *
     * @param {number} id_usuario   Identificador del usuario.
     * @param {number} distancia_m  Distancia recorrida en metros.
     * @param {string|null} fecha   Fecha del recorrido (YYYY-MM-DD).
     *                              Si es null, se utiliza la fecha actual.
     *
     * @author Alan Guevara Martínez
     * @date   2025-12-17
     */
    async guardarRecorridoDiario(id_usuario, distancia_m, fecha = null) {

        // 1. Obtener una conexión del pool
        const conn = await this.pool.getConnection();

        try {

            /* -- VALUES define los valores a insertar:
            -- 1er ?  → id_usuario (lo pasas desde el código)
            -- 2º ?  → fecha (opcional)
            -- COALESCE(?, CURDATE()):
            --    - si el parámetro fecha NO es NULL → usa esa fecha
            --    - si es NULL → usa la fecha actual del sistema (CURDATE())
            -- 3er ? → distancia_m
            -- En caso de duplicado:
            -- - no se crea una nueva fila
            -- - se actualiza la existente
            -- - suma la distancia nueva a la ya almacenada*/

            const sql = `
            INSERT INTO recorrido_diario (id_usuario, fecha, distancia_m)
            VALUES (?, COALESCE(?, CURDATE()), ?)
            ON DUPLICATE KEY UPDATE
                distancia_m = distancia_m + VALUES(distancia_m)
        `;

            // 3. Ejecutar la consulta con parámetros
            await conn.execute(sql, [
                id_usuario,
                fecha,
                distancia_m
            ]);

        } finally {

            // 4. Liberar la conexión
            conn.release();
        }
    }

    /**
     * @brief Obtiene la distancia recorrida por un usuario hoy y ayer.
     *
     * Consulta la tabla de recorridos diarios y devuelve los metros
     * recorridos en el día actual y en el día anterior.
     *
     * Si no existen registros para alguno de los días,
     * el valor correspondiente será null.
     *
     * @param {number} id_usuario Identificador del usuario.
     *
     * @returns {Object} Objeto con las distancias:
     *                   { hoy: number|null, ayer: number|null }
     *
     * @author Alan Guevara Martínez
     * @date   2025-12-17
     */
    async obtenerRecorridoHoyYAyer(id_usuario) {

        // 1. Obtener conexión del pool
        const conn = await this.pool.getConnection();

        try {

            /*   -- Suma la distancia SOLO de las filas cuya fecha sea hoy
                -- Si la fecha no es hoy, aporta 0 a la suma
                -- El resultado se devuelve con el alias "hoy"
                --Lo mimso para ayer, el ? se le pasa desde el código (id_usuario)*/

            const sql = `
            SELECT
                SUM(CASE WHEN fecha = CURDATE() THEN distancia_m ELSE 0 END) AS hoy,
                SUM(CASE WHEN fecha = CURDATE() - INTERVAL 1 DAY THEN distancia_m ELSE 0 END) AS ayer
            FROM recorrido_diario
            WHERE id_usuario = ?
        `;

            // 3. Ejecutar la consulta
            const [rows] = await conn.execute(sql, [id_usuario]);

            // 4. Devolver resultados normalizados
            return {
                hoy: rows[0]?.hoy ?? null,
                ayer: rows[0]?.ayer ?? null
            };

        } finally {

            // 5. Liberar la conexión
            conn.release();
        }
    }

    // --------------------------------------------------------------------------
}
// --------------------------------------------------------------------------
// Exportación de la clase
// --------------------------------------------------------------------------
module.exports = Logica;