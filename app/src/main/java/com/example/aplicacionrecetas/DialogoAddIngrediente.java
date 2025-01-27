package com.example.aplicacionrecetas;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Locale;

public class DialogoAddIngrediente extends DialogFragment {

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

        //Para añadir el ingrediente
        Button anadir = vista.findViewById(R.id.anadirIngrediente);
        anadir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText cajaIngrediente = vista.findViewById(R.id.nombreIngrediente);
                String ingrediente = cajaIngrediente.getText().toString();

                if (ingrediente.equals("")) {
                    Toast.makeText(getActivity().getApplicationContext(),getString(R.string.noIngrediente), Toast.LENGTH_SHORT).show();
                } else {
                    Data datos = new Data.Builder()
                            .putString("funcion", "anadirIngrediente")
                            .putString("ingrediente", ingrediente)
                            .build();

                    OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(RecetasWorker.class)
                            .setInputData(datos)
                            .build();
                    WorkManager.getInstance(getContext()).enqueue(otwr);

                    Toast.makeText(getActivity().getApplicationContext(),getString(R.string.ingredienteAnadido), Toast.LENGTH_SHORT).show();
                    cajaIngrediente.setText("");
                }
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
