package com.example.redsos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.redsos.db.RedSOSDb;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class agregarcontacto extends AppCompatActivity {

    private Button btnVolver, btnGuardarContacto;
    private TextInputEditText inputNombre, inputParentesco, inputTelefono;
    private RedSOSDb db;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_agregar_contacto);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new RedSOSDb(this);

        btnVolver = findViewById(R.id.btnVolver);
        btnGuardarContacto = findViewById(R.id.btnGuardarContacto);
        inputNombre = findViewById(R.id.inputNombre);
        inputParentesco = findViewById(R.id.inputParentesco);
        inputTelefono = findViewById(R.id.inputTelefono);

        btnVolver.setOnClickListener(v -> finish());

        btnGuardarContacto.setOnClickListener(v -> {
            String nombre = inputNombre.getText() == null ? "" : inputNombre.getText().toString().trim();
            String relacion = inputParentesco.getText() == null ? "" : inputParentesco.getText().toString().trim();
            String telefono = inputTelefono.getText() == null ? "" : inputTelefono.getText().toString().trim();

            if (nombre.isEmpty() || telefono.isEmpty()) {
                Toast.makeText(this, "Completa Nombre y Teléfono", Toast.LENGTH_SHORT).show();
                return;
            }

            executor.execute(() -> {
                db.insertarContacto(nombre, relacion, telefono, 0, 0);
                runOnUiThread(() -> {
                    Toast.makeText(this, "✅ " + nombre + " guardado", Toast.LENGTH_SHORT).show();
                    Intent resultado = new Intent();
                    resultado.putExtra("nombre", nombre);
                    resultado.putExtra("telefono", telefono);
                    setResult(RESULT_OK, resultado);
                    finish();
                });
            });
        });
    }
}
