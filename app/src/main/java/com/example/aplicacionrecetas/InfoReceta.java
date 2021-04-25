package com.example.aplicacionrecetas;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private ArrayList<String> listaIngredientes;
    private String imagen;
    private String ingredientes;
    private String pasos;

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
        Data datos = new Data.Builder()
                .putString("funcion", "datosReceta")
                .putString("nombreReceta", recetaNombre)
                .build();

        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(RecetasWorker.class)
                .setInputData(datos)
                .build();
        WorkManager.getInstance(InfoReceta.this).getWorkInfoByIdLiveData(otwr.getId())
                .observe(InfoReceta.this, status -> {
                    if (status != null && status.getState().isFinished()) {
                        String result = status.getOutputData().getString("resultado");
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            ingredientes = jsonObject.get("ingredientes").toString();
                            pasos = jsonObject.get("pasos").toString();
                            imagen = jsonObject.get("imagen").toString();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        TextView nomReceta = findViewById(R.id.nombreReceta);
                        nomReceta.setText(getString(R.string.nombre)+" "+recetaNombre);
                        TextView pasosReceta = findViewById(R.id.pasosSeguir);
                        pasosReceta.setText(pasos);
                        listaIngredientes = new ArrayList<>(Arrays.asList(ingredientes.split(",")));
                        //Obtenemos la imagen de Firebase
                        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                        StorageReference pathReference = storageRef.child(imagen);
                        pathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                ImageView imagenReceta = findViewById(R.id.imagenReceta);
                                Glide.with(getApplicationContext()).load(uri).into(imagenReceta);
                            }
                        });
                    }
                });
        WorkManager.getInstance(getApplicationContext()).enqueue(otwr);

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
                DialogFragment dialogoDescargas = new DialogoDescargas();
                Bundle b = new Bundle();
                b.putString("nombreReceta", recetaNombre);
                b.putString("ingredientes", ingredientes);
                b.putString("pasos", pasos);
                dialogoDescargas.setArguments(b);
                dialogoDescargas.show(getSupportFragmentManager(), "verIngrediente");
            }
        });
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