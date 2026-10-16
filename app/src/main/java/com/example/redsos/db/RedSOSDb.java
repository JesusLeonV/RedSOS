package com.example.redsos.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.redsos.db.ContratoRedSOS.TablaContactos;
import com.example.redsos.db.ContratoRedSOS.TablaPerfil;
import com.example.redsos.db.ContratoRedSOS.TablaReportes;

import java.util.ArrayList;
import java.util.List;

public class RedSOSDb {

    private final RedSOSDbHelper helper;

    public RedSOSDb(Context context) {
        this.helper = RedSOSDbHelper.get(context);
    }

    // ==================== CONTACTOS CRUD ====================

    public long insertarContacto(String nombre, String parentesco, String telefono,
                                 double lat, double lon) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(TablaContactos.COL_NOMBRE, nombre);
        valores.put(TablaContactos.COL_PARENTESCO, parentesco);
        valores.put(TablaContactos.COL_TELEFONO, telefono);
        valores.put(TablaContactos.COL_LATITUD, lat);
        valores.put(TablaContactos.COL_LONGITUD, lon);
        valores.put(TablaContactos.COL_SINCRONIZADO, 0);
        return db.insert(TablaContactos.NOMBRE_TABLA, null, valores);
    }

    public List<String[]> obtenerTodosContactos() {
        List<String[]> lista = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String[] columnas = {
                TablaContactos._ID,
                TablaContactos.COL_NOMBRE,
                TablaContactos.COL_PARENTESCO,
                TablaContactos.COL_TELEFONO
        };
        Cursor cursor = db.query(
                TablaContactos.NOMBRE_TABLA,
                columnas,
                null, null, null, null,
                TablaContactos._ID + " DESC"
        );
        while (cursor.moveToNext()) {
            String[] fila = new String[]{
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)
            };
            lista.add(fila);
        }
        cursor.close();
        return lista;
    }

    public int actualizarContacto(int id, String nombre, String parentesco, String telefono) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(TablaContactos.COL_NOMBRE, nombre);
        valores.put(TablaContactos.COL_PARENTESCO, parentesco);
        valores.put(TablaContactos.COL_TELEFONO, telefono);
        valores.put(TablaContactos.COL_SINCRONIZADO, 0);
        return db.update(
                TablaContactos.NOMBRE_TABLA,
                valores,
                TablaContactos._ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    public int eliminarContacto(int id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(
                TablaContactos.NOMBRE_TABLA,
                TablaContactos._ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    public List<String[]> obtenerContactosSinSincronizar() {
        List<String[]> lista = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(
                TablaContactos.NOMBRE_TABLA,
                null,
                TablaContactos.COL_SINCRONIZADO + " = ?",
                new String[]{"0"},
                null, null, null
        );
        while (cursor.moveToNext()) {
            String[] fila = new String[]{
                    String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(TablaContactos._ID))),
                    cursor.getString(cursor.getColumnIndexOrThrow(TablaContactos.COL_NOMBRE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(TablaContactos.COL_PARENTESCO)),
                    cursor.getString(cursor.getColumnIndexOrThrow(TablaContactos.COL_TELEFONO))
            };
            lista.add(fila);
        }
        cursor.close();
        return lista;
    }

    public void marcarContactoSincronizado(int id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(TablaContactos.COL_SINCRONIZADO, 1);
        db.update(
                TablaContactos.NOMBRE_TABLA,
                valores,
                TablaContactos._ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    // ==================== PERFIL CRUD ====================

    public void guardarPerfil(String nombre, String rut, String celular, String correo,
                              String tipoSangre, String alergias, String enfermedades,
                              String medicamentos, String contactoEmergencia,
                              String telefonoEmergencia, String observaciones) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(TablaPerfil.COL_NOMBRE, nombre);
        valores.put(TablaPerfil.COL_RUT, rut);
        valores.put(TablaPerfil.COL_CELULAR, celular);
        valores.put(TablaPerfil.COL_CORREO, correo);
        valores.put(TablaPerfil.COL_TIPO_SANGRE, tipoSangre);
        valores.put(TablaPerfil.COL_ALERGIAS, alergias);
        valores.put(TablaPerfil.COL_ENFERMEDADES, enfermedades);
        valores.put(TablaPerfil.COL_MEDICAMENTOS, medicamentos);
        valores.put(TablaPerfil.COL_CONTACTO_EMERGENCIA, contactoEmergencia);
        valores.put(TablaPerfil.COL_TELEFONO_EMERGENCIA, telefonoEmergencia);
        valores.put(TablaPerfil.COL_OBSERVACIONES, observaciones);
        valores.put(TablaPerfil.COL_SINCRONIZADO, 0);
        db.insertWithOnConflict(TablaPerfil.NOMBRE_TABLA, null, valores,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    public String[] obtenerPerfil() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(
                TablaPerfil.NOMBRE_TABLA,
                null, null, null, null, null, null
        );
        String[] resultado = null;
        if (cursor.moveToFirst()) {
            resultado = new String[]{
                    cursor.getString(cursor.getColumnIndexOrThrow(TablaPerfil.COL_NOMBRE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(TablaPerfil.COL_RUT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(TablaPerfil.COL_CELULAR)),
                    cursor.getString(cursor.getColumnIndexOrThrow(TablaPerfil.COL_CORREO)),
                    cursor.getString(cursor.getColumnIndexOrThrow(TablaPerfil.COL_TIPO_SANGRE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(TablaPerfil.COL_ALERGIAS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(TablaPerfil.COL_ENFERMEDADES)),
                    cursor.getString(cursor.getColumnIndexOrThrow(TablaPerfil.COL_MEDICAMENTOS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(TablaPerfil.COL_CONTACTO_EMERGENCIA)),
                    cursor.getString(cursor.getColumnIndexOrThrow(TablaPerfil.COL_TELEFONO_EMERGENCIA)),
                    cursor.getString(cursor.getColumnIndexOrThrow(TablaPerfil.COL_OBSERVACIONES))
            };
        }
        cursor.close();
        return resultado;
    }

    public void marcarPerfilSincronizado() {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(TablaPerfil.COL_SINCRONIZADO, 1);
        db.update(TablaPerfil.NOMBRE_TABLA, valores, null, null);
    }

    // ==================== REPORTES CRUD ====================

    public long insertarReporte(String tipo, String descripcion, double lat, double lon) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(TablaReportes.COL_TIPO, tipo);
        valores.put(TablaReportes.COL_DESCRIPCION, descripcion);
        valores.put(TablaReportes.COL_LATITUD, lat);
        valores.put(TablaReportes.COL_LONGITUD, lon);
        valores.put(TablaReportes.COL_FECHA, System.currentTimeMillis());
        valores.put(TablaReportes.COL_SINCRONIZADO, 0);
        return db.insert(TablaReportes.NOMBRE_TABLA, null, valores);
    }

    public List<String[]> obtenerTodosReportes() {
        List<String[]> lista = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(
                TablaReportes.NOMBRE_TABLA,
                null, null, null, null, null,
                TablaReportes.COL_FECHA + " DESC"
        );
        while (cursor.moveToNext()) {
            String[] fila = new String[]{
                    String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(TablaReportes._ID))),
                    cursor.getString(cursor.getColumnIndexOrThrow(TablaReportes.COL_TIPO)),
                    cursor.getString(cursor.getColumnIndexOrThrow(TablaReportes.COL_DESCRIPCION)),
                    String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(TablaReportes.COL_LATITUD))),
                    String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(TablaReportes.COL_LONGITUD)))
            };
            lista.add(fila);
        }
        cursor.close();
        return lista;
    }

    public int eliminarReporte(int id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(
                TablaReportes.NOMBRE_TABLA,
                TablaReportes._ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    public void marcarReporteSincronizado(int id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(TablaReportes.COL_SINCRONIZADO, 1);
        db.update(
                TablaReportes.NOMBRE_TABLA,
                valores,
                TablaReportes._ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }
}
