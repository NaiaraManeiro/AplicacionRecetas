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
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;

public class DialogoGaleriaCamara extends DialogFragment {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int CODIGO_DE_PERMISO = 1;
    static final int RESULT_LOAD_IMAGE = 100;
    private Bitmap icon;
    private String usuarioReceta;
    private String usuarioNombre;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View vista = inflater.inflate(R.layout.dialogo_galeria_camara, null);

        Bundle bundle = getArguments();
        if (bundle != null) {
            usuarioReceta = bundle.getString("usuarioReceta"); // Para saber si viene del perfil de usuario o de añadir una receta
            usuarioNombre = bundle.getString("usuario");
        }

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
                obtenerImagenGaleria();
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

    //Para el funcionamiento de la cámara
    private void comprobarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.aceptarPermiso), Toast.LENGTH_SHORT).show();
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

    //Para el funcionamiento de la galería
    private void obtenerImagenGaleria(){
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Cámara
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            icon = (Bitmap) extras.get("data");
        }
        //Galeria
        if (resultCode == RESULT_OK && requestCode == RESULT_LOAD_IMAGE){
            Uri imageUri = data.getData();
            InputStream imageStream = null;
            try {
                imageStream = getActivity().getContentResolver().openInputStream(imageUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            icon = BitmapFactory.decodeStream(imageStream);
        }

        if (icon != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            icon.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
            byte[] dataIcon = outputStream.toByteArray();

            //Añadir a la base de datos
            BaseDatos GestorDB = new BaseDatos (getActivity(), "RecetasBD", null, 1);
            SQLiteDatabase bd = GestorDB.getWritableDatabase();
            ContentValues modificacion = new ContentValues();
            if (usuarioReceta.equals("receta")) {
                modificacion.put("Imagen", dataIcon);
                bd.update("Receta", modificacion, "Nombre='NewReceta'", null);
            } else if (usuarioReceta.equals("usuario")) {
                modificacion.put("Icono", dataIcon);
                String[] argumentos = new String[]{usuarioNombre};
                bd.update("Usuario", modificacion, "Nombre=?", argumentos);
            }
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
