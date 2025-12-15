/**
 * .............................................................................
 * @file test_logica_usuarios.js
 * @brief Tests automáticos de la capa lógica relacionados con USUARIOS.
 *
 * Métodos cubiertos:
 *   - guardarUsuario()
 *   - buscarUsuarioPorEmail()
 *   - buscarUsuarioPorUID()
 *   - actualizarUsuario()
 *   - obtenerUsuarioPorId()
 *   - actualizarEstadoVerificado()
 * 
 * @author Alan Guevara Martínez
 * @date 15/12/2025 (Actualizado)
 * .............................................................................
 */

const assert = require("assert");           // Aserciones
const Logica = require("../Logica"); // Clase a probar

// Configuración de base de datos
const configBD = {
  host: "localhost",
  user: "nagufor_user",
  password: "Atmos2025Aura",
  database: "atmos_db",
  port: 3306
};

// Instancia real de la lógica
const logica = new Logica(configBD);

describe("LOGICA - USUARIOS", function () {

  // Variables reutilizadas entre tests
  let idUsuarioCreado;
  const emailTest = "test_logica@atmos.com";
  const uidTest = "UID_TEST_LOGICA_" + Date.now(); // UID dinámico para evitar duplicados

  // --------------------------------------------------------------------------
  // Test: guardarUsuario
  // --------------------------------------------------------------------------
  it("guardarUsuario inserta usuario correctamente", async function () {

    await logica.guardarUsuario(
      uidTest,
      "Usuario",
      "Test",
      emailTest,
      "1234"
    );

    // Recuperamos el usuario real desde la BD para obtener su ID
    const usuario = await logica.buscarUsuarioPorEmail(emailTest);

    assert.ok(usuario);
    assert.ok(usuario.id_Usuario); // nombre real del campo en la BBDD

    idUsuarioCreado = usuario.id_Usuario;
  });


  // --------------------------------------------------------------------------
  // Test: buscarUsuarioPorEmail
  // --------------------------------------------------------------------------
  it("buscarUsuarioPorEmail devuelve el usuario", async function () {

    const usuario = await logica.buscarUsuarioPorEmail(emailTest);

    assert.ok(usuario);
    assert.strictEqual(usuario.email, emailTest);
  });

  // --------------------------------------------------------------------------
  // Test: buscarUsuarioPorUID
  // --------------------------------------------------------------------------
  it("buscarUsuarioPorUID devuelve el usuario", async function () {

    const usuario = await logica.buscarUsuarioPorUID(uidTest);

    assert.ok(usuario);
  });

  // --------------------------------------------------------------------------
  // Test: actualizarUsuario
  // --------------------------------------------------------------------------
  it("actualizarUsuario modifica datos básicos", async function () {

    const ok = await logica.actualizarUsuario(idUsuarioCreado, {
      nombre: "NuevoNombre",
      apellidos: "NuevoApellido",
      email: emailTest
    });

    // El método devuelve un booleano según haya cambios reales o no
    assert.strictEqual(typeof ok, "boolean");
  });

  // --------------------------------------------------------------------------
  // Test: obtenerUsuarioPorId
  // --------------------------------------------------------------------------
  it("obtenerUsuarioPorId devuelve usuario existente", async function () {

    const usuario = await logica.obtenerUsuarioPorId(idUsuarioCreado);

    assert.ok(usuario);
  });

  // --------------------------------------------------------------------------
  // Test: actualizarEstadoVerificado
  // --------------------------------------------------------------------------
  it("actualizarEstadoVerificado no lanza errores", async function () {

    // Si no lanza excepción, el test es válido
    await logica.actualizarEstadoVerificado(uidTest);
  });

});
