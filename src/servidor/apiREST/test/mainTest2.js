/**
 * .............................................................................
 * @file mainTest2.js
 * @brief Pruebas automáticas del endpoint POST /resetPasswordAtmos.
 *
 * @details
 *  Este archivo comprueba tres comportamientos del endpoint:
 *     - Solicitud correcta de reset (200)
 *     - Error por campo faltante (400)
 *     - Error interno del servidor (status="error")
 *
 *  A diferencia de /usuario, este endpoint NO requiere token.
 *
 * @author Alan Guevara Martínez
 * @date 2025-12-01
 * .............................................................................
 */

const request = require("request");
const assert = require("assert");

// -----------------------------------------------------------------------------
// @brief Dirección del servidor REST a testear.
// @details Si existe la variable de entorno TEST_URL se utiliza; si no, localhost.
// -----------------------------------------------------------------------------
const IP_PUERTO = process.env.TEST_URL || "http://localhost:3000";

describe("Test POST /resetPasswordAtmos", function () {

  // ============================================================================
  /**
   * @test Solicitud correcta de reset
   * @brief Comprueba que el endpoint devuelve status="ok" cuando funciona bien.
   *
   * @details
   *  Cuerpo enviado:
   *    {
   *      correo: "test@correo.com"
   *    }
   *
   *  Se espera:
   *    - Código 200
   *    - status = "ok"
   */
  // ============================================================================
  it("POST /resetPasswordAtmos debe devolver 200 y status ok", function (done) {

    const body = { correo: "test@correo.com" };

    request.post(
      {
        url: IP_PUERTO + "/resetPasswordAtmos",
        json: body
      },
      function (err, res, respuesta) {

        assert.strictEqual(err, null);
        assert.strictEqual(res.statusCode, 200);

        assert.strictEqual(respuesta.status, "ok");

        done();
      }
    );
  });

  // ============================================================================
  /**
   * @test Campos faltantes
   * @brief Debe responder 400 cuando el campo "correo" no se envía.
   *
   * @details
   *  Cuerpo enviado:
   *    {}
   *
   *  Se espera:
   *    - Código 400
   *    - status = "error"
   *    - msg = "Falta correo"
   */
  // ============================================================================
  it("POST /resetPasswordAtmos debe devolver 400 si falta correo", function (done) {

    const body = {}; // Falta correo

    request.post(
      {
        url: IP_PUERTO + "/resetPasswordAtmos",
        json: body
      },
      function (err, res, respuesta) {

        assert.strictEqual(err, null);
        assert.strictEqual(res.statusCode, 400);

        assert.strictEqual(respuesta.status, "error");
        assert.ok(respuesta.msg.includes("Falta"));

        done();
      }
    );
  });

  // ============================================================================
  /**
   * @test Error interno
   * @brief Comprueba que el endpoint devuelve status="error" si falla Firebase
   *        o el envío del correo.
   *
   * @details
   *  Se envía un correo inventado muy improbable para provocar fallo.
   *
   *  Se espera:
   *    - Código 200 (el endpoint no lanza 500, responde JSON de error)
   *    - status = "error"
   */
  // ============================================================================
  it("POST /resetPasswordAtmos debe devolver status error si Firebase falla",
    function (done) {

      const body = { correo: "correo_que_no_existe_9999@dominioinvalido.com" };

      request.post(
        {
          url: IP_PUERTO + "/resetPasswordAtmos",
          json: body
        },
        function (err, res, respuesta) {

          assert.strictEqual(err, null);

          // El endpoint maneja errores y devuelve 200 + status error
          assert.strictEqual(res.statusCode, 200);
          assert.strictEqual(respuesta.status, "error");

          done();
        }
      );
    });

});
