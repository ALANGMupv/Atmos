package org.jordi.btlealumnos2021;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Nombre Fichero: PerfilActivity.java
 * Descripción: Muestra la información del perfil del usuario:
 *              nombre completo y correo electrónico, obtenidos
 *              de la sesión que viene de la BBDD MySQL.
 * Autor: Alan Guevara Martínez
 * Fecha: 17/11/2025
 */

public class PerfilActivity extends FuncionesBaseActivity {

    // Botón cerrar sesión
    private Button btnCerrar;
    // Botón editar perfil
    private Button btnEditar;

    // TextViews donde se muestran los datos del usuario
    private TextView txtNombreUsuario;
    private TextView txtCorreoUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.perfil);
        setupBottomNav();

        // --------------------------------------------------
        // 1. Referencias a vistas del layout
        // --------------------------------------------------
        btnCerrar        = findViewById(R.id.btnCerrarSesion);
        btnEditar        = findViewById(R.id.btnEditarPerfil);
        txtNombreUsuario = findViewById(R.id.txtNombreUsuario);
        txtCorreoUsuario = findViewById(R.id.txtCorreoUsuario);

        // --- FUNCIONALIDAD FLECHA ATRÁS ---
        // Esta flecha debe volver a la activity anterior (sea cual sea)
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        // --------------------------------------------------
        // 2. Cargar datos del usuario desde la sesión local
        // --------------------------------------------------
        cargarDatosPerfilDesdeSesion();

        // --------------------------------------------------
        // 3. FUNCIONALIDAD EDITAR PERFIL
        //    Abre la Activity donde el usuario puede modificar sus datos
        // --------------------------------------------------
        if (btnEditar != null) {
            btnEditar.setOnClickListener(v -> {
                Intent intent = new Intent(PerfilActivity.this, EditarPerfilActivity.class);
                startActivity(intent);
            });
        }

        // --------------------------------------------------
        // 4. MOSTRAR POPUP DE CERRAR SESIÓN
        // --------------------------------------------------
        if (btnCerrar != null) {
            btnCerrar.setOnClickListener(this::abrirPopupCerrarSesion);
        }
    }

    /**
     * Nombre Método: cargarDatosPerfilDesdeSesion
     * Descripción:
     *   Obtiene de SesionManager los datos guardados en SharedPreferences
     *   (nombre, apellidos y email) y los muestra en los TextViews del perfil.
     *
     *   Estos datos proceden originalmente de la BBDD MySQL, ya que
     *   fueron devueltos por el endpoint /login y guardados en la sesión.
     */
    private void cargarDatosPerfilDesdeSesion() {
        // Recuperar nombre, apellidos y correo de la sesión local
        String nombre    = SesionManager.obtenerNombre(this);
        String apellidos = SesionManager.obtenerApellidos(this);
        String email     = SesionManager.obtenerEmail(this);

        // Construir el nombre completo (si faltan apellidos, no pasa nada)
        String nombreCompleto;
        if (apellidos == null || apellidos.trim().isEmpty()) {
            nombreCompleto = nombre;  // Solo nombre
        } else {
            nombreCompleto = nombre + " " + apellidos;
        }

        // Mostrar los datos en pantalla
        if (txtNombreUsuario != null) {
            txtNombreUsuario.setText(
                    nombreCompleto.isEmpty() ? "Nombre de Usuario" : nombreCompleto
            );
        }

        if (txtCorreoUsuario != null) {
            txtCorreoUsuario.setText(
                    (email == null || email.isEmpty()) ? "Correo electrónico de Usuario" : email
            );
        }
    }

    /**
     * Nombre Método: abrirPopupCerrarSesion
     * Descripción:
     *   Muestra el popup de confirmación para cerrar sesión.
     *   Desde el popup, el usuario puede:
     *     - Cancelar → se cierra el popup.
     *     - Confirmar → se cierra sesión (Firebase + SharedPreferences) y
     *                   se redirige a la pantalla de inicio de sesión.
     *
     * Entradas:
     *  - v: Vista desde la que se ha hecho clic (botón "Cerrar sesión").
     */
    private void abrirPopupCerrarSesion(View v) {

        // Se obtiene un LayoutInflater para poder "inflar" (convertir XML en vista) el layout del popup
        LayoutInflater inflater = LayoutInflater.from(this);

        // Se infla la vista del popup desde el archivo XML popup_confirmar_cerrar_sesion
        View popupView = inflater.inflate(R.layout.popup_confirmar_cerrar_sesion, null);

        // Se crea un PopupWindow con:
        // - La vista inflada (popupView)
        // - Un ancho que ocupa toda la pantalla (MATCH_PARENT)
        // - Un alto que ocupa toda la pantalla (MATCH_PARENT)
        // - true indica que el popup es enfocables (permite cerrar al tocar fuera si se configura)
        PopupWindow popup = new PopupWindow(
                popupView,
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                true
        );

        // Se muestra el popup en el centro de la pantalla
        popup.showAtLocation(v, Gravity.CENTER, 0, 0);

        // --- BOTÓN CANCELAR - Cierra el popup ---
        AppCompatButton btnCancelar = popupView.findViewById(R.id.btnCancelarCerrar);
        btnCancelar.setOnClickListener(view -> popup.dismiss());

        // --- BOTÓN CONFIRMAR - Cierra sesión y vuelve al login ---
        AppCompatButton btnConfirmar = popupView.findViewById(R.id.btnConfirmarCerrar);
        if (btnConfirmar != null) {
            btnConfirmar.setOnClickListener(view -> {
                // 0. DETENER SERVICIO DE BEACONS
                stopService(new Intent(PerfilActivity.this, ServicioDeteccionBeacons.class));

                // 1. Cerrar sesión en Firebase (usuario actual)
                FirebaseAuth.getInstance().signOut();

                // 2. Limpiar la sesión local (SharedPreferences)
                SesionManager.cerrarSesion(PerfilActivity.this);

                // 3. Ir a la pantalla de inicio de sesión y limpiar el back stack
                Intent intent = new Intent(PerfilActivity.this, InicioSesionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                // 4. Cerrar el popup y esta Activity
                popup.dismiss();
                finish();
            });
        }
    }
}
