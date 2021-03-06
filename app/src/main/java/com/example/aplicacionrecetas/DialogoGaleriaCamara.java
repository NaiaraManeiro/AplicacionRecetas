package com.example.aplicacionrecetas;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import java.io.ByteArrayOutputStream;

import static android.app.Activity.RESULT_OK;

public class DialogoGaleriaCamara extends DialogFragment {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int CODIGO_DE_PERMISO = 1;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("¿De dónde quieres obtener la imágen?");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View vista = inflater.inflate(R.layout.dialogo_galeria_camara, null);

        ImageView camara = vista.findViewById(R.id.imagenCamara);
        camara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comprobarPermisoCamara();
            }
        });

        ImageView galeria = vista.findViewById(R.id.imagenGaleria);
        galeria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Button volver = vista.findViewById(R.id.buttonVolver);
        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        builder.setView(vista);

        return builder.create();
    }

    private void comprobarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {
                Toast.makeText(getActivity().getApplicationContext(),"Si quieres sacar fotos tienes que aceptar el permiso.", Toast.LENGTH_LONG).show();
            }
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, CODIGO_DE_PERMISO);
        }
        else {
            dispatchTakePictureIntent();
        }
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap icon = (Bitmap) extras.get("data");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            icon.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
            byte[] dataIcon =  outputStream.toByteArray();

            //Añadir a la base de datos
            BaseDatos GestorDB = new BaseDatos (getActivity(), "RecetasBD", null, 1);
            SQLiteDatabase bd = GestorDB.getWritableDatabase();
            ContentValues modificacion = new ContentValues();
            modificacion.put("Imagen", dataIcon);
            bd.update("Receta", modificacion, "Nombre='NewReceta'", null);
            bd.close();
        }
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
