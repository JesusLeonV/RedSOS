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
import androidx.appcompat.app.AlertDialog;
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

/**
 * Pantalla principal de RedSOS.
 * Aquí está el mapa pequeño, el botón de pedir ayuda, el de "estoy bien"
 * y los botones para ir a las otras pantallas.
 * También escucha el acelerómetro: si detecta una sacudida fuerte
 * abre un check-in para que el usuario confirme si está bien o no.
 */
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

        // Respaldamos en el servidor lo que quedó pendiente de la última vez
        RespaldoServidor.subirPendientes(this);

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

        // Conseguimos el acelerómetro del celular
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (MiUbicacion.tienePermiso(this)) {
            pedirUbicacion();
        } else {
            MiUbicacion.pedirPermiso(this);
            txtCoordenadas.setText("-20.2148, -70.1524");
        }

        // Botón de ayuda: abre la pantalla SOS y le manda la ubicación en un Bundle
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

        // Botón "estoy bien": guarda el estado en la base de datos
        btnbn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double lat = ubicacionActual.latitude;
                double lon = ubicacionActual.longitude;
                executor.execute(() -> {
                    db.insertarReporte("Seguro", "Estoy bien", lat, lon, null);
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "✅ Estado enviado: Estoy Bien",
                                    Toast.LENGTH_SHORT).show());
                });
            }
        });

        // Navegación a las otras pantallas
        navInformacion.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, informacion.class)));

        navMapa.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, mapa.class)));

        navContactos.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, contactos.class)));

        navAuxilios.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, auxilios.class)));
    }

    /**
     * Pide la ubicación al GPS y la muestra en pantalla.
     * Si el GPS responde, también mueve el mapa hacia ahí.
     */
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

    /**
     * Se llama cuando el mapa termina de cargar.
     * Activa el punto azul de "mi ubicación" si hay permiso.
     */
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

    /**
     * Se llama cada vez que el acelerómetro cambia.
     * Calcula qué tan fuerte se movió el celular y si pasa de 15
     * (una sacudida fuerte, en reposo da 9.8) abre el check-in.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            double magnitud = Math.sqrt(x * x + y * y + z * z);

            if (magnitud > 15) {
                long ahora = System.currentTimeMillis();
                // Esperamos 5 segundos entre detecciones para que no se abra mil veces
                if (ahora - ultimaDeteccion > 5000) {
                    ultimaDeteccion = ahora;
                    mostrarCheckIn();
                }
            }
        }
    }

    /**
     * Check-in de seguridad: se abre solo cuando el sensor detecta una sacudida.
     * El usuario confirma si está bien o si necesita ayuda y se guarda en la BD.
     */
    private void mostrarCheckIn() {
        new AlertDialog.Builder(this)
                .setTitle("🌍 ¡Sacudida detectada!")
                .setMessage("¿Te encuentras bien?")
                .setPositiveButton("Estoy bien", (d, w) -> guardarCheckIn("Estoy bien"))
                .setNegativeButton("Necesito ayuda", (d, w) -> guardarCheckIn("Necesito ayuda"))
                .show();
    }

    /**
     * Guarda el resultado del check-in en la tabla reportes.
     * @param estado lo que respondió el usuario ("Estoy bien" o "Necesito ayuda")
     */
    private void guardarCheckIn(String estado) {
        double lat = ubicacionActual.latitude;
        double lon = ubicacionActual.longitude;
        executor.execute(() -> {
            db.insertarReporte(estado, "Check-in automatico por sacudida", lat, lon, null);
            runOnUiThread(() ->
                    Toast.makeText(this, "✅ Estado guardado: " + estado,
                            Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    /**
     * Registro el oyente del sensor cuando la pantalla está visible.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (acelerometro != null) {
            sensorManager.registerListener(this, acelerometro,
                    SensorManager.SENSOR_DELAY_NORMAL);
            sensorRegistrado = true;
        }
    }

    /**
     * Quito el oyente cuando la pantalla ya no está visible para gastar menos batería.
     */
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
