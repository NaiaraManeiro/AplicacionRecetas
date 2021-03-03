package com.example.aplicacionrecetas;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.Objects;

public class DialogoVerIngredientes extends DialogFragment {
    private ArrayList<String> ingredientes;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Ingredientes añadidos:");
        builder.setMessage("*Si quiere eliminar un ingrediente mantengalo pulsado.");
        builder.setCancelable(false);

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_ver_ingredientes, null));

        Bundle bundle = getArguments();
        if (bundle != null) {
            ingredientes = bundle.getStringArrayList("listaIngredientes");
        }

        ListView listIngredientes = Objects.requireNonNull(getActivity()).findViewById(R.id.listaIngredientes);
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, ingredientes);
        listIngredientes.setAdapter(adaptador);
        adaptador.notifyDataSetChanged();

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        listIngredientes.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String eliminado = ingredientes.remove(i);
                adaptador.notifyDataSetChanged();
                Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(),"Se ha eliminado el elemento de la lista!", Toast.LENGTH_SHORT).show();

                return true;
            }
        });


        return builder.create();
    }
}