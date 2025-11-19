package org.jordi.btlealumnos2021;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

// Adaptador para ViewPager2 que muestra las páginas del onboarding.

public class AdaptadorPaginasInicio extends RecyclerView.Adapter<AdaptadorPaginasInicio.ViewHolderPagina> {

    private final List<PaginaInicio> paginas;

    public AdaptadorPaginasInicio(List<PaginaInicio> paginas) {
        this.paginas = paginas;
    }

    @NonNull
    @Override
    public ViewHolderPagina onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflamos el layout
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pagina_inicio, parent, false);
        return new ViewHolderPagina(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderPagina holder, int position) {
        PaginaInicio pagina = paginas.get(position);

        holder.imagenPagina.setImageResource(pagina.getImagen());
        holder.textoTitulo.setText(pagina.getTitulo());
        holder.textoDescripcion.setText(pagina.getDescripcion());
    }

    @Override
    public int getItemCount() {
        return paginas.size();
    }

    static class ViewHolderPagina extends RecyclerView.ViewHolder {

        ImageView imagenPagina;
        TextView textoTitulo;
        TextView textoDescripcion;

        public ViewHolderPagina(@NonNull View itemView) {
            super(itemView);

            imagenPagina = itemView.findViewById(R.id.imagenPagina);
            textoTitulo = itemView.findViewById(R.id.textoTitulo);
            textoDescripcion = itemView.findViewById(R.id.textoDescripcion);
        }
    }
}

