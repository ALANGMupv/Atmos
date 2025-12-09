package org.jordi.btlealumnos2021;

import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase de ayuda para gestionar la gráfica de calidad del aire.
 *
 * Se encarga de:
 *   - Alternar entre modo "día" (últimos 7 días) y "hora" (últimas 8 horas).
 *   - Pedir los datos a LogicaFake (endpoints /resumen7Dias y /resumen8Horas).
 *   - Pintar la gráfica con MPAndroidChart.
 *   - Actualizar la carita y el texto de estado de la gráfica.
 *
 * Autor: Alan Guevara Martínez
 * Fecha: 21/11/2025
 */
public class GraficaHelper {

    private final Context context;
    private final BarChart barChart;
    private final TextView txtRangoFechas;
    private final TextView txtEstadoCalidad;
    private final ImageView iconEstadoCalidad;
    private final TextView btnModoDia;
    private final TextView btnModoHora;
    private final RequestQueue queue;
    private final int idUsuario;

    // "dia" → /resumen7Dias   |   "hora" → /resumen8Horas
    private String modoActual = "dia";

    // Último tipo de gas usado (11,12,13,14). 0 = ninguno todavía.
    private int ultimoTipoGas = 0;


    public GraficaHelper(Context context,
                         BarChart barChart,
                         TextView txtRangoFechas,
                         TextView txtEstadoCalidad,
                         ImageView iconEstadoCalidad,
                         TextView btnModoDia,
                         TextView btnModoHora,
                         RequestQueue queue,
                         int idUsuario) {

        this.context = context;
        this.barChart = barChart;
        this.txtRangoFechas = txtRangoFechas;
        this.txtEstadoCalidad = txtEstadoCalidad;
        this.iconEstadoCalidad = iconEstadoCalidad;
        this.btnModoDia = btnModoDia;
        this.btnModoHora = btnModoHora;
        this.queue = queue;
        this.idUsuario = idUsuario;

        configurarChart();
        configurarBotonesModo();
        actualizarBotonesModo();
    }

    /**
     * Nombre Método: configurarChart
     * Descripción:
     *   Configura los aspectos visuales básicos del BarChart
     *   (sin datos todavía): ejes, rejilla, leyenda, etc.
     *
     * Autor: Alan Guevara Martínez
     * Fecha: 21/11/2025
     */
    private void configurarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setPinchZoom(false);
        barChart.setScaleEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        YAxis left = barChart.getAxisLeft();
        left.setDrawGridLines(true);
        left.setGridColor(Color.LTGRAY);
        left.setTextSize(10f);

        left.setAxisMinimum(0f);
        left.setAxisMaximum(4f);
        left.setGranularity(1f);       // <-- IMPORTANTE
        left.setGranularityEnabled(true);
        left.setLabelCount(5, true);   // 0,1,2,3,4

