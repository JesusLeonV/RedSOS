package com.example.redsos;

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

import com.example.redsos.db.RedSOSDb;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class dialog_sos_emergency extends AppCompatActivity {

    private Button btnCancelar, btnAsalto, btnAccidente, btnMecanica;
    private RedSOSDb db;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

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

        db = new RedSOSDb(this);

        btnCancelar = findViewById(R.id.btn_cancelar_sos);
        btnAsalto = findViewById(R.id.btn_sos_asalto);
        btnAccidente = findViewById(R.id.btn_sos_accidente);
        btnMecanica = findViewById(R.id.btn_sos_mecanica);

        btnCancelar.setOnClickListener(v -> {
            Toast.makeText(this, "Alerta cancelada", Toast.LENGTH_SHORT).show();
            finish();
        });

        View.OnClickListener enviarAlerta = v -> {
            String tipo = ((Button) v).getText().toString();
            double lat = MiUbicacion.IQUIQUE.latitude;
            double lon = MiUbicacion.IQUIQUE.longitude;

            executor.execute(() -> {
                db.insertarReporte(tipo, "Emergencia reportada", lat, lon);
                runOnUiThread(() -> {
                    Toast.makeText(this, "🚨 Alerta: " + tipo + " guardada",
                            Toast.LENGTH_LONG).show();
                    finish();
                });
            });
        };

        btnAsalto.setOnClickListener(enviarAlerta);
        btnAccidente.setOnClickListener(enviarAlerta);
        btnMecanica.setOnClickListener(enviarAlerta);
    }
}
