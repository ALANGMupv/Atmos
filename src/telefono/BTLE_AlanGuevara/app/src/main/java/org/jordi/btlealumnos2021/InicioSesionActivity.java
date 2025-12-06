package org.jordi.btlealumnos2021;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;

import androidx.biometric.BiometricManager;

import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.util.concurrent.Executor;


/**
 * Nombre Fichero: InicioSesionActivity.java
 * Descripción: Pantalla encargada del inicio de sesión de usuarios ya registrados.
 * Valida los datos, inicia sesión en Firebase y envía el token al servidor.
 * Autora: Nerea Aguilar Forés
 * Fecha: 2025
 */
public class InicioSesionActivity extends FuncionesBaseActivity {

    // Campos de texto y botón de login
    private EditText emailCampo, contrasenyaCampo;
    private Button loginBoton;
    private TextView olvidasteContrasenya, enlaceRegistro;
    private RequestQueue queue;


    //  BIOMETRÍA
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

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

        // Redirección a la página de Registro
        enlaceRegistro = findViewById(R.id.tvRegistrarme);
        enlaceRegistro.setOnClickListener(v -> {
            Intent intent = new Intent(InicioSesionActivity.this, RegistroActivity.class);
            startActivity(intent);
        });

        // Listener del botón Iniciar sesión
        loginBoton.setOnClickListener(v -> iniciarSesion());

        // Pulsar ojo para ver contraseña
        habilitarToggleContrasena(contrasenyaCampo);

        // -----------------------------------------------------------
        // INICIO AUTOMÁTICO CON HUELLA SI YA HAY SESIÓN GUARDADA
        // -----------------------------------------------------------
        if (SesionManager.haySesionActiva(this) && puedeUsarBiometria()) {
            configurarBiometria();
            biometricPrompt.authenticate(promptInfo);
        }

