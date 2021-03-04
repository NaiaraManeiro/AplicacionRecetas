package com.example.aplicacionrecetas;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.sql.Blob;

public class InfoReceta extends AppCompatActivity {

    private String recetaNombre;
    private byte[] imagen;
    private String ingredientes;
    private String pasos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_receta);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            recetaNombre = extras.getString("nombreReceta");
        }

        BaseDatos GestorDB = new BaseDatos (this, "RecetasBD", null, 1);
        SQLiteDatabase bd = GestorDB.getWritableDatabase();
        String[] campos = new String[] {"Imagen", "Ingredientes", "PasosSeguir"};
        String[] argumentos = new String[] {recetaNombre};
        Cursor cu = bd.query("Receta", campos,"Nombre=?", argumentos,null,null,null);
        while (cu.moveToNext()){
            imagen = cu.getBlob(0);
            ingredientes = cu.getString(1);
            pasos = cu.getString(2);
        }
        cu.close();
        bd.close();

        ImageView imagenReceta = findViewById(R.id.imagenReceta);
        //imagenReceta.setImageBitmap(imagen);
        TextView nomReceta = findViewById(R.id.nombreReceta);
        nomReceta.setText(recetaNombre);
        TextView pasosReceta = findViewById(R.id.pasosSeguir);
        pasosReceta.setText(pasos);

        Button volver = findViewById(R.id.volverMenu);
        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}