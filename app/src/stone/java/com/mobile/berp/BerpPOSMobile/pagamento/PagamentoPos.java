package com.mobile.berp.BerpPOSMobile.pagamento;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import java.util.UUID;

public class PagamentoPos implements IPagamento {
    private static final int REQUEST_CODE_PAYMENT = 3001;

    public PagamentoPos(Activity context) {
    }

    @Override
    public void iniciarPagamentoDeeplink(Activity activity, PaymentConfig config) {
        String formattedAmount = String.format("%012d", config.getAmount());
        String callerId = UUID.randomUUID().toString();

        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme("stone")
                .authority("pagamento")
                .appendPath("v1")
                .appendPath("payment")
                .appendQueryParameter("paymentType", config.getTransactionType())
                .appendQueryParameter("amount", formattedAmount)
                .appendQueryParameter("callerId", callerId)
                .appendQueryParameter("orderId", config.getOrderId());
        // Adicione outros parâmetros conforme a documentação da Stone

        Uri uri = uriBuilder.build();
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        activity.startActivityForResult(intent, REQUEST_CODE_PAYMENT);
    }

    @Override
    public void iniciarPagamentoProvider(Activity activity, PaymentConfig config) {
        // Se a Stone não oferecer provider, lance exceção
        throw new UnsupportedOperationException("Stone suporta apenas integração via deeplink.");
    }

    @Override
    public void processarResultado(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                String result = data.getStringExtra("result");
                String nsu = data.getStringExtra("nsu");
                Log.d("PagamentoStone", "Resultado: " + result + ", NSU: " + nsu);
            } else {
                Log.d("PagamentoStone", "Pagamento cancelado ou falhou.");
            }
        }
    }
}