package com.example.redsos;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.redsos.db.RedSOSDb;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener {

    private Button btnbn, btnayuda;
    private Button navInformacion, navMapa, navContactos, navAuxilios;
    private GoogleMap mapaHome;
    private TextView txtCoordenadas;
    private LatLng ubicacionActual = MiUbicacion.IQUIQUE;

    private RedSOSDb db;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private SensorManager sensorManager;
    private Sensor acelerometro;
    private boolean sensorRegistrado = false;
    private long ultimaDeteccion = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new RedSOSDb(this);

        btnayuda = findViewById(R.id.btnayuda);
        btnbn = findViewById(R.id.btnbn);
        navInformacion = findViewById(R.id.navInformacion);
        navMapa = findViewById(R.id.navMapa);
        navContactos = findViewById(R.id.navContactos);
        navAuxilios = findViewById(R.id.navAuxilios);
        txtCoordenadas = findViewById(R.id.coordenadas);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapViewHome);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (MiUbicacion.tienePermiso(this)) {
            pedirUbicacion();
        } else {
            MiUbicacion.pedirPermiso(this);
            txtCoordenadas.setText("-20.2148, -70.1524");
        }

        btnayuda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, dialog_sos_emergency.class);
                Bundle datos = new Bundle();
                datos.putString("origen", "boton_ayuda");
                datos.putDouble("lat", ubicacionActual.latitude);
                datos.putDouble("lon", ubicacionActual.longitude);
                intent.putExtras(datos);
                startActivity(intent);
            }
        });

        btnbn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double lat = ubicacionActual.latitude;
                double lon = ubicacionActual.longitude;
                executor.execute(() -> {
                    db.insertarReporte("Seguro", "Estoy bien", lat, lon);
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "✅ Estado enviado: Estoy Bien",
                                    Toast.LENGTH_SHORT).show());
                });
            }
        });

        navInformacion.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, informacion.class)));

        navMapa.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, mapa.class)));

        navContactos.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, contactos.class)));

        navAuxilios.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, auxilios.class)));
    }

    private void pedirUbicacion() {
        MiUbicacion.obtener(this, (ubicacion, esReal) -> {
            ubicacionActual = ubicacion;
            txtCoordenadas.setText(String.format(Locale.US, "%.5f, %.5f",
                    ubicacion.latitude, ubicacion.longitude));
            if (mapaHome != null) {
                if (esReal) {
                    mapaHome.animateCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 15));
                } else {
                    mapaHome.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 14));
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapaHome = googleMap;
        try {
            if (MiUbicacion.tienePermiso(this)) {
                mapaHome.setMyLocationEnabled(true);
            }
        } catch (SecurityException ignored) {}
        mapaHome.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionActual, 15));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            double magnitud = Math.sqrt(x * x + y * y + z * z);

            if (magnitud > 15) {
                long ahora = System.currentTimeMillis();
                if (ahora - ultimaDeteccion > 5000) {
                    ultimaDeteccion = ahora;
                    Toast.makeText(this, "🌍 ¡Sacudida detectada! ¿Estás bien?",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onResume() {
        super.onResume();
        if (acelerometro != null) {
            sensorManager.registerListener(this, acelerometro,
                    SensorManager.SENSOR_DELAY_NORMAL);
            sensorRegistrado = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorRegistrado) {
            sensorManager.unregisterListener(this);
            sensorRegistrado = false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pedirUbicacion();
            try {
                if (mapaHome != null) mapaHome.setMyLocationEnabled(true);
            } catch (SecurityException ignored) {}
        }
    }

    @Override
    protected void onDestroy() {
        MiUbicacion.cancelar();
        super.onDestroy();
    }
}
