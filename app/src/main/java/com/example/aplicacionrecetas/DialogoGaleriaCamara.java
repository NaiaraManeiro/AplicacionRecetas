package com.example.aplicacionrecetas;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
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
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class DialogoGaleriaCamara extends DialogFragment {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int CODIGO_DE_PERMISO = 1;
    static final int RESULT_LOAD_IMAGE = 100;
    private Bitmap icon;
    private String usuarioReceta;
    private String usuarioNombre;

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
            //Añadimos la url de la imagen a la base de datos
            String url = "";
            Data datos = null;

            if (usuarioReceta.equals("receta")) {
                url = "/Recetas/NewReceta.png";
                datos = new Data.Builder()
                        .putString("funcion", "anadirImagenReceta")
                        .putString("url", url)
                        .build();
            } else if (usuarioReceta.equals("usuario")) {
                url = "/Usuarios/"+usuarioNombre.replaceAll("\\s+", "")+".png";
                datos = new Data.Builder()
                        .putString("funcion", "anadirImagenUsuario")
                        .putString("url", url)
                        .putString("nombreUsuario", usuarioNombre.replaceAll("\\s+", ""))
                        .build();
            }

            OneTimeWorkRequest otwr = null;

            if (usuarioReceta.equals("receta")) {
                otwr = new OneTimeWorkRequest.Builder(RecetasWorker.class)
                        .setInputData(datos)
                        .build();
            } else if (usuarioReceta.equals("usuario")) {
                otwr = new OneTimeWorkRequest.Builder(UsuarioWorker.class)
                        .setInputData(datos)
                        .build();
            }

            WorkManager.getInstance(getContext()).enqueue(otwr);

            //Añadimos la imagen a Firebase
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference spaceRef = storageRef.child(url);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            icon.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imagen = baos.toByteArray();

            UploadTask uploadTask = spaceRef.putBytes(imagen);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                }
            });
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
