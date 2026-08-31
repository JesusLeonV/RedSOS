package com.example.redsos;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class informacion extends AppCompatActivity {

    private EditText intnmb, intrut, intcelular, intcorreo;
    private Button btninformacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_informacion);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        intnmb = findViewById(R.id.intnmb);
        intrut = findViewById(R.id.intrut);
        intcelular = findViewById(R.id.intcelular);
        intcorreo = findViewById(R.id.intcorreo);
        btninformacion = findViewById(R.id.btninformacion);

        btninformacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombre = intnmb.getText().toString().trim();
                String rut = intrut.getText().toString().trim();
                String celular = intcelular.getText().toString().trim();
                String correo = intcorreo.getText().toString().trim();

                if (nombre.isEmpty() || rut.isEmpty()) {
                    Toast.makeText(informacion.this, "Por favor completa los campos obligatorios", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(informacion.this, "¡Información guardada de " + nombre + "!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}