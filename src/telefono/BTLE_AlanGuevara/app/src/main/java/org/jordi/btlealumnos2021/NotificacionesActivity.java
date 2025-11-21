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
 *              automáticamente como leída y se actualiza su estado.
 *
 *              Esta pantalla sirve como centro de alertas para
 *              avisar al usuario sobre eventos importantes como:
 *              niveles críticos, sensores desconectados o
 *              resúmenes diarios.
 *
 * Autor: Alejandro Vazquez
 * Fecha: 20/11/2025
 */
package org.jordi.btlealumnos2021;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NotificacionesActivity extends AppCompatActivity {

    private RecyclerView recyclerNotificaciones;
    private NotificacionAdapter adapter;

    private ArrayList<NotificacionAtmos> listaNuevas;
    private ArrayList<NotificacionAtmos> listaLeidas;

    // 🔁 Handler para refrescar cada 30s
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refrescoPeriodico = new Runnable() {
        @Override
        public void run() {
            cargarNotificacionesDesdeServidor();
            // vuelve a programarse en 30 segundos
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
                                adapter.notifyDataSetChanged();
                            }
                        } else {
                            // De momento, tocar una leída no hace nada especial
                        }
                    }

                    @Override
                    public void onDeleteClick(boolean esNueva, int indexEnLista) {
                        if (esNueva) {
                            if (indexEnLista >= 0 && indexEnLista < listaNuevas.size()) {
                                listaNuevas.remove(indexEnLista);
                            }
                        } else {
                            if (indexEnLista >= 0 && indexEnLista < listaLeidas.size()) {
                                listaLeidas.remove(indexEnLista);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
        );

        recyclerNotificaciones.setAdapter(adapter);

        // Botón "Eliminar todas" (solo limpia en local)
        ImageView btnBorrarTodas = findViewById(R.id.btnBorrarTodas);
        if (btnBorrarTodas != null) {
            btnBorrarTodas.setOnClickListener(v -> {
                listaNuevas.clear();
                // si quieres también vaciar leídas, descomenta:
                // listaLeidas.clear();
                adapter.notifyDataSetChanged();
            });
        }

        // Primera carga inmediata
        cargarNotificacionesDesdeServidor();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Empieza el refresco periódico
        handler.post(refrescoPeriodico);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Paramos el refresco cuando sales de la pantalla
        handler.removeCallbacks(refrescoPeriodico);
    }

    private void cargarNotificacionesDesdeServidor() {

        int idUsuarioParaPruebas = 23; // 👈 user fijo que dijiste

        NotificacionesManager
                .getInstance(this)
                .refrescarNotificaciones(
                        this,
                        idUsuarioParaPruebas,
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
                                // Aquí si quieres puedes meter un Toast de debug
                            }
                        }
                );
    }
}





