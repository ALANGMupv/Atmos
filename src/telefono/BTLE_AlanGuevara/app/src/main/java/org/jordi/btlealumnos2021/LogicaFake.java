package org.jordi.btlealumnos2021;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Nombre Fichero: LogicaFake.java
 * Descripción: Clase encargada de llamar a los métodos de la lógica de negocio
 * Autores: Alan Guevara Martínez
 */


public class LogicaFake {
    private static final String TAG = ">>>>";

    // Deja tu endpoint tal cual lo tenías para no tocar nada más
    private static final String API_URL = "https://nagufor.upv.edu.es/medida";
    private static final String URL_REGISTRO = "https://nagufor.upv.edu.es/usuario";
    private static final String URL_LOGIN    = "https://nagufor.upv.edu.es/login";


    // uuid: Texto, gas: Z, valor: R, contador: Z → guardarMedicion() →
    public void guardarMedicion(String uuid, int gas, float valor, int contador) {
        new Thread(() -> {
            try {
                // --- Mapeo mínimo y campos nuevos requeridos por la BBDD ---
                String idPlaca = uuid;                     // id_placa ← uuid que ya recibimos
                int    tipo    = gas;                      // tipo     ← gas
                double val      = (double) valor;          // valor    ← valor (double)
                String latitud  = String.format(Locale.US, "%.2f", 0.00); // por ahora 0.00
                String longitud = String.format(Locale.US, "%.2f", 0.00); // por ahora 0.00
                String fechaISO = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                        .format(new Date());               // fecha_hora

                JSONObject json = new JSONObject();
                json.put("id_placa",   idPlaca);
                json.put("tipo",       tipo);
                json.put("valor",      val);
                json.put("latitud",    latitud);
                json.put("longitud",   longitud);
                json.put("fecha_hora", fechaISO);

                // (Opcional) Si tu backend ya hace NOW(), comenta la línea de fecha_hora:
                // json.remove("fecha_hora");

                // LOG para ver exactamente qué se envía
                Log.d(TAG, "Enviando JSON NUEVO: " + json.toString());

                // Conexión HTTP
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                // Cuerpo JSON
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.toString().getBytes(StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();
                Log.d(TAG, "guardarMedicion(): HTTP " + code);

                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "guardarMedicion() error", e);
            }
        }).start();
    }

    // =========================================================
    // LOGIN
    // =========================================================

    /**
     * Nombre Interfaz: LoginCallback
     * Descripción: Define los posibles resultados de la petición de login al servidor.
     * Entradas:
     *  - Se usan como callbacks al completar la petición HTTP.
     * Salidas:
     *  - No retorna nada; se ejecutan los métodos según el resultado.
     * Autores: Nerea Aguilar Forés
     */
    public interface LoginCallback {
        void onLoginOk(JSONObject usuario);
        void onEmailNoVerificado();
        void onUsuarioNoExiste();
        void onErrorServidor();
        void onErrorInesperado();
    }

    /**
     * Nombre Método: loginServidor
     * Descripción: Realiza la petición de login al servidor usando el idToken de Firebase.
     * Entradas:
     *  - idToken: Token de Firebase para autenticar la llamada.
     *  - queue: Cola de Volley para ejecutar la petición.
     *  - callback: Implementación de LoginCallback para informar del resultado.
     * Salidas:
     *  - No retorna nada; notifica el resultado a través de callback.
     * Autores: Nerea Aguilar Forés
     */
    public static void loginServidor(
            String idToken,
            RequestQueue queue,
            LoginCallback callback
    ) {

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                URL_LOGIN,
                null,
                response -> {
                    try {
                        if ("ok".equals(response.optString("status"))) {
                            JSONObject usuario = response.getJSONObject("usuario");
                            callback.onLoginOk(usuario);
                        } else {
                            // Si hubiera otros estados, se podrían manejar aquí
                            callback.onErrorServidor();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onErrorInesperado();
                    }
                },
                error -> {
                    try {
                        String body = new String(error.networkResponse.data);
                        JSONObject json = new JSONObject(body);
                        String code = json.optString("error");

                        if ("EMAIL_NO_VERIFICADO".equals(code)) {
                            callback.onEmailNoVerificado();
                            return;
                        }

                        if ("USUARIO_NO_EXISTE".equals(code)) {
                            callback.onUsuarioNoExiste();
                            return;
                        }

                        callback.onErrorServidor();

                    } catch (Exception e) {
                        callback.onErrorInesperado();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + idToken);
                return headers;
            }
        };

        queue.add(req);
    }

