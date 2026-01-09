// -*- mode: c++ -*-

/**
 * @file LED.h
 * @author Jordi Bataller i Mascarell
 * @date 2019-07-07
 * @brief Define la clase LED para el control de un diodo LED en Arduino.
 *
 * Este fichero encapsula el control básico de un LED conectado a una placa
 * Arduino (o compatible), permitiendo encenderlo, apagarlo, alternar su estado
 * y hacerlo brillar durante un tiempo determinado.
 */

#ifndef LED_H_INCLUIDO
#define LED_H_INCLUIDO

/**
 * @brief Detiene la ejecución durante un tiempo determinado.
 *
 * Esta función no es exclusiva del LED, sino una utilidad general que
 * encapsula la llamada a delay().
 *
 * @param tiempo Tiempo de espera en milisegundos.
 */
void esperar (long tiempo) {
  delay (tiempo);
}

/**
 * @class LED
 * @brief Encapsula el control de un diodo LED.
 *
 * Permite gestionar un LED conectado a un pin digital de Arduino,
 * controlando su encendido, apagado, alternancia y brillo temporal.
 */
class LED {
private:
  /// Pin digital al que está conectado el LED
  int numeroLED;

  /// Estado actual del LED (encendido o apagado)
  bool encendido;

public:

  /**
   * @brief Constructor de la clase LED.
   *
   * Inicializa el pin como salida y apaga el LED por defecto.
   *
   * @param numero Número del pin digital al que está conectado el LED.
   */
  LED (int numero)
    : numeroLED (numero), encendido(false)
  {
    pinMode(numeroLED, OUTPUT);
    apagar ();
  }

  /**
   * @brief Enciende el LED.
   */
  void encender () {
    digitalWrite(numeroLED, HIGH);
    encendido = true;
  }

  /**
   * @brief Apaga el LED.
   */
  void apagar () {
    digitalWrite(numeroLED, LOW);
    encendido = false;
  }

  /**
   * @brief Alterna el estado del LED.
   *
   * Si el LED está encendido, lo apaga.  
   * Si está apagado, lo enciende.
   */
  void alternar () {
    if (encendido) {
      apagar();
    } else {
      encender ();
    }
  }

  /**
   * @brief Hace brillar el LED durante un tiempo determinado.
   *
   * Enciende el LED, espera el tiempo indicado y lo apaga.
   *
   * @param tiempo Tiempo en milisegundos que el LED permanecerá encendido.
   */
  void brillar (long tiempo) {
    encender ();
    esperar(tiempo);
    apagar ();
  }

}; // class LED

#endif
