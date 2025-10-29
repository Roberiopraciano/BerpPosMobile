package br.com.berpsistemas.BerpPOSMobile.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;



import java.util.List;

import br.com.berpsistemas.BerpPOSMobile.R;

public class PagamentoAdapter extends ArrayAdapter<String> {
    private final List<PagamentoModel> pagamentos;
    private final PagamentoCancelListener cancelListener;
    private final PagamentoReimpressaoListener reimpressaoListener;
    private final boolean canCancelamento;
    private final boolean canReimpressao;

    // Interface pública para callback de cancelamento
    public interface PagamentoCancelListener {
        void onPagamentoCancelado(int position, PagamentoModel pagamento);
    }

    // Interface pública para callback de reimpressão
    public interface PagamentoReimpressaoListener {
        void onPagamentoReimprimir(int position, PagamentoModel pagamento);
    }

    public PagamentoAdapter(Context context,
                            List<String> descricoes,
                            List<PagamentoModel> pagamentos,
                            PagamentoCancelListener cancelListener,
                            PagamentoReimpressaoListener reimpressaoListener,
                            boolean canCancelamento,
                            boolean canReimpressao) {
        super(context, R.layout.item_pagamento, descricoes);
        this.pagamentos = pagamentos;
        this.cancelListener = cancelListener;
        this.reimpressaoListener = reimpressaoListener;
        this.canCancelamento = canCancelamento;
        this.canReimpressao = canReimpressao;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_pagamento, parent, false);
        }

        TextView txtValorPagamento = convertView.findViewById(R.id.txtValorPagamento);
        ImageButton btnCancelarPagamento = convertView.findViewById(R.id.btnCancelarPagamento);
        ImageButton btnReimprimirPagamento = convertView.findViewById(R.id.btnReimprimirPagamento);

        txtValorPagamento.setText(getItem(position));

        // Ajusta visibilidade e habilitação baseado nas flags
//        if (canCancelamento) {
//            btnCancelarPagamento.setVisibility(View.VISIBLE);
//            btnCancelarPagamento.setEnabled(true);
//            btnCancelarPagamento.setOnClickListener(v -> {
//                if (position < pagamentos.size()) {
//                    cancelListener.onPagamentoCancelado(position, pagamentos.get(position));
//                }
//            });
//        } else {
//            btnCancelarPagamento.setVisibility(View.GONE);
//            btnCancelarPagamento.setOnClickListener(null);
//        }
//
//        if (canReimpressao) {
//            btnReimprimirPagamento.setVisibility(View.VISIBLE);
//            btnReimprimirPagamento.setEnabled(true);
//            btnReimprimirPagamento.setOnClickListener(v -> {
//                if (position < pagamentos.size()) {
//                    reimpressaoListener.onPagamentoReimprimir(position, pagamentos.get(position));
//                }
//            });
//        } else {
//            btnReimprimirPagamento.setVisibility(View.GONE);
//            btnReimprimirPagamento.setOnClickListener(null);
//        }

        return convertView;
    }
}