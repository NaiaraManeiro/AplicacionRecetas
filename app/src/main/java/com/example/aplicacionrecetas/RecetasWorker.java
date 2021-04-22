package com.example.aplicacionrecetas;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RecetasWorker extends Worker {

    private String result = "";

    public RecetasWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String direccion = "http://ec2-54-167-31-169.compute-1.amazonaws.com/nmaneiro001/WEB/recetas.php";
        HttpURLConnection urlConnection = null;
        String funcion = getInputData().getString("funcion");
        try {
            URL destino = new URL(direccion);
            urlConnection = (HttpURLConnection) destino.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);

            JSONObject parametrosJSON = new JSONObject();
            parametrosJSON.put("funcion", funcion);
            if (funcion.equals("anadirReceta")) {
                parametrosJSON.put("nombreReceta", getInputData().getString("nombreReceta"));
                parametrosJSON.put("pasos", getInputData().getString("PasosSeguir"));
            } else if (funcion.equals("anadirIngrediente")) {
                parametrosJSON.put("ingrediente", getInputData().getString("ingrediente"));
            } else if (funcion.equals("actualizarIngredientes")) {
                parametrosJSON.put("nuevosIngredientes", getInputData().getString("nuevosIngredientes"));
            } else if (funcion.equals("datosReceta") || funcion.equals("existeReceta")) {
                parametrosJSON.put("nombreReceta", getInputData().getString("nombreReceta"));
            }

            urlConnection.setRequestProperty("Content-Type","application/json");

            PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
            out.print(parametrosJSON.toString());
            out.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        int statusCode;
        try {
            statusCode = urlConnection.getResponseCode();
            if (statusCode == 200) {
                BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    result += (line);
                }
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Data resultados = new Data.Builder()
                .putString("resultado", result)
                .build();
        return Result.success(resultados);
    }
}
