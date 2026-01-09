package org.jordi.btlealumnos2021;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * Pantallas de onboarding.
 * 3 páginas: Bienvenido / Conecta / Empieza ahora
 *
 */
public class ActividadInicio extends AppCompatActivity {

    private ViewPager2 viewPagerPaginas;
    private TextView botonRegresar, botonSiguiente, textoIniciarSesion;
    private Button botonComenzar;
    private List<PaginaInicio> paginas;
    private Preferencias preferencias;
    private AdaptadorPaginasInicio adaptador;
    private LinearLayout contenedorIniciarSesion;

    private TextView tvIrInicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_inicio);

        preferencias = new Preferencias(this);


        viewPagerPaginas = findViewById(R.id.viewPagerPaginas);
        botonRegresar = findViewById(R.id.botonRegresar);
        botonSiguiente = findViewById(R.id.botonSiguiente);
        botonComenzar = findViewById(R.id.botonComenzar);

        contenedorIniciarSesion = findViewById(R.id.contenedorIniciarSesion);
        tvIrInicio = findViewById(R.id.tvIrInicio);


        configurarPaginas();

        adaptador = new AdaptadorPaginasInicio(paginas);
        viewPagerPaginas.setAdapter(adaptador);

        botonRegresar.setOnClickListener(v -> {
            int pos = viewPagerPaginas.getCurrentItem();
            if (pos > 0) viewPagerPaginas.setCurrentItem(pos - 1);
        });

        botonSiguiente.setOnClickListener(v -> {
            int pos = viewPagerPaginas.getCurrentItem();
            if (pos < paginas.size() - 1) {
                viewPagerPaginas.setCurrentItem(pos + 1);
            }
        });

        botonComenzar.setOnClickListener(v -> finalizar());

        tvIrInicio.setOnClickListener(v -> {
            startActivity(new Intent(this, InicioSesionActivity.class));
            finish();
        });

        viewPagerPaginas.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                actualizarControles(position);
            }
        });
    }

    /**
     *
     * Configura el contenido de las páginas del onboarding.
     *
     */
    private void configurarPaginas() {
        paginas = new ArrayList<>();

        paginas.add(new PaginaInicio(
                R.drawable.onboarding1,
                "Bienvenido",
                "Desde el equipo de Atmos queremos que puedas visualizar el aire que respiras en cualquier momento."
        ));

        paginas.add(new PaginaInicio(
                R.drawable.onboarding2,
                "Conecta y participa",
                "Puedes usar ATMOS con o sin sensor.\nCon un sensor, contribuirás con mediciones reales; sin él, podrás explorar mapas y recibir alertas."
        ));

        paginas.add(new PaginaInicio(
                R.drawable.logo_atmos_verde,
                "Empieza ahora",
                "Accede a mapas, gráficas, alertas y consejos personalizados con tu cuenta ATMOS.\n\nÚnete a la comunidad que está cambiando el aire."
        ));
    }

    /**
     *
     * Muestra/oculta los botones según la página.
     *
     */
    private void actualizarControles(int pagina) {

        boolean primera = pagina == 0;
        boolean ultima = pagina == paginas.size() - 1;

        /**
         * REGRESAR:
         * - NO aparece en página 0/2
         * - SÍ aparece en página 1
         */
        if (primera || ultima) {
            botonRegresar.setVisibility(View.INVISIBLE);
        } else {
            botonRegresar.setVisibility(View.VISIBLE);
        }

        /**
         * SIGUIENTE:
         * - Solo aparece en páginas 0 y 1
         */
        botonSiguiente.setVisibility(ultima ? View.GONE : View.VISIBLE);

        /**
         * COMENZAR:
         * - Solo aparece en la última página
         */
        botonComenzar.setVisibility(ultima ? View.VISIBLE : View.GONE);

        /**
         * TEXTO INICIAR SESIÓN:
         * - Solo aparece en la última página
         */
        contenedorIniciarSesion.setVisibility(ultima ? View.VISIBLE : View.GONE);
    }


    /// Termina el onboarding y marca que ya se vio.
    private void finalizar() {
        preferencias.marcarNoPrimeraVez();
        startActivity(new Intent(this, RegistroActivity.class));
        finish();
    }
}

