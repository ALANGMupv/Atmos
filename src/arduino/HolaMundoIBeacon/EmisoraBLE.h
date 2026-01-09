// -*- mode: c++ -*-

/**
 * @file EmisoraBLE.h
 * @author Jordi Bataller i Mascarell
 * @date 2019-07-07
 * @brief Encapsula toda la lógica de configuración, arranque, anuncio y gestión
 *        de servicios y callbacks de una emisora Bluetooth Low Energy (BLE).
 *
 * Este fichero define la clase EmisoraBLE, encargada de inicializar la emisora BLE,
 * emitir anuncios iBeacon, gestionar servicios BLE y manejar callbacks de conexión
 * y desconexión.
 *
 * @note Se incluyen referencias y ejemplos externos sobre BLE e iBeacon.
 *
 * Fuentes:
 * - https://learn.adafruit.com/introduction-to-bluetooth-low-energy/gap
 * - https://os.mbed.com/blog/entry/BLE-Beacons-URIBeacon-AltBeacons-iBeacon/
 * - https://www.instructables.com/id/Beaconeddystone-and-Adafruit-NRF52-Advertise-Your-/
 * - https://github.com/nkolban/ESP32_BLE_Arduino/blob/master/src/BLEBeacon.h
 * - https://learn.adafruit.com/bluefruit-nrf52-feather-learning-guide/bleadvertising
 */

/**
 * @details
 * Se modificó el código para mejorar la autonomía de la batería.
 * Se aumentó el tiempo de setInterval para reducir el número de anuncios por segundo.
 *
 * @author Modificado por Alan Guevara Martínez
 * @date 20/12/2025
 */

#ifndef EMISORA_H_INCLUIDO
#define EMISORA_H_INCLUIDO

#include "ServicioEnEmisora.h"

/**
 * @class EmisoraBLE
 * @brief Gestiona una emisora Bluetooth Low Energy (BLE).
 *
 * Esta clase permite:
 * - Inicializar la emisora BLE.
 * - Emitir anuncios iBeacon estándar o con carga libre.
 * - Añadir servicios y características BLE.
 * - Gestionar callbacks de conexión y desconexión.
 */
class EmisoraBLE {
private:

  /// Nombre de la emisora BLE
  const char * nombreEmisora;

  /// Identificador del fabricante (Manufacturer ID)
  const uint16_t fabricanteID;

  /// Potencia de transmisión (Tx Power)
  const int8_t txPower;

public:

  /**
   * @brief Callback llamado cuando se establece una conexión BLE.
   * @param connHandle Identificador de la conexión.
   */
  using CallbackConexionEstablecida = void ( uint16_t connHandle );

  /**
   * @brief Callback llamado cuando se termina una conexión BLE.
   * @param connHandle Identificador de la conexión.
   * @param reason Motivo de la desconexión.
   */
  using CallbackConexionTerminada = void ( uint16_t connHandle, uint8_t reason);

  /**
   * @brief Constructor de la emisora BLE.
   *
   * @param nombreEmisora_ Nombre de la emisora.
   * @param fabricanteID_ Identificador del fabricante.
   * @param txPower_ Potencia de transmisión.
   */
  EmisoraBLE( const char * nombreEmisora_, const uint16_t fabricanteID_,
              const int8_t txPower_ )
    :
    nombreEmisora( nombreEmisora_ ),
    fabricanteID( fabricanteID_ ),
    txPower( txPower_ )
  {
    // No se enciende la emisora en el constructor para evitar problemas
    // antes de la inicialización del puerto serie.
  }

  /**
   * @brief Inicializa y enciende la emisora BLE.
   *
   * Detiene cualquier anuncio activo previo.
   */
  void encenderEmisora() {
    Bluefruit.begin();
    (*this).detenerAnuncio();
  }

  /**
   * @brief Inicializa la emisora BLE e instala callbacks de conexión.
   *
   * @param cbce Callback de conexión establecida.
   * @param cbct Callback de conexión terminada.
   */
  void encenderEmisora( CallbackConexionEstablecida cbce,
                        CallbackConexionTerminada cbct ) {

    encenderEmisora();
    instalarCallbackConexionEstablecida( cbce );
    instalarCallbackConexionTerminada( cbct );
  }

  /**
   * @brief Detiene el anuncio BLE si está activo.
   */
  void detenerAnuncio() {
    if ( (*this).estaAnunciando() ) {
      Bluefruit.Advertising.stop();
    }
  }

  /**
   * @brief Comprueba si la emisora está anunciando.
   * @return true si está anunciando, false en caso contrario.
   */
  bool estaAnunciando() {
    return Bluefruit.Advertising.isRunning();
  }

  /**
   * @brief Emite un anuncio iBeacon estándar.
   *
   * @param beaconUUID UUID del beacon.
   * @param major Valor major.
   * @param minor Valor minor.
   * @param rssi Potencia RSSI del beacon.
   */
  void emitirAnuncioIBeacon( uint8_t * beaconUUID, int16_t major,
                             int16_t minor, uint8_t rssi ) {

    (*this).detenerAnuncio();

    // Limpieza de buffers de advertising y scan response
    Bluefruit.Advertising.stop();
    Bluefruit.Advertising.clearData();
    Bluefruit.ScanResponse.clearData();

    BLEBeacon elBeacon( beaconUUID, major, minor, rssi );
    elBeacon.setManufacturer( (*this).fabricanteID );

    Bluefruit.setTxPower( (*this).txPower );
    Bluefruit.setName( (*this).nombreEmisora );
    Bluefruit.ScanResponse.addName();

    Bluefruit.Advertising.setBeacon( elBeacon );
    Bluefruit.Advertising.restartOnDisconnect(true);
    Bluefruit.Advertising.setInterval(125, 125);

    Bluefruit.Advertising.start( 0 );
  }

