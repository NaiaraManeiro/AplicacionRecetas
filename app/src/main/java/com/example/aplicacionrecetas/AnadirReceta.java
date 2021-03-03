package com.example.aplicacionrecetas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.DialogFragment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class AnadirReceta extends AppCompatActivity {

    private ArrayList<String> ingredientes = new ArrayList<>();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anadir_receta);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String ingrediente = extras.getString("ingrediente");
            ingredientes.add(ingrediente);
        }

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

        Button verIngredientes = findViewById(R.id.botonVerIngredientes);

        verIngredientes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogoVerIngredientes = new DialogoVerIngredientes();
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("ListaIngredientes", ingredientes);
                dialogoVerIngredientes.setArguments(bundle);
                dialogoVerIngredientes.show(getSupportFragmentManager(), "verIngredientes");
            }
        });

        Button anadirReceta = findViewById(R.id.botonAddReceta);
        NotificationManager elManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder elBuilder = new NotificationCompat.Builder(this, "IdCanal");

        BaseDatos GestorDB = new BaseDatos (this, "NombreBD", null, 1);

        anadirReceta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText cajaNombreReceta = findViewById(R.id.nombreNuevaReceta);
                String nombreReceta = cajaNombreReceta.getText().toString();
                String ingredientesString = Arrays.toString(new ArrayList[]{ingredientes});
                EditText pasosCaja = findViewById(R.id.pasosSeguir);
                String pasos = pasosCaja.getText().toString();

                //Añadir a la base de datos
                SQLiteDatabase bd = GestorDB.getWritableDatabase();
                ContentValues nuevo = new ContentValues();
                nuevo.put("Nombre", nombreReceta);
                nuevo.put("Imagen", "");
                nuevo.put("Ingredientes", ingredientesString);
                nuevo.put("PasosSeguir", pasos);
                bd.insert("Receta", null, nuevo);
                bd.close();

                //Añadir una notificacion

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
            }
        });
    }
}