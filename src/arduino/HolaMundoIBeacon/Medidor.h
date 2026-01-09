// -*- mode: c++ -*-

/**
 * @file Medidor.h
 * @brief Proporciona métodos para inicializar un medidor y obtener valores de medición.
 *
 * La clase Medidor encapsula la lógica de lectura de sensores analógicos.
 * Actualmente implementa la lectura de un sensor de gas (modo laboratorio)
 * y una medición simulada de temperatura.
 *
 * En versiones iniciales, el sensor funcionaba como simulación devolviendo
 * valores fijos. En esta versión se incorpora lectura analógica real y
 * calibración experimental.
 */

/**
 * @details
 * Se modificó el código para mejorar la autonomía de la batería.
 * Se comentaron algunas trazas por puerto serie.
 *
 * @author Modificado por Alan Guevara Martínez
 * @date 20/12/2025
 */

#ifndef MEDIDOR_H_INCLUIDO
#define MEDIDOR_H_INCLUIDO

/**
 * @class Medidor
 * @brief Gestiona la inicialización y lectura de sensores ambientales.
 *
 * Permite inicializar los pines del sensor y obtener valores de concentración
 * de gas (CO₂/O₃ en modo laboratorio) y temperatura.
 */
class Medidor {

private:

  // =====================================================
  // Sensor de Ozono SPEC Sensors O3 (modo laboratorio)
  // Lectura diferencial con Vref para recalibración
  // =====================================================

  /// Pin analógico de lectura del gas (VGAS)
  const int pinVgas = A4;

  /// Pin analógico de referencia (VREF)
  const int pinVref = A5;

  // -----------------------------------------------------
  // Parámetros del ADC y hardware
  // -----------------------------------------------------

  /// Voltaje de referencia del ADC
  const float Vcc = 3.3;

  /// Resolución del ADC (12 bits: 0–4095)
  const int resolucionADC = 4096;

  // -----------------------------------------------------
  // Parámetros de calibración base del sensor
  // -----------------------------------------------------

  /// Sensibilidad del sensor (nA/ppm)
  const float sensibilidad = -42.31;

  /// Ganancia del amplificador transimpedancia (kV/A)
  const float gananciaTIA = 499.0;

  /// Factor de conversión voltaje/ppm
  const float M = sensibilidad * gananciaTIA * 1e-6;

  // -----------------------------------------------------
  // Variables de corrección experimental
  // -----------------------------------------------------

  /// Factor de ganancia experimental (pendiente)
  float a = 1.143019508;

  /// Offset experimental (intersección)
  float b = -0.175050042;

public:

  /**
   * @brief Constructor por defecto del medidor.
   *
   * No inicializa los pines; debe llamarse a iniciarMedidor().
   */
  Medidor() {
  }

  /**
   * @brief Inicializa el medidor configurando los pines del sensor.
   */
  void iniciarMedidor() {
    pinMode(pinVgas, INPUT);
    pinMode(pinVref, INPUT);
  }

  /**
   * @brief Mide la concentración de gas (CO₂/O₃) en ppm.
   *
   * Realiza una lectura diferencial entre VGAS y VREF,
   * aplica calibración experimental y devuelve el valor
   * escalado en partes por millón (ppm).
   *
   * @return Valor de concentración del gas en ppm.
   */
  int medirCO2() {

    float Vgas = leerVoltaje(pinVgas);
    float Vref = leerVoltaje(pinVref);
    float deltaV = Vgas - Vref;
    float Cx = deltaV / M;
    float Cx_corregido = (Cx - b) / a;

    // El sensor devuelve valores negativos; se usa valor absoluto
    int ppm = (int)(fabs(Cx_corregido) * 1000);

    Serial.print("  ppm: ");
    Serial.println(ppm);

    return ppm;
  }

  /**
   * @brief Devuelve la temperatura medida.
   *
   * Actualmente devuelve un valor fijo como simulación.
   *
   * @return Temperatura simulada en grados.
   */
  int medirTemperatura() {
    return -12;
  }

  /**
   * @brief Lee el voltaje de un pin analógico.
   *
   * Convierte el valor ADC en voltaje real usando la referencia
   * y resolución configuradas.
   *
   * @param pin Pin analógico a leer.
   * @return Voltaje leído en voltios.
   */
  float leerVoltaje(int pin) {
    int codigo = analogRead(pin);
    float voltaje = (codigo * Vcc) / (resolucionADC - 1);
    return voltaje;
  }

}; // class Medidor

#endif
