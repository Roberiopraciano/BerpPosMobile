package br.com.berpsistemas.BerpPOSMobile.pagamento;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import br.com.berpsistemas.BerpPOSMobile.model.PagamentoModel;
import br.com.berpsistemas.BerpPOSMobile.model.TransactionModel;
import br.com.berpsistemas.BerpPOSMobile.database.EnhancedTransactionManagerV2;
import br.com.berpsistemas.BerpPOSMobile.Controller.Proxy;
import br.com.berpsistemas.BerpPOSMobile.model.BerpModel;
import br.com.berpsistemas.BerpPOSMobile.model.Variaveis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * PaymentCallbackHandler - VERSÃO UNIVERSAL COM SINCRONIZAÇÃO BACKEND
 */
public class PaymentCallbackHandler implements IPaymentCallbackHandler {
    private static final String TAG = "UniversalPaymentCallbackHandler";
    private PaymentListener paymentListener;
    private static PaymentCallbackHandler instance;

    // Gerenciador de transações universal
    private EnhancedTransactionManagerV2 transactionManager;

    // Cache de resultados "pegajosos"
    private PagamentoModel cachedRefundSuccess = null;
    private Object cachedPaymentSuccess = null;
    private String cachedError = null;
    private boolean cachedCancellation = false;

    public static synchronized PaymentCallbackHandler getInstance() {
        if (instance == null) {
            instance = new PaymentCallbackHandler();
        }
        return instance;
    }

    private PaymentCallbackHandler() {}

    /**
     * Inicializa o gerenciador universal de transações
     */
    public void initialize(Context context) {
        if (transactionManager == null) {
            transactionManager = new EnhancedTransactionManagerV2(context);
            Log.d(TAG, "TransactionManager universal inicializado");
        }
    }

    @Override
    public void setPaymentListener(PaymentListener listener) {
        this.paymentListener = listener;
        Log.d(TAG, "Payment listener registrado: " + (listener != null));

        if (listener != null) {
            deliverCachedResult();
        }
    }

