package org.jordi.btlealumnos2021;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

/**
 *
 * Pantalla de privacidad de la aplicación.
 *
 * Muestra la información relacionada con la política de privacidad
 *
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