package org.jordi.btlealumnos2021;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import java.util.Arrays;
import java.util.List;

public class MapasActivity extends FuncionesBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapas);

        //Poner título de página en el header
        setupHeader("Mapas");

        //Para funcionamiento del menu inferior
        setupBottomNav(0); // 0 = Mapas

        configurarChips();

    }

    // ==========================================================
    // MÉTODO: activar y desactivar chips de contaminante
    // ==========================================================
    private void configurarChips() {

        // Lista de todos los chips
        List<LinearLayout> chips = Arrays.asList(
                findViewById(R.id.chipCO),
                findViewById(R.id.chipO3),
                findViewById(R.id.chipSO3),
                findViewById(R.id.chipNO2)
        );

        // Listener común para todos
        View.OnClickListener listener = view -> {

            // 1) DESACTIVAR TODOS LOS CHIPS
            for (LinearLayout chip : chips) {

                chip.setBackgroundResource(R.drawable.bg_chip_unselected);

                // Dentro del chip: child 1 = TextView
                TextView texto = (TextView) chip.getChildAt(1);
                texto.setTextColor(Color.parseColor("#1F2B38"));
                texto.setTypeface(ResourcesCompat.getFont(this, R.font.roboto_light));
            }

            // 2) ACTIVAR EL CHIP SELECCIONADO
            LinearLayout chipSeleccionado = (LinearLayout) view;
            chipSeleccionado.setBackgroundResource(R.drawable.bg_chip_selected);

            TextView textoSel = (TextView) chipSeleccionado.getChildAt(1);
            textoSel.setTextColor(Color.parseColor("#059669"));
            textoSel.setTypeface(ResourcesCompat.getFont(this, R.font.roboto_bold));

            // 3) Obtener contaminante seleccionado
            String contaminante = textoSel.getText().toString();

            // 4) Llamar a tu función (actualizar grafica, datos, etc.)
            actualizarPantallaSegunContaminante(contaminante);
        };

        // 5) Asignar listener a todos los chips
        for (LinearLayout chip : chips) {
            chip.setOnClickListener(listener);
        }

        // 6) Seleccionar CO por defecto
        findViewById(R.id.chipCO).performClick();
    }

    private void actualizarPantallaSegunContaminante(String contaminante) {
        // Aquí metes tu lógica real: actualizar gráfica, enviar request, etc.
        Toast.makeText(this, "Mostrando datos de: " + contaminante, Toast.LENGTH_SHORT).show();
    }



}
