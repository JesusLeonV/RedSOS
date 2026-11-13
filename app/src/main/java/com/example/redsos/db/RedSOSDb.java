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

/**
 * RedSOSDb tiene todas las operaciones de la base de datos:
 * guardar, leer, actualizar y borrar (CRUD).
 * Es la única clase que habla directo con SQLite, las pantallas no tocan la BD.
 *
 * Para escribir usa ContentValues y para leer usa Cursor.
 * Todas las consultas con condiciones usan selectionArgs (los "?") para
 * evitar inyección SQL.
 */
public class RedSOSDb {

    private final RedSOSDbHelper helper;

    public RedSOSDb(Context context) {
        this.helper = RedSOSDbHelper.get(context);
    }

    // ==================== CONTACTOS CRUD ====================

    /**
     * Guarda un contacto nuevo en la tabla contactos.
     *
     * @param nombre     nombre de la persona
     * @param parentesco relación con el usuario (madre, hermano, etc)
     * @param telefono   número de teléfono
     * @param lat        latitud de la ubicación del contacto (puede ser 0)
     * @param lon        longitud de la ubicación del contacto (puede ser 0)
     * @return el id que la base de datos le asignó al contacto nuevo
     */
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

    /**
     * Lee todos los contactos guardados, del más nuevo al más viejo.
     *
     * @return lista de contactos. Cada contacto es un String[] con:
     *         [0] id, [1] nombre, [2] parentesco, [3] teléfono
     */
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

    /**
     * Actualiza los datos de un contacto que ya existe.
     *
     * @param id         el id del contacto a cambiar
     * @param nombre     el nombre nuevo
     * @param parentesco el parentesco nuevo
     * @param telefono   el teléfono nuevo
     * @return cuántas filas se cambiaron (0 si no existía ese id)
     */
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

