package com.example.redsos;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class contactos extends AppCompatActivity {

    private Button btnVolver, btnAgregarContacto;
    private LinearLayout listaContactos;

    // Las 2 primeras tarjetas son ejemplos fijos; las demás se cargan de prefs
    private static final int TARJETAS_EJEMPLO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contactos);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnVolver = findViewById(R.id.btnVolver);
        btnAgregarContacto = findViewById(R.id.btnAgregarContacto);
        listaContactos = findViewById(R.id.listaContactos);

        // Botones "Llamar" de las tarjetas de ejemplo
        configurarLlamada(R.id.btnLlamarMaria, "+56912345678");
        configurarLlamada(R.id.btnLlamarCarlos, "+56987654321");

        // Botones "Llamar" de los servicios de emergencia
        configurarLlamada(R.id.btnLlamarCarabineros, "133");
        configurarLlamada(R.id.btnLlamarAmbulancia, "131");
        configurarLlamada(R.id.btnLlamarBomberos, "132");

        // Volver
        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Abre la pantalla para agregar un nuevo contacto
        btnAgregarContacto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(contactos.this, agregarcontacto.class));
            }
        });
    }

    // Al volver de "Agregar Contacto" → recargar las tarjetas guardadas
    @Override
    protected void onResume() {
        super.onResume();
        recargarContactos();
    }

    // Quita las tarjetas agregadas antes y las vuelve a crear desde prefs
    private void recargarContactos() {
        while (listaContactos.getChildCount() > TARJETAS_EJEMPLO) {
            listaContactos.removeViewAt(TARJETAS_EJEMPLO);
        }
        cargarContactos();
    }

    // Abre el marcador del teléfono con el número listo (sin pedir permisos)
    private void llamarContacto(String telefono) {
        String limpio = telefono.replace(" ", "").replace("-", "");
        if (!limpio.startsWith("+") && limpio.length() >= 8) {
            limpio = "+56" + limpio;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + limpio));
        startActivity(intent);
    }

    private void configurarLlamada(int idBoton, final String telefono) {
        View boton = findViewById(idBoton);
        if (boton != null) {
            boton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    llamarContacto(telefono);
                }
            });
        }
    }

    // Crea dinámicamente la tarjeta de un contacto con su botón " Llamar"
    private void agregarTarjetaContacto(String nombre, String relacion, String telefono) {
        View tarjeta = LayoutInflater.from(this).inflate(R.layout.item_contacto, listaContactos, false);

        TextView txtNombre = tarjeta.findViewById(R.id.txtNombreContacto);
        TextView txtDetalle = tarjeta.findViewById(R.id.txtDetalleContacto);
        View btnLlamar = tarjeta.findViewById(R.id.btnLlamarContacto);

        txtNombre.setText(nombre);
        String detalle = (relacion != null && !relacion.isEmpty())
                ? relacion + " · +56 " + telefono
                : "+56 " + telefono;
        txtDetalle.setText(detalle);

        btnLlamar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llamarContacto(telefono);
            }
        });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(12);
        tarjeta.setLayoutParams(lp);

        listaContactos.addView(tarjeta);
    }

    // Lee los contactos guardados en SharedPreferences
    private void cargarContactos() {
        SharedPreferences prefs = getSharedPreferences("redsos_prefs", MODE_PRIVATE);
        String datos = prefs.getString("contactos_extra", "");
        if (datos.isEmpty()) return;
        for (String item : datos.split(";;")) {
            String[] partes = item.split("\\|", -1);
            if (partes.length >= 3 && !partes[0].trim().isEmpty()) {
                agregarTarjetaContacto(partes[0], partes[1], partes[2]);
            }
        }
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density);
    }
}
