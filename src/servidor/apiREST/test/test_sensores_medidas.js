/**
 * .............................................................................
 * @file test_sensores_medidas.js
 * @brief Tests automáticos de sensores y medidas ambientales.
 *
 * Endpoints cubiertos:
 *   - POST /medida
 *   - GET  /medidas
 *   - GET  /estadoSenal
* @author Alan Guevara Martínez
* @date 15/12/2025 (Actualizado)
 * .............................................................................
 */

const request = require("request");   // Cliente HTTP
const assert = require("assert");     // Aserciones

const BASE_URL = process.env.TEST_URL || "https://nagufor.upv.edu.es";

describe("SENSORES Y MEDIDAS", function () {

  // ---------------------------------------------------------------------------
  // Inserción de una medida ambiental
  // ---------------------------------------------------------------------------
  it("POST /medida debe insertar medida correctamente", function (done) {

    request.post({
      url: BASE_URL + "/medida",
      json: {
        id_placa: "PLACA_TEST",
        tipo: 11,
        valor: 40.5,
        latitud: 39.47,
        longitud: -0.37,
        rssi: -60
      }
    }, (err, res, body) => {

      // El endpoint devuelve status ok si se insertó
      assert.strictEqual(body.status, "ok");
      done();
    });
  });

  // ---------------------------------------------------------------------------
  // Listado de medidas
  // ---------------------------------------------------------------------------
  it("GET /medidas debe devolver lista", function (done) {

    request.get({
      url: BASE_URL + "/medidas?limit=10",
      json: true
    }, (err, res, body) => {

      // Debe devolver un array de medidas
      assert.ok(Array.isArray(body.medidas));
      done();
    });
  });

  // ---------------------------------------------------------------------------
  // Estado de señal del sensor
  // ---------------------------------------------------------------------------
  it("GET /estadoSenal devuelve nivel de señal", function (done) {

    request.get({
      url: BASE_URL + "/estadoSenal?id_usuario=1",
      json: true
    }, (err, res, body) => {

      // Debe devolver status ok y un nivel de señal
      assert.strictEqual(body.status, "ok");
      assert.ok(body.nivel);
      done();
    });
  });

});
