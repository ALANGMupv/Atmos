package org.jordi.btlealumnos2021;

import android.os.Bundle;
import android.util.Patterns;
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

    // URL del endpoint Laravel (ajústala igual que en el registro)
    private static final String URL_LOGIN = "https://aguemar.upv.edu.es/laravel/public/api/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inicio_sesion); // Carga el diseño XML de login

        // Vincula los elementos visuales del XML con las variables
        emailCampo = findViewById(R.id.login_email_tv);
        contrasenyaCampo = findViewById(R.id.login_contrasenya_tv);
        loginBoton = findViewById(R.id.login_btn);

        // Listener del botón "Iniciar sesión"
        loginBoton.setOnClickListener(v -> iniciarSesion());
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

        // Crear la petición con Volley
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, URL_LOGIN,
                response -> {
                    // Si el servidor responde correctamente
                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();

                    // Añadir SharedPreferences para mantener la sesión iniciada
                },
                error -> {
                    // Si el servidor da error o no hay conexión
                    Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
                }) {

            // Datos que se mandan al servidor (Laravel los usará para validar)
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("contrasena", password);
                return params;
            }
        };

        // Enviar la petición
        queue.add(request);
    }
}
