package org.jordi.btlealumnos2021;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.modules.MBTilesFileArchive;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * @class MapasActivity
 * @brief Activity encargada de mostrar el mapa principal y solicitar permisos BLE
 *        para iniciar el servicio de detección de beacons.
 *
 * Esta pantalla se carga después de iniciar sesión. Desde aquí se comprueba si
 * el usuario ha concedido los permisos necesarios para realizar escaneo BLE en
 * segundo plano. Si no están concedidos, se solicitan al usuario.
 * @author Alan Guevara Martinez
 * @date 05/12/2025
 */
public class MapasActivity extends FuncionesBaseActivity {

    /**
     * @brief Código interno para identificar la solicitud de permisos BLE.
     */
    private static final int CODIGO_PERMISOS_BLE = 1001;

    /**
     * @param savedInstanceState Estado previo en caso de recreación.
     * @brief Método llamado al crear la Activity.
     * <p>
     * - Configura el layout.
     * - Ajusta el header y el menú inferior.
     * - Lanza la verificación de permisos BLE.
     * - Configura el botón de información sobre contaminantes.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // MUY IMPORTANTE: inicializar configuración de OSMDroid
        Configuration.getInstance().load(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        );

        setContentView(R.layout.activity_mapas);

        /// Título del encabezado.
        setupHeader("Mapas");

        /// Selección del elemento "Mapas" en la navegación inferior.
        setupBottomNav(0); // 0 = Mapas

        /**
         * @note Se fuerza la verificación de permisos BLE una vez cargada la Activity,
         *       garantizando que el usuario pueda concederlos antes de iniciar el servicio.
         */
        runOnUiThread(() -> verificarPermisosYArrancarServicio());

        /* Llamada al método que carga el mapa de leaflet */
        inicializarMapa();

        /* ------------------- CONTAMINANTES -------------------*/
        LinearLayout bottomSheet = findViewById(R.id.bottomSheetContaminantes);

        // abrir
        findViewById(R.id.chipContaminantes).setOnClickListener(v -> {
            cerrarTodosLosPopups();
            bottomSheet.setVisibility(View.VISIBLE);
        });

        // cerrar
        findViewById(R.id.btnCerrarContaminantes).setOnClickListener(v ->
                bottomSheet.setVisibility(View.GONE)
        );

        // abrir info contaminantes
        findViewById(R.id.btnInfoContaminantes).setOnClickListener(v ->
                startActivity(new Intent(this, InfoContaminantesActivity.class))
        );

        /* ======== Seleccionar contaminantes (gases) ======== */

        LinearLayout itemTodos = findViewById(R.id.itemTodos);
        LinearLayout itemO3 = findViewById(R.id.itemO3);
        LinearLayout itemNO2 = findViewById(R.id.itemNO2);
        LinearLayout itemCO = findViewById(R.id.itemCO);
        LinearLayout itemSO2 = findViewById(R.id.itemSO2);

        // ENLACE AL TEXTO DEL CHIP SUPERIOR
        TextView txtChip = findViewById(R.id.txtChipContaminantes);

        // Listener común
        // Listener común
        View.OnClickListener listener = v -> {

            // Cambia visualmente qué gas está seleccionado
            seleccionarGas(v, itemTodos, itemO3, itemNO2, itemCO, itemSO2);

            // CAMBIA EL TEXTO Y COLOR DEL CHIP SUPERIOR
            if (v == itemTodos) {
                txtChip.setText("Contaminantes");
                txtChip.setTextColor(0xFF059669); // Verde Atmos por defecto
            }
            else if (v == itemO3) {
                txtChip.setText("O₃");
                txtChip.setTextColor(0xFF047857); // color cuando NO es "Todos"
            }
            else if (v == itemNO2) {
                txtChip.setText("NO₂");
                txtChip.setTextColor(0xFF047857);
            }
            else if (v == itemCO) {
                txtChip.setText("CO");
                txtChip.setTextColor(0xFF047857);
            }
            else if (v == itemSO2) {
                txtChip.setText("SO₂");
                txtChip.setTextColor(0xFF047857);
            }
        };

