package org.jordi.btlealumnos2021;

import android.os.Bundle;

public class MenuActivity extends FuncionesBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_acciones);

        // DESCOMENTAR cuando se añada el header
        setupHeader("Menú");

        //DESCOMENTAR cuando se añada el menú inferior
        setupBottomNav(2); // 2 = Menú
    }
}
