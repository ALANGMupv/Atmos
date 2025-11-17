package org.jordi.btlealumnos2021;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

/**
 * Nombre Fichero: EditarPerfilActivity.java
 * Descripción: Pantalla donde el usuario puede modificar su
 *              nombre, apellidos y correo, introduciendo su
 *              contraseña para confirmar los cambios.
 * Autor: Alan Guevara Martínez
 * Fecha: 17/11/2025
 */

public class EditarPerfilActivity extends FuncionesBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editar_perfil);

        // Establece el título del header
        setupHeader("Editar Perfil");


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
    }
}
