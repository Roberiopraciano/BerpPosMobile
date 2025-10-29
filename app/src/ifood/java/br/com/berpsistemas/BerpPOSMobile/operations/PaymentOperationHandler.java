package br.com.berpsistemas.BerpPOSMobile.operations;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import br.com.berpsistemas.BerpPOSMobile.model.TransactionModel;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * IMPLEMENTAÇÃO IFOOD PAGO - PaymentOperationHandler
 *
 * Usa deeplinks oficiais do iFood Pago conforme documentação:
 * - Reimpressão: Via deeplink print-local (impressão local)
 * - Cancelamento: https://portal.ifood.com.br/make-refund
 */
public class PaymentOperationHandler implements GenericPaymentOperations.PaymentOperationHandler {

    private static final String TAG = "IFoodPaymentHandler";
    private static final String IFOOD_BASE_URL = "https://portal.ifood.com.br";
    private static final String RETURN_SCHEME = "berpsistemas"; // Seu app scheme

    @Override
    public boolean canReprint(TransactionModel transaction) {
        return transaction.isApproved() &&
                !transaction.isCancelled() &&
                transaction.getTransactionId() != null &&
                !transaction.getTransactionId().isEmpty();
    }

    @Override
    public boolean canCancel(TransactionModel transaction) {
        return transaction.isApproved() &&
                !transaction.isCancelled() &&
                hasRequiredDataForCancel(transaction);
    }

    @Override
    public void reprintReceipt(Context context, TransactionModel transaction, String receiptType,
                               GenericPaymentOperations.OperationCallback callback) {
        try {
            Log.d(TAG, "Iniciando reimpressão iFood Pago via deeplink oficial");

            // iFood Pago não tem deeplink direto de reimpressão de transação específica
            // A documentação mostra apenas print-local (para arquivos) e print-file (via API)
            // Precisamos usar print-local passando os dados da transação

            JSONObject printData = new JSONObject();
            printData.put("integrationApp", "BerpPOS");
            printData.put("urlToReturn", RETURN_SCHEME + "://print-result?result=");
            printData.put("sendResultInSameIntent", false);

            // Para reimpressão, você precisaria gerar um arquivo com os dados da transação
            // ou usar a API de impressão. Por ora, vamos retornar erro informativo

            Log.w(TAG, "iFood Pago não suporta reimpressão direta via deeplink");
            callback.onError("Reimpressão não disponível no iFood Pago. Use a impressão via API ou gere um novo comprovante.");

            /* ALTERNATIVA: Se você tiver acesso à API de impressão do iFood

            // 1. Primeiro obter token de autorização
            JSONObject authRequest = new JSONObject();
            authRequest.put("integrationApp", "BerpPOS");
            authRequest.put("urlToReturn", RETURN_SCHEME + "://print-auth?result=");
            authRequest.put("sendResultInSameIntent", false);

            String authBase64 = Base64.encodeToString(
                authRequest.toString().getBytes("UTF-8"),
                Base64.NO_WRAP | Base64.URL_SAFE | Base64.NO_PADDING
            );

            Uri printAuthUri = Uri.parse(IFOOD_BASE_URL + "/print-file?content=" + authBase64);

            Intent intent = new Intent(Intent.ACTION_VIEW, printAuthUri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                callback.onSuccess("Solicitando autorização para impressão");
            } else {
                callback.onError("App iFood Pago não encontrado");
            }
            */

        } catch (Exception e) {
            Log.e(TAG, "Erro na reimpressão iFood Pago", e);
            callback.onError("Erro ao processar reimpressão: " + e.getMessage());
        }
    }

    @Override
    public void cancelTransaction(Context context, TransactionModel transaction, String reason,
                                  GenericPaymentOperations.OperationCallback callback) {
        try {
            Log.d(TAG, "Iniciando estorno iFood Pago via deeplink oficial");

            // Criar JSON para estorno conforme documentação iFood
            JSONObject refundData = new JSONObject();

            // transactionIdAdyen é obrigatório - é o ID retornado pelo iFood na transação original
            String transactionIdAdyen = getTransactionIdAdyen(transaction);
            if (transactionIdAdyen == null || transactionIdAdyen.isEmpty()) {
                callback.onError("ID da transação Adyen não encontrado. Impossível realizar estorno.");
                return;
            }

            refundData.put("transactionIdAdyen", transactionIdAdyen);
            refundData.put("printReceipt", true);
            refundData.put("urlToReturn", RETURN_SCHEME + "://refund-result?result=");
            refundData.put("sendResultInSameIntent", false);

            // Codificar em Base64 URL-safe
            String contentBase64 = Base64.encodeToString(
                    refundData.toString().getBytes("UTF-8"),
                    Base64.NO_WRAP | Base64.URL_SAFE | Base64.NO_PADDING
            );

            // Criar URI do deeplink
            Uri refundUri = Uri.parse(IFOOD_BASE_URL + "/make-refund?content=" + contentBase64);

            Log.d(TAG, "URI de estorno iFood: " + refundUri.toString());
            Log.d(TAG, "Dados do estorno: " + refundData.toString());

            Intent intent = new Intent(Intent.ACTION_VIEW, refundUri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                callback.onSuccess("Estorno iniciado via iFood Pago");
            } else {
                callback.onError("App iFood Pago não encontrado. Instale o app da Maquinona.");
            }

        } catch (JSONException e) {
            Log.e(TAG, "Erro ao criar JSON de estorno", e);
            callback.onError("Erro ao criar dados do estorno: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Erro no estorno iFood Pago", e);
            callback.onError("Erro ao processar estorno: " + e.getMessage());
        }
    }

