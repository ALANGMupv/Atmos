/**
 * ReglasREST.js
 * -------------------------
 * Define los endpoints REST de la API.
 *
 * Aquí NO se ejecutan consultas SQL directamente.
 * Este archivo se encarga de:
 *   - Recibir las peticiones HTTP (POST, GET...).
 *   - Validar los datos de entrada.
 *   - Llamar a la capa de lógica (Logica.js), que gestiona MySQL.
 *
 * Autores: Alan Guevara Martínez y Santiago Fuenmayor Ruiz
 */

const express = require("express");
const admin = require("../firebase/firebaseAdmin"); // SDK Admin de Firebase para validar tokens
const bcrypt = require("bcrypt");                   // Para futuras validaciones locales

/**
 * Crea y devuelve un router de Express con las rutas REST definidas.
 *
 * @param {Logica} logica - Instancia de la capa de negocio (conexión MySQL y funciones).
 * @returns {Router} - Objeto Router de Express con las rutas activas.
 */
function reglasREST(logica) {
    const router = express.Router(); // "Mini servidor" con las rutas REST

    // --------------------------------------------------------------------------
    //  Middleware: verificarToken
    // --------------------------------------------------------------------------
    /**
     * Verifica el token de Firebase enviado en el header Authorization.
     * Si el token es válido, añade los datos del usuario a req.user.
     * Si es inválido o falta, responde con error 401 o 403.
     */
    async function verificarToken(req, res, next) {
        try {
            const authHeader = req.headers.authorization;

            if (!authHeader || !authHeader.startsWith("Bearer ")) {
                return res.status(401).json({ error: "Token no proporcionado" });
            }

            const idToken = authHeader.split(" ")[1];
            const decoded = await admin.auth().verifyIdToken(idToken);

            req.user = decoded; // Contiene uid, email, etc.
            next();
        } catch (err) {
            console.error("Error verificando token Firebase:", err);
            return res.status(403).json({ error: "Token inválido o expirado" });
        }
    }

    // --------------------------------------------------------------------------
    //  Endpoint: POST /medida
    // --------------------------------------------------------------------------
    /**
     * Inserta una nueva medida en la base de datos.
     *
     * Cuerpo JSON esperado:
     * {
     *   "id_placa": "UUID-del-sensor",
     *   "tipo": 11,
     *   "valor": 412.7,
     *   "latitud": 0.0,
     *   "longitud": 0.0
     * }
     *
     * Respuestas posibles:
     *   200: { status: "ok", medida: {...} }
     *   400: Faltan campos obligatorios
     *   500: Error interno del servidor
     */
    router.post("/medida", async (req, res) => {
        try {
            const { id_placa, tipo, valor, latitud, longitud } = req.body;

            if (!id_placa || tipo === undefined || valor === undefined) {
                return res.status(400).json({
                    status: "error",
                    mensaje: "Faltan campos obligatorios: id_placa, tipo o valor"
                });
            }

            const medidaInsertada = await logica.guardarMedida(
                id_placa,
                tipo,
                valor,
                latitud || 0.0,
                longitud || 0.0
            );

            res.json({
                status: "ok",
                medida: medidaInsertada
            });
        } catch (err) {
            console.error("Error en POST /medida:", err);
            res.status(500).json({
                status: "error",
                mensaje: "Error interno del servidor",
                detalle: err.message
            });
        }
    });

    // --------------------------------------------------------------------------
    //  Endpoint: GET /medidas
    // --------------------------------------------------------------------------
    /**
     * Devuelve las últimas medidas registradas en la base de datos.
     *
     * Parámetro opcional: ?limit=N (máx. 500)
     *
     * Ejemplo: GET /medidas?limit=20
     *
     * Respuestas posibles:
     *   200: { status: "ok", medidas: [...] }
     *   500: Error interno
     */
    router.get("/medidas", async (req, res) => {
        try {
            const { limit } = req.query;
            console.log("GET /medidas con limit =", limit);
            const filas = await logica.listarMedidas(limit);

            res.json({
                status: "ok",
                medidas: filas
            });
        } catch (err) {
            console.error("Error en GET /medidas:", err);
            res.status(500).json({
                status: "error",
                mensaje: "Error interno del servidor"
            });
        }
    });

    // --------------------------------------------------------------------------
    //  Endpoint: POST /usuario
    // --------------------------------------------------------------------------
    /**
     * Registra un nuevo usuario autenticado por Firebase en la base de datos.
     *
     * Header: Authorization: Bearer <idTokenFirebase>
     * Body JSON esperado: { nombre, apellidos, contrasena }
     *
     * Flujo:
     *  1. Verifica el token Firebase (uid, email).
     *  2. Comprueba si el usuario ya existe.
     *  3. Inserta el nuevo usuario si no existe.
     *  4. Marca estado = 1 si el email ya está verificado.
     *
     * Respuestas posibles:
     *   200: Usuario registrado correctamente.
     *   400: Faltan campos.
     *   409: Usuario ya existente.
     *   500: Error interno o rollback de Firebase.
     */
    router.post("/usuario", verificarToken, async (req, res) => {
        try {
            console.log("POST /usuario recibido:", req.body);

            const { nombre, apellidos, contrasena } = req.body;
            const email = req.user.email;
            const uid = req.user.uid;
            const emailVerificado = req.user.email_verified || false;

            if (!nombre || !apellidos || !contrasena) {
                return res.status(400).json({
                    error: "Faltan campos: nombre, apellidos y/o contrasena"
                });
            }

            // Comprobar si el usuario ya existe
            const usuarioExistente = await logica.buscarUsuarioPorEmail(email);
            if (usuarioExistente) {
                return res.status(409).json({ error: "El usuario ya existe" });
            }

            // Guardar el nuevo usuario
            const nuevoUsuario = await logica.guardarUsuario(
                uid,
                nombre,
                apellidos,
                email,
                contrasena
            );

            // Si el correo ya está verificado en Firebase, marcar como activo
            if (emailVerificado) {
                await logica.actualizarEstadoVerificado(uid);
            }

            res.json({
                status: "ok",
                usuario: nuevoUsuario
            });
        } catch (err) {
            console.error("Error en POST /usuario:", err);

            // Rollback: eliminar usuario en Firebase si MySQL falló
            try {
                await admin.auth().deleteUser(req.user.uid);
                console.log("Rollback: usuario eliminado de Firebase por error en MySQL.");
            } catch (rollbackError) {
                console.error("Error eliminando usuario en Firebase:", rollbackError);
            }

            res.status(500).json({
                error: "Error interno del servidor. Registro revertido."
            });
        }
    });

    // -----------------------------------------------------------------------------
    // Endpoint: PUT /usuario
    // Autor: Nerea Aguilar Forés
    // -----------------------------------------------------------------------------
    // Descripción:
    // Actualiza datos si la contraseña actual es correcta
    // -----------------------------------------------------------------------------
    router.put("/usuario", async (req, res) => {
        try {
            const { id_usuario, nombre, apellidos, email, contrasena_actual, nueva_contrasena } = req.body;

            if (!id_usuario) {
                return res.status(400).json({ error: "Faltan datos obligatorios" });
            }

            // Obtener usuario
            const usuario = await logica.obtenerUsuarioPorId(id_usuario);
            if (!usuario) return res.status(404).json({ error: "Usuario no encontrado" });

            // Actualizar usuario
            const actualizado = await logica.actualizarUsuario(id_usuario, {
                nombre,
                apellidos,
                email,
            });

            if (actualizado) res.json({ status: "ok" });
            else res.status(500).json({ error: "No se pudo actualizar" });

        } catch (e) {
            console.error(e);
            res.status(500).json({ error: "Error interno al actualizar usuario" });
        }
    });

    // -----------------------------------------------------------------------------
    // Endpoint: POST /vincular
    // Autor: Nerea Aguilar Forés
    // -----------------------------------------------------------------------------
    // Descripción:
    //   Permite a un usuario vincular una placa a su cuenta.
    //   Valida los datos recibidos.
    //
    // Body esperado (JSON):
    //   {
    //     "id_usuario": 6,
    //     "id_placa": "placa1"
    //   }
    //
    // Respuesta posible:
    //    200: { status: "ok", mensaje: "Placa vinculada correctamente" }
    //    400: Faltan datos.
    //    500: Error interno del servidor.
    // -----------------------------------------------------------------------------
    router.post("/vincular", async (req, res) => {
        try {
            const { id_usuario, id_placa } = req.body;

            if (!id_usuario || !id_placa) {
                return res.status(400).json({ error: "Faltan datos: id_usuario o id_placa" });
            }

            const resultado = await logica.vincularPlacaAUsuario(id_usuario, id_placa);
            res.json(resultado);

        } catch (err) {
            console.error("Error en POST /vincular:", err);
            res.status(500).json({ error: err.message });
        }
    });

// -------------------------------------------------------------
// Endpoint: POST /login
// Descripción:
//   - Este endpoint valida el token de Firebase enviado por el frontend.
//   - Si el token es válido, Firebase devuelve información del usuario (email, uid).
//   - Con ese email buscamos el usuario en la base de datos MySQL.
//   - Si existe, retornamos sus datos para crear la sesión PHP.
//   - Si no existe, devolvemos un error.
// -------------------------------------------------------------
    router.post("/login", verificarToken, async (req, res) => {
        try {
            // ---------------------------------------------------------------------
            // req.user lo rellena el middleware verificarToken.
            // Aquí tenemos: uid, email, email_verified...
            // ---------------------------------------------------------------------
            const email = req.user.email;

            // ---------------------------------------------------------------------
            // Buscar en MySQL el usuario cuyo email coincide con el de Firebase.
            // Si no existe, significa que está registrado en Firebase pero
            // NO se ha creado en MySQL todavía (caso raro pero posible).
            // ---------------------------------------------------------------------
            const usuario = await logica.buscarUsuarioPorEmail(email);

            if (!usuario) {
                return res.status(404).json({
                    error: "Usuario no registrado en MySQL"
                });
            }

            // ---------------------------------------------------------------------
            // Devolver al frontend los datos necesarios para:
            //   - guardar la sesión en PHP (guardarSesion.php)
            //   - sincronizar contraseña si procede
            // ---------------------------------------------------------------------
            return res.json({
                status: "ok",
                usuario: {
                    id_usuario: usuario.id_Usuario,
                    nombre: usuario.nombre,
                    apellidos: usuario.apellidos,
                    email: usuario.email
                }
            });

        } catch (error) {
            // ---------------------------------------------------------------------
            // Si ocurre cualquier problema en la lógica interna, logueamos el error
            // y devolvemos un 500 al cliente.
            // ---------------------------------------------------------------------
            console.error("Error en POST /login:", error);
            return res.status(500).json({ error: "Error interno en login" });
        }
    });

// -----------------------------------------------------------------------------
// Endpoint: POST /desvincular
// Autor: Alan Guevara Martínez
// Fecha: 19/11/2025
// -----------------------------------------------------------------------------
// Descripción:
//   - Desvincula la placa asociada al usuario indicado.
//   - Internamente llama a logica.desvincularPlacaDeUsuario(id_usuario).
//
// Body esperado (JSON):
//   {
//     "id_usuario": 6
//   }
//
// Respuestas posibles:
//   200: { status: "ok", mensaje: "Placa desvinculada correctamente" }
//   200: { status: "sin_placa", mensaje: "El usuario no tiene placas vinculadas" }
//   400: { error: "Faltan datos: id_usuario" }
//   500: { error: "..." }
// -----------------------------------------------------------------------------
    router.post("/desvincular", async (req, res) => {
        try {
            const { id_usuario } = req.body;

            // Validación básica del body
            if (!id_usuario) {
                return res.status(400).json({
                    error: "Faltan datos: id_usuario"
                });
            }

            // Llamamos a la lógica de negocio
            const resultado = await logica.desvincularPlacaDeUsuario(id_usuario);

            // devolvemos tal cual el objeto { status, mensaje }
            return res.json(resultado);

        } catch (err) {
            console.error("Error en POST /desvincular:", err);
            return res.status(500).json({
                error: "Error interno al desvincular placa"
            });
        }
    });


    // -----------------------------------------------------------------------------
    // Endpoint: GET /resumenUsuarioPorGas
    // Autor: Santiago Fuenmayor Ruiz
    // Fecha: 19/11/2025
    // -----------------------------------------------------------------------------
    // Descripción:
    //   Devuelve la última medición y el promedio del día para un TIPO DE GAS
    //   específico perteneciente a la placa vinculada del usuario.
    //
    // Parámetros esperados (query):
    //   - id_usuario {number} : ID del usuario logueado.
    //   - tipo       {number} : tipo del gas (11 = NO₂, 12 = CO, 13 = O₃, 14 = SO₂)
    //
    // Devuelve:
    //   - { status: "sin_placa" }
    //   - {
    //       status: "con_placa",
    //       id_placa,
    //       tipo,
    //       ultima_medida: { valor, fecha_hora } | null,
    //       promedio: number | 0
    //     }
    // -----------------------------------------------------------------------------
    router.get("/resumenUsuarioPorGas", async (req, res) => {
        try {
            const id_usuario = req.query.id_usuario;
            const tipo = parseInt(req.query.tipo, 10);

            // Validación de parámetros
            if (!id_usuario || !tipo) {
                return res.status(400).json({
                    error: "Faltan datos: id_usuario o tipo"
                });
            }

            // 1. Obtener placa del usuario
            const placa = await logica.obtenerPlacaDeUsuario(id_usuario);

            if (!placa) {
                return res.json({status: "sin_placa"});
            }

            // 2. Obtener última medición del tipo solicitado
            const ultima = await logica.obtenerUltimaMedidaPorGas(placa, tipo);

            // 3. Obtener promedio del día del tipo solicitado
            const promedio = await logica.obtenerPromedioPorGasHoy(placa, tipo);

            // 4. Respuesta final
            return res.json({
                status: "con_placa",
                id_placa: placa,
                tipo: tipo,
                ultima_medida: ultima,
                promedio: promedio || 0
            });

        } catch (err) {
            console.error("Error en GET /resumenUsuarioPorGas:", err);
            res.status(500).json({error: "Error interno del servidor"});
        }
    });

    // -----------------------------------------------------------------------------
    // Endpoint: GET /resumen7Dias
    // -----------------------------------------------------------------------------
    // Devuelve promedios diarios de los últimos 7 días del gas seleccionado.
    // -----------------------------------------------------------------------------
    router.get("/resumen7Dias", async (req, res) => {
        try {
            const id_usuario = req.query.id_usuario;
            const tipo = parseInt(req.query.tipo, 10);

            if (!id_usuario || !tipo) {
                return res.status(400).json({ error: "Faltan id_usuario o tipo" });
            }

            // Obtener placa
            const placa = await logica.obtenerPlacaDeUsuario(id_usuario);
            if (!placa) {
                return res.json({ status: "sin_placa" });
            }

            // Obtener valores
            const valores = await logica.obtenerPromedios7Dias(placa, tipo);

            // =========================================================================
            // Corrección: desplazar 1 día hacia la DERECHA
            // (el backend devuelve un día atrasado por timezone)
            // =========================================================================
            if (Array.isArray(valores) && valores.length === 7) {
                const ultimo = valores.pop();
                valores.unshift(ultimo);
            };

            // Generar labels de días (últimos 7)
            const dias = ["Dom","Lun","Mar","Mie","Jue","Vie","Sab"];
            const hoy = new Date();
            const labels = [];

            for (let i = 6; i >= 0; i--) {
                const d = new Date();
                d.setDate(hoy.getDate() - i);
                labels.push(dias[d.getDay()]);
            }

            // Calcular promedio general
            const promedio = valores.reduce((a,b) => a+b, 0) / 7;

            res.json({
                status: "con_placa",
                labels,
                valores,
                promedio
            });

        } catch (err) {
            console.error(err);
            res.status(500).json({ error: "Error interno en resumen7Dias" });
        }
    });


    // -----------------------------------------------------------------------------
    // Endpoint: GET /resumen8Horas
    // -----------------------------------------------------------------------------
    // Descripción:
    //   Devuelve 8 promedios horarios del gas seleccionado.
    //
    // Parámetros esperados:
    //   - id_usuario
    //   - tipo
    //
    // Respuesta:
    //   {
    //     status: "con_placa",
    //     labels: ["08","09","10","11","12","13","14","15"],
    //     valores: [...],
    //     promedio: number
    //   }
    // -----------------------------------------------------------------------------
    router.get("/resumen8Horas", async (req, res) => {
        try {
            const id_usuario = req.query.id_usuario;
            const tipo = parseInt(req.query.tipo, 10);

            if (!id_usuario || !tipo) {
                return res.status(400).json({ error: "Faltan datos: id_usuario o tipo" });
            }

            const placa = await logica.obtenerPlacaDeUsuario(id_usuario);
            if (!placa) return res.json({ status: "sin_placa" });

            const valores = await logica.obtenerPromedios8HorasPorGas(placa, tipo);
            const promedio = valores.reduce((a, b) => a + b, 0) / valores.length;

            const labels = [];
            const ahora = new Date();
            for (let i = 7; i >= 0; i--) {
                const fecha = new Date(ahora.getTime() - i * 3600000);
                labels.push(fecha.getHours().toString().padStart(2, "0"));
            }

            return res.json({
                status: "con_placa",
                labels,
                valores,
                promedio
            });

        } catch (err) {
            console.error("Error en GET /resumen8Horas:", err);
            res.status(500).json({ error: "Error interno servidor" });
        }
    });


    // --------------------------------------------------------------------------
    //  Devolvemos el router con todas las rutas activas
    // --------------------------------------------------------------------------
    return router;
}

module.exports = reglasREST;