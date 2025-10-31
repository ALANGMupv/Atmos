// mainTestLogica.js - Pruebas unitarias de la capa de lógica
// @author: Alan Guevara Martínez
// fecha 30 / 10 / 2025

const assert = require("assert"); // Módulo nativo de Node.js para hacer comprobaciones (asserts)
const bcrypt = require("bcrypt"); // Librería para verificar hashes de contraseñas
const Logica = require("../Logica"); // Importa la clase principal que queremos probar

// -----------------------------------------------------------------------------
// CONFIGURACIÓN DE LA BASE DE DATOS PARA LAS PRUEBAS
// -----------------------------------------------------------------------------
const DB_CONFIG = {
  host: "localhost",
  user: "nagufor_user",
  password: "Atmos2025Aura",
  database: "atmos_db",
  port: 3306
};

// -----------------------------------------------------------------------------
// DESCRIPCIÓN PRINCIPAL DEL CONJUNTO DE TESTS
// -----------------------------------------------------------------------------
describe("Test Lógica de Negocio", function () {
  // Variable donde se almacenará la instancia de la clase Logica
  let logica;

  // Variable global para guardar el ID del usuario que se cree durante las pruebas
  let idUsuarioCreado;

  // ---------------------------------------------------------------------------
  // before(): se ejecuta **una sola vez** antes de todos los tests
  // ---------------------------------------------------------------------------
  before(async function () {
    // Crea una nueva instancia de la clase Logica con la configuración de BD
    logica = new Logica(DB_CONFIG);
  });

  // ---------------------------------------------------------------------------
  // after(): se ejecuta **una sola vez** después de todos los tests
  // ---------------------------------------------------------------------------
  after(async function () {
    // Cierra el pool de conexiones MySQL para liberar recursos
    await logica.pool.end();
  });

  // ---------------------------------------------------------------------------
  // TEST 1: guardarMedida()
  // ---------------------------------------------------------------------------
  it("guardarMedida() debe insertar correctamente una medida", async function () {
    /**
     * Este test comprueba que el método guardarMedida()
     * inserta correctamente una nueva medida en la tabla `medida`.
     * 
     * NOTA: En la base de datos `atmos_db`, el campo id_placa es VARCHAR,
     * por lo que la comparación debe realizarse como cadena.
     */

    // Ejecuta la función con datos de ejemplo
    const medida = await logica.guardarMedida("1", 11, 99.9, 10.5, 20.3);

    // Comprueba que la medida tenga un ID válido (indica que se insertó bien)
    assert.ok(medida.id_medida, "Debe devolver un id_medida válido");

    // Verifica que los valores almacenados sean los mismos que se enviaron
    assert.strictEqual(String(medida.id_placa), "1"); // Se convierte a string
    assert.strictEqual(medida.tipo, 11);
    assert.strictEqual(medida.valor, 99.9);
  });

  // ---------------------------------------------------------------------------
  // TEST 2: listarMedidas()
  // ---------------------------------------------------------------------------
  it("listarMedidas() debe devolver un array con las medidas más recientes", async function () {
    /**
     * Este test valida que listarMedidas() devuelva un array de registros
     * con un límite máximo definido por el parámetro pasado.
     */
    const medidas = await logica.listarMedidas(5);

    // Debe devolver un array, aunque esté vacío
    assert.ok(Array.isArray(medidas), "Debe devolver un array");

    // No debe devolver más elementos que el límite especificado
    assert.ok(medidas.length <= 5, "Debe respetar el límite máximo de registros");
  });

  // ---------------------------------------------------------------------------
  // TEST 3: guardarUsuario()
  // ---------------------------------------------------------------------------
  it("guardarUsuario() debe insertar correctamente un usuario nuevo con contraseña cifrada", async function () {
    /**
     * Este test comprueba que el método guardarUsuario():
     *  1. Inserta un usuario nuevo en la tabla `usuario`.
     *  2. Cifra la contraseña correctamente con bcrypt.
     *  3. Devuelve todos los datos del usuario recién creado.
     */

    // Genera un UID único para evitar conflictos
    const uid = "test_uid_" + Date.now();

    // Inserta el usuario usando el método guardarUsuario()
    const nuevo = await logica.guardarUsuario(
      uid,
      "Alan",
      "Guevara",
      "alan_test_" + Date.now() + "@correo.com",
      "123456"
    );

    // Comprueba que el método devolvió un objeto con ID válido (campo id_Usuario)
    assert.ok(nuevo.id_Usuario, "Debe devolver un id_Usuario válido");

    // Verifica los datos básicos
    assert.strictEqual(nuevo.nombre, "Alan");
    assert.strictEqual(nuevo.apellidos, "Guevara");

    // El estado por defecto debe ser 0 (no verificado)
    assert.strictEqual(nuevo.estado, 0, "Debe crearse como no verificado");

    // Guarda el ID del usuario creado para reutilizarlo después
    idUsuarioCreado = nuevo.id_Usuario;

    // Verifica que la contraseña almacenada esté cifrada (no texto plano)
    const esHashValido = await bcrypt.compare("123456", nuevo.contrasena);
    assert.ok(esHashValido, "La contraseña debe estar cifrada con bcrypt");
  });

  // ---------------------------------------------------------------------------
  // TEST 4: actualizarEstadoVerificado()
  // ---------------------------------------------------------------------------
  it("actualizarEstadoVerificado() debe cambiar el estado del usuario a verificado", async function () {
    /**
     * Este test valida que el método actualice el campo `estado` del usuario
     * para marcarlo como verificado en la base de datos.
     */

    // Recupera los datos actuales del usuario creado en el test anterior
    const usuario = await logica.obtenerUsuarioPorId(idUsuarioCreado);
    assert.ok(usuario, "Debe existir el usuario creado previamente");

    // Llama al método para actualizar el estado a verificado
    await logica.actualizarEstadoVerificado(usuario.uid_firebase);

    // Recupera el usuario actualizado
    const actualizado = await logica.obtenerUsuarioPorId(idUsuarioCreado);

    // Comprueba que el campo `estado` cambió a 1
    assert.strictEqual(actualizado.estado, 1, "El estado debe ser 1 (verificado)");
  });

  // ---------------------------------------------------------------------------
  // TEST 5: buscarUsuarioPorEmail()
  // ---------------------------------------------------------------------------
  it("buscarUsuarioPorEmail() debe devolver un usuario existente dado su email", async function () {
    /**
     * Este test comprueba que el método buscarUsuarioPorEmail()
     * localiza correctamente un usuario en la base de datos mediante su email.
     */
    const usuario = await logica.obtenerUsuarioPorId(idUsuarioCreado);

    // Busca por su email
    const encontrado = await logica.buscarUsuarioPorEmail(usuario.email);

    // Verifica que se haya encontrado
    assert.ok(encontrado, "Debe encontrar el usuario por su email");
    assert.strictEqual(encontrado.email, usuario.email);
  });

  // ---------------------------------------------------------------------------
  // TEST 6: actualizarUsuario()
  // ---------------------------------------------------------------------------
  it("actualizarUsuario() debe modificar los datos de un usuario existente", async function () {
    /**
     * Este test valida que el método actualizarUsuario():
     *  - Permite cambiar el nombre y apellidos de un usuario existente.
     *  - Devuelve true si la operación se realizó correctamente.
     */
    const usuarioAntes = await logica.obtenerUsuarioPorId(idUsuarioCreado);

    // Actualiza los campos de texto
    const actualizado = await logica.actualizarUsuario(idUsuarioCreado, {
      nombre: "Alan Modificado",
      apellidos: "Guevara Editado",
      email: usuarioAntes.email // mantiene el mismo email
    });

    // Debe devolver true si la actualización tuvo éxito
    assert.ok(actualizado, "Debe devolver true si se actualizó correctamente");

    // Comprueba que los datos realmente cambiaron en la base
    const usuarioDespues = await logica.obtenerUsuarioPorId(idUsuarioCreado);
    assert.strictEqual(usuarioDespues.nombre, "Alan Modificado");
    assert.strictEqual(usuarioDespues.apellidos, "Guevara Editado");
  });

  // ---------------------------------------------------------------------------
  // TEST 7: obtenerUsuarioPorId()
  // ---------------------------------------------------------------------------
  it("obtenerUsuarioPorId() debe devolver null si el usuario no existe", async function () {
    /**
     * Este test prueba el caso negativo del método obtenerUsuarioPorId():
     * debe devolver `null` si se busca un usuario con un ID inexistente.
     */
    const usuario = await logica.obtenerUsuarioPorId(-999); // ID que no existe
    assert.strictEqual(usuario, null, "Debe devolver null si no hay coincidencias");
  });
  
  // ---------------------------------------------------------------------------
  // TEST 8: vincularPlacaAUsuario()
  // ---------------------------------------------------------------------------
  it("vincularPlacaAUsuario() debe asociar una placa libre a un usuario", async function () {
    const conn = await logica.pool.getConnection();
    const idPlaca = "TEST_PLACA_" + Date.now();

    await conn.query(
      "INSERT INTO placa (id_placa, asignada, encendida) VALUES (?, 0, 0)",
      [idPlaca]
    );
    conn.release();

    const resultado = await logica.vincularPlacaAUsuario(idUsuarioCreado, idPlaca);
    assert.ok(resultado);
    assert.strictEqual(resultado.status, "ok");
    assert.match(resultado.mensaje, /vinculada/i);
  });
}); 
