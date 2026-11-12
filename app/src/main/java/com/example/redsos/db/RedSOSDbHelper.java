package com.example.redsos.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * RedSOSDbHelper crea y mantiene la base de datos SQLite de la app.
 * Extiende de SQLiteOpenHelper, que es la clase de Android para esto.
 *
 * onCreate() corre la primera vez que la app usa la base de datos y crea las tablas.
 * onUpgrade() corre cuando subimos VERSION: borra las tablas viejas y las crea de nuevo.
 *
 * Es un singleton: siempre se usa la misma instancia para no abrir la
 * base de datos varias veces.
 */
public class RedSOSDbHelper extends SQLiteOpenHelper {

    private static final String NOMBRE_DB = "redsos.db";

    // Si cambiamos las tablas hay que subir este número para que corra onUpgrade()
    private static final int VERSION = 3;

    private static RedSOSDbHelper instancia;

    /**
     * Devuelve siempre la misma instancia del helper.
     * @param context el contexto de la app
     * @return la instancia única de RedSOSDbHelper
     */
    public static RedSOSDbHelper get(Context context) {
        if (instancia == null) {
            instancia = new RedSOSDbHelper(context.getApplicationContext());
        }
        return instancia;
    }

    private RedSOSDbHelper(Context context) {
        super(context, NOMBRE_DB, null, VERSION);
    }

    /**
     * Se ejecuta la primera vez que se abre la base de datos.
     * Crea las 3 tablas del contrato.
     * @param db la base de datos recién creada
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ContratoRedSOS.TablaContactos.CREAR_TABLA);
        db.execSQL(ContratoRedSOS.TablaPerfil.CREAR_TABLA);
        db.execSQL(ContratoRedSOS.TablaReportes.CREAR_TABLA);
    }

    /**
     * Se ejecuta cuando VERSION es más grande que la que tenía el celular.
     * Acá lo simple es borrar todo y volver a crear (los datos se pierden).
     * @param db la base de datos a actualizar
     * @param oldVersion la versión que tenía antes
     * @param newVersion la versión nueva
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ContratoRedSOS.TablaContactos.NOMBRE_TABLA);
        db.execSQL("DROP TABLE IF EXISTS " + ContratoRedSOS.TablaPerfil.NOMBRE_TABLA);
        db.execSQL("DROP TABLE IF EXISTS " + ContratoRedSOS.TablaReportes.NOMBRE_TABLA);
        onCreate(db);
    }
}