    // =========================================================
    // REGISTRO
    // =========================================================

    /**
     * Nombre Interfaz: RegistroCallback
     * Descripción: Define los posibles resultados de la petición de registro al servidor.
     * Entradas:
     *  - Se usan como callbacks al completar la petición HTTP.
     * Salidas:
     *  - No retorna nada; se ejecutan los métodos según el resultado.
     * Autores: Nerea Aguilar Forés
     */
    public interface RegistroCallback {
        void onRegistroOk();
        void onErrorServidor();
        void onErrorInesperado();
    }

    /**
     * Nombre Método: registroServidor
     * Descripción: Realiza la petición de registro al servidor con los datos del usuario.
     * Entradas:
     *  - idToken: Token de Firebase para autenticar la llamada.
     *  - nombre: Nombre del usuario.
     *  - apellidos: Apellidos del usuario.
     *  - password: Contraseña del usuario.
     *  - queue: Cola de Volley para ejecutar la petición.
     *  - callback: Implementación de RegistroCallback para informar del resultado.
     * Salidas:
     *  - No retorna nada; notifica el resultado a través de callback.
     * Autores: Nerea Aguilar Forés
     */
    public static void registroServidor(
            String idToken,
            String nombre,
            String apellidos,
            String password,
            RequestQueue queue,
            RegistroCallback callback
    ) {

        JSONObject json = new JSONObject();
        try {
            json.put("nombre", nombre);
            json.put("apellidos", apellidos);
            json.put("contrasena", password);
        } catch (Exception e) {
            e.printStackTrace();
            callback.onErrorInesperado();
            return;
        }

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                URL_REGISTRO,
                json,
                response -> {
                    // Si el servidor no manda errores raros, aquí asumimos OK
                    callback.onRegistroOk();
                },
                error -> {
                    try {
                        // Podrías leer error.networkResponse.data si hubiera códigos concretos
                        callback.onErrorServidor();
                    } catch (Exception e) {
                        callback.onErrorInesperado();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + idToken);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(req);
    }

    /**
     * Nombre Interfaz: ActualizarUsuarioCallback
     * Descripción: Define los posibles resultados de la petición de actualización de usuario.
     * Autor: Alan Guevara Martínez
     * Fecha: 17/11/2025
     */
    public interface ActualizarUsuarioCallback {
        void onActualizacionOk();     // Se llama cuando la actualización en el servidor ha sido correcta
        void onErrorServidor();       // Se llama cuando el servidor devuelve error o respuesta no válida
        void onErrorInesperado();     // Se llama cuando ocurre una excepción no controlada
    }

    /**
     * Nombre Método: actualizarUsuarioServidor
     * Descripción:
     *   Realiza la petición PUT /usuario para actualizar nombre, apellidos y/o email
     *   de un usuario ya existente en la BBDD MySQL.
     *   Autores: Alan Guevara Martínez
     *      * Fecha: 17/11/2025
     *
     * Entradas:
     *  - idToken: Token de Firebase para autenticar la llamada (Bearer).
     *  - idUsuario: ID del usuario en la tabla usuario de MySQL.
     *  - nombre: Nuevo nombre.
     *  - apellidos: Nuevos apellidos.
     *  - email: Email actual del usuario (no se cambia, pero se envía).
     *  - contrasenaActual: Contraseña actual introducida por el usuario.
     *  - nuevaContrasena: Nueva contraseña (aquí será vacío, se usa solo para compatibilidad con el método ya hecho).
     *  - queue: Cola de Volley para ejecutar la petición.
     *  - callback: Implementación de ActualizarUsuarioCallback.
     *
     * Salidas:
     *  - No retorna nada; notifica el resultado por callback.
     */

    public static void actualizarUsuarioServidor(
            String idToken,
            int idUsuario,
            String nombre,
            String apellidos,
            String email,
            String contrasenaActual,
            String nuevaContrasena,
            RequestQueue queue,
            ActualizarUsuarioCallback callback
    ) {

        // Construimos el JSON que espera el backend (PUT /usuario)
        JSONObject json = new JSONObject();
        try {
            json.put("id_usuario", idUsuario);               // ID del usuario
            json.put("nombre", nombre);                      // Nuevo nombre
            json.put("apellidos", apellidos);                // Nuevos apellidos
            json.put("email", email);                        // Email actual (no se modifica)
            json.put("contrasena_actual", contrasenaActual); // La contraseña actual escrita por el usuario
            json.put("nueva_contrasena", nuevaContrasena);   // Nueva contraseña (vacía en este caso)
        } catch (Exception e) {
            // Si ocurre un error construyendo el JSON, se notifica como error inesperado
            e.printStackTrace();
            callback.onErrorInesperado();
            return;
        }

        // Usamos la misma URL /usuario que en el registro (pero aquí con PUT)
        final String URL_USUARIO = "https://nagufor.upv.edu.es/usuario";

        // Creamos la petición Volley de tipo PUT, enviando el JSON al backend
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.PUT,   // Método HTTP PUT
                URL_USUARIO,          // Endpoint de actualización de usuario
                json,                 // Cuerpo JSON construido arriba
                response -> {
                    try {
                        // Si el backend responde con { "status":"ok" } consideramos que ha ido bien
                        if ("ok".equals(response.optString("status"))) {
                            callback.onActualizacionOk();       // Todo correcto
                        } else {
                            callback.onErrorServidor();         // Respuesta recibida pero no válida
                        }
                    } catch (Exception e) {
                        // Cualquier excepción durante el análisis de la respuesta se considera inesperada
                        e.printStackTrace();
                        callback.onErrorInesperado();
                    }
                },
                error -> {
                    // Si ocurre un error HTTP, de red o timeout → error de servidor
                    try {
                        callback.onErrorServidor();
                    } catch (Exception e) {
                        // Si incluso al llamar al callback hay un fallo, se considera inesperado
                        callback.onErrorInesperado();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                // Cabeceras HTTP necesarias para la llamada
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + idToken); // Autenticación con token de Firebase
                headers.put("Content-Type", "application/json");   // Indica que enviamos JSON
                return headers;
            }
        };

        // Finalmente añadimos la petición a la cola de Volley para que se ejecute
        queue.add(req);
    }

