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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * @brief Pantalla de registro de nuevos usuarios.
 *
 * Permite al usuario crear una cuenta nueva en ATMOS.
 * Valida los datos introducidos, crea el usuario en Firebase,
 * obtiene el token de autenticación y lo envía al backend.
 *
 * Tras un registro correcto, el usuario es redirigido
 * a la pantalla de inicio de sesión.
 *
 * @author Nerea Aguilar Forés
 * @date 2025
 */
public class RegistroActivity extends FuncionesBaseActivity {

    // Campos del formulario y el botón
    private EditText nombreCampo, apellidosCampo, emailCampo, contrasenyaCampo, contrasenyaRepCampo;
    private Button registroBoton;
    private CheckBox check;
    private RequestQueue queue;
    private TextView enlaceLogin;

    /**
     * @brief Inicializa la pantalla de registro.
     *
     * Configura los campos del formulario, la validación de
     * términos y condiciones, los enlaces de navegación
     * y la lógica del botón de registro.
     *
     * @param savedInstanceState Estado previo de la Activity
     */
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

        // Redirección a la página de Login
        enlaceLogin = findViewById(R.id.tvIrInicio);
        enlaceLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegistroActivity.this, InicioSesionActivity.class);
            startActivity(intent);
        });

        //----------------------------------------------------------------------
        //Check términos y condiciones
        //----------------------------------------------------------------------
        check = findViewById(R.id.checkBox);
        String txt = check.getText().toString();
        SpannableString ss = new SpannableString(txt);

        int start = txt.indexOf("términos");
        int end = start + "términos de servicio".length();

        ss.setSpan(
                new ForegroundColorSpan(getColor(R.color.verde_principal)),
                start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        ss.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // ABRIR PANTALLA DE PRIVACIDAD / TÉRMINOS
                Intent intent = new Intent(RegistroActivity.this, PrivacidadActivity.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        check.setText(ss);
        check.setMovementMethod(LinkMovementMethod.getInstance());
        check.setHighlightColor(Color.TRANSPARENT);

        //----------------------------------------------------
        // Mostrar/ocultar contraseña al pulsar ojo
        habilitarToggleContrasena(contrasenyaCampo);
        habilitarToggleContrasena(contrasenyaRepCampo);
    }

    /**
     * @brief Valida el formulario y registra un nuevo usuario.
     *
     * Realiza validaciones locales, crea el usuario en Firebase,
     * obtiene el token ID y lo envía al backend para completar
     * el registro en la base de datos.
     *
     * @note El usuario debe aceptar los términos y condiciones
     *       para poder completar el registro.
     */
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

                    // 2) IMPORTANTE:
                    // Ya NO enviamos el email de verificación desde Android.
                    // El backend Atmos se encarga de generar el enlace y
                    // enviar el correo con un diseño personalizado.

                    // 3) OBTENER TOKEN ID
                    user.getIdToken(true).addOnSuccessListener(result -> {

                        String idToken = result.getToken();

                        // 4) ENVIAR AL SERVIDOR (AHORA USANDO LOGICAFAKE)
                        enviarRegistroAlServidor(idToken, nombre, apellidos, password);
                    });

                })
                .addOnFailureListener(e -> {
                    String errorTraducido = traducirErrorFirebase(e.getMessage());
                    Toast.makeText(this, errorTraducido, Toast.LENGTH_LONG).show();
                });

    }

    /**
     * @brief Envía los datos del nuevo usuario al backend.
     *
     * Utiliza el token de Firebase para registrar el usuario
     * en el servidor mediante LogicaFake.
     *
     * @param idToken   Token de autenticación de Firebase
     * @param nombre    Nombre del usuario
     * @param apellidos Apellidos del usuario
     * @param password  Contraseña del usuario
     */
    private void enviarRegistroAlServidor(String idToken, String nombre, String apellidos, String password) {

        LogicaFake.registroServidor(
                idToken,
                nombre,
                apellidos,
                password,
                queue,

                new LogicaFake.RegistroCallback() {
                    @Override
                    public void onRegistroOk() {
                        Toast.makeText(RegistroActivity.this,
                                "Registro completado. Verifica tu correo antes de iniciar sesión.",
                                Toast.LENGTH_LONG
                        ).show();

                        startActivity(new Intent(RegistroActivity.this, InicioSesionActivity.class));
                        finish();
                    }

                    @Override
                    public void onErrorServidor() {
                        Toast.makeText(RegistroActivity.this,
                                "Error en servidor",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    @Override
                    public void onErrorInesperado() {
                        Toast.makeText(RegistroActivity.this,
                                "Error inesperado",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }
    /**
     * Traduce mensajes de error de Firebase al español.
     */
    private String traducirErrorFirebase(String mensajeOriginal) {

        if (mensajeOriginal == null) return "Error desconocido";

        String msg = mensajeOriginal.toLowerCase();

        if (msg.contains("email address is badly formatted")) {
            return "El correo electrónico no es válido";
        }

        if (msg.contains("the email address is already in use")) {
            return "Este correo ya está registrado";
        }

        if (msg.contains("password should be at least")) {
            return "La contraseña es demasiado débil";
        }

        if (msg.contains("network error")) {
            return "Error de conexión. Comprueba tu internet.";
        }

        if (msg.contains("too many unsuccessful login attempts")
                || msg.contains("blocked")) {
            return "Demasiados intentos fallidos. Inténtalo más tarde.";
        }

        return "Error: " + mensajeOriginal;
    }

}
