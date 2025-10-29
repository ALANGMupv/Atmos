// .....................................................................
// @author: Alan Guevara Martínez
// mainTest1.js Comprueba que POST /medida guarda una medida correctamente.
// .....................................................................

const request = require("request");
const assert = require("assert");

// Dirección del servidor REST
const IP_PUERTO = process.env.TEST_URL || "http://localhost:3000";

describe("Test 1 - Servidor vivo", function () {
  it("GET / debe devolver 200 y confirmar que está activo", function (done) {
    request.get({ url: IP_PUERTO + "/" }, function (err, res, body) {
      assert.strictEqual(err, null);
      assert.strictEqual(res.statusCode, 200);
      const respuesta = JSON.parse(body);
      assert.strictEqual(respuesta.status, "ok");
      done();
    });
  });
});