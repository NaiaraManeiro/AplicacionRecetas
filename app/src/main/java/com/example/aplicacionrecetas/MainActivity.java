package com.example.aplicacionrecetas;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private boolean iniciarSesion = false;
    private BuscadorListAdapter adaptador;
    private final ArrayList<String> listaRecetas = new ArrayList<>();
    private SearchView buscador;
    private String nombreUsuario;
    private String existeNombreReceta = null;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
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

        setContentView(R.layout.activity_main);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            iniciarSesion = extras.getBoolean("inicio");
            nombreUsuario = extras.getString("nombre");
        }

        //Creamos el carrousel de imágenes
        ImageView rocketImage = findViewById(R.id.recetasCarrousel);
        rocketImage.setBackgroundResource(R.drawable.recetas_carrousel);

        Drawable rocketAnimation = rocketImage.getBackground();
        if (rocketAnimation instanceof Animatable) {
            ((Animatable)rocketAnimation).start();
        }

        //Implementamos el buscador para las recetas
        obtenerRecetas();
        ListView listaBuscador = findViewById(R.id.listaBuscador);
        listaBuscador.setVisibility(View.INVISIBLE);
        adaptador = new BuscadorListAdapter(this, listaRecetas);
        listaBuscador.setAdapter(adaptador);
        buscador = findViewById(R.id.buscador);
        buscador.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                listaBuscador.setVisibility(View.VISIBLE);
               return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                listaBuscador.setVisibility(View.VISIBLE);
                adaptador.filter(newText);
                return false;
            }
        });

        buscador.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                listaBuscador.setVisibility(View.INVISIBLE);
                return false;
            }
        });

        listaBuscador.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                buscador.setQuery(listaRecetas.get(position), false);
                listaBuscador.setVisibility(View.INVISIBLE);
            }
        });

        //Funcionamiento botón "buscar receta"
        Button buscarReceta = findViewById(R.id.botonBuscarRecetas);
        Intent iInfoReceta = new Intent(this, InfoReceta.class);
        buscarReceta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nomReceta = buscador.getQuery().toString();
                if (nomReceta.equals("")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.buscadorVacio), Toast.LENGTH_SHORT).show();
                } else {
                    String nombreReceta = existeReceta(nomReceta);
                    buscador.setQuery("", false);
                    if (nombreReceta != null) {
                        iInfoReceta.putExtra("nombreReceta", nombreReceta);
                        startActivity(iInfoReceta);
                    } else {
                        DialogFragment dialogoNoReceta = new DialogoNoExisteReceta();
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("inicioSesion",iniciarSesion);
                        dialogoNoReceta.setArguments(bundle);
                        dialogoNoReceta.show(getSupportFragmentManager(), "noExisteReceta");
                    }
                    listaBuscador.setVisibility(View.INVISIBLE);
                }
            }
        });

        //Funcionamiento del botón del idioma
        Button idioma = findViewById(R.id.buttonIdioma);
        idioma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogoIdioma = new DialogoIdiomas();
                dialogoIdioma.show(getSupportFragmentManager(), "idiomas");
            }
        });

        //Funcionamiento de los botones inferiores
        Button iniciarAdd = findViewById(R.id.botonIniciarAdd);
        Button registrarPerfil = findViewById(R.id.botonRegistrarPerfil);

        if (!iniciarSesion) {
            iniciarAdd.setText(getString(R.string.inicioSesion));
            registrarPerfil.setText(getString(R.string.registro));
        } else {
            iniciarAdd.setText(getString(R.string.addReceta));
            registrarPerfil.setText(getString(R.string.perfil));
        }

        Intent iLogin = new Intent(this, IniciarSesion.class);
        Intent iAddReceta = new Intent(this, AnadirReceta.class);

        iniciarAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!iniciarSesion) {
                    finish();
                    startActivity(iLogin);
                } else {
                    finish();
                    iAddReceta.putExtra("main", true);
                    iAddReceta.putExtra("inicio",true);
                    iAddReceta.putExtra("usuario", nombreUsuario);
                    startActivity(iAddReceta);
                }
                listaBuscador.setVisibility(View.INVISIBLE);
            }
        });

        Intent iRegistro = new Intent(this, RegistrarUsuario.class);
        Intent iPerfil = new Intent(this, UsuarioPerfil.class);

        registrarPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!iniciarSesion) {
                    finish();
                    startActivity(iRegistro);
                } else {
                    iPerfil.putExtra("nombre", nombreUsuario);
                    finish();
                    startActivity(iPerfil);
                }
                listaBuscador.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void obtenerRecetas(){
        Data datos = new Data.Builder()
                .putString("funcion", "obtenerRecetas")
                .build();

        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(RecetasWorker.class)
                .setInputData(datos)
                .build();
        WorkManager.getInstance(MainActivity.this).getWorkInfoByIdLiveData(otwr.getId())
                .observe(MainActivity.this, status -> {
                    if (status != null && status.getState().isFinished()) {
                        String result = status.getOutputData().getString("resultado");
                        String[] recetas = result.split(",");
                        listaRecetas.addAll(Arrays.asList(recetas));
                    }
                });
        WorkManager.getInstance(getApplicationContext()).enqueue(otwr);
    }

    private String existeReceta(String nombre) {
        Data datos = new Data.Builder()
                .putString("funcion", "existeReceta")
                .putString("nombreReceta", nombre)
                .build();

        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(RecetasWorker.class)
                .setInputData(datos)
                .build();
        WorkManager.getInstance(MainActivity.this).getWorkInfoByIdLiveData(otwr.getId())
                .observe(MainActivity.this, status -> {
                    if (status != null && status.getState().isFinished()) {
                        String result = status.getOutputData().getString("resultado");
                        if (nombre.toLowerCase().equals(result.toLowerCase())) {
                            existeNombreReceta = result;
                        }
                    }
                });
        WorkManager.getInstance(getApplicationContext()).enqueue(otwr);
        return existeNombreReceta;
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