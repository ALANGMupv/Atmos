package org.jordi.btlealumnos2021;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Adaptador oficial de notificaciones para ATMOS.
 * Estructura 100% compatible con tus layouts actuales.
 */
public class NotificacionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM_NUEVA = 1;
    private static final int TYPE_ITEM_LEIDA = 2;

    // Estructura interna para aplanar la lista
    private final List<RowItem> rows = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onNotificacionClick(boolean esNueva, int idNotificacion);
        void onDeleteClick(boolean esNueva, int idNotificacion);
    }

    // Constructor vacío inicial
    public NotificacionAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    /// ------------------- Helpers para mapear posición → sección -------------------

    private static class PosInfo {
        int viewType;
        boolean esNueva; // solo tiene sentido en items
        int index;       // índice dentro de listaNuevas / listaLeidas, -1 si header
    }

        // 1. SECCIÓN: NUEVAS
        if (!nuevas.isEmpty()) {
            rows.add(new RowItem(TYPE_HEADER, "Nuevas alertas", null));
            for (NotificacionAtmos n : nuevas) {
                rows.add(new RowItem(TYPE_ITEM_NUEVA, null, n));
            }
        }

        // 2. SECCIÓN: LEÍDAS (Con sub-secciones de fecha)
        if (!leidas.isEmpty()) {
            // Ordenamos por timestamp descendente
            Collections.sort(leidas, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

            // Agrugamos por fecha
            Map<String, List<NotificacionAtmos>> grupos = new LinkedHashMap<>();
            
            for (NotificacionAtmos n : leidas) {
                String label = getFechaLabel(n.getTimestamp());
                if (!grupos.containsKey(label)) {
                    grupos.put(label, new ArrayList<>());
                }
                grupos.get(label).add(n);
            }

            // Insertamos en la lista visual
            for (Map.Entry<String, List<NotificacionAtmos>> entry : grupos.entrySet()) {
                // Header de fecha (Ej: "Hoy", "Ayer")
                rows.add(new RowItem(TYPE_HEADER, entry.getKey(), null));
                
                for (NotificacionAtmos n : entry.getValue()) {
                    rows.add(new RowItem(TYPE_ITEM_LEIDA, null, n));
                }
            }
        }

        notifyDataSetChanged();
    }

    private String getFechaLabel(long timestamp) {
        if (timestamp == 0) return "Antiguas";

        Calendar cal = Calendar.getInstance();
        Calendar notiCal = Calendar.getInstance();
        notiCal.setTimeInMillis(timestamp);

        if (esMismoDia(cal, notiCal)) return "Hoy";
        
        cal.add(Calendar.DAY_OF_YEAR, -1);
        if (esMismoDia(cal, notiCal)) return "Ayer";

        return "Antiguas";
    }

    private boolean esMismoDia(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
               a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    // --------------------------------------------------------
    // Métodos RecyclerView
    // --------------------------------------------------------

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).type;
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View v = inflater.inflate(R.layout.item_header_notificacion, parent, false);
            return new HeaderViewHolder(v);
        } else {
            View v = inflater.inflate(R.layout.item_notificacion, parent, false);
            return new NotiViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RowItem item = rows.get(position);

        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).tvTituloSeccion.setText(item.headerTitle);
        } else if (holder instanceof NotiViewHolder) {
            NotificacionAtmos n = item.notificacion;
            boolean esNueva = (item.type == TYPE_ITEM_NUEVA);
            ((NotiViewHolder) holder).bind(n, esNueva);
        }
    }

    // --------------------------------------------------------
    // ViewHolders & Inner Classes
    // --------------------------------------------------------

    private static class RowItem {
        int type;
        String headerTitle;
        NotificacionAtmos notificacion;

        RowItem(int type, String headerTitle, NotificacionAtmos notificacion) {
            this.type = type;
            this.headerTitle = headerTitle;
            this.notificacion = notificacion;
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTituloSeccion;
        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloSeccion = itemView.findViewById(R.id.tvTituloSeccion);
        }
    }

    class NotiViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvTexto, tvHora;
        ImageView ivEstado, ivEliminar, ivIcono;

        NotiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tituloNoti);
            tvTexto = itemView.findViewById(R.id.textoNoti);
            tvHora = itemView.findViewById(R.id.horaNoti);
            ivEstado = itemView.findViewById(R.id.estadoNotis);
            ivEliminar = itemView.findViewById(R.id.btnEliminarNoti);
            ivIcono = itemView.findViewById(R.id.imagenNoti);
        }

        void bind(NotificacionAtmos n, boolean esNueva) {
            tvTitulo.setText(n.getTitulo());
            tvTexto.setText(n.getTexto());
            tvHora.setText(n.getHora());
            ivEstado.setVisibility(esNueva ? View.VISIBLE : View.GONE);

            int resId;
            switch (n.getTipo()) {
                case "SENSOR_INACTIVO": resId = R.drawable.ic_sensor_off; break;
                case "LECTURAS_ERRONEAS": resId = R.drawable.ic_warning; break;
                case "RESUMEN_DIARIO": resId = R.drawable.ic_resumen; break;
                case "DISTANCIA_SENSOR": resId = R.drawable.ic_distancia; break;
                case "O3_CRITICO": default: resId = R.drawable.ic_alerta_co2; break;
            }
            ivIcono.setImageResource(resId);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onNotificacionClick(esNueva, n.getIdNotificacion());
            });

            ivEliminar.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(esNueva, n.getIdNotificacion());
            });
        }
    }
}
