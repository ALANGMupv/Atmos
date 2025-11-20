package org.jordi.btlealumnos2021;
/**
 * Nombre Fichero: NotificacionAtmos.java
 * Descripción: Clase modelo que representa una notificación dentro
 *              del ecosistema ATMOS. Contiene la estructura base
 *              utilizada por el RecyclerView: título, texto,
 *              hora y estado de lectura.
 *
 *              Esta clase es utilizada para generar, almacenar y
 *              manipular notificaciones locales, así como para
 *              mostrar su información en la interfaz gráfica.
 *
 * Autor: Alejandro Vazquez
 * Fecha: 20/11/2025
 */


public class NotificacionAtmos {

    private String titulo;
    private String texto;
    private String hora;
    private boolean leida;

    public NotificacionAtmos(String titulo, String texto, String hora, boolean leida) {
        this.titulo = titulo;
        this.texto = texto;
        this.hora = hora;
        this.leida = leida;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getTexto() {
        return texto;
    }

    public String getHora() {
        return hora;
    }

    public boolean isLeida() {
        return leida;
    }

    public void setLeida(boolean leida) {
        this.leida = leida;
    }
}
