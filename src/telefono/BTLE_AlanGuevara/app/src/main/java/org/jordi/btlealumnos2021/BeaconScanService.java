package org.jordi.btlealumnos2021;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

/**
 * BeaconScanService
 * -------------------------------------------------------------------------
 * Este servicio es un ForegroundService encargado de escanear beacons BTLE
 * de manera continua en segundo plano. Esto permite que la aplicación siga
 * detectando sensores incluso cuando la pantalla está apagada o el usuario
 * cambia de Activity.
 *
 * FUNCIONALIDADES PRINCIPALES:
 *  - Iniciar un escaneo BLE continuo.
 *  - Mantener vivo el servicio mediante una notificación persistente.
 *  - Detectar beacons (RSSI, tramas BTLE).
 *  - Determinar si el sensor está ACTIVO o INACTIVO.
 *  - Calcular la distancia aproximada mediante RSSI (cerca/medio/lejos).
 *  - Enviar el estado al servidor (actualmente simulado con Log.d()).
 */
public class BeaconScanService extends Service {

    public static final String TAG = "BeaconScanService";
    private static final String CHANNEL_ID = "BeaconScanChannel";

    private BluetoothAdapter bluetoothAdapter;         // Acceso al Bluetooth del dispositivo
    private BluetoothLeScanner scanner;               // Escáner BLE
    private boolean escaneando = false;               // Control del estado del escaneo

    // -----------------------------------------------------------
    // Variables para detectar si el beacon está ACTIVO o INACTIVO
    // -----------------------------------------------------------
    private long ultimoTiempoVisto = 0;                // Última vez que detectamos un beacon (ms)
    private static final long TIEMPO_INACTIVO = 5000;  // Si pasan 5s sin verlo → inactivo
    private boolean estadoActual = false;              // Estado actual del sensor: false=inactivo, true=activo

    // --------------------------------------------------------------------------
    // CICLO DE VIDA DEL SERVICIO
    // --------------------------------------------------------------------------

    /**
     * onCreate()
     * ---------------------------------------------------------------------
     * Se ejecuta UNA sola vez cuando Android crea el servicio.
     * Aquí usamos esta función para:
     *  - Inicializar el Bluetooth.
     *  - Crear la notificación persistente necesaria para un ForegroundService.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Servicio creado");

        inicializarBluetooth();        // Prepara adaptador y escáner BLE
        crearNotificacionPersistente(); // Crea la notificación permanente
    }

    /**
     * onStartCommand()
     * ---------------------------------------------------------------------
     * Se ejecuta CADA VEZ que el servicio es arrancado con startForegroundService().
     * Aquí hacemos lo principal:
     *  - Iniciar el escaneo BLE.
     *  - Crear un hilo que comprueba cada segundo si el sensor está inactivo.
     *
     * @return START_STICKY → asegura que el servicio se reinicie si Android lo mata.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "Servicio iniciado. Comenzando escaneo BLE...");

        empezarEscaneo();  // Inicia el escaneo

        // HILO que revisa cada segundo si el beacon está INACTIVO.
        new Thread(() -> {
            while (true) {
                try { Thread.sleep(1000); } catch (Exception e) {}

                long ahora = System.currentTimeMillis();

                // Si el sensor estaba activo pero PASAN más de 5 segundos sin verlo → INACTIVO.
                if (estadoActual && (ahora - ultimoTiempoVisto > TIEMPO_INACTIVO)) {
                    Log.d(TAG, "El beacon está INACTIVO");
                    estadoActual = false;
                    actualizarEstadoEnServidor(0);  // Enviar estado=0 (inactivo)
                }
            }
        }).start();

        return START_STICKY;
    }

    /**
     * onDestroy()
     * ---------------------------------------------------------------------
     * Llamado cuando el servicio se detiene o Android lo elimina.
     * Aquí detenemos el escaneo BLE.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        detenerEscaneo();
        Log.d(TAG, "Servicio destruido");
    }

    /**
     * onBind()
     * ---------------------------------------------------------------------
     * Este servicio NO permite "binding", por lo que devolvemos null.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    // --------------------------------------------------------------------------
    // INICIALIZACIÓN DE BLUETOOTH
    // --------------------------------------------------------------------------

    /**
     * inicializarBluetooth()
     * ---------------------------------------------------------------------
     * Obtiene el BluetoothAdapter y el BluetoothLeScanner.
     * Ambos son necesarios para iniciar el escaneo BTLE.
     */
    private void inicializarBluetooth() {
        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = manager.getAdapter();

        if (bluetoothAdapter != null) {
            scanner = bluetoothAdapter.getBluetoothLeScanner();
        } else {
            Log.e(TAG, "BluetoothAdapter es null → este dispositivo no soporta BLE");
        }
    }


