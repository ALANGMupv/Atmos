// .....................................................................
// @author: Alan Guevara Martínez
// mainTest3.js Comprueba que la respuesta de la API devuelve lo mismo que se insertó.
// .....................................................................

const request = require("request");
const assert = require("assert");

const IP_PUERTO = process.env.TEST_URL || "http://localhost:3000";

describe("Test 3 - Validar respuesta del POST", function () {
  it("El POST /medida debe devolver la misma medida enviada", function (done) {
    const medida = {
      id_placa: 1,
      tipo: 12,
      valor: 55.7,
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
        assert.strictEqual(body.medida.tipo, medida.tipo);
        assert.strictEqual(body.medida.valor, medida.valor);
        done();
      }
    );
  });
});