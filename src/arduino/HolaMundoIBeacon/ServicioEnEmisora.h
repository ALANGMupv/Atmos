// -*- mode: c++ -*-

/**
 * @file ServicioEnEmisora.h
 * @author Jordi Bataller i Mascarell
 * @date 2019-07-17
 * @brief Representa un servicio BLE y sus características.
 *
 * Este fichero define la clase ServicioEnEmisora, pensada para cuando la placa
 * actúa como periférico BLE al que un cliente (móvil, PC, etc.) puede conectarse
 * para leer, escribir o recibir notificaciones de datos.
 *
 * Incluye la clase interna Caracteristica, que permite definir cada dato
 * individual del servicio (por ejemplo CO₂, temperatura, ozono), junto con
 * sus propiedades, permisos de acceso y tamaño máximo.
 */

#ifndef SERVICIO_EMISORA_H_INCLUIDO
#define SERVICIO_EMISORA_H_INCLUIDO

#include <vector>
#include <bluefruit.h>     // Para asegurar que se define BLESecurityMode

/// Alias para mantener compatibilidad con el tipo de seguridad BLE
using BleSecurityMode = SecureMode_t;

/**
 * @brief Invierte el contenido de un array en el propio array.
 *
 * Se utiliza porque los UUIDs en BLE se almacenan en orden inverso
 * al habitual.
 *
 * @tparam T Tipo de los elementos del array.
 * @param p Puntero al array.
 * @param n Número de elementos.
 * @return Puntero al array invertido.
 */
template< typename T >
T * alReves( T * p, int n ) {
  T aux;

  for( int i = 0; i < n/2; i++ ) {
    aux = p[i];
    p[i] = p[n-i-1];
    p[n-i-1] = aux;
  }
  return p;
}

/**
 * @brief Convierte un string en un array uint8_t copiándolo en orden inverso.
 *
 * Se usa principalmente para construir UUIDs BLE a partir de cadenas de texto.
 *
 * @param pString Cadena de entrada.
 * @param pUint Array destino.
 * @param tamMax Tamaño máximo del array.
 * @return Puntero al array destino.
 */
uint8_t * stringAUint8AlReves( const char * pString,
                               uint8_t * pUint,
                               int tamMax ) {

  int longitudString = strlen( pString );
  int longitudCopiar = ( longitudString > tamMax ? tamMax : longitudString );

  for( int i = 0; i <= longitudCopiar - 1; i++ ) {
    pUint[ tamMax - i - 1 ] = pString[ i ];
  }

  return pUint;
}

/**
 * @class ServicioEnEmisora
 * @brief Representa un servicio BLE dentro de una emisora.
 *
 * Un servicio BLE actúa como contenedor de varias características.
 * Aunque en este proyecto actualmente se trabaja solo en modo beacon
 * (broadcast sin conexión), la estructura de servicio y características
 * está preparada para futuras ampliaciones con conexión BLE.
 */
class ServicioEnEmisora {

public:

  /**
   * @brief Callback que se ejecuta cuando una característica es escrita.
   *
   * @param conn_handle Handle de la conexión BLE.
   * @param chr Puntero a la característica escrita.
   * @param data Datos recibidos.
   * @param len Longitud de los datos.
   */
  using CallbackCaracteristicaEscrita =
    void ( uint16_t conn_handle,
           BLECharacteristic * chr,
           uint8_t * data,
           uint16_t len );

  /**
   * @class Caracteristica
   * @brief Representa una característica BLE dentro de un servicio.
   *
   * Cada característica define un dato concreto (por ejemplo CO₂ o temperatura)
   * con sus propiedades (lectura, escritura, notificación), permisos y tamaño.
   */
  class Caracteristica {

  private:

    /// UUID de la característica (almacenado en orden inverso)
    uint8_t uuidCaracteristica[16] = {
      '0','1','2','3','4','5','6','7',
      '8','9','A','B','C','D','E','F'
    };

    /// Objeto BLECharacteristic subyacente
    BLECharacteristic laCaracteristica;

  public:

    /**
     * @brief Constructor de la característica.
     *
     * @param nombreCaracteristica_ Nombre de la característica (usado para generar el UUID).
     */
    Caracteristica( const char * nombreCaracteristica_ )
      :
      laCaracteristica(
        stringAUint8AlReves(
          nombreCaracteristica_,
          &uuidCaracteristica[0],
          16
        )
      )
    {
    }

    /**
     * @brief Constructor completo de la característica.
     *
     * @param nombreCaracteristica_ Nombre de la característica.
     * @param props Propiedades BLE (read, write, notify).
     * @param permisoRead Permisos de lectura.
     * @param permisoWrite Permisos de escritura.
     * @param tam Tamaño máximo de los datos.
     */
    Caracteristica( const char * nombreCaracteristica_,
                    uint8_t props,
                    BleSecurityMode permisoRead,
                    BleSecurityMode permisoWrite,
                    uint8_t tam )
      :
      Caracteristica( nombreCaracteristica_ )
    {
      (*this).asignarPropiedadesPermisosYTamanyoDatos(
        props, permisoRead, permisoWrite, tam );
    }

