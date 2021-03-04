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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.Objects;

public class DialogoAddIngrediente extends DialogFragment {
    private ArrayList<String> listaIngredientes = new ArrayList<>();
    private CharSequence[] ingredientes;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Añade un nuevo ingrediente:");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View vista = inflater.inflate(R.layout.dialog_add_ingrediente, null);

        builder.setCancelable(false);

        Button anadir = vista.findViewById(R.id.anadirIngrediente);
        anadir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText cajaIngrediente = vista.findViewById(R.id.nombreIngrediente);
                String ingrediente = cajaIngrediente.getText().toString();
                listaIngredientes.add(ingrediente);

                ingredientes = listaIngredientes.toArray(new CharSequence[0]);

                builder.setItems(ingredientes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
            }
        });

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        builder.setView(vista);

        return builder.create();
    }
}
