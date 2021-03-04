package com.example.aplicacionrecetas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
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
                    nuevo.put(nombre, contrasena);
                    bd.insert("Usuario", null, nuevo);
                    bd.close();
                    finish();
                }
            }
        });

        Button volver = findViewById(R.id.volverRegistro);
        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private boolean validacion() {
        boolean valido = true;

        EditText nombreCaja = findViewById(R.id.nombreUsuarioRegistro);
        String nombre = nombreCaja.getText().toString();
        if (nombre.equals("")){
            Toast.makeText(getApplicationContext(),"El nombre no puede estar vacío.", Toast.LENGTH_LONG).show();
            nombreCaja.setText("");
            valido = false;
        } else if (nombre.length() > 21) {
            Toast.makeText(getApplicationContext(),"El nombre no puede tener más de 20 letras.", Toast.LENGTH_LONG).show();
            nombreCaja.setText("");
            valido = false;
        }

        EditText contrasenaCaja = findViewById(R.id.contrasenaRegistro);
        String contrasena = contrasenaCaja.getText().toString();
        EditText confirmarCaja = findViewById(R.id.confirmarContrasena);
        String confContrasena = confirmarCaja.getText().toString();
        if (!confContrasena.equals(contrasena)){
            Toast.makeText(getApplicationContext(),"Las contraseñas no coindicen.", Toast.LENGTH_LONG).show();
            contrasenaCaja.setText("");
            confirmarCaja.setText("");
            valido = false;
        } else {
            if (contrasena.equals(null)){
                Toast.makeText(getApplicationContext(),"La contraseña no puede estar vacía.", Toast.LENGTH_LONG).show();
                contrasenaCaja.setText("");
                valido = false;
            } else if (contrasena.length() > 21) {
                Toast.makeText(getApplicationContext(),"La contraseña no puede tener más de 20 letras.", Toast.LENGTH_LONG).show();
                contrasenaCaja.setText("");
                valido = false;
            }
            if (confContrasena.equals(null)){
                Toast.makeText(getApplicationContext(),"La contraseña de confirmación no puede estar vacía.", Toast.LENGTH_LONG).show();
                confirmarCaja.setText("");
                valido = false;
            } else if (confContrasena.length() > 21) {
                Toast.makeText(getApplicationContext(),"La contraseña de confirmación no puede tener más de 20 letras.", Toast.LENGTH_LONG).show();
                confirmarCaja.setText("");
                valido = false;
            }
        }





        return valido;
    }
}