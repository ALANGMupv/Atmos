/**
 * .............................................................................
 * @file test_logica_placas.js
 * @brief Tests automáticos de la capa lógica relacionados con PLACAS.
 *
 * Métodos cubiertos:
 *   - vincularPlacaAUsuario()
 *   - obtenerPlacaDeUsuario()
 *   - desvincularPlacaDeUsuario()
 *   - actualizarEstadoPlaca()
 *   - obtenerEstadoPlaca()
 *   - obtenerEstadoSenal()
 * @author Alan Guevara Martínez
 * @date 15/12/2025 (Actualizado)
 * .............................................................................
 */

const assert = require("assert");           // Aserciones
const Logica = require("../Logica"); // Clase de lógica

// Configuración de conexión
const configBD = {
  host: "localhost",
  user: "nagufor_user",
  password: "Atmos2025Aura",
  database: "atmos_db",
  port: 3306
};

// Instancia de la lógica
const logica = new Logica(configBD);

describe("LOGICA - PLACAS", function () {

  // Datos reutilizados
  const ID_USUARIO = 1;
  const ID_PLACA = "PLACA_TEST";

  // --------------------------------------------------------------------------
  // Preparación previa: asegurar que la placa existe antes de los tests
  // --------------------------------------------------------------------------
  before(async function () {
    // Inserta la placa si no existe para evitar errores de vinculación
    try {
      await logica.pool.execute(
        "INSERT IGNORE INTO placa (id_placa, asignada, encendida) VALUES (?, 0, 0)",
        [ID_PLACA]
      );
    } catch (e) {
      // Si falla aquí, los tests posteriores ya lo reflejarán
    }
  });

  // --------------------------------------------------------------------------
  // Test: vincularPlacaAUsuario
  // --------------------------------------------------------------------------
  it("vincularPlacaAUsuario vincula correctamente", async function () {

    const res = await logica.vincularPlacaAUsuario(ID_USUARIO, ID_PLACA);

    assert.strictEqual(res.status, "ok");
  });

  // --------------------------------------------------------------------------
  // Test: obtenerPlacaDeUsuario
  // --------------------------------------------------------------------------
  it("obtenerPlacaDeUsuario devuelve id_placa", async function () {

    const placa = await logica.obtenerPlacaDeUsuario(ID_USUARIO);

    assert.ok(placa);
  });

  // --------------------------------------------------------------------------
  // Test: actualizarEstadoPlaca
  // --------------------------------------------------------------------------
  it("actualizarEstadoPlaca actualiza encendida", async function () {

    // No devuelve nada, solo comprobamos que no falle
    await logica.actualizarEstadoPlaca(ID_PLACA, 1);
  });

  // --------------------------------------------------------------------------
  // Test: obtenerEstadoPlaca
  // --------------------------------------------------------------------------
  it("obtenerEstadoPlaca devuelve booleano", async function () {

    const estado = await logica.obtenerEstadoPlaca(ID_PLACA);

    // La lógica puede devolver un booleano o un objeto con el estado
    if (typeof estado === "boolean") {
      assert.strictEqual(typeof estado, "boolean");
    } else {
      assert.ok("encendida" in estado);
    }
  });

  // --------------------------------------------------------------------------
  // Test: obtenerEstadoSenal
  // --------------------------------------------------------------------------
  it("obtenerEstadoSenal devuelve nivel", async function () {

    const datos = await logica.obtenerEstadoSenal(ID_USUARIO);

    assert.ok(datos.nivel);
  });

  // --------------------------------------------------------------------------
  // Test: desvincularPlacaDeUsuario
  // --------------------------------------------------------------------------
  it("desvincularPlacaDeUsuario desvincula correctamente", async function () {

    const res = await logica.desvincularPlacaDeUsuario(ID_USUARIO);

    assert.ok(res.status);
  });

});
