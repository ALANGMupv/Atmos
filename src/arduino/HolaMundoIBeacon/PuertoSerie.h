// -*- mode: c++ -*-

/**
 * @file PuertoSerie.h
 * @author Jordi Bataller i Mascarell
 * @date 2019-07-07
 * @brief Abstracción ligera del puerto serie de Arduino.
 *
 * Este fichero define la clase PuertoSerie, que encapsula la inicialización
 * y el uso del puerto serie (Serial) para simplificar la escritura de mensajes
 * y la espera de disponibilidad del puerto.
 */

#ifndef PUERTO_SERIE_H_INCLUIDO
#define PUERTO_SERIE_H_INCLUIDO

/**
 * @class PuertoSerie
 * @brief Encapsula el acceso al puerto serie de Arduino.
 *
 * Proporciona una interfaz sencilla para inicializar el puerto serie,
 * esperar a que esté disponible y escribir mensajes de distintos tipos.
 */
class PuertoSerie  {

public:

  /**
   * @brief Constructor del puerto serie.
   *
   * Inicializa la comunicación serie a la velocidad indicada.
   *
   * @param baudios Velocidad de comunicación en baudios.
   */
  PuertoSerie (long baudios) {
    Serial.begin( baudios );
    // No se espera aquí a Serial disponible para evitar bloqueos tempranos
  }

  /**
   * @brief Espera hasta que el puerto serie esté disponible.
   *
   * Bloquea la ejecución hasta que Serial esté listo.
   */
  void esperarDisponible() {

    while ( !Serial ) {
      delay(10);
    }

  }

  /**
   * @brief Escribe un mensaje por el puerto serie.
   *
   * Permite enviar mensajes de cualquier tipo compatible con Serial.print().
   *
   * @tparam T Tipo del mensaje.
   * @param mensaje Mensaje a enviar.
   */
  template<typename T>
  void escribir (T mensaje) {
    Serial.print( mensaje );
  }

}; // class PuertoSerie

#endif
