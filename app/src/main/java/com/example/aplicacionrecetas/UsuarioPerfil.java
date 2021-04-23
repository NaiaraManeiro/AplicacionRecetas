package com.example.aplicacionrecetas;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

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
import android.util.Base64;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class UsuarioPerfil extends AppCompatActivity implements DialogInterface.OnDismissListener {
    private String nombre;
    private ImageView iconoUsuario;
    private String[] recetasNombre = new String[0];
    private ArrayList<byte[]> recetasFoto = new ArrayList<>();

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

        setContentView(R.layout.activity_usuario_perfil);
        setSupportActionBar(findViewById(R.id.labarra));

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            nombre = extras.getString("nombre");
        }

        //Cargamos la imagen del usuario

        iconoUsuario = findViewById(R.id.iconoUsuario);

        Data datos = new Data.Builder()
                .putString("funcion", "obtenerImagenUsuario")
                .putString("nombreUsuario", nombre)
                .build();

        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(UsuarioWorker.class)
                .setInputData(datos)
                .build();
        WorkManager.getInstance(UsuarioPerfil.this).getWorkInfoByIdLiveData(otwr.getId())
                .observe(UsuarioPerfil.this, status -> {
                    if (status != null && status.getState().isFinished()) {
                        String result = status.getOutputData().getString("resultado");
                        if (result != null) {
                            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                            StorageReference pathReference = storageRef.child(result);
                            pathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Glide.with(getApplicationContext()).load(uri).into(iconoUsuario);
                                }
                            });
                        }
                    }
                });
        WorkManager.getInstance(getApplicationContext()).enqueue(otwr);

        TextView nomUsuario = findViewById(R.id.nombreUsuarioText);
        nomUsuario.setText(getString(R.string.nombre)+" "+nombre);

        iconoUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogoCamaraGaleria = new DialogoGaleriaCamara();
                Bundle bundle = new Bundle();
                bundle.putString("usuarioReceta", "usuario");
                bundle.putString("usuario", nombre);
                dialogoCamaraGaleria.setArguments(bundle);
                dialogoCamaraGaleria.show(getSupportFragmentManager(), "galeriaCamara");
            }
        });

        Button addreceta = findViewById(R.id.botonNewReceta);
        Intent iAddReceta = new Intent(this, AnadirReceta.class);
        addreceta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                iAddReceta.putExtra("usuario", nombre);
                startActivity(iAddReceta);
            }
        });

        //Mostramos las recetas que el usuario ha creado
        RecyclerView rv = findViewById(R.id.usuarioRecetas);

        //Obtenemos las recetas creadas
        Data datos1 = new Data.Builder()
                .putString("funcion", "obtenerRecetasUsuario")
                .putString("nombreUsuario", nombre)
                .build();

        OneTimeWorkRequest otwr1 = new OneTimeWorkRequest.Builder(UsuarioWorker.class)
                .setInputData(datos1)
                .build();
        WorkManager.getInstance(UsuarioPerfil.this).getWorkInfoByIdLiveData(otwr1.getId())
                .observe(UsuarioPerfil.this, status -> {
                    if (status != null && status.getState().isFinished()) {
                        String result = status.getOutputData().getString("resultado");
                        if (!result.equals("")) {
                            try {
                                JSONObject jsonObject = new JSONObject(result);
                                JSONArray jsonArrayRecetas = jsonObject.getJSONArray("recetas");
                                recetasNombre = jsonArrayRecetas.get(0).toString().split(",");
                                JSONArray jsonArrayImagenes = jsonObject.getJSONArray("imagenes");
                                recetasFoto = new ArrayList<>();
                                for (int i = 0; i < jsonArrayImagenes.length(); i++) {
                                    String url = jsonArrayImagenes.get(i).toString();
                                    //Obtener las imágenes de las recetas
                                    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                                    StorageReference pathReference = storageRef.child(url);
                                    pathReference.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                        @Override
                                        public void onSuccess(byte[] bytes) {
                                            recetasFoto.add(bytes);
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    //Solución para que no pete el código por no cargar las imágenes
                    if (recetasFoto.isEmpty()) {
                        for (int i = 0; i < recetasNombre.length; i++) {
                            recetasFoto.add(new byte[0]);
                        }
                    }
                    RecetasUsuarioRecyclerAdapter eladaptador = new RecetasUsuarioRecyclerAdapter(recetasNombre, recetasFoto);
                    rv.setAdapter(eladaptador);

                    GridLayoutManager elLayoutRejillaIgual= new GridLayoutManager(this,2, GridLayoutManager.HORIZONTAL,false);
                    rv.setLayoutManager(elLayoutRejillaIgual);
                });
        WorkManager.getInstance(getApplicationContext()).enqueue(otwr1);

        Intent iInfoReceta = new Intent(this, InfoReceta.class);

        rv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = rv.getChildLayoutPosition(v);
                String item = recetasNombre[itemPosition];
                iInfoReceta.putExtra("nombreReceta", item);
                startActivity(iInfoReceta);
            }
        });

        //Funcionamiento de botones

        Button volverMenu = findViewById(R.id.volverMenuBoton);
        Intent iMain = new Intent(this, MainActivity.class);
        volverMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iMain.putExtra("inicio", true);
                iMain.putExtra("nombre", nombre);
                finish();
                startActivity(iMain);
            }
        });

        Button cerrarSesion = findViewById(R.id.cerrarSesionBoton);
        cerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iMain.putExtra("inicio", false);
                finish();
                startActivity(iMain);
            }
        });
    }

    //Creación del menú para editar y eliminar un usuario
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.eliminar) {
            DialogFragment dialogoEliminar = new DialogoEliminar();
            Bundle bundle = new Bundle();
            bundle.putString("nombreUsuario", nombre);
            dialogoEliminar.setArguments(bundle);
            dialogoEliminar.show(getSupportFragmentManager(), "eliminar");
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onDismiss(DialogInterface dialog) {
        Data datos = new Data.Builder()
                .putString("funcion", "obtenerImagenUsuario")
                .putString("nombreUsuario", nombre)
                .build();

        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(UsuarioWorker.class)
                .setInputData(datos)
                .build();
        WorkManager.getInstance(UsuarioPerfil.this).getWorkInfoByIdLiveData(otwr.getId())
                .observe(UsuarioPerfil.this, status -> {
                    if (status != null && status.getState().isFinished()) {
                        String result = status.getOutputData().getString("resultado");
                        if (result != null) {
                            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                            StorageReference pathReference = storageRef.child(result);
                            pathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Glide.with(getApplicationContext()).load(uri).into(iconoUsuario);
                                }
                            });
                        }

                    }
                });
        WorkManager.getInstance(getApplicationContext()).enqueue(otwr);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Intent iMain = new Intent(this, MainActivity.class);
            iMain.putExtra("inicio", true);
            iMain.putExtra("nombre", nombre);
            finish();
            startActivity(iMain);
            return true;
        }
        return super.onKeyDown(keyCode, event);
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