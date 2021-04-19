package com.example.aplicacionrecetas;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

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
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class AnadirReceta extends AppCompatActivity implements DialogInterface.OnDismissListener {

    private String in;
    private boolean main = false;
    private String nombreUsuario;
    private byte[] imagen;
    private ImageView imagenNuevaReceta;
    private String recetaExiste;
    private static final String STATE_NOMBRE = "nombreReceta";
    private String nombreReceta;
    private static final String STATE_PASOS = "pasosReceta";
    private String pasosReceta = "";

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

        setContentView(R.layout.activity_anadir_receta);

        if (savedInstanceState != null) {
            nombreReceta = savedInstanceState.getString(STATE_NOMBRE);
            pasosReceta = savedInstanceState.getString(STATE_PASOS);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            main = extras.getBoolean("main");
            nombreUsuario = extras.getString("usuario");
        }

        //Creamos una receta para meter los datos si no existe ya (por el giro de pantalla)
        Data datos = new Data.Builder()
                .putString("funcion", "crearReceta")
                .build();

        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(RecetasWorker.class)
                .setInputData(datos)
                .build();
        WorkManager.getInstance(getApplicationContext()).enqueue(otwr);

        BaseDatos GestorDB = new BaseDatos (this, "RecetasBD", null, 1);
        SQLiteDatabase bd = GestorDB.getWritableDatabase();
        /*String[] campos = new String[] {"Nombre"};
        Cursor cu = bd.query("Receta", campos,"Nombre='NewReceta'", null,null,null,null);
        while (cu.moveToNext()){
            recetaExiste = cu.getString(0);
        }
        cu.close();
        bd.close();

        if (recetaExiste == null) {
            bd = GestorDB.getWritableDatabase();
            ContentValues nuevo = new ContentValues();
            nuevo.put("Nombre", "NewReceta");
            bd.insert("Receta", null, nuevo);
            bd.close();
        }*/

        //Para añadir una foto de la receta
        imagenNuevaReceta = findViewById(R.id.imagenNuevaReceta);

        imagenNuevaReceta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogoCamaraGaleria = new DialogoGaleriaCamara();
                Bundle bundle = new Bundle();
                bundle.putString("usuarioReceta", "receta");
                dialogoCamaraGaleria.setArguments(bundle);
                dialogoCamaraGaleria.show(getSupportFragmentManager(), "galeriaCamara");
            }
        });

        //Para que en el giro de pantalla no se pierda la imagen
        /*bd = GestorDB.getWritableDatabase();
        campos = new String[] {"Imagen"};
        cu = bd.query("Receta", campos, "Nombre='NewReceta'", null,null,null,null);
        CursorWindow cw = new CursorWindow("test", 50000000);
        AbstractWindowedCursor ac = (AbstractWindowedCursor) cu;
        ac.setWindow(cw);
        while (ac.moveToNext()){
            imagen = cu.getBlob(0);
        }
        cu.close();
        bd.close();
        if (imagen != null) {
            imagenNuevaReceta.setImageBitmap(BitmapFactory.decodeByteArray(imagen, 0, imagen.length));
        }*/

        //Para añadir un ingrediente
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
        Intent iPerfil = new Intent(this, UsuarioPerfil.class);
        //Para añadir la receta
        anadirReceta.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
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
                    Toast.makeText(getApplicationContext(), getString(R.string.toastNombreReceta), Toast.LENGTH_SHORT).show();
                } else if (count > 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.toastNameExist), Toast.LENGTH_SHORT).show();
                    cajaNombreReceta.setText("");
                } else if (pasos.equals("")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.toastPasosReceta), Toast.LENGTH_SHORT).show();
                } else if (in == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.toastIngredientesReceta), Toast.LENGTH_SHORT).show();
                } else {
                    ImageView imagenReceta = findViewById(R.id.imagenNuevaReceta);
                    Bitmap icon = ((BitmapDrawable)imagenReceta.getDrawable()).getBitmap();
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    icon.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
                    byte[] data =  outputStream.toByteArray();

                    //Añadir a la base de datos la receta
                    Data datos = new Data.Builder()
                            .putString("funcion", "anadirReceta")
                            .putString("nombreReceta", nombreReceta)
                            .putByteArray("Imagen", data)
                            .putString("PasosSeguir", pasos)
                            .build();

                    OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(RecetasWorker.class)
                            .setInputData(datos)
                            .build();
                    WorkManager.getInstance(getApplicationContext()).enqueue(otwr);

                    /*bd = GestorDB.getWritableDatabase();
                    ContentValues modificacion = new ContentValues();
                    modificacion.put("Nombre", nombreReceta);
                    modificacion.put("Imagen", data);
                    modificacion.put("PasosSeguir", pasos);
                    bd.update("Receta", modificacion, "Nombre='NewReceta'", null);
                    bd.close();*/

                    //Le añadimos la receta al usuario
                    anadirRecetaUsuario(nombreReceta, nombreUsuario);

                    //Añadir una notificación
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel elCanal = new NotificationChannel("IdCanal", "NombreCanal", NotificationManager.IMPORTANCE_DEFAULT);
                        elBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.recetaanadida))
                                .setSmallIcon(R.drawable.suma)
                                .setContentTitle(getText(R.string.notiRecetaAnadida))
                                .setContentText(getString(R.string.notiLaReceta)+" '"+nombreReceta+"' "+getString(R.string.notiHaSidoAñadida))
                                .setVibrate(new long[]{0, 1000, 500, 1000})
                                .setAutoCancel(true);
                        elCanal.enableLights(true);
                        elManager.createNotificationChannel(elCanal);
                    }
                    elManager.notify(1, elBuilder.build());

                    finish();

                    if (main) {
                        iMain.putExtra("nombre", nombreUsuario);
                        startActivity(iMain);
                    } else {
                        iPerfil.putExtra("nombre", nombreUsuario);
                        startActivity(iPerfil);
                    }
                }
            }
        });

        Button volver = findViewById(R.id.buttonVolverAdd);
        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Data datos = new Data.Builder()
                        .putString("funcion", "eliminarReceta")
                        .putString("nombreReceta", "NewReceta")
                        .build();

                OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(RecetasWorker.class)
                        .setInputData(datos)
                        .build();
                WorkManager.getInstance(getApplicationContext()).enqueue(otwr);

                /*SQLiteDatabase bd = GestorDB.getWritableDatabase();
                bd.delete("Receta", "Nombre='NewReceta'", null);
                bd.close();*/
                finish();
                if (main) {
                    iMain.putExtra("nombre", nombreUsuario);
                    iMain.putExtra("inicio",true);
                    startActivity(iMain);
                } else {
                    iPerfil.putExtra("nombre", nombreUsuario);
                    startActivity(iPerfil);
                }
            }
        });
    }

    //Cuando el dialogoGaleriaCamara se cierra viene a este método
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onDismiss(DialogInterface dialog) {
        BaseDatos GestorDB = new BaseDatos (this, "RecetasBD", null, 1);
        SQLiteDatabase bd = GestorDB.getWritableDatabase();
        String[] campos = new String[] {"Imagen"};
        Cursor cu = bd.query("Receta", campos,"Nombre='NewReceta'", null,null,null,null);
        CursorWindow cw = new CursorWindow("test", 50000000);
        AbstractWindowedCursor ac = (AbstractWindowedCursor) cu;
        ac.setWindow(cw);
        while (ac.moveToNext()){
            imagen = cu.getBlob(0);
        }
        cu.close();
        bd.close();
        if (imagen != null) {
            imagenNuevaReceta.setImageBitmap(BitmapFactory.decodeByteArray(imagen, 0, imagen.length));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void anadirRecetaUsuario(String nombreReceta, String nombreUsuario) {
        String recetas = "";
        //Vemos y cogemos las recetas que tiene
        BaseDatos GestorDB = new BaseDatos (this, "RecetasBD", null, 1);
        SQLiteDatabase bd = GestorDB.getWritableDatabase();
        String[] campos = new String[] {"RecetasCreadas"};
        String[] argumentos = new String[] {nombreUsuario};
        Cursor cu = bd.query("Usuario", campos,"Nombre=?", argumentos,null,null,null);
        while (cu.moveToNext()){
            recetas = cu.getString(0);
        }
        cu.close();
        bd.close();

        String recetasUsuario = "";

        if (recetas != null) {
            recetasUsuario = recetas+","+nombreReceta;
        } else {
            recetasUsuario = nombreReceta;
        }

        //Le añadimos la nueva receta
        bd = GestorDB.getWritableDatabase();
        ContentValues modificacion = new ContentValues();
        modificacion.put("RecetasCreadas", recetasUsuario);
        argumentos = new String[] {nombreUsuario};
        bd.update("Usuario", modificacion, "Nombre=?", argumentos);
        bd.close();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            /*BaseDatos GestorDB = new BaseDatos (this, "RecetasBD", null, 1);
            SQLiteDatabase bd = GestorDB.getWritableDatabase();
            bd.delete("Receta", "nombre='NewReceta'", null);
            bd.close(); */
            Data datos = new Data.Builder()
                    .putString("funcion", "eliminarReceta")
                    .putString("nombreReceta", "NewReceta")
                    .build();

            OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(RecetasWorker.class)
                    .setInputData(datos)
                    .build();
            WorkManager.getInstance(getApplicationContext()).enqueue(otwr);

            finish();
            Intent iMain = new Intent(this, MainActivity.class);
            Intent iPerfil = new Intent(this, UsuarioPerfil.class);
            if (main) {
                iMain.putExtra("usuario", nombreUsuario);
                iMain.putExtra("inicio",true);
                startActivity(iMain);
            } else {
                iPerfil.putExtra("nombre", nombreUsuario);
                startActivity(iPerfil);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_NOMBRE, nombreReceta);
        outState.putString(STATE_PASOS, pasosReceta);
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