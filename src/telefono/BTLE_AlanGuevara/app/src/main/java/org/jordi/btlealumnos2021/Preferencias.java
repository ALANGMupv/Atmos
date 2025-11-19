package org.jordi.btlealumnos2021;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Clase que gestiona datos persistentes simples.
 * Aquí se guarda si el usuario ya vio el onboarding.
 */
public class Preferencias {

    private static final String NOMBRE_PREFS = "preferencias_atmos";
    private static final String CLAVE_PRIMERA_VEZ = "es_primera_vez";

    private final SharedPreferences prefs;

    public Preferencias(Context context) {
        prefs = context.getSharedPreferences(NOMBRE_PREFS, Context.MODE_PRIVATE);
    }

    // Devuelve true si nunca se abrió la app (primera vez).
    public boolean esPrimeraVez() {
        return prefs.getBoolean(CLAVE_PRIMERA_VEZ, true);
    }

    // Marca que el usuario YA vio el onboarding.
    public void marcarNoPrimeraVez() {
        prefs.edit().putBoolean(CLAVE_PRIMERA_VEZ, false).apply();
    }
}
