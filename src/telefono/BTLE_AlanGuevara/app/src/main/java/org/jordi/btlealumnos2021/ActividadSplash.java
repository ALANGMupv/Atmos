package org.jordi.btlealumnos2021;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Muestra el logo de ATMOS durante 2 segundos.
 * Luego decide si mostrar:
 *  - El onboarding (si es primera vez)
 *  - La pantalla de login (si ya entró antes)
 */
public class ActividadSplash extends AppCompatActivity {

    // Duración de la pantalla de carga en milisegundos
    private static final int DURACION_SPLASH_MS = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_splash);

        // Objeto que controla si el usuario ya vio el onboarding
        Preferencias preferencias = new Preferencias(this);

        // Ejecutar código después de cierto tiempo
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            if (preferencias.esPrimeraVez()) {
                // MOSTRAR ONBOARDING
                startActivity(new Intent(ActividadSplash.this, ActividadInicio.class));
            } else {
                // IR AL LOGIN DIRECTO
                startActivity(new Intent(ActividadSplash.this, InicioSesionActivity.class));
            }

            finish();

        }, DURACION_SPLASH_MS);
    }
}