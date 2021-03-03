package com.example.aplicacionrecetas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private boolean iniciarSesion;
    private BuscadorListAdapter adaptador;
    private ArrayList<String> listaRecetas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        SearchView buscador = findViewById(R.id.buscador);
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

        //Funcionamiento de los botones inferiores
        iniciarSesion = false;

        Button iniciarAdd = findViewById(R.id.botonIniciarAdd);
        Button registrarCerrar = findViewById(R.id.botonRegistrarCerrar);

        if (!iniciarSesion) {
            iniciarAdd.setText("Iniciar sesión");
            registrarCerrar.setText("Registrarse");
        } else {
            iniciarAdd.setText("Añadir receta");
            registrarCerrar.setText("Cerrar sesión");
        }

        Intent iLogin = new Intent(this, IniciarSesion.class);
        Intent iAddReceta = new Intent(this, AnadirReceta.class);

        iniciarAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!iniciarSesion) {
                    startActivity(iLogin);
                } else {
                    startActivity(iAddReceta);
                }
            }
        });

        Intent iRegistro = new Intent(this, RegistrarUsuario.class);

        registrarCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!iniciarSesion) {
                    startActivity(iRegistro);
                } else {
                    iniciarSesion = false;
                    iniciarAdd.setText("Iniciar sesión");
                    registrarCerrar.setText("Registrarse");
                }
            }
        });
    }

    private void obtenerRecetas(){
        BaseDatos GestorDB = new BaseDatos (this, "RecetasBD", null, 1);
        SQLiteDatabase bd = GestorDB.getWritableDatabase();
        String[] campos = new String[] {"Nombre"};
        Cursor cu = bd.query("Receta",campos,null,null,null,null,"Nombre DESC");
        while (cu.moveToNext()){
            listaRecetas.add(cu.getString(0));
        }
        cu.close();
        bd.close();
    }
}