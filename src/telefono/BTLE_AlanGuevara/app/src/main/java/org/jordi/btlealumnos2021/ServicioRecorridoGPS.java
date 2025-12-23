package org.jordi.btlealumnos2021;

import android.app.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.*;

/**
 * @file ServicioRecorridoGPS.java
 * @brief Servicio GPS en primer plano para el cálculo de la distancia diaria.
 *
 * Este servicio se ejecuta en primer plano para garantizar su continuidad
 * incluso cuando la aplicación se encuentra en segundo plano.
 *
 * Funcionalidades principales:
 *  - Obtención periódica de la ubicación del usuario mediante FusedLocationProvider
 *  - Cálculo incremental de la distancia recorrida
 *  - Filtrado de lecturas erróneas o poco precisas (GPS drift)
 *  - Envío progresivo de datos al backend
 *  - Comunicación en tiempo real con la interfaz mediante Broadcasts
 *
 * Está optimizado para el consumo de batería mediante:
 *  - Intervalos de actualización razonables
 *  - Filtro de precisión y velocidad
 *  - Uso controlado de la prioridad de localización
 *
 * @author Alan Guevara Martínez
 * @date 17/12/2025
 */
public class ServicioRecorridoGPS extends Service {

    /** Identificador del canal de notificación del servicio */
    private static final String CHANNEL_ID = "canal_recorrido";

    /** Identificador único de la notificación en primer plano */
    private static final int NOTIF_ID = 2001;

    /** Cliente de localización de Google Play Services */
    private FusedLocationProviderClient fusedClient;

    /** Callback que recibe las actualizaciones de localización */
    private LocationCallback locationCallback;

    /** Última localización válida registrada */
    private Location ultimaLocalizacion;

    /** Distancia total acumulada durante el día (en metros) */
    private double distanciaAcumulada = 0;

    /** Etiqueta para mensajes de depuración */
    private static final String TAG = "ServicioRecorridoGPS";

    /**
     * Acción de broadcast utilizada para notificar a la UI
     * actualizaciones de la distancia recorrida en tiempo real.
     */
    public static final String ACTION_RECorrido_UPDATE =
            "org.jordi.btlealumnos2021.RECORRIDO_UPDATE";

    /**
     * Indica si el servicio de recorrido está actualmente en ejecución.
     * Se usa para sincronizar el estado de la UI al volver a la Activity.
     */
    private static boolean servicioActivo = false;

    /**
     * Acción broadcast enviada cuando el servicio se detiene completamente.
     * Permite a la UI sincronizar su estado (botón iniciar/detener).
     */
    public static final String ACTION_SERVICIO_DETENIDO =
            "org.jordi.btlealumnos2021.RECORRIDO_STOPPED";


    /**
     * Permite consultar desde fuera si el servicio está activo.
     */
    public static boolean isRunning() {
        return servicioActivo;
    }

    /**
     * Método invocado al iniciar el servicio.
     *
     * Configura el servicio como primer plano, crea el canal
     * de notificación, inicializa la distancia acumulada
     * desde el backend e inicia la obtención de localizaciones.
     *
     * @param intent  Intent de inicio del servicio.
     * @param flags   Flags adicionales.
     * @param startId Identificador de inicio.
     * @return Modo de reinicio del servicio.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "Servicio iniciado");

        // Marcar servicio como activo
        servicioActivo = true;

        crearCanal();
        startForeground(NOTIF_ID, crearNotificacion());

        inicializarDistanciaAcumulada();
        iniciarLocalizacion();

        return START_STICKY;
    }

    /**
     * @brief Inicializa el sistema de localización GPS.
     *
     * Configura el cliente de localización con intervalos
     * controlados y prioridad adecuada, comprobando previamente
     * que los permisos necesarios han sido concedidos.
     */
    private void iniciarLocalizacion() {

        Log.d(TAG, "Inicializando localización");

        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        // Comprobación de permisos de localización
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Log.e(TAG, "Permisos GPS NO concedidos. Abortando localización.");
            stopSelf();
            return;
        }