    /**
     * Borra un contacto de la tabla.
     *
     * @param id el id del contacto a borrar
     * @return cuántas filas se borraron (0 si no existía ese id)
     */
    public int eliminarContacto(int id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(
                TablaContactos.NOMBRE_TABLA,
                TablaContactos._ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    /**
     * Busca los contactos que todavía no se mandaron al servidor.
     *
     * @return lista de contactos con sincronizado = 0.
     *         Cada uno es: [0] id, [1] nombre, [2] parentesco, [3] teléfono
     */
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

    /**
     * Marca un contacto como ya sincronizado para no mandarlo de nuevo.
     *
     * @param id el id del contacto que ya llegó al servidor
     */
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

    /**
     * Guarda el perfil del usuario con sus datos personales y médicos.
     * Si ya había un perfil lo reemplaza (solo hay uno).
     *
     * @param nombre            nombre completo
     * @param rut               RUT chileno
     * @param celular           número de celular
     * @param correo            correo electrónico
     * @param tipoSangre        tipo de sangre (A+, O-, etc)
     * @param alergias          alergias que tiene
     * @param enfermedades      enfermedades crónicas
     * @param medicamentos      medicamentos que toma seguido
     * @param contactoEmergencia nombre de su contacto de emergencia
     * @param telefonoEmergencia teléfono de su contacto de emergencia
     * @param observaciones     cualquier dato extra útil en una emergencia
     */
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

    /**
     * Lee el perfil guardado.
     *
     * @return un String[] con: [0] nombre, [1] rut, [2] celular, [3] correo,
     *         [4] tipo de sangre, [5] alergias, [6] enfermedades, [7] medicamentos,
     *         [8] contacto de emergencia, [9] teléfono de emergencia, [10] observaciones.
     *         Devuelve null si todavía no hay perfil guardado.
     */
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

    /**
     * Marca el perfil como ya sincronizado con el servidor.
     */
    public void marcarPerfilSincronizado() {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(TablaPerfil.COL_SINCRONIZADO, 1);
        db.update(TablaPerfil.NOMBRE_TABLA, valores, null, null);
    }

    // ==================== REPORTES CRUD ====================

    /**
     * Guarda un reporte de incidente (o check-in de seguridad).
     * Se usa desde el mapa, el botón SOS y el sensor de sacudidas.
     *
     * @param tipo        tipo de reporte (Peligro, Albergue, Agua, Ayuda, Seguro, etc)
     * @param descripcion texto que explica el reporte
     * @param lat         latitud donde pasó
     * @param lon         longitud donde pasó
     * @param rutaFoto    ruta de la foto de evidencia, o null si no tiene
     * @return el id que la base de datos le asignó al reporte
     */
    public long insertarReporte(String tipo, String descripcion, double lat, double lon,
                                String rutaFoto) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(TablaReportes.COL_TIPO, tipo);
        valores.put(TablaReportes.COL_DESCRIPCION, descripcion);
        valores.put(TablaReportes.COL_LATITUD, lat);
        valores.put(TablaReportes.COL_LONGITUD, lon);
        valores.put(TablaReportes.COL_FECHA, System.currentTimeMillis());
        valores.put(TablaReportes.COL_RUTA_FOTO, rutaFoto);
        valores.put(TablaReportes.COL_SINCRONIZADO, 0);
        return db.insert(TablaReportes.NOMBRE_TABLA, null, valores);
    }

    /**
     * Lee todos los reportes, del más reciente al más viejo.
     *
     * @return lista de reportes. Cada uno es un String[] con:
     *         [0] id, [1] tipo, [2] descripción, [3] latitud, [4] longitud, [5] ruta de foto
     */
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
                    String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(TablaReportes.COL_LONGITUD))),
                    cursor.getString(cursor.getColumnIndexOrThrow(TablaReportes.COL_RUTA_FOTO))
            };
            lista.add(fila);
        }
        cursor.close();
        return lista;
    }

    /**
     * Borra un reporte de la tabla.
     *
     * @param id el id del reporte a borrar
     * @return cuántas filas se borraron (0 si no existía ese id)
     */
    public int eliminarReporte(int id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(
                TablaReportes.NOMBRE_TABLA,
                TablaReportes._ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    /**
     * Marca un reporte como ya sincronizado con el servidor.
     *
     * @param id el id del reporte que ya se mandó
     */
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

    // ==================== RESPALDO AL SERVIDOR ====================

    /**
     * Lee el perfil solo si todavía no se ha mandado al servidor
     * (sincronizado = 0). Es lo que sube la clase RespaldoServidor.
     *
     * @return el perfil en el mismo formato que {@link #obtenerPerfil()},
     *         o null si no hay perfil o ya está sincronizado.
     */
    public String[] obtenerPerfilSinSincronizar() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(
                TablaPerfil.NOMBRE_TABLA,
                null,
                TablaPerfil.COL_SINCRONIZADO + " = ?",
                new String[]{"0"},
                null, null, null
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

    /**
     * Busca los contactos que todavía no se mandaron al servidor,
     * con todas sus columnas (para el respaldo completo).
     *
     * @return lista de contactos pendientes. Cada uno es:
     *         [0] id, [1] nombre, [2] parentesco, [3] teléfono,
     *         [4] latitud, [5] longitud
     */
    public List<String[]> obtenerContactosSinSincronizarCompletos() {
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
                    cursor.getString(cursor.getColumnIndexOrThrow(TablaContactos.COL_TELEFONO)),
                    String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(TablaContactos.COL_LATITUD))),
                    String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(TablaContactos.COL_LONGITUD)))
            };
            lista.add(fila);
        }
        cursor.close();
        return lista;
    }

    /**
     * Busca los reportes que todavía no se mandaron al servidor,
     * con todas sus columnas (para el respaldo completo).
     *
     * @return lista de reportes pendientes. Cada uno es:
     *         [0] id, [1] tipo, [2] descripción, [3] latitud, [4] longitud,
     *         [5] ruta de foto, [6] fecha (milisegundos)
     */
    public List<String[]> obtenerReportesSinSincronizar() {
        List<String[]> lista = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(
                TablaReportes.NOMBRE_TABLA,
                null,
                TablaReportes.COL_SINCRONIZADO + " = ?",
                new String[]{"0"},
                null, null, null
        );
        while (cursor.moveToNext()) {
            String[] fila = new String[]{
                    String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(TablaReportes._ID))),
                    cursor.getString(cursor.getColumnIndexOrThrow(TablaReportes.COL_TIPO)),
                    cursor.getString(cursor.getColumnIndexOrThrow(TablaReportes.COL_DESCRIPCION)),
                    String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(TablaReportes.COL_LATITUD))),
                    String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(TablaReportes.COL_LONGITUD))),
                    cursor.getString(cursor.getColumnIndexOrThrow(TablaReportes.COL_RUTA_FOTO)),
                    String.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(TablaReportes.COL_FECHA)))
            };
            lista.add(fila);
        }
        cursor.close();
        return lista;
    }
}
