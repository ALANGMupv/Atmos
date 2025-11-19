/**
 * ReglasREST.js
 * -------------------------
 * Capa de API REST.
 *
 * Este módulo define todos los endpoints HTTP disponibles para el frontend.
 * Sus responsabilidades principales son:
 *   - Recibir peticiones HTTP (GET, POST, PUT...).
 *   - Validar datos de entrada.
 *   - Verificar tokens de Firebase.
 *   - Invocar a la capa de negocio (Logica.js).
 *   - Formatear las respuestas de salida.
 *
 * Autores: Alan Guevara Martínez y Santiago Fuenmayor Ruiz
 */

const express = require("express");
const admin = require("../firebase/firebaseAdmin");

/**
 * Crea y devuelve el router que contiene toda la API REST.
 *
 * @param {Logica} logica - instancia de la clase Logica.js
 */
function reglasREST(logica) {
    const router = express.Router();

    // --------------------------------------------------------------------------
    // Middleware: verificarToken()
    // --------------------------------------------------------------------------
    // Descripción:
    //   Valida el token JWT de Firebase enviado en el header Authorization.
    //   Si es válido, añade los datos decodificados a req.user.
    //   Si no, bloquea la petición (401 o 403).
    //
    // Parámetros:
    //   - req {Request}
    //   - res {Response}
    //   - next {Function}
    //
    // Devuelve:
    //   - Llama a next() si el token es correcto.
    // --------------------------------------------------------------------------
    async function verificarToken(req, res, next) {
        try {
            const authHeader = req.headers.authorization;

            // Si no hay header Authorization
            if (!authHeader || !authHeader.startsWith("Bearer ")) {
                return res.status(401).json({error: "Token no proporcionado"});
            }

            // Extraer token del header
            const idToken = authHeader.split(" ")[1];

            // Validar token con Firebase Admin
            const decoded = await admin.auth().verifyIdToken(idToken);

            // Guardar en req.user para el resto de endpoints
            req.user = decoded;

            next();

        } catch (err) {
            console.error("Error verificando token Firebase:", err);
            return res.status(403).json({error: "Token inválido o expirado"});
        }
    }

    // --------------------------------------------------------------------------
    // Endpoint: POST /medida
    // --------------------------------------------------------------------------
    // Descripción:
    //   Inserta una nueva medida enviada por un dispositivo.
    //
    // Body JSON esperado:
    //   {
    //     "id_placa": "ABC123",
    //     "tipo": 11,
    //     "valor": 0.42,
    //     "latitud": 0.0,
    //     "longitud": 0.0
    //   }
    //
    // Devuelve:
    //   - status: "ok" y la medida insertada.
    // --------------------------------------------------------------------------
    router.post("/medida", async (req, res) => {
        try {
            const {id_placa, tipo, valor, latitud, longitud} = req.body;

            // Validar campos obligatorios
            if (!id_placa || tipo === undefined || valor === undefined) {
                return res.status(400).json({
                    status: "error",
                    mensaje: "Faltan campos: id_placa, tipo o valor"
                });
            }

            // Llamar a la lógica
            const medida = await logica.guardarMedida(
                id_placa,
                tipo,
                valor,
                latitud || 0.0,
                longitud || 0.0
            );

            res.json({status: "ok", medida});

        } catch (err) {
            console.error("Error en POST /medida:", err);
            res.status(500).json({error: "Error interno del servidor"});
        }
    });

    // --------------------------------------------------------------------------
    // Endpoint: GET /medidas
    // --------------------------------------------------------------------------
    // Descripción:
    //   Devuelve una lista limitada de medidas recientes.
    //
    // Query Params:
    //   - limit {number} : máximo 500
    //
    // Devuelve:
    //   - { medidas: [...] }
    // --------------------------------------------------------------------------
    router.get("/medidas", async (req, res) => {
        try {
            const {limit} = req.query;

            const medidas = await logica.listarMedidas(limit);

            res.json({
                status: "ok",
                medidas: medidas
            });

        } catch (err) {
            console.error("Error en GET /medidas:", err);
            res.status(500).json({error: "Error interno del servidor"});
        }
    });

    // --------------------------------------------------------------------------
    // Endpoint: POST /usuario
    // --------------------------------------------------------------------------
    // Descripción:
    //   Registra un nuevo usuario autenticado por Firebase.
    //
    // Headers:
    //   - Authorization: Bearer <tokenFirebase>
    //
    // Body JSON esperado:
    //   { nombre, apellidos, contrasena }
    //
    // Devuelve:
    //   - { status: "ok", usuario: {...} }
    // --------------------------------------------------------------------------
    router.post("/usuario", verificarToken, async (req, res) => {
        try {
            const {nombre, apellidos, contrasena} = req.body;
            const email = req.user.email;
            const uid = req.user.uid;
            const verificado = req.user.email_verified;

            // Validar campos
            if (!nombre || !apellidos || !contrasena) {
                return res.status(400).json({
                    error: "Faltan campos: nombre, apellidos o contrasena"
                });
            }

            // Comprobar si existe en MySQL
            const existente = await logica.buscarUsuarioPorEmail(email);
            if (existente) {
                return res.status(409).json({error: "El usuario ya existe"});
            }

            // Guardar nuevo usuario
            const nuevoUsuario = await logica.guardarUsuario(
                uid,
                nombre,
                apellidos,
                email,
                contrasena
            );

            // Marcar como verificado si Firebase indica que el correo ya lo está
            if (verificado) {
                await logica.actualizarEstadoVerificado(uid);
            }

            res.json({status: "ok", usuario: nuevoUsuario});

        } catch (err) {
            console.error("Error en POST /usuario:", err);
            res.status(500).json({error: "Error interno"});
        }
    });

    // --------------------------------------------------------------------------
    // Endpoint: PUT /usuario
    // --------------------------------------------------------------------------
    // Descripción:
    //   Actualiza el perfil de un usuario.
    //
    // Body JSON esperado:
    //   {
    //     id_usuario,
    //     nombre,
    //     apellidos,
    //     email
    //   }
    //
    // Devuelve:
    //   - { status: "ok" }
    // --------------------------------------------------------------------------
    router.put("/usuario", async (req, res) => {
        try {
            const {id_usuario, nombre, apellidos, email} = req.body;

            if (!id_usuario) {
                return res.status(400).json({error: "id_usuario es obligatorio"});
            }

            const actualizado = await logica.actualizarUsuario(
                id_usuario,
                {nombre, apellidos, email}
            );

            if (!actualizado) {
                return res.status(500).json({error: "No se pudo actualizar"});
            }

            res.json({status: "ok"});

        } catch (err) {
            console.error("Error en PUT /usuario:", err);
            res.status(500).json({error: "Error interno"});
        }
    });

    // --------------------------------------------------------------------------
    // Endpoint: POST /vincular
    // --------------------------------------------------------------------------
    // Descripción:
    //   Asocia una placa a un usuario.
    //
    // Body JSON:
    //   {
    //     "id_usuario": 6,
    //     "id_placa": "placaABC"
    //   }
    //
    // Devuelve:
    //   - { status, mensaje }
    // --------------------------------------------------------------------------
    router.post("/vincular", async (req, res) => {
        try {
            const {id_usuario, id_placa} = req.body;

            if (!id_usuario || !id_placa) {
                return res.status(400).json({error: "Faltan datos"});
            }

            const resultado = await logica.vincularPlacaAUsuario(id_usuario, id_placa);

            res.json(resultado);

        } catch (err) {
            console.error("Error en POST /vincular:", err);
            res.status(500).json({error: err.message});
        }
    });

    // --------------------------------------------------------------------------
    // Endpoint: POST /login
    // --------------------------------------------------------------------------
    // Descripción:
    //   Valida el token de Firebase, busca el usuario en MySQL
    //   y devuelve sus datos para crear la sesión PHP.
    //
    // Headers:
    //   - Authorization: Bearer <tokenFirebase>
    //
    // Devuelve:
    //   - { status: "ok", usuario: {...} }
    // --------------------------------------------------------------------------
    router.post("/login", verificarToken, async (req, res) => {
        try {
            const email = req.user.email;

            // Consultar MySQL
            const usuario = await logica.buscarUsuarioPorEmail(email);

            if (!usuario) {
                return res.status(404).json({
                    error: "Usuario no registrado en MySQL"
                });
            }

            return res.json({
                status: "ok",
                usuario: {
                    id_usuario: usuario.id_usuario,
                    nombre: usuario.nombre,
                    apellidos: usuario.apellidos,
                    email: usuario.email
                }
            });

        } catch (err) {
            console.error("Error en POST /login:", err);
            res.status(500).json({error: "Error interno"});
        }
    });

    // -----------------------------------------------------------------------------
    // Endpoint: GET /resumenUsuarioPorGas
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


    return router;
}

module.exports = reglasREST;
