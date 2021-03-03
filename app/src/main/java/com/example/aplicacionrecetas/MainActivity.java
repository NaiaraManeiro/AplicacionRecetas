package com.example.aplicacionrecetas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private boolean iniciarSesion;
    private ListView list;
    //private ListViewAdapter adapter;
    private SearchView editsearch;
    private ArrayList<String> listaRecetas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Creamos el carrousel de imágenes
        ImageView rocketImage = findViewById(R.id.recetasCarrousel);
        rocketImage.setBackgroundResource(R.drawable.recetas_carrousel);

        Drawable rocketAnimation = rocketImage.getBackground();
        if (rocketAnimation instanceof Animatable) {
            ((Animatable)rocketAnimation).start();
        }

        //Implementamos el buscador para las recetas
        obtenerRecetas();

        //Funcionamiento de los botones
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
        this.deleteDatabase("RecetasBD");
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