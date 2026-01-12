package org.jordi.btlealumnos2021;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

/**
 * @brief Actividad de pantalla de carga (splash).
 *
 * Muestra el logo de ATMOS durante un tiempo fijo y decide
 * a qué pantalla redirigir al usuario según su estado:
 * onboarding, login o sesión existente.
 */
public class ActividadSplash extends AppCompatActivity {

    private static final int DURACION_SPLASH_MS = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_splash);

        Preferencias preferencias = new Preferencias(this);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            /// 1️ Si hay sesión → ir a la pantalla de login (que mostrará la huella)
            if (SesionManager.haySesionActiva(this)) {
                startActivity(new Intent(ActividadSplash.this, InicioSesionActivity.class));
                finish();
                return;
            }

            /// 2️ Si es la primera vez → mostrar onboarding
            if (preferencias.esPrimeraVez()) {
                startActivity(new Intent(ActividadSplash.this, ActividadInicio.class));
                finish();
                return;
            }

            /// 3️ Si NO es primera vez → mostrar LOGIN (NO registro)
            startActivity(new Intent(ActividadSplash.this, InicioSesionActivity.class));
            finish();

        }, DURACION_SPLASH_MS);
    }
}
