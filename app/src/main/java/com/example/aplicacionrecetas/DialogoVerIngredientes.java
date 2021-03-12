package com.example.aplicacionrecetas;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.Arrays;

public class DialogoVerIngredientes extends DialogFragment {
    private ArrayList<String> listaIngredientes = new ArrayList<>();
    private CharSequence[] ingredientes = new CharSequence[0];
    private boolean infoReceta = false;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        Bundle bundle = getArguments();
        if (bundle != null) {
            listaIngredientes = bundle.getStringArrayList("listaIngredientes");
            infoReceta = bundle.getBoolean("infoReceta"); //Para saber si viene de la clase info receta
        }

        if(!infoReceta) {
            builder.setTitle(getString(R.string.pulsaBorrar));

            BaseDatos GestorDB = new BaseDatos (getActivity(), "RecetasBD", null, 1);
            SQLiteDatabase bd = GestorDB.getWritableDatabase();
            String[] campos = new String[] {"Ingredientes"};
            Cursor cu = bd.query("Receta", campos,"Nombre='NewReceta'",null,null,null,null);

            while (cu.moveToNext()){
                String ingrediente = cu.getString(0);
                if (ingrediente == null) {
                    ingredientes = listaIngredientes.toArray(new CharSequence[listaIngredientes.size()]);
                } else if (cu.getCount() > 0) {
                    ArrayList<String> arrayIngredientes = new ArrayList<>(Arrays.asList(ingrediente.split(",")));
                    ingredientes = arrayIngredientes.toArray(new CharSequence[arrayIngredientes.size()]);
                }
            }
            bd.close();
        } else {
            ingredientes = listaIngredientes.toArray(new CharSequence[listaIngredientes.size()]);
        }

        builder.setItems(ingredientes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (!infoReceta) {
                    ArrayList<String> nuevaLista = new ArrayList<>();
                    String in = (String) ingredientes[i];
                    for (int j = 0; j < ingredientes.length; j++) {
                        if (!ingredientes[j].equals(in)) {
                            nuevaLista.add((String) ingredientes[j]);
                        }
                    }
                    ingredientes = nuevaLista.toArray(new CharSequence[listaIngredientes.size()]);
                    String commaseparatedlist = nuevaLista.toString();
                    String ingredientesNuevos = commaseparatedlist.replace("[", "")
                            .replace("]", "")
                            .replace(" ", "");

                    BaseDatos GestorDB = new BaseDatos (getActivity(), "RecetasBD", null, 1);
                    SQLiteDatabase bd = GestorDB.getWritableDatabase();
                    ContentValues modificacion = new ContentValues();
                    modificacion.put("Ingredientes", ingredientesNuevos);
                    bd.update("Receta", modificacion, "Nombre='NewReceta'", null);
                    bd.close();
                }
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
