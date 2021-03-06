package com.example.aplicacionrecetas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.DialogFragment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class AnadirReceta extends AppCompatActivity {

    private String in;
    private boolean main = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anadir_receta);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            main = extras.getBoolean("main");
        }

        //Creamos una receta para meter los datos
        BaseDatos GestorDB = new BaseDatos (this, "RecetasBD", null, 1);
        SQLiteDatabase bd = GestorDB.getWritableDatabase();
        ContentValues nuevo = new ContentValues();
        nuevo.put("Nombre", "NewReceta");
        bd.insert("Receta", null, nuevo);
        bd.close();

        ImageView imagenNuevaReceta = findViewById(R.id.imagenNuevaReceta);

        imagenNuevaReceta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Añadir permisos para galeria y cámara
            }
        });

        Button anadirIngrediente = findViewById(R.id.botonAddIngrediente);

        anadirIngrediente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogoAddIngrediente = new DialogoAddIngrediente();
                dialogoAddIngrediente.show(getSupportFragmentManager(), "addIngrediente");
            }
        });
        
        Button anadirReceta = findViewById(R.id.botonAddReceta);
        NotificationManager elManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder elBuilder = new NotificationCompat.Builder(this, "IdCanal");
        Intent iMain = new Intent(this, MainActivity.class);

        anadirReceta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText cajaNombreReceta = findViewById(R.id.nombreNuevaReceta);
                String nombreReceta = cajaNombreReceta.getText().toString();
                EditText pasosCaja = findViewById(R.id.pasosSeguir);
                String pasos = pasosCaja.getText().toString();

                //Para mirar si ya existe el nombre de la receta
                SQLiteDatabase bd = GestorDB.getWritableDatabase();
                String[] campos = new String[] {"Nombre"};
                String[] argumentos = new String[] {nombreReceta};
                Cursor cu = bd.query("Receta", campos,"Nombre=?",argumentos,null,null,null);
                int count = cu.getCount();

                //Para mirar si hay ingredientes añadidos
                bd = GestorDB.getWritableDatabase();
                campos = new String[] {"Nombre", "Ingredientes"};
                cu = bd.query("Receta",campos,"Nombre='NewReceta'",null,null,null,null);
                while (cu.moveToNext()) {
                    in = cu.getString(1);
                }
                cu.close();
                bd.close();
                if (nombreReceta.equals("")) {
                    Toast.makeText(getApplicationContext(),"Que no se te olvide escribir el nombre de la receta!", Toast.LENGTH_SHORT).show();
                } else if (count > 0) {
                    Toast.makeText(getApplicationContext(),"Ese nombre ya existe, piensa otro para tu receta.", Toast.LENGTH_SHORT).show();
                    cajaNombreReceta.setText("");
                } else if (pasos.equals("")) {
                    Toast.makeText(getApplicationContext(),"Que no se te olviden escribir los pasos de la receta!", Toast.LENGTH_SHORT).show();
                } else if (in == null) {
                    Toast.makeText(getApplicationContext(),"Que no se te olviden escribir los ingredientes de la receta!", Toast.LENGTH_SHORT).show();
                } else {
                    ImageView imagenReceta = findViewById(R.id.imagenNuevaReceta);
                    Bitmap icon = ((BitmapDrawable)imagenReceta.getDrawable()).getBitmap();
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    icon.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
                    byte[] data =  outputStream.toByteArray();

                    //Añadir a la base de datos
                    bd = GestorDB.getWritableDatabase();
                    ContentValues modificacion = new ContentValues();
                    modificacion.put("Nombre", nombreReceta);
                    modificacion.put("Imagen", data);
                    modificacion.put("PasosSeguir", pasos);
                    bd.update("Receta", modificacion, "Nombre='NewReceta'", null);
                    bd.close();

                    //Añadir una notificación
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel elCanal = new NotificationChannel("IdCanal", "NombreCanal", NotificationManager.IMPORTANCE_DEFAULT);
                        elBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.recetaanadida))
                                .setSmallIcon(android.R.drawable.stat_sys_warning)
                                .setContentTitle("Mensaje de Alerta")
                                .setContentText("La receta "+nombreReceta+" ha sido añadida.")
                                .setVibrate(new long[]{0, 1000, 500, 1000})
                                .setAutoCancel(true);
                        elCanal.enableLights(true);
                        elManager.createNotificationChannel(elCanal);
                    }
                    elManager.notify(1, elBuilder.build());

                    finish();
                    if (main) {
                        startActivity(iMain);
                    }

                }
            }
        });

        Button volver = findViewById(R.id.buttonVolverAdd);
        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase bd = GestorDB.getWritableDatabase();
                bd.delete("Receta", "nombre='NewReceta'", null);
                bd.close();
                finish();
            }
        });
    }
}