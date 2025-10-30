// -*- mode: c++ -*-
// Medidor.h: La clase proporciona métodos para inicializar el medidor y devolver valores de medidas.
// Actualmente devuelve valores fijos (666 para CO₂ y –12 para temperatura), funcionando como una simulación de un sensor real.
#ifndef MEDIDOR_H_INCLUIDO
#define MEDIDOR_H_INCLUIDO

// ------------------------------------------------------
// ------------------------------------------------------
class Medidor {

  // .....................................................
  // .....................................................
private:

// =====================================================
//     Variables Sensor de Ozono SPEC Sensors O3 (modo laboratorio)
//     Lectura con Vref, para recalibrar contra sensor patrón
// =====================================================

const int pinVgas = A4;   // VGAS conectado al pin físico 28 (P0.28)
const int pinVref = A5;   // VREF conectado al pin físico 29 (P0.29)

// Parámetros del ADC y hardware
const float Vcc = 3.3;           // Voltaje de referencia ADC
const int resolucionADC = 4096;  // 12 bits -> 0 a 4095

// Parámetros de calibración base del sensor
const float sensibilidad = -42.31;  // nA/ppm (según etiqueta del sensor)
const float gananciaTIA = 499.0;    // kV/A (según manual)
const float M = sensibilidad * gananciaTIA * 1e-6;  // V/ppm

// Variables de corrección 
float a = 1.143019508; // Factor de ganancia experimental (pendiente)
float b = -0.175050042;  // Offset experimental (intersección)


// =====================================================
// =====================================================

public:

  // .....................................................
  // constructor
  // .....................................................
  Medidor(  ) {
  } // ()

  // .....................................................
  // .....................................................
  void iniciarMedidor() {
// Aquí configuramos los pine
  pinMode(pinVgas, INPUT);
  pinMode(pinVref, INPUT);
  } // ()

  // .....................................................
  // .....................................................
  int medirCO2() {
	  float Vgas = leerVoltaje(pinVgas);
    float Vref = leerVoltaje(pinVref);
    float deltaV = Vgas - Vref;
    float Cx = deltaV / M;
    float Cx_corregido = (Cx - b) / a;

    // El sensor da valores negativos: usar valor absoluto ('fabs')
    int ppm = (int)(fabs(Cx_corregido) * 1000); // Escalado x1000, ya que beacon no admite decimales, luego lo dividimos por 1000.

    Serial.print("Vgas: "); Serial.print(Vgas, 4);
    Serial.print("  Vref: "); Serial.print(Vref, 4);
    Serial.print("  ΔV: "); Serial.print(deltaV, 6);
    Serial.print("  ppm: "); Serial.println(ppm);

  return ppm;
  } // ()

  // .....................................................
  // .....................................................
  int medirTemperatura() {
	return -12; // qué frío !
  } // ()

// Función para leer voltaje desde el ADC
float leerVoltaje(int pin) {
  int codigo = analogRead(pin);
  float voltaje = (codigo * Vcc) / (resolucionADC - 1);
  return voltaje;
}

	
}; // class

// ------------------------------------------------------
// ------------------------------------------------------
// ------------------------------------------------------
// ------------------------------------------------------
#endif
