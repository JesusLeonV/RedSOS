package com.example.redsos;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.redsos.repository.RedSosRepository;
import com.google.android.material.textfield.TextInputEditText;

public class agregarcontacto extends AppCompatActivity {

    private Button btnVolver, btnGuardarContacto;
    private TextInputEditText inputNombre, inputParentesco, inputTelefono;
    private RedSosRepository repository;

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

        repository = new RedSosRepository(this);

        btnVolver = findViewById(R.id.btnVolver);
        btnGuardarContacto = findViewById(R.id.btnGuardarContacto);
        inputNombre = findViewById(R.id.inputNombre);
        inputParentesco = findViewById(R.id.inputParentesco);
        inputTelefono = findViewById(R.id.inputTelefono);

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnGuardarContacto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombre = inputNombre.getText() == null ? "" : inputNombre.getText().toString().trim();
                String relacion = inputParentesco.getText() == null ? "" : inputParentesco.getText().toString().trim();
                String telefono = inputTelefono.getText() == null ? "" : inputTelefono.getText().toString().trim();

                if (nombre.isEmpty() || telefono.isEmpty()) {
                    Toast.makeText(agregarcontacto.this,
                            "Completa Nombre y Teléfono", Toast.LENGTH_SHORT).show();
                    return;
                }

                repository.guardarContacto(nombre, relacion, telefono);

                Toast.makeText(agregarcontacto.this,
                        "✅ " + nombre + " guardado localmente", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
