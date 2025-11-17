package org.jordi.btlealumnos2021;

import android.content.Intent;
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

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;


/**
 * Nombre Fichero: InicioSesionActivity.java
 * Descripción: Pantalla encargada del inicio de sesión de usuarios ya registrados.
 *              Valida los datos, inicia sesión en Firebase y envía el token al servidor.
 * Autora: Nerea Aguilar Forés
 * Fecha: 2025
 */
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

        // Cola de peticiones HTTP (Volley)
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

        // Pulsar ojo para ver contraseña
        enablePasswordToggle(contrasenyaCampo);
    }

    /**
     * Nombre Método: validarLogin
     * Descripción: Comprueba que los campos de email y contraseña sean válidos.
     * Entradas:
     *  - email: Texto introducido en el campo correo.
     *  - password: Texto introducido en el campo contraseña.
     * Salidas:
     *  - true si los datos son válidos.
     *  - false si falta algún campo o el email no es válido.
     * Autora: Nerea Aguilar Forés
     */
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

    /**
     * Nombre Método: iniciarSesion
     * Descripción: Inicia sesión en Firebase y obtiene el idToken para enviarlo al servidor.
     * Entradas: Ninguna (obtiene los valores de los campos de la vista).
     * Salidas: No retorna nada. Llama al método de login del servidor.
     * Autora: Nerea Aguilar Forés
     */
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

    /**
     * Nombre Método: enviarLoginAlServidor
     * Descripción: Envía el token de Firebase al servidor usando LogicaFake.
     * Entradas:
     *  - idToken: Token de Firebase autenticado.
     * Salidas:
     *  - No retorna nada. Gestiona resultados por callback.
     * Autora: Nerea Aguilar Forés
     */
    private void enviarLoginAlServidor(String idToken) {

        LogicaFake.loginServidor(
                idToken,
                queue,
                new LogicaFake.LoginCallback() {
                    @Override
                    public void onLoginOk(JSONObject usuario) {
                        // Guardar sesión local
                        SesionManager.guardarSesion(InicioSesionActivity.this, usuario);

                        // Ir al menú principal
                        startActivity(new Intent(InicioSesionActivity.this, MapasActivity.class));
                        finish();
                    }

                    @Override
                    public void onEmailNoVerificado() {
                        Toast.makeText(InicioSesionActivity.this,
                                "Verifica tu correo antes de iniciar sesión",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    @Override
                    public void onUsuarioNoExiste() {
                        Toast.makeText(InicioSesionActivity.this,
                                "Usuario no registrado",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    @Override
                    public void onErrorServidor() {
                        Toast.makeText(InicioSesionActivity.this,
                                "Error en servidor",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    @Override
                    public void onErrorInesperado() {
                        Toast.makeText(InicioSesionActivity.this,
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
     *  - editText: Campo EditText al que aplicar el comportamiento.
     * Salidas:
     *  - No retorna nada. Cambia visualmente la transformación del texto.
     * Autora: Nerea Aguilar Forés
     */
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