  private:

    /**
     * @brief Asigna las propiedades BLE de la característica.
     *
     * @param props Propiedades (CHR_PROPS_READ, WRITE, NOTIFY, etc.).
     */
    void asignarPropiedades( uint8_t props ) {
      (*this).laCaracteristica.setProperties( props );
    }

    /**
     * @brief Asigna los permisos de lectura y escritura.
     *
     * @param permisoRead Permiso de lectura.
     * @param permisoWrite Permiso de escritura.
     */
    void asignarPermisos( BleSecurityMode permisoRead,
                          BleSecurityMode permisoWrite ) {
      (*this).laCaracteristica.setPermission(
        permisoRead, permisoWrite );
    }

    /**
     * @brief Asigna el tamaño máximo de datos.
     *
     * @param tam Tamaño máximo en bytes.
     */
    void asignarTamanyoDatos( uint8_t tam ) {
      (*this).laCaracteristica.setMaxLen( tam );
    }

  public:

    /**
     * @brief Configura propiedades, permisos y tamaño de la característica.
     */
    void asignarPropiedadesPermisosYTamanyoDatos(
      uint8_t props,
      BleSecurityMode permisoRead,
      BleSecurityMode permisoWrite,
      uint8_t tam ) {

      asignarPropiedades( props );
      asignarPermisos( permisoRead, permisoWrite );
      asignarTamanyoDatos( tam );
    }

    /**
     * @brief Escribe datos en la característica.
     *
     * @param str Cadena a escribir.
     * @return Número de bytes escritos.
     */
    uint16_t escribirDatos( const char * str ) {
      uint16_t r = (*this).laCaracteristica.write( str );
      return r;
    }

    /**
     * @brief Notifica datos a un cliente conectado.
     *
     * @param str Cadena a notificar.
     * @return Número de bytes notificados.
     */
    uint16_t notificarDatos( const char * str ) {
      uint16_t r = laCaracteristica.notify( &str[0] );
      return r;
    }

    /**
     * @brief Instala el callback de escritura de la característica.
     *
     * @param cb Callback a instalar.
     */
    void instalarCallbackCaracteristicaEscrita(
      CallbackCaracteristicaEscrita cb ) {
      (*this).laCaracteristica.setWriteCallback( cb );
    }

    /**
     * @brief Activa la característica en el stack BLE.
     */
    void activar() {
      err_t error = (*this).laCaracteristica.begin();
      Globales::elPuerto.escribir(
        " (*this).laCaracteristica.begin(); error = " );
      Globales::elPuerto.escribir( error );
    }

  }; // class Caracteristica

private:

  /// UUID del servicio BLE
  uint8_t uuidServicio[16] = {
    '0','1','2','3','4','5','6','7',
    '8','9','A','B','C','D','E','F'
  };

  /// Servicio BLE subyacente
  BLEService elServicio;

  /// Lista de características asociadas al servicio
  std::vector< Caracteristica * > lasCaracteristicas;

public:

  /**
   * @brief Constructor del servicio BLE.
   *
   * @param nombreServicio_ Nombre del servicio (usado para generar el UUID).
   */
  ServicioEnEmisora( const char * nombreServicio_ )
    :
    elServicio(
      stringAUint8AlReves(
        nombreServicio_, &uuidServicio[0], 16 ) )
  {
  }

  /**
   * @brief Muestra el UUID del servicio por el puerto serie (debug).
   */
  void escribeUUID() {
    Serial.println ( "**********" );
    for (int i = 0; i <= 15; i++) {
      Serial.print( (char) uuidServicio[i] );
    }
    Serial.println ( "\n**********" );
  }

  /**
   * @brief Añade una característica al servicio.
   *
   * @param car Característica a añadir.
   */
  void anyadirCaracteristica( Caracteristica & car ) {
    (*this).lasCaracteristicas.push_back( & car );
  }

  /**
   * @brief Activa el servicio y todas sus características.
   */
  void activarServicio() {

    err_t error = (*this).elServicio.begin();
    Serial.print( " (*this).elServicio.begin(); error = " );
    Serial.println( error );

    for( auto pCar : (*this).lasCaracteristicas ) {
      (*pCar).activar();
    }
  }

  /**
   * @brief Conversión implícita a BLEService&.
   *
   * Permite usar un ServicioEnEmisora donde se espere un BLEService.
   */
  operator BLEService&() {
    return elServicio;
  }

}; // class ServicioEnEmisora

#endif
