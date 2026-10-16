package com.example.redsos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

public class informacion extends AppCompatActivity {

    private EditText intnmb, intrut, intcelular, intcorreo;
    private TextInputEditText intalergias, intenfermedades, intmedicamentos;
    private TextInputEditText intcontactoemergencia, inttelefonoemergencia, intobservaciones;
    private Spinner spinnerSangre;
    private Button btninformacion, btnVolver;
    private RedSOSDb db;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private static final String[] TIPOS_SANGRE = {
            "Seleccionar...", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "No lo sé"
    };

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

        db = new RedSOSDb(this);

        intnmb = findViewById(R.id.intnmb);
        intrut = findViewById(R.id.intrut);
        intcelular = findViewById(R.id.intcelular);
        intcorreo = findViewById(R.id.intcorreo);
        spinnerSangre = findViewById(R.id.spinnerSangre);
        intalergias = findViewById(R.id.intalergias);
        intenfermedades = findViewById(R.id.intenfermedades);
        intmedicamentos = findViewById(R.id.intmedicamentos);
        intcontactoemergencia = findViewById(R.id.intcontactoemergencia);
        inttelefonoemergencia = findViewById(R.id.inttelefonoemergencia);
        intobservaciones = findViewById(R.id.intobservaciones);
        btninformacion = findViewById(R.id.btninformacion);
        btnVolver = findViewById(R.id.btnVolver);

        ArrayAdapter<String> adapterSangre = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, TIPOS_SANGRE);
        spinnerSangre.setAdapter(adapterSangre);

        cargarDatosGuardados();

        btnVolver.setOnClickListener(v -> finish());

        btninformacion.setOnClickListener(v -> {
            String nombre = intnmb.getText().toString().trim();
            String rut = intrut.getText().toString().trim();
            String celular = intcelular.getText().toString().trim();
            String correo = intcorreo.getText().toString().trim();
            String tipoSangre = spinnerSangre.getSelectedItem().toString();
            String alergias = intalergias.getText().toString().trim();
            String enfermedades = intenfermedades.getText().toString().trim();
            String medicamentos = intmedicamentos.getText().toString().trim();
            String contactoEm = intcontactoemergencia.getText().toString().trim();
            String telefonoEm = inttelefonoemergencia.getText().toString().trim();
            String observaciones = intobservaciones.getText().toString().trim();

            if (nombre.isEmpty() || rut.isEmpty()) {
                Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            executor.execute(() -> {
                db.guardarPerfil(nombre, rut, celular, correo, tipoSangre,
                        alergias, enfermedades, medicamentos,
                        contactoEm, telefonoEm, observaciones);
                runOnUiThread(() -> {
                    Intent resultado = new Intent();
                    resultado.putExtra("nombre", nombre);
                    resultado.putExtra("rut", rut);
                    resultado.putExtra("tipo_sangre", tipoSangre);
                    setResult(RESULT_OK, resultado);
                    Toast.makeText(this, "✅ Información guardada", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        });
    }

    private void cargarDatosGuardados() {
        executor.execute(() -> {
            String[] perfil = db.obtenerPerfil();
            if (perfil != null) {
                runOnUiThread(() -> {
                    if (perfil[0] != null) intnmb.setText(perfil[0]);
                    if (perfil[1] != null) intrut.setText(perfil[1]);
                    if (perfil[2] != null) intcelular.setText(perfil[2]);
                    if (perfil[3] != null) intcorreo.setText(perfil[3]);
                    if (perfil[4] != null) {
                        for (int i = 0; i < TIPOS_SANGRE.length; i++) {
                            if (TIPOS_SANGRE[i].equals(perfil[4])) {
                                spinnerSangre.setSelection(i);
                                break;
                            }
                        }
                    }
                    if (perfil[5] != null) intalergias.setText(perfil[5]);
                    if (perfil[6] != null) intenfermedades.setText(perfil[6]);
                    if (perfil[7] != null) intmedicamentos.setText(perfil[7]);
                    if (perfil[8] != null) intcontactoemergencia.setText(perfil[8]);
                    if (perfil[9] != null) inttelefonoemergencia.setText(perfil[9]);
                    if (perfil[10] != null) intobservaciones.setText(perfil[10]);
                });
            }
        });
    }
}
