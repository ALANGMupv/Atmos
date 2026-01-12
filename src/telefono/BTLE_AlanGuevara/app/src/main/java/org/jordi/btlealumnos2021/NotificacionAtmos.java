package org.jordi.btlealumnos2021;
/**
 * @brief Modelo que representa una notificación dentro de ATMOS.
 *
 * Contiene la información necesaria para mostrar notificaciones
 * en la interfaz de usuario, incluyendo título, texto, hora y
 * estado de lectura.
 *
 * Se utiliza principalmente como modelo de datos para el
 * RecyclerView de notificaciones.
 *
 * @author Alejandro Vazquez
 * @date 20/11/2025
 */
public class NotificacionAtmos {

    // Identificador único de la notificación en la BBDD
    private int idNotificacion;

    // Campos lógicos de la notificación
    private String tipo;     // O3_CRITICO, SENSOR_INACTIVO, etc.
    private String titulo;
    private String texto;
    private String hora;     // "18:20", "08:05", etc.
    private long timestamp;  // Para ordenar por fecha (Hoy, Ayer, etc.)
    private boolean leida;

    /**
     * @brief Constructor de una notificación.
     *
     * @param idNotificacion Identificador único de la notificación.
     * @param tipo Tipo lógico de notificación (ej. O3_CRITICO).
     * @param titulo Título visible de la notificación.
     * @param texto Texto descriptivo.
     * @param hora Hora de la notificación en formato legible.
     * @param timestamp Marca temporal para ordenación.
     * @param leida Indica si la notificación ha sido leída.
     */
    public NotificacionAtmos(int idNotificacion,
                             String tipo,
                             String titulo,
                             String texto,
                             String hora,
                             long timestamp,
                             boolean leida) {
        this.idNotificacion = idNotificacion;
        this.tipo = tipo;
        this.titulo = titulo;
        this.texto = texto;
        this.hora = hora;
        this.timestamp = timestamp;
        this.leida = leida;
    }

    public int getIdNotificacion() { return idNotificacion; }
    public void setIdNotificacion(int idNotificacion) { this.idNotificacion = idNotificacion; }

    public String getTipo() { return tipo; }
    public String getTitulo() { return titulo; }
    public String getTexto() { return texto; }
    public String getHora() { return hora; }
    public long getTimestamp() { return timestamp; }
    public boolean isLeida() { return leida; }

    public void setLeida(boolean leida) { this.leida = leida; }
}
