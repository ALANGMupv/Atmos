/**
 * .............................................................................
 * @file test_logica_recorridos.js
 * @brief Tests automáticos de la capa lógica relacionados con RECORRIDOS.
 *
 * Métodos cubiertos:
 *   - guardarRecorridoDiario()
 *   - obtenerRecorridoHoyYAyer()
 *
 * Funcionalidad probada:
 *   - Inserción de recorridos diarios.
 *   - Acumulación de distancia para una misma fecha.
 *   - Obtención de distancia recorrida hoy y ayer.
 *
 * @author Alan Guevara Martínez
 * @date   18/12/2025
 * .............................................................................
 */

const assert = require("assert");      // Aserciones
const Logica = require("../Logica");   // Clase de lógica

// Configuración de conexión a la base de datos
const configBD = {
    host: "localhost",
    user: "nagufor_user",
    password: "Atmos2025Aura",
    database: "atmos_db",
    port: 3306
};

// Instancia de la lógica
const logica = new Logica(configBD);

describe("LOGICA - RECORRIDOS", function () {

    // Datos reutilizados en los tests
    const ID_USUARIO = 37;
    const DISTANCIA_1 = 1000;
    const DISTANCIA_2 = 500;

    // --------------------------------------------------------------------------
    // Preparación previa: limpiar recorridos del usuario de hoy y ayer
    // --------------------------------------------------------------------------
    before(async function () {

        await logica.pool.execute(
            `
      DELETE FROM recorrido_diario
      WHERE id_usuario = ?
        AND fecha IN (CURDATE(), CURDATE() - INTERVAL 1 DAY)
      `,
            [ID_USUARIO]
        );
    });

    // --------------------------------------------------------------------------
    // Test: guardarRecorridoDiario (fecha actual)
    // --------------------------------------------------------------------------
    it("guardarRecorridoDiario guarda recorrido del día actual", async function () {

        // No devuelve nada, solo comprobamos que no falle
        await logica.guardarRecorridoDiario(
            ID_USUARIO,
            DISTANCIA_1,
            null   // fuerza uso de CURDATE()
        );
    });

    // --------------------------------------------------------------------------
    // Test: guardarRecorridoDiario acumula distancia en la misma fecha
    // --------------------------------------------------------------------------
    it("guardarRecorridoDiario acumula distancia si ya existe el día", async function () {

        // Segunda inserción para el mismo día
        await logica.guardarRecorridoDiario(
            ID_USUARIO,
            DISTANCIA_2,
            null
        );

        const datos = await logica.obtenerRecorridoHoyYAyer(ID_USUARIO);

        // La distancia de hoy debe ser la suma
        assert.strictEqual(datos.hoy, DISTANCIA_1 + DISTANCIA_2);
    });

    // --------------------------------------------------------------------------
    // Test: guardarRecorridoDiario con fecha explícita (ayer)
    // --------------------------------------------------------------------------
    it("guardarRecorridoDiario guarda recorrido para una fecha concreta", async function () {

        const fechaAyer = new Date(Date.now() - 86400000)
            .toISOString()
            .slice(0, 10); // YYYY-MM-DD

        await logica.guardarRecorridoDiario(
            ID_USUARIO,
            700,
            fechaAyer
        );

        const datos = await logica.obtenerRecorridoHoyYAyer(ID_USUARIO);

        assert.strictEqual(datos.ayer, 700);
    });

    // --------------------------------------------------------------------------
    // Test: obtenerRecorridoHoyYAyer devuelve estructura correcta
    // --------------------------------------------------------------------------
    it("obtenerRecorridoHoyYAyer devuelve hoy y ayer", async function () {

        const datos = await logica.obtenerRecorridoHoyYAyer(ID_USUARIO);

        // Comprobación de estructura
        assert.ok("hoy" in datos);
        assert.ok("ayer" in datos);

        // Valores numéricos o null
        assert.ok(typeof datos.hoy === "number" || datos.hoy === null);
        assert.ok(typeof datos.ayer === "number" || datos.ayer === null);
    });

});
