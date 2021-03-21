package com.example.aplicacionrecetas;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.AbstractWindowedCursor;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

public class InfoReceta extends AppCompatActivity {

    private String recetaNombre;
    private byte[] imagen;
    private String ingredientes;
    private String pasos;
    private static final int COD_NUEVO_FICHERO = 40;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Para mantener el idioma
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String idiomaL = prefs.getString("idioma", "es");
        if (idiomaL.equals("es")) {
            idioma(getString(R.string.locationES));
        } else if (idiomaL.equals("en")) {
            idioma(getString(R.string.locationEN));
        }

        setContentView(R.layout.activity_info_receta);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            recetaNombre = extras.getString("nombreReceta");
        }

        //Obtenemos todos los datos de la receta a mostrar
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
        nomReceta.setText(getString(R.string.nombre)+" "+recetaNombre);
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
                dialogoVerIngredientes.show(getSupportFragmentManager(), "verIngrediente");
            }
        });

        Button volver = findViewById(R.id.volverMenu);
        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Boton para descargar la receta
        Button descargar = findViewById(R.id.buttonDescargar);
        descargar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TITLE, recetaNombre+".txt");
                startActivityForResult(intent, COD_NUEVO_FICHERO);
            }
        });
    }

    //Para la descarga de la receta
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == COD_NUEVO_FICHERO && resultCode == Activity.RESULT_OK) {
            Uri uri;
            if (data != null) {
                uri = data.getData();
                try {
                    ParcelFileDescriptor pfd = this.getContentResolver().openFileDescriptor(uri, "w");
                    OutputStreamWriter ficheroexterno = new OutputStreamWriter(new FileOutputStream(pfd.getFileDescriptor()));
                    ficheroexterno.write("Nombre de la receta: "+recetaNombre+"\n");
                    ficheroexterno.write("Ingredientes: "+ingredientes+"\n");
                    ficheroexterno.write("Pasos a seguir: "+pasos+"\n");
                    ficheroexterno.close();
                    pfd.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            NotificationManager elManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder elBuilder = new NotificationCompat.Builder(this, "IdCanal");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel elCanal = new NotificationChannel("IdCanal", "NombreCanal", NotificationManager.IMPORTANCE_DEFAULT);
                elBuilder.setSmallIcon(R.drawable.descarga)
                        .setContentTitle(getText(R.string.recetaDescargada))
                        .setContentText(getString(R.string.notiLaReceta)+" '"+recetaNombre+"' "+getString(R.string.notiDescarga))
                        .setVibrate(new long[]{0, 1000, 500, 1000})
                        .setAutoCancel(true);
                elCanal.enableLights(true);
                elManager.createNotificationChannel(elCanal);
            }
            elManager.notify(1, elBuilder.build());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void idioma(String idioma) {
        Locale nuevaloc = new Locale(idioma);
        Locale.setDefault(nuevaloc);
        Configuration configuration = getBaseContext().getResources().getConfiguration();
        configuration.setLocale(nuevaloc);
        configuration.setLayoutDirection(nuevaloc);
        Context context = getBaseContext().createConfigurationContext(configuration);
        getBaseContext().getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
    }
}