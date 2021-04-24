package com.example.aplicacionrecetas;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecetasUsuarioViewHolder extends RecyclerView.ViewHolder {

    public TextView eltexto;
    public ImageView laimagen;

    public RecetasUsuarioViewHolder (@NonNull View itemView){
        super(itemView);
        eltexto = itemView.findViewById(R.id.receta_name);
        laimagen = itemView.findViewById(R.id.receta_photo);

        Intent iInfoReceta = new Intent(itemView.getContext(), InfoReceta.class);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iInfoReceta.putExtra("nombreReceta", eltexto.getText().toString());
                itemView.getContext().startActivity(iInfoReceta);
            }
        });
    }
}
