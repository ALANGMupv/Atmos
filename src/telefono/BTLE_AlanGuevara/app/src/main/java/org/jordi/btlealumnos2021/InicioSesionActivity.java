package org.jordi.btlealumnos2021;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

// Clase para manejar el inicio de sesión de usuarios ya registrados
public class InicioSesionActivity extends AppCompatActivity {

    // Campos de texto y botón de login
    private EditText emailCampo, contrasenyaCampo;
    private Button loginBoton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inicio_sesion);

        emailCampo = findViewById(R.id.login_email_tv);
        contrasenyaCampo = findViewById(R.id.login_contrasenya_tv);
        loginBoton = findViewById(R.id.login_btn);

        // Listener del botón Iniciar sesión
        loginBoton.setOnClickListener(v -> iniciarSesion());

        //Pulsar ojo para ver contraseña
        enablePasswordToggle(contrasenyaCampo);
    }

    // Metodo que valida y envía los datos de login
    private void iniciarSesion() {
        // Recoger texto de los campos
        String email = emailCampo.getText().toString().trim();
        String password = contrasenyaCampo.getText().toString().trim();

        // Validar que no estén vacíos
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar formato del correo
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Correo electrónico no válido", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    // Función para ver contraseña
    private void enablePasswordToggle(final EditText editText) {

        editText.setOnTouchListener((v, event) -> {

            if (event.getAction() != MotionEvent.ACTION_UP) return false;

            final int DRAWABLE_END = 2;
            Drawable drawableEnd = editText.getCompoundDrawables()[DRAWABLE_END];

            if (drawableEnd == null) return false;

            int width = drawableEnd.getBounds().width();
            int touchArea = editText.getWidth() - width - editText.getPaddingEnd();

            if (event.getX() >= touchArea) {

                int cursor = editText.getSelectionEnd();

                if (editText.getTransformationMethod() instanceof PasswordTransformationMethod) {
                    editText.setTransformationMethod(null); // mostrar
                } else {
                    editText.setTransformationMethod(PasswordTransformationMethod.getInstance()); // ocultar
                }

                editText.setSelection(cursor);
                return true;
            }

            return false;
        });
    }

}
