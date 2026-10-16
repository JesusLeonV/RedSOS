package com.example.redsos.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RedSOSDbHelper extends SQLiteOpenHelper {

    private static final String NOMBRE_DB = "redsos.db";
    private static final int VERSION = 2;

    private static RedSOSDbHelper instancia;

    public static RedSOSDbHelper get(Context context) {
        if (instancia == null) {
            instancia = new RedSOSDbHelper(context.getApplicationContext());
        }
        return instancia;
    }

    private RedSOSDbHelper(Context context) {
        super(context, NOMBRE_DB, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ContratoRedSOS.TablaContactos.CREAR_TABLA);
        db.execSQL(ContratoRedSOS.TablaPerfil.CREAR_TABLA);
        db.execSQL(ContratoRedSOS.TablaReportes.CREAR_TABLA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ContratoRedSOS.TablaContactos.NOMBRE_TABLA);
        db.execSQL("DROP TABLE IF EXISTS " + ContratoRedSOS.TablaPerfil.NOMBRE_TABLA);
        db.execSQL("DROP TABLE IF EXISTS " + ContratoRedSOS.TablaReportes.NOMBRE_TABLA);
        onCreate(db);
    }
}
