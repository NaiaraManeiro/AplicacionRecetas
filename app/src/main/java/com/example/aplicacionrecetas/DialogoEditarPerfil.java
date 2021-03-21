package com.example.aplicacionrecetas;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.AbstractWindowedCursor;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

public class DialogoEditarPerfil extends DialogFragment implements DialogInterface.OnDismissListener {

    private String nombreUsuario;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View vista = inflater.inflate(R.layout.dialogo_editar_perfil, null);

        Bundle bundle = getArguments();
        if (bundle != null) {
            nombreUsuario = bundle.getString("nombreUsuario");
        }

        Button guardar = vista.findViewById(R.id.buttonGuardar);
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText cajaNombre = vista.findViewById(R.id.nombreUsuarioTextEdit);
                String nombre = cajaNombre.getText().toString();
                if (nombre.equals("")) {
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.nombreVacio), Toast.LENGTH_SHORT).show();
                } else if (nombre.length() > 21) {
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.nombreLargo), Toast.LENGTH_SHORT).show();
                    cajaNombre.setText("");
                } else {
                    BaseDatos GestorDB = new BaseDatos (getActivity(), "RecetasBD", null, 1);
                    SQLiteDatabase bd = GestorDB.getWritableDatabase();
                    ContentValues modificacion = new ContentValues();
                    modificacion.put("Nombre", nombre);
                    String[] argumentos = new String[]{nombreUsuario};
                    bd.update("Usuario", modificacion, "Nombre=?", argumentos);
                    bd.close();
                    dismiss();
                }
            }
        });

        Button volver = vista.findViewById(R.id.buttonVolverEdit);
        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        builder.setView(vista);

        return builder.create();
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
    }
}
