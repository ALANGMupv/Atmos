package org.jordi.btlealumnos2021;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @file IncidenciasAdapter.java
 * @brief Adapter para mostrar la lista de incidencias del usuario.
 *
 * Se encarga de:
 *  - Mostrar título, descripción y fecha formateada
 *  - Mostrar el estado de la incidencia (Recibida / En proceso / Resuelta / Rechazada)
 *  - Mostrar u ocultar el punto rojo según si la respuesta está leída por el usuario
 *
 * @author Nerea Aguilar Forés
 * @date 29/12/2025
 */
public class IncidenciasAdapter extends RecyclerView.Adapter<IncidenciasAdapter.ViewHolder> {

    /** Lista de incidencias a mostrar */
    private final List<Incidencia> lista;

    /** Listener de click sobre cada tarjeta */
    private final View.OnClickListener listener;

    /** Indica si esta pestaña debe mostrar punto rojo (RESPUESTAS sí, ENVIADAS no) */
    private final boolean mostrarPuntoRojo;

    /**
     * @brief Constructor del adapter de incidencias.
     *
     * @param lista Lista de incidencias
     * @param listener Listener al pulsar una incidencia
     * @param mostrarPuntoRojo true si se debe mostrar punto rojo (RESPUESTAS)
     */
    public IncidenciasAdapter(
            List<Incidencia> lista,
            View.OnClickListener listener,
            boolean mostrarPuntoRojo
    ) {
        this.lista = lista;
        this.listener = listener;
        this.mostrarPuntoRojo = mostrarPuntoRojo;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_incidencia, parent, false);
        v.setOnClickListener(listener);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Incidencia i = lista.get(position);

        /* ------------------ TEXTO PRINCIPAL ------------------ */
        holder.titulo.setText(i.asunto);
        holder.descripcion.setText(i.mensaje);

        /* ------------------ FECHA FORMATEADA ------------------ */
        holder.fecha.setText(formatearFechaUsuario(i.fecha));

        /* ------------------ ESTADO DE LA INCIDENCIA ------------------ */
        holder.estado.setText(getTextoEstado(i.idEstado));

        /* ------------------ PUNTO ROJO (LEÍDO / NO LEÍDO) ------------------
         * Regla acordada:
         *  - En ENVIADAS: NUNCA se muestra
         *  - En RESPUESTAS: se muestra SOLO si hay respuesta y no está leída por el usuario
         */

        if (!mostrarPuntoRojo) {
            holder.puntoRojo.setVisibility(View.GONE);
        } else {
            boolean hayRespuesta =
                    i.respuesta != null &&
                            !i.respuesta.trim().isEmpty() &&
                            !"null".equalsIgnoreCase(i.respuesta.trim());

            boolean noLeida = (i.leidaUsuario == 0);

            holder.puntoRojo.setVisibility(
                    hayRespuesta && noLeida ? View.VISIBLE : View.GONE
            );
        }

    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    /**
     * @brief ViewHolder para una tarjeta de incidencia.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView titulo;
        TextView descripcion;
        TextView fecha;
        TextView estado;
        View puntoRojo;

        ViewHolder(View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.txtTitulo);
            descripcion = itemView.findViewById(R.id.txtDescripcion);
            fecha = itemView.findViewById(R.id.txtFecha);
            estado = itemView.findViewById(R.id.txtEstado);
            puntoRojo = itemView.findViewById(R.id.puntoRojo);
        }
    }

    /* =========================================================
     * MÉTODOS AUXILIARES PRIVADOS (SIN CLASES EXTERNAS)
     * ========================================================= */

    /**
     * @brief Convierte una fecha ISO 8601 UTC a formato legible para el usuario.
     *
     * Ejemplo entrada: 2025-12-29T01:12:24.000Z
     * Ejemplo salida: 29/12/2025 · 02:12
     *
     * @param fechaIso Fecha recibida del backend
     * @return Fecha formateada para UI
     */
    private String formatearFechaUsuario(String fechaIso) {
        try {
            SimpleDateFormat isoFormat =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date date = isoFormat.parse(fechaIso);

            SimpleDateFormat userFormat =
                    new SimpleDateFormat("dd/MM/yyyy · HH:mm", Locale.getDefault());

            return userFormat.format(date);

        } catch (Exception e) {
            return fechaIso; // fallback
        }
    }

    /**
     * @brief Devuelve el texto de estado visible para el usuario.
     *
     * @param idEstado Estado numérico desde backend
     * @return Texto de estado
     */
    private String getTextoEstado(int idEstado) {
        switch (idEstado) {
            case 1: return "Recibida";
            case 2: return "En proceso";
            case 3: return "Resuelta";
            case 4: return "Rechazada";
            default: return "Recibida";
        }
    }
}
