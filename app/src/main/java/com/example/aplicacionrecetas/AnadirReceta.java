package com.example.aplicacionrecetas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;

public class AnadirReceta extends AppCompatActivity {

    private ArrayList<String> ingredientes = new ArrayList<>();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anadir_receta);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String ingrediente = extras.getString("ingrediente");
            ingredientes.add(ingrediente);
        }

        ImageView imagenNuevaReceta = findViewById(R.id.imagenNuevaReceta);

        imagenNuevaReceta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Añadir permisos para galeria y cámara
            }
        });

        Button anadirIngrediente = findViewById(R.id.botonAddIngrediente);

        anadirIngrediente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogoAddIngrediente = new DialogoAddIngrediente();
                dialogoAddIngrediente.show(getSupportFragmentManager(), "addIngrediente");
            }
        });

        Button verIngredientes = findViewById(R.id.botonVerIngredientes);

        verIngredientes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogoVerIngredientes = new DialogoVerIngredientes();
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("ListaIngredientes",ingredientes);
                dialogoVerIngredientes.setArguments(bundle);
                dialogoVerIngredientes.show(getSupportFragmentManager(), "verIngredientes");
            }
        });

        Button anadirReceta = findViewById(R.id.botonAddReceta);

        anadirReceta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText cajaNombreReceta = findViewById(R.id.nombreNuevaReceta);
                String nombreReceta = cajaNombreReceta.getText().toString();
                //Añadir a la base de datos

                //AÑadir una notificacion
                finish();
            }
        });
    }
}