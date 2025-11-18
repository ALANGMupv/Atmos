package org.jordi.btlealumnos2021;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Nombre Clase: VincularSensorActivity
 * Descripción:
 *      Pantalla que permite al usuario vincular un sensor a su cuenta
 *      utilizando dos métodos: escaneo de QR o introducción manual del código.
 *
 * Funcionalidad:
 *      - Botón atrás para volver a página donde el sensor no está vinculado.
 *      - Tarjeta para escanear código QR.
 *      - Tarjeta para introducir manualmente el código.
 *      - Botón final para vincular el sensor usando el texto introducido.
 *
 * Autor: Alan Guevara Martínez
 * Fecha: 17/11/2025
 */
public class VincularSensorActivity extends AppCompatActivity {

    private ImageView btnBack;
    private LinearLayout cardQr, cardCodigo;
    private EditText etCodigo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vincular_sensor);

        inicializarVistas();
        configurarListeners();
    }

    /**
     * Nombre Método: inicializarVistas
     * Descripción:
     *      Asocia las vistas del layout XML con las variables Java.
     */
    private void inicializarVistas() {

        btnBack = findViewById(R.id.btnBack);
        cardQr = findViewById(R.id.cardQr);
        cardCodigo = findViewById(R.id.cardCodigo);
        etCodigo = findViewById(R.id.etCodigo);
    }

    /**
     * Nombre Método: configurarListeners
     * Descripción:
     *      Configura los eventos de click:
     *          - Volver atrás.
     *          - Ir a la actividad de escaneo de QR.
     *          - Activar el botón de vinculación manual.
     */
    private void configurarListeners() {

        // Volver atrás
        btnBack.setOnClickListener(v -> finish());

        /*
        // Ir a la pantalla de escanear QR
        cardQr.setOnClickListener(v -> {
            Intent i = new Intent(this, EscanearQrActivity.class);
            startActivity(i);
        });*/

        // Ir a otra pantalla o procesar directamente el código ingresado
        cardCodigo.setOnClickListener(v -> etCodigo.requestFocus());

        // Acción del botón Vincular Sensor
        findViewById(R.id.btnVincular).setOnClickListener(v -> {

            String codigo = etCodigo.getText().toString().trim();

            if (codigo.isEmpty()) {
                etCodigo.setError("Introduce un código válido");
                return;
            }

            // Aquí iría lógica real:
            // LogicaFake.vincularSensor(...);

        });
    }
}