        System.out.println("DEBUG_SESION: " + SesionManager.obtenerSesion(this));

    }

    /**
     * Nombre Método: validarLogin
     * Descripción: Comprueba que los campos de email y contraseña sean válidos.
     * Entradas:
     * - email: Texto introducido en el campo correo.
     * - password: Texto introducido en el campo contraseña.
     * Salidas:
     * - true si los datos son válidos.
     * - false si falta algún campo o el email no es válido.
     * Autora: Nerea Aguilar Forés
     */
    private boolean validarLogin(String email, String password) {

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show();
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

                    /* -------- Bloqueo por email no verificado -------- */
                    // Recargar usuario para que Firebase actualice el estado real
                    user.reload().addOnCompleteListener(task -> {

                        if (!user.isEmailVerified()) {
                            Toast.makeText(this,
                                    "Verifica tu correo antes de iniciar sesión",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        // SI está verificado, ahora sí obtenemos token fresco
                        user.getIdToken(true)
                                .addOnSuccessListener(result ->
                                        enviarLoginAlServidor(result.getToken())
                                );
                    });
                    /* ---------------------------------------------------- */

                    // Obtener token ID
                    user.getIdToken(true)
                            .addOnSuccessListener(result ->
                                    enviarLoginAlServidor(result.getToken())
                            );

                })
                .addOnFailureListener(e -> {
                    String errorTraducido = traducirErrorFirebase(e.getMessage());
                    Toast.makeText(this, errorTraducido, Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Nombre Método: enviarLoginAlServidor
     * Descripción: Envía el token de Firebase al servidor usando LogicaFake.
     * Entradas:
     * - idToken: Token de Firebase autenticado.
     * Salidas:
     * - No retorna nada. Gestiona resultados por callback.
     * Autora: Nerea Aguilar Forés
     */
    private void enviarLoginAlServidor(String idToken) {

        LogicaFake.loginServidor(
                idToken,
                queue,
                new LogicaFake.LoginCallback() {
                    @Override
                    public void onLoginOk(JSONObject usuario) {

                        /* Solo se puede acceder si el usuario ha verificado el correo */
                        int estado = usuario.optInt("estado", 0);
                        if (estado == 0) {
                            Toast.makeText(InicioSesionActivity.this,
                                    "Tu correo aún no está verificado",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Guardar sesión local
                        SesionManager.guardarSesion(InicioSesionActivity.this, usuario);

                        // Ir a mapas
                        startActivity(new Intent(InicioSesionActivity.this, MapasActivity.class));

                        // Cerrar pantalla de login
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
     * Traduce los mensajes de error de Firebase al español.
     */
    private String traducirErrorFirebase(String mensajeOriginal) {

        if (mensajeOriginal == null) return "Error desconocido";

        mensajeOriginal = mensajeOriginal.toLowerCase();

        if (mensajeOriginal.contains("password is invalid")) {
            return "La contraseña es incorrecta";
        }

        if (mensajeOriginal.contains("no user record")) {
            return "El usuario no existe";
        }

        if (mensajeOriginal.contains("email address is badly formatted")) {
            return "El correo electrónico no es válido";
        }

        if (mensajeOriginal.contains("network error")) {
            return "Error de conexión. Revisa tu internet.";
        }

        if (mensajeOriginal.contains("too many unsuccessful login attempts")
                || mensajeOriginal.contains("blocked")) {
            return "Demasiados intentos fallidos. Inténtalo más tarde.";
        }

        if (mensajeOriginal.contains("auth credential is incorrect") ||
                mensajeOriginal.contains("auth credential") ||
                mensajeOriginal.contains("malformed") ||
                mensajeOriginal.contains("expired")) {

            return "La sesión ha caducado o las credenciales no son válidas. Inicia sesión de nuevo.";
        }

        return "Error: " + mensajeOriginal;
    }

    //BIOMETRIA

    /**
     * Nombre Método: puedeUsarBiometria
     * Descripción: Comprueba si el dispositivo soporta autenticación biométrica
     * (huella, reconocimiento facial o credenciales del dispositivo).
     * Entradas:
     * - Ninguna (usa el contexto de la Activity).
     * Salidas:
     * - boolean: true si se puede usar biometría, false en caso contrario.
     * Autora: Nerea Aguilar Forés
     */
    private boolean puedeUsarBiometria() {
        BiometricManager manager = BiometricManager.from(this);

        int res = manager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
        // DEBUG: Ver estado biométrico
        System.out.println("DEBUG_BIOMETRIA: " + res);

        switch (res) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                return true;

            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                mostrarDialogoConfigurarHuella();
                return false;

            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(this, "Este dispositivo no tiene lector de huella.", Toast.LENGTH_LONG).show();
                return false;

            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(this, "Lector biométrico no disponible.", Toast.LENGTH_LONG).show();
                return false;

            default:
                return false;
        }
    }

    /**
     * Nombre Método: mostrarDialogoConfigurarHuella
     * Descripción: Muestra un diálogo informando al usuario que debe registrar
     * una huella en los ajustes del dispositivo para poder usar biometría.
     * Entradas:
     * - Ninguna.
     * Salidas:
     * - No retorna nada. Interactúa mediante botones con el usuario.
     * Autora: Nerea Aguilar Forés
     */
    private void mostrarDialogoConfigurarHuella() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Configurar huella");
        builder.setMessage("Para usar este acceso, primero registra una huella en los ajustes del dispositivo.");

        builder.setPositiveButton("Ir a Ajustes", (dialog, which) -> {
            Intent intent = new Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS);
            startActivity(intent);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.create().show();
    }

    /**
     * Nombre Método: configurarBiometria
     * Descripción: Inicializa los componentes necesarios para el uso de biometría,
     * incluyendo el BiometricPrompt y el mensaje del cuadro de autenticación.
     * Entradas:
     * - Ninguna (usa contexto de la Activity).
     * Salidas:
     * - No retorna nada. Deja preparado el prompt biométrico.
     * Autora: Nerea Aguilar Forés
     */
    private void configurarBiometria() {

        Executor executor = ContextCompat.getMainExecutor(this);

        biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        accederConSesionGuardada();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode,
                                                      @NonNull CharSequence errString) {
                        Toast.makeText(InicioSesionActivity.this,
                                "Error: " + errString, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        Toast.makeText(InicioSesionActivity.this,
                                "Huella incorrecta", Toast.LENGTH_SHORT).show();
                    }
                });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Iniciar sesión con huella")
                .setSubtitle("Usa tu huella para acceder a Atmos")
                .setNegativeButtonText("Cancelar")
                .build();
    }

    /**
     * Nombre Método: accederConSesionGuardada
     * Descripción: Obtiene el token almacenado de forma segura y, si existe,
     * permite el acceso automático a la aplicación tras autenticación biométrica.
     * Entradas:
     * - Ninguna.
     * Salidas:
     * - No retorna nada. Realiza navegación a MapasActivity si el token existe.
     * Autora: Nerea Aguilar Forés
     */
    private void accederConSesionGuardada() {

        try {
            JSONObject usuario = SesionManager.obtenerSesion(this);

            if (usuario != null) {
                Intent intent = new Intent(this, MapasActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "No hay sesión guardada.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
