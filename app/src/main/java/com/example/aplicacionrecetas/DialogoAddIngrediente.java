package com.example.aplicacionrecetas;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import java.util.Arrays;

public class DialogoAddIngrediente extends DialogFragment {
    private ArrayList<String> listaIngredientes = new ArrayList<>();
    private String ingredientes;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Añade un nuevo ingrediente:");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View vista = inflater.inflate(R.layout.dialog_add_ingrediente, null);

        BaseDatos GestorDB = new BaseDatos (getActivity(), "RecetasBD", null, 1);

        Button anadir = vista.findViewById(R.id.anadirIngrediente);
        anadir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText cajaIngrediente = vista.findViewById(R.id.nombreIngrediente);
                String ingrediente = cajaIngrediente.getText().toString();

                SQLiteDatabase bd = GestorDB.getWritableDatabase();
                String[] campos = new String[] {"Ingredientes"};
                Cursor cu = bd.query("Receta", campos,"Nombre='NewReceta'",null,null,null,null);
                while (cu.moveToNext()){
                    String ingredienteS = cu.getString(0);
                    if (cu.getCount() > 0 && ingredienteS != null) {
                        ArrayList<String> arrayIngredientes = new ArrayList<>(Arrays.asList(ingredienteS.split(",")));
                        listaIngredientes.addAll(arrayIngredientes);
                    }
                    listaIngredientes.add(ingrediente);
                }
                bd.close();

                String commaseparatedlist = listaIngredientes.toString();
                ingredientes = commaseparatedlist.replace("[", "")
                        .replace("]", "")
                        .replace(" ", "");


                bd = GestorDB.getWritableDatabase();
                ContentValues modificacion = new ContentValues();
                modificacion.put("Ingredientes", ingredientes);
                bd.update("Receta", modificacion, "Nombre='NewReceta'", null);
                bd.close();

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
}
