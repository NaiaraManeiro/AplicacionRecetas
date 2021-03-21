package com.example.aplicacionrecetas;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class DialogoAddIngrediente extends DialogFragment {
    private ArrayList<String> listaIngredientes = new ArrayList<>();
    private String ingredientes;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        //Para mantener el idioma
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String idiomaL = prefs.getString("idioma", "es");
        if (idiomaL.equals("es")) {
            idioma(getString(R.string.locationES));
        } else if (idiomaL.equals("en")) {
            idioma(getString(R.string.locationEN));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View vista = inflater.inflate(R.layout.dialog_add_ingrediente, null);

        BaseDatos GestorDB = new BaseDatos (getActivity(), "RecetasBD", null, 1);
        //Para añadir el ingrediente
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
                        //Añadimos el nuevo ingrediente a los existentes en caso de que haya
                        ArrayList<String> arrayIngredientes = new ArrayList<>(Arrays.asList(ingredienteS.split(",")));
                        listaIngredientes = new ArrayList<>();
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

                Toast.makeText(getActivity().getApplicationContext(),getString(R.string.ingredienteAnadido), Toast.LENGTH_SHORT).show();
                cajaIngrediente.setText("");
            }
        });

        Button volver = vista.findViewById(R.id.buttonBack);
        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Button verIngredientes = vista.findViewById(R.id.buttonVerIngredientes);
        //Ver los ingedientes añadidos
        verIngredientes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogoVerIngredientes = new DialogoVerIngredientes();
                dialogoVerIngredientes.show(getActivity().getSupportFragmentManager(), "verIngredientes");
            }
        });

        builder.setView(vista);

        return builder.create();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void idioma(String idioma) {
        Locale nuevaloc = new Locale(idioma);
        Locale.setDefault(nuevaloc);
        Configuration configuration = getActivity().getBaseContext().getResources().getConfiguration();
        configuration.setLocale(nuevaloc);
        configuration.setLayoutDirection(nuevaloc);
        Context context = getActivity().getBaseContext().createConfigurationContext(configuration);
        getActivity().getBaseContext().getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
    }
}
