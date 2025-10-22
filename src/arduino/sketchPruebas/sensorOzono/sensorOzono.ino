#include <bluefruit.h>  // Librería base para el nRF52840

// =====================================================
//     Sensor de Ozono SPEC Sensors O3 (modo laboratorio)
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

// Variables de corrección (TOCARÁ SUSTITUIRLO TRAS EL LABORATORIO)
float a = 1.0;  // Factor de ganancia experimental (pendiente)
float b = 0.0;  // Offset experimental (intersección)

// Variables de medición
float Vgas, Vref, deltaV, Cx, Cx_corregido;

void setup() {
  Serial.begin(115200);
  delay(2000);
  Serial.println("=== MODO LABORATORIO: Sensor de Ozono SPEC Sensors O3 ===");
  Serial.println("Usando fórmula sin calibrar: Cx = (Vgas - Vref) / M");
  Serial.println("Listo para tomar datos experimentales.\n");

  pinMode(pinVgas, INPUT);
  pinMode(pinVref, INPUT);
}

void loop() {
  // Leer voltajes analógicos
  Vgas = leerVoltaje(pinVgas);
  Vref = leerVoltaje(pinVref);

  // Calcular diferencia y concentración
  deltaV = Vgas - Vref;
  Cx = deltaV / M;             // Fórmula base teórica
  Cx_corregido = a * Cx + b;   // Corrección (pendiente y offset)

  // Mostrar datos
  Serial.println("----- Lectura de sensor -----");
  Serial.print("Vgas = "); Serial.print(Vgas, 4); Serial.println(" V");
  Serial.print("Vref = "); Serial.print(Vref, 4); Serial.println(" V");
  Serial.print("DeltaV = "); Serial.print(deltaV, 6); Serial.println(" V");
  Serial.print("M (V/ppm) = "); Serial.println(M, 6);
  Serial.print("Cx base = "); Serial.print(Cx, 3); Serial.println(" ppm");
  Serial.print("Cx ¡corregido' = "); Serial.print(fabs(Cx_corregido), 3); Serial.println(" ppm"); // Aunque de negativo, realmente el valor es positivo
  // Físicamente, el sensor produce una corriente negativa cuando aumenta la concentración de ozono.
  Serial.println("-----------------------------\n");

  delay(2000);  // Medición cada 2 segundos
}

// Función para leer voltaje desde el ADC
float leerVoltaje(int pin) {
  int codigo = analogRead(pin);
  float voltaje = (codigo * Vcc) / (resolucionADC - 1);
  return voltaje;
}
