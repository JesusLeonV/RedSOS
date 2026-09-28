package com.example.redsos;

import android.content.Intent;
import android.content.pm.PackageManager;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Button btnbn, btnayuda;
    private Button navInformacion, navMapa, navContactos, navAuxilios;

    private MapView mapViewHome;
    private GoogleMap mapaHome;
    private TextView txtCoordenadas;
    private LatLng ubicacionActual = MiUbicacion.IQUIQUE;

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

        btnayuda = findViewById(R.id.btnayuda);
        btnbn = findViewById(R.id.btnbn);
        navInformacion = findViewById(R.id.navInformacion);
        navMapa = findViewById(R.id.navMapa);
        navContactos = findViewById(R.id.navContactos);
        navAuxilios = findViewById(R.id.navAuxilios);
        txtCoordenadas = findViewById(R.id.coordenadas);

        // Mapa real de la ubicación actual
        mapViewHome = findViewById(R.id.mapViewHome);
        mapViewHome.onCreate(savedInstanceState);
        mapViewHome.getMapAsync(this);

        // Pedir permiso de ubicación y obtener posición GPS
        if (MiUbicacion.tienePermiso(this)) {
            pedirUbicacion();
        } else {
            MiUbicacion.pedirPermiso(this);
            txtCoordenadas.setText("-20.2148, -70.1524");
        }

        // Botón Pedir Ayuda → pantalla de cuenta regresiva SOS
        btnayuda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, dialog_sos_emergency.class));
            }
        });

        // Botón Estoy Bien → aviso de estado
        btnbn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Estado actualizado: Estoy Bien", Toast.LENGTH_SHORT).show();
            }
        });

        // Navegación a otras pantallas
        navInformacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, informacion.class));
            }
        });

        navMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, mapa.class));
            }
        });

        navContactos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, contactos.class));
            }
        });

        navAuxilios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, auxilios.class));
            }
        });
    }

    private void pedirUbicacion() {
        MiUbicacion.obtener(this, new MiUbicacion.Callback() {
            @Override
            public void onUbicacion(LatLng ubicacion, boolean esReal) {
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
        } catch (SecurityException ignored) {
        }
        mapaHome.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionActual, 15));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pedirUbicacion();
            try {
                if (mapaHome != null) {
                    mapaHome.setMyLocationEnabled(true);
                }
            } catch (SecurityException ignored) {
            }
        } else {
            Toast.makeText(this, "Sin permiso de ubicación: se muestra Iquique",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // Ciclo de vida del MapView
    @Override
    protected void onResume() {
        super.onResume();
        mapViewHome.onResume();
    }

    @Override
    protected void onPause() {
        mapViewHome.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        MiUbicacion.cancelar();
        mapViewHome.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapViewHome.onLowMemory();
    }
}
