package br.com.berpsistemas.BerpPOSMobile.pagamento;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import br.com.berpsistemas.BerpPOSMobile.model.PagamentoModel;
import com.shashank.sony.fancytoastlib.FancyToast;

import br.com.uol.pagseguro.plugpagservice.wrapper.IPlugPagWrapper;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPaymentData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagVoidData;
import br.com.uol.pagseguro.plugpagservice.wrapper.listeners.PlugPagPaymentListener;

public class PagamentoPos implements IPagamento {

    private static final String TAG = "PagamentoPosPagSeguro";

    private final IPlugPagWrapper plugPag;
    private IPagamentoCallback paymentCallback; // Callback para notificar a PagamentoActivity

    public PagamentoPos(Activity context) {
        this.plugPag = new PlugPag(context);
    }

    @Override
    public void setCallback(IPagamentoCallback callback) {
        this.paymentCallback = callback;
    }

    @Override
    public void iniciarPagamentoProvider(Activity activity, PaymentConfig config) {
        if (this.paymentCallback == null) {
            Log.e(TAG, "CRÍTICO: IPagamentoCallback não foi definido. A Activity não será notificada do resultado.");
            return;
        }

        int paymentType;
        switch (config.getTransactionType().toLowerCase()) {
            case "credit":
                paymentType = PlugPag.TYPE_CREDITO;
                break;
            case "debit":
                paymentType = PlugPag.TYPE_DEBITO;
                break;
            case "pix":
                paymentType = PlugPag.TYPE_PIX;
                break;
            default:
                paymentCallback.onPagamentoFalha("Tipo de pagamento inválido para PagSeguro");
                return;
        }

        final PlugPagPaymentData paymentData = new PlugPagPaymentData(
                paymentType,
                config.getAmountInCents(),
                PlugPag.INSTALLMENT_TYPE_A_VISTA, // TODO: Adicionar lógica de parcelas se necessário
                1,
                config.getOrderId()
        );

        // Listener para o resultado do pagamento
        PlugPagPaymentListener listener = new PlugPagPaymentListener() {
            @Override
            public void onSuccess(PlugPagTransactionResult result) {
                Log.d(TAG, "Pagamento onSuccess: " + result.toString());
                PagamentoModel pag = new PagamentoModel();
                pag.setTransactionId(result.getTransactionId());
                pag.setNsu(result.getNsu());
                pag.setBandeira(result.getCardBrand());
                pag.setAutorizacao(result.getAvailableBalance()); // Exemplo, mapear campos corretos
                pag.setPgpVlrpag(result.getAmount() / 100.0);
                pag.setTipoCartaoDebCre(config.getTransactionType());
                
                paymentCallback.onPagamentoSucesso(pag);
            }

            @Override
            public void onError(PlugPagTransactionResult result) {
                Log.e(TAG, "Pagamento onError: " + result.getMessage());
                paymentCallback.onPagamentoFalha(result.getMessage());
            }

            @Override
            public void onPaymentProgress(PlugPagEventData eventData) {
                Log.d(TAG, "Pagamento onPaymentProgress: " + eventData.getCustomMessage());
                // Pode ser usado para atualizar a UI com mensagens como \"Aproxime o cartão\"
            }
        };

        plugPag.doAsyncPayment(paymentData, listener);
    }

    @Override
    public void realizarReembolso(Activity activity, PagamentoModel pag) {
        if (this.paymentCallback == null) {
            Log.e(TAG, "CRÍTICO: IPagamentoCallback não foi definido para o estorno.");
            return;
        }

        PlugPagVoidData voidData = new PlugPagVoidData(
            pag.getTransactionId(),
            pag.getNsu()
        );

        // Listener para o resultado do estorno
        PlugPagPaymentListener listener = new PlugPagPaymentListener() {
            @Override
            public void onSuccess(PlugPagTransactionResult result) {
                Log.d(TAG, "Estorno onSuccess: " + result.toString());
                paymentCallback.onRefundSuccess(pag); // Notifica a activity que o estorno deu certo
            }

            @Override
            public void onError(PlugPagTransactionResult result) {
                Log.e(TAG, "Estorno onError: " + result.getMessage());
                paymentCallback.onPagamentoFalha("Falha no estorno: " + result.getMessage());
            }

            @Override
            public void onPaymentProgress(PlugPagEventData eventData) {
                Log.d(TAG, "Estorno onPaymentProgress: " + eventData.getCustomMessage());
            }
        };

        plugPag.doAsyncVoidPayment(voidData, listener);
    }

    @Override
    public void realizarReimpressao(Activity activity, PagamentoModel pag) {
        try {
            Uri providerUri = Uri.parse("content://com.mobile.berpsistemas.BerpPOSMobile.provider.plugpag");
            activity.getContentResolver().call(providerUri, "reprint", null, null);
            FancyToast.makeText(activity, "Comando de reimpressão enviado.", FancyToast.LENGTH_SHORT, FancyToast.INFO, true).show();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao chamar reimpressão via provider", e);
            FancyToast.makeText(activity, "Falha ao solicitar reimpressão.", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
        }
    }

    @Override
    public void iniciarPagamentoDeeplink(Activity activity, PaymentConfig config) {
        throw new UnsupportedOperationException("PagSeguro não suporta integração via deeplink. Use provider.");
    }

    @Override
    public void processarResultado(int requestCode, int resultCode, Intent data) {
        // Não utilizado no fluxo do PagSeguro, pois o resultado é tratado por listeners.
    }
}
