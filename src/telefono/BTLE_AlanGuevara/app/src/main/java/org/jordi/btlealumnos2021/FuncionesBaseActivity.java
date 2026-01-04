package org.jordi.btlealumnos2021;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Clase Base: FuncionesBaseActivity
 * Descripción:
 *   Contiene funciones comunes y reutilizables entre distintas Activities,
 *   como configurar el header, la barra de navegación inferior y utilidades
 *   compartidas (como el toggle de contraseña).
 *
 * Autora: Nerea Aguilar Forés
 */
public class FuncionesBaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Nombre Método: setupHeader
     * Descripción:
     *   Configura el header superior (toolbar) asignando el título
     *   y la funcionalidad de los iconos de perfil y notificaciones.
     *
     * Entradas:
     *  - titulo: Texto que se mostrará en el header.
     *
     * Autora: Nerea Aguilar Forés
     */
    protected void setupHeader(String titulo) {
        TextView tituloHeader = findViewById(R.id.tituloHeader);
        ImageView btnNotificaciones = findViewById(R.id.btnNotificaciones);
        ImageView btnPerfil = findViewById(R.id.btnPerfil);

        if (tituloHeader != null && titulo != null) {
            tituloHeader.setText(titulo);
        }

        if (btnNotificaciones != null) {
            btnNotificaciones.setOnClickListener(v -> {
                startActivity(new Intent(this, NotificacionesActivity.class));
            });
        }

        if (btnPerfil != null) {
            btnPerfil.setOnClickListener(v -> {
                startActivity(new Intent(this, PerfilActivity.class));
            });
        }
    }

    /**
     * Nombre Método: setupBottomNav
     * Descripción:
     *   Configura la barra de navegación inferior (Bottom Navigation),
     *   marcando el botón seleccionado según la pantalla actual.
     *
     *   selected:
     *      0 = Mapas
     *      1 = Usuario
     *      2 = Menú
     *
     * Entradas:
     *  - selected: índice del botón de navegación activo.
     *
     * Autora: Nerea Aguilar Forés
     */
    protected void setupBottomNav(int selected) {
        LinearLayout navMapas = findViewById(R.id.nav_mapas);
        LinearLayout navUser  = findViewById(R.id.nav_user);
        LinearLayout navMenu  = findViewById(R.id.nav_menu);

        if (navMapas != null) {
            if (selected == 0) navMapas.setAlpha(1f); else navMapas.setAlpha(0.5f);
            navMapas.setOnClickListener(v -> {
                if (!(this instanceof MapasActivity)) {
                    startActivity(new Intent(this, MapasActivity.class));
                    // finish(); // opcional
                }
            });
        }

        if (navUser != null) {
            if (selected == 1) navUser.setAlpha(1f); else navUser.setAlpha(0.5f);
            navUser.setOnClickListener(v -> {
                if (!(this instanceof UserPageActivity)) {
                    startActivity(new Intent(this, UserPageActivity.class));
                    // finish();
                }
            });
        }

        if (navMenu != null) {
            if (selected == 2) navMenu.setAlpha(1f); else navMenu.setAlpha(0.5f);
            navMenu.setOnClickListener(v -> {
                if (!(this instanceof MenuActivity)) {
                    startActivity(new Intent(this, MenuActivity.class));
                    // finish();
                }
            });
        }
    }

    /**
     * Nombre Método: habilitarToggleContrasena
     * Descripción:
     *   Permite alternar entre mostrar y ocultar la contraseña.
     *   Además cambia el icono del ojo (abierto/cerrado).
     *
     * Entradas:
     *  - editText: EditText con drawableEnd del ojo.
     */
    public void habilitarToggleContrasena(final EditText editText) {
        if (editText == null) return;

        editText.setOnTouchListener((v, event) -> {

            final int DRAWABLE_END = 2;
            Drawable drawableEnd = editText.getCompoundDrawables()[DRAWABLE_END];

            if (drawableEnd == null) return false;

            int width = drawableEnd.getBounds().width();
            int touchArea = editText.getWidth() - width - editText.getPaddingEnd();

            boolean tocadoIcono = event.getX() >= touchArea;

            if (!tocadoIcono) return false;

            int cursor = editText.getSelectionEnd();

            switch (event.getAction()) {

                // Pulsando → mostrar contraseña
                case MotionEvent.ACTION_DOWN:
                    editText.setTransformationMethod(null);
                    editText.setCompoundDrawablesWithIntrinsicBounds(
                            null, null,
                            getResources().getDrawable(R.drawable.ic_eye), // ojo abierto
                            null
                    );
                    editText.setSelection(cursor);
                    return true;

                // Soltando → ocultar contraseña
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    editText.setCompoundDrawablesWithIntrinsicBounds(
                            null, null,
                            getResources().getDrawable(R.drawable.ic_eye_close), // ojo cerrado
                            null
                    );
                    editText.setSelection(cursor);
                    return true;
            }

            return false;
        });
    }

    /**
     * Nombre Método: setupBottomNav
     * Descripción:
     *   Cuando la página en la que está el usuario no es ninguna de las de que está en el menú se utiliza este método.
     *   Nada seleccionado.
     */
    protected void setupBottomNav() {
        setupBottomNav(-1); // Indica que ningún botón debe aparecer como seleccionado
    }

}
