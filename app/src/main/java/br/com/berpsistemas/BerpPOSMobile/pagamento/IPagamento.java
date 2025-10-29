package br.com.berpsistemas.BerpPOSMobile.pagamento;

import android.app.Activity;
import android.content.Intent;

import br.com.berpsistemas.BerpPOSMobile.model.PagamentoModel;

public interface IPagamento {
    void iniciarPagamentoDeeplink(Activity activity, PaymentConfig config);
    void iniciarPagamentoProvider(Activity activity, PaymentConfig config);
    void processarResultado(int requestCode, int resultCode, Intent data);
    void setCallback(IPagamentoCallback callback);
    void realizarReembolso(Activity activity, PagamentoModel pag);
    void realizarReimpressao(Activity activity, PagamentoModel pag);
}