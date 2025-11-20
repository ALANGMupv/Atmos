package org.jordi.btlealumnos2021;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

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


public class NotificacionesActivity extends AppCompatActivity {

    private RecyclerView recyclerNotificaciones;
    private NotificacionAdapter adapter;
    private ArrayList<NotificacionAtmos> listaNotis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificaciones);

        recyclerNotificaciones = findViewById(R.id.recyclerNotificaciones);
        recyclerNotificaciones.setLayoutManager(new LinearLayoutManager(this));

        // 1) Crear data de prueba
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

        // 2) Crear adapter
        adapter = new NotificacionAdapter(listaNotis, position -> {
            // Al tocar, marcar como leída
            NotificacionAtmos n = listaNotis.get(position);
            n.setLeida(true);
            adapter.notifyItemChanged(position);
        });

        //back de el header
        ImageView btnBack = findViewById(R.id.btnBackNotificaciones);
        btnBack.setOnClickListener(v -> finish());


        recyclerNotificaciones.setAdapter(adapter);
    }
}
