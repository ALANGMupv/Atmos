package org.jordi.btlealumnos2021;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Nombre Clase: VincularSensorActivity
 * Descripción:
 *      Pantalla que permite al usuario vincular un sensor a su cuenta
 *      utilizando dos métodos: escaneo de QR o introducción manual del código.
 *
 * Funcionalidad:
 *      - Botón atrás para volver a página anterior.
 *      - Tarjeta para escanear código QR.
 *      - Tarjeta para introducir manualmente el código.
 *      - Botón final para vincular el sensor usando el texto introducido.
 *      - Muestra popups de éxito o error según la respuesta del servidor.
 *
 * Comportamiento de popups:
 *      - Si la vinculación es correcta → se muestra popup_sensor_vinculado
 *        durante 3 segundos y luego se redirige a UserPageActivity.
 *      - Si falla la vinculación → se muestra popup_sensor_no_vinculado
 *        durante 2 segundos y se mantiene en la misma pantalla.
 *      - Si el usuario toca fuera del popup, este se cierra.
 *
 * Autor: Alan Guevara Martínez
 * Fecha: 18/11/2025
 */
public class VincularSensorActivity extends AppCompatActivity {

    // Vistas principales
    private ImageView btnBack;
    private LinearLayout cardQr, cardCodigo;
    private EditText etCodigo;

    // Cola de peticiones HTTP (Volley)
    private RequestQueue queue;

