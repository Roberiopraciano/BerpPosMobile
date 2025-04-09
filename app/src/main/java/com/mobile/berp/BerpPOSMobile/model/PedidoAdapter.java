package com.mobile.berp.BerpPOSMobile.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.mobile.berp.BerpPosMobile.R;

import java.util.List;

public class PedidoAdapter extends BaseAdapter {

    List<Produto> produtos;
    LayoutInflater inflater;

    public PedidoAdapter(Context context, List<Produto> produtos) {
        this.produtos = produtos;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return produtos.size();
    }

    @Override
    public Produto getItem(int position) {
        return produtos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return produtos.get(position).getCod();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        viewElementos elementos;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_pedido, parent, false);
            elementos = new viewElementos();

            elementos.Cod = convertView.findViewById(R.id.codprodutopedido_item);
            elementos.Qtd = convertView.findViewById(R.id.qtdprodutopedido);
            elementos.Desc = convertView.findViewById(R.id.descprodutopedido);
            elementos.Obs = convertView.findViewById(R.id.obsprodutopedido);
            elementos.valorUnitario = convertView.findViewById(R.id.valorunitprodutopedido);
            elementos.valortotal = convertView.findViewById(R.id.valortotalprodutopedido);

            convertView.setTag(elementos);
        } else {
            elementos = (viewElementos) convertView.getTag();
        }

        Produto produto = getItem(position);

        // Substituindo concatenação com +
        elementos.Cod.setText(String.valueOf(produto.getCod()));
        elementos.Qtd.setText(String.format("%sx", produto.getQtd())); // Exemplo: "5x"
        elementos.Desc.setText(produto.getDesc());
        elementos.valorUnitario.setText(produto.getValorUnitarioFormatado());
        elementos.valortotal.setText(produto.getValorTotalFormatado());

        // String.format para observação
        if (!produto.getObs().isEmpty()) {
            elementos.Obs.setText(String.format("OBS: %s", produto.getObs()));
        } else {
            elementos.Obs.setText("");
        }

        return convertView;
    }
    private static class viewElementos {
        TextView Cod, Qtd, Desc, Obs, valorUnitario, valortotal;
    }
}