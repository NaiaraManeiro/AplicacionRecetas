package com.example.aplicacionrecetas;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private boolean iniciarSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iniciarSesion = true;

        Button iniciarAdd = findViewById(R.id.botonIniciarAdd);
        Button registrarCerrar = findViewById(R.id.botonRegistrarCerrar);

        if (!iniciarSesion) {
            iniciarAdd.setText("Iniciar sesión");
            registrarCerrar.setText("Resgistrarse");
        } else {
            iniciarAdd.setText("Añadir receta");
            registrarCerrar.setText("Cerrar sesión");
        }

    }
}