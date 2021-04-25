package com.example.aplicacionrecetas;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;

public class AnadirEventosFragment extends Fragment {

    static final int CODIGO_DE_PERMISO = 3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.anadir_eventos_fragment, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Para mantener el idioma
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String idiomaL = prefs.getString("idioma", "es");
        if (idiomaL.equals("es")) {
            idioma(getString(R.string.locationES));
        } else if (idiomaL.equals("en")) {
            idioma(getString(R.string.locationEN));
        }

        Button addEventos = getActivity().findViewById(R.id.anadirEvento);
        addEventos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CALENDAR}, CODIGO_DE_PERMISO);
                }  else if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_CALENDAR}, CODIGO_DE_PERMISO);
                } else {
                    EditText tituloCaja = getActivity().findViewById(R.id.tituloEvento);
                    String titulo = tituloCaja.getText().toString();
                    EditText descripcionCaja = getActivity().findViewById(R.id.descripcionEvento);
                    String descripcion = descripcionCaja.getText().toString();
                    EditText fechaCaja = getActivity().findViewById(R.id.dateEvent);
                    String fecha = fechaCaja.getText().toString();
                    if (titulo.equals("")) {
                        Toast.makeText(getContext(), getString(R.string.noTitulo), Toast.LENGTH_SHORT).show();
                    } else if (descripcion.equals("")) {
                        Toast.makeText(getContext(), getString(R.string.noDescripcion), Toast.LENGTH_SHORT).show();
                    } else if (fecha.equals("")) {
                        Toast.makeText(getContext(), getString(R.string.noDate), Toast.LENGTH_SHORT).show();
                    } else if (!Pattern.compile("^(0[1-9]|[12][0-9]|3[01])-(0[1-9]|1[012])-((2[0-9])[0-9]{2})$").matcher(fecha).matches()) {
                        Toast.makeText(getContext(), getString(R.string.formatoFecha), Toast.LENGTH_SHORT).show();
                        fechaCaja.setText("");
                    } else {
                        String[] parts = fecha.split("-");
                        String dia = parts[0];
                        String mes = parts[1];
                        String year = parts[2];
                        long startMillis;
                        Calendar beginTime = Calendar.getInstance();
                        beginTime.set(Integer.parseInt(year), Integer.parseInt(mes), Integer.parseInt(dia), 12, 30);
                        startMillis = beginTime.getTimeInMillis();
                        long endMillis;
                        Calendar endTime = Calendar.getInstance();
                        endTime.set(Integer.parseInt(year), Integer.parseInt(mes), Integer.parseInt(dia), 13, 30);
                        endMillis = endTime.getTimeInMillis();

                        ContentValues values = new ContentValues();
                        values.put(CalendarContract.Events.DTSTART, startMillis);
                        values.put(CalendarContract.Events.DTEND, endMillis);
                        values.put(CalendarContract.Events.TITLE, titulo);
                        values.put(CalendarContract.Events.DESCRIPTION, descripcion);
                        values.put(CalendarContract.Events.CALENDAR_ID, 1);
                        values.put(CalendarContract.Events.EVENT_TIMEZONE, "Spain/Madrid");
                        getActivity().getContentResolver().insert(CalendarContract.Events.CONTENT_URI, values);

                        Toast.makeText(getContext(), getString(R.string.eventoAnadido), Toast.LENGTH_SHORT).show();
                        tituloCaja.setText("");
                        descripcionCaja.setText("");
                        fechaCaja.setText("");

                    }
                }
            }
        });
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
