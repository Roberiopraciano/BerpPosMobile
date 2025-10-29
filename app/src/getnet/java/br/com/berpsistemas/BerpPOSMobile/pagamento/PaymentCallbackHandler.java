package br.com.berpsistemas.BerpPOSMobile.pagamento;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import br.com.berpsistemas.BerpPOSMobile.model.PagamentoModel;

public class PaymentCallbackHandler implements IPaymentCallbackHandler {
    private static final String TAG = "GetnetPaymentCallbackHandler";
    private PaymentListener paymentListener;
    private static PaymentCallbackHandler instance;

    // Parâmetros de resposta conforme documentação Getnet
    private static final String ARG_RESULT = "result";
    private static final String ARG_RESULT_DETAILS = "resultDetails";
    private static final String ARG_AMOUNT = "amount";
    private static final String ARG_TYPE = "type";
    private static final String ARG_INPUT_TYPE = "inputType";
    private static final String ARG_INSTALLMENTS = "installments";
    private static final String ARG_NSU = "nsu";
    private static final String ARG_BRAND = "brand";
    private static final String ARG_CALLER_ID = "callerId";
    private static final String ARG_AUTHORIZATION_CODE = "authorizationCode";
    private static final String ARG_CARD_BIN = "cardBin";
    private static final String ARG_CARD_LAST_DIGITS = "cardLastDigits";
    private static final String ARG_CARDHOLDER_NAME = "cardholderName";
    private static final String ARG_GMT_DATE_TIME = "gmtDateTime";
    private static final String ARG_NSU_LOCAL = "nsuLocal";
    private static final String ARG_CV_NUMBER = "cvNumber";
    private static final String ARG_ORDER_ID = "orderId";
    private static final String ARG_RECEIPT_ALREADY_PRINTED = "receiptAlreadyPrinted";
    private static final String ARG_AUTOMATION_SLIP = "automationSlip";
    private static final String ARG_PRINT_MERCHANT_PREFERENCE = "printMerchantPreference";
    private static final String ARG_PIX_PAYLOAD_RESPONSE = "pixPayloadResponse";

    // Códigos de resultado conforme tabela Getnet
    private static final String RESULT_SUCCESS = "0";      // SUCESSO
    private static final String RESULT_DENIED = "1";       // NEGADA
    private static final String RESULT_CANCELLED = "2";    // CANCELADA
    private static final String RESULT_FAILED = "3";       // FALHA
    private static final String RESULT_UNKNOWN = "4";      // DESCONHECIDO
    private static final String RESULT_PENDING = "5";      // PENDENTE (PIX)

