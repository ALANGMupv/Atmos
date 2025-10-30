// mainTestServidor.js
// Comprueba que el servidor REST responde correctamente
// y que las rutas principales funcionan (GET /, GET /medidas, POST /medida).

// @author: Alan Guevara Martínez

const request = require("supertest");
const assert = require("assert");
const app = require("./mainServidorREST");

// URL base (local o servidor remoto)
const SERVER_URL = process.env.SERVER_URL || "http://localhost:3000";

describe("🧪 Test del Servidor Atmos Biometría", function () {
  this.timeout(5000);

  // GET / → comprueba que el servidor está activo
  it("GET / debe responder con 200 y mensaje de servidor activo", async function () {
    const res = await request(app).get("/");
    assert.strictEqual(res.status, 200);
    assert.strictEqual(res.body.status, "ok");
  });

  // GET /medidas → debe devolver array
  it("GET /medidas debe devolver 200 y un array", async function () {
    const res = await request(app).get("/medidas?limit=5");
    assert.strictEqual(res.status, 200);
    assert.ok(Array.isArray(res.body.medidas));
  });

  // POST /medida → inserta un valor de prueba
  it("POST /medida debe insertar una medida correctamente (mock)", async function () {
    const body = {
      id_placa: 1,
      tipo: 11,
      valor: 25.5,
      latitud: 0,
      longitud: 0
    };

    const res = await request(app).post("/medida").send(body);
    assert.strictEqual(res.status, 200);
    assert.strictEqual(res.body.status, "ok");
    assert.ok(res.body.medida.id_medida);
  });
});
