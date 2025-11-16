package org.jordi.btlealumnos2021;

import android.os.Bundle;

public class MapasActivity extends FuncionesBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapas);

        // DESCOMENTAR cuando se añada el header
        //setupHeader("Mapas");

        //DESCOMENTAR cuando se añada el menú inferior
        //setupBottomNav(0); // 0 = Mapas
    }
}
