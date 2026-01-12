package org.jordi.btlealumnos2021;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * @brief Activity que muestra un popup informativo del sensor.
 *
 * Se encarga de mostrar una ventana emergente con información
 * adicional sobre el sensor y permite cerrarla mediante un botón.
 *
 * @author Nerea Aguilar Forés
 * @date 2025
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
