/**
 * .............................................................................
 * @file test_logica_medidas.js
 * @brief Tests automáticos de la capa lógica relacionados con MEDIDAS.
 *
 * Métodos cubiertos:
 *   - guardarMedida()
 *   - guardarMedidaYActualizarDistancia()
 *   - listarMedidas()
 *   - obtenerUltimaMedidaPorGas()
 *   - obtenerPromedioPorGasHoy()
 *   - obtenerPromedios7Dias()
 *   - obtenerPromedios8HorasPorGas()
 *
 * @author Alan Guevara Martínez
 * @date 15/12/2025 (Actualizado)
 * .............................................................................
 */

const assert = require("assert");           // Módulo estándar de aserciones
const Logica = require("../Logica"); // Importamos la clase a testear

// Configuración de conexión a la base de datos
const configBD = {
  host: "localhost",
  user: "nagufor_user",
  password: "Atmos2025Aura",
  database: "atmos_db",
  port: 3306
};

// Instancia real de la capa lógica (sin mocks)
const logica = new Logica(configBD);

// Identificador de placa utilizado en las pruebas
const PLACA_TEST = "PLACA_TEST";

describe("LOGICA - MEDIDAS", function () {

  // --------------------------------------------------------------------------
  // Test: guardarMedida
  // --------------------------------------------------------------------------
  it("guardarMedida debe insertar una medida y devolverla", async function () {

    // Llamada directa al método de lógica
    const medida = await logica.guardarMedida(
      PLACA_TEST, // id de la placa
      11,         // tipo de gas
      55.5,       // valor medido
      39.47,      // latitud
      -0.37       // longitud
    );

    // Verificamos que la medida se ha insertado correctamente
    assert.ok(medida.id_medida);      // Debe existir ID autoincremental
    assert.strictEqual(medida.tipo, 11);
    assert.strictEqual(medida.valor, 55.5);
  });

  // --------------------------------------------------------------------------
  // Test: guardarMedidaYActualizarDistancia
  // --------------------------------------------------------------------------
  it("guardarMedidaYActualizarDistancia guarda medida y RSSI", async function () {

    const medida = await logica.guardarMedidaYActualizarDistancia(
      PLACA_TEST,
      11,
      60,
      39.47,
      -0.37,
      -65         // RSSI simulado
    );

    // Si no lanza error y devuelve una medida, el test es correcto
    assert.ok(medida.id_medida);
  });

  // --------------------------------------------------------------------------
  // Test: listarMedidas
  // --------------------------------------------------------------------------
  it("listarMedidas devuelve un array limitado", async function () {

    // Solicitamos como máximo 5 medidas
    const filas = await logica.listarMedidas(5);

    // Siempre debe devolver un array
    assert.ok(Array.isArray(filas));

    // Nunca debe devolver más de las solicitadas
    assert.ok(filas.length <= 5);
  });

  // --------------------------------------------------------------------------
  // Test: obtenerUltimaMedidaPorGas
  // --------------------------------------------------------------------------
  it("obtenerUltimaMedidaPorGas devuelve valor y fecha", async function () {

    const ultima = await logica.obtenerUltimaMedidaPorGas(PLACA_TEST, 11);

    // Puede ser null si no hay datos, pero si existe comprobamos estructura
    if (ultima) {
      assert.ok(ultima.valor !== undefined);
      assert.ok(ultima.fecha_hora);
    }
  });

  // --------------------------------------------------------------------------
  // Test: obtenerPromedioPorGasHoy
  // --------------------------------------------------------------------------
  it("obtenerPromedioPorGasHoy devuelve número o null", async function () {

    const promedio = await logica.obtenerPromedioPorGasHoy(PLACA_TEST, 11);

    // Si hay datos hoy, debe ser un número
    if (promedio !== null) {
      assert.strictEqual(typeof promedio, "number");
    }
  });

  // --------------------------------------------------------------------------
  // Test: obtenerPromedios7Dias
  // --------------------------------------------------------------------------
  it("obtenerPromedios7Dias devuelve 7 valores", async function () {

    const valores = await logica.obtenerPromedios7Dias(PLACA_TEST, 11);

    // Siempre deben devolverse 7 posiciones
    assert.strictEqual(valores.length, 7);
  });

  // --------------------------------------------------------------------------
  // Test: obtenerPromedios8HorasPorGas
  // --------------------------------------------------------------------------
  it("obtenerPromedios8HorasPorGas devuelve 8 valores", async function () {

    const valores = await logica.obtenerPromedios8HorasPorGas(PLACA_TEST, 11);

    // Siempre deben devolverse 8 posiciones
    assert.strictEqual(valores.length, 8);
  });

});
