package org.jordi.btlealumnos2021;
// ------------------------------------------------------------------
// ------------------------------------------------------------------

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

// ------------------------------------------------------------------
// AQUI AHORA MISMO SOBRA CASI TODO O TODO
// MainActivity.java: clase principal de la app Android que gestiona el escaneo BLE,
// muestra información de dispositivos o busca uno concreto, parsea tramas iBeacon con datos de CO₂ y los envía al servidor, además de manejar permisos y botones de la interfaz.
// ------------------------------------------------------------------

public class MainActivity extends AppCompatActivity {


    private static final int CODIGO_PERMISOS_BT = 123;

    private void pedirPermisosBluetooth() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // ANDROID 12+
            String[] permisos = {
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            };
            requestPermissions(permisos, CODIGO_PERMISOS_BT);

        } else {
            // ANDROID 6–11
            String[] permisos = {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
            requestPermissions(permisos, CODIGO_PERMISOS_BT);
        }
    }

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private static final String ETIQUETA_LOG = ">>>>";

    private static final int CODIGO_PETICION_PERMISOS = 11223344;

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private BluetoothLeScanner elEscanner; // Objeto que realizará los escaneos BTLE.

    private ScanCallback callbackDelEscaneo = null; // Se define cuando inicias un escaneo.

    // Se crea una instancia de la  clase LogicaFake
    private LogicaFake logicaFake = new LogicaFake();

    private int ultimoContador = -1; // Recuerda el último contador recibido

    private boolean dispositivoEncontrado = false; // Para el TOAST de Encontrado el dispositivo....(Placa Alan)

    // TextView provisional para mostrar el valor de ozono
    private TextView valorOzonoText;


    // --------------------------------------------------------------
    // → buscarTodosLosDispositivosBTLE() →
    private void buscarTodosLosDispositivosBTLE() {
        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): empieza ");

        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): instalamos scan callback ");

        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            // Se activa cada vez que se recibe un anuncio BLE.
            public void onScanResult( int callbackType, ScanResult resultado ) {
                super.onScanResult(callbackType, resultado);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onScanResult() ");

                mostrarInformacionDispositivoBTLE( resultado );
            }

            @Override
            // Por si acaso llegan todos juntos, te lo entrega en una lista
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onBatchScanResults() ");

            }

            @Override
            // Se activa si falla el escaneo.
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onScanFailed() ");

            }
        };

        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): empezamos a escanear ");

        // LLAMADA
        this.elEscanner.startScan( this.callbackDelEscaneo);

    } // ()

    // --------------------------------------------------------------
    // resultado: ScanResult → mostrarInformacionDispositivoBTLE()
    private void mostrarInformacionDispositivoBTLE( ScanResult resultado ) {

        BluetoothDevice bluetoothDevice = resultado.getDevice();
        byte[] bytes = resultado.getScanRecord().getBytes();
        int rssi = resultado.getRssi();

        Log.d(ETIQUETA_LOG, " ****************************************************");
        Log.d(ETIQUETA_LOG, " ****** DISPOSITIVO DETECTADO BTLE ****************** ");
        Log.d(ETIQUETA_LOG, " ****************************************************");
        Log.d(ETIQUETA_LOG, " nombre = " + bluetoothDevice.getName());
        Log.d(ETIQUETA_LOG, " toString = " + bluetoothDevice.toString());

        /*
        ParcelUuid[] puuids = bluetoothDevice.getUuids();
        if ( puuids.length >= 1 ) {
            //Log.d(ETIQUETA_LOG, " uuid = " + puuids[0].getUuid());
           // Log.d(ETIQUETA_LOG, " uuid = " + puuids[0].toString());
        }*/

        Log.d(ETIQUETA_LOG, " dirección = " + bluetoothDevice.getAddress());
        Log.d(ETIQUETA_LOG, " rssi = " + rssi );

        Log.d(ETIQUETA_LOG, " bytes = " + new String(bytes));
        Log.d(ETIQUETA_LOG, " bytes (" + bytes.length + ") = " + Utilidades.bytesToHexString(bytes));

        TramaIBeacon tib = new TramaIBeacon(bytes);

        Log.d(ETIQUETA_LOG, " ----------------------------------------------------");
        Log.d(ETIQUETA_LOG, " prefijo  = " + Utilidades.bytesToHexString(tib.getPrefijo()));
        Log.d(ETIQUETA_LOG, "          advFlags = " + Utilidades.bytesToHexString(tib.getAdvFlags()));
        Log.d(ETIQUETA_LOG, "          advHeader = " + Utilidades.bytesToHexString(tib.getAdvHeader()));
        Log.d(ETIQUETA_LOG, "          companyID = " + Utilidades.bytesToHexString(tib.getCompanyID()));
        Log.d(ETIQUETA_LOG, "          iBeacon type = " + Integer.toHexString(tib.getiBeaconType()));
        Log.d(ETIQUETA_LOG, "          iBeacon length 0x = " + Integer.toHexString(tib.getiBeaconLength()) + " ( "
                + tib.getiBeaconLength() + " ) ");
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToHexString(tib.getUUID()));
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToString(tib.getUUID()));
        Log.d(ETIQUETA_LOG, " major  = " + Utilidades.bytesToHexString(tib.getMajor()) + "( "
                + Utilidades.bytesToInt(tib.getMajor()) + " ) ");
        Log.d(ETIQUETA_LOG, " minor  = " + Utilidades.bytesToHexString(tib.getMinor()) + "( "
                + Utilidades.bytesToInt(tib.getMinor()) + " ) ");
        Log.d(ETIQUETA_LOG, " txPower  = " + Integer.toHexString(tib.getTxPower()) + " ( " + tib.getTxPower() + " )");
        Log.d(ETIQUETA_LOG, " ****************************************************");

    } // ()

    // --------------------------------------------------------------
    // dispositivoBuscado: Texto → buscarEsteDispositivoBTLE() →
    private void buscarEsteDispositivoBTLE(final String dispositivoBuscado ) {
        Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): empieza ");

        Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): instalamos scan callback ");


        // super.onScanResult(ScanSettings.SCAN_MODE_LOW_LATENCY, result); para ahorro de energía

        this.callbackDelEscaneo = new ScanCallback() {

            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                super.onScanResult(callbackType, resultado);
                // Lógica cambiada para encontrar un dispositivo en concreto
                // Compara el nombre y si lo encuentra, lo muestra y se detiene la búsqueda

                BluetoothDevice bluetoothDevice = resultado.getDevice();
                // Instanciamos el nombre dentro de la función
                String nombre = bluetoothDevice.getName();

                // Si es igual el nombre al dispositivo buscaddo
                if (nombre != null && nombre.equals(dispositivoBuscado)) {


                    // Mostrar el Toast, LOG y la información solo la PRIMERA vez
                    if (!dispositivoEncontrado) {
                        dispositivoEncontrado = true;
                        Log.d(ETIQUETA_LOG, "Encontrado el dispositivo: " + nombre);

                        Toast.makeText(MainActivity.this,
                                "Encontrado el dispositivo " + nombre,
                                Toast.LENGTH_SHORT).show();

                        // Mostramos la información del dispositivo en concreto
                        mostrarInformacionDispositivoBTLE(resultado);
                    }

                    // -------------------------------------------------------------------------
                    // -------------------------------------------------------------------------

                    // Decido hacerlo aquí porque vamos a parsear la trama de mi dispositivo en concreto y no de cualquiera
                    // Parseo de trama y envío por POST con LogicaFake

                    byte[] bytes = resultado.getScanRecord().getBytes(); // Obtener los bytes crudos del anuncio BLE
                    TramaIBeacon tib = new TramaIBeacon(bytes); // Interpretar los bytes como una trama iBeacon, llama automaticamente al constructor

                    // Extraer UUID, major y minor del beacon con la clase Utilidades
                    String uuid = Utilidades.bytesToString(tib.getUUID());
                    int major = Utilidades.bytesToInt(tib.getMajor());
                    int minor = Utilidades.bytesToInt(tib.getMinor());

                    int gas = (major >> 8) & 0xFF;   // parte alta = tipo (11 = CO2)
                    int contador = major & 0xFF;     // parte baja = contador
                    float valor = minor / 1000.0f; // Entre 1000 para la conversión y f para que se muestren los decimales

                    // Mostrar en el TextView con 4 decimales
                    runOnUiThread(() -> {
                        // Locale.US le dice a Android que use el formato estadounidense, es decir:
                        // - punto . como separador decimal
                        // - coma , como separador de miles
                        valorOzonoText.setText(String.format(Locale.US, "Valor ozono (ppm): %.4f", valor));
                    });


                    // Nuevo: solo enviamos si el contador ha cambiado
                    if (contador != ultimoContador) {

                        // Detectar huecos ( ESTO DETECTA CONTADORES PERDIDOS, HE TENIDO PROBLEMAS CON ESO)
                        if (ultimoContador != -1 && contador > ultimoContador + 1) {
                            // Hemos perdido alguno entre medio
                            for (int perdido = ultimoContador + 1; perdido < contador; perdido++) {
                                Log.w(ETIQUETA_LOG, "⚠ Se perdió el contador " + perdido);
                                Toast.makeText(MainActivity.this,
                                        "⚠ Perdido contador " + perdido,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        ultimoContador = contador;

                        Log.d(ETIQUETA_LOG, "Enviando a la API. ¡A ver si tenemos suerte!");

                        // Obtenemos el RSSI real del resultado del escaneo
                        // int rssi = result.getRssi();

                        // Enviar los datos procesados al servidor, incluyendo ahora el RSSI
                        // logicaFake.guardarMedicion(uuid, gas, valor, rssi);


                        // Mostrar Toast para verificar en el móvil
                        Toast.makeText(MainActivity.this,
                                "contador=" + contador,
                                Toast.LENGTH_SHORT).show();

                        // -------------------------------------------------------------------------
                        // -------------------------------------------------------------------------

                        Log.d(ETIQUETA_LOG, "Si todo ha ido bien, guardado en la BBDD");
                    }
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onBatchScanResults() ");

            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onScanFailed() ");

            }
        };

        ScanFilter sf = new ScanFilter.Builder().setDeviceName( dispositivoBuscado ).build();

        Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): empezamos a escanear buscando: " + dispositivoBuscado );
        //Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): empezamos a escanear buscando: " + dispositivoBuscado
        //      + " -> " + Utilidades.stringToUUID( dispositivoBuscado ) );


        // Se supone que añade un modo más agresivo de búsqueda, a ver si así no pierdo anuncios por el camino
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .build();

        this.elEscanner.startScan( this.callbackDelEscaneo );
    } // ()

    // --------------------------------------------------------------
    // → detenerBusquedaDispositivosBTLE() →
    private void detenerBusquedaDispositivosBTLE() {

        if ( this.callbackDelEscaneo == null ) {
            return;
        }

        this.elEscanner.stopScan( this.callbackDelEscaneo );
        this.callbackDelEscaneo = null;

    } // ()

    // --------------------------------------------------------------
    // inicializarBlueTooth() →
    private void inicializarBlueTooth() {
        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): obtenemos adaptador BT ");

        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): habilitamos adaptador BT ");

        bta.enable();

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): habilitado =  " + bta.isEnabled() );

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): estado =  " + bta.getState() );

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): obtenemos escaner btle ");

        this.elEscanner = bta.getBluetoothLeScanner();

        if ( this.elEscanner == null ) {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): Socorro: NO hemos obtenido escaner btle  !!!!");

        }

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): voy a perdir permisos (si no los tuviera) !!!!");

        // Cambios necesarios
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 o superior → pide permisos de dispositivos cercanos
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_CONNECT
                        },
                        CODIGO_PETICION_PERMISOS
                );
            } else {
                Log.d(ETIQUETA_LOG, "Permisos de Bluetooth (Android 12+) ya concedidos");
            }
        } else {
            // Android 11 o inferior → sigue usando localización
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        CODIGO_PETICION_PERMISOS
                );
            } else {
                Log.d(ETIQUETA_LOG, "Permisos de localización ya concedidos||inicializarBlueTooth(): parece que YA tengo los permisos necesarios !!!!");
            }
        }
    } // ()


    // --------------------------------------------------------------
    // (savedInstanceState: Bundle) → onCreate()
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pedirPermisosBluetooth();
        // -------------------------------------- (Borrar después)

        // Botón temporal que te redirige a Login (Borrar después)
        Button irLoginBtn = findViewById(R.id.irLogin);

        irLoginBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, InicioSesionActivity.class);
            startActivity(intent);
        });

        // Botón temporal que te redirige a perfil (Borrar después)
        Button irPerfilBtn = findViewById(R.id.perfilPage);

        irPerfilBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PerfilActivity.class);
            startActivity(intent);
        });

        // Botón temporal que te redirige a vincular código por qr o escrito (Borrar después)
        Button irPagQR = findViewById(R.id.vincularPage);

        irPagQR.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, VincularSensorActivity.class);
            startActivity(intent);
        });

        // Botón temporal que te redirige a menú
        Button irMenu = findViewById(R.id.irMenu);

        irMenu.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MenuActivity.class);
            startActivity(intent);
        });



        // -------------------------------------- (Borrar después)


        // Inicializamos el textView
        valorOzonoText = findViewById(R.id.valorOzono);

        Log.d(ETIQUETA_LOG, " onCreate(): empieza ");

        inicializarBlueTooth();

        Log.d(ETIQUETA_LOG, " onCreate(): termina ");

    } // onCreate()

    // --------------------------------------------------------------
    // (requestCode: Z, permissions: Texto[], grantResults: Z[])
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults);

        switch (requestCode) {
            case CODIGO_PETICION_PERMISOS:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): permisos concedidos  !!!!");
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                }  else {

                    Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): Socorro: permisos NO concedidos  !!!!");

                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    } // ()



} // class
// --------------------------------------------------------------
// --------------------------------------------------------------
// --------------------------------------------------------------
// --------------------------------------------------------------


