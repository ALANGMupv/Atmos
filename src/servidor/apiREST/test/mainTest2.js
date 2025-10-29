// .....................................................................
// @author: Alan Guevara Martínez
// mainTest2.js - Inserta una medida de prueba en la BD a través del endpoint POST
// .....................................................................

const request = require("request");
const assert = require("assert");

// Dirección del servidor REST
const IP_PUERTO = process.env.TEST_URL || "http://localhost:3000";

describe("Test 2 - Insertar medida", function () {
  it("POST /medida debe devolver status ok", function (done) {
    const medida = {
      id_placa: 1,
      tipo: 11,         // tipo de gas o sensor (ejemplo: 11 = CO₂)
      valor: 42.5,      // valor de la medida
      latitud: 0,
      longitud: 0
    };

    request.post(
      {
        url: IP_PUERTO + "/medida",
        json: medida
      },
      function (err, res, body) {
        assert.strictEqual(err, null);
        assert.strictEqual(res.statusCode, 200);
        assert.strictEqual(body.status, "ok");
        done();
      }
    );
  });
});
