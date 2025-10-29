package br.com.berpsistemas.BerpPOSMobile.pagamento;

import br.com.berpsistemas.BerpPOSMobile.model.PagamentoModel;

public interface IPagamentoCallback {
        void onPagamentoSucesso(PagamentoModel pagamento);
        void onPagamentoFalha(String motivo);
        void onPagamentoCancelado();

        void onRefundSuccess(PagamentoModel pagamentoCancelado);
}
