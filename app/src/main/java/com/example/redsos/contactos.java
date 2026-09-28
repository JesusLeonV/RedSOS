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

public class contactos extends AppCompatActivity {

    private Button btnVolver, btnAgregarContacto, btnGuardarContactos;

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
        btnGuardarContactos = findViewById(R.id.btnGuardarContactos);

        // Volver a la pantalla principal
        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnAgregarContacto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(contactos.this, "Agregar nuevo contacto de emergencia", Toast.LENGTH_SHORT).show();
            }
        });

        btnGuardarContactos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(contactos.this, "✅ Contactos guardados", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
