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


// NUEVO: servicio de email Atmos
const servicioEmail = require("../servicios/ServicioEmailAtmos.js");

// NUEVO: servicio de restablecimiento de contraseña
const servicioEmailReset = require("../servicios/ServicioEmailResetAtmos.js");


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
//  Autor: Alan Guevara Martínez
// --------------------------------------------------------------------------
/**
 * Inserta una nueva medida en la base de datos.
 *
 * Cuerpo JSON esperado (versión nueva):
 * {
 *   "id_placa": "UUID-del-sensor",
 *   "tipo": 11,
 *   "valor": 412.7,
 *   "latitud": 0.0,
 *   "longitud": 0.0,
 *   "rssi": -65        // NUEVO: intensidad de señal
 * }
 *
 * El campo rssi es opcional para mantener compatibilidad.
 */
router.post("/medida", async (req, res) => {
    try {
        const { id_placa, tipo, valor, latitud, longitud, rssi } = req.body;

        if (!id_placa || tipo === undefined || valor === undefined) {
            return res.status(400).json({
                status: "error",
                mensaje: "Faltan campos obligatorios: id_placa, tipo o valor"
            });
        }

        let medidaInsertada;

        // Si el cliente envía RSSI, usamos la nueva lógica
        if (typeof rssi === "number") {
            medidaInsertada = await logica.guardarMedidaYActualizarDistancia(
                id_placa,
                tipo,
                valor,
                latitud || 0.0,
                longitud || 0.0,
                rssi
            );
        } else {
            // Compatibilidad con clientes antiguos: solo inserta en medida
            medidaInsertada = await logica.guardarMedida(
                id_placa,
                tipo,
                valor,
                latitud || 0.0,
                longitud || 0.0
            );
        }

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
 * Registra un nuevo usuario autenticado por Firebase en la base de datos
 * y envía un correo de verificación de Atmos usando un enlace oficial
 * generado por Firebase.
 *
 * Header: Authorization: Bearer <idTokenFirebase>
 * Body JSON esperado: { nombre, apellidos, contrasena }
 *
 * Flujo:
 *  1. Verifica el token Firebase (uid, email) mediante middleware verificarToken.
 *  2. Comprueba si el usuario ya existe en MySQL.
 *  3. Inserta el nuevo usuario si no existe.
 *  4. Marca estado = 1 si el email ya está verificado en Firebase.
 *  5. Si el email aún NO está verificado, genera un enlace de verificación
 *     con Firebase Admin y lo envía por correo (HTML custom Atmos).
 *
 * Respuestas posibles:
 *   200: { status: "ok", usuario, emailVerificacionEnviado, errorEnvioEmail? }
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

        // ------------------------------------------------------------------
        // 1) Comprobar si el usuario ya existe en MySQL
        // ------------------------------------------------------------------
        const usuarioExistente = await logica.buscarUsuarioPorEmail(email);
        if (usuarioExistente) {
            return res.status(409).json({ error: "El usuario ya existe" });
        }

        // ------------------------------------------------------------------
        // 2) Guardar el nuevo usuario en MySQL
        // ------------------------------------------------------------------
        const nuevoUsuario = await logica.guardarUsuario(
            uid,
            nombre,
            apellidos,
            email,
            contrasena
        );

        // ------------------------------------------------------------------
        // 3) Marcar como verificado en MySQL si ya lo estuviera en Firebase
        // ------------------------------------------------------------------
        if (emailVerificado) {
            await logica.actualizarEstadoVerificado(uid);
        }

        // ------------------------------------------------------------------
        // 4) Generar y enviar email de verificación Atmos (si no está verificado)
        // ------------------------------------------------------------------
        let emailVerificacionEnviado = false;
        let errorEnvioEmail = null;

        if (!emailVerificado) {
            try {
                // Generamos enlace seguro de verificación con Firebase Admin
				console.log("Generando enlace de verificación...");
                const enlace = await admin.auth().generateEmailVerificationLink(email, {
                    // URL de redirección tras verificar (ajusta a tu app / web)
                    url: "https://nagufor.upv.edu.es/cliente/login.php",
                    handleCodeInApp: false
                });

				console.log("Enlace generado:", enlace);
				console.log("Enviando correo Atmos...");
				
                // Enviamos correo HTML bonito de Atmos
                await servicioEmail.enviarCorreoVerificacionAtmos(
                    email,
                    nombre,
                    enlace
                );

                emailVerificacionEnviado = true;

            } catch (errEnvio) {
                console.error("Error al enviar email de verificación Atmos:", errEnvio);
                errorEnvioEmail = errEnvio.message || "Error desconocido enviando correo";
            }
        }

        // ------------------------------------------------------------------
        // 5) Respuesta final al frontend
        // ------------------------------------------------------------------
        const respuesta = {
            status: "ok",
            usuario: nuevoUsuario,
            emailVerificacionEnviado
        };

        if (!emailVerificacionEnviado && errorEnvioEmail) {
            respuesta.errorEnvioEmail = errorEnvioEmail;
        }

        return res.json(respuesta);

    } catch (err) {
        console.error("Error en POST /usuario:", err);

        // Rollback: eliminar usuario en Firebase si MySQL falló
        try {
            await admin.auth().deleteUser(req.user.uid);
            console.log("Rollback: usuario eliminado de Firebase por error en MySQL.");
        } catch (rollbackError) {
            console.error("Error eliminando usuario en Firebase:", rollbackError);
        }

        return res.status(500).json({
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
        const email = req.user.email;
        const uid = req.user.uid;
        const emailVerificado = req.user.email_verified;

        // ---------------------------------------------------------------------
        // Buscar usuario en MySQL
        // ---------------------------------------------------------------------
        const usuario = await logica.buscarUsuarioPorEmail(email);

        if (!usuario) {
            return res.status(404).json({
                error: "Usuario no registrado en MySQL"
            });
        }

        // ---------------------------------------------------------------------
        // Sincronizar estado de verificación entre Firebase y MySQL
        // ---------------------------------------------------------------------
        /*
        Comprueba si el email está verificado en Firebase (req.user.email_verified)
        Consulta MySQL
        Si MySQL tiene estado = 0 pero Firebase dice true → lo actualiza
        Devuelve el usuario con el estado actualizado (0 o 1)
        */
        if (emailVerificado && usuario.estado === 0) {
            try {
                await logica.actualizarEstadoVerificado(uid);
                usuario.estado = 1; // actualizamos el valor en la respuesta
                console.log("Estado sincronizado para", email);
            } catch (e) {
                console.error("Error sincronizando estado:", e);
            }
        }

        // ---------------------------------------------------------------------
        // Respuesta al frontend
        // ---------------------------------------------------------------------
        return res.json({
            status: "ok",
            usuario: {
                id_usuario: usuario.id_Usuario,
                nombre: usuario.nombre,
                apellidos: usuario.apellidos,
                email: usuario.email,
                estado: usuario.estado,   // <-- AHORA DEVUELVE EL ESTADO ACTUALIZADO
				id_rol: usuario.id_rol
            }
        });

    } catch (error) {
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

	// -----------------------------------------------------------------------------
	// Endpoint: POST /actualizarEstadoPlaca
	// Autor: Alan Guevara Martínez
	// Fecha: 20/11/2025
	// -----------------------------------------------------------------------------
	// Descripción:
	//   Recibe el estado de encendida (1/0) de una placa y lo actualiza en MySQL.
	//
	// Body esperado (JSON):
	//   {
	//     "id_placa": "XXXX",
	//     "encendida": 1
	//   }
	//
	// Respuestas:
	//   200: { status: "ok" }
	//   400: faltan datos
	//   500: error interno
	// -----------------------------------------------------------------------------
	router.post("/actualizarEstadoPlaca", async (req, res) => {
		try {
			const { id_placa, encendida } = req.body;

			if (!id_placa || encendida === undefined) {
				return res.status(400).json({
					status: "error",
					mensaje: "Faltan datos: id_placa o encendida"
				});
			}

			await logica.actualizarEstadoPlaca(id_placa, encendida);

			return res.json({ status: "ok" });

		} catch (err) {
			console.error("Error en POST /actualizarEstadoPlaca:", err);
			return res.status(500).json({
				status: "error",
				mensaje: "Error interno del servidor"
			});
		}
	});
	
	// -----------------------------------------------------------------------------
	// GET /estadoPlaca
	// -----------------------------------------------------------------------------
	//  Descripción:
	//     Devuelve el estado actual del sensor asociado al usuario.
	//     Usa el campo `placa.encendida` para saber si el sensor está activo.
	//
	//  Parámetros (query):
	//     - id_usuario : ID del usuario logueado
	//
	//  Respuestas:
	//     { estado: "activo" }    → si encendida = 1
	//     { estado: "inactivo" }  → si encendida = 0
	//     { estado: "sin_placa" } → si el usuario no tiene placa asociada
	//
	//  Notas:
	//     • Este endpoint se consulta periódicamente desde el frontend.
	//     • Es muy ligero: solo hace una consulta súper pequeña.
	// -----------------------------------------------------------------------------
	router.get("/estadoPlaca", async (req, res) => {
		try {
			const id_usuario = req.query.id_usuario;

			if (!id_usuario)
				return res.status(400).json({ error: "Falta id_usuario" });

			// Obtener ID de la placa asociada al usuario
			const id_placa = await logica.obtenerPlacaDeUsuario(id_usuario);

			if (!id_placa)
				return res.json({ estado: "sin_placa" });

			// Consultar si la placa está encendida o no
			const encendida = await logica.obtenerEstadoPlaca(id_placa);

			return res.json({
				estado: encendida ? "activo" : "inactivo"
			});

		} catch (err) {
			console.error("Error en GET /estadoPlaca:", err);
			res.status(500).json({ error: "Error interno del servidor" });
		}
	});
	
	// ======================================================================
	// GET /estadoSenal?id_usuario=NUM
	// ----------------------------------------------------------------------
	// Devuelve el nivel de señal del sensor del usuario.
	// ======================================================================
	router.get("/estadoSenal", async (req, res) => {

		try {
			const idUsuario = req.query.id_usuario;

			const datos = await logica.obtenerEstadoSenal(idUsuario);

			res.json({
				status: "ok",
				rssi: datos.rssi,
				nivel: datos.nivel
			});

		} catch (e) {
			console.log("ERROR en /estadoSenal:", e);
			res.json({ status: "error", mensaje: e.toString() });
		}
	});
	
	// -----------------------------------------------------------------------------
    // NOTIFICACIONES
    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    // GET /notificacionesUsuario
    // -----------------------------------------------------------------------------
    // Descripción:
    //   Devuelve las notificaciones calculadas "al vuelo" para un usuario.
    //
    // Parámetros (query):
    //   - id_usuario
    //
    // Respuesta:
    //   {
    //     status: "ok",
    //     notificaciones: [
    //       {
    //         tipo: "CO2_CRITICO",
    //         titulo: "...",
    //         texto: "...",
    //         icono: "alerta",
    //         fecha_hora: "...",
    //         leido: false
    //       },
    //       ...
    //     ]
    //   }
    // -----------------------------------------------------------------------------
    router.get("/notificacionesUsuario", async (req, res) => {
        try {
            const id_usuario = req.query.id_usuario;

            if (!id_usuario) {
                return res.status(400).json({ error: "Falta id_usuario" });
            }

            const notis = await logica.obtenerNotificacionesUsuario(Number(id_usuario));

            return res.json({
                status: "ok",
                notificaciones: notis
            });

        } catch (err) {
            console.error("Error en GET /notificacionesUsuario:", err);
            return res.status(500).json({ error: "Error interno al obtener notificaciones" });
        }
    });
// --------------------------------------------------------------------------
//  POST /notificacionCrear
//  Crea una notificación para un usuario
// --------------------------------------------------------------------------
router.post("/notificacionCrear", async (req, res) => {
    try {
        const {
            id_usuario,
            id_placa,
            tipo,
            titulo,
            mensaje,
            nivel,
            icono
        } = req.body;

        if (!id_usuario || !tipo || !titulo || !mensaje) {
            return res.status(400).json({ status: "error", mensaje: "Faltan campos obligatorios" });
        }

        const id = await logica.insertarNotificacion({
            id_usuario: Number(id_usuario),
            id_placa: id_placa || null,
            tipo,
            titulo,
            mensaje,
            nivel: nivel || "info",
            icono: icono || null
        });

        return res.json({
            status: "ok",
            id_notificacion: id
        });

    } catch (err) {
        console.error("ERROR en /notificacionCrear:", err);
        return res.status(500).json({
            status: "error",
            mensaje: "Error interno al crear la notificación"
        });
    }
});
// --------------------------------------------------------------------------
//  POST /marcarNotificacionLeida
// --------------------------------------------------------------------------
router.post("/marcarNotificacionLeida", async (req, res) => {
    try {
        const { id_notificacion, id_usuario } = req.body;

        if (!id_notificacion || !id_usuario) {
            return res.status(400).json({ status: "error", mensaje: "Faltan parámetros" });
        }

        const ok = await logica.marcarNotificacionLeida(
            Number(id_notificacion),
            Number(id_usuario)
        );

        return res.json({
            status: "ok",
            marcada: ok
        });

    } catch (err) {
        console.error("ERROR en /marcarNotificacionLeida:", err);
        return res.status(500).json({
            status: "error",
            mensaje: "Error interno al marcar notificación como leída"
        });
    }
});
// --------------------------------------------------------------------------
//  POST /marcarTodasNotificacionesLeidas
// --------------------------------------------------------------------------
router.post("/marcarTodasNotificacionesLeidas", async (req, res) => {
    try {
        const { id_usuario } = req.body;

        if (!id_usuario) {
            return res.status(400).json({ status: "error", mensaje: "Falta id_usuario" });
        }

        const num = await logica.marcarTodasNotificacionesLeidas(Number(id_usuario));

        return res.json({
            status: "ok",
            total_actualizadas: num
        });

    } catch (err) {
        console.error("ERROR en /marcarTodasNotificacionesLeidas:", err);
        return res.status(500).json({
            status: "error",
            mensaje: "Error interno al marcar todas como leídas"
        });
    }
});
// DELETE una notificación
router.post("/borrarNotificacion", async (req, res) => {
    try {
        const { id_notificacion, id_usuario } = req.body;
        if (!id_notificacion || !id_usuario) {
            return res.status(400).json({ status: "error", mensaje: "Faltan parámetros" });
        }

        const ok = await logica.borrarNotificacion(
            Number(id_notificacion),
            Number(id_usuario)
        );

        return res.json({ status: "ok", borrada: ok });
    } catch (err) {
        console.error("ERROR en /borrarNotificacion:", err);
        return res.status(500).json({ status: "error", mensaje: "Error interno al borrar notificación" });
    }
});

// DELETE todas las notificaciones de un usuario
router.post("/borrarNotificacionesUsuario", async (req, res) => {
    try {
        const { id_usuario } = req.body;
        if (!id_usuario) {
            return res.status(400).json({ status: "error", mensaje: "Falta id_usuario" });
        }

        const num = await logica.borrarNotificacionesUsuario(Number(id_usuario));

        return res.json({ status: "ok", total_borradas: num });
    } catch (err) {
        console.error("ERROR en /borrarNotificacionesUsuario:", err);
        return res.status(500).json({ status: "error", mensaje: "Error interno al borrar notificaciones" });
    }
});

router.get("/cron/notificaciones", async (req, res) => {
    try {
        const ReglasNotificaciones = require("../logica/ReglasNotificaciones.js");
        const motor = new ReglasNotificaciones(logica);

        const resultado = await motor.evaluarYGenerar();

        res.json({
            ok: true,
            mensaje: "Cron ejecutado correctamente",
            resultado
        });

    } catch (err) {
        console.error("Error ejecutando cron:", err);
        res.status(500).json({
            ok: false,
            error: err.message
        });
    }
});


	
	
// -----------------------------------------------------------------------------
// NOTIFICACIONES
// -----------------------------------------------------------------------------

// -----------------------------------------------------------------------------
// POST /resetPasswordAtmos
// -----------------------------------------------------------------------------
/**
 * @brief Endpoint que genera un enlace de restablecimiento de contraseña
 *        y envía un correo personalizado al usuario.
 *
 * @route POST /resetPasswordAtmos
 *
 * @param {string} req.body.correo  Correo del usuario que solicita el reset.
 *
 * @returns {JSON}
 *  {
 *    "status": "ok"
 *  }
 *  o
 *  {
 *    "status": "error",
 *    "msg": "Motivo del error"
 *  }
 *
 * Flujo:
 *  1. Recibe el correo del usuario desde el frontend.
 *  2. Busca el usuario en MySQL (para obtener su nombre, si existe).
 *  3. Genera un enlace de restablecimiento usando Firebase Admin.
 *  4. Envía un correo HTML bonito usando ServicioEmailResetAtmos.
 *  5. Devuelve status "ok" si todo fue correcto.
 * 
 * @author Alan Guevara Martínez
 * @date 01/12/2025
 */
router.post("/resetPasswordAtmos", async (req, res) => {
    try {
        const correo = req.body.correo;

        console.log("[Reset] Solicitud de reset para:", correo);

        // 0) Validación básica
        if (!correo) {
            return res.status(400).json({
                status: "error",
                msg: "Falta correo"
            });
        }

        // 1) Buscar usuario en MySQL para sacar su nombre (opcional)
        const usuarioBD = await logica.buscarUsuarioPorEmail(correo);
        const nombreUsuario = usuarioBD?.nombre || "usuario/a";

        console.log("[Reset] Nombre del usuario:", nombreUsuario);

        // 2) Generar enlace seguro de Firebase para restablecer contraseña
        const enlace = await admin.auth().generatePasswordResetLink(correo, {
            url: "https://nagufor.upv.edu.es/cliente/resetPasswordAtmos.php",
            handleCodeInApp: false
        });

        console.log("[Reset] Enlace generado correctamente");

        // 3) Enviar email personalizado de Atmos
        await servicioEmailReset.enviarCorreoReset(correo, nombreUsuario, enlace);

        console.log("[Reset] Email enviado correctamente");

        return res.json({ status: "ok" });

    } catch (error) {
        console.error("[Reset] Error:", error);
        return res.json({
            status: "error",
            msg: "No se pudo enviar el correo"
        });
    }
});
	
/**
 * @route POST /verificarEmailAtmos
 * @brief Endpoint encargado de procesar el código de verificación de correo (oobCode)
 *        enviado por Firebase y actualizar el estado del usuario en la base de datos.
 *
 * El cliente envía un JSON con:
 * @param {string} oobCode - Código de verificación generado por Firebase.
 *
 * Proceso:
 *  - Valida que llegue el oobCode.
 *  - Firebase verifica dicho código mediante applyActionCode().
 *  - Obtiene el email asociado al código.
 *  - Busca el usuario correspondiente en Firebase Authentication.
 *  - Llama a la lógica de negocio para marcar al usuario como verificado en la BD.
 *
 * @return {JSON} Respuesta con estado "ok" o "error".
 * 
 * @author Alan Guevara Martínez
 * @date 02/12/2025
 */
router.post("/verificarEmailAtmos", async (req, res) => {
    try {
        const { oobCode } = req.body;

        /**
         * @brief Validación de parámetros.
         * Si no se recibe el código, se detiene el flujo.
         */
        if (!oobCode)
            return res.json({ status: "error", msg: "Falta oobCode" });

        /**
         * @brief Firebase procesa el código de verificación.
         * @throws Error si el código ya no es válido o expiró.
         */
        const info = await admin.auth().applyActionCode(oobCode);
        const email = info.data.email;

        /** Obtiene al usuario en Firebase Authentication mediante su email. */
        const usuarioFirebase = await admin.auth().getUserByEmail(email);

        /**
         * @brief Actualiza en la BD el estado de verificación del usuario.
         * Se utiliza el UID interno de Firebase.
         */
        await logica.actualizarEstadoVerificado(usuarioFirebase.uid);

        return res.json({ status: "ok" });

    } catch (err) {
        /**
         * @brief Manejo de errores (Firebase, BD, lógica, etc.)
         */
        console.error(err);
        return res.json({ status: "error", msg: err.message });
    }
});
	
	// -----------------------------------------------------------
	// GET /mapa/medidas/gas?tipo=XX
	// Devuelve las últimas medidas del gas seleccionado
	// -----------------------------------------------------------
	router.get("/mapa/medidas/gas", async (req, res) => {
		try {
			const tipo = parseInt(req.query.tipo, 10);

			if (!tipo) {
				return res.status(400).json({ error: "Falta tipo" });
			}

			// ✔️ Usamos tu método correcto de Logica.js
			const filas = await logica.obtenerUltimasMedidasGlobalPorGas(tipo);

			return res.json({
				status: "ok",
				medidas: filas
			});

		} catch (err) {
			console.error("Error en /mapa/medidas/gas:", err);
			return res.status(500).json({
				status: "error",
				mensaje: err.message
			});
		}
	});



	
	// -----------------------------------------------------------------------------
    // Endpoint: GET /mapa/medidas/todos
    // Autor: Santiago Fuenmayor Ruiz
    // Fecha: 05/12/2025
    // -----------------------------------------------------------------------------
    // Descripción:
    //   Devuelve, para CADA placa registrada, las últimas mediciones de TODOS
    //   los gases (NO2, CO, O3, SO2) en **una única fila por placa**.
    //
    //   Este endpoint NO depende del usuario. Incluye todas las placas del sistema.
    //
    //   Formato devuelto:
    //     {
    //       status: "ok",
    //       placas: [
    //         {
    //           id_placa,
    //           latitud,
    //           longitud,
    //           NO2,
    //           CO,
    //           O3,
    //           SO2
    //         },
    //         ...
    //       ]
    //     }
    //
    // Uso previsto:
    //   ✔ Pintar el modo "TODOS" del mapa
    //   ✔ Futuro sistema de interpolación por celdas
    //   ✔ Calcular índices globales de contaminación
    //   ✔ Timeline combinado de todos los gases
    //
    // Ejemplo:
    //   GET /mapa/medidas/todos
    //
    // -----------------------------------------------------------------------------
	
    router.get("/mapa/medidas/todos", async (req, res) => {
        try {
            // Llamamos a la capa de lógica para obtener los datos agregados
            const filas = await logica.obtenerUltimasMedidasGlobalTodasLasPlacas();

            return res.json({
                status: "ok",
                placas: filas
            });

        } catch (err) {
            console.error("Error en GET /mapa/medidas/todos:", err);

            return res.status(500).json({
                status: "error",
                mensaje: "Error interno del servidor"
            });
        }
    });

    // --------------------------------------------------------------------------
    //  Endpoint: GET /estadoNodos
    // --------------------------------------------------------------------------
    /**
     * Devuelve el estado de todos los nodos (placas) para el informe T019.
     *
     * Parámetros opcionales (query):
     *   - umbralInactivoMin : minutos para considerar inactivo (default 5)
     *   - horasError        : horas para considerar lecturas erróneas (default 4)
     *   - limit             : máximo de nodos a devolver (default 100)
     *
     * Ejemplo:
     *   GET /estadoNodos?umbralInactivoMin=5&horasError=4&limit=50
     *
     * Respuesta:
     *   {
     *     status: "ok",
     *     nodos: [
     *       {
     *         id_placa: "TEST_PLACA_VALENCIA",
     *         ultima_medida: "2025-12-08T12:34:56.000Z",
     *         minutos_desde_ultima: 7,
     *         estado: "inactivo",
     *         tiempo_problema_min: 7
     *       },
     *       ...
     *     ]
     *   }
	 * Agregado por: Alejandro Vazquez
     */
    router.get("/estadoNodos", async (req, res) => {
        try {
            const { umbralInactivoMin, horasError, limit } = req.query;

            const nodos = await logica.obtenerEstadoNodos(
                umbralInactivoMin ? Number(umbralInactivoMin) : undefined,
                horasError ? Number(horasError) : undefined,
                limit ? Number(limit) : undefined
            );

            return res.json({
                status: "ok",
                nodos
            });

        } catch (err) {
            console.error("Error en GET /estadoNodos:", err);
            return res.status(500).json({
                status: "error",
                mensaje: "Error interno al obtener estado de nodos"
            });
        }
    });

/**
 * @route   POST /recorrido
 * @brief   Registra el recorrido diario de un usuario.
 *
 * Este endpoint recibe la distancia recorrida por un usuario en un día
 * concreto y la almacena en el sistema. Se utiliza para guardar el total
 * de metros recorridos al finalizar un recorrido o al cerrar la sesión.
 *
 * ---
 * 
 * @param {number} id_usuario   Identificador único del usuario.
 * @param {number} distancia_m  Distancia recorrida en metros.
 * @param {string} [fecha]      Fecha opcional del recorrido (YYYY-MM-DD).
 *                              Si no se envía, el backend usará la fecha actual.
 *
 * @returns {Object} JSON con el estado de la operación.
 * @returns {string} status     "ok" si el recorrido se ha guardado correctamente.
 *
 * @throws {400} Datos incompletos si faltan parámetros obligatorios.
 * @throws {500} Error interno del servidor si ocurre una excepción.
 *
 * @author  Alan Guevara Martínez
 * @date    2025-12-17
 */
router.post("/recorrido", async (req, res) => {

    try {

        // 1. Extraer los datos del cuerpo de la petición
        const { id_usuario, distancia_m, fecha } = req.body;

        // 2. Validación básica de parámetros obligatorios
        if (!id_usuario || distancia_m === undefined) {
            return res.status(400).json({
                error: "Datos incompletos"
            });
        }

        // 3. Guardar el recorrido diario
        //    - Si fecha es null, la lógica asignará la fecha actual
        await logica.guardarRecorridoDiario(
            id_usuario,
            distancia_m,
            fecha || null
        );

        // 4. Respuesta correcta al cliente
        res.json({
            status: "ok"
        });

    } catch (err) {

        // 5. Manejo de errores inesperados
        console.error("Error POST /recorrido:", err);

        res.status(500).json({
            error: "Error interno"
        });
    }
});

/**
 * @route   GET /recorrido
 * @brief   Obtiene la distancia recorrida por un usuario hoy y ayer.
 *
 * Consulta la capa de lógica para recuperar los metros recorridos
 * en el día actual y el día anterior. Si no existen datos para
 * alguno de los días, se devuelve 0.
 *
 * @param {number} id_usuario  Identificador del usuario (query).
 *
 * @returns {Object} JSON con el estado y las distancias.
 * @returns {string} status   "ok" si la operación es correcta.
 * @returns {number} hoy      Metros recorridos hoy.
 * @returns {number} ayer     Metros recorridos ayer.
 *
 * @throws {500} Error interno del servidor.
 *
 * @author  Alan Guevara Martínez
 * @date    2025-12
 */
router.get("/recorrido", async (req, res) => {

    try {

        // 1. Obtener el identificador del usuario desde la query
        const { id_usuario } = req.query;

        // 2. Consultar la capa de lógica para obtener los recorridos
        //    del día actual y del día anterior
        const datos = await logica.obtenerRecorridoHoyYAyer(id_usuario);

        // 3. Responder al cliente
        //    - Si no hay datos, se devuelven 0 metros
        res.json({
            status: "ok",
            hoy: datos.hoy || 0,
            ayer: datos.ayer || 0
        });

    } catch (err) {

        // 4. Manejo de errores inesperados
        res.status(500).json({
            error: "Error interno"
        });
    }
});
	
// --------------------------------------------------------------------------
//  INCIDENCIAS (USUARIO)
// --------------------------------------------------------------------------

/**
 * @route   POST /incidencia
 * @brief   Crea una nueva incidencia para un usuario.
 *
 * Cuerpo JSON esperado:
 * {
 *   "id_usuario": 123,
 *   "asunto": "Título",
 *   "mensaje_enviado": "Texto de la incidencia",
 *   "id_placa": "ABC123"   // opcional
 * }
 *
 * Respuesta:
 *   200: { status: "ok", id_incidencia: number }
 *   400: { status: "error", mensaje: "..." }
 *   500: { status: "error", mensaje: "..." }
 *
 * @author  Nerea Aguilar Forés
 * @date    2025-12-28
 */
router.post("/incidencia", async (req, res) => {

    try {

        const { id_usuario, asunto, mensaje_enviado, id_placa } = req.body;

        if (!id_usuario || !asunto || !mensaje_enviado) {
            return res.status(400).json({
                status: "error",
                mensaje: "Faltan campos obligatorios: id_usuario, asunto o mensaje_enviado"
            });
        }

        const idIncidencia = await logica.crearIncidencia(
            Number(id_usuario),
            asunto,
            mensaje_enviado,
            id_placa || null
        );

        return res.json({
            status: "ok",
            id_incidencia: idIncidencia
        });

    } catch (err) {
        console.error("Error en POST /incidencia:", err);
        return res.status(500).json({
            status: "error",
            mensaje: "Error interno del servidor"
        });
    }
});

/**
 * @route   GET /incidencias
 * @brief   Devuelve todas las incidencias de un usuario.
 *
 * Query:
 *   - id_usuario
 *
 * Respuesta:
 *   200: { status: "ok", incidencias: [...] }
 *   400: { status: "error", mensaje: "..." }
 *   500: { status: "error", mensaje: "..." }
 *
 * @author  Nerea Aguilar Forés
 * @date    2025-12-28
 */
router.get("/incidencias", async (req, res) => {

    try {

        const { id_usuario } = req.query;

        if (!id_usuario) {
            return res.status(400).json({
                status: "error",
                mensaje: "Falta id_usuario"
            });
        }

        const filas = await logica.listarIncidenciasUsuario(Number(id_usuario));

        return res.json({
            status: "ok",
            incidencias: filas
        });

    } catch (err) {
        console.error("Error en GET /incidencias:", err);
        return res.status(500).json({
            status: "error",
            mensaje: "Error interno del servidor"
        });
    }
});

/**
 * @route   GET /incidencia
 * @brief   Devuelve el detalle de una incidencia.
 *
 * Query:
 *   - id_incidencia
 *
 * Respuesta:
 *   200: { status: "ok", incidencia: {...} }
 *   404: { status: "error", mensaje: "No encontrada" }
 *   500: { status: "error", mensaje: "..." }
 *
 * @author  Nerea Aguilar Forés
 * @date    2025-12-28
 */
router.get("/incidencia", async (req, res) => {

    try {

        const { id_incidencia } = req.query;

        if (!id_incidencia) {
            return res.status(400).json({
                status: "error",
                mensaje: "Falta id_incidencia"
            });
        }

        const inc = await logica.obtenerIncidenciaPorId(Number(id_incidencia));

        if (!inc) {
            return res.status(404).json({
                status: "error",
                mensaje: "No encontrada"
            });
        }

        return res.json({
            status: "ok",
            incidencia: inc
        });

    } catch (err) {
        console.error("Error en GET /incidencia:", err);
        return res.status(500).json({
            status: "error",
            mensaje: "Error interno del servidor"
        });
    }
});
	
	/**
 * @route   POST /incidencia/leida
 * @brief   Marca una incidencia como leída por el usuario.
 *
 * Body JSON:
 * {
 *   "id_incidencia": number
 * }
 *
 * @author Nerea Aguilar Forés
 */
router.post("/incidencia/leida", async (req, res) => {
    try {
        const { id_incidencia } = req.body;

        if (!id_incidencia) {
            return res.status(400).json({
                status: "error",
                mensaje: "Falta id_incidencia"
            });
        }

        await logica.marcarIncidenciaLeida(Number(id_incidencia));

        return res.json({ status: "ok" });

    } catch (err) {
        console.error("Error en POST /incidencia/leida:", err);
        return res.status(500).json({
            status: "error",
            mensaje: "Error interno del servidor"
        });
    }
});


    // --------------------------------------------------------------------------
    //  Devolvemos el router con todas las rutas activas
    // --------------------------------------------------------------------------
    return router;
}

module.exports = reglasREST;