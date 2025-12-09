package org.jordi.btlealumnos2021;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adaptador oficial de notificaciones para ATMOS.
 * Estructura 100% compatible con tus layouts actuales.
 */
public class NotificacionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER_NUEVAS = 0;
    private static final int VIEW_TYPE_ITEM_NUEVA = 1;
    private static final int VIEW_TYPE_HEADER_LEIDAS = 2;
    private static final int VIEW_TYPE_ITEM_LEIDA = 3;

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

    // --------------------------------------------------------
    // Resolver posición → tipo de vista + índice real
    // --------------------------------------------------------

    private static class PosInfo {
        int viewType;
        boolean esNueva;
        int index;
    }

    private PosInfo resolverPosicion(int position) {
        PosInfo p = new PosInfo();
        int pos = position;

        // ---- NUEVAS ----
        if (!listaNuevas.isEmpty()) {
            if (pos == 0) {
                p.viewType = VIEW_TYPE_HEADER_NUEVAS;
                p.esNueva = true;
                p.index = -1;
                return p;
            }
            pos--;

            if (pos < listaNuevas.size()) {
                p.viewType = VIEW_TYPE_ITEM_NUEVA;
                p.esNueva = true;
                p.index = pos;
                return p;
            }
            pos -= listaNuevas.size();
        }

        // ---- LEÍDAS ----
        if (!listaLeidas.isEmpty()) {
            if (pos == 0) {
                p.viewType = VIEW_TYPE_HEADER_LEIDAS;
                p.esNueva = false;
                p.index = -1;
                return p;
            }
            pos--;

            if (pos < listaLeidas.size()) {
                p.viewType = VIEW_TYPE_ITEM_LEIDA;
                p.esNueva = false;
                p.index = pos;
                return p;
            }
        }

        // fallback
        p.viewType = VIEW_TYPE_ITEM_NUEVA;
        p.esNueva = true;
        p.index = 0;
        return p;
    }

    // --------------------------------------------------------
    // Métodos base del RecyclerView.Adapter
    // --------------------------------------------------------

    @Override
    public int getItemViewType(int position) {
        return resolverPosicion(position).viewType;
    }

    @Override
    public int getItemCount() {
        int count = 0;

        if (!listaNuevas.isEmpty()) {
            count += 1 + listaNuevas.size();
        }
        if (!listaLeidas.isEmpty()) {
            count += 1 + listaLeidas.size();
        }

        return count;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType
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
        PosInfo p = resolverPosicion(position);

        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder hvh = (HeaderViewHolder) holder;
            hvh.tvTituloSeccion.setText(
                    p.viewType == VIEW_TYPE_HEADER_NUEVAS ?
                            "Nuevas alertas" :
                            "Leídas"
            );
            return;
        }

        if (holder instanceof NotiViewHolder) {
            NotificacionAtmos n = p.esNueva ?
                    listaNuevas.get(p.index) :
                    listaLeidas.get(p.index);
            ((NotiViewHolder) holder).bind(n, p.esNueva, p.index);
        }
    }

    // --------------------------------------------------------
    // ViewHolder: HEADER
    // --------------------------------------------------------

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTituloSeccion;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloSeccion = itemView.findViewById(R.id.tvTituloSeccion);
        }
    }

    // --------------------------------------------------------
    // ViewHolder: ITEM DE NOTIFICACIÓN
    // --------------------------------------------------------

    class NotiViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitulo;
        TextView tvTexto;
        TextView tvHora;
        ImageView ivEstado;     // puntito azul (solo nuevas)
        ImageView ivEliminar;   // icono X
        ImageView ivIcono;      // icono de alerta

        NotiViewHolder(@NonNull View itemView) {
            super(itemView);

            // Respeto total a tus IDs originales
            tvTitulo = itemView.findViewById(R.id.tituloNoti);   // EXISTE EN TU XML
            tvTexto = itemView.findViewById(R.id.textoNoti);
            tvHora = itemView.findViewById(R.id.horaNoti);
            ivEstado = itemView.findViewById(R.id.estadoNotis);
            ivEliminar = itemView.findViewById(R.id.btnEliminarNoti);
            ivIcono = itemView.findViewById(R.id.imagenNoti);
        }

        void bind(NotificacionAtmos n, boolean esNueva, int indexEnLista) {

            // ✔ Mostrar título real del backend
            tvTitulo.setText(n.getTitulo());

            // ✔ Mostrar texto principal
            tvTexto.setText(n.getTexto());

            // ✔ Mostrar hora
            tvHora.setText(n.getHora());

            // ✔ Punto de estado solo en nuevas
            ivEstado.setVisibility(esNueva ? View.VISIBLE : View.GONE);

            // ✔ Icono según tipo
            int resId;
            switch (n.getTipo()) {
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
                case "O3_CRITICO":
                default:
                    resId = R.drawable.ic_alerta_co2;
                    break;
            }
            ivIcono.setImageResource(resId);

            // ✔ Click → marcar como leída
            itemView.setOnClickListener(v -> {
                if (listener != null)
                    listener.onNotificacionClick(esNueva, indexEnLista);
            });

            // ✔ Click en eliminar → borrar en backend
            ivEliminar.setOnClickListener(v -> {
                if (listener != null)
                    listener.onDeleteClick(esNueva, indexEnLista);
            });
        }
    }
}
