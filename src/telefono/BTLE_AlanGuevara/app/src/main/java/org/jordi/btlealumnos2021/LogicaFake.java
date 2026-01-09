package org.jordi.btlealumnos2021;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONObject;

/**
 * @file LogicaFake.java
 * @brief Clase encargada de llamar a los métodos de la lógica de negocio.
 *
 * @details Esta clase actúa como intermediaria y gestiona las invocaciones
 * a la lógica de negocio.
 *
 * @authors Grupo 1.4 - Atmos
 */


public class LogicaFake {
    private static final String TAG = ">>>>";

    // Endpoints
    private static final String API_URL = "https://nagufor.upv.edu.es/medida"; // Parece que no se usa
    private static final String URL_REGISTRO = "https://nagufor.upv.edu.es/usuario";
    private static final String URL_LOGIN    = "https://nagufor.upv.edu.es/login";
    private static final String URL_VINCULAR = "https://nagufor.upv.edu.es/vincular";
    private static final String URL_DESVINCULAR = "https://nagufor.upv.edu.es/desvincular";
    private static final String URL_RECORRIDO = "https://nagufor.upv.edu.es/recorrido";
    private static final String URL_INCIDENCIA = "https://nagufor.upv.edu.es/incidencia";

    /**
     * @brief Envía al backend una medición (ahora también permite valores promediados).
     *
     * @param uuid     UUID de la placa.
     * @param gas      Tipo de gas.
     * @param valor    Valor medido o promedio de 10 muestras.
     * @param rssi     RSSI medido o promedio.
     * @param lat      Latitud real o -1 si no disponible.
     * @param lon      Longitud real o -1 si no disponible.
     *
     * @author Alan Guevara Martínez
     * @date 05/12/2025 (modificado a esta fecha)
     */
    public void guardarMedicion(String uuid, int gas, float valor, int rssi, double lat, double lon) {

        new Thread(() -> {
            try {
                URL url = new URL("https://nagufor.upv.edu.es/medida");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("id_placa", uuid);
                json.put("tipo", gas);
                json.put("valor", valor);
                json.put("latitud", lat);
                json.put("longitud", lon);
                json.put("rssi", rssi);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.toString().getBytes(StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();
                Log.d(">>>", "Medición enviada (promedio o normal) HTTP=" + code);

                conn.disconnect();

            } catch (Exception e) {
                Log.e(">>>", "Error enviando medición", e);
            }
        }).start();
    }



    /// =========================================================
    /// LOGIN
    /// =========================================================

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

    /// =========================================================
    /// REGISTRO
    /// =========================================================

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

    /// =========================================================
    /// RESUMEN USUARIO
    /// =========================================================
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
        void onConPlaca(String placa, double ultimaValor, String ultimaFecha, double promedio);  // Usuario con placa y datos
        void onErrorServidor();                               // Error en la simulación/petición
        void onErrorInesperado();                             // Excepción inesperada
    }

    /// =========================================================
/// RESUMEN USUARIO POR GAS
/// =========================================================

