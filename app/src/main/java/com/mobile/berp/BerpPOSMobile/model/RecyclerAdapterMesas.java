package com.mobile.berp.BerpPOSMobile.model;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mobile.berp.BerpPOSMobile.ContaActivity;
import com.mobile.berp.BerpPOSMobile.ItensMesaActivity;
import com.mobile.berp.BerpPosMobile.R;

import java.util.List;

public class RecyclerAdapterMesas extends RecyclerView.Adapter<ViewHolder> {

    private List<Mesa> mesas;
    private Context contextInternal;

    public RecyclerAdapterMesas(List<Mesa> mesas, Context context){
        this.contextInternal = context;
        this.mesas = mesas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_lista_mesa,viewGroup,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        Mesa mesa = mesas.get(position);

        // Dados do layout
        viewHolder.txtGarcon.setText(mesa.getCdFunci());
        viewHolder.txtMesaCT.setText(mesa.getNumMesa());
        viewHolder.txtValor.setText(mesa.getVlrVen());
        viewHolder.txtHora.setText(mesa.getDhAbertura());
        viewHolder.txtNomeCliente.setText(mesa.getNomeCliente());
        viewHolder.txtLocalEntrega.setText(mesa.getLocalEntrega());

        // Ícone da Modalidade de Venda
        switch (mesa.getTipoVenda()) {
            case "0":
                viewHolder.imgVendaTipo.setImageResource(R.drawable.ic_mesa64);
                break;
            case "4":
                viewHolder.imgVendaTipo.setImageResource(R.drawable.cartao64);
                break;
            case "1":
                viewHolder.imgVendaTipo.setImageResource(R.drawable.balcao64);
                break;
            case "2":
                viewHolder.imgVendaTipo.setImageResource(R.drawable.ic_mesa64);
                break;
            default:
                viewHolder.imgVendaTipo.setImageResource(R.drawable.ic_mesa64);
                break;
        }

        // Cor do CardView com base no Status
        int statusColor;
        switch (mesa.getStatus()) {
            case "1": // Aberta
                statusColor = ContextCompat.getColor(contextInternal, R.color.status_aberta);
                break;
            case "3": // Aguardando pagamento
                statusColor = ContextCompat.getColor(contextInternal, R.color.status_aguardando);
                break;
            case "0": // Fechada
                statusColor = ContextCompat.getColor(contextInternal, R.color.status_fechada);
                break;
            default:
                statusColor = ContextCompat.getColor(contextInternal, R.color.status_default);
                break;
        }
        ((CardView) viewHolder.itemView).setCardBackgroundColor(statusColor);

        // Clique no item
        viewHolder.itemView.setOnClickListener(view1 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(contextInternal);

            // Título com base no tipo de venda
            String titulo = "";
            switch (mesa.getTipoVenda()) {
                case "0":
                    titulo = "Mesa escolhida: " + mesa.getNumMesa();
                    break;
                case "1":
                    titulo = "Balcão escolhido: " + mesa.getNumMesa();
                    break;
                case "4":
                    titulo = "Cartão escolhido: " + mesa.getNumMesa();
                    break;
                default:
                    titulo = "Tipo escolhido: " + mesa.getNumMesa();
                    break;
            }

            builder.setTitle(titulo);
            builder.setMessage("Escolha uma opção para continuar.");

            builder.setPositiveButton("Lançar itens", (dialogInterface, i1) -> {
                Intent intent = new Intent(contextInternal, ItensMesaActivity.class);
                intent.putExtra("NUMERO_MESA", mesa.getNumMesa());
                intent.putExtra("ID", mesa.getId());
                intent.putExtra("NOME_CLIENTE", mesa.getNomeCliente());
                intent.putExtra("LOCAL_ENTREGA", mesa.getLocalEntrega());
                intent.putExtra("TP_VEND", mesa.getTipoVenda());
                intent.putExtra("SENDER_VIEW", "LISTA_MESAS");

                BerpModel.setNumMesa(mesa.getNumMesa());
                BerpModel.setId(mesa.getId());
                contextInternal.startActivity(intent);
                ((Activity) contextInternal).finish();
            });

            builder.setNegativeButton("Visualizar Atendimento", (dialog, which) -> {
                Intent intent = new Intent(contextInternal, ContaActivity.class);
                intent.putExtra("NUMERO_MESA", mesa.getNumMesa());
                intent.putExtra("ID", mesa.getId());
                intent.putExtra("NOME_CLIENTE", mesa.getNomeCliente());
                intent.putExtra("LOCAL_ENTREGA", mesa.getLocalEntrega());
                intent.putExtra("TP_VEND", mesa.getTipoVenda());
                intent.putExtra("SENDER_VIEW", "LISTA_MESAS");

                BerpModel.setNumMesa(mesa.getNumMesa());
                BerpModel.setId(mesa.getId());
                contextInternal.startActivity(intent);
                ((Activity) contextInternal).finish();
            });

            AlertDialog dialog = builder.create();
            dialog.show();

            // Desabilitar "Lançar itens" se for balcão
            if (mesa.getTipoVenda().equals("1")) { // 1 representa balcão
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                Toast.makeText(contextInternal, "Lançar itens não está disponível para balcão.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public int getItemCount() {
        return mesas.size();
    }
}
