package com.example.aplicacionrecetas;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


public class DialogoNoExisteReceta extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("No existe la receta que estás buscando...");
        builder.setMessage("Prueba a crear una nueva receta!");

        Intent iAnadir = new Intent(getActivity(), AnadirReceta.class);
        builder.setPositiveButton("Crear receta", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bundle bundle = getArguments();
                boolean inicioSesion = bundle.getBoolean("inicioSesion");
                if (inicioSesion) {
                    dismiss();
                    startActivity(iAnadir);
                } else {
                    dismiss();
                    DialogFragment dialogInicio = new DialogoIniciarSesion();
                    dialogInicio.show(getActivity().getSupportFragmentManager(), "dialogInicio");
                }
            }
        });

        builder.setNegativeButton("Volver", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        return builder.create();
    }
}
