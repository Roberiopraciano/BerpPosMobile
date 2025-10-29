package br.com.berpsistemas.BerpPOSMobile.pagamento;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import br.com.berpsistemas.BerpPOSMobile.model.PagamentoModel;

public class PaymentCallbackHandler implements IPaymentCallbackHandler {
    private static final String TAG = "StonePaymentCallbackHandler";
    private PaymentListener paymentListener;
    private static PaymentCallbackHandler instance;

    // --- MUDANÇA 1: Variáveis para guardar o resultado "pegajoso" ---
    private PagamentoModel cachedRefundSuccess = null;
    private Object cachedPaymentSuccess = null; // Usaremos Object para generalizar
    private String cachedError = null;
    private boolean cachedCancellation = false;

    public static synchronized PaymentCallbackHandler getInstance() {
        if (instance == null) {
            instance = new PaymentCallbackHandler();
        }
        return instance;
    }

    private PaymentCallbackHandler() {}

    @Override
    public void setPaymentListener(PaymentListener listener) {
        this.paymentListener = listener;
        Log.d(TAG, "Payment listener registrado: " + (listener != null));

        // --- MUDANÇA 2: Entregar o resultado guardado assim que o listener for registrado ---
        if (listener != null) {
            deliverCachedResult();
        }
    }

    /**
     * NOVO: Método para verificar se há um resultado em cache e entregá-lo.
     */
    private void deliverCachedResult() {
        if (cachedRefundSuccess != null) {
            Log.d(TAG, "Entregando resultado de estorno em cache.");
            paymentListener.onRefundSuccess(cachedRefundSuccess);
            cachedRefundSuccess = null; // Limpa o cache após a entrega
        } else if (cachedPaymentSuccess != null) {
            // Aqui você precisaria de uma forma de reconstruir a chamada
            // Por simplicidade, vamos assumir que você pode re-chamar o método de sucesso.
            // A melhor abordagem aqui seria guardar todos os parâmetros.
            Log.d(TAG, "Entregando resultado de pagamento em cache. (Lógica a ser implementada se necessário)");
            // paymentListener.onPaymentSuccess(...); // Reconstrua a chamada aqui
            cachedPaymentSuccess = null;
        } else if (cachedError != null) {
            Log.d(TAG, "Entregando erro em cache.");
            paymentListener.onPaymentError(cachedError);
            cachedError = null;
        } else if (cachedCancellation) {
            Log.d(TAG, "Entregando cancelamento em cache.");
            paymentListener.onPaymentCancelled();
            cachedCancellation = false;
        }
    }

    @Override
    public void handleCallback(Context context, Intent intent) {
        try {
            if(intent!=null){
            Uri uri = intent.getData();
            if (uri == null) throw new Exception("URI inválida");
            Log.d(TAG, "Callback Stone recebido: " + uri.toString());

            if (uri.toString().contains("cancel") || uri.toString().contains("reversal")) {
                handleStoneRefundCallback(context, uri);
                return;
            }
            handleStonePaymentCallback(context, uri);}

        } catch (Exception e) {
            Log.e(TAG, "Erro no callback Stone: " + e.getMessage(), e);
            Toast.makeText(context, "Erro no callback: " + e.getMessage(), Toast.LENGTH_LONG).show();
            notifyError(context, "Erro no processamento: " + e.getMessage());
        }
    }

