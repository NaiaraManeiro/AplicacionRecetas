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

public class RegistrarUsuario extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_usuario);

        BaseDatos GestorDB = new BaseDatos (this, "RecetasBD", null, 1);
        SQLiteDatabase bd = GestorDB.getWritableDatabase();

        Intent iMain = new Intent(this, MainActivity.class);
        Button registrarBoton = findViewById(R.id.registroBoton);

        registrarBoton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean valido = validacion();
                if (valido){
                    EditText nombreCaja = findViewById(R.id.nombreUsuarioRegistro);
                    String nombre = nombreCaja.getText().toString();
                    EditText contrasenaCaja = findViewById(R.id.contrasenaRegistro);
                    String contrasena = contrasenaCaja.getText().toString();

                    ContentValues nuevo = new ContentValues();
                    nuevo.put("Nombre", nombre);
                    nuevo.put("Contrasena", contrasena);
                    bd.insert("Usuario", null, nuevo);
                    Toast.makeText(getApplicationContext(),getString(R.string.registroCorrecto), Toast.LENGTH_SHORT).show();
                    bd.close();
                    finish();
                    startActivity(iMain);
                }
            }
        });

        Button volver = findViewById(R.id.volverRegistro);
        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(iMain);
            }
        });
    }

    private boolean validacion() {
        boolean valido = true;

        EditText nombreCaja = findViewById(R.id.nombreUsuarioRegistro);
        String nombre = nombreCaja.getText().toString();
        if (nombre.equals("")){
            Toast.makeText(getApplicationContext(), getString(R.string.nombreVacio), Toast.LENGTH_SHORT).show();            nombreCaja.setText("");
            valido = false;
        } else if (nombre.length() > 21) {
            Toast.makeText(getApplicationContext(), getString(R.string.nombreLargo), Toast.LENGTH_SHORT).show();
            nombreCaja.setText("");
            valido = false;
        }

        EditText contrasenaCaja = findViewById(R.id.contrasenaRegistro);
        String contrasena = contrasenaCaja.getText().toString();
        EditText confirmarCaja = findViewById(R.id.confirmarContrasena);
        String confContrasena = confirmarCaja.getText().toString();
        if (!confContrasena.equals(contrasena)){
            Toast.makeText(getApplicationContext(), getString(R.string.contraNoCoincide), Toast.LENGTH_SHORT).show();
            contrasenaCaja.setText("");
            confirmarCaja.setText("");
            valido = false;
        } else {
            if (contrasena.equals("")){
                Toast.makeText(getApplicationContext(), getString(R.string.contraVacia), Toast.LENGTH_SHORT).show();
                contrasenaCaja.setText("");
                valido = false;
            } else if (contrasena.length() > 21) {
                Toast.makeText(getApplicationContext(), getString(R.string.contraLarga), Toast.LENGTH_SHORT).show();
                contrasenaCaja.setText("");
                valido = false;
            }
            if (confContrasena.equals("")){
                Toast.makeText(getApplicationContext(), getString(R.string.contraConfVacia), Toast.LENGTH_SHORT).show();
                confirmarCaja.setText("");
                valido = false;
            } else if (confContrasena.length() > 21) {
                Toast.makeText(getApplicationContext(), getString(R.string.contraConfLarga), Toast.LENGTH_SHORT).show();
                confirmarCaja.setText("");
                valido = false;
            }
        }

        //Comprobamos si el nombre de usuario existe
        BaseDatos GestorDB = new BaseDatos (this, "RecetasBD", null, 1);
        SQLiteDatabase bd = GestorDB.getWritableDatabase();
        String[] campos = new String[] {"Nombre"};
        String[] argumentos = new String[] {nombre};
        Cursor cu = bd.query("Usuario", campos,"Nombre=?", argumentos,null,null,null);
        int cursorCount = cu.getCount();
        cu.close();
        bd.close();
        if (cursorCount >= 1) {
            Toast.makeText(getApplicationContext(), getString(R.string.nombreEnUso), Toast.LENGTH_SHORT).show();
            nombreCaja.setText("");
            valido = false;
        }

        return valido;
    }
}