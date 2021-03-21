package com.example.aplicacionrecetas;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DialogoEliminar extends DialogFragment {

    private String nombreUsuario;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View vista = inflater.inflate(R.layout.dialogo_eliminar, null);

        Bundle bundle = getArguments();
        if (bundle != null) {
            nombreUsuario = bundle.getString("nombreUsuario");
        }

        TextView textEliminar = vista.findViewById(R.id.textEliminar);
        textEliminar.setText(getString(R.string.eliminarUsuario));

        ImageView si = vista.findViewById(R.id.imageSi);
        Intent iMain = new Intent(getActivity(), MainActivity.class);
        BaseDatos GestorDB = new BaseDatos (getActivity(), "RecetasBD", null, 1);
        si.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase bd = GestorDB.getWritableDatabase();
                String[] argumentos = new String[]{nombreUsuario};
                bd.delete("Usuario", "Nombre=?", argumentos);
                bd.close();
                dismiss();
                startActivity(iMain);
            }
        });

        ImageView no = vista.findViewById(R.id.imageNo);
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        builder.setView(vista);

        return builder.create();
    }
}
