package org.jordi.btlealumnos2021;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

// Clase para manejar el inicio de sesión de usuarios ya registrados
public class InicioSesionActivity extends AppCompatActivity {

    // Campos de texto y botón de login
    private EditText emailCampo, contrasenyaCampo;
    private Button loginBoton;
    private TextView olvidasteContrasenya;

    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inicio_sesion);

        queue = Volley.newRequestQueue(this);

        emailCampo = findViewById(R.id.login_email_tv);
        contrasenyaCampo = findViewById(R.id.login_contrasenya_tv);
        loginBoton = findViewById(R.id.login_btn);

        // Redirección a la página de Reestablecer Contraseña
        olvidasteContrasenya = findViewById(R.id.enlaceReestablecer);
        olvidasteContrasenya.setOnClickListener(v -> {
            Intent intent = new Intent(InicioSesionActivity.this, RestablecerContrasenaActivity.class);
            startActivity(intent);
        });

        // Listener del botón Iniciar sesión
        loginBoton.setOnClickListener(v -> iniciarSesion());

        //Pulsar ojo para ver contraseña
        enablePasswordToggle(contrasenyaCampo);
    }

    // Metodo que valida los datos para el login

    private boolean validarLogin(String email, String password) {

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Correo electrónico no válido", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void iniciarSesion() {

        String email = emailCampo.getText().toString().trim();
        String password = contrasenyaCampo.getText().toString().trim();

        if (!validarLogin(email, password)) return;

        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if (user == null) {
                        Toast.makeText(this, "Error inesperado", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 1. Obtener token ID
                    user.getIdToken(true)
                            .addOnSuccessListener(result ->
                                    enviarLoginAlServidor(result.getToken())
                            );

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void enviarLoginAlServidor(String idToken) {

        String url = "https://nagufor.upv.edu.es/login";

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                url,
                null,
                response -> {
                    try {

                        if (response.getString("status").equals("ok")) {

                            JSONObject usuario = response.getJSONObject("usuario");
                            guardarSesion(usuario);

                            // Ir al menú principal
                            startActivity(new Intent(this, MapasActivity.class));
                            finish();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {

                    try {
                        String body = new String(error.networkResponse.data);
                        JSONObject json = new JSONObject(body);
                        String code = json.optString("error");

                        if (code.equals("EMAIL_NO_VERIFICADO")) {
                            mostrarPantallaEmailNoVerificado();
                            return;
                        }

                        if (code.equals("USUARIO_NO_EXISTE")) {
                            Toast.makeText(this, "Usuario no registrado", Toast.LENGTH_LONG).show();
                            return;
                        }

                        Toast.makeText(this, "Error en servidor", Toast.LENGTH_LONG).show();

                    } catch (Exception e) {
                        Toast.makeText(this, "Error inesperado", Toast.LENGTH_LONG).show();
                    }

                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + idToken);
                return headers;
            }
        };

        queue.add(req);
    }

    private void guardarSesion(JSONObject userJson) {

        try {
            SharedPreferences prefs = getSharedPreferences("SESION", MODE_PRIVATE);
            SharedPreferences.Editor e = prefs.edit();

            e.putInt("id_usuario", userJson.getInt("id_usuario"));
            e.putString("nombre", userJson.getString("nombre"));
            e.putString("apellidos", userJson.getString("apellidos"));
            e.putString("email", userJson.getString("email"));
            e.putInt("estado", userJson.getInt("estado"));
            e.apply();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void mostrarPantallaEmailNoVerificado() {
        Toast.makeText(this, "Verifica tu correo antes de iniciar sesión", Toast.LENGTH_LONG).show();
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
