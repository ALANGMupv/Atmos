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
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NotificacionesActivity extends AppCompatActivity {

    private RecyclerView recyclerNotificaciones;
    private NotificacionAdapter adapter;
    private ArrayList<NotificacionAtmos> listaNotis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificaciones);

        // Flecha atrás
        ImageView btnBack = findViewById(R.id.btnBackNotificaciones);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        recyclerNotificaciones = findViewById(R.id.recyclerNotificaciones);
        recyclerNotificaciones.setLayoutManager(new LinearLayoutManager(this));

        // Data de prueba (luego la puedes sustituir por datos reales del servidor)
        listaNotis = new ArrayList<>();
        listaNotis.add(new NotificacionAtmos(
                "Nivel crítico en Nodo 3",
                "El nodo 3 ha detectado niveles de NO₂ por encima del umbral configurado.",
                "18:20",
                false
        ));
        listaNotis.add(new NotificacionAtmos(
                "Sensor desconectado",
                "No se reciben datos del Nodo 5 desde hace 15 minutos.",
                "17:55",
                false
        ));
        listaNotis.add(new NotificacionAtmos(
                "Resumen diario",
                "La calidad del aire hoy fue moderada en tu zona.",
                "08:05",
                true
        ));

        adapter = new NotificacionAdapter(listaNotis, new NotificacionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // Marcar como leída al tocar el item
                NotificacionAtmos n = listaNotis.get(position);
                if (!n.isLeida()) {
                    n.setLeida(true);
                    adapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onDeleteClick(int position) {
                // Borrar solo esta notificación
                listaNotis.remove(position);
                adapter.notifyItemRemoved(position);
                // Opcional: actualizar posiciones siguientes
                adapter.notifyItemRangeChanged(position, listaNotis.size() - position);
            }
        });

        recyclerNotificaciones.setAdapter(adapter);

        // Botón de "Eliminar todas" (icono de basura arriba a la derecha)
        ImageView btnBorrarTodas = findViewById(R.id.btnBorrarTodas);
        if (btnBorrarTodas != null) {
            btnBorrarTodas.setOnClickListener(v -> {
                listaNotis.clear();
                adapter.notifyDataSetChanged();
            });
        }
    }
}
