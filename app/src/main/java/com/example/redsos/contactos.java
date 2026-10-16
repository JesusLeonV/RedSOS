package com.example.redsos;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.redsos.adapters.ContactoAdapter;
import com.example.redsos.db.RedSOSDb;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class contactos extends AppCompatActivity implements ContactoAdapter.OnContactoClickListener {

    private Button btnVolver, btnAgregarContacto;
    private RecyclerView recyclerContactos;
    private ContactoAdapter adapter;
    private RedSOSDb db;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

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

        db = new RedSOSDb(this);

        btnVolver = findViewById(R.id.btnVolver);
        btnAgregarContacto = findViewById(R.id.btnAgregarContacto);
        recyclerContactos = findViewById(R.id.recyclerContactos);

        adapter = new ContactoAdapter(this);
        recyclerContactos.setLayoutManager(new LinearLayoutManager(this));
        recyclerContactos.setAdapter(adapter);

        configurarLlamada(R.id.btnLlamarCarabineros, "133");
        configurarLlamada(R.id.btnLlamarAmbulancia, "131");
        configurarLlamada(R.id.btnLlamarBomberos, "132");

        btnVolver.setOnClickListener(v -> finish());

        btnAgregarContacto.setOnClickListener(v ->
                startActivity(new Intent(contactos.this, agregarcontacto.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarContactos();
    }

    private void cargarContactos() {
        executor.execute(() -> {
            java.util.List<String[]> contactos = db.obtenerTodosContactos();
            runOnUiThread(() -> adapter.actualizar(contactos));
        });
    }

    @Override
    public void onLlamar(String telefono) {
        String limpio = telefono.replace(" ", "").replace("-", "");
        if (!limpio.startsWith("+") && limpio.length() >= 8) {
            limpio = "+56" + limpio;
        }
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + limpio)));
    }

    @Override
    public void onEliminar(int id, int posicion) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar contacto")
                .setMessage("¿Deseas eliminar este contacto?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    executor.execute(() -> {
                        db.eliminarContacto(id);
                        runOnUiThread(() -> cargarContactos());
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void configurarLlamada(int idBoton, String telefono) {
        View boton = findViewById(idBoton);
        if (boton != null) {
            boton.setOnClickListener(v -> onLlamar(telefono));
        }
    }
}
