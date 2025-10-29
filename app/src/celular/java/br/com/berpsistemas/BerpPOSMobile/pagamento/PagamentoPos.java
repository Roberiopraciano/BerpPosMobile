package br.com.berpsistemas.BerpPOSMobile.pagamento;

import android.app.Activity;
import android.content.Intent;

import br.com.berpsistemas.BerpPOSMobile.model.PagamentoModel;

public class PagamentoPos implements IPagamento {
    private static final int REQUEST_CODE_PAYMENT = 3001;

    public PagamentoPos(Activity context) {
    }

    @Override
    public void iniciarPagamentoDeeplink(Activity activity, PaymentConfig config) {

    }

    @Override
    public void iniciarPagamentoProvider(Activity activity, PaymentConfig config) {
        // Se a Stone não oferecer provider, lance exceção
        throw new UnsupportedOperationException("Stone suporta apenas integração via deeplink.");
    }

    @Override
    public void processarResultado(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void setCallback(IPagamentoCallback callback) {

    }

    @Override
    public void realizarReembolso(Activity activity, PagamentoModel pag) {

    }
}