package org.jordi.btlealumnos2021;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.Locale;

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
    private TextView txtUltima, txtPromedio;
    private Spinner spinner;

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
        // SPINNER
        // ---------------------------------------------------------------
        spinner = findViewById(R.id.spinnerContaminante);

        String[] gases = {"NO₂", "CO₂", "O₃"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                gases
        );

        spinner.setAdapter(adapter);


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
     * Nombre Método: onResume
     * Autor: Alan Guevara Martínez
     * Fecha: 18/11/2025
     */
    @Override
    protected void onResume() {
        super.onResume();
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
     *      Vuelve a consultar al servidor si el usuario tiene una placa vinculada
     *      y actualiza la interfaz (mostrar layoutSinSensor o layoutConSensor).
     *
     * Objetivo: Que tras vincular un sensor se recarge automáticamente el layout
     *
     * Autor: Alan Guevara Martínez
     * Fecha: 18/11/2025
     */
    private void recargarEstadoUsuario() {

        // Ocultar ambas mientras cargamos
        layoutSinSensor.setVisibility(View.GONE);
        layoutConSensor.setVisibility(View.GONE);

        int idUsuario = SesionManager.obtenerIdUsuario(this);

        // ---------------------------------------------------------------
        // CONSULTAR EN EL SERVIDOR EL RESUMEN DEL USUARIO
        // ---------------------------------------------------------------
        // Aquí preguntamos:
        //   - ¿Tiene una placa vinculada?
        //   - Si sí → ¿cuál es la última medida y el promedio?
        LogicaFake.resumenUsuario(
                idUsuario,
                queue,
                new LogicaFake.ResumenUsuarioCallback() {

                    // ----------------------------------------------------
                    // CASO 1: EL USUARIO NO TIENE PLACA VINCULADA
                    // ----------------------------------------------------
                    @Override
                    public void onSinPlaca() {
                        layoutConSensor.setVisibility(View.GONE); // Ocultar tarjeta con datos sensor
                        layoutSinSensor.setVisibility(View.VISIBLE); // mostrar pantalla "Ups..."
                    }

                    // ----------------------------------------------------
                    // CASO 2: EL USUARIO SÍ TIENE PLACA VINCULADA
                    // ----------------------------------------------------
                    @Override
                    public void onConPlaca(String placa, double ultima, double promedio) {


                        // Mostrar la tarjeta con datos del sensor
                        layoutConSensor.setVisibility(View.VISIBLE);
                        layoutSinSensor.setVisibility(View.GONE); // Ocultar "Ups..."

                        // Formateamos los decimales a dos cifras
                        txtUltima.setText(
                                String.format(Locale.US, "%.2f", ultima)
                        );

                        txtPromedio.setText(
                                String.format(Locale.US, "%.2f", promedio)
                        );
                    }

                    // ----------------------------------------------------
                    // CASO 3: ERROR CONTROLADO DESDE EL SERVIDOR
                    // ----------------------------------------------------
                    @Override
                    public void onErrorServidor() {
                        Toast.makeText(UserPageActivity.this,
                                "Error en el servidor", Toast.LENGTH_SHORT).show();
                    }

                    // ----------------------------------------------------
                    // CASO 4: ERROR NO ESPERADO
                    // ----------------------------------------------------
                    @Override
                    public void onErrorInesperado() {
                        Toast.makeText(UserPageActivity.this,
                                "Error inesperado", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}
