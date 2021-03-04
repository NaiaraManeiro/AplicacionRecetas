package com.example.aplicacionrecetas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class UsuarioPerfil extends AppCompatActivity {
    boolean inicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_perfil);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            inicio = extras.getBoolean("inicio");
        }

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
}