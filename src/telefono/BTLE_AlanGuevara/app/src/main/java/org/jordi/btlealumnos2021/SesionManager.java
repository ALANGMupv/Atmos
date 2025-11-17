package org.jordi.btlealumnos2021;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

/**
 * Nombre Fichero: SesionManager.java
 * Descripción: Clase encargada de gestionar la sesión local del usuario
 *              mediante SharedPreferences (guardar y recuperar datos básicos).
 * Autores: Nerea Aguilar Forés (ampliado en Perfil por Alan)
 */

public class SesionManager {

    // Nombre del fichero de preferencias donde se guarda la sesión
    private static final String NOMBRE_PREFERENCIAS = "SESION";

    // Claves usadas dentro de SharedPreferences
    private static final String CLAVE_ID_USUARIO = "id_usuario";
    private static final String CLAVE_NOMBRE     = "nombre";
    private static final String CLAVE_APELLIDOS  = "apellidos";
    private static final String CLAVE_EMAIL      = "email";
    private static final String CLAVE_ESTADO     = "estado";

    /**
     * Nombre Método: guardarSesion
     * Descripción: Guarda en SharedPreferences los datos básicos del usuario.
     * Entradas:
     *  - context: Contexto para acceder a SharedPreferences.
     *  - userJson: Objeto JSON con los datos del usuario devueltos por el servidor.
     * Salidas:
     *  - No retorna nada. Persiste los datos en el almacenamiento local.
     */
    public static void guardarSesion(Context context, JSONObject userJson) {
        try {
            SharedPreferences prefs = context.getSharedPreferences("SESION", Context.MODE_PRIVATE);
            SharedPreferences.Editor e = prefs.edit();

            e.putInt("id_usuario", userJson.getInt("id_usuario"));
            e.putString("nombre", userJson.getString("nombre"));
            e.putString("apellidos", userJson.getString("apellidos"));
            e.putString("email", userJson.getString("email"));
            e.putInt("estado", userJson.optInt("estado", 0));

            e.apply();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // =========================================================
    // MÉTODOS DE LECTURA DE LA SESIÓN
    // Autor: Alan Guevara Martínez
    // Fecha: 17/11/2025
    // =========================================================

    /**
     * Nombre Método: obtenerIdUsuario
     * Descripción: Devuelve el id_usuario guardado en la sesión.
     * Entradas:
     *  - context: Contexto para acceder a SharedPreferences.
     * Salidas:
     *  - int con el id_usuario, o -1 si no hay sesión guardada.
     */
    public static int obtenerIdUsuario(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(NOMBRE_PREFERENCIAS, Context.MODE_PRIVATE);
        return prefs.getInt(CLAVE_ID_USUARIO, -1);
    }

    /**
     * Nombre Método: obtenerNombre
     * Descripción: Devuelve el nombre del usuario guardado en la sesión.
     * Entradas:
     *  - context: Contexto para acceder a SharedPreferences.
     * Salidas:
     *  - String con el nombre, o cadena vacía si no hay dato.
     */
    public static String obtenerNombre(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(NOMBRE_PREFERENCIAS, Context.MODE_PRIVATE);
        return prefs.getString(CLAVE_NOMBRE, "");
    }

    /**
     * Nombre Método: obtenerApellidos
     * Descripción: Devuelve los apellidos del usuario guardados en la sesión.
     * Entradas:
     *  - context: Contexto para acceder a SharedPreferences.
     * Salidas:
     *  - String con los apellidos, o cadena vacía si no hay dato.
     */
    public static String obtenerApellidos(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(NOMBRE_PREFERENCIAS, Context.MODE_PRIVATE);
        return prefs.getString(CLAVE_APELLIDOS, "");
    }

    /**
     * Nombre Método: obtenerEmail
     * Descripción: Devuelve el email del usuario guardado en la sesión.
     * Entradas:
     *  - context: Contexto para acceder a SharedPreferences.
     * Salidas:
     *  - String con el email, o cadena vacía si no hay dato.
     */
    public static String obtenerEmail(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(NOMBRE_PREFERENCIAS, Context.MODE_PRIVATE);
        return prefs.getString(CLAVE_EMAIL, "");
    }

    /**
     * Nombre Método: haySesionActiva
     * Thecripción: Indica si parece haber una sesión guardada (id_usuario válido).
     * Entradas:
     *  - context: Contexto para acceder a SharedPreferences.
     * Salidas:
     *  - true si id_usuario > 0, false en caso contrario.
     */
    public static boolean haySesionActiva(Context context) {
        return obtenerIdUsuario(context) > 0;
    }

    /**
     * Nombre Método: cerrarSesion
     * Descripción: Elimina todos los datos de la sesión guardados en SharedPreferences.
     * Entradas:
     *  - context: Contexto para acceder a SharedPreferences.
     * Salidas:
     *  - No retorna nada. Deja el fichero de sesión vacío.
     */
    public static void cerrarSesion(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(NOMBRE_PREFERENCIAS, Context.MODE_PRIVATE);
        SharedPreferences.Editor e = prefs.edit();
        e.clear();   // Elimina todas las claves y valores
        e.apply();   // Aplica los cambios de forma asíncrona
    }
    // =========================================================
    // =========================================================
}
