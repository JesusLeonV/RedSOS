package com.example.redsos.db.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "contactos")
public class Contacto {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String nombre;
    public String parentesco;
    public String telefono;

    @ColumnInfo(name = "sincronizado")
    public boolean sincronizado;
}
