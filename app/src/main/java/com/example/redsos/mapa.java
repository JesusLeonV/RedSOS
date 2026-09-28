package com.example.redsos;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class mapa extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private Button btnVolver;
    private GoogleMap mMap;

    // Posición desde la que se calculan las distancias (real si hay GPS, si no Iquique)
    private LatLng ubicacion = MiUbicacion.IQUIQUE;
    private Marker marcadorMiUbicacion;

    // Listas de marcadores por tipo (para filtrar con los chips)
    private final List<Marker> marcadoresAlbergues = new ArrayList<>();
    private final List<Marker> marcadoresPeligro = new ArrayList<>();
    private final List<Marker> marcadoresAgua = new ArrayList<>();
    private final List<Marker> marcadoresAyuda = new ArrayList<>();
    private final List<Polyline> lineasDistancia = new ArrayList<>();

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

        // Ciclo de vida del MapView
        mapView = findViewById(R.id.mapView2);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Volver a la pantalla principal
        btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Botón Mi Ubicación → centra la cámara
        FloatingActionButton fabMiUbicacion = findViewById(R.id.fabMiUbicacion);
        fabMiUbicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMap != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 16));
                }
            }
        });

        // Botón Reportar → agrega un marcador donde está el centro del mapa
        ExtendedFloatingActionButton fabReportar = findViewById(R.id.fabReportar);
        fabReportar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMap == null) return;
                LatLng centro = mMap.getCameraPosition().target;
                Marker m = mMap.addMarker(new MarkerOptions()
                        .position(centro)
                        .title("Incidente reportado")
                        .snippet("A " + calcularDistancia(centro) + " de ti")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                if (m != null) marcadoresPeligro.add(m);
                Toast.makeText(mapa.this, "📍 Incidente reportado en el mapa", Toast.LENGTH_SHORT).show();
            }
        });

        // Chips → filtran (muestran/ocultan) marcadores por tipo
        configurarChip(R.id.chipNegocios, marcadoresAlbergues);
        configurarChip(R.id.chipPeligro, marcadoresPeligro);
        configurarChip(R.id.chipAgua, marcadoresAgua);
        configurarChip(R.id.chipAyuda, marcadoresAyuda);

        // Ubicación GPS (si no hay permiso, se pide y luego vuelve a resolver)
        if (MiUbicacion.tienePermiso(this)) {
            resolverUbicacion();
        } else {
            MiUbicacion.pedirPermiso(this);
        }
    }

    private void resolverUbicacion() {
        MiUbicacion.obtener(this, new MiUbicacion.Callback() {
            @Override
            public void onUbicacion(LatLng nueva, boolean esReal) {
                ubicacion = nueva;
                if (mMap == null) return;
                refrescarPuntos();
                if (esReal) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 15));
                    Toast.makeText(mapa.this, "📍 Ubicación GPS encontrada", Toast.LENGTH_SHORT).show();
                } else {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 14));
                }
            }
        });
    }

    // Cuando Google Maps está listo → agregar puntos
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            if (MiUbicacion.tienePermiso(this)) {
                mMap.setMyLocationEnabled(true);
            }
        } catch (SecurityException ignored) {
        }

        refrescarPuntos();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 15));
    }

    // (Re)dibuja todos los puntos y líneas desde la posición actual.
    // Se puede llamar varias veces (primero con respaldo, luego con GPS real).
    private void refrescarPuntos() {
        if (mMap == null) return;

        // Limpiar lo anterior
        for (Marker m : marcadoresAlbergues) m.remove();
        for (Marker m : marcadoresPeligro) m.remove();
        for (Marker m : marcadoresAgua) m.remove();
        for (Marker m : marcadoresAyuda) m.remove();
        marcadoresAlbergues.clear();
        marcadoresPeligro.clear();
        marcadoresAgua.clear();
        marcadoresAyuda.clear();
        for (Polyline p : lineasDistancia) p.remove();
        lineasDistancia.clear();
        if (marcadorMiUbicacion != null) {
            marcadorMiUbicacion.remove();
            marcadorMiUbicacion = null;
        }

        // Sin permiso no hay punto azul de Google → marcador manual de respaldo
        if (!MiUbicacion.tienePermiso(this)) {
            marcadorMiUbicacion = mMap.addMarker(new MarkerOptions()
                    .position(ubicacion)
                    .title("Mi ubicación")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }

        // ===== PUNTOS DE EJEMPLO EN IQUIQUE =====
        // A) ALBERGUES (verde)
        agregarPunto("Albergue Escuela D-3", -20.2095, -70.1475, marcadoresAlbergues, BitmapDescriptorFactory.HUE_GREEN);
        agregarPunto("Albergue Municipal", -20.2205, -70.1575, marcadoresAlbergues, BitmapDescriptorFactory.HUE_GREEN);

        // B) ZONAS DE PELIGRO (rojo)
        agregarPunto("Derrumbe Av. Angamos", -20.2178, -70.1425, marcadoresPeligro, BitmapDescriptorFactory.HUE_RED);
        agregarPunto("Fuga de gas", -20.2255, -70.1505, marcadoresPeligro, BitmapDescriptorFactory.HUE_RED);

        // C) AGUA POTABLE (celeste)
        agregarPunto("Fuente de agua potable", -20.2130, -70.1565, marcadoresAgua, BitmapDescriptorFactory.HUE_CYAN);

        // D) PUNTOS DE AYUDA (naranja)
        agregarPunto("Hospital Clínico de Iquique", -20.2135, -70.1485, marcadoresAyuda, BitmapDescriptorFactory.HUE_ORANGE);
        agregarPunto("Centro de acopio", -20.2190, -70.1570, marcadoresAyuda, BitmapDescriptorFactory.HUE_ORANGE);
    }

    // Agrega un marcador + línea punteada desde mi ubicación hasta el punto
    private void agregarPunto(String nombre, double lat, double lng,
                              List<Marker> lista, float color) {
        LatLng punto = new LatLng(lat, lng);

        Marker m = mMap.addMarker(new MarkerOptions()
                .position(punto)
                .title(nombre)
                .snippet("A " + calcularDistancia(punto) + " de ti")
                .icon(BitmapDescriptorFactory.defaultMarker(color)));
        if (m != null) lista.add(m);

        // Línea punteada: mi ubicación → el punto
        Polyline linea = mMap.addPolyline(new PolylineOptions()
                .add(ubicacion, punto)
                .color((int) color)
                .width(8f)
                .pattern(Arrays.asList(new Dash(20)))
                .startCap(new RoundCap())
                .endCap(new RoundCap()));
        lineasDistancia.add(linea);
    }

    // Distancia en metros/km entre mi ubicación y un punto
    private String calcularDistancia(LatLng destino) {
        float[] resultado = new float[1];
        Location.distanceBetween(
                ubicacion.latitude, ubicacion.longitude,
                destino.latitude, destino.longitude,
                resultado);
        float metros = resultado[0];
        if (metros < 1000) {
            return Math.round(metros) + " m";
        } else {
            return String.format(Locale.getDefault(), "%.1f km", metros / 1000);
        }
    }

    // Muestra/oculta los marcadores de una lista según el chip
    private void configurarChip(int chipId, final List<Marker> marcadores) {
        Chip chip = findViewById(chipId);
        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (Marker m : marcadores) {
                m.setVisible(isChecked);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                if (mMap != null) {
                    mMap.setMyLocationEnabled(true);
                }
            } catch (SecurityException ignored) {
            }
            resolverUbicacion();
        } else {
            Toast.makeText(this, "Sin permiso de ubicación: se usa Iquique",
                    Toast.LENGTH_SHORT).show();
            if (mMap != null) refrescarPuntos();
        }
    }

    // ---------- Ciclo de vida del MapView ----------
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        MiUbicacion.cancelar();
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