    private void deliverCachedResult() {
        if (cachedRefundSuccess != null) {
            Log.d(TAG, "Entregando resultado de estorno em cache.");
            paymentListener.onRefundSuccess(cachedRefundSuccess);
            cachedRefundSuccess = null;
        } else if (cachedPaymentSuccess != null) {
            Log.d(TAG, "Entregando resultado de pagamento em cache.");
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
        if (transactionManager == null) {
            initialize(context);
        }

        try {
            if (intent != null) {
                Uri uri = intent.getData();
                if (uri == null) throw new Exception("URI inválida");
                Log.d(TAG, "Callback recebido: " + uri.toString());

                if (isRefundOrCancelCallback(uri)) {
                    handleRefundCallback(context, uri);
                    return;
                }

                if (isReprintCallback(uri)) {
                    handleReprintCallback(context, uri);
                    return;
                }

                handlePaymentCallback(context, uri);
            }

        } catch (Exception e) {
            Log.e(TAG, "Erro no callback: " + e.getMessage(), e);
            Toast.makeText(context, "Erro no callback: " + e.getMessage(), Toast.LENGTH_LONG).show();
            notifyError(context, "Erro no processamento: " + e.getMessage());
        }
    }

    private boolean isRefundOrCancelCallback(Uri uri) {
        String uriString = uri.toString().toLowerCase();
        return uriString.contains("cancel") ||
                uriString.contains("reversal") ||
                uriString.contains("refund") ||
                (uri.getScheme() != null && uri.getScheme().equals("berp") && uriString.contains("cancel"));
    }

    private boolean isReprintCallback(Uri uri) {
        String uriString = uri.toString().toLowerCase();
        return uriString.contains("reprint") ||
                uriString.contains("print") ||
                (uri.getScheme() != null && uri.getScheme().equals("berp") && uriString.contains("print"));
    }

    private void handleReprintCallback(Context context, Uri uri) {
        try {
            String result = uri.toString();
            Log.d(TAG, "Callback de reimpressão recebido: " + result);

            if (result.contains("SUCCESS")) {
                Toast.makeText(context, "Reimpressão realizada com sucesso!", Toast.LENGTH_SHORT).show();

                String transactionId = uri.getQueryParameter("transaction_id");
                if (transactionId != null && transactionManager != null) {
                    boolean marked = transactionManager.markReceiptPrinted(transactionId);
                    Log.d(TAG, "Comprovante marcado como impresso: " + marked);
                }

            } else if (result.contains("ERROR") || result.contains("PRINTER_")) {
                String errorMsg = extractPrintError(result);
                Toast.makeText(context, "Erro na impressão: " + errorMsg, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Erro na reimpressão: " + errorMsg);
            }

        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar callback de reimpressão: " + e.getMessage(), e);
        }
    }

    private String extractPrintError(String result) {
        if (result.contains("PRINTER_OUT_OF_PAPER")) return "Impressora sem papel";
        if (result.contains("PRINTER_INIT_ERROR")) return "Erro ao inicializar impressora";
        if (result.contains("PRINTER_LOW_ENERGY")) return "Baixa energia";
        if (result.contains("PRINTER_BUSY")) return "Impressora ocupada";
        if (result.contains("PRINTER_UNSUPPORTED_FORMAT")) return "Formato não suportado";
        if (result.contains("PRINTER_INVALID_DATA")) return "Dados inválidos";
        if (result.contains("PRINTER_OVERHEATING")) return "Superaquecimento";
        if (result.contains("PRINTER_PAPER_JAM")) return "Papel preso";
        if (result.contains("PRINTER_PRINT_ERROR")) return "Erro genérico";
        return "Erro desconhecido";
    }

    // ========== HANDLER UNIVERSAL DE ESTORNO COM SINCRONIZAÇÃO BACKEND ==========
    private void handleRefundCallback(Context context, Uri uri) {
        try {
            // ---- Normalização de parâmetros vindos do deeplink ----
            final String pSuccess = first(uri, "success");
            final String pCode = first(uri, "code", "responsecode", "status_code");
            final String pReason = first(uri, "reason", "message");
            final String pAuthCode = first(uri, "authorizationcode", "authcode", "authorization_code");
            final String pNsu = first(uri, "authorizationcode", "authcode", "authorization_code");
            final String pOrderId = first(uri, "order_id", "orderid");
            final String pTransactionId = first(uri, "atk", "transaction_id", "transactionid");
            final String pPlatformId = first(uri, "atk", "platformid");

            final String pAmountRaw = first(uri, "canceledamount", "amount", "transactionamount");
            final java.math.BigDecimal amount = parseAmountFlexible(pAmountRaw);

            final String pPaymentType = first(uri, "paymenttype", "payment_type");

            final boolean success = parseSuccess(pSuccess, pCode, pReason);

            Log.d(TAG, "=== CALLBACK DE CANCELAMENTO RECEBIDO ===");
            Log.d(TAG, "success=" + pSuccess + ", code=" + pCode);
            Log.d(TAG, "authCode=" + pAuthCode + ", nsu=" + pNsu);
            Log.d(TAG, "orderId=" + pOrderId + ", transactionId=" + pTransactionId);
            Log.d(TAG, "platformId=" + pPlatformId + ", amount=" + amount);

            if (success) {
                // 1) Recupera a transação original
                TransactionModel originalTransaction = findOriginalTransactionUniversal(
                        pOrderId, pTransactionId, pPlatformId
                );

                if (originalTransaction != null) {
                    Log.d(TAG, "Transação original encontrada: " + originalTransaction.getTransactionId());

                    // 2) Cancela LOCALMENTE
                    boolean cancelOk = transactionManager.cancelTransaction(
                            originalTransaction.getTransactionId(),
                            "Estorno realizado - " + humanMsg(null, pReason, pCode)
                    );
                    Log.d(TAG, "Cancelamento local: " + cancelOk);

                    if (cancelOk) {
                        // 3) SINCRONIZAR COM BACKEND (NOVO)
                        syncCancellationWithBackend(
                                context,
                                originalTransaction,
                                pTransactionId,
                                pNsu,
                                pAuthCode,
                                amount != null ? amount.toPlainString() : "0",
                                pPlatformId,
                                pOrderId,
                                pPaymentType,
                                pCode,
                                pReason
                        );
                    } else {
                        Toast.makeText(context,
                                "Erro ao cancelar transação localmente",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.w(TAG, "Transação original não encontrada para cancelamento");
                    Toast.makeText(context,
                            "Aviso: Transação não encontrada no banco local",
                            Toast.LENGTH_LONG).show();
                }

            } else {
                // ERRO/NEGADO
                final String errorMsg = humanMsg(null, pReason, pCode);
                Toast.makeText(context, "Erro no estorno: " + errorMsg, Toast.LENGTH_LONG).show();
                notifyError(context, "Erro no estorno: " + errorMsg);
            }

        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar deeplink de estorno: " + e.getMessage(), e);
            notifyError(context, "Erro ao processar estorno: " + e.getMessage());
        }
    }

    /**
     * NOVO: Sincroniza cancelamento com o backend
     */
    private void syncCancellationWithBackend(
            final Context context,
            final TransactionModel originalTransaction,
            final String transactionId,
            final String nsu,
            final String authCode,
            final String amount,
            final String platformId,
            final String orderId,
            final String paymentType,
            final String code,
            final String reason
    ) {
        // Mostrar feedback inicial
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context,
                        "Sincronizando cancelamento com servidor...",
                        Toast.LENGTH_SHORT).show()
        );

        // Executar em background
        new Thread(() -> {
            try {
                // Gerar controle de duplicidade
                String controle = BerpModel.ControleDuplicidade();

                // Identificador para o backend (prioriza NSU)
                String idParaBackend = nsu != null && !nsu.isEmpty() ? nsu :
                        transactionId != null && !transactionId.isEmpty() ? transactionId :
                                originalTransaction.getNsu();

                Log.d(TAG, "=== SINCRONIZANDO COM BACKEND ===");
                Log.d(TAG, "ID usado: " + idParaBackend);
                Log.d(TAG, "Controle: " + controle);

                // Chamar Proxy.deletePagamento
                CompletableFuture<Boolean> future = Proxy.deletePagamento(nsu,transactionId,orderId, controle);

                Boolean backendSuccess = future.get(15, java.util.concurrent.TimeUnit.SECONDS);

                // Voltar para UI thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (backendSuccess != null && backendSuccess) {
                        Log.d(TAG, "✓ Cancelamento sincronizado com sucesso no backend");

//                        // Criar registro de estorno para auditoria
//                        createUniversalRefundRecord(
//                                originalTransaction,
//                                amount,
//                                platformId,
//                                transactionId,
//                                nsu
//                        );

                        // Criar modelo de pagamento cancelado para UI
                        PagamentoModel pagCancelado = createUniversalCancelledPaymentModel(
                                orderId, transactionId, platformId, amount
                        );
                        pagCancelado.setAutorizacao(authCode);
                        pagCancelado.setRede(paymentType);
                        pagCancelado.setNsu(nsu);

                        Toast.makeText(context,
                                "Estorno realizado e sincronizado com sucesso!",
                                Toast.LENGTH_LONG).show();

                        // Notificar listener
                        if (paymentListener != null) {
                            paymentListener.onRefundSuccess(pagCancelado);
                        } else {
                            Log.w(TAG, "Listener nulo, guardando em cache");
                            cachedRefundSuccess = pagCancelado;
                        }

                    } else {
                        Log.w(TAG, "⚠ Backend retornou false para cancelamento");
                        handleBackendSyncFailure(context, originalTransaction,
                                transactionId, nsu, authCode, amount,
                                platformId, orderId, paymentType,
                                "Servidor não confirmou o cancelamento");
                    }
                });

            } catch (java.util.concurrent.TimeoutException e) {
                Log.e(TAG, "✗ Timeout ao sincronizar com backend", e);
                new Handler(Looper.getMainLooper()).post(() ->
                        handleBackendSyncFailure(context, originalTransaction,
                                transactionId, nsu, authCode, amount,
                                platformId, orderId, paymentType,
                                "Timeout ao conectar com servidor")
                );

            } catch (Exception e) {
                Log.e(TAG, "✗ Erro ao sincronizar com backend: " + e.getMessage(), e);
                new Handler(Looper.getMainLooper()).post(() ->
                        handleBackendSyncFailure(context, originalTransaction,
                                transactionId, nsu, authCode, amount,
                                platformId, orderId, paymentType,
                                "Erro: " + e.getMessage())
                );
            }
        }).start();
    }

    /**
     * NOVO: Trata falha na sincronização com backend
     */
    private void handleBackendSyncFailure(
            Context context,
            TransactionModel originalTransaction,
            String transactionId,
            String nsu,
            String authCode,
            String amount,
            String platformId,
            String orderId,
            String paymentType,
            String errorMsg
    ) {
        Log.w(TAG, "Falha na sincronização: " + errorMsg);

        // Mesmo com falha no backend, notificar UI pois o cancelamento local foi feito
        PagamentoModel pagCancelado = createUniversalCancelledPaymentModel(
                orderId, transactionId, platformId, amount
        );
        pagCancelado.setAutorizacao(authCode);
        pagCancelado.setRede(paymentType);
        pagCancelado.setNsu(nsu);

        Toast.makeText(context,
                "Aviso: Cancelamento local OK, mas servidor não confirmou\n" + errorMsg,
                Toast.LENGTH_LONG).show();

        // Ainda assim notificar o listener (cancelamento local foi bem-sucedido)
        if (paymentListener != null) {
            paymentListener.onRefundSuccess(pagCancelado);
        } else {
            cachedRefundSuccess = pagCancelado;
        }
    }

    // ========== MÉTODOS AUXILIARES ==========

    private String first(Uri uri, String... keys) {
        for (String k : keys) {
            final String v = uri.getQueryParameter(k);
            if (v != null && !v.trim().isEmpty()) return v.trim();
        }
        return null;
    }

    private boolean parseSuccess(String successRaw, String codeRaw, String reasonRaw) {
        if (successRaw != null) {
            final String s = successRaw.trim().toLowerCase();
            if (s.equals("true") || s.equals("1") || s.equals("yes")) return true;
            if (s.equals("false") || s.equals("0") || s.equals("no")) return false;
        }
        String ok = null;
        if (codeRaw != null) ok = codeRaw.trim().toUpperCase();
        else if (reasonRaw != null) ok = reasonRaw.trim().toUpperCase();

        if (ok != null) {
            if (ok.equals("0") || ok.equals("00") || ok.equals("0000")
                    || ok.equals("SUCCESS") || ok.equals("APPROVED")) {
                return true;
            }
        }
        return false;
    }

    private java.math.BigDecimal parseAmountFlexible(String raw) {
        if (raw == null || raw.isEmpty()) return java.math.BigDecimal.ZERO;
        String s = raw.trim().replace(",", ".");
        try {
            java.math.BigDecimal v = new java.math.BigDecimal(s);
            if (v.compareTo(new java.math.BigDecimal("1000")) >= 0) {
                return v.movePointLeft(2);
            }
            return v;
        } catch (Exception e) {
            Log.w(TAG, "Falha ao converter amount: " + raw, e);
            return java.math.BigDecimal.ZERO;
        }
    }

    private String bestId(String... candidates) {
        for (String c : candidates) {
            if (c != null && !c.trim().isEmpty()) return c.trim();
        }
        return null;
    }

    private String humanMsg(String message, String reason, String code) {
        StringBuilder sb = new StringBuilder();
        if (message != null && !message.isEmpty()) sb.append(message);
        if (reason != null && !reason.isEmpty()) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(reason);
        }
        if (code != null && !code.isEmpty()) {
            if (sb.length() > 0) sb.append(" (");
            else sb.append("(");
            sb.append("código: ").append(code).append(")");
        }
        return sb.length() == 0 ? "Sem descrição" : sb.toString();
    }

    private TransactionModel findOriginalTransactionUniversal(String orderId, String transactionId, String platformId) {
        if (transactionManager == null) return null;

        if (orderId != null && !orderId.isEmpty()) {
            TransactionModel transaction = transactionManager.findTransactionById(orderId);
            if (transaction != null) {
                Log.d(TAG, "Transação encontrada por orderId: " + orderId);
                return transaction;
            }
        }

        if (transactionId != null && !transactionId.isEmpty()) {
            TransactionModel transaction = transactionManager.findTransactionById(transactionId);
            if (transaction != null) {
                Log.d(TAG, "Transação encontrada por transactionId: " + transactionId);
                return transaction;
            }
        }

        if (platformId != null && !platformId.isEmpty()) {
            TransactionModel transaction = transactionManager.findTransactionById(platformId);
            if (transaction != null) {
                Log.d(TAG, "Transação encontrada por platformId: " + platformId);
                return transaction;
            }
        }

        TransactionModel lastTransaction = transactionManager.getLastTransaction();
        if (lastTransaction != null && lastTransaction.isApproved() && !lastTransaction.isCancelled()) {
            Log.d(TAG, "Usando última transação como fallback");
            return lastTransaction;
        }

        Log.w(TAG, "Nenhuma transação encontrada para cancelamento");
        return null;
    }

    private void createUniversalRefundRecord(
            TransactionModel originalTransaction,
            String refundAmount,
            String platformId,
            String transactionCancel,
            String nsu
    ) {
        try {
            if (transactionManager == null) return;

            double refundValue = originalTransaction.getAmount();
            if (refundAmount != null && !refundAmount.isEmpty()) {
                try {
                    refundValue = Double.parseDouble(refundAmount);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Erro ao converter valor do estorno, usando valor original");
                }
            }

            boolean refundSaved = transactionManager.saveRefundTransaction(
                    transactionCancel,
                    refundValue,
                    "Estorno via callback - PlatformId: " + (platformId != null ? platformId : "N/A"),
                    originalTransaction.getAcquirer(),
                    transactionCancel,
                    nsu,
                    originalTransaction.getOrderId(),
                    originalTransaction.getTransactionId()
            );

            Log.d(TAG, "Registro de estorno salvo: " + refundSaved);

        } catch (Exception e) {
            Log.e(TAG, "Erro ao criar registro de estorno: " + e.getMessage(), e);
        }
    }

    private PagamentoModel createUniversalCancelledPaymentModel(
            String orderId,
            String transactionId,
            String platformId,
            String amount
    ) {
        PagamentoModel pagCancelado = new PagamentoModel();

        if (orderId != null) {
            pagCancelado.setIdOrder(orderId);
            pagCancelado.setPgpCdusua(Variaveis.getUserId());
        }

        if (transactionId != null) {
            pagCancelado.setTransactionId(transactionId);
        }

        if (platformId != null) {
            pagCancelado.setIdPlataforma(platformId);
        }

        if (amount != null && !amount.isEmpty()) {
            try {
                double amountValue = Double.parseDouble(amount);
                pagCancelado.setPgpVlrpag(amountValue);
            } catch (NumberFormatException e) {
                Log.w(TAG, "Erro ao converter valor do cancelamento");
            }
        }

        pagCancelado.setStatus("CANCELLED");
        pagCancelado.setCancelledAt(System.currentTimeMillis());

        return pagCancelado;
    }

    // ========== RESTANTE DOS MÉTODOS (handlePaymentCallback, etc) ==========
    // ... (manter todo o resto do código como está)

    private void handlePaymentCallback(Context context, Uri uri) {
        try {
            Map<String, String> callbackParams = extractUniversalCallbackParams(uri);

            Log.d(TAG, String.format(
                    "Callback de pagamento: code=%s, amount=%s, auth=%s, brand=%s",
                    callbackParams.get("code"), callbackParams.get("amount"),
                    callbackParams.get("authCode"), callbackParams.get("brand")
            ));

            if (isPaymentSuccessful(callbackParams.get("code"), callbackParams.get("success"))) {
                processSuccessfulPayment(context, callbackParams);
            } else {
                processFailedPayment(context, callbackParams.get("code"), callbackParams.get("message"));
            }

        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar pagamento: " + e.getMessage(), e);
            notifyError(context, "Erro no processamento do pagamento");
        }
    }

    private Map<String, String> extractUniversalCallbackParams(Uri uri) {
        Map<String, String> params = new HashMap<>();

        params.put("code", uri.getQueryParameter("code"));
        params.put("success", uri.getQueryParameter("success"));
        params.put("message", uri.getQueryParameter("message"));

        params.put("transactionId", getFirstNonNull(
                uri.getQueryParameter("atk"),
                uri.getQueryParameter("transactionId"),
                uri.getQueryParameter("tid")
        ));

        params.put("nsu", getFirstNonNull(
                uri.getQueryParameter("authorization_code"),
                uri.getQueryParameter("authCode"),
                uri.getQueryParameter("nsu")
        ));

        params.put("orderId", getFirstNonNull(
                uri.getQueryParameter("order_id"),
                uri.getQueryParameter("paymentId"),
                uri.getQueryParameter("merchantOrderId")
        ));

        params.put("platformId", getFirstNonNull(
                uri.getQueryParameter("atk"),
                uri.getQueryParameter("cieloCode"),
                uri.getQueryParameter("aid")
        ));

        params.put("amount", uri.getQueryParameter("amount"));
        params.put("installments", getFirstNonNull(
                uri.getQueryParameter("installment_count"),
                uri.getQueryParameter("installments"),
                "1"
        ));

        params.put("brand", uri.getQueryParameter("brand"));
        params.put("maskedPan", getFirstNonNull(
                uri.getQueryParameter("pan"),
                uri.getQueryParameter("mask"),
                uri.getQueryParameter("maskedCardNumber")
        ));
        params.put("cardholderName", uri.getQueryParameter("cardholder_name"));
        params.put("paymentType", uri.getQueryParameter("type"));

        return params;
    }

    private String getFirstNonNull(String... values) {
        for (String value : values) {
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return "";
    }

    private boolean isPaymentSuccessful(String code, String success) {
        return "0".equals(code) || "true".equalsIgnoreCase(success);
    }

    private void processSuccessfulPayment(Context context, Map<String, String> params) {
        try {
            double amountReais = 0;
            String amountStr = params.get("amount");
            if (amountStr != null && !amountStr.isEmpty()) {
                amountReais = Double.parseDouble(amountStr) / 100.0;
            }

            String paymentTypeCode = mapPaymentTypeToCode(params.get("paymentType"));
            String acquirer = determineAcquirer(params);

            Toast.makeText(context,
                    String.format("Pagamento aprovado! Valor: R$ %.2f", amountReais),
                    Toast.LENGTH_LONG).show();

            if (paymentListener != null) {
                paymentListener.onPaymentSuccess(
                        params.get("brand"),
                        params.get("nsu"),
                        params.get("maskedPan"),
                        params.get("nsu"),
                        "",
                        acquirer,
                        params.get("platformId"),
                        params.get("orderId"),
                        "",
                        acquirer,
                        params.get("transactionId"),
                        params.get("orderId"),
                        "",
                        paymentTypeCode,
                        amountReais
                );
            }

        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar sucesso: " + e.getMessage(), e);
            notifyError(context, "Erro ao processar dados do pagamento");
        }
    }

    private String determineAcquirer(Map<String, String> params) {
        return "STONE";
    }

    private String mapPaymentTypeToCode(String type) {
        if (type == null) return "CRE";

        String lowerType = type.toLowerCase();
        if (lowerType.contains("débito") || lowerType.contains("debit")) {
            return "DEB";
        } else if (lowerType.contains("crédito") || lowerType.contains("credit")) {
            return "CRE";
        } else if (lowerType.contains("pix")) {
            return "PIX";
        } else if (lowerType.contains("voucher")) {
            return "VOU";
        }
        return "CRE";
    }

    private void processFailedPayment(Context context, String code, String message) {
        Log.e(TAG, "Pagamento falhou - Código: " + code + ", Mensagem: " + message);

        if ("2".equals(code)) {
            Toast.makeText(context, "Pagamento cancelado pelo usuário", Toast.LENGTH_LONG).show();

            if (paymentListener != null) {
                paymentListener.onPaymentCancelled();
            } else {
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

        if (paymentListener != null) {
            paymentListener.onPaymentError(reason);
        } else {
            cachedError = reason;
        }
    }

    // =================== MÉTODOS PÚBLICOS ===================

    public TransactionModel getLastTransaction(Context context) {
        if (transactionManager == null) {
            initialize(context);
        }
        return transactionManager != null ? transactionManager.getLastTransaction() : null;
    }

    public List<TransactionModel> getTransactionsByAcquirer(Context context, String acquirer) {
        if (transactionManager == null) {
            initialize(context);
        }
        return transactionManager != null ? transactionManager.getTransactionsByAcquirer(acquirer) :
                new java.util.ArrayList<>();
    }

    public boolean canCancelTransaction(Context context, String transactionId) {
        if (transactionManager == null) {
            initialize(context);
        }
        return transactionManager != null && transactionManager.canCancelTransaction(transactionId);
    }

    public boolean markReceiptPrinted(Context context, String transactionId) {
        if (transactionManager == null) {
            initialize(context);
        }
        return transactionManager != null && transactionManager.markReceiptPrinted(transactionId);
    }

    public Map<String, Object> getTransactionStats(Context context) {
        if (transactionManager == null) {
            initialize(context);
        }

        if (transactionManager != null) {
            Map<String, Object> stats = transactionManager.getTransactionStats();
            return stats;
        }

        return new HashMap<>();
    }
}
