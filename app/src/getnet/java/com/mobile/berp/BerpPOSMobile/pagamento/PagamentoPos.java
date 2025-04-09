package com.mobile.berp.BerpPOSMobile.pagamento;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.util.UUID;

public class PagamentoPos implements IPagamento {
    private static final int REQUEST_CODE_PAYMENT = 1001;
    private final Activity activity;


    public PagamentoPos(Activity activity) {
        this.activity = activity;
        // Instancia o PlugPag passando o contexto da Activity

    }
    @Override
    public void iniciarPagamentoDeeplink(Activity activity, PaymentConfig config) {
        // Formata o valor para 12 dígitos (ex.: "000000010000" para R$100,00)
        String formattedAmount = String.format("%012d", config.getAmount());
        // Gera um callerId único
        String callerId = UUID.randomUUID().toString();

        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme("getnet")
                .authority("pagamento")
                .appendPath("v3")
                .appendPath("payment")
                .appendQueryParameter("paymentType", config.getTransactionType())
                .appendQueryParameter("amount", formattedAmount)
                .appendQueryParameter("callerId", callerId)
                .appendQueryParameter("currencyCode", config.getCurrencyCode())
                .appendQueryParameter("orderId", config.getOrderId());
        // Você pode acrescentar outros parâmetros conforme a documentação (ex.: extraData, disableMagStripe, etc.)

        Uri uri = uriBuilder.build();
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        activity.startActivityForResult(intent, REQUEST_CODE_PAYMENT);
    }

    @Override
    public void iniciarPagamentoProvider(Activity activity, PaymentConfig config) {
        // Getnet utiliza deeplink na integração V3 – este método não é suportado
        throw new UnsupportedOperationException("Getnet suporta apenas integração via deeplink.");
    }

    @Override
    public void processarResultado(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                // Recupera os parâmetros de retorno, conforme a documentação:
                String result = data.getStringExtra("result");
                String resultDetails = data.getStringExtra("resultDetails");
                String nsu = data.getStringExtra("nsu");
                String brand = data.getStringExtra("brand");
                String callerId = data.getStringExtra("callerId");
                // Outros parâmetros podem ser processados aqui

                Log.d("PagamentoGetnet", "Resultado: " + result);
                Log.d("PagamentoGetnet", "Detalhes: " + resultDetails);
                Log.d("PagamentoGetnet", "NSU: " + nsu);
                Log.d("PagamentoGetnet", "Bandeira: " + brand);
                Log.d("PagamentoGetnet", "Caller ID: " + callerId);
            } else {
                Log.d("PagamentoGetnet", "Pagamento cancelado ou falhou.");
            }
        }
    }
}