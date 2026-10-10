package com.example.redsos.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.redsos.db.entity.UsuarioPerfil;

@Dao
public interface UsuarioPerfilDao {

    @Query("SELECT * FROM usuario_perfil WHERE id = 'unico'")
    UsuarioPerfil get();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void guardar(UsuarioPerfil perfil);

    @Query("UPDATE usuario_perfil SET sincronizado = 1 WHERE id = 'unico'")
    void marcarSincronizado();
}
