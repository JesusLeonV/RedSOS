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

public class dialog_sos_emergency extends AppCompatActivity {

    private Button btnCancelar, btnAsalto, btnAccidente, btnMecanica;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dialog_sos_emergency);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnCancelar = findViewById(R.id.btn_cancelar_sos);
        btnAsalto = findViewById(R.id.btn_sos_asalto);
        btnAccidente = findViewById(R.id.btn_sos_accidente);
        btnMecanica = findViewById(R.id.btn_sos_mecanica);

        // Cancelar alerta → volver a la pantalla principal
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(dialog_sos_emergency.this, "Alerta cancelada", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // Confirmar tipo de emergencia → enviar alerta y volver
        View.OnClickListener enviarAlerta = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tipo = ((Button) v).getText().toString();
                Toast.makeText(dialog_sos_emergency.this, "🚨 Alerta enviada: " + tipo, Toast.LENGTH_LONG).show();
                finish();
            }
        };

        btnAsalto.setOnClickListener(enviarAlerta);
        btnAccidente.setOnClickListener(enviarAlerta);
        btnMecanica.setOnClickListener(enviarAlerta);
    }
}
