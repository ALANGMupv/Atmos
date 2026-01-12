package org.jordi.btlealumnos2021;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

/**
 * @brief Gestor de sesión local del usuario.
 *
 * Se encarga de almacenar y recuperar la información de sesión
 * del usuario mediante SharedPreferences.
 *
 * La sesión se guarda como un único objeto JSON completo,
 * lo que permite mayor robustez ante cambios en el backend
 * y facilita el acceso a campos individuales (id, nombre,
 * apellidos, email, estado, etc.).
 *
 * @author Nerea Aguilar Forés
 * @author Alan Guevara Martínez
 * @author Judit Espinoza Cervera
 */

public class SesionManager {

    // ------------------------------------------------------------
    // Nombre del fichero de preferencias donde se guarda la sesión
    // ------------------------------------------------------------
    private static final String PREFS = "SESION";

    // Clave única donde guardaremos el JSON completo
    private static final String KEY_JSON = "usuario_json";


    /// ============================================================
    /// MÉTODO: guardarSesion
    /// ============================================================
    /**
     * @brief Guarda la sesión del usuario en el dispositivo.
     *
     * Almacena el objeto JSON completo devuelto por el backend
     * en una única clave de SharedPreferences, evitando problemas
     * si se añaden o modifican campos en el futuro.
     *
     * @param context  Contexto de la aplicación
     * @param userJson Objeto JSON con los datos del usuario
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


    /// ============================================================
    /// MÉTODO: obtenerSesion
    /// ============================================================
    /**
     * @brief Obtiene la sesión completa del usuario.
     *
     * Recupera y reconstruye el objeto JSON almacenado
     * en SharedPreferences.
     *
     * @param context Contexto de la aplicación
     * @return JSONObject con los datos del usuario,
     *         o null si no existe sesión
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


    /// ============================================================
    /// MÉTODOS DE ACCESO A CAMPOS INDIVIDUALES
    /// Compatibles con EditarPerfil y otras pantallas
    /// ============================================================

    /**
     * @brief Devuelve el identificador del usuario.
     *
     * @param context Contexto de la aplicación
     * @return ID del usuario o -1 si no existe sesión
     */
    public static int obtenerIdUsuario(Context context) {
        JSONObject sesion = obtenerSesion(context);
        if (sesion == null) return -1;
        return sesion.optInt("id_usuario", -1);
    }

    /**
     * @brief Devuelve el nombre del usuario.
     *
     * @param context Contexto de la aplicación
     * @return Nombre del usuario o cadena vacía
     */
    public static String obtenerNombre(Context context) {
        JSONObject sesion = obtenerSesion(context);
        if (sesion == null) return "";
        return sesion.optString("nombre", "");
    }

    /**
     * @brief Devuelve los apellidos del usuario.
     *
     * @param context Contexto de la aplicación
     * @return Apellidos del usuario o cadena vacía
     */
    public static String obtenerApellidos(Context context) {
        JSONObject sesion = obtenerSesion(context);
        if (sesion == null) return "";
        return sesion.optString("apellidos", "");
    }

    /**
     * @brief Devuelve el correo electrónico del usuario.
     *
     * @param context Contexto de la aplicación
     * @return Email del usuario o cadena vacía
     */
    public static String obtenerEmail(Context context) {
        JSONObject sesion = obtenerSesion(context);
        if (sesion == null) return "";
        return sesion.optString("email", "");
    }

    /**
     * @brief Devuelve el estado del usuario.
     *
     * @param context Contexto de la aplicación
     * @return Estado del usuario o 0 si no existe
     */
    public static int obtenerEstado(Context context) {
        JSONObject sesion = obtenerSesion(context);
        if (sesion == null) return 0;
        return sesion.optInt("estado", 0);
    }


    /// ============================================================
    /// MÉTODO: haySesionActiva
    /// ============================================================
    /**
     * @brief Indica si existe una sesión activa.
     *
     * @param context Contexto de la aplicación
     * @return true si hay sesión guardada, false en caso contrario
     */
    public static boolean haySesionActiva(Context context) {
        return obtenerSesion(context) != null;
    }


    /// ============================================================
    /// MÉTODO: cerrarSesion
    /// ============================================================
    /**
     * @brief Cierra la sesión del usuario.
     *
     * Elimina completamente la información de sesión
     * almacenada en el dispositivo.
     *
     * @param context Contexto de la aplicación
     */
    public static void cerrarSesion(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
