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

public class NotificacionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER_NUEVAS = 0;
    private static final int VIEW_TYPE_ITEM_NUEVA    = 1;
    private static final int VIEW_TYPE_HEADER_LEIDAS = 2;
    private static final int VIEW_TYPE_ITEM_LEIDA    = 3;

    private final List<NotificacionAtmos> listaNuevas;
    private final List<NotificacionAtmos> listaLeidas;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onNotificacionClick(boolean esNueva, int indexEnLista);
        void onDeleteClick(boolean esNueva, int indexEnLista);
    }

    public NotificacionAdapter(List<NotificacionAtmos> listaNuevas,
                               List<NotificacionAtmos> listaLeidas,
                               OnItemClickListener listener) {
        this.listaNuevas = listaNuevas;
        this.listaLeidas = listaLeidas;
        this.listener = listener;
    }

    // ------------------- Helpers para mapear posición → sección -------------------

    private static class PosInfo {
        int viewType;
        boolean esNueva; // solo tiene sentido en items
        int index;       // índice dentro de listaNuevas / listaLeidas, -1 si header
    }

    private PosInfo resolverPosicion(int position) {
        PosInfo p = new PosInfo();
        int pos = position;

        // Bloque de NUEVAS
        if (!listaNuevas.isEmpty()) {
            if (pos == 0) {
                p.viewType = VIEW_TYPE_HEADER_NUEVAS;
                p.esNueva = true;
                p.index = -1;
                return p;
            }
            pos--; // saltamos el header

            if (pos < listaNuevas.size()) {
                p.viewType = VIEW_TYPE_ITEM_NUEVA;
                p.esNueva = true;
                p.index = pos;
                return p;
            }
            pos -= listaNuevas.size();
        }

        // Bloque de LEÍDAS
        if (!listaLeidas.isEmpty()) {
            if (pos == 0) {
                p.viewType = VIEW_TYPE_HEADER_LEIDAS;
                p.esNueva = false;
                p.index = -1;
                return p;
            }
            pos--; // saltamos header

            if (pos < listaLeidas.size()) {
                p.viewType = VIEW_TYPE_ITEM_LEIDA;
                p.esNueva = false;
                p.index = pos;
                return p;
            }
        }

        // Fallback por si acaso (no debería llegar aquí)
        p.viewType = VIEW_TYPE_ITEM_NUEVA;
        p.esNueva = true;
        p.index = 0;
        return p;
    }

    // ------------------- Métodos obligatorios del adapter -------------------

    @Override
    public int getItemViewType(int position) {
        return resolverPosicion(position).viewType;
    }

    @Override
    public int getItemCount() {
        int count = 0;

        if (!listaNuevas.isEmpty()) {
            count += 1 + listaNuevas.size(); // header + items nuevas
        }

        if (!listaLeidas.isEmpty()) {
            count += 1 + listaLeidas.size(); // header + items leídas
        }

        return count;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_HEADER_NUEVAS || viewType == VIEW_TYPE_HEADER_LEIDAS) {
            View v = inflater.inflate(R.layout.item_header_notificacion, parent, false);
            return new HeaderViewHolder(v);
        } else {
            View v = inflater.inflate(R.layout.item_notificacion, parent, false);
            return new NotiViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder,
            int position
    ) {
        PosInfo posInfo = resolverPosicion(position);

        switch (posInfo.viewType) {

            case VIEW_TYPE_HEADER_NUEVAS: {
                HeaderViewHolder hvh = (HeaderViewHolder) holder;
                hvh.tvTituloSeccion.setText("Nuevas alertas");
                break;
            }

            case VIEW_TYPE_HEADER_LEIDAS: {
                HeaderViewHolder hvh = (HeaderViewHolder) holder;
                hvh.tvTituloSeccion.setText("Leídas");
                break;
            }

            case VIEW_TYPE_ITEM_NUEVA: {
                NotificacionAtmos n = listaNuevas.get(posInfo.index);
                ((NotiViewHolder) holder).bind(n, true, posInfo.index);
                break;
            }

            case VIEW_TYPE_ITEM_LEIDA: {
                NotificacionAtmos n = listaLeidas.get(posInfo.index);
                ((NotiViewHolder) holder).bind(n, false, posInfo.index);
                break;
            }
        }
    }

    // ------------------- ViewHolders -------------------

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTituloSeccion;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloSeccion = itemView.findViewById(R.id.tvTituloSeccion);
        }
    }

    class NotiViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitulo;
        TextView tvTexto;
        TextView tvHora;
        ImageView ivEstado;     // puntito
        ImageView ivEliminar;   // X
        ImageView ivIcono;      // 🔹 icono de la notificación

        NotiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo  = itemView.findViewById(R.id.tituloNoti);
            tvTexto   = itemView.findViewById(R.id.textoNoti);
            tvHora    = itemView.findViewById(R.id.horaNoti);
            ivEstado  = itemView.findViewById(R.id.estadoNotis);
            ivEliminar = itemView.findViewById(R.id.btnEliminarNoti);
            ivIcono   = itemView.findViewById(R.id.imagenNoti);
        }

        void bind(NotificacionAtmos n, boolean esNueva, int indexEnLista) {
            tvTitulo.setText(n.getTitulo());
            tvTexto.setText(n.getTexto());
            tvHora.setText(n.getHora());

            // 👀 Puntito solo en nuevas
            if (ivEstado != null) {
                ivEstado.setVisibility(esNueva ? View.VISIBLE : View.GONE);
            }

            // 🎨 ICONO según el tipo
            if (ivIcono != null) {
                int resId;

                switch (n.getTipo()) {
                    case "O3_CRITICO":
                        resId = R.drawable.ic_alerta_co2;   // usa aquí el nombre real de tu drawable
                        break;
                    case "SENSOR_INACTIVO":
                        resId = R.drawable.ic_sensor_off;
                        break;
                    case "LECTURAS_ERRONEAS":
                        resId = R.drawable.ic_warning;
                        break;
                    case "RESUMEN_DIARIO":
                        resId = R.drawable.ic_resumen;
                        break;
                    case "DISTANCIA_SENSOR":
                        resId = R.drawable.ic_distancia;
                        break;
                    default:
                        resId = R.drawable.ic_alerta_co2;
                        break;
                }

                ivIcono.setImageResource(resId);
            }

            // Click en la tarjeta
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificacionClick(esNueva, indexEnLista);
                }
            });

            // Click en la X
            if (ivEliminar != null) {
                ivEliminar.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDeleteClick(esNueva, indexEnLista);
                    }
                });
            }
        }
    }

}


