package org.jordi.btlealumnos2021;

/**
 * @brief Modelo que representa una incidencia del usuario.
 *
 * Contiene la información básica de una incidencia enviada
 * por el usuario desde la aplicación Android.
 *
 * @author Nerea Aguilar Forés
 */
public class Incidencia {

    /** Identificador de la incidencia */
    public int idIncidencia;

    /** Asunto o título de la incidencia */
    public String asunto;

    /** Mensaje enviado por el usuario */
    public String mensaje;

    /** Respuesta del administrador (puede ser null) */
    public String respuesta;

    /** Fecha de creación de la incidencia */
    public String fecha;

    /** Estado de la incidencia */
    public int idEstado;

    /** Indica si el usuario ha leído la respuesta */
    public int leidaUsuario;

    public Incidencia(
            int idIncidencia,
            String asunto,
            String mensaje,
            String respuesta,
            String fecha,
            int idEstado,
            int leidaUsuario
    ) {
        this.idIncidencia = idIncidencia;
        this.asunto = asunto;
        this.mensaje = mensaje;
        this.respuesta = respuesta;
        this.fecha = fecha;
        this.idEstado = idEstado;
        this.leidaUsuario = leidaUsuario;

    }
}

