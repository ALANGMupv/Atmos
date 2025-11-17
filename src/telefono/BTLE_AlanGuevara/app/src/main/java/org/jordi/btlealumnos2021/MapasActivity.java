package org.jordi.btlealumnos2021;

import android.os.Bundle;

public class MapasActivity extends FuncionesBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapas);

        //Poner título de página en el header
        setupHeader("Mapas");

        //Para funcionamiento del menu inferior
        setupBottomNav(0); // 0 = Mapas
    }
}
