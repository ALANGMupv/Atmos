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

    private static final int DURACION_SPLASH_MS = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_splash);

        Preferencias preferencias = new Preferencias(this);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            // 1️ Si hay sesión → ir a la pantalla de login (que mostrará la huella)
            if (SesionManager.haySesionActiva(this)) {
                startActivity(new Intent(ActividadSplash.this, InicioSesionActivity.class));
                finish();
                return;
            }

            // 2️ Si es la primera vez → mostrar onboarding
            if (preferencias.esPrimeraVez()) {
                startActivity(new Intent(ActividadSplash.this, ActividadInicio.class));
                finish();
                return;
            }

            // 3️ Si NO es primera vez → mostrar LOGIN (NO registro)
            startActivity(new Intent(ActividadSplash.this, InicioSesionActivity.class));
            finish();

        }, DURACION_SPLASH_MS);
    }
}
