package org.jordi.btlealumnos2021;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

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

        adapter = new NotificacionAdapter(
                listaNuevas,
                listaLeidas,
                new NotificacionAdapter.OnItemClickListener() {

                    @Override
                    public void onNotificacionClick(boolean esNueva, int index) {
                        if (esNueva && index >= 0 && index < listaNuevas.size()) {

                            NotificacionAtmos n = listaNuevas.remove(index);
                            n.setLeida(true);

                            listaLeidas.add(0, n);

                            // marcar como leída en backend
                            NotificacionesManager.getInstance(NotificacionesActivity.this)
                                    .marcarNotificacionComoLeida(
                                            NotificacionesActivity.this,
                                            idUsuario,
                                            n.getIdNotificacion()
                                    );

                            guardarLeidasEnPrefs();
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onDeleteClick(boolean esNueva, int index) {

                        NotificacionAtmos n;

                        if (esNueva) {
                            if (index < 0 || index >= listaNuevas.size()) return;
                            n = listaNuevas.remove(index);
                        } else {
                            if (index < 0 || index >= listaLeidas.size()) return;
                            n = listaLeidas.remove(index);
                        }

                        // 🛑 Agregar a lista negra → no vuelve JAMÁS esta sesión
                        listaNegraBorrados.add(n.getIdNotificacion());

                        // Borrar en backend
                        NotificacionesManager.getInstance(NotificacionesActivity.this)
                                .borrarNotificacionBackend(
                                        NotificacionesActivity.this,
                                        idUsuario,
                                        n.getIdNotificacion(),
                                        () -> {
                                            // NO limpiamos la lista negra
                                        }
                                );

                        adapter.notifyDataSetChanged();
                    }
                }
        );

        recyclerNotificaciones.setAdapter(adapter);

        ImageView btnBorrarTodas = findViewById(R.id.btnBorrarTodas);
        btnBorrarTodas.setOnClickListener(v -> borrarTodasNotificaciones());

        cargarNotificacionesDesdeServidor();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(refrescoPeriodico);
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

                                adapter.notifyDataSetChanged();
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
                    adapter.notifyDataSetChanged();
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
                                o.optString("texto", ""),
                                o.optString("texto", ""),
                                o.optString("hora", ""),
                                true
                        )
                );
            }
        } catch (Exception ignored) {}
    }
}
