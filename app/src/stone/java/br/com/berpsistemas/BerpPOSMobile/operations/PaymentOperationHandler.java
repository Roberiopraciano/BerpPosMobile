package br.com.berpsistemas.BerpPOSMobile.operations;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import br.com.berpsistemas.BerpPOSMobile.model.TransactionModel;

/**
 * IMPLEMENTAÇÃO STONE - PaymentOperationHandler CORRIGIDA
 *
 * Usa deeplinks oficiais da Stone conforme documentação:
 * - Cancelamento: https://sdkandroid.stone.com.br/reference/cancelamento-deeplink
 * - Reimpressão: https://sdkandroid.stone.com.br/reference/reimpressão
 */
public class PaymentOperationHandler implements GenericPaymentOperations.PaymentOperationHandler {

    private static final String TAG = "StonePaymentHandler";

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
            Log.d(TAG, "Iniciando reimpressão Stone via deeplink oficial");

            // Usar deeplink oficial da Stone para reimpressão
            // CORRIGIDO: Parâmetros conforme documentação oficial

            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.scheme("reprinter-app");
            uriBuilder.authority("reprint");

            // Parâmetros obrigatórios conforme documentação Stone
            uriBuilder.appendQueryParameter("SHOW_FEEDBACK_SCREEN", "true");
            uriBuilder.appendQueryParameter("SCHEME_RETURN", "berp"); // seu app scheme
            uriBuilder.appendQueryParameter("ATK", getAtkFromTransaction(transaction));

            // Tipo de comprovante corrigido
            String typeCustomer = "CLIENT".equalsIgnoreCase(receiptType) ? "CLIENT" : "MERCHANT";
            uriBuilder.appendQueryParameter("TYPE_CUSTOMER", typeCustomer);

            Uri reprintUri = uriBuilder.build();

            Log.d(TAG, "URI de reimpressão Stone: " + reprintUri.toString());

