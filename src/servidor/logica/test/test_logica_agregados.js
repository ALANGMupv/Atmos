/**
 * .............................................................................
 * @file test_logica_agregados.js
 * @brief Tests automáticos de métodos de agregación global.
 *
 * Métodos cubiertos:
 *   - obtenerUltimasMedidasGlobalPorGas()
 *   - obtenerUltimasMedidasGlobalTodasLasPlacas()
 *   - obtenerEstadoNodos()
 * @author Alan Guevara Martínez
 * @date 15/12/2025 (Actualizado)
 * .............................................................................
 */

const assert = require("assert");           // Aserciones
const Logica = require("../Logica"); // Lógica principal

// Configuración BD
const configBD = {
  host: "localhost",
  user: "nagufor_user",
  password: "Atmos2025Aura",
  database: "atmos_db",
  port: 3306
};


// Instancia real
const logica = new Logica(configBD);

describe("LOGICA - AGREGADOS GLOBALES", function () {

  // --------------------------------------------------------------------------
  // Test: obtenerUltimasMedidasGlobalPorGas
  // --------------------------------------------------------------------------
  it("obtenerUltimasMedidasGlobalPorGas devuelve array", async function () {

    const filas = await logica.obtenerUltimasMedidasGlobalPorGas(11);

    assert.ok(Array.isArray(filas));
  });

  // --------------------------------------------------------------------------
  // Test: obtenerUltimasMedidasGlobalTodasLasPlacas
  // --------------------------------------------------------------------------
  it("obtenerUltimasMedidasGlobalTodasLasPlacas devuelve array", async function () {

    const filas = await logica.obtenerUltimasMedidasGlobalTodasLasPlacas();

    assert.ok(Array.isArray(filas));
  });

  // --------------------------------------------------------------------------
  // Test: obtenerEstadoNodos
  // --------------------------------------------------------------------------
  it("obtenerEstadoNodos devuelve nodos", async function () {

    const nodos = await logica.obtenerEstadoNodos(5, 4, 10);

    assert.ok(Array.isArray(nodos));
  });

});
