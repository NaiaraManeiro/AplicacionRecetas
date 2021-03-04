package com.example.aplicacionrecetas;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

public class DialogoVerIngredientes extends DialogFragment {
    private ArrayList<String> listaIngredientes = new ArrayList<>();
    private CharSequence[] ingredientes = new CharSequence[0];

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setCancelable(true);

        Bundle bundle = getArguments();
        if (bundle != null) {
            listaIngredientes = bundle.getStringArrayList("listaIngredientes");
        }

        ingredientes = listaIngredientes.toArray(new CharSequence[listaIngredientes.size()]);

        builder.setItems(ingredientes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        return builder.create();
    }
}
