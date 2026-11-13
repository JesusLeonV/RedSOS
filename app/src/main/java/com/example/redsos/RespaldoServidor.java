package com.example.redsos;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.example.redsos.db.RedSOSDb;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Sube los datos pendientes de la base local al servidor de respaldo:
 * {@code https://cloudsolutions.cl/api/redsos/backup}
 *
 * <p>Funciona así:</p>
 * <ol>
 *   <li>Lee solo las filas con sincronizado = 0 (perfil, contactos y reportes).</li>
 *   <li>Las arma en un JSON y las manda por POST en un hilo secundario
 *       (nunca en el hilo principal, para no congelar la pantalla).</li>
 *   <li>Si el servidor responde 2xx, marca las filas como sincronizadas
 *       para no volver a subirlas.</li>
 * </ol>
 *
 * <p>La llave del endpoint va en {@code local.properties} (REDSOS_API_KEY)
 * y entra al APK por BuildConfig; no está escrita en el código fuente.</p>
 */
public class RespaldoServidor {

    private static final String TAG = "RespaldoServidor";
    private static final String URL_RESPALDO = "https://cloudsolutions.cl/api/redsos/backup";
    private static final int TIMEOUT_MS = 15000;

    /** Evita lanzar dos subidas al mismo tiempo. */
    private static final AtomicBoolean EN_CURSO = new AtomicBoolean(false);

    private RespaldoServidor() {
        // utilidad: no se instancia
    }

    /**
     * Pide subir lo que esté pendiente. Si ya hay una subida en curso
     * no hace nada (se puede llamar desde onCreate y onResume sin miedo).
     *
     * @param contexto contexto de la app, para leer la base de datos
     */
    public static void subirPendientes(final Context contexto) {
        if (!EN_CURSO.compareAndSet(false, true)) {
            Log.d(TAG, "ya hay un respaldo en curso, se omite");
            return;
        }
        new Thread(() -> {
            try {
                subir(contexto.getApplicationContext());
            } finally {
                EN_CURSO.set(false);
            }
        }, "redsos-respaldo").start();
    }

    /** Corre en el hilo secundario: arma el JSON, lo manda y marca filas. */
    private static void subir(Context ctx) {
        try {
            RedSOSDb db = new RedSOSDb(ctx);

            String[] perfil = db.obtenerPerfilSinSincronizar();
            List<String[]> contactos = db.obtenerContactosSinSincronizarCompletos();
            List<String[]> reportes = db.obtenerReportesSinSincronizar();

            if (perfil == null && contactos.isEmpty() && reportes.isEmpty()) {
                Log.d(TAG, "nada pendiente de subir");
                return;
            }

            JSONObject raiz = new JSONObject();
            raiz.put("app", "RedSOS");
            raiz.put("version", 1);
            raiz.put("fecha", System.currentTimeMillis());
            raiz.put("dispositivo", Build.MODEL != null ? Build.MODEL : "desconocido");

            if (perfil != null) {
                JSONObject p = new JSONObject();
                p.put("nombre", valor(perfil[0]));
                p.put("rut", valor(perfil[1]));
                p.put("celular", valor(perfil[2]));
                p.put("correo", valor(perfil[3]));
                p.put("tipo_sangre", valor(perfil[4]));
                p.put("alergias", valor(perfil[5]));
                p.put("enfermedades", valor(perfil[6]));
                p.put("medicamentos", valor(perfil[7]));
                p.put("contacto_emergencia", valor(perfil[8]));
                p.put("telefono_emergencia", valor(perfil[9]));
                p.put("observaciones", valor(perfil[10]));
                raiz.put("perfil", p);
            }

            JSONArray arrContactos = new JSONArray();
            for (String[] c : contactos) {
                JSONObject o = new JSONObject();
                o.put("id", c[0]);
                o.put("nombre", valor(c[1]));
                o.put("parentesco", valor(c[2]));
                o.put("telefono", valor(c[3]));
                o.put("latitud", c[4]);
                o.put("longitud", c[5]);
                arrContactos.put(o);
            }
            raiz.put("contactos", arrContactos);

            JSONArray arrReportes = new JSONArray();
            for (String[] r : reportes) {
                JSONObject o = new JSONObject();
                o.put("id", r[0]);
                o.put("tipo", valor(r[1]));
                o.put("descripcion", valor(r[2]));
                o.put("latitud", r[3]);
                o.put("longitud", r[4]);
                o.put("ruta_foto", valor(r[5]));
                o.put("fecha", r[6]);
                arrReportes.put(o);
            }
            raiz.put("reportes", arrReportes);

            if (!enviar(raiz.toString())) {
                Log.w(TAG, "el servidor no respondió, se reintentará la próxima vez");
                return;
            }

            // Solo ahora marcamos como sincronizado lo que sí llegó
            if (perfil != null) {
                db.marcarPerfilSincronizado();
            }
            for (String[] c : contactos) {
                db.marcarContactoSincronizado(Integer.parseInt(c[0]));
            }
            for (String[] r : reportes) {
                db.marcarReporteSincronizado(Integer.parseInt(r[0]));
            }
            Log.i(TAG, "respaldo ok: " + contactos.size() + " contactos, "
                    + reportes.size() + " reportes"
                    + (perfil != null ? ", 1 perfil" : ""));

        } catch (Exception e) {
            Log.e(TAG, "error al preparar el respaldo: " + e.getMessage());
        }
    }

    /**
     * Manda el JSON por POST y devuelve true si el servidor aceptó (2xx).
     *
     * @param cuerpo el JSON a subir
     * @return true si el servidor respondió 2xx
     */
    private static boolean enviar(String cuerpo) {
        HttpURLConnection conexion = null;
        try {
            URL url = new URL(URL_RESPALDO);
            conexion = (HttpURLConnection) url.openConnection();
            conexion.setRequestMethod("POST");
            conexion.setConnectTimeout(TIMEOUT_MS);
            conexion.setReadTimeout(TIMEOUT_MS);
            conexion.setDoOutput(true);
            conexion.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conexion.setRequestProperty("X-RedSOS-Key", BuildConfig.REDSOS_API_KEY);

            byte[] datos = cuerpo.getBytes(StandardCharsets.UTF_8);
            conexion.setFixedLengthStreamingMode(datos.length);
            try (OutputStream salida = conexion.getOutputStream()) {
                salida.write(datos);
            }

            int codigo = conexion.getResponseCode();
            Log.d(TAG, "servidor respondió " + codigo);
            return codigo >= 200 && codigo < 300;
        } catch (Exception e) {
            Log.e(TAG, "sin conexión con el servidor: " + e.getMessage());
            return false;
        } finally {
            if (conexion != null) {
                conexion.disconnect();
            }
        }
    }

    /** Convierte null en JSONObject.NULL para que no se pierda la clave. */
    private static Object valor(String texto) {
        return texto == null ? JSONObject.NULL : texto;
    }
}
