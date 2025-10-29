// .....................................................................
// @author: Alan Guevara Martínez
// mainTest4.js - Comprueba que GET /medidas devuelve un array de medidas
// .....................................................................

const request = require("request");
const assert = require("assert");

const IP_PUERTO = process.env.TEST_URL || "http://localhost:3000";

describe("Test 4 - Listar medidas", function () {
  it("GET /medidas debe devolver un array con medidas", function (done) {
    request.get({ url: IP_PUERTO + "/medidas?limit=5" }, function (err, res, body) {
      assert.strictEqual(err, null);
      assert.strictEqual(res.statusCode, 200);
      const datos = JSON.parse(body);
      assert.strictEqual(datos.status, "ok");
      assert.ok(Array.isArray(datos.medidas));
      done();
    });
  });
});