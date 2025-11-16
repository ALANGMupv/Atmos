package org.jordi.btlealumnos2021;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FuncionesBaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // HEADER (toolbar)
    protected void setupHeader(String titulo) {
        TextView tituloHeader = findViewById(R.id.tituloHeader);
        ImageView btnNotificaciones = findViewById(R.id.btnNotificaciones);
        ImageView btnPerfil = findViewById(R.id.btnPerfil);

        if (tituloHeader != null && titulo != null) {
            tituloHeader.setText(titulo);
        }

        if (btnNotificaciones != null) {
            btnNotificaciones.setOnClickListener(v -> {
                // TODO: cambia NotificacionesActivity por la tuya real si existe
                // startActivity(new Intent(this, NotificacionesActivity.class));
            });
        }

        if (btnPerfil != null) {
            btnPerfil.setOnClickListener(v -> {
                // TODO: cambia PerfilActivity por la tuya real si existe
                // startActivity(new Intent(this, PerfilActivity.class));
            });
        }
    }

    // BOTTOM NAV
    // selected: 0 = Mapas, 1 = User, 2 = Menú
    protected void setupBottomNav(int selected) {
        LinearLayout navMapas = findViewById(R.id.nav_mapas);
        LinearLayout navUser  = findViewById(R.id.nav_user);
        LinearLayout navMenu  = findViewById(R.id.nav_menu);

        if (navMapas != null) {
            if (selected == 0) navMapas.setAlpha(1f); else navMapas.setAlpha(0.5f);
            navMapas.setOnClickListener(v -> {
                if (!(this instanceof MapasActivity)) {
                    startActivity(new Intent(this, MapasActivity.class));
                    // finish(); // opcional
                }
            });
        }

        if (navUser != null) {
            if (selected == 1) navUser.setAlpha(1f); else navUser.setAlpha(0.5f);
            navUser.setOnClickListener(v -> {
                if (!(this instanceof UserPageActivity)) {
                    startActivity(new Intent(this, UserPageActivity.class));
                    // finish();
                }
            });
        }

        if (navMenu != null) {
            if (selected == 2) navMenu.setAlpha(1f); else navMenu.setAlpha(0.5f);
            navMenu.setOnClickListener(v -> {
                if (!(this instanceof MenuActivity)) {
                    startActivity(new Intent(this, MenuActivity.class));
                    // finish();
                }
            });
        }
    }
}
