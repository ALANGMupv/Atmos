/**
 * .............................................................................
 * @file test_recorrido.js
 * @brief Tests automáticos de endpoints relacionados con recorridos diarios.
 *
 * Endpoints cubiertos:
 *   - POST /recorrido
 *   - GET  /recorrido
 *
 * Funcionalidad probada:
 *   - Registro y acumulación de distancia diaria.
 *   - Consulta de distancia recorrida hoy y ayer.
 *
 * @author Alan Guevara Martínez
 * @date   18/12/2025
 * .............................................................................
 */

const request = require("request");   // Librería para realizar peticiones HTTP
const assert = require("assert");    // Módulo de aserciones de Node.js

// URL base del servidor REST (Plesk o local)
const BASE_URL = process.env.TEST_URL || "https://nagufor.upv.edu.es";

// Datos de prueba reutilizados
const ID_USUARIO_TEST = 37;
const DISTANCIA_TEST = 1200; // metros

describe("RECORRIDO - Tests REST", function () {

    // ---------------------------------------------------------------------------
    // Test de guardado de recorrido diario sin fecha (usa fecha actual)
    // ---------------------------------------------------------------------------
    it("POST /recorrido debe guardar el recorrido de hoy", function (done) {

        request.post({
            url: BASE_URL + "/recorrido",
            json: {
                id_usuario: ID_USUARIO_TEST,
                distancia_m: DISTANCIA_TEST
                // fecha no enviada → backend usa CURDATE()
            }
        }, (err, res, body) => {

            // El servidor debe responder correctamente
            assert.strictEqual(res.statusCode, 200);

            // El backend devuelve status ok
            assert.strictEqual(body.status, "ok");

            done();
        });
    });

    // ---------------------------------------------------------------------------
    // Test de guardado de recorrido diario con fecha explícita
    // ---------------------------------------------------------------------------
    it("POST /recorrido debe guardar el recorrido para una fecha concreta", function (done) {

        const fechaAyer = new Date(Date.now() - 86400000)
            .toISOString()
            .slice(0, 10); // YYYY-MM-DD

        request.post({
            url: BASE_URL + "/recorrido",
            json: {
                id_usuario: ID_USUARIO_TEST,
                distancia_m: 500,
                fecha: fechaAyer
            }
        }, (err, res, body) => {

            // Respuesta correcta del servidor
            assert.strictEqual(res.statusCode, 200);

            // Confirmación de operación correcta
            assert.strictEqual(body.status, "ok");

            done();
        });
    });

    // ---------------------------------------------------------------------------
    // Test de error por datos incompletos
    // ---------------------------------------------------------------------------
    it("POST /recorrido debe fallar si faltan parámetros obligatorios", function (done) {

        request.post({
            url: BASE_URL + "/recorrido",
            json: {
                distancia_m: 1000   // falta id_usuario
            }
        }, (err, res, body) => {

            // El backend debe devolver error de validación
            assert.strictEqual(res.statusCode, 400);
            assert.ok(body.error);

            done();
        });
    });

    // ---------------------------------------------------------------------------
    // Test de consulta de recorrido de hoy y ayer
    // ---------------------------------------------------------------------------
    it("GET /recorrido devuelve la distancia recorrida hoy y ayer", function (done) {

        request.get({
            url: BASE_URL + `/recorrido?id_usuario=${ID_USUARIO_TEST}`,
            json: true
        }, (err, res, body) => {

            // El servidor responde correctamente
            assert.strictEqual(res.statusCode, 200);

            // Estructura esperada de la respuesta
            assert.strictEqual(body.status, "ok");
            assert.ok(body.hasOwnProperty("hoy"));
            assert.ok(body.hasOwnProperty("ayer"));

            // Los valores deben ser numéricos (el backend devuelve 0 si no hay datos)
            assert.strictEqual(typeof body.hoy, "number");
            assert.strictEqual(typeof body.ayer, "number");

            done();
        });
    });

});
