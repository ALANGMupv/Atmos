// .....................................................................
// @file mainTest1.js
// @brief Pruebas automáticas del endpoint POST /usuario.
// @details
//   Este archivo comprueba distintos comportamientos del endpoint:
//     - Registro correcto (200)
//     - Error por campos faltantes (400)
//     - Error por usuario existente (409)
//   Se requiere un token Firebase válido para que el middleware
//   verificarToken permita el acceso.
//
// @author Alan Guevara Martínez
// @version 1.0
// @date 2025-12-01
// .....................................................................

const request = require("request");
const assert = require("assert");

// ---------------------------------------------------------------------
// @brief Dirección del servidor REST para los tests.
// @details Puede definirse mediante variable de entorno TEST_URL.
// ---------------------------------------------------------------------
const IP_PUERTO = process.env.TEST_URL || "http://localhost:3000";

// ---------------------------------------------------------------------
// @brief Token Firebase válido para pruebas.
// @details Si no se define FIREBASE_TEST_TOKEN, se usa un placeholder.
// ---------------------------------------------------------------------
const TOKEN_VALIDO = process.env.FIREBASE_TEST_TOKEN || "TOKEN_DE_EJEMPLO";

describe("Test POST /usuario", function () {

  // =====================================================================
  // @test Registro correcto del usuario
  // @brief Comprueba que el endpoint devuelve 200 y crea el usuario.
  // @details
  //   Cuerpo enviado:
  //      {
  //        nombre: "Alan",
  //        apellidos: "Guevara",
  //        contrasena: "1234"
  //      }
  //   Se espera:
  //      - Código 200
  //      - status = "ok"
  //      - Objeto usuario devuelto
  //      - emailVerificacionEnviado presente
  // =====================================================================
  it("POST /usuario debe devolver 200 y registrar el usuario", function (done) {

    const body = {
      nombre: "Alan",
      apellidos: "Guevara",
      contrasena: "1234"
    };

    request.post(
      {
        url: IP_PUERTO + "/usuario",
        headers: {
          Authorization: "Bearer " + TOKEN_VALIDO
        },
        json: body
      },
      function (err, res, responseBody) {
        assert.strictEqual(err, null);
        assert.strictEqual(res.statusCode, 200);

        assert.strictEqual(responseBody.status, "ok");
        assert.ok(responseBody.usuario, "Debe devolver el objeto usuario");
        assert.ok(
          responseBody.emailVerificacionEnviado !== undefined,
          "Debe indicar si se envió email de verificación"
        );

        done();
      }
    );
  });

  // =====================================================================
  // @test Campos faltantes
  // @brief Comprueba que el endpoint responde 400 si faltan campos.
  // @details
  //   Cuerpo enviado:
  //      { nombre: "Alan" }
  //   Se espera:
  //      - Código 400
  //      - error indicando campos faltantes
  // =====================================================================
  it("POST /usuario debe devolver 400 si faltan campos", function (done) {

    const body = { nombre: "Alan" }; // faltan apellidos y contraseña

    request.post(
      {
        url: IP_PUERTO + "/usuario",
        headers: {
          Authorization: "Bearer " + TOKEN_VALIDO
        },
        json: body
      },
      function (err, res, responseBody) {
        assert.strictEqual(err, null);
        assert.strictEqual(res.statusCode, 400);
        assert.ok(responseBody.error.includes("Faltan campos"));
        done();
      }
    );
  });

  // =====================================================================
  // @test Usuario existente
  // @brief Comprueba que el endpoint responde 409 cuando el usuario
  //        ya está registrado con el email del token Firebase.
  // @details
  //   Cuerpo enviado (similar al de registro):
  //      {
  //        nombre: "Alan",
  //        apellidos: "Guevara",
  //        contrasena: "abcd"
  //      }
  //   Se espera:
  //      - Código 409
  //      - error "El usuario ya existe"
  // =====================================================================
  it("POST /usuario debe devolver 409 si el usuario ya existe", function (done) {

    const body = {
      nombre: "Alan",
      apellidos: "Guevara",
      contrasena: "abcd"
    };

    request.post(
      {
        url: IP_PUERTO + "/usuario",
        headers: {
          Authorization: "Bearer " + TOKEN_VALIDO
        },
        json: body
      },
      function (err, res, responseBody) {
        assert.strictEqual(err, null);

        assert.strictEqual(res.statusCode, 409);
        assert.ok(responseBody.error.includes("existe"));

        done();
      }
    );
  });

});
