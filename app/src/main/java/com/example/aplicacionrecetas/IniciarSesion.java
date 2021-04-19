package com.example.aplicacionrecetas;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class IniciarSesion extends AppCompatActivity {

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

        setContentView(R.layout.activity_iniciar_sesion);

        Button iniciarBoton = findViewById(R.id.loginBoton);
        Intent iPerfil = new Intent(this, UsuarioPerfil.class);
        iniciarBoton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean valido = validacion();
                if (valido){
                    EditText nombreCaja = findViewById(R.id.nombreUsuario);
                    String nombre = nombreCaja.getText().toString();
                    EditText contrasenaCaja = findViewById(R.id.contrasena);
                    String contrasena = contrasenaCaja.getText().toString();

                    Data datos = new Data.Builder()
                            .putString("nombre",nombre)
                            .putString("contrasena",contrasena)
                            .putString("inicioRegistro", "Inicio")
                            .build();

                    OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(RegistroInicioWorker.class)
                            .setInputData(datos)
                            .build();
                    WorkManager.getInstance(IniciarSesion.this).getWorkInfoByIdLiveData(otwr.getId())
                            .observe(IniciarSesion.this, status -> {
                                if (status != null && status.getState().isFinished()) {
                                    String result = status.getOutputData().getString("resultado");
                                    if (result.equals("Error")) {
                                        Toast.makeText(getApplicationContext(), getString(R.string.nomContrIncorrectas), Toast.LENGTH_SHORT).show();
                                        nombreCaja.setText("");
                                        contrasenaCaja.setText("");
                                    } else if (result.equals("Exito")) {
                                        iPerfil.putExtra("inicio", true);
                                        iPerfil.putExtra("nombre", nombre);
                                        finish();
                                        startActivity(iPerfil);
                                    }
                                }
                            });

                    WorkManager.getInstance(getApplicationContext()).enqueue(otwr);
                }
            }
        });

        TextView linkRegistro = findViewById(R.id.linkRegistro);
        Intent iRegistro = new Intent(this, RegistrarUsuario.class);
        linkRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(iRegistro);
            }
        });

        Button volver = findViewById(R.id.volverInicio);
        Intent iMain = new Intent(this, MainActivity.class);
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

        EditText nombreCaja = findViewById(R.id.nombreUsuario);
        String nombre = nombreCaja.getText().toString();
        if (nombre.equals("")){
            Toast.makeText(getApplicationContext(), getString(R.string.nombreVacio), Toast.LENGTH_SHORT).show();
            nombreCaja.setText("");
            valido = false;
        }
        if (nombre.length() > 21) {
            Toast.makeText(getApplicationContext(), getString(R.string.nombreLargo), Toast.LENGTH_SHORT).show();
            nombreCaja.setText("");
            valido = false;
        }

        EditText contrasenaCaja = findViewById(R.id.contrasena);
        String contrasena = contrasenaCaja.getText().toString();
        if (contrasena.equals("")){
            Toast.makeText(getApplicationContext(), getString(R.string.contraVacia), Toast.LENGTH_SHORT).show();
            contrasenaCaja.setText("");
            valido = false;
        }
        if (contrasena.length() > 21) {
            Toast.makeText(getApplicationContext(), getString(R.string.contraLarga), Toast.LENGTH_SHORT).show();
            contrasenaCaja.setText("");
            valido = false;
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