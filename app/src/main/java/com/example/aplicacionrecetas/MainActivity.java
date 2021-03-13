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
    private String nombreUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            iniciarSesion = extras.getBoolean("inicio");
            nombreUsuario = extras.getString("nombre");
        }

        this.deleteDatabase("RecetasBD");
        anadirImagenes();

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

    private String existeReceta(String nombre) {
        String receta = null;
        BaseDatos GestorDB = new BaseDatos (this, "RecetasBD", null, 1);
        SQLiteDatabase bd = GestorDB.getWritableDatabase();
        String[] campos = new String[] {"Nombre"};
        Cursor cu = bd.query("Receta", campos,null, null,null,null,null);
        while (cu.moveToNext()) {
            String nombreAct = cu.getString(0);
            if (nombre.toLowerCase().equals(nombreAct.toLowerCase())) {
                cu.close();
                bd.close();
                return nombreAct;
            }
        }
        cu.close();
        bd.close();
        return receta;
    }

    private void anadirImagenes() {
        BaseDatos GestorDB = new BaseDatos (this, "RecetasBD", null, 1);
        SQLiteDatabase bd = GestorDB.getWritableDatabase();

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.pasta);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        icon.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        byte[] data =  outputStream.toByteArray();

        ContentValues modificacion = new ContentValues();
        modificacion.put("Imagen", data);
        bd.update("Receta", modificacion, "Nombre='Pasta'", null);


        Bitmap icon2 = BitmapFactory.decodeResource(getResources(), R.drawable.pollo);
        ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
        icon2.compress(Bitmap.CompressFormat.PNG, 0, outputStream2);
        byte[] data2 =  outputStream2.toByteArray();

        ContentValues modificacion2 = new ContentValues();
        modificacion2.put("Imagen", data2);
        bd.update("Receta", modificacion2, "Nombre='Pollo'", null);

        Bitmap icon3 = BitmapFactory.decodeResource(getResources(), R.drawable.hamburguesa);
        ByteArrayOutputStream outputStream3 = new ByteArrayOutputStream();
        icon3.compress(Bitmap.CompressFormat.PNG, 0, outputStream3);
        byte[] data3 =  outputStream3.toByteArray();

        ContentValues modificacion3 = new ContentValues();
        modificacion3.put("Imagen", data3);
        bd.update("Receta", modificacion3, "Nombre='Hamburguesa'", null);

        bd.close();
    }
}