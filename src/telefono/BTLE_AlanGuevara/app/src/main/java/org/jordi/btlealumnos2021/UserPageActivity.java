package org.jordi.btlealumnos2021;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Nombre fichero: UserPageActivity.java
 * Descripción: Pantalla "Mi sensor" del usuario. Muestra el saludo con el nombre del usuario,
 *              detecta si tiene una placa vinculada y elige entre:
 *              - Mostrar pantalla "Ups... no tienes sensor"
 *              - Mostrar pantalla con últimas medidas y promedio del sensor
 *
 * Entradas:
 *   - Datos guardados en sesión: id_usuario, nombre, etc.
 *   - Datos obtenidos del servidor mediante LogicaFake.resumenUsuario()
 *
 * Salidas:
 *   - Vista en pantalla según si el usuario tiene o no placa
 *   - Valores de última medida y promedio en caso afirmativo
 *
 * Autora: Nerea Aguilar Forés
 */
public class UserPageActivity extends FuncionesBaseActivity {

    private String nombre;
    private TextView txtHolaUsuario;
    private RequestQueue queue;

    private LinearLayout layoutSinSensor, layoutConSensor;
    private TextView txtUltima, txtPromedio, txtUltimaFecha, txtPromedioFecha, txtEstadoUltima, txtEstadoPromedio;
    private TextView txtUltimaCalidad, txtPromedioCalidad;
    private ImageView imgUltimaCalidad, imgPromedioCalidad;
    private Spinner spinner;

    // --- CAMPOS PARA LA GRÁFICA ---
    private com.github.mikephil.charting.charts.BarChart barChartCalidadAire;
    private TextView txtRangoFechasGrafica;
    private TextView txtEstadoCalidad;
    private ImageView iconEstadoCalidad;
    private TextView btnModoDia;
    private TextView btnModoHora;

    // modo actual de la gráfica ("dia" o "hora")
    private String modoGrafica = "dia";

