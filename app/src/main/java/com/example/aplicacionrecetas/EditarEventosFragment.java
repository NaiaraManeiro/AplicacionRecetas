package com.example.aplicacionrecetas;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.regex.Pattern;

public class EditarEventosFragment extends Fragment {

    private ArrayList<String> titulos = new ArrayList<>();
    private ArrayList<String> descripciones = new ArrayList<>();
    private ArrayList<String> fechasInicio = new ArrayList<>();
    private ArrayList<String> ids = new ArrayList<>();
    static final int CODIGO_DE_PERMISO = 4;
    private ListView lalista;
    private long selectedId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.editar_eventos_fragment, container, false);
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

        lalista = getActivity().findViewById(R.id.editarListaEventos);
        //Obtenemos los eventos del calendario
        rellenarLista();

        EditText tituloCaja = getActivity().findViewById(R.id.editarTituloEvento);
        EditText descripcionCaja = getActivity().findViewById(R.id.editDescripcionEvento);
        EditText fechaCaja = getActivity().findViewById(R.id.editDateEvent);

        //Mostramos la información del evento seleccionado
        lalista.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                tituloCaja.setText(titulos.get(position));
                descripcionCaja.setText(descripciones.get(position));
                fechaCaja.setText(fechasInicio.get(position));
                selectedId = Long.parseLong(ids.get(position));
            }
        });

        //Editamos la información del evento seleccionado
        Button aditarEvento = getActivity().findViewById(R.id.editarEvento);
        aditarEvento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titulo = tituloCaja.getText().toString();
                String descripcion = descripcionCaja.getText().toString();
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
                    Uri updatedUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, selectedId);
                    getActivity().getContentResolver().update(updatedUri, values, null, null);

                    rellenarLista();

                    Toast.makeText(getContext(), getString(R.string.eventoEditado), Toast.LENGTH_SHORT).show();
                    tituloCaja.setText("");
                    descripcionCaja.setText("");
                    fechaCaja.setText("");
                }
            }
        });
    }

    private void rellenarLista() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CALENDAR}, CODIGO_DE_PERMISO);
        } else {

            Cursor cursor = getContext().getContentResolver().query(CalendarContract.Events.CONTENT_URI, null, null, null, null);

            while (cursor.moveToNext()) {
                if (cursor != null) {
                    int id1 = cursor.getColumnIndex(CalendarContract.Events.DTSTART);
                    int id2 = cursor.getColumnIndex(CalendarContract.Events.TITLE);
                    int id3 = cursor.getColumnIndex(CalendarContract.Events.DESCRIPTION);
                    int id4 = cursor.getColumnIndex(CalendarContract.Events._ID);

                    String dateValue = cursor.getString(id1);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(Long.parseLong(dateValue));
                    int mYear = calendar.get(Calendar.YEAR);
                    int mMonth = calendar.get(Calendar.MONTH);
                    int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                    String date = "";
                    if (mDay < 10 && mMonth < 10) {
                        date = "0"+mDay+"-0"+mMonth+"-"+mYear;
                    } else if (mDay < 10) {
                        date = "0"+mDay+"-"+mMonth+"-"+mYear;
                    } else if (mMonth < 10) {
                        date = mDay+"-0"+mMonth+"-"+mYear;
                    }
                    fechasInicio.add(date);

                    String titleValue = cursor.getString(id2);
                    titulos.add(titleValue);

                    String descriptionValue = cursor.getString(id3);
                    descripciones.add(descriptionValue);

                    String idValue = cursor.getString(id4);
                    ids.add(idValue);
                }
            }

            if (titulos.size() == 0) {
                Toast.makeText(getContext(), getString(R.string.noEventos), Toast.LENGTH_SHORT).show();
            } else {

                Collections.reverse(titulos);
                Collections.reverse(descripciones);
                Collections.reverse(fechasInicio);
                Collections.reverse(ids);

                ArrayAdapter eladaptador = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_2, android.R.id.text1, titulos){
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View vista= super.getView(position, convertView, parent);
                        TextView lineaprincipal = vista.findViewById(android.R.id.text1);
                        TextView lineasecundaria = vista.findViewById(android.R.id.text2);
                        lineaprincipal.setText(getText(R.string.titulo) +" : "+titulos.get(position));
                        lineasecundaria.setText(getText(R.string.descripcion) +" : "+descripciones.get(position));
                        return vista;
                    }
                };
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
