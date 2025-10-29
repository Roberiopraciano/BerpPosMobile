package br.com.berpsistemas.BerpPOSMobile.pagamento;

import android.content.Context;
import android.content.Intent;

import br.com.berpsistemas.BerpPOSMobile.model.PagamentoModel;

/**
 * Interface para o manipulador de callbacks de pagamento.
 * Define os métodos necessários para processar os resultados de pagamentos
 * e fornecer notificação para ouvintes interessados.
 */
public interface IPaymentCallbackHandler {
    /**
     * Processa o resultado de uma operação de pagamento.
     *
     * @param context O contexto do aplicativo
     * @param data A intent contendo os dados do resultado do pagamento
     */
    void handleCallback(Context context, Intent data);

    /**
     * Registra um ouvinte para receber notificações de resultados de pagamento.
     *
     * @param listener O ouvinte a ser registrado
     */
    void setPaymentListener(PaymentListener listener);

    /**
     * Interface para ouvintes interessados em receber notificações de
     * resultados de operações de pagamento.
     */
    interface PaymentListener {
        /**
         * Chamado quando um pagamento é processado com sucesso.
         *
         * @param brand A bandeira do cartão ou método de pagamento
         * @param authCode O código de autorização da transação
         * @param mask O mascaramento de dados sensíveis (como NSU)
         * @param amount O valor do pagamento em reais
         */
        void onPaymentSuccess(String brand, String authCode, String mask,String Doc,String Terminal,String Adquirente,String Id_plataforma,
                              String IdPlataformaResumido,String CodpagMaq,String Rede,String TransactionId, String idPgamento,String binCartao,String debcre, double amount);

        /**
         * Chamado quando ocorre um erro no processamento do pagamento.
         *
         * @param reason A razão/motivo do erro
         */
        void onPaymentError(String reason);

        /**
         * Chamado quando o pagamento é cancelado pelo usuário ou sistema.
         */
        void onPaymentCancelled();

        void onRefundSuccess(PagamentoModel pagamentoCancelado);
    }
}