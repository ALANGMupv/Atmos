package org.jordi.btlealumnos2021;

import android.util.Log;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// -----------------------------------------------------------------------------------
// @author: Alan Guevara Martínez
// LogicaFake.java: Envío de mediciones adaptado a la nueva BBDD ('medida')
// -----------------------------------------------------------------------------------
public class LogicaFake {
    private static final String TAG = ">>>>";

    // Deja tu endpoint tal cual lo tenías para no tocar nada más
    private static final String API_URL = "https://nagufor.upv.edu.es/medida";

    // uuid: Texto, gas: Z, valor: R, contador: Z → guardarMedicion() →
    public void guardarMedicion(String uuid, int gas, float valor, int contador) {
        new Thread(() -> {
            try {
                // --- Mapeo mínimo y campos nuevos requeridos por la BBDD ---
                String idPlaca = uuid;                     // id_placa ← uuid que ya recibimos
                int    tipo    = gas;                      // tipo     ← gas
                double val      = (double) valor;          // valor    ← valor (double)
                String latitud  = String.format(Locale.US, "%.2f", 0.00); // por ahora 0.00
                String longitud = String.format(Locale.US, "%.2f", 0.00); // por ahora 0.00
                String fechaISO = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                        .format(new Date());               // fecha_hora

                JSONObject json = new JSONObject();
                json.put("id_placa",   idPlaca);
                json.put("tipo",       tipo);
                json.put("valor",      val);
                json.put("latitud",    latitud);
                json.put("longitud",   longitud);
                json.put("fecha_hora", fechaISO);

                // (Opcional) Si tu backend ya hace NOW(), comenta la línea de fecha_hora:
                // json.remove("fecha_hora");

                // LOG para ver exactamente qué se envía
                Log.d(TAG, "Enviando JSON NUEVO: " + json.toString());

                // Conexión HTTP
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                // Cuerpo JSON
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.toString().getBytes(StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();
                Log.d(TAG, "guardarMedicion(): HTTP " + code);

                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "guardarMedicion() error", e);
            }
        }).start();
    }
}