    // Código de solicitud usado para identificar el resultado del escaneo QR
    private static final int REQ_QR = 777;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vincular_sensor);

        // Inicializar cola de Volley
        queue = Volley.newRequestQueue(this);

        inicializarVistas();
        configurarListeners();
    }

    /**
     * Nombre Método: inicializarVistas
     * Descripción:
     * Asocia las vistas del layout XML con las variables Java.
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
     * Configura los eventos de click:
     * - Volver atrás.
     * - Ir a la actividad de escaneo de QR.
     * - Dar foco al campo de código al pulsar la tarjeta.
     * - Lógica completa de vinculación al pulsar el botón.
     */
    private void configurarListeners() {

        // Volver atrás
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Ir a la pantalla de escanear QR.
        if (cardQr != null) {
            cardQr.setOnClickListener(v -> {
                Intent i = new Intent(this, EscanearQrActivity.class);
                startActivity(i);
            });
        }

        // Al tocar la tarjeta de código manual, damos foco al EditText
        if (cardCodigo != null && etCodigo != null) {
            cardCodigo.setOnClickListener(v -> etCodigo.requestFocus());
        }

        // Acción del botón "Vincular Sensor"
        View btnVincular = findViewById(R.id.btnVincular);
        if (btnVincular != null) {
            btnVincular.setOnClickListener(v -> {
                String codigo = etCodigo.getText().toString().trim();

                if (codigo.isEmpty()) {
                    etCodigo.setError("Introduce un código válido");
                    return;
                }

                // Llamamos a un método auxiliar para seguir el flujo de vinculación por código
                vincularPorCodigo(codigo);
            });
        }
    }

    /**
     * Nombre Método: vincularPorCodigo
     * Descripción:
     * Ejecuta el flujo de vinculación de sensor usando el código
     * introducido manualmente en el EditText.
     * <p>
     * 1. Obtiene id_usuario desde SesionManager.
     * 2. Llama a LogicaFake.vincularPlacaServidor.
     * 3. Muestra popup de éxito o error según la respuesta.
     * <p>
     * Entradas:
     * - codigo: String con el código/UUID de la placa.
     */
    private void vincularPorCodigo(String codigo) {
        // 1. Obtener id_usuario desde la sesión local
        int idUsuario = SesionManager.obtenerIdUsuario(this);
        if (idUsuario <= 0) {
            Toast.makeText(this, "No se ha encontrado el usuario en la sesión local", Toast.LENGTH_LONG).show();
            return;
        }

        // 2. Desactivar el botón para evitar múltiples clics
        View btnVincular = findViewById(R.id.btnVincular);
        if (btnVincular != null) {
            btnVincular.setEnabled(false);
        }

        // 3. Llamar a la lógica fake para realizar la petición al servidor
        LogicaFake.vincularPlacaServidor(
                idUsuario,
                codigo,
                queue,
                new LogicaFake.VincularPlacaCallback() {
                    @Override
                    public void onVinculacionOk() {
                        // Rehabilitar botón
                        if (btnVincular != null) btnVincular.setEnabled(true);

                        // Mostrar popup de sensor vinculado y redirigir después
                        mostrarPopupSensorVinculado(codigo);
                    }

                    @Override
                    public void onCodigoNoValido() {
                        if (btnVincular != null) btnVincular.setEnabled(true);

                        // Código incorrecto / placa no encontrada / ya asignada
                        mostrarPopupSensorNoVinculado();
                    }

                    @Override
                    public void onErrorServidor() {
                        if (btnVincular != null) btnVincular.setEnabled(true);

                        Toast.makeText(VincularSensorActivity.this,
                                "Error al contactar con el servidor",
                                Toast.LENGTH_LONG).show();

                        mostrarPopupSensorNoVinculado();
                    }

                    @Override
                    public void onErrorInesperado() {
                        if (btnVincular != null) btnVincular.setEnabled(true);

                        Toast.makeText(VincularSensorActivity.this,
                                "Error inesperado al vincular el sensor",
                                Toast.LENGTH_LONG).show();

                        mostrarPopupSensorNoVinculado();
                    }
                }
        );
    }

    /**
     * Nombre Método: mostrarPopupSensorVinculado
     * Descripción:
     * Muestra el popup de sensor vinculado correctamente
     * (popup_sensor_vinculado.xml), lo mantiene visible durante 3 segundos
     * y luego redirige a UserPageActivity.
     *
     * Si el usuario toca fuera del popup, este se cierra (o eso se espera).
     *
     * Entradas:
     *  - codigoQueEstesVinculando: String con el código que se ha vinculado correctamente,
     *    mostrado en el popup.
     */
    private void mostrarPopupSensorVinculado(String codigoQueEstesVinculando) {

        // Inflamos la vista del popup desde XML
        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.popup_sensor_vinculado, null);

        // Creamos un PopupWindow a pantalla completa
        PopupWindow popup = new PopupWindow(
                popupView,
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                true
        );

        // Permite cerrar tocando fuera del popup
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setOutsideTouchable(true);

        // Mostramos en el popup el código del sensor vinculado correctamente
        TextView txtCodigo = popupView.findViewById(R.id.txtCodigoSensor);
        txtCodigo.setText(codigoQueEstesVinculando); // Mostramos el código vinculado

        // Mostrar el popup centrado en pantalla
        popup.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        // Handler que cierra el popup a los 3 segundos y redirige
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            if (popup.isShowing()) {
                popup.dismiss();
            }

            // Redirigir a UserPageActivity
            Intent data = new Intent();
            data.putExtra("status", "vinculado");

            setResult(RESULT_OK, data);
            finish(); // ← ESTO hace que UserPageActivity llame a onResume()

        }, 3000); // 3 segundos de espera hasta userPage
    }


    /**
     * Nombre Método: mostrarPopupSensorNoVinculado
     * Descripción:
     * Muestra el popup de sensor NO vinculado
     * (popup_sensor_no_vinculado.xml), lo mantiene visible durante
     * 2 segundos y luego lo cierra, permaneciendo en la misma pantalla.
     * <p>
     * Si el usuario toca fuera del popup, este se cierra.
     */
    private void mostrarPopupSensorNoVinculado() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.popup_sensor_no_vinculado, null);

        PopupWindow popup = new PopupWindow(
                popupView,
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                true
        );

        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setOutsideTouchable(true);

        popup.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        // Cerrar automáticamente a los 2 segundos
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (popup.isShowing()) {
                popup.dismiss();
            }
        }, 2000); // 2 segundos
    }


    /**
     * Nombre Método: onActivityResult
     * Descripción:
     * Método que recibe el resultado de una Activity llamada desde esta.
     * En este caso, se utiliza para recibir el código QR obtenido desde
     * EscanearQrActivity.
     * <p>
     * Si el resultado proviene del lector QR (REQ_QR) y es correcto
     * (RESULT_OK), se extrae el código leído y se llama al método
     * vincularPorCodigo(codigo) para continuar con el proceso de vinculación.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Llama al método padre para mantener el comportamiento estándar al recibir resultados de otras Activities

        // Verifica que la respuesta provenga de la Activity que escanea el QR
        // (REQ_QR es el código con el que iniciamos EscanearQrActivity)
        if (requestCode == REQ_QR && resultCode == RESULT_OK) {
            // También valida que la Activity haya finalizado correctamente (RESULT_OK)

            // Comprueba que el Intent devuelto no sea nulo y que contenga el extra "codigo_qr"
            if (data != null && data.hasExtra("codigo_qr")) {

                // Obtiene el texto del QR leído y enviado desde EscanearQrActivity
                String codigo = data.getStringExtra("codigo_qr");

                // Reutilizamos tu método
                // Llama al método interno que procesa la vinculación usando el código recibido
                vincularPorCodigo(codigo);
            }
        }
    }
}


