package com.example.aplicacionrecetas;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;

public class BaseDatos extends SQLiteOpenHelper {

    public BaseDatos(@Nullable Context context, @Nullable String name,
                @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Usuario ('Nombre' VARCHAR(20) PRIMARY KEY NOT NULL, 'Contrasena' VARCHAR(20) NOT NULL," +
                "'RecetasCreadas' VARCHAR(50), 'Icono')");
        db.execSQL("INSERT INTO Usuario ('Nombre','Contrasena','RecetasCreadas','Icono') VALUES ('n', 'n', 'Pasta,Pollo,Hamburguesa', null)");
        db.execSQL("CREATE TABLE Receta ('Nombre' VARCHAR(100) PRIMARY KEY NOT NULL, 'Imagen' BLOB," +
                "'Ingredientes' VARCHAR(30), 'PasosSeguir' VARCHAR(500))");
        db.execSQL("INSERT INTO Receta ('Nombre','Imagen','Ingredientes', 'PasosSeguir') VALUES ('Pasta', null, 'Pasta, Tomate, Carne'," +
                "'Cocermos la pasta. Hacemos la carne. Le echamos el tomate a la pasta y lo juntamos todo.')");
        db.execSQL("INSERT INTO Receta ('Nombre','Imagen','Ingredientes', 'PasosSeguir') VALUES ('Pollo', null, 'Pollo, Patatas, Verduras'," +
                "'Cocinamos el pollo. Asamos las patatas y hacemos las verduras.')");
        db.execSQL("INSERT INTO Receta ('Nombre','Imagen','Ingredientes', 'PasosSeguir') VALUES ('Hamburguesa', null, 'Pan, Hamburguesa, Queso'," +
                "'Cocinamos la hamburguesa, la metemos entre el pan y le añadimos el queso.')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
