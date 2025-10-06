// .....................................................................
// @author: Alan Guevara Martínez
// mainTestLogica.js - Pruebas unitarias de la capa de Lógica de Negocio
// .....................................................................

const assert  = require("assert");
const Logica  = require("../Logica"); // importa la clase de logica/Logica.js

// Configuración de conexión a la BD (igual que en mainServidorREST.js)
const DB_CONFIG = {
    host: "localhost",
    port: 3306,
    user: "alan",
    password: "12345pleskGuevara",
    database: "aguemar_proyecto_biometria"
};

describe("Test de Lógica de Negocio", function() {
    let logica;

    // Antes de los tests, instanciamos la lógica
    before(async function() {
        logica = new Logica(DB_CONFIG);
    });

    // Después de todos los tests, cerramos el pool
    after(async function() {
        await logica.pool.end();
    });

    it("guardarMedida inserta y devuelve la fila correctamente", async function() {
        const medida = await logica.guardarMedida("TEST-UUID", 12, 123, 99);
        assert.strictEqual(medida.uuid, "TEST-UUID");
        assert.strictEqual(medida.gas, 12);
        assert.strictEqual(medida.valor, 123);
        assert.strictEqual(medida.contador, 99);
    });

    it("listarMedidas devuelve un array con medidas", async function() { // Confirmación de que la función está aplicando bien la seguridad en el límite
        const medidas = await logica.listarMedidas(5);
        assert.ok(Array.isArray(medidas), "¿No devolvió array?");
        assert.ok(medidas.length > 0, "¿Array vacío?");
    });
});
