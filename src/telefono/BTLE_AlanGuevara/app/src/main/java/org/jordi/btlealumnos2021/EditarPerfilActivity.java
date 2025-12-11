package org.jordi.btlealumnos2021;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONObject;

/**
 * Nombre Fichero: EditarPerfilActivity.java
 * Descripción: Pantalla donde el usuario puede modificar su
 *              nombre y apellidos. El correo se muestra en modo
 *              lectura. Para aplicar cambios debe introducir su
 *              contraseña actual (validada contra Firebase).
 *              Si pulsa "Cambiar contraseña" se le cerrará la
 *              sesión y será redirigido a la pantalla de
 *              restablecer contraseña.
 * Autor: Alan Guevara Martínez
 * Fecha: 17/11/2025
 */

public class EditarPerfilActivity extends FuncionesBaseActivity {

    // Campos de texto
    private EditText etNombre;
    private EditText etApellidos;
    private EditText etCorreo;
    private EditText etContrasena;

    // Botón actualizar
    private Button btnActualizar;

    // Enlace Cambiar contraseña
    private TextView btnCambiarContrasena;

    // Cola de peticiones HTTP (Volley) para usar en LogicaFake
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editar_perfil);
        setupBottomNav();

        // Inicializar cola de Volley
        queue = Volley.newRequestQueue(this);

        // Referencias a vistas
        etNombre          = findViewById(R.id.etNombre);
        etApellidos       = findViewById(R.id.etApellidos);
        etCorreo          = findViewById(R.id.etCorreo);
        etContrasena      = findViewById(R.id.etContrasena);
        btnActualizar     = findViewById(R.id.btnActualizar);
        btnCambiarContrasena = findViewById(R.id.btnCambiarContrasena);

        // --- FUNCIONALIDAD FLECHA ATRÁS ---
        // Al pulsar la flecha, vuelve a la pantalla de Perfil.
        ImageView btnBack = findViewById(R.id.btnBackEditar);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Intent intent = new Intent(EditarPerfilActivity.this, PerfilActivity.class);
                startActivity(intent);
                finish(); // Evita que EditarPerfilActivity quede en el historial
            });
        }

        // ----------------------------------------------------
        // 1. Cargar datos iniciales desde la sesión local
        // ----------------------------------------------------
        cargarDatosDesdeSesion();

        // ----------------------------------------------------
        // 2. Ojo para mostrar/ocultar la contraseña
        // ----------------------------------------------------
        habilitarToggleContrasena(etContrasena);

        // ----------------------------------------------------
        // 3. Lógica botón "Actualizar mi información"
        // ----------------------------------------------------
        if (btnActualizar != null) {
            btnActualizar.setOnClickListener(v -> actualizarPerfil());
        }

        // ----------------------------------------------------
        // 4. Lógica "Cambiar contraseña" (popup + redirección)
        // ----------------------------------------------------
        if (btnCambiarContrasena != null) {
            btnCambiarContrasena.setOnClickListener(this::abrirPopupCambiarContrasena);
        }
    }

    /**
     * Nombre Método: cargarDatosDesdeSesion
     * Descripción:
     *   Rellena los EditText de nombre, apellidos y correo con
     *   los datos guardados en SesionManager (SharedPreferences).
     *   Estos datos vienen originalmente de la BBDD MySQL.
     */
    private void cargarDatosDesdeSesion() {
        String nombre    = SesionManager.obtenerNombre(this);
        String apellidos = SesionManager.obtenerApellidos(this);
        String email     = SesionManager.obtenerEmail(this);

        if (etNombre != null) {
            etNombre.setText(nombre);
        }
        if (etApellidos != null) {
            etApellidos.setText(apellidos);
        }
        if (etCorreo != null) {
            etCorreo.setText(email);
            // Ya lo marcamos como solo lectura en XML (enabled=false),
            // pero por seguridad lo aseguramos también por código
            etCorreo.setEnabled(false);
        }
    }

    /**
     * Nombre Método: actualizarPerfil
     * Descripción:
     *   Método principal llamado al pulsar el botón
     *   "Actualizar mi información".
     *
     *   Flujo:
     *    1. Validar que nombre, apellidos y contraseña no estén vacíos.
     *    2. Obtener usuario actual de Firebase.
     *    3. Reautenticar en Firebase con email + contraseña.
     *    4. Obtener idToken.
     *    5. Llamar a LogicaFake.actualizarUsuarioServidor (PUT /usuario).
     *    6. Si OK, actualizar SesionManager y volver a PerfilActivity.
     */
    private void actualizarPerfil() {
        // 1. Leer datos del formulario
        String nombre    = etNombre.getText().toString().trim();
        String apellidos = etApellidos.getText().toString().trim();
        String email     = etCorreo.getText().toString().trim();   // solo lectura, pero lo mandamos
        String contrasenaActual = etContrasena.getText().toString();

        // Validaciones básicas
        if (nombre.isEmpty() || apellidos.isEmpty()) {
            Toast.makeText(this, "Nombre y apellidos no pueden estar vacíos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (contrasenaActual.isEmpty()) {
            Toast.makeText(this, "Introduce tu contraseña actual para confirmar los cambios", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Obtener usuario actual de Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Tu sesión ha caducado, vuelve a iniciar sesión", Toast.LENGTH_LONG).show();
            // Opcional: redirigir al login
            Intent intent = new Intent(EditarPerfilActivity.this, InicioSesionActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // 3. Reautenticar con Firebase usando el email de Firebase y la contraseña actual
        String emailFirebase = user.getEmail();
        if (emailFirebase == null) {
            Toast.makeText(this, "No se ha podido obtener el correo de Firebase", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(emailFirebase, contrasenaActual);

        // Desactivar botón para evitar pulsaciones múltiples
        btnActualizar.setEnabled(false);
        Toast.makeText(this, "Confirmando contraseña...", Toast.LENGTH_SHORT).show();

        user.reauthenticate(credential)
                .addOnSuccessListener(authResult -> {
                    // 4. Obtener un token actualizado
                    user.getIdToken(true)
                            .addOnSuccessListener((GetTokenResult result) -> {
                                String idToken = result.getToken();
                                if (idToken == null) {
                                    btnActualizar.setEnabled(true);
                                    Toast.makeText(EditarPerfilActivity.this, "No se pudo obtener el token de sesión", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // 5. Llamar al backend para actualizar MySQL usando LogicaFake
                                int idUsuario = SesionManager.obtenerIdUsuario(EditarPerfilActivity.this);
                                if (idUsuario <= 0) {
                                    btnActualizar.setEnabled(true);
                                    Toast.makeText(EditarPerfilActivity.this, "No se ha encontrado el usuario en la sesión local", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                LogicaFake.actualizarUsuarioServidor(
                                        idToken,
                                        idUsuario,
                                        nombre,
                                        apellidos,
                                        email,
                                        contrasenaActual,
                                        "",        // nuevaContrasena vacía (no se cambia aquí)
                                        queue,
                                        new LogicaFake.ActualizarUsuarioCallback() {
                                            @Override
                                            public void onActualizacionOk() {
                                                // 6. Actualizar sesión local con los nuevos datos
                                                try {
                                                    JSONObject userJson = new JSONObject();
                                                    userJson.put("id_usuario", idUsuario);
                                                    userJson.put("nombre", nombre);
                                                    userJson.put("apellidos", apellidos);
                                                    userJson.put("email", email);
                                                    // aquí ponemos 1 porque es el estado en el cual está, activo
                                                    userJson.put("estado", 1);

                                                    SesionManager.guardarSesion(EditarPerfilActivity.this, userJson);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                                btnActualizar.setEnabled(true);
                                                Toast.makeText(EditarPerfilActivity.this, "Datos actualizados correctamente", Toast.LENGTH_LONG).show();

                                                // Volver a PerfilActivity
                                                Intent intent = new Intent(EditarPerfilActivity.this, PerfilActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                                finish();
                                            }

                                            @Override
                                            public void onErrorServidor() {
                                                btnActualizar.setEnabled(true);
                                                Toast.makeText(EditarPerfilActivity.this, "Error al actualizar en el servidor", Toast.LENGTH_LONG).show();
                                            }

                                            @Override
                                            public void onErrorInesperado() {
                                                btnActualizar.setEnabled(true);
                                                Toast.makeText(EditarPerfilActivity.this, "Error inesperado al actualizar", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                );
                            })
                            .addOnFailureListener(e -> {
                                btnActualizar.setEnabled(true);
                                Toast.makeText(EditarPerfilActivity.this, "Error al obtener token de sesión", Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnActualizar.setEnabled(true);
                    // Contraseña incorrecta
                    Toast.makeText(EditarPerfilActivity.this, "Contraseña incorrecta", Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Nombre Método: abrirPopupCambiarContrasena
     * Descripción:
     *   Muestra el popup de confirmación para cambiar la contraseña.
     *   Si el usuario confirma:
     *      - Se cierra la sesión Firebase
     *      - Se limpia la sesión local (SharedPreferences)
     *      - Se redirige a RestablecerContrasenaActivity
     *   Si cancela: simplemente se cierra el popup.
     *
     * Entradas:
     *  - v: Vista que ha lanzado el evento (TextView "Cambiar contraseña").
     */
    private void abrirPopupCambiarContrasena(View v) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.popup_confirmar_cambiar_contrasenya, null);

        PopupWindow popup = new PopupWindow(
                popupView,
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                true
        );

        popup.showAtLocation(v, Gravity.CENTER, 0, 0);

        AppCompatButton btnCancelar = popupView.findViewById(R.id.btnCancelarCerrar);
        AppCompatButton btnConfirmar = popupView.findViewById(R.id.btnConfirmarCerrar);

        // Botón cancelar: solo cierra el popup
        btnCancelar.setOnClickListener(view -> popup.dismiss());

        // Botón confirmar:
        btnConfirmar.setOnClickListener(view -> {
            stopService(new Intent(EditarPerfilActivity.this, ServicioDeteccionBeacons.class));

            // 1. Cerrar sesión en Firebase
            FirebaseAuth.getInstance().signOut();

            // 2. Limpiar sesión local
            SesionManager.cerrarSesion(EditarPerfilActivity.this);

            // 3. Ir a RestablecerContrasenaActivity
            Intent intent = new Intent(EditarPerfilActivity.this, RestablecerContrasenaActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // 4. Cerrar popup y esta Activity
            popup.dismiss();
            finish();
        });
    }
}
