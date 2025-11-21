package org.jordi.btlealumnos2021;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class MapasActivity extends FuncionesBaseActivity {

    private static final int CODIGO_PERMISOS_BLE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapas);

        // Poner título de página en el header
        setupHeader("Mapas");

        // Configurar menú inferior
        setupBottomNav(0); // 0 = Mapas

        // FORZAMOS que se pidan permisos tras cargar la activity
        runOnUiThread(() -> verificarPermisosYArrancarServicio());


        ImageView infoBtn = findViewById(R.id.infoContaminantes);
        infoBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, InfoContaminantesActivity.class);
            startActivity(intent);
        });
    }

    private boolean permisosBLEOK() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC) == PackageManager.PERMISSION_GRANTED;
    }

    private void verificarPermisosYArrancarServicio() {

        if (!permisosBLEOK()) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permisos, @NonNull int[] resultados) {
        super.onRequestPermissionsResult(requestCode, permisos, resultados);

        if (requestCode == CODIGO_PERMISOS_BLE) {

            boolean concedidos = true;
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

    private void iniciarServicioBeacons() {
        Intent s = new Intent(MapasActivity.this, ServicioDeteccionBeacons.class);
        startForegroundService(s); // obligatorio Android 12+
    }
}
