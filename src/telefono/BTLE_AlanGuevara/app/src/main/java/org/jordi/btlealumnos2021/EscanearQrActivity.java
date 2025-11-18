package org.jordi.btlealumnos2021;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

/**
 * Nombre Clase: EscanearQrActivity
 * Descripción:
 *      Pantalla que muestra la cámara y espera a leer un código QR.
 *      Cuando el QR se detecta:
 *          - Se devuelve el texto leído a VincularSensorActivity
 *            mediante setResult().
 *          - Allí se reutiliza el método vincularPorCodigo(codigo).
 *
 * Funcionamiento:
 *      - Se usa la librería ZXing para el escaneo en tiempo real.
 *      - Cuando detecta un QR válido, se cierra la Activity.
 *
 * Autor: Alan Guevara Martínez
 * Fecha: 18/11/2025
 */
public class EscanearQrActivity extends AppCompatActivity {

    // Vista que contiene la cámara y el lector de códigos
    private DecoratedBarcodeView scannerView;

    // Botón para volver atrás sin escanear
    private ImageView btnBackQr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.escaner_qr);  // Layout con la cámara y el botón back

        // Referencias a vistas
        scannerView = findViewById(R.id.barcodeScanner);
        btnBackQr   = findViewById(R.id.btnBackQr);

        // Botón atrás: simplemente cierra la Activity sin devolver nada
        if (btnBackQr != null) {
            btnBackQr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
        }

        // Configura el escaneo continuo para detectar QR en tiempo real
        scannerView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {

                // Si no hay texto, no procesamos nada
                if (result == null || result.getText() == null) return;

                // Pausa la cámara para evitar múltiples lecturas seguidas
                scannerView.pause();

                // Obtiene el texto del QR y elimina espacios alrededor
                String codigo = result.getText().trim();

                // Devuelve el código leído a la Activity anterior
                Intent i = new Intent();
                i.putExtra("codigo_qr", codigo);  // clave "codigo_qr"
                setResult(RESULT_OK, i);

                // Cierra esta Activity y vuelve a VincularSensorActivity
                finish();
            }
        });

        // Comienza a escanear
        scannerView.resume();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reanuda la cámara al volver a la pantalla
        if (scannerView != null) {
            scannerView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pausa la cámara cuando la Activity no está visible
        if (scannerView != null) {
            scannerView.pause();
        }
    }
}
