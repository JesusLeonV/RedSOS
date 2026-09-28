package com.example.redsos;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;

/**
 * Obtiene la ubicación GPS del dispositivo.
 * Si nunca respondió, se usa Iquique como respaldo y se avisa con esReal = false.
 * Si el GPS llega después, se llama una segunda vez con esReal = true.
 */
public final class MiUbicacion {

    // Respaldo: centro de Iquique
    public static final LatLng IQUIQUE = new LatLng(-20.2148, -70.1524);

    public interface Callback {
        void onUbicacion(LatLng ubicacion, boolean esReal);
    }

    private static LocationManager gestor;
    private static LocationListener oyente;

    private MiUbicacion() {
    }

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
        LocationManager manager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        if (manager == null) {
            cb.onUbicacion(IQUIQUE, false);
            return;
        }

        Location ultima = mejorUltima(ctx, manager);
        if (ultima != null) {
            cb.onUbicacion(new LatLng(ultima.getLatitude(), ultima.getLongitude()), true);
            return;
        }

        // Sin historial todavía: responde ya con el respaldo...
        cb.onUbicacion(IQUIQUE, false);

        // ...y si el GPS entrega después, avisa de nuevo con la posición real
        if (!tienePermiso(ctx)) {
            return;
        }
        cancelar();
        try {
            gestor = manager;
            oyente = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    cancelar();
                    cb.onUbicacion(new LatLng(location.getLatitude(), location.getLongitude()), true);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            };
            String proveedor = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                    ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER;
            manager.requestLocationUpdates(proveedor, 0, 0, oyente, Looper.getMainLooper());
        } catch (SecurityException e) {
            cancelar();
        }
    }

    public static void cancelar() {
        if (gestor != null && oyente != null) {
            try {
                gestor.removeUpdates(oyente);
            } catch (SecurityException ignored) {
            }
        }
        gestor = null;
        oyente = null;
    }

    private static Location mejorUltima(Context ctx, LocationManager manager) {
        if (!tienePermiso(ctx)) {
            return null;
        }
        try {
            Location gps = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location red = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (gps == null) return red;
            if (red == null) return gps;
            return gps.getTime() >= red.getTime() ? gps : red;
        } catch (SecurityException e) {
            return null;
        }
    }
}
