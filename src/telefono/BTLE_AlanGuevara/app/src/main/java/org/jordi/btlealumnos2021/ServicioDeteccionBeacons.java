package org.jordi.btlealumnos2021;

import android.annotation.SuppressLint;
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

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;
// IMPORTANTE: No voy a hacer el promedio porque ya de por sí (por lo que sea) el sensor mide mal, no quiero más problemas, ya hay bastantes (Att: Alan)
// Segun chat en pantalla apagada no recibe beacons por que Pantalla apagada + apps en segundo plano =  ALTA restricción
/**
 * @class ServicioDeteccionBeacons
 * @brief Servicio responsable del escaneo BLE Atmos, parseo iBeacon, validación de placa,
 *        promedio de mediciones, obtención GPS y envío al backend.
 *
 * @details
 *  - Escanea continuamente anuncios BLE iBeacon Atmos.
 *  - Filtra por UUID correspondiente a la placa asignada al usuario.
 *  - Interpreta major/minor → tipoGas / valorMedido.
 *  - Acumula 10 muestras → calcula promedio valor + RSSI.
 *  - Obtiene la ubicación del móvil vía FusedLocationProviderClient.
 *  - Envía la medición promediada mediante LogicaFake.guardarMedicion().
 *  - Mantiene control del estado encendida/apagada de la placa.
 *
 * @date 06/12/2025
 * @author
 * Alan Guevara Martínez
 */
public class ServicioDeteccionBeacons extends Service {

    private static final String TAG = "ServicioBeacons";
    private static final String CANAL_ID = "CANAL_BEACON_ATMOS";

    // BLE
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner escanerBLE;

    // Placa asignada (UUID esperado en ASCII)
    private String placaVinculada;

    // Última vez que se recibió un beacon válido
    private long ultimoBeaconTimestamp = 0;
    private Handler handlerEstado = new Handler();

    // Promedio de medidas
    private final List<Float> bufferValores = new ArrayList<>();
    private final List<Integer> bufferRSSI = new ArrayList<>();

    // GPS
    private FusedLocationProviderClient fusedLocation;

    // Lógica API
    private LogicaFake logicaFake;

    /*  Variable para recordar el último valor real
    // Necesario hacerlo así para el promedio ya que Android recibe la misma medida muchas veces, si hacemos el promedio de 10 en bruto
    // solo estamos haciendo el promedio de 10 anuncios y no de 10 medidas reales
    private float ultimoValorReal = -999f; */

    // SI EL CONTADOR (major) NO CAMBIA → NO ENVIAR LA MEDIDA
    private int ultimoContador = -1;

    // Apagar el servicio desde la noti
    public static final String ACCION_APAGAR = "APAGAR_SERVICIO";




    // ---------------------------------------------------------------------------------------------

    /**
     * @brief Inicializa notificación, lógica BLE, GPS y obtiene la placa asignada.
     *
     * @details
     *  - Crea canal de notificación (Foreground Service).
     *  - Inicializa GPS con FusedLocationProviderClient.
     *  - Obtiene la placa vinculada mediante resumenUsuarioPorGas().
     *  - Inicia escaneo BLE.
     *
     * @return void
     */
    @Override
    public void onCreate() {
        super.onCreate();

        crearCanalNotificaciones();
        iniciarComoServicioEnPrimerPlano();

        logicaFake = new LogicaFake();
        fusedLocation = LocationServices.getFusedLocationProviderClient(this);

        inicializarBluetoothYBuscarPlaca();
    }

