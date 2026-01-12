package org.jordi.btlealumnos2021;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @brief Acceso a la API OpenAQ para la obtención de estaciones oficiales
 *        de calidad del aire y sus contaminantes.
 *
 * Gestiona la descarga, procesado y cacheo temporal de estaciones
 * oficiales, evitando peticiones innecesarias a la API y devolviendo
 * los resultados de forma segura al hilo principal mediante callbacks.
 *
 * @author Alan Guevara Martínez
 * @date 17/12/2025
 */

public class EstacionesMedidaAPI {

    /**
     * @brief Cache temporal de estaciones oficiales (OpenAQ).
     *
     */
    private static volatile List<EstacionOficial> cacheEstaciones = null;

    /**
     * @brief Marca temporal de la última actualización de la cache (ms).
     */
    private static volatile long cacheTimestampMs = 0;

    /**
     * @brief Tiempo de vida de la cache: 3 minutos.
     */
    private static final long CACHE_TTL_MS = 3 * 60 * 1000;


    /**
     * @brief Obtiene estaciones oficiales y sus datos de contaminantes uno a uno
     *        utilizando una estrategia fiable basada en OpenAQ v3.
     *
     * @details
     * Este método implementa una estrategia en tres fases:
     *
     * 1. Descarga una lista limitada de estaciones (locations) dentro de una
     *    zona geográfica concreta, para evitar saturar la API.
     * 2. Para cada estación obtenida, realiza una petición adicional al endpoint
     *    `/locations/{id}/sensors` para recuperar los sensores y su última medición.
     * 3. Rellena los datos de cada {@link EstacionOficial} y devuelve el resultado
     *    al hilo principal mediante un callback.
     *
     *
     * @param callback Callback que recibe la lista final de estaciones oficiales
     *                 completamente procesadas.
     *
     */
    public void obtenerEstacionesOficiales(EstacionesCallback callback) {

        // Handler asociado al hilo principal (UI thread).
        // Se utilizará para devolver el resultado al mapa de forma segura.
        Handler mainHandler = new Handler(Looper.getMainLooper());

        // ---------------------------------------------------------
        // USO DE CACHE (si es reciente)
        // Si se veulve a MapasActivity en menos de 1 minuto - NO hay ninguna llamada de red - Resultado inmediato y consistente
        // ---------------------------------------------------------
        long ahora = System.currentTimeMillis();

        if (cacheEstaciones != null && (ahora - cacheTimestampMs) < CACHE_TTL_MS) {
            Log.d("OPENAQ", "Usando estaciones desde CACHE ("
                    + cacheEstaciones.size() + ")");
            mainHandler.post(() -> callback.onResult(cacheEstaciones));
            return;
        }


        // Se lanza un hilo secundario para evitar bloquear el hilo principal
        // durante las múltiples operaciones de red.
        new Thread(() -> {
            try {

                // Usamos varios hilos para descargar los sensores y que no tarde tanto
                ExecutorService pool = Executors.newFixedThreadPool(6);

                // Lista de tareas para poder esperar a que todas terminen
                List<Future<?>> tareas = new ArrayList<>();

                // -----------------------------------------------------------
                // PASO 1: Descarga de la lista de estaciones (locations)
                // -----------------------------------------------------------
                // Se limita el número de estaciones a 40 para evitar realizar
                // demasiadas peticiones individuales en el siguiente paso.
                String urlLoc = "https://api.openaq.org/v3/locations"
                        + "?bbox=-2.0,37.7,0.8,40.8"
                        + "&limit=40";

                Log.d("OPENAQ", "Paso 1: Bajando lista de estaciones...");

                // Descarga del JSON con la lista de estaciones
                String jsonLoc = descargarUrl(urlLoc);

                // Si ocurre un error en la descarga, se aborta el proceso
                // (el método descargarUrl ya registra el error en Logcat)
                if (jsonLoc == null) return;

                // Parseo del JSON recibido
                JSONObject rootLoc = new JSONObject(jsonLoc);
                JSONArray resultsLoc = rootLoc.getJSONArray("results");

                // Lista estaciones sincronizada, evita que devuelva la lista si no está completa
                List<EstacionOficial> listaEstaciones =
                        Collections.synchronizedList(new ArrayList<>());

                Log.d(
                        "OPENAQ",
                        "Estaciones encontradas: " + resultsLoc.length() + ". Bajando detalles..."
                );

                // -----------------------------------------------------------
                // PASO 2: Descarga del detalle de cada estación (sensores)
                // -----------------------------------------------------------
                // Para cada estación, se consulta su endpoint específico
                // /locations/{id}/sensors para obtener los contaminantes disponibles
                for (int i = 0; i < resultsLoc.length(); i++) {

                    JSONObject e = resultsLoc.getJSONObject(i);

                    // Se crea el objeto EstacionOficial para esta estación
                    EstacionOficial est = new EstacionOficial();

                    // Identificador único de la estación en OpenAQ
                    est.id = e.getInt("id");

                    // Nombre de la estación (si no existe, se genera uno genérico)
                    est.nombre = e.optString("name", "Estación " + est.id);

                    // Coordenadas geográficas de la estación
                    JSONObject coords = e.getJSONObject("coordinates");
                    est.lat = coords.getDouble("latitude");
                    est.lon = coords.getDouble("longitude");

                    /// -------------------------------------------------------
                    /// DESCARGA DEL DETALLE DE SENSORES DE ESTA ESTACIÓN
                    /// -------------------------------------------------------

                    /// Cada estación se descarga en un hilo independiente
                    Future<?> tarea = pool.submit(() -> {
                        try {

                            String urlDetalle =
                                    "https://api.openaq.org/v3/locations/" + est.id + "/sensors";

                            // Descarga bloqueante, pero ahora en hilo paralelo
                            String jsonDetalle = descargarUrl(urlDetalle);

                            if (jsonDetalle != null) {

                                JSONObject rootDet = new JSONObject(jsonDetalle);
                                JSONArray sensors = rootDet.getJSONArray("results");

                                // Recorremos sensores de esta estación
                                for (int j = 0; j < sensors.length(); j++) {

                                    JSONObject s = sensors.getJSONObject(j);
                                    JSONObject paramObj = s.getJSONObject("parameter");

                                    String paramName =
                                            paramObj.getString("name").toLowerCase();

                                    String unit =
                                            paramObj.optString("units", "µg/m³");

                                    // Última medición del sensor
                                    if (s.has("latest") && !s.isNull("latest")) {

                                        JSONObject latest = s.getJSONObject("latest");
                                        double val = latest.getDouble("value");

                                        // Asignación según contaminante
                                        if (paramName.contains("no2")
                                                || paramName.contains("nitrogen")) {

                                            est.no2 = val;
                                            est.unidadNO2 = unit;

                                        } else if (paramName.contains("o3")
                                                || paramName.contains("ozone")) {

                                            est.o3 = val;
                                            est.unidadO3 = unit;

                                        } else if (paramName.contains("co")
                                                || paramName.contains("carbon")) {

                                            est.co = val;
                                            est.unidadCO = unit;

                                        } else if (paramName.contains("so2")
                                                || paramName.contains("sulfur")) {

                                            est.so2 = val;
                                            est.unidadSO2 = unit;
                                        }
                                    }
                                }
                            }

                            // Una vez se completa:
                            listaEstaciones.add(est);

                        } catch (Exception ex) {
                            // Si falla esta estación, no bloquea a las demás
                            Log.w("OPENAQ", "Error bajando detalle estación " + est.id);
                        }
                    });

                    // Guardamos la tarea para esperar luego a que termine
                    tareas.add(tarea);

                    // Se añade la estación (con los datos disponibles) a la lista final
                    // listaEstaciones.add(est);
                }

                /// -----------------------------------------------------------
                /// ESPERAR A QUE TODAS LAS DESCARGAS TERMINEN
                /// -----------------------------------------------------------
                for (Future<?> f : tareas) {
                    try {
                        // Bloquea hasta que la tarea termina
                        f.get();
                    } catch (Exception ignored) {
                    }
                }

                // Cerramos el pool de hilos
                pool.shutdown();


                // -----------------------------------------------------------
                // PASO 3: Envío del resultado al hilo principal
                // -----------------------------------------------------------
                Log.d(
                        "OPENAQ",
                        "Proceso terminado. Enviando "
                                + listaEstaciones.size()
                                + " estaciones."
                );

                // ---------------------------------------------------------
                // GUARDAR CACHE Y DEVOLVER RESULTADO
                // ---------------------------------------------------------

                // Clonamos la lista para evitar modificaciones externas
                cacheEstaciones = new ArrayList<>(listaEstaciones);
                cacheTimestampMs = System.currentTimeMillis();

                Log.d("OPENAQ", "Cache actualizada con "
                        + cacheEstaciones.size() + " estaciones");

                // Enviar resultado al hilo principal
                mainHandler.post(() -> callback.onResult(cacheEstaciones));

            } catch (Exception e) {
                // Captura de cualquier error no controlado durante el proceso
                Log.e("OPENAQ", "Error fatal: " + e.getMessage(), e);
            }
        }).start();
    }

