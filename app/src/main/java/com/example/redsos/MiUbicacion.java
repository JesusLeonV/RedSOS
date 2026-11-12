package com.example.redsos;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * MiUbicacion consigue la ubicación del celular con FusedLocationProviderClient
 * (el GPS de Google, que es más preciso que el LocationManager normal).
 *
 * Primero intenta con la última ubicación conocida.
 * Si no hay ninguna, devuelve Iquique como respaldo (con esReal = false)
 * y se queda esperando al GPS. Cuando el GPS responde, avisa otra vez
 * con esReal = true para que la pantalla actualice el mapa.
 */
public final class MiUbicacion {

    // Respaldo si el GPS no responde: centro de Iquique
    public static final LatLng IQUIQUE = new LatLng(-20.2148, -70.1524);

    /**
     * Avisa cuando hay una ubicación lista.
     * @param ubicacion las coordenadas encontradas
     * @param esReal true si viene del GPS, false si es el respaldo de Iquique
     */
    public interface Callback {
        void onUbicacion(LatLng ubicacion, boolean esReal);
    }

    private static FusedLocationProviderClient fusedClient;
    private static LocationCallback locationCallback;

    private MiUbicacion() {}

    /**
     * Revisa si la app ya tiene permiso de ubicación.
     * @param ctx el contexto
     * @return true si ACCESS_FINE o ACCESS_COARSE ya están concedidos
     */
    public static boolean tienePermiso(Context ctx) {
        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Muestra el diálogo de Android para pedir el permiso de ubicación.
     * La respuesta llega en onRequestPermissionsResult de la activity.
     * @param activity la pantalla que pide el permiso
     */
    public static void pedirPermiso(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
    }

    /**
     * Consigue la ubicación del celular.
     * Primero prueba con la última conocida y si no hay ninguna
     * devuelve el respaldo y pide actualizaciones al GPS.
     *
     * @param ctx el contexto
     * @param cb el callback que recibe la ubicación cuando esté lista
     */
    public static void obtener(Context ctx, Callback cb) {
        if (!tienePermiso(ctx)) {
            cb.onUbicacion(IQUIQUE, false);
            return;
        }

        fusedClient = LocationServices.getFusedLocationProviderClient(ctx);

        fusedClient.getLastLocation()
                .addOnSuccessListener((Activity) ctx, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            cb.onUbicacion(
                                    new LatLng(location.getLatitude(), location.getLongitude()),
                                    true
                            );
                        } else {
                            // Sin última ubicación: respaldo + esperar al GPS
                            cb.onUbicacion(IQUIQUE, false);
                            pedirActualizaciones(ctx, cb);
                        }
                    }
                });
    }

    /**
     * Pide al GPS una actualización nueva (solo 1, con prioridad alta).
     *
     * @param ctx el contexto
     * @param cb el callback que recibe la ubicación cuando llegue
     */
    private static void pedirActualizaciones(Context ctx, Callback cb) {
        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .setMaxUpdates(1)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                Location loc = result.getLastLocation();
                if (loc != null) {
                    cancelar();
                    cb.onUbicacion(
                            new LatLng(loc.getLatitude(), loc.getLongitude()),
                            true
                    );
                }
            }
        };

        try {
            fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
        } catch (SecurityException ignored) {
        }
    }

    /**
     * Deja de escuchar al GPS (se llama al cerrar la pantalla
     * para no gastar batería de más).
     */
    public static void cancelar() {
        if (fusedClient != null && locationCallback != null) {
            fusedClient.removeLocationUpdates(locationCallback);
        }
        locationCallback = null;
    }
}
