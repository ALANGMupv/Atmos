package org.jordi.btlealumnos2021;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Nombre fichero: UserPageActivity.java
 * Descripción: Pantalla "Mi sensor" del usuario. Muestra el saludo con el nombre del usuario,
 * detecta si tiene una placa vinculada y elige entre:
 * - Mostrar pantalla "Ups... no tienes sensor"
 * - Mostrar pantalla con últimas medidas y promedio del sensor
 * <p>
 * Entradas:
 * - Datos guardados en sesión: id_usuario, nombre, etc.
 * - Datos obtenidos del servidor mediante LogicaFake.resumenUsuario()
 * <p>
 * Salidas:
 * - Vista en pantalla según si el usuario tiene o no placa
 * - Valores de última medida y promedio en caso afirmativo
 * <p>
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

    ImageView ivEstadoSensor;
    TextView tvEstadoSensor;

    private GraficaHelper graficaHelper;

    // tipo de gas seleccionado en el spinner
    private int tipoSeleccionado = 13;

    // Instancia a la clase recorrido para no añadir más lineas de código aquí
    private RecorridoController recorridoController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_page);

        // Configurar header y bottom nav
        setupHeader("Mi sensor");
        setupBottomNav(1);

        queue = Volley.newRequestQueue(this);

        int idUsuario = SesionManager.obtenerIdUsuario(this);

        nombre = SesionManager.obtenerNombre(this);

        // ---------------------------------------------------------------
        // REFERENCIAS A LA TARJETA DE PROMEDIO Y ULTIMA
        // ---------------------------------------------------------------
        txtUltimaFecha = findViewById(R.id.tv_ultima_fecha);
        txtPromedioFecha = findViewById(R.id.tv_promedio_fecha);

        ivEstadoSensor = findViewById(R.id.iv_estado_sensor);
        tvEstadoSensor = findViewById(R.id.tv_estado_texto);


        txtEstadoUltima = findViewById(R.id.tv_ultima_gas);
        txtEstadoPromedio = findViewById(R.id.tv_promedio_gas);

        txtUltimaCalidad = findViewById(R.id.tv_ultima_calidad);
        txtPromedioCalidad = findViewById(R.id.tv_promedio_calidad);
        imgPromedioCalidad = findViewById(R.id.img_promedio_calidad);
        imgUltimaCalidad = findViewById(R.id.img_ultima_calidad);

        /* ------------ Distancia recorrida ------------ */
        Button btnIniciar = findViewById(R.id.btnIniciarRecorrido);
        Button btnDetener = findViewById(R.id.btnDetenerRecorrido);
        TextView tvRecorridoHoy = findViewById(R.id.tvRecorridoHoy);
        TextView tvRecorridoAyer = findViewById(R.id.tvRecorridoAyer);

        // Constructor de la clase RecorridoController
        recorridoController = new RecorridoController(
                this,
                btnIniciar,
                btnDetener,
                tvRecorridoHoy,
                tvRecorridoAyer
        );
        /* ------------ FIN - Distancia recorrida ------------ */

        // ---------------------------------------------------------------
        // REFERENCIAS DE LA TARJETA DE LA GRÁFICA
        // ---------------------------------------------------------------
        com.github.mikephil.charting.charts.BarChart barChart = findViewById(R.id.barChartCalidadAire);
        TextView txtRangoFechasGrafica = findViewById(R.id.txtRangoFechasGrafica);
        TextView txtEstadoCalidad = findViewById(R.id.txtEstadoCalidad);
        ImageView iconEstadoCalidad = findViewById(R.id.iconEstadoCalidad);
        TextView btnModoDia = findViewById(R.id.btnModoDia);
        TextView btnModoHora = findViewById(R.id.btnModoHora);

    // ---------------------------------------------------------------
    // CREAR HELPER DE GRÁFICA
    // ---------------------------------------------------------------
        graficaHelper = new GraficaHelper(
                this,
                barChart,
                txtRangoFechasGrafica,
                txtEstadoCalidad,
                iconEstadoCalidad,
                btnModoDia,
                btnModoHora,
                queue,
                idUsuario
        );

        // ACTIVAR TOOLTIP DE LA CARITA
        activarMensajeCarita(iconEstadoCalidad, txtEstadoCalidad);

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
        spinner.setSelection(2);

        // =========================================================
        // Cada vez que el usuario cambie el gas en el spinner,
        // refrescamos los valores automáticamente
        // =========================================================
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                recargarEstadoUsuario();     // Tarjetas
                if (graficaHelper != null) {
                    graficaHelper.recargarGrafica(tipoSeleccionado());
                };   // Gráfica

                int idUsuario = SesionManager.obtenerIdUsuario(UserPageActivity.this);

                // Estado del sensor
                actualizarEstadoSensor(idUsuario);

                // Señal del sensor
                actualizarEstadoSenal(idUsuario);

                // ======================================================
                // REFRESCO AUTOMÁTICO DEL ESTADO DEL SENSOR (20s)
                // ======================================================
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        actualizarEstadoSensor(idUsuario);
                        new android.os.Handler().postDelayed(this, 2000);
                    }
                }, 2000);

                // ======================================================
                // REFRESCO AUTOMÁTICO DEL ESTADO DE LA SEÑAL (5s)
                // ======================================================
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        actualizarEstadoSenal(idUsuario);
                        new android.os.Handler().postDelayed(this, 1000);
                    }
                }, 1000);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
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
        recargarEstadoUsuario();
        ;
    }

    /**
     * Nombre Método: onActivityResult
     * Descripción:
     * Método que recibe el resultado de una Activity que fue abierta
     * desde esta pantalla. En este caso, lo usamos para detectar cuándo
     * volvemos de la pantalla de "Vincular Sensor".
     * <p>
     * Si el resultado viene con el requestCode 999 y es RESULT_OK,
     * significa que se ha realizado algún cambio (como vincular/desvincular
     * un sensor) y entonces recargamos los datos del usuario.
     * <p>
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
     * Consulta al servidor si el usuario tiene una placa vinculada y,
     * en caso afirmativo, obtiene:
     * - la última medición del gas seleccionado
     * - el promedio del día del gas seleccionado
     * <p>
     * Este método usa el endpoint actualizado:
     * GET /resumenUsuarioPorGas?id_usuario=XX&tipo=YY
     * <p>
     * Entradas:
     * - No recibe parámetros. Usa:
     * tipoSeleccionado (gas del spinner)
     * SesionManager.obtenerIdUsuario()
     * <p>
     * Salidas:
     * - Actualiza la interfaz de usuario mostrando:
     * * layoutSinSensor → si NO tiene placa
     * * layoutConSensor → si SÍ tiene una placa
     * <p>
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

                        txtUltima.setText(String.format(Locale.US, "%.2f", ultimaValor));
                        txtPromedio.setText(String.format(Locale.US, "%.2f", promedio));

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

                        graficaHelper.recargarGrafica(tipoSeleccionado());

                        if (graficaHelper != null) {
                            graficaHelper.recargarGrafica(tipoSeleccionado());
                        }

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
     * Devuelve el texto "Buena", "Regular", "Mala" o "Sin datos"
     * según el valor medido y el tipo de gas seleccionado.
     * <p>
     * Entradas:
     * - valor: doble con la medida (última o promedio)
     * - tipoGas: código del gas (11 = NO2, 12 = CO, 13 = O3, 14 = SO2)
     * <p>
     * Salidas:
     * - String con el texto correspondiente al estado de calidad
     * <p>
     * Autora: Nerea Aguilar Forés
     */
    private String obtenerTextoCalidad(double valor, int tipoGas) {

        // Si no hay datos o es 0 → Sin datos
        if (valor <= 0) return "Sin datos";

        switch (tipoGas) {

            case 12: // CO
                if (valor <= 1.7) return "Buena";
                if (valor <= 4.4) return "Moderada";
                if (valor <= 8.7) return "Insalubre";
                return "Mala";

            case 11: // NO2
                if (valor <= 0.021) return "Buena";
                if (valor <= 0.053) return "Moderada";
                if (valor <= 0.106) return "Insalubre";
                return "Mala";

            case 13: // O3
                if (valor <= 0.031) return "Buena";
                if (valor <= 0.061) return "Moderada";
                if (valor <= 0.092) return "Insalubre";
                return "Mala";

            case 14: // SO2
                if (valor <= 0.0076) return "Buena";
                if (valor <= 0.019) return "Moderada";
                if (valor <= 0.038) return "Insalubre";
                return "Mala";
        }

        return "Sin datos";
    }

    /**
     * Nombre Método: obtenerIconoCalidad
     * Descripción:
     * Devuelve el recurso drawable del icono correspondiente
     * al estado de calidad: bueno, regular, malo o sin datos.
     * <p>
     * Entradas:
     * - valor: doble con la medida (última o promedio)
     * - tipoGas: código del gas (11–14)
     * <p>
     * Salidas:
     * - int con el identificador del drawable a usar
     * <p>
     * Autora: Nerea Aguilar Forés
     */
    private int obtenerIconoCalidad(double valor, int tipoGas) {

        // Sin datos → icono gris
        if (valor <= 0) return R.drawable.ic_estado_sin_datos;

        switch (tipoGas) {

            case 12: // CO
                if (valor <= 1.7) return R.drawable.ic_estado_bueno;
                if (valor <= 4.4) return R.drawable.ic_estado_moderado;
                if (valor <= 8.7) return R.drawable.ic_estado_insalubre;
                return R.drawable.ic_estado_malo;

            case 11: // NO2
                if (valor <= 0.021) return R.drawable.ic_estado_bueno;
                if (valor <= 0.053) return R.drawable.ic_estado_moderado;
                if (valor <= 0.106) return R.drawable.ic_estado_insalubre;
                return R.drawable.ic_estado_malo;

            case 13: // O3
                if (valor <= 0.031) return R.drawable.ic_estado_bueno;
                if (valor <= 0.061) return R.drawable.ic_estado_moderado;
                if (valor <= 0.092) return R.drawable.ic_estado_insalubre;
                return R.drawable.ic_estado_malo;

            case 14: // SO2
                if (valor <= 0.0076) return R.drawable.ic_estado_bueno;
                if (valor <= 0.019) return R.drawable.ic_estado_moderado;
                if (valor <= 0.038) return R.drawable.ic_estado_insalubre;
                return R.drawable.ic_estado_malo;
        }

        return R.drawable.ic_estado_sin_datos;
    }


    /**
     * Nombre Método: formatearFecha
     * Descripción:
     * Convierte una fecha en formato "yyyy-MM-dd HH:mm:ss"
     * (que viene del backend) a un formato más legible
     * como "dd/MM/yyyy".
     * <p>
     * Entradas:
     * - fechaISO: cadena de fecha recibida del servidor.
     * <p>
     * Salidas:
     * - Cadena formateada o "-" si falla.
     * <p>
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
            case 11:
                return "NO\u2082"; // NO₂
            case 12:
                return "CO";
            case 13:
                return "O\u2083"; // O₃
            case 14:
                return "SO\u2082"; // SO₂
            default:
                return "-";
        }
    }

    /**
     * Nombre Método: actualizarEstadoSensor
     * Descripción:
     *     Consulta la capa LogicaFake, que a su vez llama al endpoint
     *     GET /estadoPlaca para obtener si la placa asociada al usuario
     *     está activa, inactiva o si el usuario no tiene placa vinculada.
     *
     *     Según el estado recibido, actualiza la tarjeta izquierda en la UI:
     *         - Cambia el icono del sensor
     *         - Cambia el texto del estado
     *         - Cambia el color del texto (verde/rojo/gris)
     *
     * Entradas:
     *     - idUsuario : ID del usuario almacenado en sesión
     *
     * Salidas:
     *     - Actualiza ivEstadoSensor y tvEstadoSensor en pantalla
     *
     * Autora: Nerea Aguilar Forés
     * Fecha: 21/11/2025
     */
    private void actualizarEstadoSensor(int idUsuario) {

        LogicaFake.estadoPlacaServidor(idUsuario, queue, new LogicaFake.EstadoPlacaCallback() {

            @Override
            public void onActivo() {
                tvEstadoSensor.setText("Sensor activo");
                tvEstadoSensor.setTextColor(Color.parseColor("#2ECC71"));  // verde
                ivEstadoSensor.setImageResource(R.drawable.ic_sensor_activo);
            }

            @Override
            public void onInactivo() {
                tvEstadoSensor.setText("Sensor inactivo");
                tvEstadoSensor.setTextColor(Color.parseColor("#E74C3C"));  // rojo
                ivEstadoSensor.setImageResource(R.drawable.ic_sensor_inactivo);

                ImageView icono = findViewById(R.id.img_wifi);
                TextView texto = findViewById(R.id.tv_distancia);

                texto.setText("Sin datos");
                icono.setImageResource(R.drawable.ic_sin_senal);
            }

            @Override
            public void onSinPlaca() {
                tvEstadoSensor.setText("Sin placa asociada");
                tvEstadoSensor.setTextColor(Color.GRAY);
                ivEstadoSensor.setImageResource(R.drawable.ic_sensor_inactivo);
            }

            @Override
            public void onErrorServidor() {
                // No rompemos UI, solo opcionalmente log
                Log.e("UserPageActivity", "Error en estadoPlacaServidor()");
            }

            @Override
            public void onErrorInesperado() {
                Log.e("UserPageActivity", "Error inesperado en estadoPlacaServidor()");
            }
        });
    }

    /**
     * Nombre Método: actualizarEstadoSenal
     * Descripción:
     *     Consulta la capa LogicaFake, que llama al endpoint GET /estadoSenal
     *     para obtener el nivel de intensidad de señal del sensor del usuario.
     *
     *     Según el nivel recibido (fuerte, media, baja, mala o sin_datos),
     *     actualiza la tarjeta derecha en la pantalla:
     *         - Cambia el icono de señal
     *         - Cambia el texto descriptivo
     *
     * Entradas:
     *     - idUsuario : ID del usuario almacenado en sesión
     *
     * Salidas:
     *     - Actualiza img_wifi y tv_distancia en la interfaz de usuario
     *
     * Autora: Nerea Aguilar Forés
     * Fecha: 21/11/2025
     */
    private void actualizarEstadoSenal(int idUsuario) {

        LogicaFake.estadoSenalServidor(idUsuario, queue, new LogicaFake.EstadoSenalCallback() {

            @Override
            public void onResultado(String nivel, int rssi) {

                ImageView icono = findViewById(R.id.img_wifi);
                TextView texto = findViewById(R.id.tv_distancia);
                if (tvEstadoSensor.getText().toString().equals("Sensor inactivo")) {
                    icono.setImageResource(R.drawable.ic_sin_senal);
                    texto.setText("Sin datos");
                    return;
                }

                switch (nivel) {

                    case "fuerte":
                        texto.setText("Señal alta");
                        icono.setImageResource(R.drawable.ic_senal_alta);
                        break;

                    case "media":
                        texto.setText("Señal regular");
                        icono.setImageResource(R.drawable.ic_senal_media);
                        break;

                    case "baja":
                        texto.setText("Señal baja");
                        icono.setImageResource(R.drawable.ic_senal_baja);
                        break;

                    case "mala":
                        texto.setText("Señal muy baja");
                        icono.setImageResource(R.drawable.ic_sin_senal);
                        break;

                    default:
                        texto.setText("Sin datos");
                        icono.setImageResource(R.drawable.ic_sin_senal);
                        break;
                }
            }

            @Override
            public void onErrorServidor() {
                Log.e("UserPageActivity", "Error en estadoSenalServidor()");
            }

            @Override
            public void onErrorInesperado() {
                Log.e("UserPageActivity", "Error inesperado en estadoSenalServidor()");
            }
        });
    }

    // Mensajes caritas
    /**
     * Nombre Método: mensajePorCategoria
     * Descripción:
     *     Devuelve el mensaje descriptivo asociado a cada categoría de calidad
     *     del aire, similar a los tooltips usados en la versión web.
     *
     *     Los textos explican de forma sencilla el estado promedio de la calidad
     *     del aire según la categoría actual (Buena, Moderada, Insalubre o Mala).
     *
     * Entradas:
     *     - categoria : Texto que indica la calidad actual ("Buena", "Regular",
     *                   "Moderada", "Insalubre" o "Mala").
     *
     * Salidas:
     *     - String con el mensaje explicativo que debe mostrarse al usuario.
     *
     * Autora: Nerea Aguilar Forés
     * Fecha: 06/12/2025
     */
    private String mensajePorCategoria(String categoria) {

        switch (categoria) {

            case "Buena":
                return "En promedio, la calidad del aire es buena.";

            case "Moderada":
                return "En promedio, la calidad del aire es moderada. Las personas sensibles pueden notar molestias.";

            case "Insalubre":
                return "En promedio, la calidad del aire es insalubre con varios picos de contaminación.";

            case "Mala":
                return "En promedio, la calidad del aire es mala. Evita la exposición prolongada.";

            default:
                return "Aún no hay suficiente información para evaluar la calidad del aire.";
        }
    }

    /**
     * Nombre Método: activarMensajeCarita
     * Descripción:
     *     Muestra un mensaje flotante (PopupWindow) junto a la carita,
     *     sin bloquear la pantalla, imitando el estilo de la versión web.
     *
     *     El popup aparece cerca del icono, tiene fondo blanco
     *     con borde redondeado y se cierra automáticamente a los 3s.
     *
     * Entradas:
     *     - iconCarita : ImageView que el usuario toca
     *     - txtEstado  : Texto que contiene "Buena", "Mala", etc.
     *
     * Salidas:
     *     - Muestra un popup ligero estilo tooltip.
     *
     * Autor: Alan + ChatGPT
     * Fecha: 06/12/2025
     */
    private void activarMensajeCarita(ImageView iconCarita, TextView txtEstado) {

        iconCarita.setOnClickListener(v -> {

            String categoria = txtEstado.getText().toString().trim();
            String mensaje = mensajePorCategoria(categoria);

            // Inflar layout
            View popupView = getLayoutInflater().inflate(R.layout.mensaje_carita, null);

            TextView txt = popupView.findViewById(R.id.txtMensajeCarita);
            txt.setText(mensaje);

            // Medir popup antes
            popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int popupWidth = popupView.getMeasuredWidth();
            int popupHeight = popupView.getMeasuredHeight();

            // Crear PopupWindow
            final PopupWindow popupWindow = new PopupWindow(
                    popupView,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    false
            );

            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popupWindow.setOutsideTouchable(true);

            // Obtener posición ABSOLUTA del icono
            int[] loc = new int[2];
            iconCarita.getLocationOnScreen(loc);

            int iconX = loc[0];
            int iconY = loc[1];

            // -----------------------------
            // POSICIÓN PERFECTA:
            // A la izquierda, MISMA ALTURA
            // -----------------------------
            int popupX = iconX - popupWidth - 40;    // ajusta más o menos a la izquierda
            int popupY = iconY - (popupHeight / 2) + (iconCarita.getHeight() / 2);

            // Mostrar popup EXACTAMENTE ahí
            popupWindow.showAtLocation(iconCarita, Gravity.NO_GRAVITY, popupX, popupY);

            // Fade in
            popupView.setAlpha(0f);
            popupView.animate().alpha(1f).setDuration(150).start();

            // Auto cerrar
            new android.os.Handler().postDelayed(popupWindow::dismiss, 3000);
        });
    }

    /**
     * @brief Se ejecuta cuando la actividad es destruida.
     *
     * Libera los recursos utilizados por el controlador del recorrido
     * para evitar fugas de memoria antes de finalizar la actividad.
     *
     * @author Alan
     * @date 2025-12-17
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (recorridoController != null) {
            recorridoController.liberar();
        }
    }
}
