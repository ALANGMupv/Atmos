package org.jordi.btlealumnos2021;

/**
 * Nombre Fichero: AppLifecycleTracker.java
 * Descripción:
 *      Clase auxiliar que mantiene un único flag estático para indicar
 *      si la aplicación viene de un inicio en frío (app recién abierta)
 *      o si ya estaba ejecutándose en segundo plano.
 *
 *      Se utiliza para decidir cuándo mostrar la autenticación biométrica.
 */
public class AppLifecycleTracker {

    /**
     * true  -> la app acaba de abrirse desde cero
     * false -> la app ya estaba abierta (regreso desde segundo plano)
     */
    public static boolean appFueCerrada = true;
}
