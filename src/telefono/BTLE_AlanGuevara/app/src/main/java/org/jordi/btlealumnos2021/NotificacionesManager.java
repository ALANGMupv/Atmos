// NotificacionesManager.java
package org.jordi.btlealumnos2021;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NotificacionesManager {

    private static NotificacionesManager instancia;
    private final RequestQueue queue;

    // Guarda la última lista conocida para detectar novedades
    private List<NotificacionAtmos> ultimaLista = new ArrayList<>();

    // URL real del endpoint de tu backend
    private static final String URL_NOTIFICACIONES =
            "https://nagufor.upv.edu.es/notificacionesUsuario";

    // Listener para callbacks
    public interface Listener {
        void onResultado(List<NotificacionAtmos> lista, boolean hayAlgoNuevo);
        void onError(String mensaje);
    }

    private NotificacionesManager(Context ctx) {
        queue = Volley.newRequestQueue(ctx.getApplicationContext());
    }

    public static synchronized NotificacionesManager getInstance(Context ctx) {
        if (instancia == null) {
            instancia = new NotificacionesManager(ctx);
        }
        return instancia;
    }

    /**
     * Llama al backend y obtiene la lista de notificaciones reales.
     *
     * @param ctx                  Contexto Android
     * @param idUsuarioForzado     ID de usuario a usar para la petición.
     *                             Si es > 0, se usa tal cual.
     *                             Si es <= 0, se intenta leer de SesionManager.
     */
    public void refrescarNotificaciones(Context ctx, int idUsuarioForzado, Listener listener) {

        // 1) Resolver qué id_usuario vamos a usar
        int idUsuario;
        if (idUsuarioForzado > 0) {
            idUsuario = idUsuarioForzado; // modo pruebas
        } else {
            idUsuario = SesionManager.obtenerIdUsuario(ctx); // modo normal
        }

        if (idUsuario <= 0) {
            if (listener != null) {
                listener.onError("No se encontró el usuario en la sesión local.");
            }
            return;
        }

        String url = URL_NOTIFICACIONES + "?id_usuario=" + idUsuario;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        String status = response.optString("status", "error");
                        if (!status.equalsIgnoreCase("ok")) {
                            if (listener != null) {
                                listener.onError("El servidor devolvió un estado no OK.");
                            }
                            return;
                        }

                        JSONArray arr = response.optJSONArray("notificaciones");
                        List<NotificacionAtmos> nuevaLista = new ArrayList<>();

                        if (arr != null) {
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject nJson = arr.getJSONObject(i);

                                String tipo      = nJson.optString("tipo", "");
                                String titulo    = nJson.optString("titulo", "");
                                String texto     = nJson.optString("texto", "");
                                String fechaHora = nJson.optString("fecha_hora", "");

                                // ------------------------------------------------------------------
                                // 🔥 Conversión correcta de fecha UTC ("...Z") → hora local del móvil
                                // ------------------------------------------------------------------
                                String horaCorta = fechaHora;
                                try {
                                    if (fechaHora != null && fechaHora.endsWith("Z")) {

                                        // Ejemplo que viene del backend:
                                        // 2025-11-21T19:30:00.000Z
                                        java.text.SimpleDateFormat parser =
                                                new java.text.SimpleDateFormat(
                                                        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                                                        java.util.Locale.getDefault()
                                                );
                                        parser.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

                                        java.util.Date date = parser.parse(fechaHora);

                                        java.text.SimpleDateFormat formatter =
                                                new java.text.SimpleDateFormat(
                                                        "HH:mm",
                                                        java.util.Locale.getDefault()
                                                );
                                        formatter.setTimeZone(java.util.TimeZone.getDefault());

                                        horaCorta = formatter.format(date);

                                    } else if (fechaHora.length() >= 16) {
                                        // Fallback: cortar hora HH:mm al estilo antiguo
                                        horaCorta = fechaHora.substring(11, 16);
                                    }
                                } catch (Exception e) {
                                    horaCorta = fechaHora; // fallback si algo falla
                                }

                                boolean leido = nJson.optBoolean("leido", false);

                                nuevaLista.add(
                                        new NotificacionAtmos(
                                                tipo,
                                                titulo,
                                                texto,
                                                horaCorta,
                                                leido
                                        )
                                );
                            }
                        }

                        boolean hayNuevas = hayNovedades(nuevaLista, ultimaLista);

                        // Actualizar la última lista
                        ultimaLista = nuevaLista;

                        if (listener != null) {
                            listener.onResultado(nuevaLista, hayNuevas);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        if (listener != null) {
                            listener.onError("Error al procesar la respuesta de notificaciones.");
                        }
                    }
                },
                error -> {
                    if (listener != null) {
                        listener.onError("Error de red al obtener notificaciones.");
                    }
                }
        );

        queue.add(request);
    }

    /**
     * Detecta si una nueva lista contiene novedades respecto a la anterior.
     */
    private boolean hayNovedades(List<NotificacionAtmos> nueva, List<NotificacionAtmos> anterior) {
        if (anterior == null || anterior.isEmpty()) {
            return !nueva.isEmpty();
        }

        if (nueva.size() != anterior.size()) {
            return true;
        }

        for (int i = 0; i < nueva.size(); i++) {
            NotificacionAtmos a = nueva.get(i);
            NotificacionAtmos b = anterior.get(i);

            String hashA = a.getTipo() + "|" + a.getTexto() + "|" + a.getHora();
            String hashB = b.getTipo() + "|" + b.getTexto() + "|" + b.getHora();

            if (!hashA.equals(hashB)) {
                return true;
            }
        }

        return false;
    }

    public List<NotificacionAtmos> getUltimaLista() {
        return ultimaLista;
    }
}
