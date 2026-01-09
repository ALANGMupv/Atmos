/**
 * @file mainTestServidor.js
 * @brief Tests automáticos del servidor REST de Atmos Biometría.
 *
 * Este fichero contiene pruebas de integración básicas para comprobar:
 *  - Que el servidor REST está activo (GET /).
 *  - Que el endpoint de lectura de medidas funciona correctamente (GET /medidas).
 *  - Que la inserción de una medida funciona (POST /medida).
 *
 * Las pruebas se ejecutan usando Mocha como framework de testing,
 * Supertest para realizar peticiones HTTP simuladas
 * y Assert para validaciones.
 *
 * @author Alan Guevara Martínez
 * @date 2025
 * @version 1.0
 */

const request = require("supertest");
const assert = require("assert");
const app = require("./mainServidorREST");

/**
 * @constant {string} SERVER_URL
 * @brief URL base del servidor REST.
 *
 * Se obtiene desde la variable de entorno SERVER_URL o,
 * en su defecto, se utiliza localhost.
 */
const SERVER_URL = process.env.SERVER_URL || "http://localhost:3000";

/**
 * @describe Test del Servidor Atmos Biometría
 * @brief Conjunto de pruebas para validar el funcionamiento básico del servidor REST.
 */
describe("🧪 Test del Servidor Atmos Biometría", function () {

  /**
   * @brief Tiempo máximo permitido para cada test.
   */
  this.timeout(5000);

  /**
   * @test GET /
   * @brief Comprueba que el servidor REST está activo.
   *
   * Verifica que:
   *  - El código HTTP sea 200.
   *  - El cuerpo de la respuesta contenga status = "ok".
   */
  it("GET / debe responder con 200 y mensaje de servidor activo", async function () {
    const res = await request(app).get("/");
    assert.strictEqual(res.status, 200);
    assert.strictEqual(res.body.status, "ok");
  });

  /**
   * @test GET /medidas
   * @brief Comprueba que el endpoint de medidas devuelve datos correctamente.
   *
   * Verifica que:
   *  - El código HTTP sea 200.
   *  - El campo `medidas` sea un array.
   */
  it("GET /medidas debe devolver 200 y un array", async function () {
    const res = await request(app).get("/medidas?limit=5");
    assert.strictEqual(res.status, 200);
    assert.ok(Array.isArray(res.body.medidas));
  });

  /**
   * @test POST /medida
   * @brief Comprueba la inserción de una medida de prueba.
   *
   * Inserta una medida mock y valida que:
   *  - El servidor responda con código 200.
   *  - El estado sea "ok".
   *  - Se devuelva un identificador de medida generado.
   */
  it("POST /medida debe insertar una medida correctamente (mock)", async function () {

    /**
     * @brief Cuerpo de la medida de prueba.
     */
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
