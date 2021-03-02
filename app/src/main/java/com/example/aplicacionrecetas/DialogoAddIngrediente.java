package com.example.aplicacionrecetas;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DialogoAddIngrediente extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Añade un nuevo ingrediente:");

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_add_ingrediente, null));

        Intent anadirReceta = new Intent(getActivity(), AnadirReceta.class);
        builder.setCancelable(false);
        builder.setPositiveButton("Añadir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                EditText cajaIngrediente = getDialog().findViewById(R.id.nombreUsuario);
                String ingrediente = cajaIngrediente.getText().toString();
                anadirReceta.putExtra("ingrediente", ingrediente);
                getActivity().startActivity(anadirReceta);
            }
        });
        return builder.create();
    }
}