    /**
     * @brief Crea canal de notificación requerido por Android 8+.
     *
     * @details Si el canal no existe, Android puede bloquear el servicio en primer plano.
     *
     * @return void
     */
    private void crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CANAL_ID,
                    "Detección de Beacons Atmos",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(canal);
        }
    }

    /**
     * @brief Inicia el servicio como Foreground Service para permitir escaneo continuo BLE.
     *
     * @details Se muestra una notificación persistente mientras el servicio está activo.
     *
     * @return void
     */
    private void iniciarComoServicioEnPrimerPlano() {
        Notification notif = new NotificationCompat.Builder(this, CANAL_ID)
                .setContentTitle("Atmos – Sensor activo")
                .setContentText("Detectando beacons y enviando mediciones...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();

        startForeground(1, notif);
    }

    /**
     * @brief Prepara los componentes BLE y consulta la placa asignada al usuario.
     *
     * @details
     *  - Obtiene el BluetoothAdapter y su escáner.
     *  - Pide al backend la placa a la que pertenece el usuario logueado.
     *  - Al obtener placa, inicia el escaneo BLE.
     *
     * @return void
     */
    private void inicializarBluetoothYBuscarPlaca() {

        BluetoothManager manager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        bluetoothAdapter = manager.getAdapter();
        escanerBLE = bluetoothAdapter.getBluetoothLeScanner();

        int idUsuario = SesionManager.obtenerIdUsuario(this);
        if (idUsuario <= 0) {
            stopSelf();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        LogicaFake.resumenUsuarioPorGas(
                idUsuario,
                11,
                queue,
                new LogicaFake.ResumenUsuarioCallback() {

                    @Override public void onSinPlaca() { stopSelf(); }

                    @Override
                    public void onConPlaca(String placa, double u, String f, double p) {

                        placaVinculada = placa.trim();
                        iniciarEscaneoBLE();
                    }

                    @Override public void onErrorServidor() { stopSelf(); }
                    @Override public void onErrorInesperado() { stopSelf(); }
                }
        );
    }

    /**
     * @brief Inicia el escaneo BLE continuo en modo baja latencia.
     *
     * @details
     *  - Usa ScanSettings.SCAN_MODE_LOW_LATENCY.
     *  - Conecta ScanCallback definido en callbackBLE.
     *  - Activa monitor de estado (encendida/apagada).
     *
     * @return void
     */
    private void iniciarEscaneoBLE() {
        try {

            // Intentar que deje de filtrar anuncios BLE cuando la pantalla se apaga. (-- NO FUNCIONA :( --)
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                    .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                    .build();


            escanerBLE.startScan(null, settings, callbackBLE);

            iniciarVerificacionEstado();

        } catch (Exception e) {
            Log.e(TAG, "Error iniciando BLE", e);
        }
    }

    /**
     * @brief Callback principal invocado en cada anuncio BLE recibido.
     *
     * @details
     *  - Registra cualquier beacon Atmos detectado.
     *  - Delegado hacia procesarBeaconDetectado() para análisis profundo.
     */
    private final ScanCallback callbackBLE = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            BluetoothDevice dev = result.getDevice();
            Log.d(TAG, "BLE detectado: " + dev.getName());

            Log.d("ATMOS_SCAN", "Beacon bruto detectado: " + result.getDevice().getName());

            procesarBeaconDetectado(result);
        }
    };

    /**
     * @brief Procesa una trama BLE tipo Atmos (iBeacon) y valida si pertenece a la placa del usuario.
     *
     * @details
     *  - Convierte bytes en objeto TramaIBeacon.
     *  - Obtiene UUID ASCII y compara con placaVinculada.
     *  - Interpreta major/minor → tipoGas y valorMedido.
     *  - Registra actividad para el monitor de estado.
     *  - Envía valores al sistema de promedio → onBeaconDetectado().
     *
     * @param[in] result ScanResult recibido por el escáner BLE.
     *
     * @return void
     */
    private void procesarBeaconDetectado(ScanResult result) {

        try {
            if (result.getScanRecord() == null) return;

            byte[] bytes = result.getScanRecord().getBytes();
            TramaIBeacon tib = new TramaIBeacon(bytes);

            String uuidLeido = Utilidades.bytesToString(tib.getUUID());
            if (uuidLeido == null) return;

            uuidLeido = uuidLeido.trim();

            if (placaVinculada == null) return;
            if (!uuidLeido.equals(placaVinculada)) return;

            ultimoBeaconTimestamp = System.currentTimeMillis();
            logicaFake.actualizarEstadoPlaca(placaVinculada, 1);

            int rssi = result.getRssi();
            int major = Utilidades.bytesToInt(tib.getMajor());
            int minor = Utilidades.bytesToInt(tib.getMinor());

            int tipoGas = (major >> 8) & 0xFF;
            float valorMedido = minor / 10000f;

            // LOG PARA VER CADA MEDICIÓN RECIBIDA
            Log.d("ATMOS_MEDICION",
                    "UUID=" + uuidLeido +
                            " | tipoGas=" + tipoGas +
                            " | valor=" + valorMedido +
                            " | rssi=" + rssi +
                            " | timestamp=" + System.currentTimeMillis()
            );


            // Ahora le pasamos también el contador, para que distinga que es una medida distinta, no hay promedio, no sirve de nada, SI FUNCIONA NO SE TOCA
            int contador = major & 0xFF;
            onBeaconDetectado(uuidLeido, valorMedido, rssi, tipoGas, contador);


        } catch (Exception e) {
            Log.e(TAG, "Error procesando beacon", e);
        }
    }

    /**
     * @brief Recibe cada medición válida y calcula un promedio cada 10 muestras.
     *
     * @details
     *  - Acumula 10 muestras en bufferValores y bufferRSSI.
     *  - Al completar 10 → calcula promedio valor + RSSI.
     *  - Obtiene GPS vía FusedLocationProviderClient.
     *  - Envía la medición promediada al backend usando LogicaFake.guardarMedicion().
     *
     * @param[in] uuid UUID ASCII de la placa Atmos.
     * @param[in] valor Valor medido crudo (float).
     * @param[in] rssi RSSI del paquete BLE recibido.
     * @param[in] tipoGas Tipo de gas Atmos (11/12/13/14).
     *
     * @return void
     */
    public void onBeaconDetectado(String uuid, float valor, int rssi, int tipoGas, int contador) {

        /*
        --- ESTO ESTÁ ROMPIENDO LA LÓGICA DEL ESTADO DEL SENSOR (encendido o apagado y la señal rssi ---
        --- APARTE NO SIRVE DE NADA HACER EL PROMEDIO EL SENSOR SIGUE MIDIENDO MAL ---
        --- LO ÚNICO QUE HAY QUE CONTROLAR ES QUE NO SE ENVIE UNA MEDIDA IGUAL A LA ANTERIOR Y YA --- (contador distinto al anterior, como dijo Jordi)

        // 1) Detectar si esta medición es REAL (ha cambiado)
        if (Math.abs(valor - ultimoValorReal) < 0.0001f) {
            // Es el mismo valor que antes → es anuncio duplicado → ignorar
            Log.d("ATMOS_REAL", "Duplicado BLE ignorado. valor=" + valor);
            return;
        }

        // 2) Es una nueva medición real → guardarla
        ultimoValorReal = valor;

        Log.d("ATMOS_REAL", "Nueva medición REAL añadida al buffer → valor=" + valor);

        bufferValores.add(valor);
        bufferRSSI.add(rssi);

        // 3) Si aún no tenemos 5 mediciones reales → salir
        if (bufferValores.size() < 5)
            return;

        // 4) Calcular promedios reales
        double promValor = bufferValores.stream().mapToDouble(Float::doubleValue).average().orElse(0);
        int promRSSI = (int) bufferRSSI.stream().mapToInt(Integer::intValue).average().orElse(0);

        bufferValores.clear();
        bufferRSSI.clear(); */

        // 1) Si el contador es igual al anterior → es duplicado BLE → ignorar
        if (contador == ultimoContador) {
            Log.d("ATMOS_DUPLICADO", "Ignorado contador repetido = " + contador);
            return;
        }

        // 2) Nuevo contador real → guardar
        ultimoContador = contador;

        Log.d("ATMOS_REAL", "NUEVA medida real: valor=" + valor + " contador=" + contador);

        // 3) Obtener GPS y enviar normalmente
        obtenerGPS((lat, lon) -> {

            logicaFake.guardarMedicion(
                    uuid,
                    tipoGas,
                    valor,    // valor real sin promedio
                    rssi,
                    lat,
                    lon
            );

            Log.d("ATMOS_ENVIO",
                    "ENVIADO valor=" + valor +
                            " tipoGas=" + tipoGas +
                            " contador=" + contador +
                            " rssi=" + rssi +
                            " lat=" + lat +
                            " lon=" + lon);
        });
    }

    /**
     * @brief Obtiene la ubicación aproximada del dispositivo o (-1, -1) si no disponible.
     *
     * @details
     *  - Accede a getLastLocation().
     *  - Si falla → aplica fallback a (-1, -1).
     *
     * @param[in] callback Función que recibe latitud y longitud.
     *
     * @return void
     */
    @SuppressLint("MissingPermission")
    private void obtenerGPS(GPSCallback callback) {

        fusedLocation.getLastLocation()
                .addOnSuccessListener(loc -> {
                    if (loc != null)
                        callback.onGPS(loc.getLatitude(), loc.getLongitude());
                    else
                        callback.onGPS(-1, -1);
                })
                .addOnFailureListener(e -> callback.onGPS(-1, -1));
    }

    /**
     * @brief Interfaz callback para recibir coordenadas GPS.
     */
    private interface GPSCallback {
        void onGPS(double lat, double lon);
    }

    /**
     * @brief Comprueba periódicamente si la placa sigue enviando beacons.
     *
     * @details
     *  - Si pasan >5s sin recibir anuncios BLE → encendida = 0.
     *  - Se ejecuta cada 2 segundos.
     *
     * @return void
     */
    private void iniciarVerificacionEstado() {

        handlerEstado.postDelayed(new Runnable() {
            @Override
            public void run() {

                long ahora = System.currentTimeMillis();

                if (placaVinculada != null &&
                        ahora - ultimoBeaconTimestamp > 5000) {

                    logicaFake.actualizarEstadoPlaca(placaVinculada, 0);
                }

                handlerEstado.postDelayed(this, 2000);
            }
        }, 2000);
    }

    /**
     * @brief Servicio no enlazado; devuelve siempre null.
     *
     * @return IBinder Siempre null porque no se usa bound service.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
