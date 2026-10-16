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

public final class MiUbicacion {

    public static final LatLng IQUIQUE = new LatLng(-20.2148, -70.1524);

    public interface Callback {
        void onUbicacion(LatLng ubicacion, boolean esReal);
    }

    private static FusedLocationProviderClient fusedClient;
    private static LocationCallback locationCallback;

    private MiUbicacion() {}

    public static boolean tienePermiso(Context ctx) {
        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void pedirPermiso(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
    }

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
                            cb.onUbicacion(IQUIQUE, false);
                            pedirActualizaciones(ctx, cb);
                        }
                    }
                });
    }

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

    public static void cancelar() {
        if (fusedClient != null && locationCallback != null) {
            fusedClient.removeLocationUpdates(locationCallback);
        }
        locationCallback = null;
    }
}
