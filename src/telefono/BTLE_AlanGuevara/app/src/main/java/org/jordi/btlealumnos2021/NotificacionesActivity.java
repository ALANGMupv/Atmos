/**
 * Nombre Fichero: NotificacionesActivity.java
 * Descripción: Pantalla encargada de mostrar el listado de
 *              notificaciones generadas por la plataforma ATMOS.
 *              Aquí se cargan y renderizan las notificaciones
 *              dentro de un RecyclerView utilizando su adapter.
 *
 *              Las notificaciones incluyen título, descripción,
 *              hora y un indicador visual de “sin leer”.
 *              Al pulsar sobre una notificación, ésta se marca
 *              automáticamente como leída y se mueve a la sección
 *              de "Notificaciones leídas".
 *
 * Autor: Alejandro Vazquez
 * Fecha: 20/11/2025
 */
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
import java.util.List;

public class NotificacionesActivity extends AppCompatActivity {

    private RecyclerView recyclerNotificaciones;
    private NotificacionAdapter adapter;

    // Nuevas (con puntito) y leídas (sin puntito, en sección aparte)
    private ArrayList<NotificacionAtmos> listaNuevas;
    private ArrayList<NotificacionAtmos> listaLeidas;

    // ID de usuario (por ahora fijo para pruebas, luego puede venir de SesionManager)
    private int idUsuario = 23;

