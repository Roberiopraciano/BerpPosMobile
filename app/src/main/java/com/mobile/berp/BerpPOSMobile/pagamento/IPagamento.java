package com.mobile.berp.BerpPOSMobile.pagamento;

import android.app.Activity;
import android.content.Intent;

public interface IPagamento {
    // Inicia pagamento via deeplink (caso o flavor use essa abordagem)
    void iniciarPagamentoDeeplink(Activity activity, PaymentConfig config);

    // Inicia pagamento via provider (caso o flavor use um SDK ou API)
    void iniciarPagamentoProvider(Activity activity, PaymentConfig config);

    // Processa o resultado do pagamento (callback)
    void processarResultado(int requestCode, int resultCode, Intent data);
}