            Intent intent = new Intent(Intent.ACTION_VIEW, reprintUri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                callback.onSuccess("Reimpressão iniciada via Stone");
            } else {
                callback.onError("App Stone não encontrado. Instale o Stone Smart ou Stone Vendas.");
            }

        } catch (Exception e) {
            Log.e(TAG, "Erro na reimpressão Stone", e);
            callback.onError("Erro ao criar deeplink Stone: " + e.getMessage());
        }
    }

    @Override
    public void cancelTransaction(Context context, TransactionModel transaction, String reason,
                                  GenericPaymentOperations.OperationCallback callback) {
        try {
            Log.d(TAG, "Iniciando cancelamento Stone via deeplink oficial");

            // Usar deeplink oficial da Stone para cancelamento
            // Baseado na documentação: https://sdkandroid.stone.com.br/reference/cancelamento-deeplink

            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.scheme("cancel-app");
            uriBuilder.authority("cancel");
            uriBuilder.appendQueryParameter("returnscheme", "berp"); // CORRIGIDO: usar o mesmo scheme

            // Parâmetros obrigatórios conforme documentação Stone
            uriBuilder.appendQueryParameter("atk", getAtkFromTransaction(transaction));
            uriBuilder.appendQueryParameter("amount", String.valueOf(getAmountInCents(transaction)));

            // Parâmetros opcionais mas recomendados
            if (transaction.getTransactionId() != null) {
                uriBuilder.appendQueryParameter("transaction_id", transaction.getTransactionId());
            }

            if (transaction.getPaymentId() != null) {
                uriBuilder.appendQueryParameter("order_id", transaction.getPaymentId());
            }

            if (transaction.getAuthorizationCode() != null) {
                uriBuilder.appendQueryParameter("authorization_code", transaction.getAuthorizationCode());
            }

            Uri cancelUri = uriBuilder.build();

            Log.d(TAG, "URI de cancelamento Stone: " + cancelUri.toString());

            Intent intent = new Intent(Intent.ACTION_VIEW, cancelUri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                callback.onSuccess("Cancelamento iniciado via Stone");
            } else {
                callback.onError("App Stone não encontrado. Instale o Stone Smart ou Stone Vendas.");
            }

        } catch (Exception e) {
            Log.e(TAG, "Erro no cancelamento Stone", e);
            callback.onError("Erro ao criar deeplink Stone: " + e.getMessage());
        }
    }

    /**
     * Extrai o ATK (Authorizer Transaction Key) da transação
     * ATK é obrigatório para reimpressão e cancelamento na Stone
     */
    private String getAtkFromTransaction(TransactionModel transaction) {
        // Tentar extrair ATK de diferentes campos

        // 1. Verificar se está no idPlataforma (campo usado no código antigo)
        String atk = transaction.getExternalId();
        if (atk != null && !atk.isEmpty() && !atk.equals("\"\"")) {
            return atk.replace("\"", ""); // remover aspas se existirem
        }

        // 2. Verificar metadados
        if (transaction.getFlavorSpecificData() != null && !transaction.getFlavorSpecificData().isEmpty()) {
            try {
                // Se metadados estiver em JSON, tentar extrair ATK
                if (transaction.getFlavorSpecificData().contains("atk")) {
                    String metadata = transaction.getFlavorSpecificData();
                    // Parsing simples para extrair ATK
                    String[] parts = metadata.split("atk");
                    if (parts.length > 1) {
                        String atkPart = parts[1].replaceAll("[^a-zA-Z0-9_-]", "");
                        if (atkPart.length() > 5) { // ATK tem um tamanho mínimo
                            return atkPart;
                        }
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Erro ao extrair ATK dos metadados: " + e.getMessage());
            }
        }

        // 3. Usar transactionId como fallback (pode funcionar em alguns casos)
        if (transaction.getTransactionId() != null && !transaction.getTransactionId().isEmpty()) {
            Log.w(TAG, "Usando transactionId como ATK (fallback)");
            return transaction.getTransactionId();
        }

        Log.w(TAG, "ATK não encontrado na transação, usando valor padrão");
        return "00000000000000"; // Valor padrão conforme exemplo da documentação
    }

    /**
     * Converte valor para centavos
     */
    private long getAmountInCents(TransactionModel transaction) {
        // Se já está em centavos
        if (transaction.getAmountCents() > 0) {
            return transaction.getAmountCents();
        }

        // Converter de reais para centavos
        return Math.round(transaction.getAmount() * 100);
    }

    /**
     * Verifica se a transação tem os dados necessários para cancelamento
     */
    private boolean hasRequiredDataForCancel(TransactionModel transaction) {
        // ATK é obrigatório para cancelamento na Stone
        String atk = getAtkFromTransaction(transaction);
        if (atk == null || atk.isEmpty()) {
            Log.w(TAG, "Transação não possui ATK, cancelamento pode falhar");
            return false;
        }

        // Valor é obrigatório
        if (transaction.getAmount() <= 0 && transaction.getAmountCents() <= 0) {
            Log.w(TAG, "Transação não possui valor válido");
            return false;
        }

        return true;
    }

    /**
     * Método para processar retorno do deeplink de reimpressão
     * Adicione este método na Activity que chama a reimpressão
     */
    public void handleReprintResult(Intent intent, GenericPaymentOperations.OperationCallback callback) {
        if (intent != null && intent.getData() != null) {
            String result = intent.getData().toString();
            Log.d(TAG, "Retorno reimpressão Stone: " + result);

            if (result.contains("SUCCESS")) {
                callback.onSuccess("Reimpressão realizada com sucesso");
            } else if (result.contains("PRINTER_OUT_OF_PAPER")) {
                callback.onError("Impressora sem papel ou com a tampa aberta");
            } else if (result.contains("PRINTER_INIT_ERROR")) {
                callback.onError("Erro ao inicializar a impressora");
            } else if (result.contains("PRINTER_LOW_ENERGY")) {
                callback.onError("Máquina com baixa energia");
            } else if (result.contains("PRINTER_BUSY")) {
                callback.onError("Impressora ocupada");
            } else if (result.contains("PRINTER_UNSUPPORTED_FORMAT")) {
                callback.onError("Formato não suportado");
            } else if (result.contains("PRINTER_INVALID_DATA")) {
                callback.onError("Dados inválidos para impressão");
            } else if (result.contains("PRINTER_OVERHEATING")) {
                callback.onError("Superaquecimento da impressora");
            } else if (result.contains("PRINTER_PAPER_JAM")) {
                callback.onError("Papel preso na impressora");
            } else if (result.contains("PRINTER_PRINT_ERROR")) {
                callback.onError("Erro genérico da impressora");
            } else {
                callback.onError("Erro desconhecido: " + result);
            }
        }
    }
}