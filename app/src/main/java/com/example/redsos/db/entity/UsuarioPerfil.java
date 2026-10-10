package com.example.redsos.db.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "usuario_perfil")
public class UsuarioPerfil {

    @PrimaryKey @NonNull
    public String id = "unico";

    public String nombre;
    public String rut;
    public String celular;
    public String correo;

    @ColumnInfo(name = "sincronizado")
    public boolean sincronizado;
}
