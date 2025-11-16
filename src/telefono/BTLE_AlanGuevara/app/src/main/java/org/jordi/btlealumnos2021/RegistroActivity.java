package org.jordi.btlealumnos2021;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

// Clase que maneja el registro de nuevos usuarios
public class RegistroActivity extends AppCompatActivity {

    // Campos del formulario y el botón
    private EditText nombreCampo, apellidosCampo, emailCampo, contrasenyaCampo, contrasenyaRepCampo;
    private Button registroBoton;
    private CheckBox check;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registro);

        queue = Volley.newRequestQueue(this);

        nombreCampo = findViewById(R.id.nombre_tv);
        apellidosCampo = findViewById(R.id.apellidos_tv);
        emailCampo = findViewById(R.id.correo_tv);
        contrasenyaCampo = findViewById(R.id.contrasenya_tv);
        contrasenyaRepCampo = findViewById(R.id.contrasenyaRep_tv);
        registroBoton = findViewById(R.id.confirmarRegistro_btn);
        registroBoton.setOnClickListener(v -> registrarUsuario());

        // Cuando el usuario pulse el botón de registro, se ejecutará este metodo
        registroBoton.setOnClickListener(v -> registrarUsuario());

        //----------------------------------------------------------------------
        //Check terminos y condiciones
        //----------------------------------------------------------------------
        check = findViewById(R.id.checkBox);

        String txt = check.getText().toString();
        SpannableString ss = new SpannableString(txt);

        // Localizar "términos de servicio"
        int start = txt.indexOf("términos");
        int end = start + "términos de servicio".length();

        // Poner ese trozo en verde
        ss.setSpan(
                new ForegroundColorSpan(getColor(R.color.verde_principal)),
                start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // Hacerlo clicable
        ss.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Abrir pantalla terminos y servicos
                // startActivity(new Intent(RegistroActivity.this, TerminosActivity.class));
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Aplicar el texto enriquecido al CheckBox
        check.setText(ss);

        // Necesario para que los spans se puedan pulsar
        check.setMovementMethod(LinkMovementMethod.getInstance());
        check.setHighlightColor(Color.TRANSPARENT);

        //----------------------------------------------------

        // Ver contraseña al pulsar ojo
        enablePasswordToggle(contrasenyaCampo);
        enablePasswordToggle(contrasenyaRepCampo);

    }

    // Metodo que recoge los datos, valida y los envía al servidor
    private void registrarUsuario() {

        String nombre = nombreCampo.getText().toString().trim();
        String apellidos = apellidosCampo.getText().toString().trim();
        String email = emailCampo.getText().toString().trim();
        String password = contrasenyaCampo.getText().toString().trim();
        String passwordRep = contrasenyaRepCampo.getText().toString().trim();
        boolean aceptaTerminos = check.isChecked();

        // --------------------------
        // VALIDACIONES LOCALES
        // --------------------------
        if (nombre.isEmpty() || apellidos.isEmpty() || email.isEmpty() || password.isEmpty() || passwordRep.isEmpty()) {
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Correo electrónico no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(passwordRep)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.matches("^(?=.*[0-9])(?=.*[a-zA-Z]).{6,}$")) {
            Toast.makeText(this, "La contraseña debe tener mínimo 6 caracteres con letras y números", Toast.LENGTH_LONG).show();
            return;
        }

        if (!aceptaTerminos) {
            Toast.makeText(this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show();
            return;
        }

        // --------------------------
        // 1) CREAR USUARIO EN FIREBASE
        // --------------------------
        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if (user == null) {
                        Toast.makeText(this, "Error inesperado", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 2) ENVIAR EMAIL DE VERIFICACIÓN
                    user.sendEmailVerification();

                    // 3) OBTENER TOKEN ID
                    user.getIdToken(true).addOnSuccessListener(result -> {

                        String idToken = result.getToken();

                        // 4) ENVIAR AL SERVIDOR
                        enviarRegistroAlServidor(idToken, nombre, apellidos, password);
                    });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error creando usuario: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void enviarRegistroAlServidor(String idToken, String nombre, String apellidos, String password) {

        String url = "https://nagufor.upv.edu.es/usuario";

        JSONObject json = new JSONObject();
        try {
            json.put("nombre", nombre);
            json.put("apellidos", apellidos);
            json.put("contrasena", password);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                url,
                json,
                response -> {
                    Toast.makeText(this, "Registro completado. Verifica tu correo antes de iniciar sesión.", Toast.LENGTH_LONG).show();

                    startActivity(new Intent(this, InicioSesionActivity.class));
                    finish();
                },
                error -> {
                    Toast.makeText(this, "Error en servidor", Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + idToken);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(req);
    }


    //
    private void enablePasswordToggle(EditText editText) {
        editText.setOnTouchListener((v, event) -> {

            if (event.getAction() != MotionEvent.ACTION_UP) return false;

            int drawableRight = editText.getWidth() - editText.getCompoundPaddingRight();

            // Si el dedo toca el área del icono
            if (event.getX() >= drawableRight) {

                int cursorPos = editText.getSelectionEnd();

                if (editText.getTransformationMethod() instanceof PasswordTransformationMethod) {
                    editText.setTransformationMethod(null); // Mostrar
                } else {
                    editText.setTransformationMethod(PasswordTransformationMethod.getInstance()); // Ocultar
                }

                editText.setSelection(cursorPos);
                return true;
            }

            return false;
        });
    }


}
