// @author: Alan Guevara Martínez
// mainTestLogica.js - Pruebas unitarias de la capa de lógica

const assert = require("assert");
const Logica = require("../Logica");

// Configuración para entorno local (usa tu .env si prefieres)
const DB_CONFIG = {
  host: "localhost",
  user: "nerea_local",
  password: "12345",
  database: "biometria",
  port: 3306
};

describe("Test Lógica de Negocio", function () {
  let logica;

  before(async function () {
    logica = new Logica(DB_CONFIG);
  });

  after(async function () {
    await logica.pool.end();
  });

  it("guardarMedida debe insertar correctamente una fila", async function () {
    const medida = await logica.guardarMedida(1, 11, 99.9, 0, 0);
    assert.equal(medida.id_placa, 1);
    assert.strictEqual(medida.tipo, 11);
    assert.strictEqual(medida.valor, 99.9);
  });

  it("listarMedidas devuelve un array con medidas", async function () {
    const medidas = await logica.listarMedidas(5);
    assert.ok(Array.isArray(medidas));
  });
});