        left.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                if (value == 1f) return "Buena";
                if (value == 2f) return "Moderada";
                if (value == 3f) return "Insalubre";
                if (value == 4f) return "Mala";
                return "";
            }
        });


        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);
    }

    /**
     * Nombre Método: configurarBotonesModo
     * Descripción:
     *   Asigna los listeners a los botones "D" y "H" para cambiar
     *   entre modo día y modo hora. Cada vez que se pulsa,
     *   se vuelve a recargar la gráfica con el último gas usado.
     *
     * Autor: Alan Guevara Martínez
     * Fecha: 21/11/2025
     */
    private void configurarBotonesModo() {
        btnModoDia.setOnClickListener(v -> {
            modoActual = "dia";

            // Si ya tenemos algún gas cargado, recargamos la gráfica
            if (ultimoTipoGas != 0) {
                recargarGrafica(ultimoTipoGas);
            } else {
                // Si todavía no se ha cargado nada, solo actualizamos botones
                actualizarBotonesModo();
            }
        });

        btnModoHora.setOnClickListener(v -> {
            modoActual = "hora";

            if (ultimoTipoGas != 0) {
                recargarGrafica(ultimoTipoGas);
            } else {
                actualizarBotonesModo();
            }
        });
    }


    /**
     * Nombre Método: actualizarBotonesModo
     * Descripción:
     *   Cambia los fondos de los botones D/H según el modo activo.
     *
     * Autor: Alan Guevara Martínez
     * Fecha: 21/11/2025
     */
    private void actualizarBotonesModo() {
        if ("dia".equals(modoActual)) {
            btnModoDia.setBackgroundResource(R.drawable.bg_modo_selected);
            btnModoHora.setBackgroundResource(R.drawable.bg_modo_unselected);
            txtRangoFechas.setText("Últimos 7 días");
        } else {
            btnModoDia.setBackgroundResource(R.drawable.bg_modo_unselected);
            btnModoHora.setBackgroundResource(R.drawable.bg_modo_selected);
            txtRangoFechas.setText("Últimas 8 horas");
        }
    }

    /**
     * Nombre Método: recargarGrafica
     * Descripción:
     *   Llama a LogicaFake para obtener los datos de la gráfica según:
     *     - modoActual ("dia" o "hora")
     *     - idUsuario
     *     - tipoGas seleccionado
     *
     *   Una vez recibidos los datos, los pinta en el BarChart y
     *   actualiza la carita y el texto de estado.
     */
    public void recargarGrafica(int tipoGas) {
        // Guardamos qué gas se está usando, para poder recargarlo
        // cuando el usuario pulse D/H.
        this.ultimoTipoGas = tipoGas;

        // Actualizamos aspecto de botones y texto "Últimos 7 días / Últimas 8 horas"
        actualizarBotonesModo();

        LogicaFake.GraficaCallback callback = new LogicaFake.GraficaCallback() {
            @Override
            public void onSinPlaca() {
                // Si no hay placa, limpiamos la gráfica
                pintarGrafica(new ArrayList<>(), new ArrayList<>(), 0, tipoGas);
            }

            @Override
            public void onDatosObtenidos(List<String> labels, List<Float> valores, double promedio) {
                pintarGrafica(labels, valores, promedio, tipoGas);
            }

            @Override
            public void onErrorServidor() {
                // En caso de error, lo más seguro es no tocar nada o limpiar
            }

            @Override
            public void onErrorInesperado() {
                // Igual que arriba
            }
        };

        if ("dia".equals(modoActual)) {
            LogicaFake.resumen7Dias(idUsuario, tipoGas, queue, callback);
        } else {
            LogicaFake.resumen8Horas(idUsuario, tipoGas, queue, callback);
        }
    }

    /**
     * Nombre Método: pintarGrafica
     * Descripción:
     *   Recibe listas de labels y valores y las representa en el
     *   BarChart. Calcula también el promedio para actualizar la
     *   carita y el texto de estado de la gráfica.
     *
     * Entradas:
     *   - labels:  etiquetas del eje X (días u horas).
     *   - valores: valores numéricos de cada barra.
     *   - promedio: valor promedio general del periodo.
     *   - tipoGas: código del gas para evaluar rangos.
     *
     * Salidas:
     *   - No retorna nada. Solo actualiza la UI.
     *
     * Autor: Alan Guevara Martínez
     * Fecha: 21/11/2025
     */
    private void pintarGrafica(List<String> labels,
                               List<Float> valores,
                               double promedio,
                               int tipoGas) {

        // ---------------------------
        // Construcción de entradas (CON ALTURA FIJA POR CATEGORÍA)
        // ---------------------------
        List<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < valores.size(); i++) {
            float v = valores.get(i);

            // CONVERTIR valor real (0.01, 0.04...) a altura FIJA POR CATEGORÍA
            // Usamos 1, 2, 3, 4 para que coincida con el eje Y
            float altura;

            if (tipoGas == 13) {  // O3
                if (v <= 0.031f) altura = 1f;
                else if (v <= 0.061f) altura = 2f;
                else if (v <= 0.092f) altura = 3f;
                else altura = 4f;

            } else if (tipoGas == 11) { // NO2
                if (v <= 0.021f) altura = 1f;
                else if (v <= 0.053f) altura = 2f;
                else if (v <= 0.106f) altura = 3f;
                else altura = 4f;

            } else if (tipoGas == 12) { // CO
                if (v <= 1.7f) altura = 1f;
                else if (v <= 4.4f) altura = 2f;
                else if (v <= 8.7f) altura = 3f;
                else altura = 4f;

            } else if (tipoGas == 14) { // SO2
                if (v <= 0.0076f) altura = 1f;
                else if (v <= 0.019f) altura = 2f;
                else if (v <= 0.038f) altura = 3f;
                else altura = 4f;

            } else {
                altura = 0f;
            }

            entries.add(new BarEntry(i, altura));
        }



        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setDrawValues(false);

        // Colores por barra según valor del gas
        List<Integer> colores = new ArrayList<>();
        for (float v : valores) {
            colores.add(colorParaValor(v, tipoGas));
        }
        dataSet.setColors(colores);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        // Asignar datos al chart
        barChart.setData(data);

        // Eje X con labels
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelRotationAngle(0f);

        barChart.invalidate();

        // Actualizar carita y texto según promedio
        actualizarEstadoCalidad(promedio, tipoGas);
    }

    /**
     * Nombre Método: actualizarEstadoCalidad
     * Descripción:
     *   Determina el estado general (Buena, Moderada, Insalubre, Mala)
     *   a partir de un índice de calidad entre 0 y 1, y actualiza
     *   el texto y la carita.
     *
     *   0.00 – 0.25 → Buena       → cara_buena
     *   0.25 – 0.50 → Moderada    → cara_regular
     *   0.50 – 0.75 → Insalubre   → cara_regular
     *   0.75 – 1.00 → Mala        → cara_mala
     */
    private void actualizarEstadoCalidad(double promedio, int tipoGas) {

        String estado = "Sin datos";
        int icono = R.drawable.ic_estado_neutro;

        if (promedio <= 0) {
            txtEstadoCalidad.setText(estado);
            iconEstadoCalidad.setImageResource(icono);
            return;
        }

        switch (tipoGas) {

            case 13: // O3
                if (promedio <= 0.031) {
                    estado = "Buena";
                    icono = R.drawable.ic_cara_buena;
                } else if (promedio <= 0.061) {
                    estado = "Moderada";
                    icono = R.drawable.ic_cara_moderado;
                } else if (promedio <= 0.092) {
                    estado = "Insalubre";
                    icono = R.drawable.ic_cara_regular;
                } else {
                    estado = "Mala";
                    icono = R.drawable.ic_cara_mala;
                }
                break;

            case 11: // NO2
                if (promedio <= 0.021) {
                    estado = "Buena";
                    icono = R.drawable.ic_cara_buena;
                } else if (promedio <= 0.053) {
                    estado = "Moderada";
                    icono = R.drawable.ic_cara_moderado;
                } else if (promedio <= 0.106) {
                    estado = "Insalubre";
                    icono = R.drawable.ic_cara_regular;
                } else {
                    estado = "Mala";
                    icono = R.drawable.ic_cara_mala;
                }
                break;

            case 12: // CO
                if (promedio <= 1.7) {
                    estado = "Buena";
                    icono = R.drawable.ic_cara_buena;
                } else if (promedio <= 4.4) {
                    estado = "Moderada";
                    icono = R.drawable.ic_cara_moderado;
                } else if (promedio <= 8.7) {
                    estado = "Insalubre";
                    icono = R.drawable.ic_cara_regular;
                } else {
                    estado = "Mala";
                    icono = R.drawable.ic_cara_mala;
                }
                break;

            case 14: // SO2
                if (promedio <= 0.0076) {
                    estado = "Buena";
                    icono = R.drawable.ic_cara_buena;
                } else if (promedio <= 0.019) {
                    estado = "Moderada";
                    icono = R.drawable.ic_cara_moderado;
                } else if (promedio <= 0.038) {
                    estado = "Insalubre";
                    icono = R.drawable.ic_cara_regular;
                } else {
                    estado = "Mala";
                    icono = R.drawable.ic_cara_mala;
                }
                break;
        }

        txtEstadoCalidad.setText(estado);
        iconEstadoCalidad.setImageResource(icono);
    }



    /**
     * Nombre Método: colorParaValor
     * Descripción:
     *   Devuelve un color (verde, amarillo, naranja, rojo)
     *   según un índice de calidad entre 0 y 1.
     *
     *   0.00 – 0.25 → Buena  (verde)
     *   0.25 – 0.50 → Moderada (amarillo)
     *   0.50 – 0.75 → Insalubre (naranja)
     *   0.75 – 1.00 → Mala (rojo)
     *
     * Entradas:
     *   - valor: índice de calidad (0–1) para esa barra.
     *   - tipoGas: NO lo usamos aquí, pero lo dejamos por compatibilidad.
     */
    private int colorParaValor(float v, int tipoGas) {

        // O3
        if (tipoGas == 13) {
            if (v <= 0.031f) return Color.rgb(76, 175, 80);      // Buena
            else if (v <= 0.061f) return Color.rgb(255, 235, 59); // Moderada
            else if (v <= 0.092f) return Color.rgb(255, 152, 0);  // Insalubre
            else return Color.rgb(244, 67, 54);                   // Mala
        }

        // NO2
        if (tipoGas == 11) {
            if (v <= 0.021f) return Color.rgb(76, 175, 80);
            else if (v <= 0.053f) return Color.rgb(255, 235, 59);
            else if (v <= 0.106f) return Color.rgb(255, 152, 0);
            else return Color.rgb(244, 67, 54);
        }

        // CO
        if (tipoGas == 12) {
            if (v <= 1.7f) return Color.rgb(76, 175, 80);
            else if (v <= 4.4f) return Color.rgb(255, 235, 59);
            else if (v <= 8.7f) return Color.rgb(255, 152, 0);
            else return Color.rgb(244, 67, 54);
        }

        // SO2
        if (tipoGas == 14) {
            if (v <= 0.0076f) return Color.rgb(76, 175, 80);
            else if (v <= 0.019f) return Color.rgb(255, 235, 59);
            else if (v <= 0.038f) return Color.rgb(255, 152, 0);
            else return Color.rgb(244, 67, 54);
        }

        return Color.GRAY; // fallback
    }
}
