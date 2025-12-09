package org.jordi.btlealumnos2021;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class NotificacionesManager {

    private static NotificacionesManager instancia;
    private final RequestQueue queue;

    private List<NotificacionAtmos> ultimaLista = new ArrayList<>();

    private static final String URL_NOTIFICACIONES =
            "https://nagufor.upv.edu.es/notificacionesUsuario";
    private static final String URL_MARCAR_LEIDA =
            "https://nagufor.upv.edu.es/marcarNotificacionLeida";
    private static final String URL_BORRAR_NOTI =
            "https://nagufor.upv.edu.es/borrarNotificacion";
    private static final String URL_BORRAR_TODAS =
            "https://nagufor.upv.edu.es/borrarNotificacionesUsuario";

    public interface Listener {
        void onResultado(List<NotificacionAtmos> lista, boolean hayNuevas);
        void onError(String msg);
    }

    private NotificacionesManager(Context ctx) {
        queue = Volley.newRequestQueue(ctx.getApplicationContext());
    }

    public static synchronized NotificacionesManager getInstance(Context ctx) {
        if (instancia == null) instancia = new NotificacionesManager(ctx);
        return instancia;
    }

    // ─────────────────────────────────────────────────────────────
    //     Conversión de UTC → Local (HH:mm)
    // ─────────────────────────────────────────────────────────────

    private String convertirUtcALocal(String fechaUtc) {
        try {
            SimpleDateFormat formatoUtc =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            formatoUtc.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date date = formatoUtc.parse(fechaUtc);

            SimpleDateFormat formatoLocal =
                    new SimpleDateFormat("HH:mm");
            formatoLocal.setTimeZone(TimeZone.getDefault());

            return formatoLocal.format(date);

        } catch (Exception e) {
            return fechaUtc;
        }
    }

    // ─────────────────────────────────────────────────────────────
    //   Obtener notificaciones del backend
    // ─────────────────────────────────────────────────────────────

    public void refrescarNotificaciones(Context ctx, int idUsuario, Listener listener) {

        String url = URL_NOTIFICACIONES + "?id_usuario=" + idUsuario;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (!"ok".equals(response.optString("status"))) {
                            if (listener != null) listener.onError("Estado no OK");
                            return;
                        }

                        JSONArray arr = response.optJSONArray("notificaciones");
                        List<NotificacionAtmos> nuevaLista = new ArrayList<>();

                        if (arr != null) {
                            for (int i = 0; i < arr.length(); i++) {

                                JSONObject o = arr.getJSONObject(i);

                                int id = o.optInt("id_notificacion", -1);
                                String tipo = o.optString("tipo", "");
                                String titulo = o.optString("titulo", "");
                                String texto = o.optString("texto", "");
                                String fechaUtc = o.optString("fecha_hora", "");
                                boolean leido = o.optBoolean("leido", false);

                                // ✔ conversión REAL UTC → hora local
                                String horaLocal = convertirUtcALocal(fechaUtc);

                                nuevaLista.add(
                                        new NotificacionAtmos(
                                                id, tipo, titulo, texto, horaLocal, leido
                                        )
                                );
                            }
                        }

                        boolean hayNuevas = hayNovedades(nuevaLista, ultimaLista);
                        ultimaLista = nuevaLista;

                        if (listener != null) listener.onResultado(nuevaLista, hayNuevas);

                    } catch (Exception e) {
                        if (listener != null) listener.onError("Error procesando JSON");
                    }
                },
                error -> {
                    if (listener != null) listener.onError("Error de red");
                }
        );

        queue.add(req);
    }

    // ─────────────────────────────────────────────────────────────
    //   Marcar como leída
    // ─────────────────────────────────────────────────────────────

    public void marcarNotificacionComoLeida(Context ctx, int idUsuario, int idNoti) {
        try {
            JSONObject body = new JSONObject();
            body.put("id_usuario", idUsuario);
            body.put("id_notificacion", idNoti);

            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.POST,
                    URL_MARCAR_LEIDA,
                    body,
                    response -> {},
                    error -> {}
            );

            queue.add(req);

        } catch (Exception ignored) {}
    }

    // ─────────────────────────────────────────────────────────────
    //   Borrar UNA notificación (con callback)
    // ─────────────────────────────────────────────────────────────

    public void borrarNotificacionBackend(Context ctx, int idUsuario, int idNoti, Runnable onDone) {
        try {
            JSONObject body = new JSONObject();
            body.put("id_usuario", idUsuario);
            body.put("id_notificacion", idNoti);

            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.POST,
                    URL_BORRAR_NOTI,
                    body,
                    response -> { if (onDone != null) onDone.run(); },
                    error -> { if (onDone != null) onDone.run(); }
            );

            queue.add(req);

        } catch (Exception e) {
            if (onDone != null) onDone.run();
        }
    }

    // ─────────────────────────────────────────────────────────────
    //   Borrar TODAS las notificaciones
    // ─────────────────────────────────────────────────────────────

    public void borrarTodasBackend(int idUsuario, Runnable onDone) {
        try {
            JSONObject body = new JSONObject();
            body.put("id_usuario", idUsuario);

            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.POST,
                    URL_BORRAR_TODAS,
                    body,
                    response -> { if (onDone != null) onDone.run(); },
                    error -> { if (onDone != null) onDone.run(); }
            );

            queue.add(req);

        } catch (Exception e) {
            if (onDone != null) onDone.run();
        }
    }

    // ─────────────────────────────────────────────────────────────
    //   Detección de novedades
    // ─────────────────────────────────────────────────────────────

    private boolean hayNovedades(List<NotificacionAtmos> nueva, List<NotificacionAtmos> anterior) {

        if (anterior == null || anterior.size() != nueva.size()) return true;

        for (int i = 0; i < nueva.size(); i++) {

            NotificacionAtmos a = nueva.get(i);
            NotificacionAtmos b = anterior.get(i);

            if (a.getIdNotificacion() != b.getIdNotificacion()) return true;
        }

        return false;
    }

    public List<NotificacionAtmos> getUltimaLista() {
        return ultimaLista;
    }
}
