package org.jordi.btlealumnos2021;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Nombre Clase: IncidenciasActivity
 * Descripción:
 *   Activity encargada de mostrar el listado de incidencias
 *   del usuario y permitir ver o enviar nuevas incidencias.
 *
 * Autora: Nerea Aguilar Forés
 * Fecha: 17/12/2025
 */
public class IncidenciasActivity extends AppCompatActivity {

    private static final int MODO_RESPUESTAS = 0;
    private static final int MODO_ENVIADAS = 1;

    private int modoActual = MODO_RESPUESTAS;

    private RecyclerView recyclerView;
    private RequestQueue requestQueue;
    private int idUsuario = 1; // Ajustar según sesión real

    /**
     * Nombre Método: onCreate
     * Descripción:
     *   Inicializa la activity, el RecyclerView y carga
     *   las incidencias del usuario desde el backend.
     *
     * @param savedInstanceState Estado previo de la activity
     *
     * Autora: Nerea Aguilar Forés
     * Fecha: 17/12/2025
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incidencias);

        //Recycler View

        recyclerView = findViewById(R.id.rvIncidencias);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        requestQueue = Volley.newRequestQueue(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnNuevaIncidencia)
                .setOnClickListener(v -> abrirPopupEnviarIncidencia());

        cargarIncidencias();

        // Tab

        TabLayout tabs = findViewById(R.id.tabIncidencias);

        tabs.addTab(tabs.newTab().setText("Respuestas"), true);
        tabs.addTab(tabs.newTab().setText("Enviadas       "));

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    modoActual = MODO_RESPUESTAS;
                } else {
                    modoActual = MODO_ENVIADAS;
                }
                cargarIncidencias();
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });

    }

    /**
     * Nombre Método: cargarIncidencias
     * Descripción:
     *   Solicita al backend el listado de incidencias
     *   del usuario y las muestra en el RecyclerView.
     *
     * Autora: Nerea Aguilar Forés
     * Fecha: 17/12/2025
     */
    private void cargarIncidencias() {

        LogicaFake.listarIncidenciasUsuario(
                idUsuario,
                requestQueue,
                new LogicaFake.ListarIncidenciasCallback() {

                    @Override
                    public void onIncidenciasOk(JSONArray arr) {

                        List<Incidencia> lista = new ArrayList<>();

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject o = arr.optJSONObject(i);
                            if (o == null) continue;

                            String respuesta = o.optString("respuesta", null);

                            // Si estamos en "Respuestas", solo mostramos incidencias que tengan respuesta
                            if (modoActual == MODO_RESPUESTAS && (respuesta == null || respuesta.equals("null") || respuesta.isEmpty())) {
                                continue;
                            }

                            lista.add(new Incidencia(
                                    o.optInt("id_incidencia"),
                                    o.optString("asunto"),
                                    o.optString("mensaje_enviado"),
                                    respuesta,
                                    o.optString("fecha"),
                                    o.optInt("id_estado"),
                                    o.optInt("leida_usuario")
                            ));
                        }

                        // En caso de no haber incidencias muestra que no hay
                        TextView txtVacio = findViewById(R.id.txtSinIncidencias);

                        if (lista.isEmpty()) {
                            txtVacio.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);

                            if (modoActual == MODO_RESPUESTAS) {
                                txtVacio.setText("Aún no tienes respuestas");
                            } else {
                                txtVacio.setText("No has enviado incidencias");
                            }

                            return; // ⬅ importante: no seguimos
                        } else {
                            txtVacio.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }

                        IncidenciasAdapter adapter =
                                new IncidenciasAdapter(
                                        lista,
                                        v -> {
                                            int pos = recyclerView.getChildAdapterPosition(v);
                                            if (pos != RecyclerView.NO_POSITION) {
                                                abrirIncidencia(lista.get(pos));
                                            }
                                        },
                                        modoActual == MODO_RESPUESTAS
                                );

                        recyclerView.setAdapter(adapter);

                    }

                    @Override
                    public void onErrorServidor() {
                        // Se podría mostrar un mensaje de error al usuario
                    }

                    @Override
                    public void onErrorInesperado() {
                        // Error inesperado durante la petición
                    }
                }
        );
    }

    /**
     * @brief Abre un popup con la información detallada
     *        de la incidencia seleccionada.
     *
     * Muestra:
     *  - Título
     *  - Descripción
     *  - Respuesta del administrador (si existe)
     *  - Estado de la incidencia
     *  - Fecha formateada
     *
     * @param incidencia Incidencia seleccionada por el usuario
     *
     * @author Nerea Aguilar Forés
     * @date 29/12/2025
     */
    private void abrirIncidencia(Incidencia incidencia) {

        // Marcar incidencia como leída en backend
        if (incidencia.leidaUsuario == 0) {
            LogicaFake.marcarIncidenciaLeida(
                    incidencia.idIncidencia,
                    requestQueue
            );

            // Actualizamos el modelo local para esta sesión
            incidencia.leidaUsuario = 1;
        }

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_overlay_ver_incidencia);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView txtEstado = dialog.findViewById(R.id.txtEstado);
        txtEstado.setText(getTextoEstado(incidencia.idEstado));

        TextView txtRespuesta = dialog.findViewById(R.id.txtRespuestaAdmin);

        if (incidencia.respuesta != null && !incidencia.respuesta.trim().isEmpty()
                && !"null".equalsIgnoreCase(incidencia.respuesta.trim())) {
            txtRespuesta.setText(incidencia.respuesta);
        } else {
            txtRespuesta.setText(
                    "Aún estamos trabajando para resolver tu incidencia.\n" +
                            "Cuando esté resuelta te avisaremos."
            );
        }

        ((TextView) dialog.findViewById(R.id.txtTituloGrande))
                .setText(incidencia.asunto);

        ((TextView) dialog.findViewById(R.id.txtDescripcionGrande))
                .setText(incidencia.mensaje);

        ((TextView) dialog.findViewById(R.id.txtFechaGrande))
                .setText(formatearFechaUsuario(incidencia.fecha));

        dialog.findViewById(R.id.btnCerrar)
                .setOnClickListener(v -> {
                    dialog.dismiss();
                    cargarIncidencias(); // vuelve a pedir datos al backend
                });


        dialog.show();

        // Marcar como leída si tiene respuesta
        if (incidencia.respuesta != null
                && !incidencia.respuesta.trim().isEmpty()
                && !"null".equalsIgnoreCase(incidencia.respuesta.trim())
                && incidencia.leidaUsuario == 0) {

            LogicaFake.marcarIncidenciaLeida(
                    incidencia.idIncidencia,
                    requestQueue
            );

            incidencia.leidaUsuario = 1; // actualizamos en memoria
        }
    }

    /**
     * Nombre Método: abrirPopupEnviarIncidencia
     * Descripción:
     *   Abre el popup que permite al usuario
     *   enviar una nueva incidencia al backend.
     *
     * Autora: Nerea Aguilar Forés
     * Fecha: 17/12/2025
     */
    private void abrirPopupEnviarIncidencia() {

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_overlay_enviar_incidencia);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etTitulo = dialog.findViewById(R.id.etTitulo);
        EditText etDescripcion = dialog.findViewById(R.id.etDescripcion);

        dialog.findViewById(R.id.btnCerrar)
                .setOnClickListener(v -> dialog.dismiss());

        dialog.findViewById(R.id.btnEnviar).setOnClickListener(v -> {

            String titulo = etTitulo.getText().toString().trim();
            String descripcion = etDescripcion.getText().toString().trim();

            if (titulo.isEmpty() || descripcion.isEmpty()) return;

            LogicaFake.enviarIncidencia(
                    idUsuario,
                    titulo,
                    descripcion,
                    null,
                    requestQueue,
                    new LogicaFake.EnviarIncidenciaCallback() {

                        @Override
                        public void onOk(int idIncidencia) {
                            dialog.dismiss();
                            cargarIncidencias();
                        }

                        @Override
                        public void onErrorServidor() {
                            // Error del servidor
                        }

                        @Override
                        public void onErrorInesperado() {
                            // Error inesperado
                        }
                    }
            );
        });

        dialog.show();
    }

    private String formatearFechaUsuario(String fechaIso) {
        try {
            SimpleDateFormat isoFormat =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date date = isoFormat.parse(fechaIso);

            SimpleDateFormat userFormat =
                    new SimpleDateFormat("dd/MM/yyyy · HH:mm", Locale.getDefault());

            return userFormat.format(date);

        } catch (Exception e) {
            return fechaIso;
        }
    }

    private String getTextoEstado(int idEstado) {
        switch (idEstado) {
            case 1: return "Recibida";
            case 2: return "En proceso";
            case 3: return "Resuelta";
            case 4: return "Rechazada";
            default: return "Recibida";
        }
    }


}