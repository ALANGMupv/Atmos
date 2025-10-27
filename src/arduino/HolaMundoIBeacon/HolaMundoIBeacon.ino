// -*-c++-*-

// --------------------------------------------------------------
//
// Jordi Bataller i Mascarell
// 2019-07-07
// HolaMundoIBeacon: Este fichero define el programa principal de la placa nRF52: inicializa el LED, el puerto serie, la emisora BLE y el medidor. En el setup prepara la placa 
// y en el loop hace parpadear el LED, mide CO₂ y publica el valor periódicamente mediante anuncios iBeacon.
// --------------------------------------------------------------

// https://learn.sparkfun.com/tutorials/nrf52840-development-with-arduino-and-circuitpython

// https://stackoverflow.com/questions/29246805/can-an-ibeacon-have-a-data-payload

// --------------------------------------------------------------
// --------------------------------------------------------------
#include <bluefruit.h>

// Eliminar macros “molestas”
#undef min // vaya tela, están definidos en bluefruit.h y  !
#undef max // colisionan con los de la biblioteca estándar

// --------------------------------------------------------------
// --------------------------------------------------------------
#include "LED.h"
#include "PuertoSerie.h"

// --------------------------------------------------------------
// --------------------------------------------------------------
namespace Globales {
  
  LED elLED ( /* NUMERO DEL PIN LED = */ 7 );

  PuertoSerie elPuerto ( /* velocidad = */ 115200 ); // 115200 o 9600 o ...

  // Serial1 en el ejemplo de Curro creo que es la conexión placa-sensor 
};

// --------------------------------------------------------------
// --------------------------------------------------------------
#include "EmisoraBLE.h"
#include "Publicador.h"
#include "Medidor.h"


// --------------------------------------------------------------
// --------------------------------------------------------------
namespace Globales {

  Publicador elPublicador;

  Medidor elMedidor;

}; // namespace

// --------------------------------------------------------------
// --------------------------------------------------------------
void inicializarPlaquita () {

  // de momento nada

} // ()

// --------------------------------------------------------------
// setup()
// --------------------------------------------------------------
void setup() {

// Se queda en bucle hasta que el puerto serie (Serial) esté listo para imprimir
  //Globales::elPuerto.esperarDisponible();

  // 
  // 
  // 
  inicializarPlaquita(); // Nada definido

  // Suspend Loop() to save power. Sirve para suspender temporalmente el loop() y ahorrar energía.
  // suspendLoop();

  // 
  // 
  // 

// Inicializa el chip Bluetooth (nRF52) y asegura que al inicio no haya anuncios activos
  Globales::elPublicador.encenderEmisora();

  // Globales::elPublicador.laEmisora.pruebaEmision();
  
  // 
  // 
  // 
  Globales::elMedidor.iniciarMedidor();

  // 
  // 
  // 
  esperar( 1000 );

  Globales::elPuerto.escribir( "---- setup(): fin ---- \n " );

} // setup ()

// --------------------------------------------------------------
// --------------------------------------------------------------
inline void lucecitas() { // La llamamos en el loop.
  using namespace Globales;

  elLED.brillar( 100 ); // 100 encendido
  esperar ( 400 ); //  100 apagado
  elLED.brillar( 100 ); // 100 encendido
  esperar ( 400 ); //  100 apagado
  Globales::elLED.brillar( 100 ); // 100 encendido
  esperar ( 400 ); //  100 apagado
  Globales::elLED.brillar( 1000 ); // 1000 encendido
  esperar ( 1000 ); //  100 apagado
} // ()

// --------------------------------------------------------------
// loop ()
// --------------------------------------------------------------
namespace Loop {
  uint8_t cont = 0;
};

// ..............................................................
// ..............................................................
void loop () {

  using namespace Loop;
  using namespace Globales;

  cont++;

  elPuerto.escribir( "\n---- loop(): empieza " );
  elPuerto.escribir( cont );
  elPuerto.escribir( "\n" );


  lucecitas();

  // 
  // mido y publico
  // 
  int valorCO2 = elMedidor.medirCO2();
  
  // UUID = fijo (EPSG-GTI-PROY-3A). Major = 2 bytes con el ID + contador. Minor = 2 bytes con el valor de CO₂. TxPower = 1 byte.
  
  elPublicador.publicarCO2( valorCO2, // Se publica en el UUID
							cont,
							1000 // intervalo de emisión
							);
  
  // 
  // mido y publico
  //
   
  /* COMENTAMOS, SOLO VAMOS A EMITIR CO2
  int valorTemperatura = elMedidor.medirTemperatura();
  
 
  elPublicador.publicarTemperatura( valorTemperatura, 
									cont,
									1000 // intervalo de emisión
									);
                  */

  // 
  // prueba para emitir un iBeacon y poner
  // en la carga (21 bytes = uuid 16 major 2 minor 2 txPower 1 )
  // lo que queramos (sin seguir dicho formato)
  // 
  // Al terminar la prueba hay que hacer Publicador::laEmisora privado
  // 

  
  char datos[21] = {
	'H', 'o', 'l', 'a',
	'H', 'o', 'l', 'a',
	'H', 'o', 'l', 'a',
	'H', 'o', 'l', 'a',
	'H', 'o', 'l', 'a',
	'H'
  };

/* COMENTAMOS, SOLO VAMOS A EMITIR CO2
// elPublicador.laEmisora.emitirAnuncioIBeaconLibre ( &datos[0], 21 );
elPublicador.laEmisora.emitirAnuncioIBeaconLibre(datos, 21);
*/


  esperar( 2500 ); // Ahora mismo sirve para que a Android le de tiempo a procesar y enviar el anuncio a la api
  // Le añado 500ms más por precaución, no pierde y Android detecta todos los anuncios, pero como he tenido problemas: “Hombre prevenido vale por dos.”

  // elPublicador.laEmisora.detenerAnuncio(); ESTO YA NO ME SIRVE, PUEDE DETENER EL ANUNCIO PERO NO LIMPIA BUFFER POR LO QUE NO EMITE UNO NUEVO, AHORA LIMPIO YA BUFFERS EN emitirAnuncioIBeacon()
  
  // 
  // 
  // 
  elPuerto.escribir( "---- loop(): acaba **** " );
  elPuerto.escribir( cont );
  elPuerto.escribir( "\n" );
  
} // loop ()
// --------------------------------------------------------------
// --------------------------------------------------------------
// --------------------------------------------------------------
// --------------------------------------------------------------