    // =========================================================
    // RESUMEN USUARIO
    // =========================================================
    /**
     * Nombre Interfaz: ResumenUsuarioCallback
     * Descripción:
     *   Define los posibles resultados de la petición que obtiene
     *   información del sensor asociado al usuario.
     *
     * Entradas:
     *   - Se usa como callback del método resumenUsuario().
     *
     * Salidas:
     *   - No retorna nada; activa un método según el resultado.
     *
     * Autores: Nerea Aguilar Forés
     */
    public interface ResumenUsuarioCallback {
        void onSinPlaca();                                    // Usuario no tiene placa asociada
        void onConPlaca(String placa, double ultima, double promedio);  // Usuario con placa y datos
        void onErrorServidor();                               // Error en la simulación/petición
        void onErrorInesperado();                             // Excepción inesperada
    }

    /**
     * Nombre Método: resumenUsuario
     * Descripción: Consulta al backend si un usuario tiene una placa vinculada
     *              y devuelve sus datos resumidos (última medida y promedio).
     *
     * Entradas:
     *   - idUsuario → id del usuario guardado en sesión
     *   - queue → cola Volley para ejecutar la petición HTTP
     *   - callback → interface para devolver datos al Activity
     *
     * Salidas:
     *   - Callback indicando si tiene o no placa
     *   - Datos del sensor (última medida, promedio)
     *
     * Autora: Nerea Aguilar Forés
     */
    public static void resumenUsuario(
            int idUsuario,
            RequestQueue queue,
            ResumenUsuarioCallback callback
    ) {
        String url = "https://nagufor.upv.edu.es/resumenUsuario?id_usuario=" + idUsuario;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        String status = response.getString("status");

                        if ("sin_placa".equals(status)) {
                            callback.onSinPlaca();
                            return;
                        }

                        if ("con_placa".equals(status)) {
                            String placa = response.getString("id_placa");
                            double ultima = response.getDouble("ultima_medida");
                            double promedio = response.getDouble("promedio");

                            callback.onConPlaca(placa, ultima, promedio);
                            return;
                        }

                        callback.onErrorInesperado();

                    } catch (Exception e) {
                        callback.onErrorInesperado();
                    }
                },
                error -> callback.onErrorServidor()
        );

        queue.add(req);
    }
}

