package com.example.aplicacionrecetas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private boolean iniciarSesion = false;
    private BuscadorListAdapter adaptador;
    private ArrayList<String> listaRecetas = new ArrayList<>();
    private SearchView buscador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            iniciarSesion = extras.getBoolean("inicio");
        }

        //this.deleteDatabase("RecetasBD");


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
                    Toast.makeText(getApplicationContext(),"Que no se te olvide escribir el nombre de la receta!", Toast.LENGTH_LONG).show();
                } else {
                    boolean existe = existeReceta(nomReceta);
                    buscador.setQuery("", false);
                    if (existe) {
                        iInfoReceta.putExtra("nombreReceta", nomReceta);
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

        //Funcionamiento de los botones inferiores

        Button iniciarAdd = findViewById(R.id.botonIniciarAdd);
        Button registrarPerfil = findViewById(R.id.botonRegistrarPerfil);

        if (!iniciarSesion) {
            iniciarAdd.setText("Iniciar sesión");
            registrarPerfil.setText("Registrarse");
        } else {
            iniciarAdd.setText("Añadir receta");
            registrarPerfil.setText("Ir al perfil");
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
                    iPerfil.putExtra("inicio", iniciarSesion);
                    finish();
                    startActivity(iPerfil);
                }
                listaBuscador.setVisibility(View.INVISIBLE);
            }
        });


    }

    private void obtenerRecetas(){
        BaseDatos GestorDB = new BaseDatos (this, "RecetasBD", null, 1);
        SQLiteDatabase bd = GestorDB.getWritableDatabase();
        String[] campos = new String[] {"Nombre"};
        Cursor cu = bd.query("Receta",campos,null,null,null,null,"Nombre ASC");
        while (cu.moveToNext()){
            listaRecetas.add(cu.getString(0));
        }
        cu.close();
        bd.close();
    }

    private boolean existeReceta(String nombre) {
        boolean existe = true;
        BaseDatos GestorDB = new BaseDatos (this, "RecetasBD", null, 1);
        SQLiteDatabase bd = GestorDB.getWritableDatabase();
        String[] campos = new String[] {"Nombre"};
        String[] argumentos = new String[] {nombre};
        Cursor cu = bd.query("Receta", campos,"Nombre=?", argumentos,null,null,null);
        if (cu.getCount() <= 0) {
            existe = false;
        }
        cu.close();
        bd.close();
        return existe;
    }
}