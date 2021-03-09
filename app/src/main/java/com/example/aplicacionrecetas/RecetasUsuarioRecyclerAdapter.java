package com.example.aplicacionrecetas;

import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecetasUsuarioRecyclerAdapter extends RecyclerView.Adapter<RecetasUsuarioViewHolder> {
    private String[] losnombres;
    private ArrayList<byte[]> lasimagenes;

    public RecetasUsuarioRecyclerAdapter (String[] nombres, ArrayList<byte[]> imagenes) {
        losnombres = nombres;
        lasimagenes = imagenes;
    }

    @NonNull
    @Override
    public RecetasUsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View elLayoutDeCadaItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycleview_item,null);
        RecetasUsuarioViewHolder evh = new RecetasUsuarioViewHolder(elLayoutDeCadaItem);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecetasUsuarioViewHolder holder, int position) {
        holder.eltexto.setText(losnombres[position]);
        holder.laimagen.setImageBitmap(BitmapFactory.decodeByteArray(lasimagenes.get(position), 0, lasimagenes.get(position).length));
    }

    @Override
    public int getItemCount() {
        return losnombres.length;
    }
}