    /**
     * @brief Descarga el contenido JSON desde una URL mediante una petición HTTP GET.
     *
     * @details
     * Método auxiliar encargado de realizar una petición HTTP a la URL indicada,
     * configurando las cabeceras necesarias (incluida la API Key).
     *
     * @param urlString Cadena con la URL completa desde la que se desea descargar el JSON.
     * @return Cadena con el contenido JSON descargado, o {@code null} si ocurre un error.
     *
     */
    private String descargarUrl(String urlString) {

        // Referencia a la conexión HTTP para poder cerrarla en el bloque finally
        HttpURLConnection conn = null;

        try {
            // Creación del objeto URL a partir de la cadena proporcionada
            URL url = new URL(urlString);

            // Apertura de la conexión HTTP
            conn = (HttpURLConnection) url.openConnection();

            // Configuración del método HTTP (GET)
            conn.setRequestMethod("GET");

            // Cabecera obligatoria para autenticación en la API OpenAQ
            conn.setRequestProperty(
                    "X-API-Key",
                    "3dd56585357ae0bd5b39f7c77852e61d63b0d3ac21f6da1b8befedd154d58e0a"
            );

            // Indica que se espera una respuesta en formato JSON
            conn.setRequestProperty("Accept", "application/json");

            // Se obtiene el código de respuesta HTTP del servidor
            int code = conn.getResponseCode();

            // Si el código HTTP indica error (4xx o 5xx),
            // se registra en el log y se devuelve null
            if (code >= 400) {
                Log.e("OPENAQ", "Error HTTP " + code + " en: " + urlString);
                return null;
            }

            // Si la respuesta es correcta, se lee todo el InputStream
            // y se devuelve el contenido JSON como String
            return new Scanner(conn.getInputStream())
                    .useDelimiter("\\A")
                    .next();

        } catch (Exception e) {
            // Captura de errores de red, formato de URL o lectura de datos
            Log.e("OPENAQ", "Error de red o lectura: " + e.getMessage());
            return null;

        } finally {
            // Se cierra la conexión HTTP para liberar recursos del sistema
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * @interface EstacionesCallback
     * @brief Callback para recibir la lista de estaciones oficiales de calidad del aire.
     *
     */
    public interface EstacionesCallback {
        /**
         * @param estaciones Lista de objetos {@link EstacionOficial} con los datos
         *                   de localización y contaminantes (NO2, O3, CO, SO2).
         *
         * @note Si ocurre un error durante la petición o el procesado de datos,
         *       la lista puede estar vacía.
         */
        void onResult(List<EstacionOficial> estaciones);
    }
}