    // --------------------------------------------------------------------------
    // NOTIFICACIÓN DEL SERVICIO
    // --------------------------------------------------------------------------

    /**
     * crearNotificacionPersistente()
     * ---------------------------------------------------------------------
     * Crea una notificación permanente que mantiene vivo el servicio
     * mientras escanea. Es OBLIGATORIO en Android para usar un ForegroundService.
     *
     * También permite que el usuario vuelva a la aplicación haciendo clic.
     */
    private void crearNotificacionPersistente() {

        // Crear canal en Android 8+ (API 26 o superior)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CHANNEL_ID,
                    "Escaneo BLE",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(canal);
        }

        // Intent para volver a MainActivity al pulsar la notificación
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Construcción de la notificación
        Notification noti = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Sensor Atmos")
                .setContentText("Escaneando sensores cercanos...")
                .setSmallIcon(R.drawable.ic_sensor)
                .setContentIntent(pendingIntent)
                .setOngoing(true) // No se puede quitar
                .build();

        // Convertimos el servicio en ForegroundService
        startForeground(1, noti);
    }


    // --------------------------------------------------------------------------
    // CALLBACK DEL ESCANEO BLE
    // --------------------------------------------------------------------------

    /**
     * callbackEscaneo
     * ---------------------------------------------------------------------
     * Este objeto recibe los resultados del escaneo BTLE.
     * Cada vez que un beacon es detectado, esta función se ejecuta.
     *
     * Aquí:
     *  - Obtenemos el RSSI (fuerza de señal).
     *  - Marcamos el sensor como ACTIVO.
     *  - Calculamos su distancia aproximada.
     */
    private final ScanCallback callbackEscaneo = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            int rssi = result.getRssi();                     // Fuerza de señal
            byte[] bytes = result.getScanRecord().getBytes();// Trama BTLE

            Log.d(TAG, "Beacon detectado → RSSI=" + rssi);

            // ACTUALIZAR ÚLTIMA DETECCIÓN
            ultimoTiempoVisto = System.currentTimeMillis();

            // Si estaba INACTIVO y ahora aparece → cambiar a ACTIVO
            if (!estadoActual) {
                estadoActual = true;
                actualizarEstadoEnServidor(1); // Enviar activo=1
            }

            // Calcular nivel de distancia (cerca / medio / lejos)
            String nivel = interpretarDistancia(rssi);
            Log.d(TAG, "Nivel de distancia: " + nivel);
        }
    };


    // --------------------------------------------------------------------------
    // CONTROL DEL ESCANEO BLE
    // --------------------------------------------------------------------------

    /**
     * empezarEscaneo()
     * ---------------------------------------------------------------------
     * Inicia el escaneo BLE si no estaba activo.
     * Comprueba también que tengamos permisos.
     */
    private void empezarEscaneo() {
        if (scanner != null && !escaneando) {
            escaneando = true;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                return; // Sin permisos no podemos escanear
            }
            scanner.startScan(callbackEscaneo);
            Log.d(TAG, "Escaneo BLE iniciado");
        }
    }

    /**
     * detenerEscaneo()
     * ---------------------------------------------------------------------
     * Detiene el escaneo BLE si estaba activo.
     */
    private void detenerEscaneo() {
        if (scanner != null && escaneando) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            scanner.stopScan(callbackEscaneo);
            escaneando = false;
            Log.d(TAG, "Escaneo BLE detenido");
        }
    }

    // --------------------------------------------------------------------------
    // ENVÍO DEL ESTADO AL SERVIDOR
    // --------------------------------------------------------------------------

    /**
     * actualizarEstadoEnServidor()
     * ---------------------------------------------------------------------
     * Envía el estado del sensor al backend.
     * De momento está SIMULADO con Log.d().
     *
     * @param activo 1 = sensor activo, 0 = sensor inactivo
     */
    private void actualizarEstadoEnServidor(int activo) {
        Log.d(TAG, "Enviando estado a servidor: activo=" + activo);

        // Futuro:
        // LogicaReal.getInstance().actualizarEstado(sensorID, activo);
    }

    // --------------------------------------------------------------------------
    // INTERPRETACIÓN DE LA DISTANCIA SEGÚN RSSI
    // --------------------------------------------------------------------------

    /**
     * interpretarDistancia()
     * ---------------------------------------------------------------------
     * Traduce el RSSI en categorías comprensibles:
     *  - "cerca"  (muy buena señal)
     *  - "medio"  (señal media)
     *  - "lejos"  (señal débil)
     *
     * @param rssi nivel de señal recibido
     * @return texto con el nivel de distancia
     */
    private String interpretarDistancia(int rssi) {

        if (rssi > -55) {
            return "cerca";  // Muy cerca del beacon
        } else if (rssi > -70) {
            return "medio";  // Distancia media
        } else {
            return "lejos";  // Beacon lejos
        }
    }
}
