package org.jordi.btlealumnos2021;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONObject;


/**
 * Nombre Fichero: ReestablecerContrasenaActivity.java
 * Descripción: Actividad de Android que permite restablecer la contraseña enviando un correo de recuperación mediante Firebase Authentication.
 * Autor: Alan Guevara Martínez
 * Fecha: 15/11/2025
 */

public class RestablecerContrasenaActivity extends AppCompatActivity {

    // --------------------------------------------------------------
    // CAMPOS
    // --------------------------------------------------------------
    private EditText campoCorreo;
    private Button botonEnviar;
    private FirebaseAuth auth;   // Manejo de Firebase Authentication (igual que en web)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rest_contrasenya);

        // Inicializamos Firebase exactamente igual que en el login/registro
        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();

        // Campo correo + botón enviar
        campoCorreo = findViewById(R.id.reset_email_tv);
        botonEnviar = findViewById(R.id.btnEnviarReset);

        // Elementos que deben llevar al login (tanto el icono como el texto)
        LinearLayout layoutVolver = findViewById(R.id.layout_volver);
        ImageView btnVolver = findViewById(R.id.btn_volver);
        TextView tvVolverLogin = findViewById(R.id.tvVolverLogin);

        // Listener que llama a iniciar sesión
        View.OnClickListener irLoginListener = v -> {
            Intent intent = new Intent(RestablecerContrasenaActivity.this, InicioSesionActivity.class);
            startActivity(intent);
            finish(); // Cierra esta pantalla para no volver atrás
        };

        // Asignar el mismo listener a los 3 elementos
        layoutVolver.setOnClickListener(irLoginListener);
        btnVolver.setOnClickListener(irLoginListener);
        tvVolverLogin.setOnClickListener(irLoginListener);

        // Listener del botón para enviar el correo (llamamos al método enviarCorreoReestablecer())
        botonEnviar.setOnClickListener(v -> enviarCorreoRestablecer());
    }


    /// --------------------------------------------------------------
    /// MÉTODO PRINCIPAL → enviarCorreoRestablecer()
    /// --------------------------------------------------------------
    private void enviarCorreoRestablecer() {

        String correo = campoCorreo.getText().toString().trim();

        // Validar que el campo no esté vacío
        if (correo.isEmpty()) {
            Toast.makeText(this, "Introduce tu correo electrónico.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar formato del correo
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Formato de correo no válido.", Toast.LENGTH_SHORT).show();
            return;
        }

        // ------------------------------
        // 1) LLAMAR AL BACKEND ATMOS para reestablecer la contraseña, en vez de con firebase que hacíamos antes
        // ------------------------------

        try {
            JSONObject json = new JSONObject();
            json.put("correo", correo);

            RequestQueue queue = Volley.newRequestQueue(this);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    "https://nagufor.upv.edu.es/resetPasswordAtmos",
                    json,
                    response -> {

                        try {
                            if (response.getString("status").equals("ok")) {

                                Toast.makeText(
                                        this,
                                        "Correo enviado. Revisa tu bandeja de entrada o spam.",
                                        Toast.LENGTH_LONG
                                ).show();

                                // Ir a login
                                Intent intent = new Intent(this, InicioSesionActivity.class);
                                startActivity(intent);
                                finish();

                            } else {
                                Toast.makeText(
                                        this,
                                        "No se ha podido enviar el correo.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }

                        } catch (Exception e) {
                            Toast.makeText(this, "Error inesperado.", Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> {
                        Toast.makeText(
                                this,
                                "Error con el servidor. Inténtalo más tarde.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
            );

            queue.add(request);

        } catch (Exception e) {
            Toast.makeText(this, "Error inesperado.", Toast.LENGTH_LONG).show();
        }
    }
}
