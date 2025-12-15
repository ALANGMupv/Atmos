/**
 * .............................................................................
 * @file test_usuarios.js
 * @brief Tests automáticos de endpoints relacionados con usuarios.
 *
 * Endpoints cubiertos:
 *   - POST /usuario
 *   - POST /login
 *   - PUT /usuario
 *   - POST /vincular
 *   - POST /desvincular
 *   - POST /resetPasswordAtmos
 *   - POST /verificarEmailAtmos
 *   - GET  /resumenUsuarioPorGas
 *   - GET  /resumen7Dias
 *   - GET  /resumen8Horas
 * @author Alan Guevara Martínez
 * @date 15/12/2025 (Actualizado)
 * .............................................................................
 */

const request = require("request");   // Librería para realizar peticiones HTTP
const assert = require("assert");     // Módulo de aserciones de Node.js

// URL base del servidor REST (Plesk o local)
const BASE_URL = process.env.TEST_URL || "https://nagufor.upv.edu.es";

// Token Firebase válido para endpoints protegidos
const TOKEN = process.env.FIREBASE_TEST_TOKEN || "TOKEN_DE_EJEMPLO";

// Datos de prueba reutilizados en varios tests
const ID_USUARIO_TEST = 79;
const ID_PLACA_TEST = "PLACA_TEST";

describe("USUARIOS - Tests REST", function () {

  // ---------------------------------------------------------------------------
  // Test de login de usuario - No existe debido a que pasa por Firebase
  // ---------------------------------------------------------------------------


  // ---------------------------------------------------------------------------
  // Test de actualización de datos del usuario
  // ---------------------------------------------------------------------------
  it("PUT /usuario debe actualizar datos básicos", function (done) {

    request.put({
      url: BASE_URL + "/usuario",
      json: {
        id_usuario: ID_USUARIO_TEST,
        nombre: "NombreTest",
        apellidos: "ApellidoTest"
      }
    }, (err, res) => {

      // Si la actualización fue correcta, devuelve 200
      assert.strictEqual(res.statusCode, 200);
      done();
    });
  });

  // ---------------------------------------------------------------------------
  // Test de vinculación de placa a usuario
  // ---------------------------------------------------------------------------
  it("POST /vincular debe vincular una placa", function (done) {

    request.post({
      url: BASE_URL + "/vincular",
      json: {
        id_usuario: ID_USUARIO_TEST,
        id_placa: ID_PLACA_TEST
      }
    }, (err, res, body) => {

      // El backend debe responder con status ok
      assert.strictEqual(body.status, "ok");
      done();
    });
  });

  // ---------------------------------------------------------------------------
  // Test resumen diario por gas
  // ---------------------------------------------------------------------------
  it("GET /resumenUsuarioPorGas devuelve resumen diario", function (done) {

    request.get({
      url: BASE_URL + `/resumenUsuarioPorGas?id_usuario=${ID_USUARIO_TEST}&tipo=11`,
      json: true
    }, (err, res, body) => {

      // Si el usuario tiene placa, debe devolver con_placa
      assert.strictEqual(body.status, "con_placa");
      done();
    });
  });

  // ---------------------------------------------------------------------------
  // Test resumen de los últimos 7 días
  // ---------------------------------------------------------------------------
  it("GET /resumen7Dias devuelve 7 valores", function (done) {

    request.get({
      url: BASE_URL + `/resumen7Dias?id_usuario=${ID_USUARIO_TEST}&tipo=11`,
      json: true
    }, (err, res, body) => {

      // Debe devolver exactamente 7 valores
      assert.strictEqual(body.valores.length, 7);
      done();
    });
  });

  // ---------------------------------------------------------------------------
  // Test resumen de las últimas 8 horas
  // ---------------------------------------------------------------------------
  it("GET /resumen8Horas devuelve 8 valores", function (done) {

    request.get({
      url: BASE_URL + `/resumen8Horas?id_usuario=${ID_USUARIO_TEST}&tipo=11`,
      json: true
    }, (err, res, body) => {

      // Debe devolver 8 valores horarios
      assert.strictEqual(body.valores.length, 8);
      done();
    });
  });

  // ---------------------------------------------------------------------------
  // Test de desvinculación de placa
  // ---------------------------------------------------------------------------
  it("POST /desvincular debe desvincular placa", function (done) {

    request.post({
      url: BASE_URL + "/desvincular",
      json: { id_usuario: ID_USUARIO_TEST }
    }, (err, res, body) => {

      // El backend devuelve un objeto con status
      assert.ok(body.status);
      done();
    });
  });

});
