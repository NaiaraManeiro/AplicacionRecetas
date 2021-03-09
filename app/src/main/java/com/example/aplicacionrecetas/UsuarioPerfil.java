package com.example.aplicacionrecetas;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.AbstractWindowedCursor;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class UsuarioPerfil extends AppCompatActivity implements DialogInterface.OnDismissListener{
    private boolean inicio;
    private String nombre;
    private byte[] imagen;
    private ImageView iconoUsuario;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_perfil);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            inicio = extras.getBoolean("inicio");
            nombre = extras.getString("nombre");
        }

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
        nomUsuario.setText("Nombre: "+nombre);

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
                iAddReceta.putExtra("usuarioReceta", nombre);
                startActivity(iAddReceta);
            }
        });

        RecyclerView rv = findViewById(R.id.usuarioRecetas);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);

        Button volverMenu = findViewById(R.id.volverMenuBoton);
        Intent iMain = new Intent(this, MainActivity.class);
        volverMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iMain.putExtra("inicio", inicio);
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
}