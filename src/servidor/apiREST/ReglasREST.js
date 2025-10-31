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

            if (!id_usuario || !contrasena_actual) {
                return res.status(400).json({ error: "Faltan datos obligatorios" });
            }

            // Obtener usuario
            const usuario = await logica.obtenerUsuarioPorId(id_usuario);
            if (!usuario) return res.status(404).json({ error: "Usuario no encontrado" });

            // Verificar contraseña actual
            const ok = await bcrypt.compare(contrasena_actual, usuario.contrasena);
            if (!ok) return res.status(401).json({ error: "Contraseña actual incorrecta" });

            // Hashear nueva contraseña si se envía
            let contrasenaFinal = usuario.contrasena;
            if (nueva_contrasena) {
                contrasenaFinal = await bcrypt.hash(nueva_contrasena, 10);
            }

            // Actualizar usuario
            const actualizado = await logica.actualizarUsuario(id_usuario, {
                nombre,
                apellidos,
                email,
                contrasena: contrasenaFinal
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

    // --------------------------------------------------------------------------
    //  Devolvemos el router con todas las rutas activas
    // --------------------------------------------------------------------------
    return router;
}

module.exports = reglasREST;
