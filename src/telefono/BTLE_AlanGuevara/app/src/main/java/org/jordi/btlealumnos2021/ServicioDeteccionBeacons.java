package org.jordi.btlealumnos2021;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


/**
 * Nombre del fichero: ServicioDeteccionBeacons.java
 * Autor: Alan Guevara Martínez
 * Fecha: 20/11/2025
 *
 * Descripción:
 *   Servicio en segundo plano encargado de:
 *     - Escanear continuamente beacons BLE.
 *     - Detectar la placa vinculada al usuario.
 *     - Extraer UUID, major, minor, RSSI.
 *     - Enviar mediciones al backend usando LogicaFake.
 */

public class ServicioDeteccionBeacons extends Service {

    private static final String TAG = "ServicioBeacons";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner escanerBLE;

    private LogicaFake logicaFake;

    private String placaVinculada; // normalmente el NOMBRE BT de la placa (AtmosPlacaGrupo4, etc.)

    // variable para recordar el "último beacon recibido"
    private long ultimoBeaconTimestamp = 0;

    // Handler para comprobar si la placa deja de emitir
    private Handler handlerEstado = new Handler();


    @Override
    public void onCreate() {
        super.onCreate();

        // Crear canal de notificación obligatorio (Android 8+)
        crearCanalNotificaciones();

        logicaFake = new LogicaFake();

        // Inicializar Bluetooth
        BluetoothManager manager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = manager.getAdapter();
        escanerBLE = bluetoothAdapter.getBluetoothLeScanner();

        iniciarComoServicioEnPrimerPlano();
        comprobarRequisitosAntesDeEscanear();
    }


