package com.example.redsos.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.redsos.db.dao.ContactoDao;
import com.example.redsos.db.dao.UsuarioPerfilDao;
import com.example.redsos.db.entity.Contacto;
import com.example.redsos.db.entity.UsuarioPerfil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {UsuarioPerfil.class, Contacto.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    public static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public abstract UsuarioPerfilDao usuarioPerfilDao();
    public abstract ContactoDao contactoDao();

    public static AppDatabase get(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "redsos_db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
