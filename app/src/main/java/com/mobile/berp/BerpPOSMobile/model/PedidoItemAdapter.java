package com.mobile.berp.BerpPOSMobile.model;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import com.mobile.berp.BerpPosMobile.R;

import java.util.Vector;

public class PedidoItemAdapter extends ArrayAdapter<Produto> {

    private Context context;
    private Vector<Produto> produtos;
    private LayoutInflater inflater;
    private int resourceId;
    private int tipo_layout;

    public PedidoItemAdapter(Context context, int resourceId, Vector<Produto> produtos, int tipo_layout){
        // TODO Auto-generated constructor stub
        super(context, resourceId, produtos);
        this.resourceId = resourceId;
        this.context = context;
        this.produtos = produtos;
        this.tipo_layout = tipo_layout;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return produtos.size();
    }


    public Produto getItem(int index){
        return produtos.get(index);
    }





    @NonNull
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(resourceId, null);
        }

        TextView codigo 		=  convertView.findViewById(R.id.tvCod);
        TextView decricao 		=  convertView.findViewById(R.id.tvDesc);
        TextView quantidate 	=  convertView.findViewById(R.id.tvQTD);
        TextView valor_unitario =  convertView.findViewById(R.id.tvValUnit);
        TextView atendente 		=  convertView.findViewById(R.id.tvAtend);
        TextView valor_total 	=  convertView.findViewById(R.id.tvVLRTOT);
        TextView hora 			=  convertView.findViewById(R.id.tvHora);

        codigo.setText(String.valueOf(produtos.get(position).getCod()));
        decricao.setText(produtos.get(position).getDesc());
        quantidate.setText(produtos.get(position).getQtd());
        valor_unitario.setText(produtos.get(position).getValorUnitarioFormatado());
        atendente.setText(produtos.get(position).getGarcon());
        valor_total.setText(produtos.get(position).getValorTotalFormatado());
        hora.setText(produtos.get(position).getDhLancamento());

        if(tipo_layout == 1){
            atendente.setVisibility(View.GONE);
            hora.setVisibility(View.GONE);
        }


        return convertView;
    }
}
