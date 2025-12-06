package org.jordi.btlealumnos2021;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

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

    /** @brief Código interno para identificar la solicitud de permisos BLE. */
    private static final int CODIGO_PERMISOS_BLE = 1001;

    /**
     * @brief Método llamado al crear la Activity.
     *
     * - Configura el layout.
     * - Ajusta el header y el menú inferior.
     * - Lanza la verificación de permisos BLE.
     * - Configura el botón de información sobre contaminantes.
     *
     * @param savedInstanceState Estado previo en caso de recreación.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        /// Botón para abrir el panel de información de contaminantes.
        ImageView infoBtn = findViewById(R.id.infoContaminantes);
        infoBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, InfoContaminantesActivity.class);
            startActivity(intent);
        });
    }

    /**
     * @brief Comprueba si todos los permisos necesarios para BLE están concedidos.
     *
     * @return true si todos los permisos están aprobados; false en caso contrario.
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
     *        inicia el servicio de detección de beacons.
     *
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
     * @brief Callback que recibe el resultado del diálogo de permisos.
     *
     * @param requestCode Código identificador de la solicitud.
     * @param permisos Lista de permisos solicitados.
     * @param resultados Resultado de cada permiso (concedido o denegado).
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
     *
     * @note Este servicio requiere permisos BLE y location para funcionar.
     *       En Android 12+ es obligatorio iniciarlo mediante startForegroundService().
     */
    private void iniciarServicioBeacons() {
        Intent s = new Intent(MapasActivity.this, ServicioDeteccionBeacons.class);
        startForegroundService(s); ///< Obligatorio para servicios BLE en Android 12+
    }
}