    /**
     * Crea el canal de notificación que exige startForeground() desde Android 8+.
     * Si no existe → CRASHEA.
     */
    private void crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    "canal_beacons",
                    "Detección de Beacons",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(canal);
        }
    }


    private void iniciarComoServicioEnPrimerPlano() {

        Notification notification = new NotificationCompat.Builder(this, "canal_beacons")
                .setContentTitle("Atmos – Sensor activo")
                .setContentText("Detectando beacons en segundo plano…")
                // IMPORTANTE: usa un icono que EXISTE para evitar crasheo
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(1, notification);
    }


    /**
     * Comprueba si el usuario está logueado y tiene placa asignada.
     * Si sí → empieza escaneo BLE
     */
    private void comprobarRequisitosAntesDeEscanear() {

        // Obtener ID usuario desde SesionManager (ESTÁTICO)
        int idUsuario = SesionManager.obtenerIdUsuario(this);

        if (idUsuario <= 0) {
            Log.e(TAG, "No hay sesión activa.");
            stopSelf();
            return;
        }

        // Crear cola Volley (sin VolleySingleton)
        RequestQueue queue = Volley.newRequestQueue(this);

        // Usamos /resumenUsuarioPorGas solo para obtener id_placa
        LogicaFake.resumenUsuarioPorGas(
                idUsuario,
                11,
                queue,
                new LogicaFake.ResumenUsuarioCallback() {
                    @Override
                    public void onSinPlaca() {
                        Log.e(TAG, "Usuario sin placa asignada.");
                        stopSelf();
                    }

                    @Override
                    public void onConPlaca(String placa, double u, String f, double p) {
                        // IMPORTANTE:
                        // Aquí asumimos que 'placa' es el NOMBRE BT de la placa
                        // igual que usabas en MainActivity (AtmosPlacaGrupo4, etc.)
                        placaVinculada = placa;
                        Log.d(TAG, "Placa vinculada: " + placaVinculada);
                        iniciarEscaneoBLE();
                    }

                    @Override
                    public void onErrorServidor() {
                        Log.e(TAG, "Error servidor al obtener placa.");
                        stopSelf();
                    }

                    @Override
                    public void onErrorInesperado() {
                        Log.e(TAG, "Error inesperado al obtener placa.");
                        stopSelf();
                    }
                }
        );
    }


    private void iniciarEscaneoBLE() {
        try {
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();

            escanerBLE.startScan(null, settings, callbackBLE);

            Log.d(TAG, "Escaneo BLE iniciado.");

            // Iniciar comprobación periódica del estado (encendida o apagada)
            iniciarVerificacionEstado();

        } catch (Exception e) {
            Log.e(TAG, "ERROR iniciando escaneo BLE", e);
        }
    }


    private final ScanCallback callbackBLE = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            BluetoothDevice dev = result.getDevice();
            Log.d(TAG, "BLE detectado: " + dev.getName());

            procesarBeaconDetectado(result);
        }
    };


    /**
     * Procesa cada anuncio BLE recibido.
     * Aquí replicamos la lógica de Jordi en MainActivity.buscarEsteDispositivoBTLE():
     *   1) Filtrar por NOMBRE del dispositivo (dispositivoBuscado).
     *   2) SOLO si el nombre coincide → parsear TramaIBeacon.
     *   3) Extraer uuid, major, minor igual que antes.
     *   4) Enviar medición al backend.
     */
    /**
     * Procesa cada anuncio BLE recibido.
     * Copiado exactamente del comportamiento de MainActivity.buscarEsteDispositivoBTLE().
     */
    private void procesarBeaconDetectado(ScanResult result) {

        try {
            if (result.getScanRecord() == null) return;

            BluetoothDevice bluetoothDevice = result.getDevice();
            String nombre = bluetoothDevice.getName();

            // Si aún no sabemos qué placa tiene el usuario → no hacemos nada
            if (placaVinculada == null) {
                Log.d(TAG, "placaVinculada aún es null, ignorando beacon.");
                return;
            }

            // Igual que en MainActivity: filtrar solo por nombre
            if (nombre == null || !nombre.equals(placaVinculada)) {
                return;
            }

            // Llegados aquí, ES la placa del usuario (por nombre BT)
            Log.d(TAG, ">> Beacon de la placa detectado: " + nombre);

            // Obtener bytes crudos del anuncio BLE
            byte[] bytes = result.getScanRecord().getBytes();

            // Interpretar como trama iBeacon — igual que MainActivity
            TramaIBeacon tib = new TramaIBeacon(bytes);

            // Extraer UUID como texto (solo para log, no para API — igual que antes)
            String uuid = Utilidades.bytesToString(tib.getUUID());
            Log.d(TAG, "UUID detectado para la placa = '" + uuid + "'");

            // Registrar último beacon recibido
            ultimoBeaconTimestamp = System.currentTimeMillis();

            // Actualizar encendida = 1
            logicaFake.actualizarEstadoPlaca(placaVinculada, 1);

            // Obtener RSSI
            int rssi = result.getRssi();

            // EXTRAER DATOS EXACTAMENTE COMO MAINACTIVITY
            int major = Utilidades.bytesToInt(tib.getMajor());   // NO usar bytesToIntOK
            int minor = Utilidades.bytesToInt(tib.getMinor());

            int tipoGas   = (major >> 8) & 0xFF;
            int contador  = (major & 0xFF);

            float valorMedido = minor / 1000.0f;

            // Enviar medición TAL CUAL hacía MainActivity:
            // - placaVinculada = nombre de la emisora
            logicaFake.guardarMedicion(
                    placaVinculada, // EXACTAMENTE como MainActivity
                    tipoGas,
                    valorMedido,
                    rssi
            );

            Log.d(TAG, "Medición enviada OK. Gas=" + tipoGas +
                    " Valor=" + valorMedido +
                    " RSSI=" + rssi);

        } catch (Exception e) {
            Log.e(TAG, "Error procesando beacon", e);
        }
    }



    /**
     * Comprueba cada 2 segundos:
     *  - Si han pasado más de 5s sin recibir beacons → encendida = 0
     */
    private void iniciarVerificacionEstado() {

        handlerEstado.postDelayed(new Runnable() {
            @Override
            public void run() {

                long ahora = System.currentTimeMillis();

                if (placaVinculada != null) {
                    if (ahora - ultimoBeaconTimestamp > 5000) { // 5 segundos sin beacons
                        logicaFake.actualizarEstadoPlaca(placaVinculada, 0);
                        Log.d(TAG, "Placa sin señal → encendida = 0");
                    }
                }

                // Repetimos cada 2 segundos
                handlerEstado.postDelayed(this, 2000);
            }
        }, 2000);
    }


    @Override
    public int onStartCommand(Intent i, int flags, int id) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