    // Singleton
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
    }

    @Override
    public void handleCallback(Context context, Intent intent) {
        try {
            if (intent == null || intent.getExtras() == null) {
                Log.e(TAG, "Intent ou extras são null");
                throw new Exception("Dados de retorno inválidos");
            }

            Bundle extras = intent.getExtras();

            // Log de todos os parâmetros recebidos para debug
            logAllParameters(extras);

            // Extrai parâmetros principais
            String result = extras.getString(ARG_RESULT);
            String resultDetails = extras.getString(ARG_RESULT_DETAILS);

            Log.d(TAG, String.format("Callback Getnet - Result: %s, Details: %s", result, resultDetails));

            // Processa o resultado conforme tabela da Getnet
            processGetnetResult(context, extras, result, resultDetails);

        } catch (Exception e) {
            Log.e(TAG, "Erro no callback Getnet: " + e.getMessage(), e);
            Toast.makeText(context, "Erro no callback: " + e.getMessage(), Toast.LENGTH_LONG).show();
            if (paymentListener != null) {
                paymentListener.onPaymentError("Erro no processamento: " + e.getMessage());
            }
        }
    }

    private void logAllParameters(Bundle extras) {
        Log.d(TAG, "=== Parâmetros recebidos da Getnet ===");
        for (String key : extras.keySet()) {
            Object value = extras.get(key);
            Log.d(TAG, String.format("%s = %s", key, value));
        }
        Log.d(TAG, "=====================================");
    }

    private void processGetnetResult(Context context, Bundle extras, String result, String resultDetails) {
        switch (result != null ? result : "") {
            case RESULT_SUCCESS:
                processSuccessfulPayment(context, extras);
                break;

            case RESULT_CANCELLED:
                processCancelledPayment(context, resultDetails);
                break;

            case RESULT_DENIED:
                processDeniedPayment(context, resultDetails);
                break;

            case RESULT_FAILED:
                processFailedPayment(context, resultDetails);
                break;

            case RESULT_PENDING:
                processPendingPayment(context, extras, resultDetails);
                break;

            case RESULT_UNKNOWN:
            default:
                processUnknownResult(context, result, resultDetails);
                break;
        }
    }

    private void processSuccessfulPayment(Context context, Bundle extras) {
        try {
            // Extrai todos os dados do pagamento aprovado
            String amount = extras.getString(ARG_AMOUNT);
            String nsu = extras.getString(ARG_NSU);
            String brand = extras.getString(ARG_BRAND);
            String callerId = extras.getString(ARG_CALLER_ID);
            String authorizationCode = extras.getString(ARG_AUTHORIZATION_CODE);
            String cardBin = extras.getString(ARG_CARD_BIN);
            String cardLastDigits = extras.getString(ARG_CARD_LAST_DIGITS);
            String cardholderName = extras.getString(ARG_CARDHOLDER_NAME);
            String type = extras.getString(ARG_TYPE);
            String inputType = extras.getString(ARG_INPUT_TYPE);
            String installments = extras.getString(ARG_INSTALLMENTS);
            String gmtDateTime = extras.getString(ARG_GMT_DATE_TIME);
            String nsuLocal = extras.getString(ARG_NSU_LOCAL);
            String cvNumber = extras.getString(ARG_CV_NUMBER);
            String orderId = extras.getString(ARG_ORDER_ID);

            // Converte o valor (formato Getnet: 12 dígitos, últimos 2 são decimais)
            double amountReais = 0;
            if (amount != null && !amount.isEmpty()) {
                try {
                    long amountCents = Long.parseLong(amount);
                    amountReais = amountCents / 100.0;
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Erro ao converter valor: " + amount, e);
                }
            }

            // Mapeia tipo de transação para formato interno
            String debCre = mapGetnetTransactionType(type);

            // Monta máscara do cartão
            String mask = "";
            if (cardBin != null && cardLastDigits != null) {
                mask = cardBin + "******" + cardLastDigits;
            }

            Toast.makeText(context,
                    String.format("Pagamento aprovado! Valor: R$ %.2f\nNSU: %s", amountReais, nsu),
                    Toast.LENGTH_LONG).show();

            Log.i(TAG, String.format(
                    "Pagamento Getnet aprovado: valor=%.2f, nsu=%s, auth=%s, brand=%s, tipo=%s",
                    amountReais, nsu, authorizationCode, brand, debCre
            ));

            if (paymentListener != null) {
                paymentListener.onPaymentSuccess(
                        brand != null ? brand : "",                    // brand
                        authorizationCode != null ? authorizationCode : "", // authCode
                        mask,                                          // mask
                        nsu != null ? nsu : "",                        // cieloCode (usando NSU)
                        "",                                            // terminal
                        "GETNET",                                      // adquirente
                        callerId != null ? callerId : "",              // idPlataforma
                        orderId != null ? orderId : "",                // idPlataformaResumido
                        nsuLocal != null ? nsuLocal : "",              // CodMaqPagamento
                        "GETNET",                                      // flavor
                        nsu != null ? nsu : "",                        // transactionId
                        callerId != null ? callerId : "",              // paymentId
                        cardBin != null ? cardBin : "",                // binCartao
                        debCre,                                        // debCre
                        amountReais                                    // amount
                );
            } else {
                Log.w(TAG, "paymentListener não registrado para sucesso");
            }

        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar pagamento aprovado: " + e.getMessage(), e);
            notifyError(context, "Erro ao processar dados do pagamento aprovado");
        }
    }

    private void processCancelledPayment(Context context, String resultDetails) {
        Log.i(TAG, "Pagamento cancelado pelo usuário: " + resultDetails);
        Toast.makeText(context, "Pagamento cancelado pelo usuário", Toast.LENGTH_LONG).show();

        if (paymentListener != null) {
            paymentListener.onPaymentCancelled();
        }
    }

    private void processDeniedPayment(Context context, String resultDetails) {
        String message = "Pagamento negado";
        if (resultDetails != null && !resultDetails.isEmpty()) {
            message += ": " + resultDetails;
        }

        Log.e(TAG, message);
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

        if (paymentListener != null) {
            paymentListener.onPaymentError(message);
        }
    }

    private void processFailedPayment(Context context, String resultDetails) {
        String message = "Falha no pagamento";
        if (resultDetails != null && !resultDetails.isEmpty()) {
            message += ": " + resultDetails;
        }

        Log.e(TAG, message);
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

        if (paymentListener != null) {
            paymentListener.onPaymentError(message);
        }
    }

    private void processPendingPayment(Context context, Bundle extras, String resultDetails) {
        // PIX pendente - recomenda consultar status posteriormente
        String callerId = extras.getString(ARG_CALLER_ID);
        String message = "Transação PIX pendente";
        if (resultDetails != null && !resultDetails.isEmpty()) {
            message += ": " + resultDetails;
        }

        Log.i(TAG, message + " - CallerId: " + callerId);
        Toast.makeText(context, message + "\nConsulte o status posteriormente", Toast.LENGTH_LONG).show();

        if (paymentListener != null) {
            // Pode implementar um método específico para pendente ou usar onPaymentError
            paymentListener.onPaymentError("PIX_PENDING: " + message);
        }
    }

    private void processUnknownResult(Context context, String result, String resultDetails) {
        String message = "Resultado desconhecido";
        if (result != null) {
            message += " (código: " + result + ")";
        }
        if (resultDetails != null && !resultDetails.isEmpty()) {
            message += ": " + resultDetails;
        }

        Log.e(TAG, message);
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

        if (paymentListener != null) {
            paymentListener.onPaymentError(message);
        }
    }

    private String mapGetnetTransactionType(String getnetType) {
        if (getnetType == null) return "";

        switch (getnetType) {
            case "02": // Débito
                return "DEB";
            case "11": // Crédito à vista
            case "12": // Crédito parcelado Lojista
            case "13": // Crédito parcelado Emissor
                return "CRE";
            case "03": // Voucher
                return "DEB"; // Voucher normalmente é tratado como débito
            case "30": // PIX
                return "PIX";
            default:
                return "CRE"; // Default para crédito
        }
    }

    private void notifyError(Context context, String reason) {
        Log.e(TAG, reason);
        Toast.makeText(context, reason, Toast.LENGTH_LONG).show();
        if (paymentListener != null) {
            paymentListener.onPaymentError(reason);
        } else {
            Log.e(TAG, "Listener não registrado para processar erro");
        }
    }

    /**
     * Processa resultado de reembolso
     */
    public void handleRefundCallback(Context context, Intent intent) {
        try {
            if (intent == null || intent.getExtras() == null) {
                throw new Exception("Dados de reembolso inválidos");
            }

            Bundle extras = intent.getExtras();
            String result = extras.getString(ARG_RESULT);
            String resultDetails = extras.getString(ARG_RESULT_DETAILS);

            Log.d(TAG, String.format("Callback reembolso Getnet - Result: %s, Details: %s", result, resultDetails));

            if (RESULT_SUCCESS.equals(result)) {
                // Reembolso aprovado
                String orderId = extras.getString(ARG_ORDER_ID);
                String callerId = extras.getString(ARG_CALLER_ID);

                PagamentoModel pagCancelado = new PagamentoModel();
                if (orderId != null) {
                    pagCancelado.setIdOrder(orderId);
                }

                Toast.makeText(context, "Reembolso realizado com sucesso!", Toast.LENGTH_LONG).show();

                if (paymentListener != null) {
                    paymentListener.onRefundSuccess(pagCancelado);
                }
            } else {
                // Reembolso com erro
                String errorMsg = "Erro no reembolso";
                if (resultDetails != null && !resultDetails.isEmpty()) {
                    errorMsg = resultDetails;
                }

                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();

                if (paymentListener != null) {
                    paymentListener.onPaymentError(errorMsg);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar reembolso Getnet: " + e.getMessage(), e);
            notifyError(context, "Erro no processamento do reembolso");
        }
    }
}