  /**
   * @brief Emite un anuncio iBeacon con carga libre.
   *
   * Permite enviar una carga personalizada de hasta 21 bytes en lugar
   * del formato iBeacon estándar.
   *
   * @param carga Datos a emitir.
   * @param tamanyoCarga Tamaño de la carga.
   */
  void emitirAnuncioIBeaconLibre( const char * carga,
                                  const uint8_t tamanyoCarga ) {

    (*this).detenerAnuncio();

    Bluefruit.Advertising.clearData();
    Bluefruit.ScanResponse.clearData();

    Bluefruit.setName( (*this).nombreEmisora );
    Bluefruit.ScanResponse.addName();

    Bluefruit.Advertising.addFlags(
      BLE_GAP_ADV_FLAGS_LE_ONLY_GENERAL_DISC_MODE );

    uint8_t restoPrefijoYCarga[4+21] = {
      0x4c, 0x00,
      0x02,
      21,
      '-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-',
      '-', '-', '-', '-', '-', '-', '-', '-', '-'
    };

    memcpy( &restoPrefijoYCarga[4], &carga[0],
            ( tamanyoCarga > 21 ? 21 : tamanyoCarga ) );

    Bluefruit.Advertising.addData(
      BLE_GAP_AD_TYPE_MANUFACTURER_SPECIFIC_DATA,
      &restoPrefijoYCarga[0],
      4+21 );

    Bluefruit.Advertising.restartOnDisconnect(true);
    Bluefruit.Advertising.setInterval(125, 125);
    Bluefruit.Advertising.setFastTimeout( 1 );

    Bluefruit.Advertising.start( 0 );

    Globales::elPuerto.escribir(
      "emitiriBeacon libre  Bluefruit.Advertising.start( 0 );  \n");
  }

  /**
   * @brief Añade un servicio BLE a la emisora.
   *
   * @param servicio Servicio a añadir.
   * @return true si se añadió correctamente, false en caso contrario.
   */
  bool anyadirServicio( ServicioEnEmisora & servicio ) {

    Globales::elPuerto.escribir(
      " Bluefruit.Advertising.addService( servicio ); \n");

    bool r = Bluefruit.Advertising.addService( servicio );

    if ( ! r ) {
      Serial.println( " SERVICION NO AÑADIDO \n");
    }

    return r;
  }

  /**
   * @brief Añade un servicio BLE sin características adicionales.
   * @param servicio Servicio a añadir.
   * @return true si se añadió correctamente.
   */
  bool anyadirServicioConSusCaracteristicas(
      ServicioEnEmisora & servicio ) {
    return (*this).anyadirServicio( servicio );
  }

  /**
   * @brief Añade un servicio BLE junto con múltiples características.
   *
   * @tparam T Tipos de las características restantes.
   * @param servicio Servicio BLE.
   * @param caracteristica Primera característica.
   * @param restoCaracteristicas Resto de características.
   * @return true si se añadió correctamente.
   */
  template <typename ... T>
  bool anyadirServicioConSusCaracteristicas(
      ServicioEnEmisora & servicio,
      ServicioEnEmisora::Caracteristica & caracteristica,
      T& ... restoCaracteristicas) {

    servicio.anyadirCaracteristica( caracteristica );
    return anyadirServicioConSusCaracteristicas(
      servicio, restoCaracteristicas... );
  }

  /**
   * @brief Añade un servicio con características y lo activa.
   *
   * @tparam T Tipos de las características.
   * @param servicio Servicio BLE.
   * @param restoCaracteristicas Características a añadir.
   * @return true si se añadió correctamente.
   */
  template <typename ... T>
  bool anyadirServicioConSusCaracteristicasYActivar(
      ServicioEnEmisora & servicio,
      T& ... restoCaracteristicas) {

    bool r = anyadirServicioConSusCaracteristicas(
      servicio, restoCaracteristicas... );

    servicio.activarServicio();
    return r;
  }

  /**
   * @brief Instala el callback de conexión establecida.
   * @param cb Callback a instalar.
   */
  void instalarCallbackConexionEstablecida(
      CallbackConexionEstablecida cb ) {
    Bluefruit.Periph.setConnectCallback( cb );
  }

  /**
   * @brief Instala el callback de conexión terminada.
   * @param cb Callback a instalar.
   */
  void instalarCallbackConexionTerminada(
      CallbackConexionTerminada cb ) {
    Bluefruit.Periph.setDisconnectCallback( cb );
  }

  /**
   * @brief Obtiene la conexión BLE asociada a un handle.
   *
   * @param connHandle Identificador de la conexión.
   * @return Puntero a la conexión BLE.
   */
  BLEConnection * getConexion( uint16_t connHandle ) {
    return Bluefruit.Connection( connHandle );
  }

}; // class EmisoraBLE

#endif
