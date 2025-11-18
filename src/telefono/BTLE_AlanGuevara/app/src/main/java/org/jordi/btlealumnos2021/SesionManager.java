package org.jordi.btlealumnos2021;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

/**
 * Nombre Fichero: SesionManager.java
 * Descripción: Clase encargada de gestionar la sesión local del usuario
 *              mediante SharedPreferences (guardar datos básicos).
 * Autores: Nerea Aguilar Forés
 */
public class SesionManager {

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

    public static String getNombre(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences("SESION", Context.MODE_PRIVATE);
        return prefs.getString("nombre", "");
    }

    /**
     * Nombre Método: getIdUsuario
     * Descripción: Devuelve el id del usuario guardado en la sesión local.
     *
     * Entradas:
     *   - ctx → contexto para acceder a SharedPreferences
     *
     * Salidas:
     *   - int → ID del usuario (o -1 si no existe)
     *
     * Autora: Nerea Aguilar Forés
     */
    public static int getIdUsuario(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences("SESION", Context.MODE_PRIVATE);
        return prefs.getInt("id_usuario", -1);
    }


}
