package com.example.redsos.db;

import android.provider.BaseColumns;

public final class ContratoRedSOS {

    private ContratoRedSOS() {}

    public static abstract class TablaContactos implements BaseColumns {
        public static final String NOMBRE_TABLA = "contactos";
        public static final String COL_NOMBRE = "nombre";
        public static final String COL_PARENTESCO = "parentesco";
        public static final String COL_TELEFONO = "telefono";
        public static final String COL_LATITUD = "latitud";
        public static final String COL_LONGITUD = "longitud";
        public static final String COL_SINCRONIZADO = "sincronizado";

        public static final String CREAR_TABLA =
                "CREATE TABLE " + NOMBRE_TABLA + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NOMBRE + " TEXT NOT NULL, " +
                        COL_PARENTESCO + " TEXT, " +
                        COL_TELEFONO + " TEXT NOT NULL, " +
                        COL_LATITUD + " REAL, " +
                        COL_LONGITUD + " REAL, " +
                        COL_SINCRONIZADO + " INTEGER DEFAULT 0)";
    }

    public static abstract class TablaPerfil implements BaseColumns {
        public static final String NOMBRE_TABLA = "perfil";
        public static final String COL_NOMBRE = "nombre";
        public static final String COL_RUT = "rut";
        public static final String COL_CELULAR = "celular";
        public static final String COL_CORREO = "correo";
        public static final String COL_TIPO_SANGRE = "tipo_sangre";
        public static final String COL_ALERGIAS = "alergias";
        public static final String COL_ENFERMEDADES = "enfermedades";
        public static final String COL_MEDICAMENTOS = "medicamentos";
        public static final String COL_CONTACTO_EMERGENCIA = "contacto_emergencia";
        public static final String COL_TELEFONO_EMERGENCIA = "telefono_emergencia";
        public static final String COL_OBSERVACIONES = "observaciones";
        public static final String COL_SINCRONIZADO = "sincronizado";

        public static final String CREAR_TABLA =
                "CREATE TABLE " + NOMBRE_TABLA + " (" +
                        _ID + " INTEGER PRIMARY KEY, " +
                        COL_NOMBRE + " TEXT, " +
                        COL_RUT + " TEXT, " +
                        COL_CELULAR + " TEXT, " +
                        COL_CORREO + " TEXT, " +
                        COL_TIPO_SANGRE + " TEXT, " +
                        COL_ALERGIAS + " TEXT, " +
                        COL_ENFERMEDADES + " TEXT, " +
                        COL_MEDICAMENTOS + " TEXT, " +
                        COL_CONTACTO_EMERGENCIA + " TEXT, " +
                        COL_TELEFONO_EMERGENCIA + " TEXT, " +
                        COL_OBSERVACIONES + " TEXT, " +
                        COL_SINCRONIZADO + " INTEGER DEFAULT 0)";
    }

    public static abstract class TablaReportes implements BaseColumns {
        public static final String NOMBRE_TABLA = "reportes";
        public static final String COL_TIPO = "tipo";
        public static final String COL_DESCRIPCION = "descripcion";
        public static final String COL_LATITUD = "latitud";
        public static final String COL_LONGITUD = "longitud";
        public static final String COL_FECHA = "fecha";
        public static final String COL_SINCRONIZADO = "sincronizado";

        public static final String CREAR_TABLA =
                "CREATE TABLE " + NOMBRE_TABLA + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_TIPO + " TEXT NOT NULL, " +
                        COL_DESCRIPCION + " TEXT, " +
                        COL_LATITUD + " REAL NOT NULL, " +
                        COL_LONGITUD + " REAL NOT NULL, " +
                        COL_FECHA + " INTEGER, " +
                        COL_SINCRONIZADO + " INTEGER DEFAULT 0)";
    }
}
