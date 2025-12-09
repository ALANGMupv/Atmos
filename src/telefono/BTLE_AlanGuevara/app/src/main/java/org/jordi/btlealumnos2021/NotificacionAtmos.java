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

    // Identificador único de la notificación en la BBDD
    private int idNotificacion;

    // Campos lógicos de la notificación
    private String tipo;     // O3_CRITICO, SENSOR_INACTIVO, etc.
    private String titulo;
    private String texto;
    private String hora;     // "18:20", "08:05", etc.
    private boolean leida;

    public NotificacionAtmos(int idNotificacion,
                             String tipo,
                             String titulo,
                             String texto,
                             String hora,
                             boolean leida) {
        this.idNotificacion = idNotificacion;
        this.tipo = tipo;
        this.titulo = titulo;
        this.texto = texto;
        this.hora = hora;
        this.leida = leida;
    }

    public int getIdNotificacion() { return idNotificacion; }
    public void setIdNotificacion(int idNotificacion) { this.idNotificacion = idNotificacion; }

    public String getTipo() { return tipo; }
    public String getTitulo() { return titulo; }
    public String getTexto() { return texto; }
    public String getHora() { return hora; }
    public boolean isLeida() { return leida; }

    public void setLeida(boolean leida) { this.leida = leida; }
}