    // 🔁 Handler para refrescar periódicamente
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refrescoPeriodico = new Runnable() {
        @Override
        public void run() {
            cargarNotificacionesDesdeServidor();
            // vuelve a programarse en X milisegundos (ahora 5 seg para pruebas)
            handler.postDelayed(this, 5_000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificaciones);

        // Flecha atrás en el header
        ImageView btnBack = findViewById(R.id.btnBackNotificaciones);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        recyclerNotificaciones = findViewById(R.id.recyclerNotificaciones);
        recyclerNotificaciones.setLayoutManager(new LinearLayoutManager(this));

        listaNuevas = new ArrayList<>();
        listaLeidas = new ArrayList<>();

        // 🔄 Recuperar notificaciones leídas guardadas en el móvil
        cargarLeidasDePrefs();

        adapter = new NotificacionAdapter(
                listaNuevas,
                listaLeidas,
                new NotificacionAdapter.OnItemClickListener() {
                    @Override
                    public void onNotificacionClick(boolean esNueva, int indexEnLista) {
                        // Pulsar una NUEVA → pasa a LEÍDAS y pierde el punto
                        if (esNueva) {
                            if (indexEnLista >= 0 && indexEnLista < listaNuevas.size()) {
                                NotificacionAtmos n = listaNuevas.remove(indexEnLista);
                                n.setLeida(true);
                                listaLeidas.add(0, n); // la más reciente al principio

                                // Avisar al backend de que esta notificación ya está leída
                                NotificacionesManager
                                        .getInstance(NotificacionesActivity.this)
                                        .marcarNotificacionComoLeida(
                                                NotificacionesActivity.this,
                                                idUsuario,
                                                n.getIdNotificacion()
                                        );

                                adapter.notifyDataSetChanged();
                                guardarLeidasEnPrefs();
                            }
                        } else {
                            // Por ahora, tocar una leída no hace nada extra
                        }
                    }

                    @Override
                    public void onDeleteClick(boolean esNueva, int indexEnLista) {
                        if (esNueva) {
                            if (indexEnLista >= 0 && indexEnLista < listaNuevas.size()) {
                                NotificacionAtmos n = listaNuevas.remove(indexEnLista);

                                // Avisar al backend de que esta notificación se ha eliminado
                                NotificacionesManager
                                        .getInstance(NotificacionesActivity.this)
                                        .borrarNotificacionBackend(
                                                NotificacionesActivity.this,
                                                idUsuario,
                                                n.getIdNotificacion()
                                        );
                            }
                        } else {
                            if (indexEnLista >= 0 && indexEnLista < listaLeidas.size()) {
                                NotificacionAtmos n = listaLeidas.remove(indexEnLista);

                                NotificacionesManager
                                        .getInstance(NotificacionesActivity.this)
                                        .borrarNotificacionBackend(
                                                NotificacionesActivity.this,
                                                idUsuario,
                                                n.getIdNotificacion()
                                        );

                                guardarLeidasEnPrefs();
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
        );

        recyclerNotificaciones.setAdapter(adapter);

        // Botón "Eliminar todas" (ahora mismo solo limpia las nuevas en local)
        ImageView btnBorrarTodas = findViewById(R.id.btnBorrarTodas);
        if (btnBorrarTodas != null) {
            btnBorrarTodas.setOnClickListener(v -> {
                listaNuevas.clear();
                // Si quisieras vaciar también las leídas, descomenta:
                // listaLeidas.clear();
                // guardarLeidasEnPrefs();
                adapter.notifyDataSetChanged();
            });
        }

        // Primera carga inmediata
        cargarNotificacionesDesdeServidor();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Empieza el refresco periódico al entrar a la pantalla
        handler.post(refrescoPeriodico);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Paramos el refresco cuando sales de la pantalla
        handler.removeCallbacks(refrescoPeriodico);
        // Por si acaso, guardamos el estado actual de leídas
        guardarLeidasEnPrefs();
    }

    // -------------------------------------------------------------------------
    // Carga de notificaciones desde el backend
    // -------------------------------------------------------------------------
    private void cargarNotificacionesDesdeServidor() {

        NotificacionesManager
                .getInstance(this)
                .refrescarNotificaciones(
                        this,
                        idUsuario,
                        new NotificacionesManager.Listener() {
                            @Override
                            public void onResultado(List<NotificacionAtmos> nuevas, boolean hayAlgoNuevo) {

                                // Solo tocamos la lista de NUEVAS
                                listaNuevas.clear();

                                for (NotificacionAtmos n : nuevas) {
                                    // Evitar duplicar notis que ya están en leídas
                                    boolean yaLeida = false;
                                    for (NotificacionAtmos l : listaLeidas) {
                                        String hashN = n.getTipo() + "|" + n.getTexto() + "|" + n.getHora();
                                        String hashL = l.getTipo() + "|" + l.getTexto() + "|" + l.getHora();
                                        if (hashN.equals(hashL)) {
                                            yaLeida = true;
                                            break;
                                        }
                                    }
                                    if (!yaLeida) {
                                        n.setLeida(false);
                                        listaNuevas.add(n);
                                    }
                                }

                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(String mensaje) {
                                // Aquí puedes meter un Toast si quieres debug visual
                                // Toast.makeText(NotificacionesActivity.this, mensaje, Toast.LENGTH_SHORT).show();
                            }
                        }
                );
    }

    // -------------------------------------------------------------------------
    // Persistencia simple de NOTIFICACIONES LEÍDAS en SharedPreferences
    // -------------------------------------------------------------------------

    /**
     * Guarda la lista de notificaciones leídas en SharedPreferences
     * para conservarlas incluso si cambias de vista o cierras la app.
     */
    private void guardarLeidasEnPrefs() {
        SharedPreferences prefs = getSharedPreferences("notis", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        JSONArray arr = new JSONArray();
        for (NotificacionAtmos n : listaLeidas) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("tipo", n.getTipo());
                obj.put("texto", n.getTexto());
                obj.put("hora", n.getHora());
                arr.put(obj);
            } catch (Exception ignored) {}
        }

        editor.putString("leidas_json", arr.toString());
        editor.apply();
    }

    /**
     * Recupera de SharedPreferences la lista de notificaciones leídas
     * guardadas en ejecuciones anteriores.
     */
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
                                0, // idNotificacion no se persiste en prefs
                                o.optString("tipo", ""),
                                "", // título no lo usamos en la sección de leídas
                                o.optString("texto", ""),
                                o.optString("hora", ""),
                                true // ya viene leída
                        )
                );
            }
        } catch (Exception ignored) {}
    }
}
