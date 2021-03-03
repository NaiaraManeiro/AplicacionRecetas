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
        db.execSQL("CREATE TABLE Receta ('Nombre' VARCHAR(100) PRIMARY KEY NOT NULL, 'Imagen' BLOB," +
                "'ingredientes' VARCHAR(30), 'pasosSeguir' VARCHAR(500))");
        db.execSQL("INSERT INTO Receta ('Nombre','Image','ingredientes', 'pasosSeguir') VALUES ('Pasta', null, 'Pasta, Tomate, Carne'," +
                "'Cocermos la pasta. Hacemos la carne. Le echamos el tomate a la pasta y lo juntamos todo.')");
        db.execSQL("INSERT INTO Receta ('Nombre','Image','ingredientes', 'pasosSeguir') VALUES ('Pollo', null, 'Pollo, Patatas, Verduras'," +
                "'Cocinamos el pollo. Asamos las patatas y hacemos las verduras.')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
