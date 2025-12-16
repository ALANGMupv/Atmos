package org.jordi.btlealumnos2021;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.modules.MBTilesFileArchive;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alan Guevara Martinez
 * @class MapasActivity
 * @brief Activity encargada de mostrar el mapa principal y solicitar permisos BLE
 * para iniciar el servicio de detección de beacons.
 * <p>
 * Esta pantalla se carga después de iniciar sesión. Desde aquí se comprueba si
 * el usuario ha concedido los permisos necesarios para realizar escaneo BLE en
 * segundo plano. Si no están concedidos, se solicitan al usuario.
 */
public class MapasActivity extends FuncionesBaseActivity {
    /**
     * @brief Variables privadas
     */
    private static final int CODIGO_PERMISOS_BLE = 1001;
    private String ultimaQuery = "";
    private android.os.Handler handler = new android.os.Handler();
    private Runnable tareaBusqueda = null;
    private android.text.TextWatcher watcher;
    private Marker marcadorUsuario = null;
    // Overlay encargado de pintar el mapa interpolado de contaminación
    private ContaminacionOverlay overlayContaminacion;
    // Acceso a la lógica fake que realiza peticiones a la API y devuelve JSON
    private LogicaFake logica = new LogicaFake();
    // Variable global mapa
    private MapView mapa;
    // Para que el marcador de ubi no se mueva
    private android.location.Location ultimaLoc = null;
    // Variables para evitar recalcular el índice mientras el usuario arrastra el mapa (VA/IBA LENTO TRAS PONER LOS INDICES EN FUNCIONAMIENTO).
    private boolean mapaEnMovimiento = false;
    private final android.os.Handler handlerIndice = new android.os.Handler();
    private Runnable tareaDelayedIndice;
    // -----------------------------------------------------------------------
    // handler para detectar FIN DE MOVIMIENTO
    private final android.os.Handler handlerMovimiento = new android.os.Handler();
    private Runnable tareaFinMovimiento = null;
    // -----------------------------------------------------------------------
    private boolean indiceRecibidoTrasMovimiento = false;

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
        View.OnClickListener listener = v -> {

            // Cambia visualmente qué gas está seleccionado
            seleccionarGas(v, itemTodos, itemO3, itemNO2, itemCO, itemSO2);

            // Texto del chip
            if (v == itemTodos) {
                txtChip.setText("Contaminantes");
                txtChip.setTextColor(0xFF059669);

                // CARGAR TODOS LOS GASES
                cargarContaminacion("TODOS");

            } else if (v == itemO3) {
                txtChip.setText("O₃");
                txtChip.setTextColor(0xFF047857);

                cargarContaminacion("13"); // O3

            } else if (v == itemNO2) {
                txtChip.setText("NO₂");
                txtChip.setTextColor(0xFF047857);

                cargarContaminacion("11"); // NO2

            } else if (v == itemCO) {
                txtChip.setText("CO");
                txtChip.setTextColor(0xFF047857);

                cargarContaminacion("12"); // CO

            } else if (v == itemSO2) {
                txtChip.setText("SO₂");
                txtChip.setTextColor(0xFF047857);

                cargarContaminacion("14"); // SO2
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

        /* ----------------- BUSCADOR DE UBICACIONES ------------------*/
        EditText edtBuscar = findViewById(R.id.edtBuscar);

        LinearLayout layoutBusqueda = findViewById(R.id.layoutBusqueda);

        //  El cuadro completo funciona como un solo input
        layoutBusqueda.setOnClickListener(v -> {
            edtBuscar.requestFocus();
            edtBuscar.setCursorVisible(true);

            // Mostrar teclado
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager)
                            getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(edtBuscar, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        });

        ListView listaSugerencias = findViewById(R.id.listaSugerencias);

        edtBuscar.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                edtBuscar.setCursorVisible(false);
                listaSugerencias.setVisibility(View.GONE);

                // IMPORTANTE: evita peleas de foco
                new android.os.Handler().postDelayed(() ->
                        findViewById(R.id.layoutBusqueda).clearFocus(), 50);

            } else {
                edtBuscar.setCursorVisible(true);
            }
        });


        ArrayAdapter<String> adapterSugerencias = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, new ArrayList<>());

        listaSugerencias.setAdapter(adapterSugerencias);

        /**
         * @brief Listener que detecta cambios en el texto para autocompletar.
         */
        watcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String texto = s.toString().trim();
                ultimaQuery = texto;

                // Si no hay texto → ocultar sugerencias
                if (texto.length() < 3) {
                    listaSugerencias.setVisibility(View.GONE);
                    return;
                }

                // Cancelamos la última búsqueda programada
                if (tareaBusqueda != null) handler.removeCallbacks(tareaBusqueda);

                // Programamos una nueva búsqueda con retraso (debounce)
                tareaBusqueda = () -> {
                    autocompletar(texto, resultados -> {

                        // Ignorar resultados que no coincidan con la última query
                        if (!texto.equals(ultimaQuery)) return;

                        adapterSugerencias.clear();
                        adapterSugerencias.addAll(resultados);
                        adapterSugerencias.notifyDataSetChanged();
                        listaSugerencias.setVisibility(View.VISIBLE);
                    });
                };

                // Espera 300ms antes de llamar a Nominatim → MUCHÍSIMO MÁS FLUIDO
                handler.postDelayed(tareaBusqueda, 300);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        };

        edtBuscar.addTextChangedListener(watcher);

        /*----------------------------------------------------------------------------*/
        /* -------------- SECCIÓN BUSCADOR DE UBICACIONES ---------------*/

        edtBuscar.setOnEditorActionListener((v, actionId, event) -> {

            // Cuando el usuario pulsa "Buscar" en el teclado
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER
                            && event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {

                String texto = edtBuscar.getText().toString().trim();

                if (!texto.isEmpty()) {
                    listaSugerencias.setVisibility(View.GONE);
                    buscarUbicacion(texto);

                    // Cerrar teclado
                    android.view.inputmethod.InputMethodManager imm =
                            (android.view.inputmethod.InputMethodManager)
                                    getSystemService(INPUT_METHOD_SERVICE);

                    imm.hideSoftInputFromWindow(edtBuscar.getWindowToken(), 0);
                }

                return true; // Consumimos el evento
            }

            return false;
        });


        /**
         * @brief Cuando el usuario toca una sugerencia → usarla como búsqueda
         */
        listaSugerencias.setOnItemClickListener((parent, view, position, id) -> {
            String seleccionado = adapterSugerencias.getItem(position);

            // Desactivar textWatcher para evitar que vuelva a aparecer
            edtBuscar.removeTextChangedListener(watcher);

            edtBuscar.setText(seleccionado);
            edtBuscar.clearFocus();
            findViewById(R.id.mapaOSM).requestFocus(); // Dispara el foco fuera del EditText

            // Reactivar textWatcher
            edtBuscar.addTextChangedListener(watcher);

            listaSugerencias.setVisibility(View.GONE);
            adapterSugerencias.clear();

            buscarUbicacion(seleccionado);
        });

        /* -------------- FIN SECCIÓN BUSCADOR DE UBICACIONES ---------------*/
    }

    /**
     * @param seleccionado Vista (LinearLayout) que el usuario ha seleccionado.
     * @param todos        Lista variable de todos los LinearLayout que forman parte del grupo.
     * @brief Cambia la selección visual de un elemento de gas.
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
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(s);
        } else {
            startService(s);
        }
    }
    // ---------------------------------------------------------


    // ------------------ MAPA CONTAMINACIÓN ------------------

    /**
     * @brief Inicializa el mapa OSM en su estado básico
     * @date 07-12-2025
     */
    private void inicializarMapa() {

        mapa = findViewById(R.id.mapaOSM);

        // Evitar recalcular el índice mientras el usuario arrastra el mapa
        mapa.addMapListener(new org.osmdroid.events.MapListener() {

            @Override
            public boolean onScroll(org.osmdroid.events.ScrollEvent event) {

                // Cada vez que hay un scroll → marcar como en movimiento
                mapaEnMovimiento = true;
                indiceRecibidoTrasMovimiento = false;

                // Cancelar detección anterior
                if (tareaFinMovimiento != null)
                    handlerMovimiento.removeCallbacks(tareaFinMovimiento);

                // Programar detección de “movimiento detenido”
                tareaFinMovimiento = () -> {
                    // Solo marcamos que el mapa ha dejado de moverse
                    mapaEnMovimiento = false;
                };

                // Esperamos un poco a ver si el overlay responde
                new android.os.Handler().postDelayed(() -> {
                    if (!indiceRecibidoTrasMovimiento) {
                        runOnUiThread(() ->
                                actualizarPanelIndice(0, 0, 0, 0, "sin_datos")
                        );
                    }
                }, 300);

                // Si pasan 120 ms sin nuevos scrolls → movimiento parado
                handlerMovimiento.postDelayed(tareaFinMovimiento, 120);

                return false;
            }

            @Override
            public boolean onZoom(org.osmdroid.events.ZoomEvent event) {
                // Zoom no cuenta como movimiento del dedo
                mapaEnMovimiento = false;
                return false;
            }
        });
        // ----------------------------------------------------------------------

        // Escala los tiles según la densidad → más nitidez
        new XYTileSource(
                "StadiaBright",
                0, 19, 512, ".png",
                new String[]{
                        "https://tiles.stadiamaps.com/tiles/osm_bright/?api_key=24c196c7-e10b-44a0-b046-fc160c2f51fe"
                }
        );

        Configuration.getInstance().setUserAgentValue("AtmosApp");
        Configuration.getInstance().setMapViewHardwareAccelerated(true);
        Configuration.getInstance().setDebugMode(false);


        // ----- CACHE Y RENDIMIENTO -----
        Configuration.getInstance().setCacheMapTileCount((short) 64);
        Configuration.getInstance().setCacheMapTileOvershoot((short) 32);

        // Más hilos para descargar mapas
        Configuration.getInstance().setTileDownloadThreads((short) 8);

        // Cola de descargas más grande
        Configuration.getInstance().setTileDownloadMaxQueueSize((short) 100);

        // Usa datos móviles/wifi
        mapa.setUseDataConnection(true);

        // Gestos multitáctiles
        mapa.setMultiTouchControls(true);

        // Configuración de inicio
        mapa.getController().setZoom(14.0);
        mapa.getController().setCenter(new GeoPoint(38.995, -0.160));

        // Botón Mi Ubicación
        findViewById(R.id.btnMiUbicacion).setOnClickListener(v -> pedirUbicacion(mapa));

        // Dibujamos el puntito de ubicación del usuario al abrir el mapa
        pedirUbicacion(mapa);

        // Escuchar ubicación en tiempo real (solución definitiva)
        iniciarActualizacionUbicacion(mapa);

        /* --- PINTAR MAPA --- */
        overlayContaminacion = new ContaminacionOverlay();
        mapa.getOverlays().add(overlayContaminacion);
        /* --------------------------- */

        /* -------------------------------------------------------------------------
         * RECEPCIÓN DEL ÍNDICE DE CALIDAD DEL AIRE DESDE EL OVERLAY
         * @brief Recibe los porcentajes calculados por el ContaminacionOverlay.
         *
         * Si no hay datos visibles en el área actual del mapa, se limpia el panel
         * de índice para evitar mostrar información obsoleta (última ciudad válida).
         * ------------------------------------------------------------------------- */
        overlayContaminacion.setOnIndiceUpdateListener((b, m, i, ma, dominante) -> {

            indiceRecibidoTrasMovimiento = true;

            // Cancelamos cualquier actualización pendiente
            if (tareaDelayedIndice != null)
                handlerIndice.removeCallbacks(tareaDelayedIndice);

            // Tarea diferida para evitar recalcular mientras el mapa se mueve
            tareaDelayedIndice = () -> {
                if (!mapaEnMovimiento) {

                    int total = b + m + i + ma;

                    // CASO: no hay datos visibles → limpiar panel de índice
                    if (total == 0) {
                        runOnUiThread(() ->
                                actualizarPanelIndice(0, 0, 0, 0, "sin_datos")
                        );
                        return;
                    }

                    // CASO: hay datos válidos → actualizar panel normalmente
                    runOnUiThread(() ->
                            actualizarPanelIndice(b, m, i, ma, dominante)
                    );
                }
            };

            handlerIndice.postDelayed(tareaDelayedIndice, 150);
        });
        /* --------------------------- */

        // Dibujar mapa desde el principio al incializarlo
        cargarContaminacion("TODOS");
    }

    /* -------------------------------------------------------------------------
     * LIMPIEZA DEL PANEL DE ÍNDICE DE CALIDAD DEL AIRE
     * ------------------------------------------------------------------------- */

    /**
     * Solicita la ubicación actual del usuario y centra el mapa en ese punto.
     *
     * @param mapa Vista del mapa OSM donde se hará el centrado.
     */
    private void pedirUbicacion(MapView mapa) {

        // Comprobamos si el permiso de ubicación está concedido.
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2002);
            return;
        }

        // ------------------------------------------------------------------
        // 1) SI YA TENEMOS UNA UBICACIÓN REAL (GPS o NETWORK) → usarla
        // ------------------------------------------------------------------
        if (ultimaLoc != null) {

            GeoPoint p = new GeoPoint(
                    ultimaLoc.getLatitude(),
                    ultimaLoc.getLongitude()
            );

            mapa.getController().setZoom(18.0);
            mapa.getController().animateTo(p);

            if (marcadorUsuario == null) {
                marcadorUsuario = new Marker(mapa);
                marcadorUsuario.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                marcadorUsuario.setIcon(
                        androidx.core.content.ContextCompat.getDrawable(
                                this,
                                R.drawable.marker_mi_ubicacion
                        )
                );
                mapa.getOverlays().add(marcadorUsuario);
            }

            marcadorUsuario.setPosition(p);
            mapa.invalidate();
            return;
        }

        // ------------------------------------------------------------------
        // 2) SI NO HAY UBICACIÓN AÚN → probar con lastKnownLocation
        // ------------------------------------------------------------------
        android.location.LocationManager lm =
                (android.location.LocationManager) getSystemService(LOCATION_SERVICE);

        android.location.Location loc =
                lm.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);

        if (loc == null) {
            loc = lm.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);
        }

        if (loc != null) {

            ultimaLoc = loc; // MUY IMPORTANTE: sincronizar

            GeoPoint p = new GeoPoint(loc.getLatitude(), loc.getLongitude());
            mapa.getController().setZoom(18.0);
            mapa.getController().animateTo(p);

            if (marcadorUsuario == null) {
                marcadorUsuario = new Marker(mapa);
                marcadorUsuario.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                marcadorUsuario.setIcon(
                        androidx.core.content.ContextCompat.getDrawable(
                                this,
                                R.drawable.marker_mi_ubicacion
                        )
                );
                mapa.getOverlays().add(marcadorUsuario);
            }

            marcadorUsuario.setPosition(p);
            mapa.invalidate();

        } else {
            // SOLO ahora tiene sentido mostrar este mensaje
            Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * @brief Cierra todos los popups o paneles inferiores visibles.
     * <p>
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

    /**
     * @param textoBuscado Texto introducido por el usuario que representa la ubicación a buscar.
     * @brief Busca una dirección o lugar a partir de un texto y centra el mapa en ese punto.
     * <p>
     * Este método utiliza el Geocoder de Android para convertir el texto introducido
     * por el usuario (nombre de calle, ciudad, punto de interés…) en coordenadas
     * geográficas (latitud y longitud). Si encuentra un resultado válido, mueve el
     * mapa OSMDroid hasta esa ubicación y aplica un zoom adecuado.
     */
    private void buscarUbicacion(String textoBuscado) {

        MapView mapa = findViewById(R.id.mapaOSM);

        // Hacemos la petición de red en un hilo secundario (Nominatim no permite llamadas en el UI thread)
        new Thread(() -> {
            try {

                // Construimos la URL de búsqueda en Nominatim (OpenStreetMap)
                String url =
                        "https://nominatim.openstreetmap.org/search?format=json&q=" +
                                java.net.URLEncoder.encode(textoBuscado, "UTF-8") +
                                "&limit=1";

                // Abrimos la conexión HTTP
                java.net.URL u = new java.net.URL(url);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u.openConnection();
                conn.setRequestProperty("User-Agent", "AtmosApp/1.0"); // Obligatorio para Nominatim

                // Leemos la respuesta completa en formato JSON
                java.io.InputStream is = conn.getInputStream();
                String json = new java.util.Scanner(is).useDelimiter("\\A").next();

                // Convertimos la respuesta a un array JSON
                org.json.JSONArray arr = new org.json.JSONArray(json);

                // Si no hay resultados → avisamos en UI
                if (arr.length() == 0) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "No se encontró la ubicación", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                // Tomamos el primer resultado devuelto
                org.json.JSONObject obj = arr.getJSONObject(0);

                // Extraemos latitud y longitud
                double lat = obj.getDouble("lat");
                double lon = obj.getDouble("lon");

                GeoPoint destino = new GeoPoint(lat, lon);

                // Hilo principal
                runOnUiThread(() -> {

                    // Centramos el mapa y ajustamos el zoom
                    mapa.getController().setZoom(17.0);
                    mapa.getController().animateTo(destino);

                    Toast.makeText(this, "Ubicación encontrada", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                e.printStackTrace(); // Errores de red / JSON / formato
            }
        }).start();
    }


    /**
     * @param query    Texto parcial introducido por el usuario en el buscador.
     * @param callback Función callback que recibirá la lista de sugerencias.
     * @brief Obtiene sugerencias de ubicaciones desde Nominatim (OSM) para autocompletado.
     * @details Realiza una petición HTTP a la API de Nominatim buscando coincidencias
     * y devuelve hasta 5 resultados. Es ideal para mostrar autocompletado estilo Google Maps.
     * @note Obligatorio enviar "User-Agent" en Nominatim o devuelve error 403.
     */
    private void autocompletar(String query, AutocompleteCallback callback) {

        new Thread(() -> {
            try {
                String url =
                        "https://nominatim.openstreetmap.org/search?format=json&q=" +
                                java.net.URLEncoder.encode(query, "UTF-8") +
                                "&addressdetails=1&limit=5";

                java.net.URL u = new java.net.URL(url);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u.openConnection();
                conn.setRequestProperty("User-Agent", "AtmosApp/1.0");

                java.io.InputStream is = conn.getInputStream();
                String json = new java.util.Scanner(is).useDelimiter("\\A").next();

                org.json.JSONArray arr = new org.json.JSONArray(json);

                java.util.List<String> sugerencias = new java.util.ArrayList<>();

                for (int i = 0; i < arr.length(); i++) {
                    sugerencias.add(arr.getJSONObject(i).getString("display_name"));
                }

                runOnUiThread(() -> callback.onResult(sugerencias));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * @interface AutocompleteCallback
     * @brief Interfaz para recibir resultados de autocompletado.
     */
    interface AutocompleteCallback {
        /**
         * @param resultados Lista de sugerencias obtenidas desde Nominatim.
         */
        void onResult(java.util.List<String> resultados);
    }

    /**
     * @param mapa Mapa OSMDroid donde se dibuja el marcador de ubicación.
     * @brief Inicia la actualización continua de la ubicación del usuario en el mapa.
     * <p>
     * Este método registra un LocationListener que:
     * - Obtiene la ubicación real del GPS cada pocos segundos o cada metro recorrido.
     * - Dibuja automáticamente el marcador de ubicación del usuario.
     * - Actualiza su posición si el usuario se mueve.
     * @note Este método soluciona el problema de que getLastKnownLocation()
     * puede devolver null y el puntito no aparezca al abrir la app.
     */
    private void iniciarActualizacionUbicacion(MapView mapa) {

        // Accedemos al LocationManager del sistema
        android.location.LocationManager lm =
                (android.location.LocationManager) getSystemService(LOCATION_SERVICE);

        // Verificamos permisos antes de solicitar actualizaciones
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        /*
         * Solicitamos actualizaciones continuas de la ubicación:
         *   - Cada 2000 ms (2 segundos)
         *   - Cada 1 metro recorrido
         */

        // -------------------------------------------------------------
        // 1) GPS_PROVIDER → muy preciso, pero puede NO emitir en interior
        // -------------------------------------------------------------
        lm.requestLocationUpdates(
                android.location.LocationManager.GPS_PROVIDER,
                2000,   // intervalo mínimo en ms
                1,      // distancia mínima en metros
                location -> {

                    // Si tenemos una ubicación previa, verificamos si el movimiento es significativo
                    if (ultimaLoc != null) {

                        float distancia = location.distanceTo(ultimaLoc);

                        // Si el movimiento es menor de 2 metros → ignorar para evitar jitter
                        if (distancia < 2) {
                            return;
                        }
                    }

                    // Guardamos esta ubicación como última válida
                    ultimaLoc = location;

                    // Convertimos la ubicación a un GeoPoint compatible con OSMDroid
                    GeoPoint p = new GeoPoint(location.getLatitude(), location.getLongitude());

                    // Si el marcador no existe, lo creamos una vez
                    if (marcadorUsuario == null) {
                        marcadorUsuario = new Marker(mapa);

                        // Centramos el icono en el punto
                        marcadorUsuario.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);

                        // Icono personalizado: tu circulito de ubicación
                        marcadorUsuario.setIcon(
                                androidx.core.content.ContextCompat.getDrawable(
                                        this,
                                        R.drawable.marker_mi_ubicacion
                                )
                        );

                        // Lo añadimos al mapa
                        mapa.getOverlays().add(marcadorUsuario);
                    }

                    // Actualizamos la posición del marcador
                    marcadorUsuario.setPosition(p);

                    // Redibujamos
                    mapa.invalidate();
                }
        );

        // -----------------------------------------------------------------
        // 2) NETWORK_PROVIDER → menos preciso, pero SIEMPRE responde en ciudad
        // -----------------------------------------------------------------
        lm.requestLocationUpdates(
                android.location.LocationManager.NETWORK_PROVIDER,
                2000,   // intervalo mínimo en ms
                1,      // distancia mínima en metros
                location -> {

                    // Si tenemos una ubicación previa, verificamos si el movimiento es significativo
                    if (ultimaLoc != null) {

                        float distancia = location.distanceTo(ultimaLoc);

                        // Si el movimiento es menor de 2 metros → ignorar para evitar jitter
                        if (distancia < 2) {
                            return;
                        }
                    }

                    // Guardamos esta ubicación como última válida
                    ultimaLoc = location;

                    // Convertimos la ubicación a un GeoPoint compatible con OSMDroid
                    GeoPoint p = new GeoPoint(location.getLatitude(), location.getLongitude());

                    // Si el marcador no existe, lo creamos una vez
                    if (marcadorUsuario == null) {
                        marcadorUsuario = new Marker(mapa);

                        // Centramos el icono en el punto
                        marcadorUsuario.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);

                        // Icono personalizado: tu circulito de ubicación
                        marcadorUsuario.setIcon(
                                androidx.core.content.ContextCompat.getDrawable(
                                        this,
                                        R.drawable.marker_mi_ubicacion
                                )
                        );

                        // Lo añadimos al mapa
                        mapa.getOverlays().add(marcadorUsuario);
                    }

                    // Actualizamos la posición del marcador
                    marcadorUsuario.setPosition(p);

                    // Redibujamos
                    mapa.invalidate();
                }
        );
    }

    /* ----- SECCIÓN PINTAR MAPA ----- */
    /**
     * @brief Carga puntos de contaminación desde la API y los envía al overlay.
     *
     * @details
     * Este método solicita datos al servidor según el tipo de gas indicado.
     *
     * Flujo:
     *  - Si tipoGas = "TODOS": obtiene medidas de varios gases y escoge la peor.
     *  - Si tipoGas = código numérico: solo dibuja ese gas.
     *
     * Luego:
     *  - Normaliza el valor según tablas EPA.
     *  - Convierte el nivel normalizado a un color RGB.
     *  - Crea objetos PuntoContaminacion que luego el overlay interpolará.
     *
     * Finalmente se actualiza el mapa en el hilo principal.
     *
     * @param tipoGas Cadena "TODOS" o código numérico del gas ("11", "12", "13", "14").
     */
    private void cargarContaminacion(String tipoGas) {

        // Caso A: se piden todos los gases y se elige el peor índice para cada placa
        if (tipoGas.equals("TODOS")) {

            logica.obtenerMedidasTodos(arr -> {

                if (arr == null) return;

                // Lista donde almacenaremos los puntos preparados para el overlay
                List<ContaminacionOverlay.PuntoContaminacion> lista = new ArrayList<>();

                for (int i = 0; i < arr.length(); i++) {
                    try {
                        JSONObject o = arr.getJSONObject(i);

                        // Coordenadas geográficas del punto
                        double lat = o.getDouble("latitud");
                        double lon = o.getDouble("longitud");

                        // Valores de cada gas (pueden no existir)
                        double no2 = o.optDouble("NO2", -1);
                        double co  = o.optDouble("CO", -1);
                        double o3  = o.optDouble("O3", -1);
                        double so2 = o.optDouble("SO2", -1);

                        // Normalizamos cada gas individualmente y nos quedamos con el peor nivel
                        double peor = Math.max(
                                Math.max(normal(no2, 11), normal(co, 12)),
                                Math.max(normal(o3, 13), normal(so2, 14))
                        );

                        // Creamos punto para el overlay
                        lista.add(new ContaminacionOverlay.PuntoContaminacion(lat, lon, peor));

                    } catch (Exception ignored) {}
                }

                // Los overlays deben actualizarse desde el hilo principal (UI Thread)
                runOnUiThread(() -> {
                    overlayContaminacion.setPuntos(lista);
                    mapa.invalidate(); // fuerza repintado
                });
            });

        } else {

            // Caso B: se pide solo un gas específico
            int gas = Integer.parseInt(tipoGas);

            logica.obtenerMedidasPorGas(gas, arr -> {

                if (arr == null) return;

                List<ContaminacionOverlay.PuntoContaminacion> lista = new ArrayList<>();

                for (int i = 0; i < arr.length(); i++) {
                    try {
                        JSONObject o = arr.getJSONObject(i);

                        double lat = o.getDouble("latitud");
                        double lon = o.getDouble("longitud");

                        // Valor del gas seleccionado
                        double v = o.getDouble("valor");

                        // Normalizar el valor según la tabla del gas correspondiente
                        double n = normal(v, gas);

                        /*
                         * En modo gas individual:
                         * seguimos usando IDW clásico,
                         * pero enviamos el nivel, no el color
                         */
                        lista.add(new ContaminacionOverlay.PuntoContaminacion(lat, lon, n));

                    } catch (Exception ignored) {}
                }

                // Actualizamos en UI Thread
                runOnUiThread(() -> {
                    overlayContaminacion.setPuntos(lista);
                    mapa.invalidate();
                });
            });
        }
    }

    /**
     * @brief Normaliza un valor de un gas a un nivel entre 0 y 1.
     *
     * @details
     * Cada gas tiene sus propios umbrales (tomados de estándares EPA).
     * De esta forma, valores pequeños se convierten en niveles bajos (verde),
     * y valores altos se convierten en niveles altos (rojo).
     *
     * @param valor Valor absoluto medido del gas.
     * @param tipo Código del gas (11=NO2, 12=CO, 13=O3, 14=SO2).
     * @return Nivel normalizado: 0.1, 0.45, 0.75 o 1.0 según rangos.
     */
    private double normal(double valor, int tipo) {
        if (valor < 0) return 0;

        switch (tipo) {
            case 12: // CO
                if (valor <= 1.7) return 0.1;
                if (valor <= 4.4) return 0.45;
                if (valor <= 8.7) return 0.75;
                return 1.0;

            case 11: // NO2
                if (valor <= 0.021) return 0.1;
                if (valor <= 0.053) return 0.45;
                if (valor <= 0.106) return 0.75;
                return 1.0;

            case 13: // O3
                if (valor <= 0.031) return 0.1;
                if (valor <= 0.061) return 0.45;
                if (valor <= 0.092) return 0.75;
                return 1.0;

            case 14: // SO2
                if (valor <= 0.0076) return 0.1;
                if (valor <= 0.019) return 0.45;
                if (valor <= 0.038) return 0.75;
                return 1.0;
        }

        return 0;
    }

    /* ----- FIN SECCIÓN PINTAR MAPA -----*/

    /* ----- SECCIÓN ACTUALIZAR PANEL ÍNDICES MAPA -----*/
    /**
     * @brief Actualiza el panel de índice según los valores interpolados visibles.
     * @details Colores, textos y recomendaciones se ajustan automáticamente
     *          igual que en la versión Web.
     *
     * @param buena Porcentaje de celdas buenas.
     * @param moderada Porcentaje moderadas.
     * @param insalubre Porcentaje insalubres.
     * @param mala Porcentaje malas.
     * @param dominante Categoría predominante → define el color principal.
     */
    private void actualizarPanelIndice(int buena, int moderada, int insalubre, int mala, String dominante) {

        // === Obtener referencias a los TextView ===
        TextView tBuena = findViewById(R.id.txtPctBuena);
        TextView tModerada = findViewById(R.id.txtPctModerada);
        TextView tInsalubre = findViewById(R.id.txtPctInsalubre);
        TextView tMala = findViewById(R.id.txtPctMala);

        TextView txtCalidad = findViewById(R.id.txtCalidadIndice);

        LinearLayout recBox = findViewById(R.id.boxRecomendacion);
        TextView recText = findViewById(R.id.txtRecomendacion);

        // IMPORTANTE: reactivar recomendaciones tras haberlas ocultado
        recBox.setVisibility(View.VISIBLE);

        // === Mostrar porcentajes ===
        tBuena.setText(buena + "%");
        tModerada.setText(moderada + "%");
        tInsalubre.setText(insalubre + "%");
        tMala.setText(mala + "%");

        // === Cambiar color, texto y recomendación ===
        switch (dominante) {

            case "sin_datos":
                txtCalidad.setText("Sin datos disponibles");
                txtCalidad.setTextColor(Color.parseColor("#6B7280"));
                recBox.setBackgroundColor(Color.parseColor("#E5E7EB"));
                recText.setText("No existen mediciones de calidad del aire en esta zona.");
                recText.setTextColor(Color.parseColor("#374151"));
                break;

            case "buena":
                txtCalidad.setText("Calidad Buena");
                txtCalidad.setTextColor(Color.parseColor("#059669"));
                recBox.setBackgroundColor(Color.parseColor("#D1FAE5"));
                recText.setText("La calidad del aire es excelente. Actividades al aire libre recomendadas.");
                recText.setTextColor(Color.parseColor("#065F46"));
                break;

            case "moderada":
                txtCalidad.setText("Calidad Moderada");
                txtCalidad.setTextColor(Color.parseColor("#CA8A04"));
                recBox.setBackgroundColor(Color.parseColor("#FEF9C3"));
                recText.setText("Personas sensibles deben limitar esfuerzos prolongados.");
                recText.setTextColor(Color.parseColor("#854D0E"));
                break;

            case "insalubre":
                txtCalidad.setText("Calidad Insalubre");
                txtCalidad.setTextColor(Color.parseColor("#EA580C"));
                recBox.setBackgroundColor(Color.parseColor("#FFEDD5"));
                recText.setText("Personas sensibles deben reducir actividad al aire libre.");
                recText.setTextColor(Color.parseColor("#9A3412"));
                break;

            case "mala":
                txtCalidad.setText("Calidad Mala");
                txtCalidad.setTextColor(Color.parseColor("#DC2626"));
                recBox.setBackgroundColor(Color.parseColor("#FECACA"));
                recText.setText("Evitar actividades al aire libre. Se recomienda permanecer en interiores.");
                recText.setTextColor(Color.parseColor("#7F1D1D"));
                break;

            default:
                txtCalidad.setText("Sin datos disponibles");
                txtCalidad.setTextColor(Color.parseColor("#6B7280"));
                recBox.setBackgroundColor(Color.parseColor("#E5E7EB"));
                recText.setText("No existen mediciones de calidad del aire en esta zona.");
                recText.setTextColor(Color.parseColor("#374151"));
                break;
        }
    }
    /* ----- FIN SECCIÓN ACTUALIZAR PANEL ÍNDICES MAPA -----*/
    // ---------------------------------------------------------
}