    /**
     * Extrai o transactionIdAdyen da transação
     * Este ID é retornado pelo iFood Pago após o pagamento e é OBRIGATÓRIO para estorno
     */
    private String getTransactionIdAdyen(TransactionModel transaction) {
        // 1. Verificar se está no externalId (campo principal)
        String adyenId = transaction.getExternalId();
        if (adyenId != null && !adyenId.isEmpty() && !adyenId.equals("\"\"")) {
            return adyenId.replace("\"", "");
        }

        // 2. Verificar no paymentId (pode ter sido armazenado aqui)
        if (transaction.getPaymentId() != null &&
                transaction.getPaymentId().contains("-")) { // UUID format
            return transaction.getPaymentId();
        }

        // 3. Verificar metadados
        if (transaction.getFlavorSpecificData() != null && !transaction.getFlavorSpecificData().isEmpty()) {
            try {
                String metadata = transaction.getFlavorSpecificData();

                // Tentar parsear como JSON
                if (metadata.contains("transactionIdAdyen")) {
                    JSONObject json = new JSONObject(metadata);
                    if (json.has("transactionIdAdyen")) {
                        return json.getString("transactionIdAdyen");
                    }
                }

                // Parsing simples se não for JSON válido
                if (metadata.contains("transactionIdAdyen")) {
                    String[] parts = metadata.split("transactionIdAdyen");
                    if (parts.length > 1) {
                        // Extrair UUID (formato: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx)
                        String possibleId = parts[1].replaceAll("[^a-fA-F0-9-]", "");
                        if (possibleId.length() >= 36) {
                            return possibleId.substring(0, 36);
                        }
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Erro ao extrair transactionIdAdyen dos metadados: " + e.getMessage());
            }
        }

        Log.w(TAG, "transactionIdAdyen não encontrado na transação");
        return null;
    }

    /**
     * Verifica se a transação tem os dados necessários para cancelamento
     */
    private boolean hasRequiredDataForCancel(TransactionModel transaction) {
        // transactionIdAdyen é obrigatório para estorno no iFood Pago
        String adyenId = getTransactionIdAdyen(transaction);
        if (adyenId == null || adyenId.isEmpty()) {
            Log.w(TAG, "Transação não possui transactionIdAdyen, estorno não é possível");
            return false;
        }

        return true;
    }

    /**
     * Método para processar retorno do deeplink de estorno
     * Adicione este método na Activity que chama o estorno
     */
    public void handleRefundResult(Intent intent, GenericPaymentOperations.OperationCallback callback) {
        if (intent != null && intent.getData() != null) {
            Uri data = intent.getData();
            String result = data.getQueryParameter("result");

            if (result != null && !result.isEmpty()) {
                try {
                    // Decodificar Base64
                    byte[] decodedBytes = Base64.decode(result, Base64.URL_SAFE);
                    String decodedResult = new String(decodedBytes, "UTF-8");

                    Log.d(TAG, "Retorno estorno iFood: " + decodedResult);

                    JSONObject jsonResult = new JSONObject(decodedResult);
                    String status = jsonResult.optString("status", "ERROR");
                    String deviceSerialNumber = jsonResult.optString("deviceSerialNumber", "");

                    if ("SUCCESS".equalsIgnoreCase(status)) {
                        callback.onSuccess("Estorno realizado com sucesso. Terminal: " + deviceSerialNumber);
                    } else {
                        callback.onError("Erro ao realizar estorno. Status: " + status);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Erro ao processar retorno do estorno", e);
                    callback.onError("Erro ao processar retorno: " + e.getMessage());
                }
            } else {
                callback.onError("Retorno vazio do iFood Pago");
            }
        }
    }

    /**
     * Método para processar retorno do deeplink de impressão (se usar print-local)
     */
    public void handlePrintResult(Intent intent, GenericPaymentOperations.OperationCallback callback) {
        if (intent != null && intent.getData() != null) {
            Uri data = intent.getData();
            String result = data.getQueryParameter("result");

            if (result != null && !result.isEmpty()) {
                try {
                    byte[] decodedBytes = Base64.decode(result, Base64.URL_SAFE);
                    String decodedResult = new String(decodedBytes, "UTF-8");

                    Log.d(TAG, "Retorno impressão iFood: " + decodedResult);

                    JSONObject jsonResult = new JSONObject(decodedResult);
                    String status = jsonResult.optString("status", "ERROR");

                    if ("SUCCESS".equalsIgnoreCase(status)) {
                        callback.onSuccess("Impressão realizada com sucesso");
                    } else {
                        callback.onError("Erro na impressão. Status: " + status);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Erro ao processar retorno da impressão", e);
                    callback.onError("Erro ao processar retorno: " + e.getMessage());
                }
            }
        }
    }

    /**
     * MÉTODO AUXILIAR: Realizar pagamento via iFood Pago
     * Este método não faz parte da interface, mas é útil para completar a integração
     */
    public void makePayment(Context context, TransactionModel transaction, String paymentMethod,
                            GenericPaymentOperations.OperationCallback callback) {
        try {
            Log.d(TAG, "Iniciando pagamento iFood Pago");

            JSONObject paymentData = new JSONObject();

            // Método de pagamento: DEBIT, CREDIT, PIX, VOUCHER
            paymentData.put("paymentMethod", paymentMethod.toUpperCase());

            // Valor em centavos
            long valueInCents = transaction.getAmountCents() > 0 ?
                    transaction.getAmountCents() :
                    Math.round(transaction.getAmount() * 100);
            paymentData.put("value", valueInCents);

            // ID da transação do seu sistema
            paymentData.put("transactionId", transaction.getTransactionId());

            // Número da mesa (opcional)
//            if (transaction.getTableNumber() != null) {
//                paymentData.put("tableId", transaction.getTableNumber());
//            }

            // Imprimir comprovante
            paymentData.put("printReceipt", true);

            // URL de retorno
            paymentData.put("urlToReturn", RETURN_SCHEME + "://payment-result?result=");

            // Retorno via Intent Result
            paymentData.put("sendResultInSameIntent", false);

            // Codificar em Base64
            String contentBase64 = Base64.encodeToString(
                    paymentData.toString().getBytes("UTF-8"),
                    Base64.NO_WRAP | Base64.URL_SAFE | Base64.NO_PADDING
            );

            Uri paymentUri = Uri.parse(IFOOD_BASE_URL + "/make-payment?content=" + contentBase64);

            Log.d(TAG, "URI de pagamento iFood: " + paymentUri.toString());
            Log.d(TAG, "Dados do pagamento: " + paymentData.toString());

            Intent intent = new Intent(Intent.ACTION_VIEW, paymentUri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                callback.onSuccess("Pagamento iniciado via iFood Pago");
            } else {
                callback.onError("App iFood Pago não encontrado");
            }

        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar pagamento iFood Pago", e);
            callback.onError("Erro ao processar pagamento: " + e.getMessage());
        }
    }

    /**
     * Processar retorno de pagamento
     */
    public void handlePaymentResult(Intent intent, GenericPaymentOperations.OperationCallback callback) {
        if (intent != null && intent.getData() != null) {
            Uri data = intent.getData();
            String result = data.getQueryParameter("result");

            if (result != null && !result.isEmpty()) {
                try {
                    byte[] decodedBytes = Base64.decode(result, Base64.URL_SAFE);
                    String decodedResult = new String(decodedBytes, "UTF-8");

                    Log.d(TAG, "Retorno pagamento iFood: " + decodedResult);

                    JSONObject jsonResult = new JSONObject(decodedResult);
                    String status = jsonResult.optString("status", "ERROR");
                    String transactionIdAdyen = jsonResult.optString("transactionIdAdyen", "");
                    String cardBrand = jsonResult.optString("cardBrand", "");
                    String authCode = jsonResult.optString("authCode", "");
                    String errorReason = jsonResult.optString("errorReason", "");

                    if ("SUCCESS".equalsIgnoreCase(status)) {
                        String message = "Pagamento aprovado!\n" +
                                "Bandeira: " + cardBrand + "\n" +
                                "NSU: " + authCode + "\n" +
                                "ID Adyen: " + transactionIdAdyen;
                        callback.onSuccess(message);

                        // IMPORTANTE: Armazenar transactionIdAdyen para poder fazer estorno depois
                        Log.i(TAG, "GUARDAR transactionIdAdyen: " + transactionIdAdyen);
                    } else {
                        callback.onError("Pagamento recusado: " + errorReason);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Erro ao processar retorno do pagamento", e);
                    callback.onError("Erro ao processar retorno: " + e.getMessage());
                }
            }
        }
    }
}