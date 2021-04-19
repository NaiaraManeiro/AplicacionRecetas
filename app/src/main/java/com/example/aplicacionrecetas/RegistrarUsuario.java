package com.example.aplicacionrecetas;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;

public class RegistrarUsuario extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Para mantener el idioma
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String idiomaL = prefs.getString("idioma", "es");
        if (idiomaL.equals("es")) {
            idioma(getString(R.string.locationES));
        } else if (idiomaL.equals("en")) {
            idioma(getString(R.string.locationEN));
        }

        setContentView(R.layout.activity_registrar_usuario);

        Intent iMain = new Intent(this, MainActivity.class);
        Button registrarBoton = findViewById(R.id.registroBoton);

        //Añadimos el usuario a la base de datos
        registrarBoton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean valido = validacion();
                if (valido){
                    EditText nombreCaja = findViewById(R.id.nombreUsuarioRegistro);
                    String nombre = nombreCaja.getText().toString();
                    EditText contrasenaCaja = findViewById(R.id.contrasenaRegistro);
                    String contrasena = contrasenaCaja.getText().toString();

                    Data datos = new Data.Builder()
                            .putString("nombre",nombre)
                            .putString("contrasena",contrasena)
                            .putString("inicioRegistro", "Registro")
                            .build();

                    OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(RegistroInicioWorker.class)
                            .setInputData(datos)
                            .build();
                    WorkManager.getInstance(RegistrarUsuario.this).getWorkInfoByIdLiveData(otwr.getId())
                            .observe(RegistrarUsuario.this, status -> {
                                if (status != null && status.getState().isFinished()) {
                                    String result = status.getOutputData().getString("resultado");
                                    if (result.equals("Error")) {
                                        Toast.makeText(getApplicationContext(), getString(R.string.nombreEnUso), Toast.LENGTH_SHORT).show();
                                        nombreCaja.setText("");
                                    } else {
                                        Toast.makeText(getApplicationContext(), getString(R.string.registroCorrecto), Toast.LENGTH_SHORT).show();
                                        finish();
                                        startActivity(iMain);
                                    }
                                }
                            });

                    WorkManager.getInstance(getApplicationContext()).enqueue(otwr);
                }
            }
        });

        Button volver = findViewById(R.id.volverRegistro);
        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(iMain);
            }
        });
    }

    private boolean validacion() {
        boolean valido = true;

        EditText nombreCaja = findViewById(R.id.nombreUsuarioRegistro);
        String nombre = nombreCaja.getText().toString();
        if (nombre.equals("")){
            Toast.makeText(getApplicationContext(), getString(R.string.nombreVacio), Toast.LENGTH_SHORT).show();            nombreCaja.setText("");
            valido = false;
        } else if (nombre.length() > 21) {
            Toast.makeText(getApplicationContext(), getString(R.string.nombreLargo), Toast.LENGTH_SHORT).show();
            nombreCaja.setText("");
            valido = false;
        }

        EditText contrasenaCaja = findViewById(R.id.contrasenaRegistro);
        String contrasena = contrasenaCaja.getText().toString();
        EditText confirmarCaja = findViewById(R.id.confirmarContrasena);
        String confContrasena = confirmarCaja.getText().toString();
        if (!confContrasena.equals(contrasena)){
            Toast.makeText(getApplicationContext(), getString(R.string.contraNoCoincide), Toast.LENGTH_SHORT).show();
            contrasenaCaja.setText("");
            confirmarCaja.setText("");
            valido = false;
        } else {
            if (contrasena.equals("")){
                Toast.makeText(getApplicationContext(), getString(R.string.contraVacia), Toast.LENGTH_SHORT).show();
                contrasenaCaja.setText("");
                valido = false;
            } else if (contrasena.length() > 21) {
                Toast.makeText(getApplicationContext(), getString(R.string.contraLarga), Toast.LENGTH_SHORT).show();
                contrasenaCaja.setText("");
                valido = false;
            }
            if (confContrasena.equals("")){
                Toast.makeText(getApplicationContext(), getString(R.string.contraConfVacia), Toast.LENGTH_SHORT).show();
                confirmarCaja.setText("");
                valido = false;
            } else if (confContrasena.length() > 21) {
                Toast.makeText(getApplicationContext(), getString(R.string.contraConfLarga), Toast.LENGTH_SHORT).show();
                confirmarCaja.setText("");
                valido = false;
            }
        }

        return valido;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Intent iMain = new Intent(this, MainActivity.class);
            finish();
            startActivity(iMain);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void idioma(String idioma) {
        Locale nuevaloc = new Locale(idioma);
        Locale.setDefault(nuevaloc);
        Configuration configuration = getBaseContext().getResources().getConfiguration();
        configuration.setLocale(nuevaloc);
        configuration.setLayoutDirection(nuevaloc);
        Context context = getBaseContext().createConfigurationContext(configuration);
        getBaseContext().getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
    }
}