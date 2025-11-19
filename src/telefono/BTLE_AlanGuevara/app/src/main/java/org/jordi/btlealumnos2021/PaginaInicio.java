package org.jordi.btlealumnos2021;


/**
 * Representa una página del onboarding:
 *  - Imagen
 *  - Título
 *  - Descripción
 */
public class PaginaInicio {

    private final int imagen;
    private final String titulo;
    private final String descripcion;

    public PaginaInicio(int imagen, String titulo, String descripcion) {
        this.imagen = imagen;
        this.titulo = titulo;
        this.descripcion = descripcion;
    }

    public int getImagen() { return imagen; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
}

