package org.jordi.btlealumnos2021;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

/**
 * Nombre Fichero: SesionManager.java
 * Descripción:
 *    Clase encargada de gestionar la sesión local del usuario mediante SharedPreferences.
 *    Esta versión guarda el JSON COMPLETO del usuario (más robusto) y además expone
 *    funciones para obtener campos individuales (nombre, email, apellidos…) para mantener
 *    compatibilidad con EditarPerfil y el resto de la app.
 *
 * Autores:
 *    - Original: Nerea Aguilar Forés y Alan Guevara Martínez
 *    - Modificaciones: Judit Espinoza Cervera
 *
 */

public class SesionManager {

    // ------------------------------------------------------------
    // Nombre del fichero de preferencias donde se guarda la sesión
    // ------------------------------------------------------------
    private static final String PREFS = "SESION";

    // Clave única donde guardaremos el JSON completo
    private static final String KEY_JSON = "usuario_json";


    // ============================================================
    // MÉTODO: guardarSesion
    // ============================================================
    /**
     * Nombre Método: guardarSesion
     * Descripción:
     *      Guarda EN UNA SOLA CLAVE el JSON completo con todos los datos del usuario.
     *      Esto evita errores si el backend cambia algún campo o añade nuevos.
     *
     * Entradas:
     *      - context : contexto Android
     *      - userJson : objeto JSON devuelto por el backend al hacer login
     *
     * Salidas:
     *      - No retorna nada. Persiste la sesión localmente.
     */
    public static void guardarSesion(Context context, JSONObject userJson) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            prefs.edit()
                    .putString(KEY_JSON, userJson.toString())  // Guardamos el JSON ENTERO
                    .apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // ============================================================
    // MÉTODO: obtenerSesion
    // ============================================================
    /**
     * Nombre Método: obtenerSesion
     * Descripción:
     *      Devuelve el JSON completo guardado en SharedPreferences.
     *
     * Entradas:
     *      - context : contexto Android
     *
     * Salidas:
     *      - JSONObject si existe sesión
     *      - null si no existe sesión
     */
    public static JSONObject obtenerSesion(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            String json = prefs.getString(KEY_JSON, null);
            if (json == null) return null;
            return new JSONObject(json);
        } catch (Exception e) {
            return null;
        }
    }


    // ============================================================
    // MÉTODOS DE ACCESO A CAMPOS INDIVIDUALES
    // Compatibles con EditarPerfil y otras pantallas
    // ============================================================

    /**
     * Nombre Método: obtenerIdUsuario
     * Descripción: Devuelve el ID del usuario guardado.
     */
    public static int obtenerIdUsuario(Context context) {
        JSONObject sesion = obtenerSesion(context);
        if (sesion == null) return -1;
        return sesion.optInt("id_usuario", -1);
    }

    /**
     * Nombre Método: obtenerNombre
     * Descripción: Devuelve el nombre guardado.
     */
    public static String obtenerNombre(Context context) {
        JSONObject sesion = obtenerSesion(context);
        if (sesion == null) return "";
        return sesion.optString("nombre", "");
    }

    /**
     * Nombre Método: obtenerApellidos
     * Descripción: Devuelve los apellidos guardados.
     */
    public static String obtenerApellidos(Context context) {
        JSONObject sesion = obtenerSesion(context);
        if (sesion == null) return "";
        return sesion.optString("apellidos", "");
    }

    /**
     * Nombre Método: obtenerEmail
     * Descripción: Devuelve el email guardado.
     */
    public static String obtenerEmail(Context context) {
        JSONObject sesion = obtenerSesion(context);
        if (sesion == null) return "";
        return sesion.optString("email", "");
    }

    /**
     * Nombre Método: obtenerEstado
     * Descripción: Devuelve el estado guardado (si existe).
     */
    public static int obtenerEstado(Context context) {
        JSONObject sesion = obtenerSesion(context);
        if (sesion == null) return 0;
        return sesion.optInt("estado", 0);
    }


    // ============================================================
    // MÉTODO: haySesionActiva
    // ============================================================
    /**
     * Nombre Método: haySesionActiva
     * Descripción:
     *      Comprueba si existe una sesión guardada (JSON no nulo).
     *
     * Entradas:
     *      - context : contexto Android
     *
     * Salidas:
     *      - true si existe sesión
     *      - false si no existe
     */
    public static boolean haySesionActiva(Context context) {
        return obtenerSesion(context) != null;
    }


    // ============================================================
    // MÉTODO: cerrarSesion
    // ============================================================
    /**
     * Nombre Método: cerrarSesion
     * Descripción:
     *      Elimina la sesión completa del dispositivo (logout).
     *
     * Entradas:
     *      - context : contexto Android
     *
     * Salidas:
     *      - No retorna nada.
     */
    public static void cerrarSesion(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
