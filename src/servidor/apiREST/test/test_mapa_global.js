/**
 * .............................................................................
 * @file test_mapa_global.js
 * @brief Tests automáticos de endpoints globales de mapa.
 *
 * Endpoints cubiertos:
 *   - GET /mapa/medidas/gas
 *   - GET /mapa/medidas/todos
 * @author Alan Guevara Martínez
 * @date 15/12/2025 (Actualizado)
 * .............................................................................
 */

const request = require("request");
const assert = require("assert");

const BASE_URL = process.env.TEST_URL || "https://nagufor.upv.edu.es";


describe("MAPA GLOBAL", function () {

  // ---------------------------------------------------------------------------
  // Medidas globales por tipo de gas
  // ---------------------------------------------------------------------------
  it("GET /mapa/medidas/gas devuelve medidas por gas", function (done) {

    request.get({
      url: BASE_URL + "/mapa/medidas/gas?tipo=11",
      json: true
    }, (err, res, body) => {

      // Debe devolver status ok y un array de medidas
      assert.strictEqual(body.status, "ok");
      assert.ok(Array.isArray(body.medidas));
      done();
    });
  });

  // ---------------------------------------------------------------------------
  // Últimas medidas de todas las placas
  // ---------------------------------------------------------------------------
  it("GET /mapa/medidas/todos devuelve placas agregadas", function (done) {

    request.get({
      url: BASE_URL + "/mapa/medidas/todos",
      json: true
    }, (err, res, body) => {

      // Devuelve una lista de placas con datos agregados
      assert.strictEqual(body.status, "ok");
      assert.ok(Array.isArray(body.placas));
      done();
    });
  });

});