    /**
     * Nombre Método: resumenUsuario
     * Descripción:
     *      Consulta al backend si el usuario tiene placa y devuelve
     *      la última medida + promedio del gas seleccionado.
     *
     * Entradas:
     *   - idUsuario : ID del usuario
     *   - tipoGas   : código del gas (11, 12, 13, 14)
     *   - queue     : cola Volley
     *   - callback  : interface con los 4 posibles resultados
     *
     * Autor: Nerea Aguilar Forés
     * Modificado por: Alan Guevara Martínez (20/11/2025)
     */
    public static void resumenUsuarioPorGas(
            int idUsuario,
            int tipoGas,
            RequestQueue queue,
            ResumenUsuarioCallback callback
    ) {

        // URL actualizada
        String url = "https://nagufor.upv.edu.es/resumenUsuarioPorGas"
                + "?id_usuario=" + idUsuario
                + "&tipo=" + tipoGas
                + "&t=" + System.currentTimeMillis();  // ← Rompe caché

        // Petición GET
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        // Leemos el campo "status"
                        String status = response.optString("status", "");

                        // Caso: usuario sin placa
                        if ("sin_placa".equals(status)) {
                            callback.onSinPlaca();
                            return;
                        }

                        // Caso: usuario con placa
                        if ("con_placa".equals(status)) {

                            // Leer última medida
                            double ultima = 0;
                            JSONObject objUltima = response.optJSONObject("ultima_medida");

                            if (objUltima != null) {
                                ultima = objUltima.optDouble("valor", 0);
                            }

                            String fecha = "";
                            if (objUltima != null) {
                                fecha = objUltima.optString("fecha_hora", "");
                            }


                            // Leer promedio
                            double promedio = response.optDouble("promedio", 0);

                            // Devolver datos
                            callback.onConPlaca(
                                    response.optString("id_placa", ""),
                                    ultima,
                                    fecha,
                                    promedio
                            );
                            return;
                        }

                        // Cualquier cosa rara
                        callback.onErrorInesperado();

                    } catch (Exception e) {
                        callback.onErrorInesperado();
                    }
                },
                error -> callback.onErrorServidor()
        );

        // Desactivar caché de Volley
        req.setShouldCache(false);

        // Ejecutar petición
        queue.add(req);
    }


    /// =========================================================
    /// VINCULAR PLACA A USUARIO
    /// =========================================================

    /**
     * Nombre Interfaz: VincularPlacaCallback
     * Descripción:
     *   Define los posibles resultados de la petición de vinculación
     *   de una placa a un usuario (/vincular).
     *
     * Autora: Alan Guevara Martínez
     * Fecha: 18/11/2025
     */
    public interface VincularPlacaCallback {
        void onVinculacionOk();       // Vinculación correcta
        void onCodigoNoValido();      // Placa no encontrada o ya asignada
        void onErrorServidor();       // Error de servidor / HTTP
        void onErrorInesperado();     // Excepción no controlada al procesar
    }

    /**
     * Nombre Método: vincularPlacaServidor
     * Descripción:
     *   Llama al endpoint POST /vincular para asociar una placa a un usuario.
     *   Envía al backend los campos:
     *      - id_usuario
     *      - id_placa
     *
     * Entradas:
     *  - idUsuario: ID del usuario en la tabla MySQL.
     *  - idPlaca:   Código/UUID de la placa a vincular.
     *  - queue:     Cola de Volley para ejecutar la petición HTTP.
     *  - callback:  Implementación de VincularPlacaCallback para informar del resultado.
     *
     * Salidas:
     *  - No retorna nada. Informa del resultado mediante callback.
     *
     * Autor: Alan Guevara Martínez
     * Fecha: 18/11/2025
     */

    public static void vincularPlacaServidor(
            int idUsuario,                // ID del usuario que quiere vincular la placa
            String idPlaca,               // ID o código de la placa a vincular
            RequestQueue queue,           // Cola de peticiones Volley para ejecutar la solicitud HTTP
            VincularPlacaCallback callback // Callback con los métodos para manejar las respuestas
    ) {

        // Construimos el JSON que enviaremos al backend
        JSONObject json = new JSONObject();
        try {
            json.put("id_usuario", idUsuario); // Insertamos el ID del usuario dentro del JSON
            json.put("id_placa", idPlaca);     // Insertamos el ID/código de la placa dentro del JSON

        } catch (Exception e) {                 // Si ocurre cualquier error construyendo el JSON...
            e.printStackTrace();                // ...imprimimos el error en consola
            callback.onErrorInesperado();       // ...y notificamos error inesperado al callback
            return;                             // ...y salimos del método
        }

        // Creamos la petición HTTP al servidor usando método POST y enviando el JSON creado
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST, // Método HTTP POST
                URL_VINCULAR,        // URL del backend que realiza la vinculación
                json,                // Cuerpo JSON enviado en la petición
                response -> {        // Listener cuando el servidor responde correctamente

                    try {
                        // Se espera un JSON similar a: { status: "ok", mensaje: "Placa vinculada correctamente" }
                        String status = response.optString("status", ""); // Obtenemos el campo "status" del JSON

                        if ("ok".equals(status)) {  // Si el servidor devuelve "ok"...
                            callback.onVinculacionOk(); // ...notificamos vinculación exitosa
                        } else {                    // Si el servidor no devolvió "ok"
                            callback.onCodigoNoValido(); // ...tratamos el caso como código no válido
                        }

                    } catch (Exception e) {          // Si algo falla procesando la respuesta del servidor...
                        e.printStackTrace();         // ...lo imprimimos
                        callback.onErrorInesperado(); // ...y notificamos error inesperado
                    }
                },

                error -> { // Listener cuando la petición falla o el servidor devuelve error HTTP

                    try {
                        // Si el servidor ha devuelto un error con contenido (normalmente mensajes como
                        // "Placa no encontrada" o "La placa ya está asignada a otro usuario")
                        if (error != null &&
                                error.networkResponse != null &&
                                error.networkResponse.data != null) {

                            callback.onCodigoNoValido(); // Consideramos el error como código/placa no válido

                        } else { // Si no hay detalles útiles del error
                            callback.onErrorServidor(); // Notificamos error genérico del servidor
                        }

                    } catch (Exception e) { // Si algo falla incluso manejando el error...
                        callback.onErrorInesperado(); // ...notificamos error inesperado
                    }
                }
        ) {

            @Override
            public Map<String, String> getHeaders() {
                // Cabeceras HTTP que se envían al servidor
                Map<String, String> headers = new HashMap<>();       // Creamos mapa de cabeceras
                headers.put("Content-Type", "application/json");     // Declaramos que el cuerpo es JSON
                return headers;                                      // Devolvemos las cabeceras
            }
        };

        // Finalmente, añadimos la petición a la cola para que Volley la ejecute
        queue.add(req);
    }

    /// =========================================================
    /// DESVINCULAR PLACA DE USUARIO
    /// =========================================================

    /**
     * Nombre Interfaz: DesvincularPlacaCallback
     * Descripción:
     *   Define los posibles resultados de la petición de
     *   desvinculación de una placa para un usuario.
     *
     * Autor: Alan Guevara Martínez
     * Fecha: 19/11/2025
     */
    public interface DesvincularPlacaCallback {
        void onDesvinculacionOk();    // Se ha desvinculado correctamente
        void onUsuarioSinPlaca();     // El usuario no tenía placa vinculada
        void onErrorServidor();       // Error HTTP / servidor
        void onErrorInesperado();     // Excepción al procesar la respuesta
    }

    /**
     * Nombre Método: desvincularPlacaServidor
     * Descripción:
     *   Llama al endpoint POST /desvincular para eliminar la relación
     *   entre un usuario y su placa (tabla usuarioplaca) y poner
     *   asignada = 0 en la tabla placa.
     *
     * Entradas:
     *   - idUsuario: ID del usuario en MySQL.
     *   - queue:     Cola Volley para ejecutar la petición HTTP.
     *   - callback:  Implementación de DesvincularPlacaCallback.
     *
     * Salidas:
     *   - No retorna nada, pero notificará el resultado mediante callback.
     *
     * Autor: Alan Guevara Martínez
     * Fecha: 19/11/2025
     */
    public static void desvincularPlacaServidor(
            int idUsuario,                          // ID del usuario logueado
            RequestQueue queue,                     // Cola de peticiones Volley
            DesvincularPlacaCallback callback       // Callback con el resultado
    ) {

        // Construimos el JSON con el id_usuario que espera el backend
        JSONObject json = new JSONObject();
        try {
            json.put("id_usuario", idUsuario);      // Inserta el id_usuario en el objeto JSON
        } catch (Exception e) {                     // Si algo falla construyendo el JSON...
            e.printStackTrace();                    // ...mostramos error en Logcat
            callback.onErrorInesperado();           // ...y avisamos al callback
            return;                                 // ...y salimos del método
        }

        // Creamos la petición HTTP POST a /desvincular
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,                // Método HTTP POST
                URL_DESVINCULAR,                   // URL del endpoint
                json,                              // Cuerpo JSON con id_usuario
                response -> {                      // Listener de respuesta OK (código 2xx)

                    try {
                        // Se espera algo del estilo: { "status":"ok", "mensaje":"..." }
                        String status = response.optString("status", "");

                        if ("ok".equals(status)) {                 // Caso éxito
                            callback.onDesvinculacionOk();
                        } else if ("sin_placa".equals(status)) {   // Usuario sin placa
                            callback.onUsuarioSinPlaca();
                        } else {                                   // Cualquier otro status
                            callback.onErrorServidor();
                        }

                    } catch (Exception e) {         // Si falla el parseo del JSON...
                        e.printStackTrace();
                        callback.onErrorInesperado();
                    }
                },
                error -> {                          // Listener de error HTTP / red

                    try {
                        // Aquí consideramos cualquier error de red como error de servidor
                        callback.onErrorServidor();
                    } catch (Exception e) {
                        // Si al notificar el error algo más falla, lo marcamos como inesperado
                        callback.onErrorInesperado();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                // En este endpoint no estamos usando token, solo indicamos JSON
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // Añadimos la petición a la cola de Volley para que se ejecute
        queue.add(req);
    }

    /**
     * Nombre Método: actualizarEstadoPlaca
     * Fecha: 20/11/2025
     * Autor: Alan Guevara Martínez
     *
     * Descripción:
     *   Envía al backend el estado actual de la placa (encendida/apagada).
     *   El backend debe actualizar la columna `encendida` de la tabla placa.
     *
     * Parámetros:
     *   - idPlaca : UUID de la placa
     *   - estado  : 1 = encendida, 0 = apagada
     */
    public void actualizarEstadoPlaca(String idPlaca, int estado) {

        new Thread(() -> {
            try {
                URL url = new URL("https://nagufor.upv.edu.es/actualizarEstadoPlaca");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("id_placa", idPlaca);
                json.put("encendida", estado);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.toString().getBytes(StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();
                Log.d(TAG, "Respuesta /actualizarEstadoPlaca: HTTP " + code);

                conn.disconnect();

            } catch (Exception e) {
                Log.e(TAG, "Error actualizando estado placa", e);
            }
        }).start();
    }


    public interface GraficaCallback {
        void onSinPlaca();
        void onDatosObtenidos(List<String> labels, List<Float> valores, double promedio);
        void onErrorServidor();
        void onErrorInesperado();
    }

    /**
     * Nombre Método: resumen7Dias
     * Descripción:
     *   Llama al endpoint GET /resumen7Dias para obtener los promedios
     *   diarios de los últimos 7 días del gas indicado para el usuario.
     *
     * Entradas:
     *   - idUsuario: ID del usuario en MySQL.
     *   - tipoGas:   Código del gas (11,12,13,14).
     *   - queue:     Cola Volley para ejecutar la petición HTTP.
     *   - callback:  Implementación de GraficaCallback para recibir el resultado.
     *
     * Salidas:
     *   - No retorna nada directamente.
     *   - Notifica a través de callback:
     *       - onSinPlaca() si el servidor responde status="sin_placa".
     *       - onDatosObtenidos(labels, valores, promedio) si hay datos.
     *       - onErrorServidor() si hay error de servidor.
     *       - onErrorInesperado() si hay error de parseo u otro.
     *
     * Autor: Alan Guevara Martínez
     * Fecha: 21/11/2025
     */
    public static void resumen7Dias(
            int idUsuario,
            int tipoGas,
            RequestQueue queue,
            GraficaCallback callback
    ) {
        try {
            String url = "https://nagufor.upv.edu.es" +
                    "/resumen7Dias?id_usuario=" + idUsuario +
                    "&tipo=" + tipoGas;

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    response -> {
                        try {
                            String status = response.optString("status", "");

                            if ("sin_placa".equals(status)) {
                                callback.onSinPlaca();
                                return;
                            }

                            JSONArray arrLabels = response.getJSONArray("labels");
                            JSONArray arrValores = response.getJSONArray("valores");
                            double promedio = response.getDouble("promedio");

                            List<String> labels = new ArrayList<>();
                            List<Float> valores = new ArrayList<>();

                            for (int i = 0; i < arrLabels.length(); i++) {
                                labels.add(arrLabels.getString(i));
                                valores.add((float) arrValores.getDouble(i));
                            }

                            callback.onDatosObtenidos(labels, valores, promedio);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            callback.onErrorInesperado();
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        callback.onErrorServidor();
                    }
            );

            queue.add(request);

        } catch (Exception e) {
            e.printStackTrace();
            callback.onErrorInesperado();
        }
    }


    /**
     * Nombre Método: resumen8Horas
     * Descripción:
     *   Llama al endpoint GET /resumen8Horas para obtener los promedios
     *   horarios de las últimas 8 horas del gas indicado para el usuario.
     *
     * Entradas:
     *   - idUsuario: ID del usuario en MySQL.
     *   - tipoGas:   Código del gas (11,12,13,14).
     *   - queue:     Cola Volley para ejecutar la petición HTTP.
     *   - callback:  Implementación de GraficaCallback para recibir el resultado.
     *
     * Salidas:
     *   - No retorna nada directamente.
     *   - Notifica a través de callback:
     *       - onSinPlaca() si el servidor responde status="sin_placa".
     *       - onDatosObtenidos(labels, valores, promedio) si hay datos.
     *       - onErrorServidor() si hay error de servidor.
     *       - onErrorInesperado() si hay error de parseo u otro.
     *
     * Autor: Alan Guevara Martínez
     * Fecha: 21/11/2025
     */
    public static void resumen8Horas(
            int idUsuario,
            int tipoGas,
            RequestQueue queue,
            GraficaCallback callback
    ) {
        try {
            String url = "https://nagufor.upv.edu.es" +
                    "/resumen8Horas?id_usuario=" + idUsuario +
                    "&tipo=" + tipoGas;

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    response -> {
                        try {
                            String status = response.optString("status", "");

                            if ("sin_placa".equals(status)) {
                                callback.onSinPlaca();
                                return;
                            }

                            JSONArray arrLabels = response.getJSONArray("labels");
                            JSONArray arrValores = response.getJSONArray("valores");
                            double promedio = response.getDouble("promedio");

                            List<String> labels = new ArrayList<>();
                            List<Float> valores = new ArrayList<>();

                            for (int i = 0; i < arrLabels.length(); i++) {
                                labels.add(arrLabels.getString(i));
                                valores.add((float) arrValores.getDouble(i));
                            }

                            callback.onDatosObtenidos(labels, valores, promedio);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            callback.onErrorInesperado();
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        callback.onErrorServidor();
                    }
            );

            queue.add(request);

        } catch (Exception e) {
            e.printStackTrace();
            callback.onErrorInesperado();
        }
    }

    public interface EstadoPlacaCallback {
        void onActivo();
        void onInactivo();
        void onSinPlaca();
        void onErrorServidor();
        void onErrorInesperado();
    }

    /**
     * Nombre Método: estadoPlacaServidor
     * Descripción:
     *     Llama al endpoint GET /estadoPlaca para obtener si la placa
     *     del usuario está activa, inactiva o si no tiene placa.
     *
     * Autora: Nerea Aguilar Forés
     * Fecha: 21/11/2025
     */
    public static void estadoPlacaServidor(
            int idUsuario,
            RequestQueue queue,
            EstadoPlacaCallback callback
    ) {

        String url = "https://nagufor.upv.edu.es/estadoPlaca?id_usuario=" + idUsuario;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        String estado = response.optString("estado", "sin_placa");

                        switch (estado) {
                            case "activo":
                                callback.onActivo();
                                break;
                            case "inactivo":
                                callback.onInactivo();
                                break;
                            default:
                                callback.onSinPlaca();
                                break;
                        }

                    } catch (Exception e) {
                        callback.onErrorInesperado();
                    }
                },
                error -> callback.onErrorServidor()
        );

        queue.add(req);
    }

    public interface EstadoSenalCallback {
        void onResultado(String nivel, int rssi);
        void onErrorServidor();
        void onErrorInesperado();
    }

    /**
     * Nombre Método: estadoSenalServidor
     * Descripción:
     *     Llama al endpoint GET /estadoSenal para obtener la intensidad
     *     de la señal del sensor del usuario (nivel + rssi).
     *
     * Autora: Nerea Aguilar Forés
     * Fecha: 21/11/2025
     */
    public static void estadoSenalServidor(
            int idUsuario,
            RequestQueue queue,
            EstadoSenalCallback callback
    ) {

        String url = "https://nagufor.upv.edu.es/estadoSenal?id_usuario=" + idUsuario;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        String status = response.optString("status", "");
                        if (!"ok".equals(status)) {
                            callback.onErrorServidor();
                            return;
                        }

                        String nivel = response.optString("nivel", "sin_datos");
                        int rssi = response.optInt("rssi", 0);

                        callback.onResultado(nivel, rssi);

                    } catch (Exception e) {
                        callback.onErrorInesperado();
                    }
                },
                error -> callback.onErrorServidor()
        );

        queue.add(req);
    }

    /**
     * @brief Obtiene del backend todas las placas con sus últimas mediciones.
     *
     * @details Llama al endpoint /mapa/medidas/todos y devuelve una lista de objetos
     * JSON con latitud, longitud y mediciones de NO2, CO, O3 y SO2.
     *
     * @param callback Callback que recibe el JSONArray de placas o null en error.
     *
     * @date 10/12/2025
     * @author Alan Guevara Martínez
     */
    public void obtenerMedidasTodos(CallbackJSONArray callback) {

        // Crear un nuevo hilo para evitar bloquear el hilo principal
        new Thread(() -> {
            try {
                // URL del endpoint que devuelve todas las medidas
                URL url = new URL("https://nagufor.upv.edu.es/mapa/medidas/todos");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                // Configurar método GET y cabeceras
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                // Obtener respuesta del servidor
                InputStream is = conn.getInputStream();

                // Leer el JSON completo de la respuesta
                String json = new java.util.Scanner(is).useDelimiter("\\A").next();

                // Parsear a un objeto JSON
                JSONObject obj = new JSONObject(json);

                // Verificar que el estado sea "ok"
                if (!obj.getString("status").equals("ok")) {
                    callback.onResult(null);
                    return;
                }

                // Extraer el array de placas de la respuesta
                JSONArray placas = obj.getJSONArray("placas");

                // Enviar el resultado a través del callback
                callback.onResult(placas);

            } catch (Exception e) {
                // Log del error para diagnóstico
                e.printStackTrace();

                // Notificar error al callback
                callback.onResult(null);
            }
        }).start();
    }

    /**
     * @brief Obtiene del backend la última medición de un gas específico por cada placa.
     *
     * @details Llama al endpoint /mapa/medidas/gas?tipo=X obteniendo latitud, longitud
     * y valor medido.
     *
     * @param tipoGas Código del gas (11,12,13,14).
     * @param callback Callback que recibe el JSONArray de medidas o null en caso de error.
     *
     * @date 10/12/2025
     * @author Alan Guevara Martínez
     */
    public void obtenerMedidasPorGas(int tipoGas, CallbackJSONArray callback) {

        // Crear un nuevo hilo para mantener libre el hilo principal
        new Thread(() -> {
            try {
                // Construcción de la URL incluyendo el parámetro del tipo de gas
                URL url = new URL("https://nagufor.upv.edu.es/mapa/medidas/gas?tipo=" + tipoGas);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                // Configuración de la petición GET
                conn.setRequestMethod("GET");

                // Obtener flujo de respuesta del servidor
                InputStream is = conn.getInputStream();

                // Convertir toda la respuesta a un String (JSON bruto)
                String json = new java.util.Scanner(is).useDelimiter("\\A").next();

                // Parseo del JSON recibido
                JSONObject obj = new JSONObject(json);

                // Verificación del estado devuelto por el servidor
                if (!obj.getString("status").equals("ok")) {
                    callback.onResult(null);
                    return;
                }

                // Extraer el array de medidas del JSON
                JSONArray medidas = obj.getJSONArray("medidas");

                // Enviar el resultado al callback
                callback.onResult(medidas);

            } catch (Exception e) {
                // Mostrar trazas del error en consola
                e.printStackTrace();

                // Notificar el error al callback
                callback.onResult(null);
            }
        }).start();
    }

    /**
     * @interface CallbackJSONArray
     * @brief Callback genérico para devolver un JSONArray.
     * @date 10/12/2025
     * @author Alan Guevara Martínez
     */
    public interface CallbackJSONArray {
        void onResult(JSONArray arr);
    }

    /* ------------------- DISTANCIA RECORRIDA ------------------- */
    /**
     * @interface CallbackRecorrido
     * @brief Callback para devolver el recorrido del usuario (hoy y ayer).
     *
     * @author Alan Guevara Martínez
     * @date 17/12/2025
     */
    public interface CallbackRecorrido {

        /**
         * @brief Respuesta con las distancias de hoy y ayer.
         *
         * @param hoy  Metros recorridos hoy
         * @param ayer Metros recorridos ayer
         */
        void onRespuesta(double hoy, double ayer);
    }

    /**
     * @brief Envía al backend la distancia acumulada de un recorrido.
     *
     * Llama al endpoint POST /recorrido enviando la distancia recorrida
     * para que el backend la acumule en el día correspondiente.
     *
     * @param idUsuario ID del usuario
     * @param distancia Distancia acumulada en metros
     * @param queue     Cola Volley para ejecutar la petición
     *
     * @author Alan Guevara Martínez
     * @date 17/12/2025
     */
    public static void guardarRecorrido(
            int idUsuario,
            double distancia,
            RequestQueue queue
    ) {

        try {
            // Construir el cuerpo JSON
            JSONObject body = new JSONObject();
            body.put("id_usuario", idUsuario);
            body.put("distancia_m", distancia);

            // Crear petición POST
            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.POST,
                    URL_RECORRIDO,
                    body,
                    r -> {
                        // Guardado correcto (sin acción adicional)
                    },
                    e -> Log.e(TAG, "Error guardando recorrido", e)
            );

            // Enviar petición
            queue.add(req);

        } catch (Exception e) {
            Log.e(TAG, "Error creando JSON de recorrido", e);
        }
    }

    /**
     * @brief Obtiene del backend los metros recorridos hoy y ayer.
     *
     * Llama al endpoint GET /recorrido?id_usuario=XX.
     * En caso de error, devuelve valores 0 para no romper la UI.
     *
     * @param idUsuario ID del usuario
     * @param queue     Cola Volley
     * @param callback  Callback con (hoy, ayer)
     *
     * @author Alan Guevara Martínez
     * @date 17/12/2025
     */
    public static void obtenerRecorrido(
            int idUsuario,
            RequestQueue queue,
            CallbackRecorrido callback
    ) {

        String url = URL_RECORRIDO + "?id_usuario=" + idUsuario;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                r -> {
                    double hoy = r.optDouble("hoy", 0);
                    double ayer = r.optDouble("ayer", 0);
                    callback.onRespuesta(hoy, ayer);
                },
                e -> {
                    Log.e(TAG, "Error obteniendo recorrido", e);
                    callback.onRespuesta(0, 0);
                }
        );

        queue.add(req);
    }
    /* ------------------- FIN DISTANCIA RECORRIDA ------------------- */

    /**
     * @brief Envía una incidencia al backend.
     *
     * Llama al endpoint POST /incidencia enviando los datos de la incidencia
     * introducidos por el usuario.
     *
     * @param idUsuario ID del usuario
     * @param asunto    Asunto o título de la incidencia
     * @param mensaje   Texto descriptivo de la incidencia
     * @param idPlaca   Identificador de la placa (puede ser null)
     * @param queue     Cola Volley
     * @param callback  Callback con el resultado de la operación
     *
     * @author Nerea Aguilar Forés
     * @date 17/12/2025
     */
    public static void enviarIncidencia(
            int idUsuario,
            String asunto,
            String mensaje,
            String idPlaca,
            RequestQueue queue,
            EnviarIncidenciaCallback callback
    ) {

        try {

            JSONObject body = new JSONObject();
            body.put("id_usuario", idUsuario);
            body.put("asunto", asunto);
            body.put("mensaje_enviado", mensaje);

            if (idPlaca != null) {
                body.put("id_placa", idPlaca);
            }

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    URL_INCIDENCIA,
                    body,
                    response -> {
                        if ("ok".equals(response.optString("status"))) {
                            callback.onOk(response.optInt("id_incidencia"));
                        } else {
                            callback.onErrorServidor();
                        }
                    },
                    error -> callback.onErrorServidor()
            );

            queue.add(request);

        } catch (Exception e) {
            callback.onErrorInesperado();
        }
    }

    /**
     * Nombre Interfaz: EnviarIncidenciaCallback
     * Descripción:
     *   Define los posibles resultados del envío de una incidencia al servidor.
     *
     * Autora: Nerea Aguilar Forés
     * Fecha: 17/12/2025
     */
    public interface EnviarIncidenciaCallback {
        void onOk(int idIncidencia);   // Incidencia creada correctamente
        void onErrorServidor();        // Error HTTP / servidor
        void onErrorInesperado();      // Excepción no controlada
    }

    /**
     * Nombre Método: listarIncidenciasUsuario
     * Descripción:
     *   Llama al endpoint GET /incidencias para obtener todas las
     *   incidencias asociadas a un usuario.
     *
     * Entradas:
     *   - idUsuario : ID del usuario
     *   - queue     : Cola Volley
     *   - callback  : Callback con el resultado
     *
     * Salidas:
     *   - No retorna nada; notifica por callback
     *
     * Autora: Nerea Aguilar Forés
     * Fecha: 17/12/2025
     */
    public static void listarIncidenciasUsuario(
            int idUsuario,
            RequestQueue queue,
            ListarIncidenciasCallback callback
    ) {

        String url = "https://nagufor.upv.edu.es/incidencias?id_usuario=" + idUsuario;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if ("ok".equals(response.optString("status"))) {
                            JSONArray incidencias = response.getJSONArray("incidencias");
                            callback.onIncidenciasOk(incidencias);
                        } else {
                            callback.onErrorServidor();
                        }
                    } catch (Exception e) {
                        callback.onErrorInesperado();
                    }
                },
                error -> callback.onErrorServidor()
        );

        queue.add(request);
    }

    /**
     * Nombre Interfaz: ListarIncidenciasCallback
     * Descripción:
     *   Define los posibles resultados de la petición que obtiene
     *   todas las incidencias de un usuario.
     *
     * Autora: Nerea Aguilar Forés
     * Fecha: 17/12/2025
     */
    public interface ListarIncidenciasCallback {
        void onIncidenciasOk(JSONArray incidencias);
        void onErrorServidor();
        void onErrorInesperado();
    }

    /**
     * @brief Marca una incidencia como leída por el usuario.
     *
     * Llama al endpoint POST /incidencia/leida.
     *
     * @param idIncidencia ID de la incidencia
     * @param queue Cola Volley
     *
     * @author Nerea Aguilar Forés
     */
    public static void marcarIncidenciaLeida(
            int idIncidencia,
            RequestQueue queue
    ) {
        try {
            JSONObject body = new JSONObject();
            body.put("id_incidencia", idIncidencia);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    "https://nagufor.upv.edu.es/incidencia/leida",
                    body,
                    response -> {
                        // No hacemos nada en UI
                    },
                    error -> {
                        // No bloqueamos la UI si falla
                    }
            );

            queue.add(request);

        } catch (Exception e) {
            // Silencioso: no afecta a la experiencia
        }
    }
}