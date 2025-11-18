package org.jordi.btlealumnos2021;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
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
        // MOSTRAR ¡HOLA, USUARIO!
        // ---------------------------------------------------------------
        txtHolaUsuario = findViewById(R.id.textBienvenida);
        txtHolaUsuario.setText("¡Hola, " + nombre + "!");

        // ---------------------------------------------------------------
        // OBTENER REFERENCIAS A LOS DOS LAYOUTS:
        // - layoutSinSensor → la vista "Ups..."
        // - layoutConSensor → los datos reales del sensor
        // ---------------------------------------------------------------
        LinearLayout layoutSinSensor = findViewById(R.id.layoutSinSensor);
        LinearLayout layoutConSensor = findViewById(R.id.layoutConSensor);

        TextView txtUltima = findViewById(R.id.tv_ultima_valor);
        TextView txtPromedio = findViewById(R.id.tv_promedio_valor);
        TextView txtUltimaFecha = findViewById(R.id.tv_ultima_fecha);
        TextView txtPromedioFecha = findViewById(R.id.tv_promedio_fecha);


        // Ocultamos ambas vistas por defecto
        layoutSinSensor.setVisibility(View.GONE);
        layoutConSensor.setVisibility(View.GONE);

        // ---------------------------------------------------------------
        // PEDIR DATOS AL SERVIDOR: ¿TIENE USUARIO UNA PLACA?
        // ---------------------------------------------------------------
        int idUsuario = SesionManager.obtenerIdUsuario(this);

        LogicaFake.resumenUsuario(
                idUsuario,
                queue,
                new LogicaFake.ResumenUsuarioCallback() {

                    @Override
                    public void onSinPlaca() {
                        layoutSinSensor.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onConPlaca(String placa, double ultima, double promedio) {
                        layoutConSensor.setVisibility(View.VISIBLE);

                        txtUltima.setText("Última medida: " +
                                String.format(Locale.US, "%.2f", ultima));

                        txtPromedio.setText("Promedio: " +
                                String.format(Locale.US, "%.2f", promedio));
                    }

                    @Override
                    public void onErrorServidor() {
                        Toast.makeText(UserPageActivity.this,
                                "Error en el servidor", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onErrorInesperado() {
                        Toast.makeText(UserPageActivity.this,
                                "Error inesperado", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}
