package org.jordi.btlealumnos2021;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @file RecorridoStopReceiver.java
 * @brief Receptor de difusión encargado de detener el servicio de recorrido GPS.
 *
 * Este BroadcastReceiver se activa al recibir una acción específica enviada
 * normalmente desde una notificación persistente. Su función principal es
 * detener el servicio {@link ServicioRecorridoGPS}, finalizando así la captura
 * o seguimiento de la ubicación del usuario.
 *
 * Se utiliza como mecanismo de control externo del servicio, permitiendo
 * al usuario finalizar el recorrido sin necesidad de abrir la aplicación.
 *
 * @author Alan Guevara Martínez
 * @date 17/12/2025
 */
public class RecorridoStopReceiver extends BroadcastReceiver {

    /**
     * Acción personalizada utilizada para identificar el intento
     * de detener el servicio de recorrido GPS.
     *
     */
    public static final String ACTION_STOP =
            "org.jordi.btlealumnos2021.STOP_RECORRIDO";

    /**
     * Etiqueta utilizada para los mensajes de depuración (Logcat).
     */
    private static final String TAG = "RecorridoStopReceiver";

    /**
     * Método invocado automáticamente cuando el receptor recibe un broadcast.
     *
     * @param context Contexto de la aplicación necesario para acceder
     *                a los servicios del sistema.
     * @param intent  Intent recibido que contiene la acción del broadcast.
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        // Registro del broadcast recibido para depuración
        Log.d(TAG, "Broadcast recibido: " + intent.getAction());

        // Verificación de que la acción corresponde a la orden de parada
        if (ACTION_STOP.equals(intent.getAction())) {

            Log.d(TAG, "Deteniendo servicio GPS");

            // Detención explícita del servicio de recorrido GPS
            context.stopService(
                    new Intent(context, ServicioRecorridoGPS.class)
            );
        }
    }
}
