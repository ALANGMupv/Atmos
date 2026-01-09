// -*- mode: c++ -*-

/**
 * @file Publicador.h
 * @author Jordi Bataller i Mascarell
 * @brief Encargada de enviar (publicar) datos de medidas mediante anuncios BLE iBeacon.
 *
 * La clase Publicador utiliza una instancia de EmisoraBLE para emitir
 * medidas ambientales (CO₂, temperatura, ozono) codificadas en anuncios
 * iBeacon, usando los campos major y minor.
 */

#ifndef PUBLICADOR_H_INCLUIDO
#define PUBLICADOR_H_INCLUIDO

/**
 * @class Publicador
 * @brief Publica medidas ambientales mediante anuncios BLE iBeacon.
 *
 * Gestiona la creación de anuncios iBeacon a partir de valores de sensores
 * y controla el encendido, emisión y parada de la emisora BLE.
 */
class Publicador {

private:

  /**
   * @brief UUID fijo del beacon.
   *
   * Identificador único utilizado para todos los anuncios emitidos
   * por esta placa.
   */
  uint8_t beaconUUID[16] = { 
    'U', 'U', 'I', 'D', '-', 'F', 'I', 'J', 
    'O', '-', '-', '-', 'A', 'L', 'A', 'N'
  };

public:

  /**
   * @brief Emisora BLE utilizada para publicar los anuncios.
   *
   * Configurada con nombre, fabricante Apple (obligatorio para iBeacon)
   * y potencia máxima de transmisión.
   */
  EmisoraBLE laEmisora {
    "AtmosPlacaGrupo4", // nombre emisora
    0x004c,             // fabricanteID (Apple) obligatorio para iBeacon
    4                   // txPower: +4 dBm (potencia máxima)
  };

  /**
   * @brief Valor RSSI de referencia.
   *
   * Se utiliza para estimar distancia en dispositivos receptores.
   * Actualmente es un valor fijo inventado y debería calcularse
   * experimentalmente.
   */
  const int RSSI = -53;

public:

  /**
   * @enum MedicionesID
   * @brief Identificadores de tipo de medición.
   *
   * Se codifican en el campo major del iBeacon junto con un contador.
   */
  enum MedicionesID  {
    CO2 = 11,
    TEMPERATURA = 12,
    OZONO = 13
  };

  /**
   * @brief Constructor por defecto del publicador.
   *
   * No enciende la emisora BLE. Debe llamarse explícitamente a
   * encenderEmisora() desde setup().
   */
  Publicador() {
    // La emisora no debe encenderse aquí
  }

  /**
   * @brief Enciende la emisora BLE.
   */
  void encenderEmisora() {
    (*this).laEmisora.encenderEmisora();
  }

  /**
   * @brief Publica una medición de CO₂ mediante un anuncio iBeacon.
   *
   * Codifica el tipo de medición y un contador en el campo major
   * y el valor de CO₂ en el campo minor.
   *
   * @param valorCO2 Valor de CO₂ a publicar.
   * @param contador Contador de la medición.
   * @param tiempoEspera Tiempo de emisión del anuncio en milisegundos.
   */
  void publicarCO2( int16_t valorCO2, uint8_t contador,
                    long tiempoEspera ) {

    uint16_t major = (MedicionesID::OZONO << 8) + contador;

    (*this).laEmisora.emitirAnuncioIBeacon(
      (*this).beaconUUID,
      major,
      valorCO2,
      (*this).RSSI
    );

    delay( tiempoEspera );
    (*this).laEmisora.detenerAnuncio();
  }

  /**
   * @brief Publica una medición de temperatura mediante un anuncio iBeacon.
   *
   * @param valorTemperatura Valor de temperatura a publicar.
   * @param contador Contador de la medición.
   * @param tiempoEspera Tiempo de emisión del anuncio en milisegundos.
   */
  void publicarTemperatura( int16_t valorTemperatura,
                            uint8_t contador,
                            long tiempoEspera ) {

    uint16_t major = (MedicionesID::TEMPERATURA << 8) + contador;

    (*this).laEmisora.emitirAnuncioIBeacon(
      (*this).beaconUUID,
      major,
      valorTemperatura,
      (*this).RSSI
    );

    delay( tiempoEspera );
    (*this).laEmisora.detenerAnuncio();
  }

}; // class Publicador

#endif
