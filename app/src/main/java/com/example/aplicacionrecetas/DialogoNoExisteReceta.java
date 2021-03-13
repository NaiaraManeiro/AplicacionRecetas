package com.example.aplicacionrecetas;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


public class DialogoNoExisteReceta extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View vista = inflater.inflate(R.layout.dialogo_no_existe_receta, null);

        Intent iAnadir = new Intent(getActivity(), AnadirReceta.class);
        Button crearReceta = vista.findViewById(R.id.crearRecetaBoton);
        crearReceta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        Button volver = vista.findViewById(R.id.volverBoton);
        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        builder.setView(vista);

        return builder.create();
    }
}
