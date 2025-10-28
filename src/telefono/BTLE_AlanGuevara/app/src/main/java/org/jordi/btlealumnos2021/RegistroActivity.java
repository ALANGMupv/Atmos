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

// Clase que maneja el registro de nuevos usuarios
public class RegistroActivity extends AppCompatActivity {

    // Campos del formulario y el botón
    private EditText nombreCampo, apellidosCampo, emailCampo, contrasenyaCampo, contrasenyaRepCampo;
    private Button registroBoton;

    // URL del endpoint Laravel (ajústala según dónde instales la API)
    private static final String URL_REGISTER = "https://aguemar.upv.edu.es/api/register";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registro);

        nombreCampo = findViewById(R.id.nombre_tv);
        apellidosCampo = findViewById(R.id.apellidos_tv);
        emailCampo = findViewById(R.id.correo_tv);
        contrasenyaCampo = findViewById(R.id.contrasenya_tv);
        contrasenyaRepCampo = findViewById(R.id.contrasenyaRep_tv);
        registroBoton = findViewById(R.id.confirmarRegistro_btn);

        // Cuando el usuario pulse el botón de registro, se ejecutará este metodo
        registroBoton.setOnClickListener(v -> registrarUsuario());
    }

    // Metodo que recoge los datos, valida y los envía al servidor
    private void registrarUsuario() {
        // Obtiene el texto que el usuario escribió en los campos
        String nombre = nombreCampo.getText().toString().trim();
        String apellidos = apellidosCampo.getText().toString().trim();
        String email = emailCampo.getText().toString().trim();
        String password = contrasenyaCampo.getText().toString().trim();
        String passwordRep = contrasenyaRepCampo.getText().toString().trim();

        // Validaciones básicas antes de enviar
        if (nombre.isEmpty() || apellidos.isEmpty() || email.isEmpty() || password.isEmpty() || passwordRep.isEmpty()) {
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Comprobar si las contraseñas coinciden
        if (!password.equals(passwordRep)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar formato del correo
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Correo electrónico no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        //Si está bien, se prepara la petición HTTP con Volley
        RequestQueue queue = Volley.newRequestQueue(this);

        // StringRequest = petición al servidor tipo POST
        StringRequest request = new StringRequest(Request.Method.POST, URL_REGISTER,
                response -> {
                    // Si el servidor responde correctamente
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    // Si hay algún problema (conexión o error en Laravel)
                    Toast.makeText(this, "Error en el registro", Toast.LENGTH_SHORT).show();
                }) {

            // Aquí se definen los datos que se van a enviar al servidor
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("nombre", nombre);
                params.put("email", email);
                params.put("contrasena", password);
                params.put("id_rol", "1"); // 1 = usuario normal
                return params;
            }
        };

        // Finalmente, se lanza la petición
        queue.add(request);
    }
}
