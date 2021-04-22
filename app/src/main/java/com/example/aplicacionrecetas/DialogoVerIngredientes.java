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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class DialogoVerIngredientes extends DialogFragment {
    private ArrayList<String> listaIngredientes = new ArrayList<>();
    private CharSequence[] ingredientes = new CharSequence[0];
    private boolean infoReceta = false;

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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK);

        Bundle bundle = getArguments();
        if (bundle != null) {
            listaIngredientes = bundle.getStringArrayList("listaIngredientes");
            infoReceta = bundle.getBoolean("infoReceta"); //Para saber si viene de la clase info receta
        }

        if(!infoReceta) {
            builder.setTitle(getString(R.string.pulsaBorrar));

            Data datos = new Data.Builder()
                    .putString("funcion", "verIngredientes")
                    .build();

            OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(RecetasWorker.class)
                    .setInputData(datos)
                    .build();
            WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                    .observe(getActivity(), status -> {
                        if (status != null && status.getState().isFinished()) {
                            String result = status.getOutputData().getString("resultado");
                            if (!result.equals("")) {
                                ArrayList<String> arrayIngredientes = new ArrayList<>(Arrays.asList(result.split(",")));
                                ingredientes = arrayIngredientes.toArray(new CharSequence[arrayIngredientes.size()]);
                            }
                        }

                    });
            WorkManager.getInstance(getContext()).enqueue(otwr);

            /*BaseDatos GestorDB = new BaseDatos (getActivity(), "RecetasBD", null, 1);
            SQLiteDatabase bd = GestorDB.getWritableDatabase();
            String[] campos = new String[] {"Ingredientes"};
            Cursor cu = bd.query("Receta", campos,"Nombre='NewReceta'",null,null,null,null);

            while (cu.moveToNext()) {
                String ingrediente = cu.getString(0);
                if (ingrediente != null) {
                    ArrayList<String> arrayIngredientes = new ArrayList<>(Arrays.asList(ingrediente.split(",")));
                    ingredientes = arrayIngredientes.toArray(new CharSequence[arrayIngredientes.size()]);
                }
            }
            bd.close();*/
        } else {
            ingredientes = listaIngredientes.toArray(new CharSequence[listaIngredientes.size()]);
        }

        //Mostramos los ingredientes que tiene la receta
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
                    ingredientes = nuevaLista.toArray(new CharSequence[nuevaLista.size()]);
                    String commaseparatedlist = nuevaLista.toString();
                    String ingredientesNuevos = commaseparatedlist.replace("[", "")
                            .replace("]", "")
                            .replace(" ", "");

                    Data datos = new Data.Builder()
                            .putString("funcion", "actualizarIngredientes")
                            .putString("nuevosIngredientes", ingredientesNuevos)
                            .build();

                    OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(RecetasWorker.class)
                            .setInputData(datos)
                            .build();
                    WorkManager.getInstance(getContext()).enqueue(otwr);

                    /*BaseDatos GestorDB = new BaseDatos (getActivity(), "RecetasBD", null, 1);
                    SQLiteDatabase bd = GestorDB.getWritableDatabase();
                    ContentValues modificacion = new ContentValues();
                    modificacion.put("Ingredientes", ingredientesNuevos);
                    bd.update("Receta", modificacion, "Nombre='NewReceta'", null);
                    bd.close();*/
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
