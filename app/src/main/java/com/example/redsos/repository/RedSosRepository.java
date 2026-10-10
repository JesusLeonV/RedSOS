package com.example.redsos.repository;

import android.content.Context;
import android.util.Log;

import com.example.redsos.db.AppDatabase;
import com.example.redsos.db.entity.Contacto;
import com.example.redsos.db.entity.UsuarioPerfil;
import com.example.redsos.network.ServidorSync;

import org.json.JSONObject;

import java.util.List;

public class RedSosRepository {

    private static final String TAG = "RedSosRepository";
    private final AppDatabase db;

    public RedSosRepository(Context context) {
        this.db = AppDatabase.get(context);
    }

    public void guardarPerfil(String nombre, String rut, String celular, String correo) {
        AppDatabase.executor.execute(() -> {
            UsuarioPerfil p = db.usuarioPerfilDao().get();
            if (p == null) p = new UsuarioPerfil();
            p.nombre = nombre;
            p.rut = rut;
            p.celular = celular;
            p.correo = correo;
            p.sincronizado = false;
            db.usuarioPerfilDao().guardar(p);

            try {
                JSONObject json = new JSONObject();
                json.put("nombre", nombre);
                json.put("rut", rut);
                json.put("celular", celular);
                json.put("correo", correo);
                if (ServidorSync.enviar("/perfil", json)) {
                    db.usuarioPerfilDao().marcarSincronizado();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage());
            }
        });
    }

    public void guardarContacto(String nombre, String parentesco, String telefono) {
        Contacto c = new Contacto();
        c.nombre = nombre;
        c.parentesco = parentesco;
        c.telefono = telefono;

        AppDatabase.executor.execute(() -> {
            long id = db.contactoDao().insertar(c);
            c.id = (int) id;

            try {
                JSONObject json = new JSONObject();
                json.put("nombre", nombre);
                json.put("parentesco", parentesco);
                json.put("telefono", telefono);
                if (ServidorSync.enviar("/contactos", json)) {
                    db.contactoDao().marcarSincronizado(c.id);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage());
            }
        });
    }

    public void reintentarSincronizacion() {
        AppDatabase.executor.execute(() -> {
            List<Contacto> pendientes = db.contactoDao().getSinSincronizar();
            for (Contacto c : pendientes) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("nombre", c.nombre);
                    json.put("parentesco", c.parentesco);
                    json.put("telefono", c.telefono);
                    if (ServidorSync.enviar("/contactos", json)) {
                        db.contactoDao().marcarSincronizado(c.id);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error: " + e.getMessage());
                }
            }
        });
    }
}
