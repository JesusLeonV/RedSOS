package com.example.redsos;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class mapa extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng ubicacion = MiUbicacion.IQUIQUE;
    private RedSOSDb db;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private final List<Marker> marcadoresAlbergues = new ArrayList<>();
    private final List<Marker> marcadoresPeligro = new ArrayList<>();
    private final List<Marker> marcadoresAgua = new ArrayList<>();
    private final List<Marker> marcadoresAyuda = new ArrayList<>();
    private final Map<String, List<Marker>> marcadoresPorTipo = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mapa);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new RedSOSDb(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        findViewById(R.id.btnVolver).setOnClickListener(v -> finish());

        findViewById(R.id.fabMiUbicacion).setOnClickListener(v -> {
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 16));
            }
        });

        ExtendedFloatingActionButton fabReportar = findViewById(R.id.fabReportar);
        fabReportar.setOnClickListener(v -> mostrarDialogoReporte());

        configurarChip(R.id.chipAlbergues, marcadoresAlbergues);
        configurarChip(R.id.chipPeligro, marcadoresPeligro);
        configurarChip(R.id.chipAgua, marcadoresAgua);
        configurarChip(R.id.chipAyuda, marcadoresAyuda);

        if (MiUbicacion.tienePermiso(this)) {
            resolverUbicacion();
        } else {
            MiUbicacion.pedirPermiso(this);
        }
    }

    private void resolverUbicacion() {
        MiUbicacion.obtener(this, (nueva, esReal) -> {
            ubicacion = nueva;
            if (mMap == null) return;
            cargarReportesDesdeDB();
            if (esReal) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 15));
            } else {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 14));
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            if (MiUbicacion.tienePermiso(this)) {
                mMap.setMyLocationEnabled(true);
            }
        } catch (SecurityException ignored) {}
        cargarReportesDesdeDB();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 15));
    }

    private void cargarReportesDesdeDB() {
        executor.execute(() -> {
            List<String[]> reportes = db.obtenerTodosReportes();
            runOnUiThread(() -> {
                if (mMap == null) return;
                for (Marker m : marcadoresAlbergues) m.remove();
                for (Marker m : marcadoresPeligro) m.remove();
                for (Marker m : marcadoresAgua) m.remove();
                for (Marker m : marcadoresAyuda) m.remove();
                marcadoresAlbergues.clear();
                marcadoresPeligro.clear();
                marcadoresAgua.clear();
                marcadoresAyuda.clear();

                for (String[] r : reportes) {
                    String tipo = r[1];
                    double lat = Double.parseDouble(r[3]);
                    double lon = Double.parseDouble(r[4]);
                    LatLng punto = new LatLng(lat, lon);

                    float color;
                    List<Marker> lista;
                    switch (tipo) {
                        case "Albergue":
                            color = BitmapDescriptorFactory.HUE_GREEN;
                            lista = marcadoresAlbergues;
                            break;
                        case "Agua":
                            color = BitmapDescriptorFactory.HUE_CYAN;
                            lista = marcadoresAgua;
                            break;
                        case "Ayuda":
                            color = BitmapDescriptorFactory.HUE_ORANGE;
                            lista = marcadoresAyuda;
                            break;
                        default:
                            color = BitmapDescriptorFactory.HUE_RED;
                            lista = marcadoresPeligro;
                            break;
                    }

                    Marker m = mMap.addMarker(new MarkerOptions()
                            .position(punto)
                            .title(tipo + ": " + r[2])
                            .snippet("A " + calcularDistancia(punto) + " de ti")
                            .icon(BitmapDescriptorFactory.defaultMarker(color)));
                    if (m != null) lista.add(m);
                }
            });
        });
    }

    private void mostrarDialogoReporte() {
        final String[] tipos = {"Peligro", "Albergue", "Agua", "Ayuda"};
        new AlertDialog.Builder(this)
                .setTitle("Reportar incidente")
                .setItems(tipos, (dialog, which) -> {
                    String tipo = tipos[which];
                    executor.execute(() -> {
                        db.insertarReporte(tipo, "Reportado por usuario",
                                ubicacion.latitude, ubicacion.longitude);
                        runOnUiThread(() -> {
                            cargarReportesDesdeDB();
                            Toast.makeText(this, "📍 Reporte guardado: " + tipo,
                                    Toast.LENGTH_SHORT).show();
                        });
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private String calcularDistancia(LatLng destino) {
        float[] resultado = new float[1];
        Location.distanceBetween(
                ubicacion.latitude, ubicacion.longitude,
                destino.latitude, destino.longitude, resultado);
        float metros = resultado[0];
        if (metros < 1000) {
            return Math.round(metros) + " m";
        }
        return String.format(Locale.getDefault(), "%.1f km", metros / 1000);
    }

    private void configurarChip(int chipId, List<Marker> marcadores) {
        Chip chip = findViewById(chipId);
        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (Marker m : marcadores) m.setVisible(isChecked);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                if (mMap != null) mMap.setMyLocationEnabled(true);
            } catch (SecurityException ignored) {}
            resolverUbicacion();
        }
    }

    @Override
    protected void onDestroy() {
        MiUbicacion.cancelar();
        super.onDestroy();
    }
}
