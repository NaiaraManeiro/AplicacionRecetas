package com.example.aplicacionrecetas;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.Objects;

public class DialogoAddIngrediente extends DialogFragment {
    private static final String STATE_INGREDIENTES = "ingredientes";
    private ArrayList<String> listaIngredientes = new ArrayList<>();

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Añade un nuevo ingrediente:");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View vista = inflater.inflate(R.layout.dialog_add_ingrediente, null);

        if (savedInstanceState != null) {
            listaIngredientes = savedInstanceState.getStringArrayList(STATE_INGREDIENTES);
        }


        Button anadir = vista.findViewById(R.id.anadirIngrediente);
        anadir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText cajaIngrediente = vista.findViewById(R.id.nombreIngrediente);
                String ingrediente = cajaIngrediente.getText().toString();
                listaIngredientes.add(ingrediente);
                Toast.makeText(getActivity().getApplicationContext(),"Ingrediente añadido", Toast.LENGTH_SHORT).show();
                cajaIngrediente.setText("");
            }
        });

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        builder.setNeutralButton("Ver ingredientes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DialogFragment dialogoVerIngredientes = new DialogoVerIngredientes();
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("listaIngredientes", listaIngredientes);
                dialogoVerIngredientes.setArguments(bundle);
                dialogoVerIngredientes.show(getActivity().getSupportFragmentManager(), "verIngredientes");
            }
        });

        builder.setView(vista);

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(STATE_INGREDIENTES, listaIngredientes);
    }
}
