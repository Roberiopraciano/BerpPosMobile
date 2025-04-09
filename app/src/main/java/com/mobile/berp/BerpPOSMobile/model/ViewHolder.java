package com.mobile.berp.BerpPOSMobile.model;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobile.berp.BerpPosMobile.R;


public class ViewHolder extends RecyclerView.ViewHolder {

    public ImageView imgStatus, imgVendaTipo; // Adicionado imgVendaTipo
    public TextView txtMesaCT, txtGarcon, txtValor, txtHora, txtNomeCliente,txtLocalEntrega;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);

     //   this.imgStatus = itemView.findViewById(R.id.imgStatus);
        this.imgVendaTipo = itemView.findViewById(R.id.imgVendaTipo); // Inicializado aqui
        this.txtMesaCT = itemView.findViewById(R.id.txtMesaCT);
        this.txtGarcon = itemView.findViewById(R.id.txtGarcon);
        this.txtValor = itemView.findViewById(R.id.txtValor);
        this.txtHora = itemView.findViewById(R.id.txtHora);
        this.txtNomeCliente = itemView.findViewById(R.id.textView8);
        this.txtLocalEntrega = itemView.findViewById(R.id.textView9);


    }
}