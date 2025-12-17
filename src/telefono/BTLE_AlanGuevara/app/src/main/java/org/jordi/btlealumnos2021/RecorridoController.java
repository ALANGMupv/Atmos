package org.jordi.btlealumnos2021;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * @file RecorridoController.java
 * @brief Controlador del panel de recorrido diario del usuario.
 *
 * Esta clase actúa como intermediario entre la interfaz de usuario
 * y la lógica de negocio relacionada con el recorrido diario.
 *
 * Centraliza las siguientes responsabilidades:
 *  - Gestión del ciclo de vida del servicio GPS
 *  - Recepción de actualizaciones de distancia en tiempo real
 *  - Comunicación con el backend para obtener recorridos históricos
 *  - Actualización dinámica de los componentes de la interfaz
 *
 * De este modo se desacopla la Activity de la lógica de control,
 * facilitando el mantenimiento y la reutilización del código.
 *
 * @author Alan Guevara Martínez
 * @date 17/12/2025
 * @version 1.0
 */
public class RecorridoController {

    /** Contexto de la aplicación necesario para acceder a servicios del sistema */
    private final Context context;

    /** Botón para iniciar el servicio de recorrido */
    private final Button btnIniciar;

    /** Botón para detener el servicio de recorrido */
    private final Button btnDetener;

    /** TextView que muestra la distancia recorrida hoy */
    private final TextView tvHoy;

    /** TextView que muestra la distancia recorrida ayer */
    private final TextView tvAyer;

    /** Receiver encargado de recibir actualizaciones en tiempo real del servicio GPS */
    private BroadcastReceiver recorridoReceiver;

    /**
     * @brief Constructor del controlador.
     *
     * Recibe las referencias a los componentes de la interfaz
     * desde {@code UserPageActivity} e inicializa la lógica
     * del panel de recorrido.
     *
     * @param context    Contexto de la aplicación.
     * @param btnIniciar Botón para iniciar el recorrido.
     * @param btnDetener Botón para detener el recorrido.
     * @param tvHoy      TextView de recorrido de hoy.
     * @param tvAyer     TextView de recorrido de ayer.
     */
    public RecorridoController(
            Context context,
            Button btnIniciar,
            Button btnDetener,
            TextView tvHoy,
            TextView tvAyer
    ) {
        this.context = context;
        this.btnIniciar = btnIniciar;
        this.btnDetener = btnDetener;
        this.tvHoy = tvHoy;
        this.tvAyer = tvAyer;

        inicializar();
        cargarRecorrido();
    }

    /**
     * @brief Inicializa el estado inicial de la interfaz y los listeners.
     *
     * Configura la visibilidad de los botones, asigna los manejadores
     * de eventos de usuario y registra el {@link BroadcastReceiver}
     * encargado de recibir las actualizaciones del recorrido en tiempo real.
     */
    private void inicializar() {

        // Estado inicial: solo se permite iniciar el recorrido
        btnDetener.setVisibility(View.GONE);

        btnIniciar.setOnClickListener(v -> iniciarRecorrido());
        btnDetener.setOnClickListener(v -> detenerRecorrido());

        // Receiver para recibir actualizaciones periódicas del servicio GPS
        recorridoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                double total = intent.getDoubleExtra("distancia_total", 0);

                // Actualización de la interfaz con la distancia acumulada
                tvHoy.setText("Recorrido de hoy: " + (int) total + " m");

                Log.d("RecorridoController",
                        "UI actualizada en tiempo real: " + total + " m");
            }
        };

        // Registro del receiver para escuchar los broadcasts del servicio
        context.registerReceiver(
                recorridoReceiver,
                new IntentFilter(ServicioRecorridoGPS.ACTION_RECorrido_UPDATE)
        );
    }

    /**
     * @brief Inicia el servicio de recorrido GPS.
     *
     * Actualiza el estado de la interfaz y lanza el servicio
     * {@link ServicioRecorridoGPS} como servicio en primer plano.
     */
    private void iniciarRecorrido() {

        btnIniciar.setVisibility(View.GONE);
        btnDetener.setVisibility(View.VISIBLE);

        Intent intent = new Intent(context, ServicioRecorridoGPS.class);
        context.startForegroundService(intent);
    }

    /**
     * @brief Detiene el servicio de recorrido GPS.
     *
     * Restablece el estado inicial de la interfaz, detiene el servicio
     * y solicita de nuevo los datos de recorrido al backend.
     */
    private void detenerRecorrido() {

        btnDetener.setVisibility(View.GONE);
        btnIniciar.setVisibility(View.VISIBLE);

        Intent intent = new Intent(context, ServicioRecorridoGPS.class);
        context.stopService(intent);

        // Recarga de los datos tras finalizar el recorrido
        cargarRecorrido();
    }

    /**
     * @brief Obtiene del backend el recorrido de hoy y de ayer.
     *
     * Recupera el identificador del usuario activo y realiza
     * una petición utilizando Volley para obtener los datos
     * históricos del recorrido, actualizando la interfaz
     * cuando se recibe la respuesta.
     */
    private void cargarRecorrido() {

        int idUsuario = SesionManager.obtenerIdUsuario(context);

        RequestQueue queue = Volley.newRequestQueue(context);

        LogicaFake.obtenerRecorrido(
                idUsuario,
                queue,
                new LogicaFake.CallbackRecorrido() {
                    @Override
                    public void onRespuesta(double hoy, double ayer) {

                        tvHoy.setText("Recorrido de hoy: " + (int) hoy + " m");
                        tvAyer.setText("Ayer: " + (int) ayer + " m");
                    }
                }
        );
    }

    /**
     * @brief Libera los recursos utilizados por el controlador.
     *
     * Desregistra el {@link BroadcastReceiver} para evitar
     * fugas de memoria cuando la Activity es destruida.
     * Debe llamarse desde {@code onDestroy()}.
     */
    public void liberar() {
        try {
            context.unregisterReceiver(recorridoReceiver);
            Log.d("RecorridoController", "Receiver liberado");
        } catch (Exception ignored) {
            // Evita fallo si el receiver ya estaba desregistrado
        }
    }
}
