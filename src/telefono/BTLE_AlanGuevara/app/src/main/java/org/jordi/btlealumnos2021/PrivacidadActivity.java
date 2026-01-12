package org.jordi.btlealumnos2021;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * @brief Pantalla de política de privacidad de la aplicación.
 *
 * Esta Activity muestra la información relacionada con la
 * política de privacidad de ATMOS.
 *
 * El usuario puede cerrar la pantalla mediante el botón
 * de cierre (X), volviendo a la pantalla anterior.
 *
 * @author Nerea Aguilar Forés
 * @date 2025
 */
public class PrivacidadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacidad);

        // Botón cerrar (X)
        ImageView btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }
}