package com.example.aplicacionrecetas;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.ContentValues;
import android.database.AbstractWindowedCursor;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class InfoReceta extends AppCompatActivity {

    private String recetaNombre;
    private byte[] imagen;
    private String ingredientes;
    private String pasos;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_receta);

        //anadirImagenes();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            recetaNombre = extras.getString("nombreReceta");
        }

        BaseDatos GestorDB = new BaseDatos (this, "RecetasBD", null, 1);
        SQLiteDatabase bd = GestorDB.getWritableDatabase();
        String[] campos = new String[] {"Imagen", "Ingredientes", "PasosSeguir"};
        String[] argumentos = new String[] {recetaNombre};
        Cursor cu = bd.query("Receta", campos,"Nombre=?", argumentos,null,null,null);
        CursorWindow cw = new CursorWindow("test", 5000000);
        AbstractWindowedCursor ac = (AbstractWindowedCursor) cu;
        ac.setWindow(cw);
        while (ac.moveToNext()){
            imagen = cu.getBlob(0);
            ingredientes = cu.getString(1);
            pasos = cu.getString(2);
        }
        cu.close();
        bd.close();

        ImageView imagenReceta = findViewById(R.id.imagenReceta);
        imagenReceta.setImageBitmap(BitmapFactory.decodeByteArray(imagen, 0, imagen.length));
        TextView nomReceta = findViewById(R.id.nombreReceta);
        nomReceta.setText(recetaNombre);
        TextView pasosReceta = findViewById(R.id.pasosSeguir);
        pasosReceta.setText(pasos);

        ArrayList<String> listaIngredientes = new ArrayList<>(Arrays.asList(ingredientes.split(",")));
        Button verIngredientes = findViewById(R.id.botonVerIngredientes);
        verIngredientes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogoVerIngredientes = new DialogoVerIngredientes();
                Bundle b = new Bundle();
                b.putStringArrayList("listaIngredientes", listaIngredientes);
                b.putBoolean("infoReceta", true);
                dialogoVerIngredientes.setArguments(b);
                dialogoVerIngredientes.show(getSupportFragmentManager(), "addIngrediente");
            }
        });

        Button volver = findViewById(R.id.volverMenu);
        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void anadirImagenes() {
        BaseDatos GestorDB = new BaseDatos (this, "RecetasBD", null, 1);
        SQLiteDatabase bd = GestorDB.getWritableDatabase();

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.pasta);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        icon.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        byte[] data =  outputStream.toByteArray();

        ContentValues modificacion = new ContentValues();
        modificacion.put("Imagen", data);
        bd.update("Receta", modificacion, "Nombre='Pasta'", null);


        Bitmap icon2 = BitmapFactory.decodeResource(getResources(), R.drawable.pollo);
        ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
        icon2.compress(Bitmap.CompressFormat.PNG, 0, outputStream2);
        byte[] data2 =  outputStream2.toByteArray();

        ContentValues modificacion2 = new ContentValues();
        modificacion2.put("Imagen", data2);
        bd.update("Receta", modificacion2, "Nombre='Pollo'", null);

        Bitmap icon3 = BitmapFactory.decodeResource(getResources(), R.drawable.hamburguesa);
        ByteArrayOutputStream outputStream3 = new ByteArrayOutputStream();
        icon3.compress(Bitmap.CompressFormat.PNG, 0, outputStream3);
        byte[] data3 =  outputStream3.toByteArray();

        ContentValues modificacion3 = new ContentValues();
        modificacion3.put("Imagen", data3);
        bd.update("Receta", modificacion3, "Nombre='Hamburguesa'", null);

        bd.close();
    }
}