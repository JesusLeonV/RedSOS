package com.example.redsos.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.redsos.db.entity.Contacto;

import java.util.List;

@Dao
public interface ContactoDao {

    @Query("SELECT * FROM contactos ORDER BY id DESC")
    List<Contacto> obtenerTodos();

    @Insert
    long insertar(Contacto contacto);

    @Delete
    void eliminar(Contacto contacto);

    @Query("SELECT * FROM contactos WHERE sincronizado = 0")
    List<Contacto> getSinSincronizar();

    @Query("UPDATE contactos SET sincronizado = 1 WHERE id = :id")
    void marcarSincronizado(int id);
}
