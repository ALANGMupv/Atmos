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

import androidx.appcompat.widget.AppCompatButton;

/**
 * Nombre Fichero: PerfilActivity.java
 * Descripción: Muestra la información del perfil del usuario.
 * Autor: Alan Guevara Martínez
 * Fecha: 17/11/2025
 */

public class PerfilActivity extends FuncionesBaseActivity {

    // Botón cerrar sesión
    Button btnCerrar;
    // Botón editar perfil
    Button btnEditar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.perfil);

        // Inicializar botones
        btnCerrar = findViewById(R.id.btnCerrarSesion);
        btnEditar = findViewById(R.id.btnEditarPerfil);

        // Configura el header superior
        setupHeader("Mi Perfil");

        // --- FUNCIONALIDAD FLECHA ATRÁS ---
        // Esta flecha debe volver a la activity anterior (sea cual sea)
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        // --- FUNCIONALIDAD EDITAR PERFIL ---
        // Abre la Activity donde el usuario puede modificar sus datos
        if (btnEditar != null) {
            btnEditar.setOnClickListener(v -> {
                Intent intent = new Intent(PerfilActivity.this, EditarPerfilActivity.class);
                startActivity(intent);
            });
        }

        // --- MOSTRAR POPUP DE CERRAR SESIÓN ---
        btnCerrar.setOnClickListener(v -> abrirPopupCerrarSesion(v));
    }


    /**
     * Método que muestra el popup de confirmación para cerrar sesión.
     * View: v -> abrirPopupCerrarSesion
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
    }
}