        // Asignar listeners
        itemTodos.setOnClickListener(listener);
        itemO3.setOnClickListener(listener);
        itemNO2.setOnClickListener(listener);
        itemCO.setOnClickListener(listener);
        itemSO2.setOnClickListener(listener);
        /* ---------------- FIN SECCIÓN CONTAMINANTES -----------------*/

        /* ----------------- ÍNDICE CALIDAD DEL AIRE ------------------*/

        //Abrir "popup"
        findViewById(R.id.chipIndice).setOnClickListener(v -> {
            cerrarTodosLosPopups();
            findViewById(R.id.bottomSheetIndice).setVisibility(View.VISIBLE);
        });

        // Cerrar "popup"
        findViewById(R.id.btnCerrarIndice).setOnClickListener(v ->
                findViewById(R.id.bottomSheetIndice).setVisibility(View.GONE)
        );

        /* -------------- FIN SECCIÓN CALIDAD DEL AIRE ---------------*/
    }

    /**
     * @brief Cambia la selección visual de un elemento de gas.
     * @param seleccionado Vista (LinearLayout) que el usuario ha seleccionado.
     * @param todos Lista variable de todos los LinearLayout que forman parte del grupo.
     */
    private void seleccionarGas(View seleccionado, LinearLayout... todos) {

        // Recorremos todos los elementos de la lista
        for (LinearLayout item : todos) {

            // Si este item es el que fue pulsado → lo marcamos como seleccionado
            if (item == seleccionado) {
                item.setBackgroundResource(R.drawable.bg_gasitem_selected);

                // En caso contrario → lo dejamos con el estilo normal
            } else {
                item.setBackgroundResource(R.drawable.bg_gasitem);
            }
        }
    }

    // ------------------ PERMISOS BLE Y ARRANQUE SERVICIO ------------------

    /**
     * @return true si todos los permisos están aprobados; false en caso contrario.
     * @brief Comprueba si todos los permisos necesarios para BLE están concedidos.
     */
    private boolean permisosBLEOK() {

        // Android 12+ (S)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {

            /* Pèrmisos con los q funciona, antes habían otros mas y no funcionaba bien la detección */
            return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        }

        // Android 6–11
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * @brief Verifica si los permisos BLE están concedidos y, si es así,
     * inicia el servicio de detección de beacons.
     * <p>
     * Si los permisos no están concedidos, se solicitan al usuario.
     */
    private void verificarPermisosYArrancarServicio() {

        if (!permisosBLEOK()) {

            /**
             * @note En Android 12+ es obligatorio solicitar permisos BLE
             *       en tiempo de ejecución antes de iniciar un servicio que escanee beacons.
             */
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.FOREGROUND_SERVICE_LOCATION,
                    Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC
            }, CODIGO_PERMISOS_BLE);


        } else {
            iniciarServicioBeacons();
        }
    }

    /**
     * @param requestCode Código identificador de la solicitud.
     * @param permisos    Lista de permisos solicitados.
     * @param resultados  Resultado de cada permiso (concedido o denegado).
     * @brief Callback que recibe el resultado del diálogo de permisos.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permisos, @NonNull int[] resultados) {
        super.onRequestPermissionsResult(requestCode, permisos, resultados);

        if (requestCode == CODIGO_PERMISOS_BLE) {

            boolean concedidos = true;

            /// Comprobar uno por uno si se otorgaron todos los permisos.
            for (int r : resultados) {
                if (r != PackageManager.PERMISSION_GRANTED) {
                    concedidos = false;
                    break;
                }
            }

            if (concedidos) {
                iniciarServicioBeacons();
            } else {
                Toast.makeText(this, "Debes aceptar los permisos para activar el sensor.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * @brief Inicia el ServicioDeteccionBeacons como servicio en primer plano.
     * @note Este servicio requiere permisos BLE y location para funcionar.
     * En Android 12+ es obligatorio iniciarlo mediante startForegroundService().
     */
    private void iniciarServicioBeacons() {
        Intent s = new Intent(MapasActivity.this, ServicioDeteccionBeacons.class);
        startForegroundService(s); ///< Obligatorio para servicios BLE en Android 12+
    }
    // ---------------------------------------------------------


    // ------------------ MAPA CONTAMINACIÓN ------------------

    /**
     * @brief Inicializa el mapa OSM en su estado básico
     * @date 07-12-2025
     */
    private void inicializarMapa() {

        MapView mapa = findViewById(R.id.mapaOSM);

        // Usa 8 hilos para descargar tiles (más rápido que el valor por defecto)
        Configuration.getInstance().setTileDownloadThreads((short) 8);

        // Permite hasta 100 descargas en cola antes de procesarse
        Configuration.getInstance().setTileDownloadMaxQueueSize((short) 100);

        // Cantidad de tiles cacheados en memoria para evitar recargar
        Configuration.getInstance().setCacheMapTileCount((short) 12);

        // Escala los tiles según la densidad de pantalla (mejor nitidez)
        mapa.setTilesScaledToDpi(true);

        // Habilita uso de datos móviles/WiFi para descargar tiles
        mapa.setUseDataConnection(true);

        // Permite zoom con dos dedos y gestos multitouch
        mapa.setMultiTouchControls(true);

        // Activa aceleración por hardware para mover el mapa más fluido
        mapa.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // Usa la fuente de mapa MAPNIK (rápida y estándar de OpenStreetMap)
        mapa.setTileSource(TileSourceFactory.MAPNIK);

        // Zoom por defecto
        mapa.getController().setZoom(13.0);

        // Vista inicial a Gandía
        mapa.getController().setCenter(
                new org.osmdroid.util.GeoPoint(38.995, -0.160)
        );

        // Geolocalización (cuando se le da al botón en el mapa)
        findViewById(R.id.btnMiUbicacion).setOnClickListener(v -> {
            pedirUbicacion(mapa);
        });
    }

    /**
     * Solicita la ubicación actual del usuario y centra el mapa en ese punto.
     *
     * @param mapa Vista del mapa OSM donde se hará el centrado.
     */
    private void pedirUbicacion(MapView mapa) {

        // Comprobamos si el permiso de ubicación fina está concedido.
        // Si no lo está, se solicita al usuario.
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Pedimos el permiso de ubicación (solo este, porque es el necesario).
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2002);
            return; // Salimos para esperar la respuesta del usuario.
        }

        // Obtenemos el gestor de localización del sistema.
        android.location.LocationManager lm =
                (android.location.LocationManager) getSystemService(LOCATION_SERVICE);

        // Intentamos obtener la última ubicación conocida vía GPS.
        // Puede devolver null si el GPS nunca se ha usado o está desactivado.
        android.location.Location loc =
                lm.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);

        if (loc != null) {
            // Si existe una ubicación válida, centramos el mapa suavemente (animación).
            GeoPoint p = new GeoPoint(loc.getLatitude(), loc.getLongitude());
            mapa.getController().setZoom(16.0);
            mapa.getController().animateTo(p);

            // Avisamos al usuario de que la acción tuvo éxito.
            Toast.makeText(this, "Ubicación centrada", Toast.LENGTH_SHORT).show();

        } else {
            // Si no hay ubicación disponible, mostramos un mensaje de error.
            Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * @brief Cierra todos los popups o paneles inferiores visibles.
     *
     * Este método oculta los distintos BottomSheets de la pantalla
     * (contaminantes, índice, fecha —cuando exista—) para garantizar
     * que solo un panel esté visible a la vez.
     */
    private void cerrarTodosLosPopups() {

        // Oculta el panel de información de contaminantes
        findViewById(R.id.bottomSheetContaminantes).setVisibility(View.GONE);

        // Oculta el panel del índice de calidad del aire
        findViewById(R.id.bottomSheetIndice).setVisibility(View.GONE);

        // Aquí añadiremos el BottomSheet de "Fecha" cuando esté implementado
    }

    // ---------------------------------------------------------
}
