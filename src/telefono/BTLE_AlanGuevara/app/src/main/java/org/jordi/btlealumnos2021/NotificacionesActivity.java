package org.jordi.btlealumnos2021;

import android.content.SharedPreferences;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NotificacionesActivity extends AppCompatActivity {

    private RecyclerView recyclerNotificaciones;
    private NotificacionAdapter adapter;

    private ArrayList<NotificacionAtmos> listaNuevas;
    private ArrayList<NotificacionAtmos> listaLeidas;

    // 🛑 Lista negra PERMANENTE durante la sesión
    private final ArrayList<Integer> listaNegraBorrados = new ArrayList<>();

    private int idUsuario = 23;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refrescoPeriodico = new Runnable() {
        @Override
        public void run() {
            cargarNotificacionesDesdeServidor();
            handler.postDelayed(this, 5000);
        }
    };

    // Lanzador para solicitar permiso
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                // Si acepta, genial. Si no, no insistimos (política amigable).
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificaciones);

        ImageView btnBack = findViewById(R.id.btnBackNotificaciones);
        btnBack.setOnClickListener(v -> finish());

        recyclerNotificaciones = findViewById(R.id.recyclerNotificaciones);
        recyclerNotificaciones.setLayoutManager(new LinearLayoutManager(this));

        listaNuevas = new ArrayList<>();
        listaLeidas = new ArrayList<>();

        cargarLeidasDePrefs();

        adapter = new NotificacionAdapter(new NotificacionAdapter.OnItemClickListener() {
            @Override
            public void onNotificacionClick(boolean esNueva, int idNotificacion) {
                if (esNueva) {
                    // Buscar en listaNuevas por ID
                    NotificacionAtmos target = null;
                    for (NotificacionAtmos n : listaNuevas) {
                        if (n.getIdNotificacion() == idNotificacion) {
                            target = n;
                            break;
                        }
                    }

                    if (target != null) {
                        listaNuevas.remove(target);
                        target.setLeida(true);
                        listaLeidas.add(0, target);

                        // Marcar en backend
                        NotificacionesManager.getInstance(NotificacionesActivity.this)
                                .marcarNotificacionComoLeida(
                                        NotificacionesActivity.this,
                                        idUsuario,
                                        target.getIdNotificacion()
                                );

                        guardarLeidasEnPrefs();
                        adapter.updateData(listaNuevas, listaLeidas);
                    }
                }
            }

            @Override
            public void onDeleteClick(boolean esNueva, int idNotificacion) {
                // Borrar de la lista correspondiente
                List<NotificacionAtmos> lista = esNueva ? listaNuevas : listaLeidas;
                NotificacionAtmos target = null;
                for (NotificacionAtmos n : lista) {
                     if (n.getIdNotificacion() == idNotificacion) {
                         target = n;
                         break;
                     }
                }

                if (target != null) {
                    lista.remove(target);
                    // 🛑 Agregar a lista negra
                    listaNegraBorrados.add(idNotificacion);

                    NotificacionesManager.getInstance(NotificacionesActivity.this)
                            .borrarNotificacionBackend(
                                    NotificacionesActivity.this,
                                    idUsuario,
                                    idNotificacion,
                                    null
                            );
                    
                    adapter.updateData(listaNuevas, listaLeidas);
                }
            }
        });

        recyclerNotificaciones.setAdapter(adapter);

        ImageView btnBorrarTodas = findViewById(R.id.btnBorrarTodas);
        btnBorrarTodas.setOnClickListener(v -> borrarTodasNotificaciones());

        pedirPermisosNotificaciones();
    }

    private void pedirPermisosNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarNotificacionesDesdeServidor();
        // Iniciar polling (refresco automático cada 5s)
        handler.postDelayed(refrescoPeriodico, 5000);
    }





    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(refrescoPeriodico);
        guardarLeidasEnPrefs();
    }

    // ─────────────────────────────────────────────
    //     Cargar desde backend con lista negra
    // ─────────────────────────────────────────────

    private void cargarNotificacionesDesdeServidor() {

        NotificacionesManager.getInstance(this)
                .refrescarNotificaciones(
                        this,
                        idUsuario,
                        new NotificacionesManager.Listener() {

                            @Override
                            public void onResultado(
                                    java.util.List<NotificacionAtmos> nuevas,
                                    boolean hayAlgoNuevo
                            ) {

                                listaNuevas.clear();

                                for (NotificacionAtmos n : nuevas) {

                                    // 🛑 Si está en lista negra → no aparece JAMÁS
                                    if (listaNegraBorrados.contains(n.getIdNotificacion())) {
                                        continue;
                                    }

                                    boolean yaLeida = false;

                                    for (NotificacionAtmos l : listaLeidas) {
                                        if (l.getIdNotificacion() == n.getIdNotificacion()) {
                                            yaLeida = true;
                                            break;
                                        }
                                    }

                                    if (!yaLeida) listaNuevas.add(n);
                                }

                                adapter.updateData(listaNuevas, listaLeidas);
                            }

                            @Override
                            public void onError(String msg) { }
                        }
                );
    }

    // ─────────────────────────────────────────────
    //              Borrar TODAS
    // ─────────────────────────────────────────────

    private void borrarTodasNotificaciones() {

        NotificacionesManager.getInstance(this)
                .borrarTodasBackend(idUsuario, () -> {

                    listaNuevas.clear();
                    listaLeidas.clear();
                    listaNegraBorrados.clear();

                    guardarLeidasEnPrefs();
                    adapter.updateData(listaNuevas, listaLeidas);
                });
    }

    // ─────────────────────────────────────────────
    //        Persistencia local de leídas
    // ─────────────────────────────────────────────

    private void guardarLeidasEnPrefs() {
        SharedPreferences prefs = getSharedPreferences("notis", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        JSONArray arr = new JSONArray();

        for (NotificacionAtmos n : listaLeidas) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("id", n.getIdNotificacion());
                obj.put("tipo", n.getTipo());
                obj.put("texto", n.getTexto());
                obj.put("hora", n.getHora());
                obj.put("timestamp", n.getTimestamp());
                arr.put(obj);
            } catch (Exception ignored) {}
        }

        editor.putString("leidas_json", arr.toString());
        editor.apply();
    }

    private void cargarLeidasDePrefs() {
        SharedPreferences prefs = getSharedPreferences("notis", MODE_PRIVATE);
        String json = prefs.getString("leidas_json", "[]");

        listaLeidas.clear();

        try {
            JSONArray arr = new JSONArray(json);

            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);

                listaLeidas.add(
                        new NotificacionAtmos(
                                o.optInt("id", -1),
                                o.optString("tipo", ""),
                                o.optString("titulo", ""), // Fix: Load title if possible, or empty
                                o.optString("texto", ""),
                                o.optString("hora", ""),
                                o.optLong("timestamp", 0L),
                                true
                        )
                );
            }
        } catch (Exception ignored) {}
    }
}
