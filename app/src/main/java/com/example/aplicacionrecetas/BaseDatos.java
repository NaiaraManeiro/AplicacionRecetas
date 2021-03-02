package com.example.aplicacionrecetas;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class BaseDatos extends SQLiteOpenHelper {

    public BaseDatos(@Nullable Context context, @Nullable String name,
                @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Usuario ('Nombre' VARCHAR(20) PRIMARY KEY NOT NULL, 'Contrasena' VARCHAR(20) NOT NULL," +
                "'recetasCreadas' VARCHAR(50))");
        db.execSQL("CREATE TABLE Receta ('IdReceta' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 'Nombre' VARCHAR(100), " +
                "'ingredientes' VARCHAR(30), 'pasosSeguir' VARCHAR(500))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
