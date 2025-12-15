/**
 * .............................................................................
 * @file test_placas_estado.js
 * @brief Tests automáticos de estado de placas y nodos.
 *
 * Endpoints cubiertos:
 *   - POST /actualizarEstadoPlaca
 *   - GET  /estadoPlaca
 *   - GET  /estadoNodos
 * @author Alan Guevara Martínez
 * @date 15/12/2025 (Actualizado)
 * .............................................................................
 */

const request = require("request");
const assert = require("assert");

const BASE_URL = process.env.TEST_URL || "https://nagufor.upv.edu.es";

describe("PLACAS Y ESTADO", function () {

  // ---------------------------------------------------------------------------
  // Actualizar estado de una placa
  // ---------------------------------------------------------------------------
  it("POST /actualizarEstadoPlaca debe actualizar estado", function (done) {

    request.post({
      url: BASE_URL + "/actualizarEstadoPlaca",
      json: {
        id_placa: "PLACA_TEST",
        encendida: 1
      }
    }, (err, res, body) => {

      // Si se actualiza correctamente, devuelve ok
      assert.strictEqual(body.status, "ok");
      done();
    });
  });

  // ---------------------------------------------------------------------------
  // Consultar estado de la placa del usuario
  // ---------------------------------------------------------------------------
  it("GET /estadoPlaca devuelve activo/inactivo", function (done) {

    request.get({
      url: BASE_URL + "/estadoPlaca?id_usuario=1",
      json: true
    }, (err, res, body) => {

      // El estado debe existir (activo / inactivo / sin_placa)
      assert.ok(body.estado);
      done();
    });
  });

  // ---------------------------------------------------------------------------
  // Estado global de nodos
  // ---------------------------------------------------------------------------
  it("GET /estadoNodos devuelve nodos", function (done) {

    request.get({
      url: BASE_URL + "/estadoNodos?limit=5",
      json: true
    }, (err, res, body) => {

      // Debe devolver un array de nodos
      assert.strictEqual(body.status, "ok");
      assert.ok(Array.isArray(body.nodos));
      done();
    });
  });

});
