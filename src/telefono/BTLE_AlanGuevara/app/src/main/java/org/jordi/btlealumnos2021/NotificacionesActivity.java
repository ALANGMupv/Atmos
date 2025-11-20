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
    private ArrayList<NotificacionAtmos> listaNotis;

    // 🔁 Handler para refresco periódico
    private Handler handler = new Handler(Looper.getMainLooper());
    private static final long INTERVALO_REFRESH_MS = 5_000; // 5 segundos

    // Runnable que se ejecuta cada X tiempo
    private final Runnable refrescoPeriodico = new Runnable() {
        @Override
        public void run() {
            cargarNotificacionesDesdeServidor();
            // Programamos la siguiente ejecución
            handler.postDelayed(this, INTERVALO_REFRESH_MS);
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

        // RecyclerView
        recyclerNotificaciones = findViewById(R.id.recyclerNotificaciones);
        recyclerNotificaciones.setLayoutManager(new LinearLayoutManager(this));

        // Lista local + adapter
        listaNotis = new ArrayList<>();
        adapter = new NotificacionAdapter(listaNotis, new NotificacionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // Marcar como leída al tocar la tarjeta
                NotificacionAtmos n = listaNotis.get(position);
                if (!n.isLeida()) {
                    n.setLeida(true);
                    adapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onDeleteClick(int position) {
                // Borrar solo esta notificación (localmente)
                if (position >= 0 && position < listaNotis.size()) {
                    listaNotis.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, listaNotis.size() - position);
                }
            }
        });

        recyclerNotificaciones.setAdapter(adapter);

        // Botón "Eliminar todas" (icono de basura en el header de la lista)
        ImageView btnBorrarTodas = findViewById(R.id.btnBorrarTodas);
        if (btnBorrarTodas != null) {
            btnBorrarTodas.setOnClickListener(v -> {
                listaNotis.clear();
                adapter.notifyDataSetChanged();
                // Solo borra en la app, no en el backend.
            });
        }

        // Carga inicial
        cargarNotificacionesDesdeServidor();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cada vez que vuelves a esta pantalla:
        cargarNotificacionesDesdeServidor();
        // 🔁 Empieza refresco periódico
        handler.postDelayed(refrescoPeriodico, INTERVALO_REFRESH_MS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 🛑 Detener refresco cuando sales de la pantalla
        handler.removeCallbacks(refrescoPeriodico);
    }

    private void cargarNotificacionesDesdeServidor() {

        // MODO PRUEBA → usuario 23
        int idUsuarioParaPruebas = 23;

        NotificacionesManager
                .getInstance(this)
                .refrescarNotificaciones(
                        this,
                        idUsuarioParaPruebas,
                        new NotificacionesManager.Listener() {
                            @Override
                            public void onResultado(List<NotificacionAtmos> lista, boolean hayAlgoNuevo) {
                                listaNotis.clear();
                                listaNotis.addAll(lista);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(String mensaje) {
                                // Si quieres debug visual:
                                // Toast.makeText(NotificacionesActivity.this, mensaje, Toast.LENGTH_SHORT).show();
                            }
                        }
                );
    }
}



