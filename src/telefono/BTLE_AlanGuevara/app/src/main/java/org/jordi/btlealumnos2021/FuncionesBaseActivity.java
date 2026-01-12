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
 * @brief Activity base con funcionalidades comunes reutilizables.
 *
 * Proporciona métodos compartidos para configurar el header superior,
 * la barra de navegación inferior y utilidades generales como el
 * toggle de visibilidad de contraseñas.
 *
 * @author Nerea Aguilar Forés
 */
public class FuncionesBaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * @brief Configura el header superior de la pantalla.
     *
     * Asigna el título del header y define la funcionalidad de los iconos
     * de notificaciones y perfil.
     *
     * @param titulo Texto que se mostrará en el header.
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
     * @brief Configura la barra de navegación inferior.
     *
     * Marca el botón correspondiente como seleccionado según la pantalla
     * actual y define la navegación entre secciones.
     *
     * Valores de selección:
     * - 0 → Mapas
     * - 1 → Usuario
     * - 2 → Menú
     *
     * @param selected Índice del botón activo.
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
     * @brief Habilita la visualización temporal de la contraseña.
     *
     * Permite alternar entre mostrar y ocultar el contenido de un campo
     * de contraseña al pulsar el icono del ojo situado al final del EditText.
     *
     * @param editText Campo de texto que contiene la contraseña.
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
     * @brief Configura la barra de navegación inferior sin ningún botón seleccionado.
     *
     * Se utiliza cuando la pantalla actual no corresponde a ninguna de las
     * opciones principales del menú inferior.
     */
    protected void setupBottomNav() {
        setupBottomNav(-1); // Indica que ningún botón debe aparecer como seleccionado
    }

}