    // tipo de gas seleccionado en el spinner
    private int tipoSeleccionado = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_page);

        // Configurar header y bottom nav
        setupHeader("Mi sensor");
        setupBottomNav(1);

        queue = Volley.newRequestQueue(this);

        nombre = SesionManager.obtenerNombre(this);

        // ---------------------------------------------------------------
        // REFERENCIAS A LA TARJETA DE LA GRÁFICA
        // ---------------------------------------------------------------
        barChartCalidadAire = findViewById(R.id.barChartCalidadAire);
        txtRangoFechasGrafica = findViewById(R.id.txtRangoFechasGrafica);
        txtEstadoCalidad = findViewById(R.id.txtEstadoCalidad);
        txtUltimaFecha = findViewById(R.id.tv_ultima_fecha);
        txtPromedioFecha = findViewById(R.id.tv_promedio_fecha);
        iconEstadoCalidad = findViewById(R.id.iconEstadoCalidad);
        btnModoDia = findViewById(R.id.btnModoDia);
        btnModoHora = findViewById(R.id.btnModoHora);
        txtEstadoUltima = findViewById(R.id.tv_ultima_gas);
        txtEstadoPromedio = findViewById(R.id.tv_promedio_gas);

        txtUltimaCalidad = findViewById(R.id.tv_ultima_calidad);
        txtPromedioCalidad = findViewById(R.id.tv_promedio_calidad);
        imgPromedioCalidad = findViewById(R.id.img_promedio_calidad);
        imgUltimaCalidad = findViewById(R.id.img_ultima_calidad);


        // Modo por defecto: día seleccionado
        actualizarModoGrafica("dia");

        // ---------------------------------------------------------------
        // BOTONES D / H PARA CAMBIAR EL MODO DE LA GRÁFICA
        // ---------------------------------------------------------------
        btnModoDia.setOnClickListener(v -> actualizarModoGrafica("dia"));
        btnModoHora.setOnClickListener(v -> actualizarModoGrafica("hora"));

        // ---------------------------------------------------------------
        // SPINNER
        // ---------------------------------------------------------------
        spinner = findViewById(R.id.spinnerContaminante);

        String[] gases = {"NO₂", "CO", "O₃", "SO₃"};

        /*
        11 = NO2
        12 = CO
        13 = O3
        14 = SO3
        */
        // Códigos reales en BD (tabla medida.tipo)
        int[] tiposGases = {11, 12, 13, 14};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                gases
        );

        spinner.setAdapter(adapter);

        // =========================================================
        // Cada vez que el usuario cambie el gas en el spinner,
        // refrescamos los valores automáticamente
        // =========================================================
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                recargarEstadoUsuario();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        // ---------------------------------------------------------------
        // POPUP
        // ---------------------------------------------------------------
        ImageView iconoInfo = findViewById(R.id.iconoInfo);

        iconoInfo.setOnClickListener(v -> {
            Intent intent = new Intent(UserPageActivity.this, InfoPopupActivity.class);
            startActivity(intent);
        });

        // ---------------------------------------------------------------
        // MOSTRAR ¡HOLA, USUARIO!
        // ---------------------------------------------------------------
        txtHolaUsuario = findViewById(R.id.textBienvenida);
        txtHolaUsuario.setText("¡Hola, " + nombre + "!");

        // ---------------------------------------------------------------
        // OBTENER REFERENCIAS A LOS DOS LAYOUTS:
        // - layoutSinSensor → la vista "Ups..."
        // - layoutConSensor → los datos reales del sensor
        // ---------------------------------------------------------------
        layoutSinSensor = findViewById(R.id.layoutSinSensor);
        layoutConSensor = findViewById(R.id.layoutConSensor);

        txtUltima = findViewById(R.id.tv_ultima_valor);
        txtPromedio = findViewById(R.id.tv_promedio_valor);

        TextView txtUltimaFecha = findViewById(R.id.tv_ultima_fecha);
        TextView txtPromedioFecha = findViewById(R.id.tv_promedio_fecha);


        // Ocultamos ambas vistas por defecto
        layoutSinSensor.setVisibility(View.GONE);
        layoutConSensor.setVisibility(View.GONE);

        // Redirige a la página de vincular sensor, cuando no hay un sensor vinculado a ese usario
        Button ir_a_vincular = findViewById(R.id.btnVincularSensor);

        ir_a_vincular.setOnClickListener(v -> {
            Intent intent = new Intent(UserPageActivity.this, VincularSensorActivity.class);
            startActivityForResult(intent, 999);
        });

        // La lógica que establa implementada aquí, está ahora en un método
        recargarEstadoUsuario();
    }

    /**
     * Nombre Método: tipoSeleccionado
     * Descripción: Este método devuelve el tipo correcto según la posición del spinner
     * Autor: Alan Guevara Martínez
     * Fecha: 19/11/2025
     */
    private int tipoSeleccionado() {

        int pos = spinner.getSelectedItemPosition();

        switch (pos) {

            case 0:
                return 11; // NO2

            case 1:
                return 12; // CO

            case 2:
                return 13; // O3

            case 3:
                return 14; // SO3

            default:
                return 11; // Valor seguro
        }
    }


    /**
     * Nombre Método: onResume
     * Autor: Alan Guevara Martínez
     * Fecha: 18/11/2025
     */
    @Override
    protected void onResume() {
        super.onResume();
        recargarEstadoUsuario();;
    }

    /**
     * Nombre Método: onActivityResult
     * Descripción:
     *      Método que recibe el resultado de una Activity que fue abierta
     *      desde esta pantalla. En este caso, lo usamos para detectar cuándo
     *      volvemos de la pantalla de "Vincular Sensor".
     *
     *      Si el resultado viene con el requestCode 999 y es RESULT_OK,
     *      significa que se ha realizado algún cambio (como vincular/desvincular
     *      un sensor) y entonces recargamos los datos del usuario.
     *
     * Autor: Alan Guevara Martínez
     * Fecha: 18/11/2025
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Llamamos al método padre para mantener el comportamiento estándar de Android
        super.onActivityResult(requestCode, resultCode, data);

        // Verifica si el resultado proviene de la Activity con código 999
        // y si terminó correctamente con RESULT_OK
        if (requestCode == 999 && resultCode == RESULT_OK) {

            // Recarga el estado del usuario para refrescar datos en pantalla
            recargarEstadoUsuario();
        }
    }

    /**
     * Nombre Método: recargarEstadoUsuario
     * Descripción:
     *      Consulta al servidor si el usuario tiene una placa vinculada y,
     *      en caso afirmativo, obtiene:
     *          - la última medición del gas seleccionado
     *          - el promedio del día del gas seleccionado
     *
     *      Este método usa el endpoint actualizado:
     *          GET /resumenUsuarioPorGas?id_usuario=XX&tipo=YY
     *
     * Entradas:
     *      - No recibe parámetros. Usa:
     *          tipoSeleccionado (gas del spinner)
     *          SesionManager.obtenerIdUsuario()
     *
     * Salidas:
     *      - Actualiza la interfaz de usuario mostrando:
     *          * layoutSinSensor → si NO tiene placa
     *          * layoutConSensor → si SÍ tiene una placa
     *
     * Autora: Nerea Aguilar Forés
     */
    private void recargarEstadoUsuario() {

        layoutSinSensor.setVisibility(View.GONE);
        layoutConSensor.setVisibility(View.GONE);

        int idUsuario = SesionManager.obtenerIdUsuario(this);

        LogicaFake.resumenUsuarioPorGas(
                idUsuario,
                tipoSeleccionado(),   // ← GAS SELECCIONADO
                queue,
                new LogicaFake.ResumenUsuarioCallback() {

                    // ----------------------------------------------------
                    // CASO 1: EL USUARIO NO TIENE PLACA
                    // ----------------------------------------------------
                    @Override
                    public void onSinPlaca() {
                        layoutConSensor.setVisibility(View.GONE);
                        layoutSinSensor.setVisibility(View.VISIBLE);
                    }

                    // ----------------------------------------------------
                    // CASO 2: EL USUARIO TIENE PLACA ASIGNADA
                    // ----------------------------------------------------
                    @Override
                    public void onConPlaca(String placa, double ultimaValor, String ultimaFecha, double promedio) {

                        Log.d("RESUMEN", "ultimaFecha cruda = '" + ultimaFecha + "'");

                        layoutConSensor.setVisibility(View.VISIBLE);
                        layoutSinSensor.setVisibility(View.GONE);

                        txtUltima.setText(String.format(Locale.US, "%.3f", ultimaValor));
                        txtPromedio.setText(String.format(Locale.US, "%.3f", promedio));

                        txtUltimaFecha.setText(formatearFecha(ultimaFecha));
                        txtPromedioFecha.setText("Hoy");

                        String simbolo = simboloGas(tipoSeleccionado);
                        txtEstadoUltima.setText(simbolo + "\nppm");
                        txtEstadoPromedio.setText(simbolo + "\nppm");

                        // ----------------------------------------------------
                        // ACTUALIZAR ESTADO DE CALIDAD EN LAS TARJETAS
                        // ----------------------------------------------------
                        //
                        // Última medición
                        String textoCalidadUltima = obtenerTextoCalidad(ultimaValor, tipoSeleccionado);
                        int iconoCalidadUltima = obtenerIconoCalidad(ultimaValor, tipoSeleccionado);

                        // Actualizar UI de la tarjeta de última medición
                        txtUltimaCalidad.setText(textoCalidadUltima);
                        imgUltimaCalidad.setImageResource(iconoCalidadUltima);

                        // Promedio del día
                        String textoCalidadProm = obtenerTextoCalidad(promedio, tipoSeleccionado);
                        int iconoCalidadProm = obtenerIconoCalidad(promedio, tipoSeleccionado);

                        // Actualizar UI de la tarjeta de promedio
                        txtPromedioCalidad.setText(textoCalidadProm);
                        imgPromedioCalidad.setImageResource(iconoCalidadProm);



                        cargarDatosGrafica();
                    }

                    // ----------------------------------------------------
                    // CASO 3: ERROR CONTROLADO
                    // ----------------------------------------------------
                    @Override
                    public void onErrorServidor() {
                        Toast.makeText(UserPageActivity.this,
                                "Error en el servidor", Toast.LENGTH_SHORT).show();
                    }

                    // ----------------------------------------------------
                    // CASO 4: ERROR INESPERADO
                    // ----------------------------------------------------
                    @Override
                    public void onErrorInesperado() {
                        Toast.makeText(UserPageActivity.this,
                                "Error inesperado", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * Nombre Método: obtenerTextoCalidad
     * Descripción:
     *      Devuelve el texto "Buena", "Regular", "Mala" o "Sin datos"
     *      según el valor medido y el tipo de gas seleccionado.
     *
     * Entradas:
     *      - valor: doble con la medida (última o promedio)
     *      - tipoGas: código del gas (11 = NO2, 12 = CO, 13 = O3, 14 = SO2)
     *
     * Salidas:
     *      - String con el texto correspondiente al estado de calidad
     *
     * Autora: Nerea Aguilar Forés
     */
    private String obtenerTextoCalidad(double valor, int tipoGas) {

        // Si no hay datos o es 0 → Sin datos
        if (valor <= 0) return "Sin datos";

        switch (tipoGas) {

            case 12: // CO
                if (valor <= 1.7)  return "Buena";
                if (valor <= 4.4)  return "Regular";
                return "Mala";

            case 11: // NO2
                if (valor <= 0.021) return "Buena";
                if (valor <= 0.053) return "Regular";
                return "Mala";

            case 13: // O3
                if (valor <= 0.031) return "Buena";
                if (valor <= 0.061) return "Regular";
                return "Mala";

            case 14: // SO2
                if (valor <= 0.0076) return "Buena";
                if (valor <= 0.019)  return "Regular";
                return "Mala";
        }

        return "Sin datos";
    }

    /**
     * Nombre Método: obtenerIconoCalidad
     * Descripción:
     *      Devuelve el recurso drawable del icono correspondiente
     *      al estado de calidad: bueno, regular, malo o sin datos.
     *
     * Entradas:
     *      - valor: doble con la medida (última o promedio)
     *      - tipoGas: código del gas (11–14)
     *
     * Salidas:
     *      - int con el identificador del drawable a usar
     *
     * Autora: Nerea Aguilar Forés
     */
    private int obtenerIconoCalidad(double valor, int tipoGas) {

        // Sin datos → icono gris
        if (valor <= 0) return R.drawable.ic_estado_sin_datos;

        switch (tipoGas) {

            case 12: // CO
                if (valor <= 1.7)  return R.drawable.ic_estado_bueno;
                if (valor <= 4.4)  return R.drawable.ic_estado_regular;
                return R.drawable.ic_estado_malo;

            case 11: // NO2
                if (valor <= 0.021) return R.drawable.ic_estado_bueno;
                if (valor <= 0.053) return R.drawable.ic_estado_regular;
                return R.drawable.ic_estado_malo;

            case 13: // O3
                if (valor <= 0.031) return R.drawable.ic_estado_bueno;
                if (valor <= 0.061) return R.drawable.ic_estado_regular;
                return R.drawable.ic_estado_malo;

            case 14: // SO2
                if (valor <= 0.0076) return R.drawable.ic_estado_bueno;
                if (valor <= 0.019)  return R.drawable.ic_estado_regular;
                return R.drawable.ic_estado_malo;
        }

        return R.drawable.ic_estado_sin_datos;
    }



    /**
     * Nombre Método: formatearFecha
     * Descripción:
     *      Convierte una fecha en formato "yyyy-MM-dd HH:mm:ss"
     *      (que viene del backend) a un formato más legible
     *      como "dd/MM/yyyy".
     *
     * Entradas:
     *      - fechaISO: cadena de fecha recibida del servidor.
     *
     * Salidas:
     *      - Cadena formateada o "-" si falla.
     *
     * Autora: Nerea Aguilar Forés
     */
    private String formatearFecha(String fechaISO) {

        if (fechaISO == null || fechaISO.isEmpty()) {
            return "-";
        }

        try {
            // Formato que llega del servidor: 2025-11-19T19:12:00.000Z
            SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            iso.setTimeZone(TimeZone.getTimeZone("UTC")); // muy importante

            Date fecha = iso.parse(fechaISO);

            // Formato deseado: 19/11/2025 - 20:12
            SimpleDateFormat bonito = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
            bonito.setTimeZone(TimeZone.getDefault());

            return bonito.format(fecha);

        } catch (Exception e) {
            e.printStackTrace();
            return "-";
        }
    }

    /**
     * Devuelve el símbolo del gas según el tipo (11–14)
     * Autora: Nerea Aguilar Forés
     */
    private String simboloGas(int tipoGas) {
        switch (tipoGas) {
            case 11: return "NO\u2082"; // NO₂
            case 12: return "CO";
            case 13: return "O\u2083"; // O₃
            case 14: return "SO\u2082"; // SO₂
            default: return "-";
        }
    }




    /**
     * Nombre Método: actualizarModoGrafica
     * Descripción:
     *      Cambia el modo de visualización de la gráfica entre:
     *      - "dia"  → promedio por día (últimos 7 días)
     *      - "hora" → promedio por hora (día actual)
     *
     *      Además actualiza el aspecto visual de los botones D/H
     *      para que se vea cuál está seleccionado.
     *
     * Entradas:
     *      - nuevoModo: Cadena con el valor "dia" o "hora"
     *
     * Salidas:
     *      - No retorna nada. Actualiza variables de la Activity
     *        y la interfaz de usuario.
     *
     * Autora: Nerea Aguilar Forés
     */
    private void actualizarModoGrafica(String nuevoModo) {

        // Guardamos el modo actual
        modoGrafica = nuevoModo;

        // Cambiamos el fondo de los botones según el modo
        if ("dia".equals(nuevoModo)) {
            btnModoDia.setBackgroundResource(R.drawable.bg_modo_selected);
            btnModoHora.setBackgroundResource(R.drawable.bg_modo_unselected);
        } else {
            btnModoDia.setBackgroundResource(R.drawable.bg_modo_unselected);
            btnModoHora.setBackgroundResource(R.drawable.bg_modo_selected);
        }

        cargarDatosGrafica();
    }

    /**
     * Nombre Método: actualizarEstadoCalidad
     * Descripción:
     *      Determina el estado de la calidad del aire (buena, regular o mala)
     *      según el promedio de las medidas visibles y el tipo de gas seleccionado.
     *
     *      Cambia dinámicamente:
     *          - El texto mostrado al usuario
     *          - El icono de carita (verde, naranja o rojo)
     *
     * Entradas:
     *      - promedio: valor promedio calculado de la gráfica
     *      - tipoGas: código del gas (11=NO2, 12=CO, 13=O3, 14=SO2)
     *
     * Salidas:
     *      - No retorna nada. Actualiza la interfaz de usuario.
     *
     * Autora: Nerea Aguilar Forés
     */
    private void actualizarEstadoCalidad(double promedio, int tipoGas) {

        String estado = "";
        int icono = R.drawable.ic_estado_neutro; // Por si algo falla

        // ---------------------------------------------------------
        // Evaluar rangos según el tipo de gas
        // ---------------------------------------------------------
        switch (tipoGas) {

            case 12: // CO
                if (promedio <= 1.7) {
                    estado = "Calidad de aire buena en promedio.";
                    icono = R.drawable.ic_cara_buena;
                } else if (promedio <= 4.4) {
                    estado = "Calidad de aire regular en promedio.";
                    icono = R.drawable.ic_cara_regular;
                } else {
                    estado = "Calidad de aire mala en promedio.";
                    icono = R.drawable.ic_cara_mala;
                }
                break;

            case 11: // NO2
                if (promedio <= 0.021) {
                    estado = "Calidad de aire buena en promedio.";
                    icono = R.drawable.ic_cara_buena;
                } else if (promedio <= 0.053) {
                    estado = "Calidad de aire regular en promedio.";
                    icono = R.drawable.ic_cara_regular;
                } else {
                    estado = "Calidad de aire mala en promedio.";
                    icono = R.drawable.ic_cara_mala;
                }
                break;

            case 13: // O3
                if (promedio <= 0.031) {
                    estado = "Calidad de aire buena en promedio.";
                    icono = R.drawable.ic_cara_buena;
                } else if (promedio <= 0.061) {
                    estado = "Calidad de aire regular en promedio.";
                    icono = R.drawable.ic_cara_regular;
                } else {
                    estado = "Calidad de aire mala en promedio.";
                    icono = R.drawable.ic_cara_mala;
                }
                break;

            case 14: // SO2
                if (promedio <= 0.0076) {
                    estado = "Calidad de aire buena en promedio.";
                    icono = R.drawable.ic_cara_buena;
                } else if (promedio <= 0.019) {
                    estado = "Calidad de aire regular en promedio.";
                    icono = R.drawable.ic_cara_regular;
                } else {
                    estado = "Calidad de aire mala en promedio.";
                    icono = R.drawable.ic_cara_mala;
                }
                break;
        }

        // ---------------------------------------------------------
        // Actualizar la UI en pantalla
        // ---------------------------------------------------------
        txtEstadoCalidad.setText(estado);
        iconEstadoCalidad.setImageResource(icono);
    }

    /**
     * Nombre Método: cargarDatosGrafica
     * Descripción:
     *      Carga los datos necesarios para pintar la gráfica de calidad del aire.
     *      De momento usa datos fake ya que el backend aún no existe.
     *      Cuando el backend esté listo, solo será necesario sustituir
     *      la parte donde se crean los valores simulados.
     *
     * Entradas:
     *      - No recibe parámetros. Usa las variables globales:
     *          modoGrafica ("dia" o "hora")
     *          tipoSeleccionado (11-14)
     *
     * Salidas:
     *      - Actualiza la gráfica en pantalla
     *
     * Autora: Nerea Aguilar Forés
     */
    private void cargarDatosGrafica() {

        // ---------------------------
        // DATOS FAKE TEMPORALES
        // ---------------------------
        List<String> labels = new ArrayList<>();
        List<Float> valores = new ArrayList<>();

        if (modoGrafica.equals("dia")) {
            // Últimos 7 días (fake)
            labels.add("Lun");   valores.add(0.10f);
            labels.add("Mar");   valores.add(0.22f);
            labels.add("Mié");   valores.add(0.90f);
            labels.add("Jue");   valores.add(0.40f);
            labels.add("Vie");   valores.add(0.18f);
            labels.add("Sáb");   valores.add(0.06f);
            labels.add("Dom");   valores.add(0.09f);

            txtRangoFechasGrafica.setText("17/11 al 23/11");

        } else {
            // 8 últimas horas (fake)
            labels.add("08:00"); valores.add(0.02f);
            labels.add("09:00"); valores.add(0.04f);
            labels.add("10:00"); valores.add(0.05f);
            labels.add("11:00"); valores.add(0.20f);
            labels.add("12:00"); valores.add(0.17f);
            labels.add("13:00"); valores.add(0.06f);
            labels.add("14:00"); valores.add(0.03f);

            txtRangoFechasGrafica.setText("Hoy");
        }

        // ---------------------------
        // CONSTRUCCIÓN DE LA GRÁFICA
        // ---------------------------
        List<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < valores.size(); i++) {
            entries.add(new BarEntry(i, valores.get(i)));
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setDrawValues(false); // quitar números encima

        // Colores según promedio INDIVIDUAL
        List<Integer> colores = new ArrayList<>();
        for (float v : valores) {
            colores.add(colorParaValor(v, tipoSeleccionado));
        }
        dataSet.setColors(colores);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);

        barChartCalidadAire.setData(data);
        barChartCalidadAire.getDescription().setEnabled(false);
        barChartCalidadAire.getXAxis().setGranularity(1f);

        // Eje X → etiquetas (días o horas)
        XAxis xAxis = barChartCalidadAire.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        // Eje Y
        YAxis left = barChartCalidadAire.getAxisLeft();
        left.setDrawGridLines(true);
        left.setGridColor(Color.LTGRAY);
        left.setTextSize(10f);
        barChartCalidadAire.getAxisRight().setEnabled(false);

        barChartCalidadAire.invalidate();

        // ---------------------------
        // PROMEDIO GENERAL
        //----------------------------
        double suma = 0;
        for (float v : valores) suma += v;
        double promedio = suma / valores.size();

        // Cambiar la carita y texto
        actualizarEstadoCalidad(promedio, tipoSeleccionado);
    }

    /**
     * Nombre Método: colorParaValor
     * Descripción:
     *      Determina el color verde / naranja / rojo para una barra
     *      según el valor promedio individual y el gas.
     *
     * Entradas:
     *      - valor: valor promedio de la barra
     *      - tipoGas: código del gas (11–14)
     *
     * Salidas:
     *      - int con el color correspondiente
     *
     * Autora: Nerea Aguilar Forés
     */
    private int colorParaValor(float valor, int tipoGas) {

        switch (tipoGas) {

            case 12: // CO
                if (valor <= 1.7f) return Color.rgb(76, 175, 80);     // verde
                if (valor <= 4.4f) return Color.rgb(255, 152, 0);     // naranja
                return Color.rgb(244, 67, 54);                       // rojo

            case 11: // NO2
                if (valor <= 0.021f) return Color.rgb(76, 175, 80);
                if (valor <= 0.053f) return Color.rgb(255, 152, 0);
                return Color.rgb(244, 67, 54);

            case 13: // O3
                if (valor <= 0.031f) return Color.rgb(76, 175, 80);
                if (valor <= 0.061f) return Color.rgb(255, 152, 0);
                return Color.rgb(244, 67, 54);

            case 14: // SO2
                if (valor <= 0.0076f) return Color.rgb(76, 175, 80);
                if (valor <= 0.019f) return Color.rgb(255, 152, 0);
                return Color.rgb(244, 67, 54);
        }

        return Color.GRAY;
    }

}
