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

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.AbstractWindowedCursor;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class UsuarioPerfil extends AppCompatActivity implements DialogInterface.OnDismissListener {
    private String nombre;
    private byte[] imagen;
    private ImageView iconoUsuario;
    private String recetasUsuario;
    private String[] recetasNombre;
    private ArrayList<byte[]> recetasFoto;

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
        BaseDatos GestorDB = new BaseDatos (this, "RecetasBD", null, 1);
        SQLiteDatabase bd = GestorDB.getWritableDatabase();
        String[] campos = new String[] {"Icono"};
        String[] argumentos = new String[] {nombre};
        Cursor cu = bd.query("Usuario", campos,"Nombre=?", argumentos,null,null,null);
        CursorWindow cw = new CursorWindow("test", 50000000);
        AbstractWindowedCursor ac = (AbstractWindowedCursor) cu;
        ac.setWindow(cw);
        while (ac.moveToNext()){
            imagen = cu.getBlob(0);
        }
        cu.close();
        bd.close();

        iconoUsuario = findViewById(R.id.iconoUsuario);

        if (imagen != null) {
            iconoUsuario.setImageBitmap(BitmapFactory.decodeByteArray(imagen, 0, imagen.length));
        }
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
        GestorDB = new BaseDatos (this, "RecetasBD", null, 1);
        bd = GestorDB.getWritableDatabase();
        campos = new String[] {"RecetasCreadas"};
        argumentos = new String[] {nombre};
        cu = bd.query("Usuario", campos,"Nombre=?", argumentos,null,null,null);
        while (cu.moveToNext()){
            recetasUsuario = cu.getString(0);
        }
        cu.close();
        bd.close();

        if (recetasUsuario != null) {
            recetasNombre = recetasUsuario.split(",");
            recetasFoto = new ArrayList<>();
            GestorDB = new BaseDatos (this, "RecetasBD", null, 1);
            for (String receta : recetasNombre) {
                bd = GestorDB.getWritableDatabase();
                campos = new String[] {"Imagen"};
                argumentos = new String[] {receta};
                cu = bd.query("Receta", campos,"Nombre=?", argumentos,null,null,null);
                cw = new CursorWindow("test", 50000000);
                ac = (AbstractWindowedCursor) cu;
                ac.setWindow(cw);
                while (ac.moveToNext()){
                    byte[] imagen = cu.getBlob(0);
                    recetasFoto.add(imagen);
                }
            }
            cu.close();
            bd.close();

            RecetasUsuarioRecyclerAdapter eladaptador = new RecetasUsuarioRecyclerAdapter(recetasNombre, recetasFoto);
            rv.setAdapter(eladaptador);

            GridLayoutManager elLayoutRejillaIgual= new GridLayoutManager(this,2, GridLayoutManager.HORIZONTAL,false);
            rv.setLayoutManager(elLayoutRejillaIgual);
        }

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
        BaseDatos GestorDB = new BaseDatos (this, "RecetasBD", null, 1);
        SQLiteDatabase bd = GestorDB.getWritableDatabase();
        String[] campos = new String[] {"Icono"};
        String[] argumentos = new String[] {nombre};
        Cursor cu = bd.query("Usuario", campos,"Nombre=?", argumentos,null,null,null);
        CursorWindow cw = new CursorWindow("test", 50000000);
        AbstractWindowedCursor ac = (AbstractWindowedCursor) cu;
        ac.setWindow(cw);
        while (ac.moveToNext()){
            imagen = cu.getBlob(0);
        }
        cu.close();
        bd.close();
        if (imagen != null) {
            iconoUsuario.setImageBitmap(BitmapFactory.decodeByteArray(imagen, 0, imagen.length));
        }
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