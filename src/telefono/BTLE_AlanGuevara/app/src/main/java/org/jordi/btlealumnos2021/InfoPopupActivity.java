package org.jordi.btlealumnos2021;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Nombre Fichero: InfoPopupActivity.java
 * Descripción: Maneja el popup con la información de la página del sensor.
 * Autora: Nerea Aguilar Forés
 * Fecha: 2025
 */
public class InfoPopupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Carga el diseño
        setContentView(R.layout.popup_info);

        // Cerrar popup
        ImageView btnCerrar = findViewById(R.id.btnCerrarPopup);
        btnCerrar.setOnClickListener(v -> finish());
    }
}