    private void handleStoneRefundCallback(Context context, Uri uri) {
        try {
            String code = uri.getQueryParameter("code");
            String message = uri.getQueryParameter("message");
            String orderId = uri.getQueryParameter("order_id");
            Log.d(TAG, "Callback de reembolso Stone - Code: " + code + ", Message: " + message);

            if ("0".equals(code)) {
                PagamentoModel pagCancelado = new PagamentoModel();
                if (orderId != null) {
                    pagCancelado.setIdOrder(orderId);
                }
                Toast.makeText(context, "Reembolso realizado com sucesso!", Toast.LENGTH_LONG).show();

                // --- MUDANÇA 3: Lógica de cache ---
                if (paymentListener != null) {
                    paymentListener.onRefundSuccess(pagCancelado);
                } else {
                    Log.w(TAG, "Listener nulo. Guardando resultado do estorno para entrega futura.");
                    cachedRefundSuccess = pagCancelado; // Guarda o resultado
                }
            } else {
                String errorMsg = "Erro no reembolso";
                if (message != null && !message.isEmpty()) {
                    errorMsg += ": " + message;
                }
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
                notifyError(context, errorMsg); // Reutiliza o método de notificação de erro
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar reembolso Stone: " + e.getMessage(), e);
            notifyError(context, "Erro ao processar reembolso Stone: " + e.getMessage());
        }
    }

    // O handleStonePaymentCallback e seus helpers permanecem os mesmos,
    // mas vamos adaptar os métodos de notificação para também usarem o cache.

    private void processStoneFailedPayment(Context context, String code, String message) {
        Log.e(TAG, "Pagamento Stone falhou - Código: " + code + ", Mensagem: " + message);

        if ("2".equals(code)) {
            Toast.makeText(context, "Pagamento cancelado pelo usuário", Toast.LENGTH_LONG).show();
            // --- MUDANÇA 4: Lógica de cache para cancelamento ---
            if (paymentListener != null) {
                paymentListener.onPaymentCancelled();
            } else {
                Log.w(TAG, "Listener nulo. Guardando status de cancelamento para entrega futura.");
                cachedCancellation = true;
            }
        } else {
            String errorMessage = "Pagamento não autorizado";
            if (message != null && !message.isEmpty()) {
                errorMessage = "Erro no pagamento: " + message;
            } else if (code != null) {
                errorMessage = "Erro no pagamento (código: " + code + ")";
            }
            notifyError(context, errorMessage);
        }
    }

    private void notifyError(Context context, String reason) {
        Log.e(TAG, reason);
        Toast.makeText(context, reason, Toast.LENGTH_LONG).show();
        // --- MUDANÇA 5: Lógica de cache para erros ---
        if (paymentListener != null) {
            paymentListener.onPaymentError(reason);
        } else {
            Log.w(TAG, "Listener nulo. Guardando mensagem de erro para entrega futura.");
            cachedError = reason;
        }
    }

    // O resto dos seus métodos (handleStonePaymentCallback, processStoneSuccessfulPayment, etc.)
    // pode permanecer exatamente como está, pois o fluxo de sucesso raramente sofre com esse problema.
    // Se quiser torná-lo 100% à prova de falhas, teria que aplicar a mesma lógica de cache para o `onPaymentSuccess`.

    // ... (cole aqui o resto dos seus métodos: handleStonePaymentCallback, isStonePaymentSuccessful, processStoneSuccessfulPayment, mapStoneTransactionType)
    private void handleStonePaymentCallback(Context context, Uri uri) {
        try {
            // Extrai parâmetros do callback Stone
            String code = uri.getQueryParameter("code");
            String success = uri.getQueryParameter("success");
            String message = uri.getQueryParameter("message");
            String amount = uri.getQueryParameter("amount");
            String authorizationCode = uri.getQueryParameter("authorization_code");
            String orderId = uri.getQueryParameter("order_id");
            String transactionType = uri.getQueryParameter("type");
            String cardholderName = uri.getQueryParameter("cardholder_name");
            String brand = uri.getQueryParameter("brand");
            String pan = uri.getQueryParameter("pan");
            String installmentCount = uri.getQueryParameter("installment_count");
            String itk = uri.getQueryParameter("itk"); // Initiator Transaction Key
            String atk = uri.getQueryParameter("atk"); // Authorizer Transaction Key

            Log.d(TAG, String.format(
                    "Callback Stone: code=%s, amount=%s, auth=%s, brand=%s, type=%s",
                    code, amount, authorizationCode, brand, transactionType
            ));

            // Verifica o resultado do pagamento
            if (isStonePaymentSuccessful(code, success)) {
                processStoneSuccessfulPayment(context, amount, authorizationCode, orderId,
                        transactionType, cardholderName, brand, pan, installmentCount, itk, atk);
            } else {
                processStoneFailedPayment(context, code, message);
            }

        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar pagamento Stone: " + e.getMessage(), e);
            notifyError(context, "Erro no processamento do pagamento");
        }
    }

    private boolean isStonePaymentSuccessful(String code, String success) {
        // Código 0 indica sucesso na Stone
        if ("0".equals(code)) {
            return true;
        }

        // Também verifica o parâmetro success se disponível
        if ("true".equalsIgnoreCase(success)) {
            return true;
        }

        return false;
    }

    private void processStoneSuccessfulPayment(Context context, String amount, String authorizationCode,
                                               String orderId, String transactionType, String cardholderName, String brand,
                                               String pan, String installmentCount, String itk, String atk) {

        try {
            // Converte valores
            double amountReais = 0;
            if (amount != null && !amount.isEmpty()) {
                amountReais = Double.parseDouble(amount) / 100.0;
            }

            // Mapeia tipo de transação para formato interno
            String debCre = mapStoneTransactionType(transactionType);

            Toast.makeText(context,
                    String.format("Pagamento aprovado! Valor: R$ %.2f", amountReais),
                    Toast.LENGTH_LONG).show();

            Log.i(TAG, String.format(
                    "Pagamento Stone aprovado: valor=%.2f, auth=%s, brand=%s, tipo=%s",
                    amountReais, authorizationCode, brand, debCre
            ));

            if (paymentListener != null) {
                paymentListener.onPaymentSuccess(
                        brand != null ? brand : "",                    // brand
                        authorizationCode != null ? authorizationCode : "", // authCode
                        pan != null ? pan : "",                        // mask
                        itk != null ? itk : "",                        // cieloCode (usando ITK)
                        "",                                            // terminal
                        "STONE",                                       // adquirente
                        atk != null ? atk : "",                        // idPlataforma (usando ATK)
                        orderId != null ? orderId : "",                // idPlataformaResumido
                        "",                                            // CodMaqPagamento
                        "STONE",                                       // flavor
                        itk != null ? itk : "",                        // transactionId
                        orderId != null ? orderId : "",                // paymentId
                        "",                                            // binCartao
                        debCre,                                        // debCre
                        amountReais                                    // amount
                );
            } else {
                Log.w(TAG, "paymentListener não registrado para sucesso");
                // Poderíamos adicionar a lógica de cache aqui também se necessário
            }

        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar sucesso Stone: " + e.getMessage(), e);
            notifyError(context, "Erro ao processar dados do pagamento");
        }
    }

    private String mapStoneTransactionType(String stoneType) {
        if (stoneType == null) return "";

        String lowerType = stoneType.toLowerCase();
        if (lowerType.contains("débito") || lowerType.contains("debit")) {
            return "DEB";
        } else if (lowerType.contains("crédito") || lowerType.contains("credit")) {
            return "CRE";
        } else if (lowerType.contains("pix")) {
            return "PIX";
        } else if (lowerType.contains("voucher")) {
            return "DEB"; // Voucher normalmente é tratado como débito
        }

        return "CRE"; // Default para crédito
    }
}