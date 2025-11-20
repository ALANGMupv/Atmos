/**
 * Nombre Fichero: NotificacionAdapter.java
 * Descripción: Adaptador del RecyclerView encargado de vincular
 *              cada objeto NotificacionAtmos con su vista dentro
 *              del archivo item_notificacion.xml.
 *
 *              Gestiona la presentación de cada notificación,
 *              incluyendo icono, título, descripción, hora y el
 *              indicador visual de “sin leer”. También permite
 *              manejar eventos de clic para actualizar el estado
 *              de lectura.
 *
 * Autor: Alejandro Vazquez Remes
 * Fecha: 20/11/2025
 */
package org.jordi.btlealumnos2021;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificacionAdapter extends RecyclerView.Adapter<NotificacionAdapter.NotiViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(int position);     // tocar la tarjeta → marcar leída
        void onDeleteClick(int position);   // tocar la X → borrar
    }

    private final List<NotificacionAtmos> notificaciones;
    private final OnItemClickListener listener;

    public NotificacionAdapter(List<NotificacionAtmos> notificaciones, OnItemClickListener listener) {
        this.notificaciones = notificaciones;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notificacion, parent, false);
        return new NotiViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull NotiViewHolder holder, int position) {
        NotificacionAtmos n = notificaciones.get(position);

        holder.tituloNoti.setText(n.getTitulo());
        holder.textoNoti.setText(n.getTexto());
        holder.horaNoti.setText(n.getHora());

        // Puntito verde: visible si NO está leída
        if (n.isLeida()) {
            holder.estadoNotis.setVisibility(View.INVISIBLE);
        } else {
            holder.estadoNotis.setVisibility(View.VISIBLE);
        }

        // Icono según el tipo de notificación
        holder.imagenNoti.setImageResource(obtenerIconoPorTipo(n.getTipo()));

        // Tocar la tarjeta → marcar como leída
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                listener.onItemClick(holder.getAdapterPosition());
            }
        });

        // Tocar la X → borrar solo esta notificación
        holder.btnEliminarNoti.setOnClickListener(v -> {
            if (listener != null && holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                listener.onDeleteClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificaciones.size();
    }

    // Mapea el tipo (string) al drawable correspondiente
    private int obtenerIconoPorTipo(String tipo) {
        if (tipo == null) {
            return R.drawable.ic_alerta_generica;
        }

        switch (tipo) {
            case "CO2_CRITICO":
                return R.drawable.ic_alerta_co2;
            case "SENSOR_INACTIVO":
                return R.drawable.ic_sensor_off;
            case "LECTURAS_ERRONEAS":
                return R.drawable.ic_warning;
            case "RESUMEN_DIARIO":
                return R.drawable.ic_resumen;
            case "DISTANCIA_SENSOR":
                return R.drawable.ic_distancia;
            default:
                return R.drawable.ic_alerta_generica;
        }
    }

    public static class NotiViewHolder extends RecyclerView.ViewHolder {

        ImageView imagenNoti;
        TextView tituloNoti;
        TextView textoNoti;
        TextView horaNoti;
        ImageView estadoNotis;
        ImageView btnEliminarNoti;

        public NotiViewHolder(@NonNull View itemView) {
            super(itemView);
            imagenNoti     = itemView.findViewById(R.id.imagenNoti);
            tituloNoti     = itemView.findViewById(R.id.tituloNoti);
            textoNoti      = itemView.findViewById(R.id.textoNoti);
            horaNoti       = itemView.findViewById(R.id.horaNoti);
            estadoNotis    = itemView.findViewById(R.id.estadoNotis);
            btnEliminarNoti = itemView.findViewById(R.id.btnEliminarNoti);
        }
    }
}

