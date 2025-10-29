/**
 * firebaseAdmin.js
 * -------------------------
 * Inicializa el SDK de Firebase Admin para el backend.
 *
 * Este módulo permite:
 *   - Validar tokens de autenticación enviados desde el frontend.
 *   - Acceder a servicios de Firebase (Authentication, Firestore, etc.).
 *   - Ejecutar acciones administrativas seguras desde el servidor.
 *
 * En esta versión se utiliza un archivo local de texto con la clave privada,
 * ubicado en la misma carpeta, llamado "firebase-private-key.txt".
 * Esto evita incluir el JSON completo de credenciales en el repositorio.
 *
 * Autores: Alan Guevara Martínez y Santiago Fuenmayor Ruiz
 */

const admin = require("firebase-admin");
const fs = require("fs");
const path = require("path");

// --------------------------------------------------------------------------
//  Lectura segura de la clave privada
// --------------------------------------------------------------------------
/**
 * Carga la clave privada del servicio desde un archivo de texto local.
 * El archivo "firebase-private-key.txt" contiene la clave privada sin comillas
 * ni saltos de línea adicionales, tal como se obtiene del archivo JSON original.
 */
const privateKeyPath = path.join(__dirname, "firebase-private-key.txt");
const privateKey = fs.readFileSync(privateKeyPath, "utf8");

// --------------------------------------------------------------------------
//  Configuración del objeto de credenciales del servicio
// --------------------------------------------------------------------------
/**
 * Define los campos necesarios para inicializar la app de Firebase Admin.
 * La mayoría de estos valores provienen del archivo JSON descargado
 * desde Firebase Console (Configuración del proyecto → Cuentas de servicio).
 */
const serviceAccount = {
  type: "service_account",
  project_id: "atmos-e3f6c",
  private_key_id: "8027f98f718e3e1c547bd9bd06e082dae2c695d0",
  private_key: privateKey,
  client_email: "firebase-adminsdk-fbsvc@atmos-e3f6c.iam.gserviceaccount.com",
  client_id: "102955951662312466915",
  auth_uri: "https://accounts.google.com/o/oauth2/auth",
  token_uri: "https://oauth2.googleapis.com/token",
  auth_provider_x509_cert_url: "https://www.googleapis.com/oauth2/v1/certs",
  client_x509_cert_url:
    "https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-fbsvc%40atmos-e3f6c.iam.gserviceaccount.com",
};

// --------------------------------------------------------------------------
//  Inicialización de Firebase Admin
// --------------------------------------------------------------------------
/**
 * Inicializa la aplicación de Firebase Admin utilizando las credenciales
 * configuradas en el objeto `serviceAccount`.
 */
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

// --------------------------------------------------------------------------
//  Exportación del módulo
// --------------------------------------------------------------------------
/**
 * Exporta la instancia inicializada de Firebase Admin para ser utilizada
 * en otros módulos (por ejemplo, en ReglasREST.js para validar tokens).
 */
module.exports = admin;