        LocationRequest request = LocationRequest.create()
                .setInterval(2000)
                .setFastestInterval(1000)
                .setSmallestDisplacement(1.0f) // Antes 1 metros mínimo para que Google decida enviar algo
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY);


        // Callback que procesa las localizaciones recibidas
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {

                if (result == null) {
                    Log.e(TAG, "LocationResult es NULL");
                    return;
                }

                Log.d(TAG,
                        "Localizaciones recibidas: "
                                + result.getLocations().size());

                for (Location loc : result.getLocations()) {
                    procesarLocalizacion(loc);
                }
            }
        };

        fusedClient.requestLocationUpdates(
                request,
                locationCallback,
                getMainLooper()
        );

        Log.d(TAG, "requestLocationUpdates lanzado correctamente");
    }

    /**
     * @brief Procesa una nueva localización recibida.
     *
     * Calcula el incremento de distancia respecto a la última
     * localización válida, aplicando filtros para descartar
     * lecturas erróneas, saltos irreales o situaciones de
     * usuario en reposo.
     *
     * @param nueva Nueva localización recibida.
     */
    private void procesarLocalizacion(Location nueva) {

        Log.d(TAG, "Nueva localización: "
                + nueva.getLatitude() + ", "
                + nueva.getLongitude()
                + " | Accuracy: " + nueva.getAccuracy());

        // ------------------------------------------------------------------
        // Filtro de precisión GPS
        //    Si la precisión es peor de 15 metros, se descarta la lectura
        //    porque puede generar saltos falsos en la distancia.
        // ------------------------------------------------------------------
        if (nueva.getAccuracy() > 18) { // Original 15
            Log.w(TAG,
                    "Localización descartada por baja precisión: "
                            + nueva.getAccuracy());
            return;
        }

        // ------------------------------------------------------------------
        // Primera localización válida
        //    No se puede calcular distancia todavía porque no hay
        //    un punto anterior con el que comparar.
        // ------------------------------------------------------------------
        if (ultimaLocalizacion == null) {
            ultimaLocalizacion = nueva;
            Log.d(TAG, "Primera localización válida registrada");
            return;
        }

        // ------------------------------------------------------------------
        // Cálculo del incremento real de distancia
        //    Se calcula la distancia entre la última localización válida
        //    y la nueva localización recibida.
        // ------------------------------------------------------------------
        double incremento = ultimaLocalizacion.distanceTo(nueva);

        // ------------------------------------------------------------------
        // Filtro de ruido
        //    Incrementos demasiado pequeños (< 0.8 m) suelen ser ruido
        //    del GPS cuando el usuario está parado o se mueve muy poco.
        // ------------------------------------------------------------------
        if (incremento < 0.9) { // Original 0.8
            Log.d(TAG,
                    "Incremento demasiado pequeño descartado: "
                            + incremento + " m");
            return;
        }

        // ------------------------------------------------------------------
        // Filtro de saltos irreales
        //    Incrementos grandes (> 8 m) en poco tiempo suelen indicar
        //    errores del GPS (rebotes o cambios bruscos de señal).
        // ------------------------------------------------------------------
        if (incremento > 7) { // Original 8
            Log.w(TAG,
                    "Salto GPS descartado: "
                            + incremento + " m");
            return;
        }

        // Filtro de velocidad (nuevo)
        float velocidad = nueva.getSpeed(); // m/s

        // Si la velocidad es muy baja, probablemente está parado
        if (velocidad < 0.3f) { // < ? km/h
            Log.d(TAG, "Descartado por velocidad baja: " + velocidad);
            return;
        }


        // ------------------------------------------------------------------
        // Incremento válido
        //    Se suma la distancia al total acumulado del día.
        // ------------------------------------------------------------------
        distanciaAcumulada += incremento;

        int idUsuario = SesionManager.obtenerIdUsuario(this);

        Log.d(TAG, "Incremento aceptado: " + incremento + " m");
        Log.d(TAG,
                "Total acumulado (local): "
                        + distanciaAcumulada + " m");

        if (idUsuario > 0) {

            // Guardado incremental en backend
            LogicaFake.guardarRecorrido(
                    idUsuario,
                    incremento,
                    Volley.newRequestQueue(this)
            );

            Log.d(TAG, "Incremento enviado al backend");

            // Broadcast para actualización inmediata de la UI
            Intent intent = new Intent(ACTION_RECorrido_UPDATE);
            intent.putExtra("distancia_total", distanciaAcumulada);
            sendBroadcast(intent);

            Log.d(TAG,
                    "Broadcast enviado. Distancia total: "
                            + distanciaAcumulada);
        }

        // ------------------------------------------------------------------
        // Actualización de la última localización válida
        //    Solo se actualiza cuando el incremento ha sido aceptado.
        // ------------------------------------------------------------------
        ultimaLocalizacion = nueva;
    }

    /**
     * Método invocado al destruir el servicio.
     *
     * Libera los recursos asociados al sistema de localización
     * y detiene la recepción de actualizaciones GPS.
     */
    @Override
    public void onDestroy() {

        Log.d(TAG, "Servicio detenido");

        // Marcar servicio como inactivo
        servicioActivo = false;

        if (fusedClient != null && locationCallback != null) {
            fusedClient.removeLocationUpdates(locationCallback);
            Log.d(TAG, "LocationUpdates eliminados");
        }

        // Avisar a la UI de que el servicio se ha detenido
        Intent intent = new Intent(ACTION_SERVICIO_DETENIDO);
        sendBroadcast(intent);

        super.onDestroy();
    }


    /**
     * Servicio no enlazable.
     *
     * @param intent Intent de enlace.
     * @return Siempre {@code null}.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * @brief Crea la notificación del servicio en primer plano.
     *
     * @return Notificación configurada.
     */
    private Notification crearNotificacion() {

        Intent stopIntent = new Intent(this, RecorridoStopReceiver.class);
        stopIntent.setAction(RecorridoStopReceiver.ACTION_STOP);

        PendingIntent stopPendingIntent =
                PendingIntent.getBroadcast(
                        this,
                        0,
                        stopIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Recorrido activo")
                .setContentText("Calculando distancia diaria")
                .setSmallIcon(R.drawable.ic_walk)
                .addAction(
                        R.drawable.ic_stop,
                        "Detener",
                        stopPendingIntent
                )
                .build();
    }

    /**
     * @brief Crea el canal de notificación del servicio.
     *
     * Obligatorio para Android O (API 26) y versiones superiores.
     */
    private void crearCanal() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Recorrido GPS",
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager nm =
                    getSystemService(NotificationManager.class);

            nm.createNotificationChannel(channel);
        }
    }

    /**
     * @brief Inicializa la distancia acumulada del día desde el backend.
     *
     * Recupera la distancia ya recorrida durante el día actual
     * para continuar el cálculo de forma coherente tras reinicios
     * del servicio.
     */
    private void inicializarDistanciaAcumulada() {

        int idUsuario = SesionManager.obtenerIdUsuario(this);

        if (idUsuario <= 0) {
            Log.e(TAG,
                    "ID usuario inválido al inicializar distancia");
            return;
        }

        LogicaFake.obtenerRecorrido(
                idUsuario,
                Volley.newRequestQueue(this),
                new LogicaFake.CallbackRecorrido() {
                    @Override
                    public void onRespuesta(double hoy, double ayer) {

                        distanciaAcumulada = hoy;

                        Log.d(TAG,
                                "Distancia inicial del día cargada: "
                                        + distanciaAcumulada + " m");
                    }
                }
        );
    }
}
