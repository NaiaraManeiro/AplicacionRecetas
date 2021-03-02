package com.example.aplicacionrecetas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class IniciarSesion extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciar_sesion);

        BaseDatos GestorDB = new BaseDatos (this, "RecetasBD", null, 1);
        SQLiteDatabase bd = GestorDB.getWritableDatabase();

        Button iniciarBoton = findViewById(R.id.loginBoton);
        Intent iPerfil = new Intent(this, UsuarioPerfil.class);

        iniciarBoton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean valido = validacion();
                if (valido){
                    EditText nombreCaja = findViewById(R.id.nombreUsuario);
                    String nombre = nombreCaja.getText().toString();
                    EditText contrasenaCaja = findViewById(R.id.contrasena);
                    String contrasena = contrasenaCaja.getText().toString();

                    String[] campos = new String[] {"Nombre", "Contrasena"};
                    String[] argumentos = new String[] {nombre, contrasena};
                    Cursor cu = bd.query("Usuario",campos,"Nombre>? AND Contrasena>?",argumentos,null,null,null);
                    int cursorCount = cu.getCount();
                    cu.close();
                    bd.close();
                    if (cursorCount < 0) {
                        Toast.makeText(getApplicationContext(),"Nombre o contraseña incorrectas", Toast.LENGTH_LONG).show();
                        nombreCaja.setText("");
                        contrasenaCaja.setText("");
                    } else {
                        finish();
                        startActivity(iPerfil);
                    }
                }
            }
        });
    }

    private boolean validacion() {
        boolean valido = true;

        EditText nombreCaja = findViewById(R.id.nombreUsuario);
        String nombre = nombreCaja.getText().toString();
        if (nombre.equals(null)){
            Toast.makeText(getApplicationContext(),"El nombre no puede estar vacío.", Toast.LENGTH_LONG).show();
            nombreCaja.setText("");
            valido = false;
        }
        if (nombre.length() > 21) {
            Toast.makeText(getApplicationContext(),"El nombre no puede tener más de 20 letras.", Toast.LENGTH_LONG).show();
            nombreCaja.setText("");
            valido = false;
        }

        EditText contrasenaCaja = findViewById(R.id.contrasena);
        String contrasena = contrasenaCaja.getText().toString();
        if (contrasena.equals(null)){
            Toast.makeText(getApplicationContext(),"La contraseña no puede estar vacía.", Toast.LENGTH_LONG).show();
            contrasenaCaja.setText("");
            valido = false;
        }
        if (contrasena.length() > 21) {
            Toast.makeText(getApplicationContext(),"La contraseña no puede tener más de 20 letras.", Toast.LENGTH_LONG).show();
            contrasenaCaja.setText("");
            valido = false;
        }

        return valido;
    }
}