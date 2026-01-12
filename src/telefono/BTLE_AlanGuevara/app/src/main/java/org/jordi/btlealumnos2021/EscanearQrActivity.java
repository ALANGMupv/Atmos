package org.jordi.btlealumnos2021;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

/**
 * @brief Pantalla para el escaneo de códigos QR.
 *
 * Activa la cámara del dispositivo y realiza la lectura de códigos QR
 * en tiempo real utilizando la librería ZXing. El resultado del escaneo
 * se devuelve a la Activity anterior mediante setResult().
 *
 * El flujo incluye la gestión del permiso de cámara y el control del
 * ciclo de vida del escáner.
 *
 * @author Alan Guevara Martínez
 * @date 18/11/2025
 */
public class EscanearQrActivity extends AppCompatActivity {

    // Vista especializada de ZXing que muestra la cámara y detecta códigos
    private DecoratedBarcodeView scannerView;

    // Botón para volver atrás sin leer ningún QR
    private ImageView btnBackQr;

    // Código interno para identificar la petición del permiso de cámara
    private static final int CAMERA_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.escaner_qr);  // Cargar layout del escáner

        // Obtener referencias de las vistas del layout
        scannerView = findViewById(R.id.barcodeScanner);
        btnBackQr   = findViewById(R.id.btnBackQr);

        /*
         * Acción del botón atrás:
         *  - Cierra la Activity
         *  - Devuelve RESULT_CANCELED porque no se ha leído ningún QR
         */
        btnBackQr.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        /*
         * Antes de iniciar la cámara, comprobamos si el permiso ya está concedido.
         * Si NO lo está → se solicita al usuario.
         * Si sí lo está → iniciamos directamente el escaneo.
         */
        if (checkSelfPermission(android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Solicita permiso de cámara
            requestPermissions(
                    new String[]{android.Manifest.permission.CAMERA},
                    CAMERA_REQUEST_CODE
            );

        } else {
            // Permiso ya concedido → iniciar escáner
            iniciarEscaneo();
        }
    }

    /**
     * @brief Inicia el escaneo continuo de códigos QR.
     *
     * Configura el callback de ZXing para detectar códigos QR en tiempo real.
     * Al detectar uno válido, se pausa la cámara, se devuelve el resultado
     * a la Activity anterior y se cierra esta pantalla.
     */
    private void iniciarEscaneo() {

        /// Establece un callback que se ejecutará cada vez que ZXing detecte algo
        scannerView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {

                // Si no hay contenido, no hacemos nada
                if (result == null || result.getText() == null) return;

                // Pausar cámara para evitar múltiples lecturas consecutivas
                scannerView.pause();

                // Obtener el texto limpio del código QR
                String codigo = result.getText().trim();

                // Preparar Intent con el resultado
                Intent i = new Intent();
                i.putExtra("codigo_qr", codigo);

                // Devolver resultado correcto a la Activity que llamó
                setResult(RESULT_OK, i);

                // Cerrar la pantalla del escáner
                finish();
            }
        });

        // Encender cámara y comenzar escaneo
        scannerView.resume();
    }

    /**
     * @brief Reanuda la cámara al volver a primer plano.
     *
     * Si el permiso de cámara está concedido, se reactiva el escáner
     * para continuar con la lectura de códigos QR.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Solo reanudar si el permiso de cámara fue concedido
        if (scannerView != null &&
                checkSelfPermission(android.Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {

            scannerView.resume();
        }
    }

    /**
     * @brief Pausa la cámara al salir de la pantalla.
     *
     * Detiene el escáner para liberar recursos del sistema.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (scannerView != null) {
            scannerView.pause();
        }
    }

    /**
     * @brief Maneja el resultado de la solicitud de permiso de cámara.
     *
     * Si el usuario concede el permiso, se inicia el escaneo QR.
     * En caso contrario, la pantalla se cierra al no poder funcionar
     * sin acceso a la cámara.
     *
     * @param requestCode  Código de la petición.
     * @param permissions  Permisos solicitados.
     * @param grantResults Resultado de los permisos.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_REQUEST_CODE) {

            // Comprobar si el usuario concedió el permiso
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Permiso concedido → iniciar escaner ahora sí
                iniciarEscaneo();

            } else {

                // Sin permiso no tiene sentido seguir en esta pantalla → cerrar
                finish();
            }
        }
    }
}
