package com.example.aplicacionrecetas;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class MostrarEventosFragment extends Fragment {

    private final ArrayList<String> titulos = new ArrayList<>();
    private final ArrayList<String> descripciones = new ArrayList<>();
    static final int CODIGO_DE_PERMISO = 2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.mostrar_eventos_fragment, container, false);
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

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CALENDAR}, CODIGO_DE_PERMISO);
        } else {

            //Obtenemos los eventos del calendario
            Cursor cursor = getContext().getContentResolver().query(CalendarContract.Events.CONTENT_URI, null, null, null, null);

            while (cursor.moveToNext()) {
                if (cursor != null) {
                    int id2 = cursor.getColumnIndex(CalendarContract.Events.TITLE);
                    int id3 = cursor.getColumnIndex(CalendarContract.Events.DESCRIPTION);

                    String titleValue = cursor.getString(id2);
                    titulos.add(titleValue);
                    String descriptionValue = cursor.getString(id3);
                    descripciones.add(descriptionValue);
                }
            }

            if (titulos.size() == 0) {
                Toast.makeText(getContext(), getString(R.string.noEventos), Toast.LENGTH_SHORT).show();
            } else {

                Collections.reverse(titulos);
                Collections.reverse(descripciones);

                ArrayAdapter eladaptador = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_2, android.R.id.text1, titulos) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View vista = super.getView(position, convertView, parent);
                        TextView lineaprincipal = vista.findViewById(android.R.id.text1);
                        TextView lineasecundaria = vista.findViewById(android.R.id.text2);
                        lineaprincipal.setText(getText(R.string.titulo) + " : " + titulos.get(position));
                        lineasecundaria.setText(getText(R.string.descripcion) + " : " + descripciones.get(position));
                        return vista;
                    }
                };
                ListView lalista = getActivity().findViewById(R.id.listaEventos);
                lalista.setAdapter(eladaptador);
            }

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
