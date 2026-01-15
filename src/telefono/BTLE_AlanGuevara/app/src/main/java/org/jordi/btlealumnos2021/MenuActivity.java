package org.jordi.btlealumnos2021;

import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;
import android.widget.FrameLayout;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Nombre Clase: MenuActivity
 * Descripción:
 *   Pantalla "Menú de Acciones" donde el usuario puede:
 *     - Consultar el manual de usuario.   (de momento: Toast "no activo")
 *     - Reportar incidencias.             (de momento: Toast "no activo")
 *     - Desvincular el sensor.           (abre popup de confirmación)
 *
 *   La opción "Desvincular sensor" solo se muestra si el usuario tiene
 *   una placa asociada en el backend. Esta comprobación se realiza mediante
 *   LogicaFake.resumenUsuario().
 *
 * Autor: Alan Guevara Martínez
 * Fecha modificación: 19/11/2025
 */
public class MenuActivity extends FuncionesBaseActivity {

    // Tarjetas del menú
    private LinearLayout cardManualUsuario;
    private LinearLayout cardIncidencias;
    private LinearLayout cardDesvincularSensor;

    // Cola Volley para peticiones HTTP
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_acciones);

        // Inicializamos la cola de Volley
        queue = Volley.newRequestQueue(this);

        // Configuramos el header y el bottom nav como en el resto de pantallas
        setupHeader("Menú");
        setupBottomNav(2); // 2 = Menú

        // Asociamos vistas
        inicializarVistas();

        // Configuramos listeners de las tarjetas
        configurarListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cada vez que volvamos a esta pantalla comprobamos si el usuario
        // tiene placa o no, para mostrar u ocultar la tarjeta de desvincular.
        comprobarEstadoPlacaYActualizarUI();
    }

    /**
     * Nombre Método: inicializarVistas
     * Descripción:
     *   Enlaza las vistas del XML con las variables de la Activity.
     */
    private void inicializarVistas() {
        cardManualUsuario    = findViewById(R.id.cardManualUsuario);
        cardIncidencias      = findViewById(R.id.cardIncidencias);
        cardDesvincularSensor = findViewById(R.id.cardDesvincularSensor);
    }

    /**
     * Nombre Método: configurarListeners
     * Descripción:
     *   Asigna el comportamiento a cada tarjeta del menú:
     *     - Manual de usuario → Toast informativo.
     *     - Incidencias       → Toast informativo.
     *     - Desvincular sensor → muestra popup de confirmación.
     */
    private void configurarListeners() {

        // Tarjeta "Manual de Usuario"
        if (cardManualUsuario != null) {
            cardManualUsuario.setOnClickListener(v ->
                    mostrarDialogoConfirmarDescarga()

            );
        }

        // Tarjeta "Incidencias"
        if (cardIncidencias != null) {
            cardIncidencias.setOnClickListener(v -> {
                Intent intent = new Intent(MenuActivity.this, IncidenciasActivity.class);
                startActivity(intent);
            });
        }


        // Tarjeta "Desvincular sensor"
        if (cardDesvincularSensor != null) {
            cardDesvincularSensor.setOnClickListener(v -> mostrarPopupDesvincular());
        }
    }

    /**
     * Nombre Método: comprobarEstadoPlacaYActualizarUI
     * Descripción:
     *   Llama al backend para saber si el usuario tiene una placa asociada.
     *   En función del resultado:
     *     - onSinPlaca()  → Oculta la tarjeta de "Desvincular sensor".
     *     - onConPlaca()  → Muestra la tarjeta.
     *
     *   Utiliza el método resumenUsuario() de LogicaFake.
     */
    private void comprobarEstadoPlacaYActualizarUI() {

        // Obtenemos id_usuario desde el gestor de sesión
        int idUsuario = SesionManager.obtenerIdUsuario(this);

        // Si no hay id de usuario guardado, ocultamos directamente la tarjeta
        if (idUsuario <= 0) {
            if (cardDesvincularSensor != null) {
                cardDesvincularSensor.setVisibility(View.GONE);
            }
            return;
        }

        // Llamamos a la lógica fake para saber si tiene o no placa asociada
        LogicaFake.resumenUsuarioPorGas(
                idUsuario,
                11,        // <- tipo de gas por defecto y el único que hay
                queue,
                new LogicaFake.ResumenUsuarioCallback() {
                    @Override
                    public void onSinPlaca() {
                        // Si no hay placa, ocultamos la opción de desvincular
                        if (cardDesvincularSensor != null) {
                            cardDesvincularSensor.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onConPlaca(String placa, double ultima, String fecha, double promedio) {
                        if (cardDesvincularSensor != null) {
                            cardDesvincularSensor.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onErrorServidor() {
                        // Ante error de servidor, mejor ser conservador y mostrar la tarjeta,
                        // por si el usuario realmente sí tiene placa y quiere desvincularla.
                        if (cardDesvincularSensor != null) {
                            cardDesvincularSensor.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onErrorInesperado() {
                        if (cardDesvincularSensor != null) {
                            cardDesvincularSensor.setVisibility(View.VISIBLE);
                        }
                    }
                }
        );
    }

    /**
     * Nombre Método: mostrarPopupDesvincular
     * Descripción:
     *   Muestra el popup de confirmación para desvincular el sensor
     *   usando el layout popup_desvincular_sensor.xml
     *
     *   - Botón "Cancelar": cierra el popup sin hacer nada más.
     *   - Botón "Desvincular": llama a LogicaFake.desvincularPlacaServidor.
     */
    private void mostrarPopupDesvincular() {

        // Inflamos la vista del popup desde el XML
        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.popup_desvincular_sensor, null);

        // Creamos un PopupWindow a pantalla completa (ancho y alto = MATCH_PARENT)
        PopupWindow popup = new PopupWindow(
                popupView,
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                true   // focusable = true → permite recibir eventos
        );

        // Fondo transparente para poder ver el overlay gris
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // Permite cerrar el popup pulsando fuera de la tarjeta blanca
        popup.setOutsideTouchable(true);

        // Mostramos el popup centrado en la pantalla
        popup.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        // Referencias a los botones del popup
        View btnDesvincular = popupView.findViewById(R.id.btnDesvincularConfirmar);
        View btnCancelar    = popupView.findViewById(R.id.btnDesvincularCancelar);

        // Comportamiento del botón "Cancelar"
        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v -> popup.dismiss());
        }

        // Comportamiento del botón "Desvincular"
        if (btnDesvincular != null) {
            btnDesvincular.setOnClickListener(v -> {
                // Desactivamos el botón para evitar múltiples pulsaciones
                btnDesvincular.setEnabled(false);

                // Obtenemos el id_usuario desde el gestor de sesión
                int idUsuario = SesionManager.obtenerIdUsuario(MenuActivity.this);
                if (idUsuario <= 0) {
                    Toast.makeText(
                            MenuActivity.this,
                            "No se ha encontrado el usuario en la sesión local",
                            Toast.LENGTH_LONG
                    ).show();
                    popup.dismiss();
                    return;
                }

                // Llamamos al método de LogicaFake para desvincular
                LogicaFake.desvincularPlacaServidor(
                        idUsuario,
                        queue,
                        new LogicaFake.DesvincularPlacaCallback() {
                            @Override
                            public void onDesvinculacionOk() {
                                // Volvemos a habilitar el botón (por si reaparece el popup)
                                btnDesvincular.setEnabled(true);

                                // Mostramos mensaje de éxito
                                Toast.makeText(
                                        MenuActivity.this,
                                        "Sensor desvinculado correctamente",
                                        Toast.LENGTH_SHORT
                                ).show();

                                // Cerramos el popup
                                popup.dismiss();

                                // Actualizamos la UI para ocultar la tarjeta
                                comprobarEstadoPlacaYActualizarUI();

                                // Volvemos a la pantalla anterior (el layout anterior / menú)
                                // En la mayoría de flujos esto cerrará MenuActivity
                                // y regresará a la Activity que lo abrió.
                                new Handler(Looper.getMainLooper()).postDelayed(
                                        () -> {
                                            setResult(RESULT_OK);   // <-- avisa a UserPageActivity
                                            finish();               // <-- cierra
                                        },
                                        300
                                );

                            }

                            @Override
                            public void onUsuarioSinPlaca() {
                                btnDesvincular.setEnabled(true);

                                // El backend indica que ya no había placa asociada.
                                Toast.makeText(
                                        MenuActivity.this,
                                        "No se encontró ningún sensor vinculado",
                                        Toast.LENGTH_SHORT
                                ).show();

                                popup.dismiss();
                                comprobarEstadoPlacaYActualizarUI();
                            }

                            @Override
                            public void onErrorServidor() {
                                btnDesvincular.setEnabled(true);

                                Toast.makeText(
                                        MenuActivity.this,
                                        "Error al contactar con el servidor",
                                        Toast.LENGTH_LONG
                                ).show();
                            }

                            @Override
                            public void onErrorInesperado() {
                                btnDesvincular.setEnabled(true);

                                Toast.makeText(
                                        MenuActivity.this,
                                        "Error inesperado al desvincular el sensor",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
            });
        }
    }

    //----------------------------------------------------------------------------------------------
    // Descargar manual de usuario
    //----------------------------------------------------------------------------------------------

    private void descargarManual(){
        String urlPdf = "https://nagufor.upv.edu.es/manual/Manual_de_usuario_Atmos.pdf";

        DownloadManager downloadManager =
                (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        Uri uri = Uri.parse(urlPdf);

        DownloadManager.Request request =
                new DownloadManager.Request(uri);

        request.setTitle("Manual de usuario");
        request.setDescription("Descargando manual de usuario");
        request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
        );

        request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "manual_usuario.pdf"
        );

        downloadManager.enqueue(request);

        Toast.makeText(
                this,
                "Descarga iniciada",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void mostrarDialogoConfirmarDescarga() {

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Descarga de manual")
                .setMessage("Se va a descargar el PDF del manual de usuario.\n¿Desea continuar?")
                .setPositiveButton("Descargar", (dialog, which) -> {
                    descargarManual();
                })
                .setNegativeButton("Cancelar", null)
                .setCancelable(true)
                .show();
    }

}
