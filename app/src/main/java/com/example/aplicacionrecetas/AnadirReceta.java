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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class AnadirReceta extends AppCompatActivity implements DialogInterface.OnDismissListener {

    private boolean main = false;
    private String nombreUsuario;
    private ImageView imagenNuevaReceta;
    private static final String STATE_NOMBRE = "nombreReceta";
    private String nombreReceta;
    private static final String STATE_PASOS = "pasosReceta";
    private String pasosReceta = "";
    private String token = "";

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

        //Guardamos el token del usuario en la db
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        // Get new FCM registration token
                        token = task.getResult();

                        Data datos = new Data.Builder()
                                .putString("funcion", "guardarToken")
                                .putString("nombreUsuario", nombreUsuario)
                                .putString("token", token)
                                .build();

                        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(UsuarioWorker.class)
                                .setInputData(datos)
                                .build();
                        WorkManager.getInstance(getApplicationContext()).enqueue(otwr);
                    }
                });

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
        Data datos2 = new Data.Builder()
                .putString("funcion", "obtenerImagenReceta")
                .putString("nombreReceta", "NewReceta")
                .build();

        OneTimeWorkRequest otwr2 = new OneTimeWorkRequest.Builder(RecetasWorker.class)
                .setInputData(datos2)
                .build();
        WorkManager.getInstance(AnadirReceta.this).getWorkInfoByIdLiveData(otwr2.getId())
                .observe(AnadirReceta.this, status2 -> {
                    if (status2 != null && status2.getState().isFinished()) {
                        String result2 = status2.getOutputData().getString("resultado");
                        if (result2 != null) {
                            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                            if (result2.equals("")) {
                                result2 = "/Recetas/addfotoreceta.png";
                            }
                            StorageReference pathReference = storageRef.child(result2);
                            pathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Glide.with(getApplicationContext()).load(uri).into(imagenNuevaReceta);
                                }
                            });
                        }
                    }
                });
        WorkManager.getInstance(getApplicationContext()).enqueue(otwr2);

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

                if (nombreReceta.equals("")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.toastNombreReceta), Toast.LENGTH_SHORT).show();
                } else if (pasos.equals("")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.toastPasosReceta), Toast.LENGTH_SHORT).show();
                } else {
                    //Añadir a la base de datos la receta
                    Data datos = new Data.Builder()
                            .putString("funcion", "anadirReceta")
                            .putString("nombreReceta", nombreReceta)
                            .putString("PasosSeguir", pasos)
                            .putString("Imagen", "/Recetas/"+nombreReceta+".png")
                            .build();

                    OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(RecetasWorker.class)
                            .setInputData(datos)
                            .build();
                    WorkManager.getInstance(AnadirReceta.this).getWorkInfoByIdLiveData(otwr.getId())
                            .observe(AnadirReceta.this, status -> {
                                if (status != null && status.getState().isFinished()) {
                                    String result = status.getOutputData().getString("resultado");
                                    if (result.equals("ErrorNombre")) {
                                        Toast.makeText(getApplicationContext(), getString(R.string.toastNameExist), Toast.LENGTH_SHORT).show();
                                        cajaNombreReceta.setText("");
                                    } else if (result.equals("ErrorIngredientes")) {
                                        Toast.makeText(getApplicationContext(), getString(R.string.toastIngredientesReceta), Toast.LENGTH_SHORT).show();
                                    } else {

                                        //Le añadimos la receta al usuario
                                        anadirRecetaUsuario(nombreReceta, nombreUsuario);

                                        //Actualización del nombre de la imagen de la receta en firebase
                                        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                                        //--Obtenemos la foto con el nombre genérico
                                        StorageReference islandRef = storageRef.child("/Recetas/NewReceta.png");
                                        islandRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                            @Override
                                            public void onSuccess(byte[] bytes) {
                                                //--Subimos la foto con el nuevo nombre
                                                StorageReference spaceRef = storageRef.child("/Recetas/"+nombreReceta+".png");
                                                UploadTask uploadTask = spaceRef.putBytes(bytes);
                                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                StorageReference islandRef = storageRef.child("/Recetas/addfotoreceta.png");
                                                islandRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                    @Override
                                                    public void onSuccess(byte[] bytes) {
                                                        //--Subimos la foto con el nuevo nombre
                                                        StorageReference spaceRef = storageRef.child("/Recetas/"+nombreReceta+".png");
                                                        UploadTask uploadTask = spaceRef.putBytes(bytes);
                                                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                            @Override
                                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                        //--Eliminamos la foto con el nombre genérico
                                        deleteImageFirebase();

                                        //Añadir una notificación

                                        Data datos1 = new Data.Builder()
                                                .putString("token", token)
                                                .putString("title", getString(R.string.notiRecetaAnadida))
                                                .putString("body", getString(R.string.notiLaReceta)+" '"+nombreReceta+"' "+getString(R.string.notiHaSidoAñadida))
                                                .build();

                                        OneTimeWorkRequest otwr1 = new OneTimeWorkRequest.Builder(NotificacionesWorker.class)
                                                .setInputData(datos1)
                                                .build();
                                        WorkManager.getInstance(getApplicationContext()).enqueue(otwr1);

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
                    WorkManager.getInstance(getApplicationContext()).enqueue(otwr);
                }
            }
        });

        Button volver = findViewById(R.id.buttonVolverAdd);
        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eliminarDatosReceta();
            }
        });
    }

    //Cuando el dialogoGaleriaCamara se cierra viene a este método
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onDismiss(DialogInterface dialog) {
        Data datos = new Data.Builder()
                .putString("funcion", "obtenerImagenReceta")
                .putString("nombreReceta", "NewReceta")
                .build();

        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(RecetasWorker.class)
                .setInputData(datos)
                .build();
        WorkManager.getInstance(AnadirReceta.this).getWorkInfoByIdLiveData(otwr.getId())
                .observe(AnadirReceta.this, status -> {
                    if (status != null && status.getState().isFinished()) {
                        String result = status.getOutputData().getString("resultado");
                        if (result != null) {
                            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                            StorageReference pathReference = storageRef.child(result);
                            pathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Glide.with(getApplicationContext()).load(uri).into(imagenNuevaReceta);
                                }
                            });
                        }
                    }
                });
        WorkManager.getInstance(getApplicationContext()).enqueue(otwr);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void anadirRecetaUsuario(String nombreReceta, String nombreUsuario) {
        Data datos = new Data.Builder()
                .putString("funcion", "anadirRecetaUsuario")
                .putString("nombreReceta", nombreReceta)
                .putString("nombreUsuario", nombreUsuario)
                .build();

        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(UsuarioWorker.class)
                .setInputData(datos)
                .build();
        WorkManager.getInstance(getApplicationContext()).enqueue(otwr);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            eliminarDatosReceta();
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

    private void eliminarDatosReceta() {
        Data datos = new Data.Builder()
                .putString("funcion", "eliminarReceta")
                .build();

        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(RecetasWorker.class)
                .setInputData(datos)
                .build();
        WorkManager.getInstance(getApplicationContext()).enqueue(otwr);

        deleteImageFirebase();

        finish();
        Intent iMain = new Intent(this, MainActivity.class);
        Intent iPerfil = new Intent(this, UsuarioPerfil.class);
        if (main) {
            iMain.putExtra("nombre", nombreUsuario);
            iMain.putExtra("inicio",true);
            startActivity(iMain);
        } else {
            iPerfil.putExtra("nombre", nombreUsuario);
            startActivity(iPerfil);
        }
    }

    private void deleteImageFirebase() {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference pathReference = storageRef.child("/Recetas/NewReceta.png");
        pathReference.delete();
    }
}