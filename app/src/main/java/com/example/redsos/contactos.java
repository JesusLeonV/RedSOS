package com.example.redsos;

import android.content.Intent;
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

import com.example.redsos.db.AppDatabase;
import com.example.redsos.db.entity.Contacto;

import java.util.List;

public class contactos extends AppCompatActivity {

    private Button btnVolver, btnAgregarContacto;
    private LinearLayout listaContactos;

    // Las 2 primeras tarjetas son ejemplos fijos; las demás se cargan de SQLite
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

        configurarLlamada(R.id.btnLlamarMaria, "+56912345678");
        configurarLlamada(R.id.btnLlamarCarlos, "+56987654321");
        configurarLlamada(R.id.btnLlamarCarabineros, "133");
        configurarLlamada(R.id.btnLlamarAmbulancia, "131");
        configurarLlamada(R.id.btnLlamarBomberos, "132");

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnAgregarContacto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(contactos.this, agregarcontacto.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        recargarContactos();
    }

    private void recargarContactos() {
        while (listaContactos.getChildCount() > TARJETAS_EJEMPLO) {
            listaContactos.removeViewAt(TARJETAS_EJEMPLO);
        }
        cargarContactosDesdeSQLite();
    }

    private void cargarContactosDesdeSQLite() {
        AppDatabase.executor.execute(() -> {
            List<Contacto> contactos = AppDatabase.get(this).contactoDao().obtenerTodos();
            runOnUiThread(() -> {
                for (Contacto c : contactos) {
                    agregarTarjetaContacto(c.nombre, c.parentesco, c.telefono);
                }
            });
        });
    }

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

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density);
    }
}
