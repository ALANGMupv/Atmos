package org.jordi.btlealumnos2021;

import android.view.View;
import android.widget.Button;

/**
 * @file RecorridoController.java
 * @brief Controlador del panel de recorrido.
 *
 * Gestiona el estado del recorrido y el
 * comportamiento del botón Iniciar / Detener.
 *
 * Esta clase NO conoce Activities ni layouts completos,
 * solo las vistas necesarias.
 *
 * @author Alan Guevara Martínez
 * @date 2025-12-17
 *
 * @note El principal objetivo es no añadir líneas de código en UserPageActivity
 */
public class RecorridoController {

    /** Botón iniciar recorrido */
    private final Button btnIniciar;

    /** Botón detener recorrido */
    private final Button btnDetener;

    /** Estado del recorrido */
    private boolean recorridoActivo = false;

    /**
     * @brief Constructor del controlador.
     *
     * @param btnIniciar  Botón verde "Iniciar"
     * @param btnDetener  Botón rojo "Detener"
     */
    public RecorridoController(Button btnIniciar, Button btnDetener) {
        this.btnIniciar = btnIniciar;
        this.btnDetener = btnDetener;
        inicializar();
    }

    /**
     * @brief Inicializa el estado y listeners.
     */
    private void inicializar() {

        btnDetener.setVisibility(View.GONE);

        btnIniciar.setOnClickListener(v -> iniciar());
        btnDetener.setOnClickListener(v -> detener());
    }

    /**
     * @brief Inicia el recorrido.
     */
    private void iniciar() {
        recorridoActivo = true;
        btnIniciar.setVisibility(View.GONE);
        btnDetener.setVisibility(View.VISIBLE);

        // TODO: lógica real de inicio (GPS, contador, etc.)
    }

    /**
     * @brief Detiene el recorrido.
     */
    private void detener() {
        recorridoActivo = false;
        btnDetener.setVisibility(View.GONE);
        btnIniciar.setVisibility(View.VISIBLE);

        // TODO: lógica real de parada
    }

    /**
     * @brief Indica si el recorrido está activo.
     *
     * @return true si está en curso
     */
    public boolean isRecorridoActivo() {
        return recorridoActivo;
    }
}
