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

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Nombre Fichero: RegistroActivity.java
 * Descripción: Pantalla encargada del registro de nuevos usuarios.
 *              Valida los datos, crea el usuario en Firebase, envía el token al servidor
 *              y redirige a la pantalla de inicio de sesión.
 * Autora: Nerea Aguilar Forés
 * Fecha: 2025
 */
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
                // Abrir pantalla terminos
                // startActivity(new Intent(RegistroActivity.this, TerminosActivity.class));
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
        enablePasswordToggle(contrasenyaCampo);
        enablePasswordToggle(contrasenyaRepCampo);
    }

    /**
     * Nombre Método: registrarUsuario
     * Descripción: Valida los datos del formulario, crea el usuario en Firebase,
     *              obtiene el token ID y lo envía al servidor.
     * Entradas:
     *  - Ninguna (lee directamente los campos de la vista)
     * Salidas:
     *  - No retorna nada. Muestra mensajes o invoca el flujo de registro.
     * Autora: Nerea Aguilar Forés
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

                    // 2) ENVIAR EMAIL DE VERIFICACIÓN
                    user.sendEmailVerification();

                    // 3) OBTENER TOKEN ID
                    user.getIdToken(true).addOnSuccessListener(result -> {

                        String idToken = result.getToken();

                        // 4) ENVIAR AL SERVIDOR (AHORA USANDO LOGICAFAKE)
                        enviarRegistroAlServidor(idToken, nombre, apellidos, password);
                    });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error creando usuario: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Nombre Método: enviarRegistroAlServidor
     * Descripción: Llama a la lógica de negocio (LogicaFake) para registrar el usuario
     *              en el backend usando el token de Firebase.
     * Entradas:
     *  - idToken: Token de Firebase ya autenticado
     *  - nombre: Nombre introducido
     *  - apellidos: Apellidos introducidos
     *  - password: Contraseña
     * Salidas:
     *  - No retorna nada. Procesa el resultado mediante callbacks.
     * Autora: Nerea Aguilar Forés
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
     * Nombre Método: enablePasswordToggle
     * Descripción: Permite mostrar u ocultar la contraseña al pulsar el icono del ojo.
     * Entradas:
     *  - editText: Campo EditText sobre el que aplicar el comportamiento.
     * Salidas:
     *  - No retorna nada. Cambia la visibilidad del texto.
     * Autora: Nerea Aguilar Forés
     */
    private void enablePasswordToggle(EditText editText) {
        editText.setOnTouchListener((v, event) -> {

            if (event.getAction() != MotionEvent.ACTION_UP) return false;

            int drawableRight = editText.getWidth() - editText.getCompoundPaddingRight();

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
