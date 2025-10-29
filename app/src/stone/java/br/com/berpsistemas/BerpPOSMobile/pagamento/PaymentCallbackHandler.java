package br.com.berpsistemas.BerpPOSMobile.pagamento;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import br.com.berpsistemas.BerpPOSMobile.model.PagamentoModel;
import br.com.berpsistemas.BerpPOSMobile.model.TransactionModel;
import br.com.berpsistemas.BerpPOSMobile.database.EnhancedTransactionManagerV2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PaymentCallbackHandler - VERSÃO UNIVERSAL
 * ADAPTADO PARA MODELO UNIVERSAL E CAMPOS ESSENCIAIS TEF/PIX
 *
 * COMPATIBILIDADE TOTAL:
 * ✅ Usa TransactionModel universal
 * ✅ Busca inteligente por múltiplos IDs
 * ✅ Salva usando saveUniversalCallback
 * ✅ Compatível com Stone, Zoop, Cielo, iFood
 * ✅ Tratamento de reimpressão e cancelamento
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
        // Inicializar transaction manager se necessário
        if (transactionManager == null) {
            initialize(context);
        }

        try {
            if (intent != null) {
                Uri uri = intent.getData();
                if (uri == null) throw new Exception("URI inválida");
                Log.d(TAG, "Callback recebido: " + uri.toString());

                // Detecção do tipo de callback
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

    /**
     * Detecta se é callback de cancelamento/estorno
     */
    private boolean isRefundOrCancelCallback(Uri uri) {
        String uriString = uri.toString().toLowerCase();
        return uriString.contains("cancel") ||
                uriString.contains("reversal") ||
                uriString.contains("refund") ||
                (uri.getScheme() != null && uri.getScheme().equals("berp") && uriString.contains("cancel"));
    }

    /**
     * Detecta se é callback de reimpressão
     */
    private boolean isReprintCallback(Uri uri) {
        String uriString = uri.toString().toLowerCase();
        return uriString.contains("reprint") ||
                uriString.contains("print") ||
                (uri.getScheme() != null && uri.getScheme().equals("berp") && uriString.contains("print"));
    }

    /**
     * Trata callback de reimpressão universal
     */
    private void handleReprintCallback(Context context, Uri uri) {
        try {
            String result = uri.toString();
            Log.d(TAG, "Callback de reimpressão recebido: " + result);

            if (result.contains("SUCCESS")) {
                Toast.makeText(context, "Reimpressão realizada com sucesso!", Toast.LENGTH_SHORT).show();

                // Marcar como impresso no banco usando busca universal
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

    /**
     * Extrai mensagem de erro da impressão
     */
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

    /**
     * Processa callback de cancelamento/estorno universal
     */


    // ---------- HELPERS ----------
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

    /**
     * Converte amount de forma tolerante:
     * - "100" => assume centavos (R$ 1,00) se for grande (>=1000)
     * - "1.00" / "1,00" => reais
     */
    private java.math.BigDecimal parseAmountFlexible(String raw) {
        if (raw == null || raw.isEmpty()) return java.math.BigDecimal.ZERO;
        String s = raw.trim().replace(",", ".");
        try {
            java.math.BigDecimal v = new java.math.BigDecimal(s);
            // heurística: valores grandes provavelmente estão em centavos
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

    // ---------- HANDLER UNIVERSAL ----------
    private void handleRefundCallback(Context context, Uri uri) {
        try {
            // ---- Normalização de parâmetros vindos do deeplink ----
            final String pSuccess = first(uri, "success"); // true/false (opcional)
            final String pCode = first(uri, "code", "responsecode", "status_code");
            final String pReason = first(uri, "reason", "message"); // alguns provedores usam reason como texto
            final String pAuthCode = first(uri, "authorizationcode", "authcode", "authorization_code");

            final String pOrderId = first(uri, "order_id", "orderid");
            final String pTransactionId = first(uri, "transaction_id", "transactionid", "tid", "nsu");
            final String pPlatformId = first(uri, "atk", "platformid"); // Stone/afins

            // Valor: prioriza canceledamount -> amount -> transactionamount
            final String pAmountRaw = first(uri, "canceledamount", "amount", "transactionamount");
            final java.math.BigDecimal amount = parseAmountFlexible(pAmountRaw);

            final String pPaymentType = first(uri, "paymenttype", "payment_type");

            // Decide sucesso
            final boolean success = parseSuccess(pSuccess, pCode, pReason);

            // Melhor identificador para buscar a transação original no teu banco
            final String orderIdBest = bestId(pOrderId, pTransactionId, pPlatformId);

            Log.d(TAG, "Deeplink recebido: "
                    + "success=" + pSuccess
                    + ", code=" + pCode
                    + ", reason=" + pReason
                    + ", authCode=" + pAuthCode
                    + ", orderId=" + pOrderId
                    + ", transactionId=" + pTransactionId
                    + ", platformId(atk)=" + pPlatformId
                    + ", paymentType=" + pPaymentType
                    + ", amountRaw=" + pAmountRaw
                    + ", amount=" + amount);

            if (success) {
                // 1) Recupera a transação original (usa o que tiver disponível)
                TransactionModel originalTransaction =
                        findOriginalTransactionUniversal(pOrderId, pPlatformId, pPlatformId);

                if (originalTransaction != null) {
                    // 2) Cancela a transação original no teu gerenciador
                    boolean cancelOk = transactionManager.cancelTransaction(
                            originalTransaction.getTransactionId(),
                            "Estorno realizado - " + humanMsg(null, pReason, pCode)
                    );
                    Log.d(TAG, "Transação original cancelada? " + cancelOk);

                    // 3) Cria registro de estorno (auditoria)
                    if (cancelOk) {
                        // amount pode ser zero (alguns provedores mandam 0 no cancel)
                        createUniversalRefundRecord(originalTransaction,
                                amount != null ? amount.toPlainString() : "0",
                                pPlatformId);
                    }
                } else {
                    Log.w(TAG, "Transação original não encontrada para cancelamento. orderIdBest=" + orderIdBest);
                }

                // 4) Cria o modelo de pagamento cancelado para a UI/camada superior
                PagamentoModel pagCancelado = createUniversalCancelledPaymentModel(
                        pOrderId, pTransactionId, pPlatformId,
                        amount != null ? amount.toPlainString() : "0"
                );
                // (opcional) preencher extras úteis
                pagCancelado.setAutorizacao(pAuthCode);
                pagCancelado.setRede(pPaymentType);

                Toast.makeText(context, "Estorno realizado com sucesso!", Toast.LENGTH_LONG).show();

                if (paymentListener != null) {
                    paymentListener.onRefundSuccess(pagCancelado);
                } else {
                    Log.w(TAG, "paymentListener nulo. Guardando resultado do estorno.");
                    cachedRefundSuccess = pagCancelado; // tua variável já existente
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
     * BUSCA UNIVERSAL: Encontra transação por qualquer ID
     */
    private TransactionModel findOriginalTransactionUniversal(String orderId, String transactionId, String platformId) {
        if (transactionManager == null) return null;

        // Busca inteligente usando findTransactionById universal
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

        // Fallback: última transação ativa
        TransactionModel lastTransaction = transactionManager.getLastTransaction();
        if (lastTransaction != null && lastTransaction.isApproved() && !lastTransaction.isCancelled()) {
            Log.d(TAG, "Usando última transação como fallback");
            return lastTransaction;
        }

        Log.w(TAG, "Nenhuma transação encontrada para cancelamento");
        return null;
    }

    /**
     * Cria registro de estorno universal
     */
    private void createUniversalRefundRecord(TransactionModel originalTransaction, String refundAmount, String platformId) {
        try {
            if (transactionManager == null) return;

            // Calcular valor do estorno
            double refundValue = originalTransaction.getAmount();
            if (refundAmount != null && !refundAmount.isEmpty()) {
                try {
                    refundValue = Double.parseDouble(refundAmount) / 100.0;
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Erro ao converter valor do estorno, usando valor original");
                }
            }

            // Criar registro de estorno usando saveRefundTransaction
            boolean refundSaved = transactionManager.saveRefundTransaction(
                    originalTransaction.getTransactionId(),
                    refundValue,
                    "Estorno via callback - PlatformId: " + (platformId != null ? platformId : "N/A"),
                    originalTransaction.getAcquirer()
            );

            Log.d(TAG, "Registro de estorno salvo: " + refundSaved);

        } catch (Exception e) {
            Log.e(TAG, "Erro ao criar registro de estorno: " + e.getMessage(), e);
        }
    }

    /**
     * Cria modelo de pagamento cancelado universal
     */
    private PagamentoModel createUniversalCancelledPaymentModel(String orderId, String transactionId, String platformId, String amount) {
        PagamentoModel pagCancelado = new PagamentoModel();

        if (orderId != null) {
            pagCancelado.setIdOrder(orderId);
        }

        if (transactionId != null) {
            pagCancelado.setTransactionId(transactionId);
        }

        if (platformId != null) {
            pagCancelado.setIdPlataforma(platformId);
        }

        if (amount != null && !amount.isEmpty()) {
            try {
                double amountValue = Double.parseDouble(amount) / 100.0;
                pagCancelado.setPgpVlrpag(amountValue);
            } catch (NumberFormatException e) {
                Log.w(TAG, "Erro ao converter valor do cancelamento");
            }
        }

        pagCancelado.setStatus("CANCELLED");
        pagCancelado.setCancelledAt(System.currentTimeMillis());

        return pagCancelado;
    }

    /**
     * Processa callback de pagamento universal
     */
    private void handlePaymentCallback(Context context, Uri uri) {
        try {
            // Extrair parâmetros universais
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

    /**
     * Extrai parâmetros universais do callback
     */
    private Map<String, String> extractUniversalCallbackParams(Uri uri) {
        Map<String, String> params = new HashMap<>();

        // Parâmetros universais de status
        params.put("code", uri.getQueryParameter("code"));
        params.put("success", uri.getQueryParameter("success"));
        params.put("message", uri.getQueryParameter("message"));

        // Identificadores universais
        params.put("transactionId", getFirstNonNull(
                uri.getQueryParameter("atk"),           // Stone ITK
                uri.getQueryParameter("transactionId"), // Zoop/Generic
                uri.getQueryParameter("tid")            // Cielo TID
        ));

        params.put("nsu", getFirstNonNull(
                uri.getQueryParameter("authorization_code"), // Stone
                uri.getQueryParameter("authCode"),           // Zoop
                uri.getQueryParameter("nsu")                 // Cielo
        ));

        params.put("orderId", getFirstNonNull(
                uri.getQueryParameter("order_id"),     // Stone
                uri.getQueryParameter("paymentId"),    // Zoop
                uri.getQueryParameter("merchantOrderId") // Cielo
        ));

        params.put("platformId", getFirstNonNull(
                uri.getQueryParameter("atk"),          // Stone ATK
                uri.getQueryParameter("cieloCode"),    // Zoop
                uri.getQueryParameter("aid")           // Cielo AID
        ));

        // Dados financeiros
        params.put("amount", uri.getQueryParameter("amount"));
        params.put("installments", getFirstNonNull(
                uri.getQueryParameter("installment_count"),
                uri.getQueryParameter("installments"),
                "1"
        ));

        // Dados do cartão
        params.put("brand", uri.getQueryParameter("brand"));
        params.put("maskedPan", getFirstNonNull(
                uri.getQueryParameter("pan"),
                uri.getQueryParameter("mask"),
                uri.getQueryParameter("maskedCardNumber")
        ));
        params.put("cardholderName", uri.getQueryParameter("cardholder_name"));

        // Tipo de transação
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

    /**
     * Processa pagamento bem-sucedido universal
     */
    private void processSuccessfulPayment(Context context, Map<String, String> params) {
        try {
            // Converter valor
            double amountReais = 0;
            String amountStr = params.get("amount");
            if (amountStr != null && !amountStr.isEmpty()) {
                amountReais = Double.parseDouble(amountStr) / 100.0;
            }

            // Mapear tipo de pagamento
            String paymentTypeCode = mapPaymentTypeToCode(params.get("paymentType"));

            // Determinar adquirente baseado nos parâmetros
            String acquirer = determineAcquirer(params);

            // Criar dados de callback universal
            Map<String, Object> callbackData = new HashMap<>();
            callbackData.put("transactionId", params.get("platformId"));
            callbackData.put("nsu", params.get("nsu"));
            callbackData.put("orderId", params.get("orderId"));
            callbackData.put("platformId", params.get("itk"));
            callbackData.put("amount", amountReais);
            callbackData.put("acquirer", acquirer);
            callbackData.put("type", params.get("type"));
            callbackData.put("paymentTypeCode", paymentTypeCode);
            callbackData.put("cardBrand", params.get("brand"));
            callbackData.put("maskedPan", params.get("maskedPan"));
            callbackData.put("cardholderName", params.get("cardholderName"));
            callbackData.put("installments", Integer.parseInt(params.getOrDefault("installments", "1")));
            callbackData.put("status", "APPROVED");
            // A responsabilidade de salvar a transação foi movida para a PagamentoActivity
            // para garantir o fluxo PENDING -> APPROVED e evitar duplicidade.
            // if (transactionManager != null) {
            //     boolean dbSaved = transactionManager.saveUniversalCallback(acquirer, callbackData);
            //     Log.d(TAG, "Transação universal salva: " + dbSaved);
            // }

            Toast.makeText(context,
                    String.format("Pagamento aprovado! Valor: R$ %.2f", amountReais),
                    Toast.LENGTH_LONG).show();

            // Notificar listener usando dados universais
            if (paymentListener != null) {
                paymentListener.onPaymentSuccess(
                        params.get("brand"),
                        params.get("nsu"),
                        params.get("maskedPan"),
                        params.get("transactionId"),
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

    // =================== MÉTODOS PÚBLICOS UNIVERSAIS ===================

    /**
     * Obtém última transação universal
     */
    public TransactionModel getLastTransaction(Context context) {
        if (transactionManager == null) {
            initialize(context);
        }
        return transactionManager != null ? transactionManager.getLastTransaction() : null;
    }

    /**
     * Obtém todas as transações por adquirente
     */
    public List<TransactionModel> getTransactionsByAcquirer(Context context, String acquirer) {
        if (transactionManager == null) {
            initialize(context);
        }
        return transactionManager != null ? transactionManager.getTransactionsByAcquirer(acquirer) :
                new java.util.ArrayList<>();
    }

    /**
     * Verifica se pode cancelar transação
     */
    public boolean canCancelTransaction(Context context, String transactionId) {
        if (transactionManager == null) {
            initialize(context);
        }
        return transactionManager != null && transactionManager.canCancelTransaction(transactionId);
    }

    /**
     * Marca comprovante como impresso
     */
    public boolean markReceiptPrinted(Context context, String transactionId) {
        if (transactionManager == null) {
            initialize(context);
        }
        return transactionManager != null && transactionManager.markReceiptPrinted(transactionId);
    }

    /**
     * Obtém estatísticas universais
     */
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