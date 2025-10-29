package br.com.berpsistemas.BerpPOSMobile.pagamento;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import br.com.berpsistemas.BerpPOSMobile.model.BerpModel;
import br.com.berpsistemas.BerpPOSMobile.model.PagamentoModel;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.UUID;

public class Pagamento implements IPagamento {
    private static final String TAG = "GetnetPagamento";
    private static final int REQUEST_CODE_PAYMENT = 3001;
    private static final int REQUEST_CODE_CANCEL = 3002;
    private Activity context;

    // Constantes para tipos de pagamento da Getnet
    public static final String CREDIT = "credit";
    public static final String DEBIT = "debit";
    public static final String VOUCHER = "voucher";
    public static final String PIX = "pix";

    // Constantes para tipos de parcelamento da Getnet
    public static final String CREDIT_MERCHANT = "creditMerchant"; // parcelado lojista
    public static final String CREDIT_ISSUER = "creditIssuer";     // parcelado emissor

    // Parâmetros de requisição conforme documentação Getnet
    private static final String PARAM_PAYMENT_TYPE = "paymentType";
    private static final String PARAM_CREDIT_TYPE = "creditType";
    private static final String PARAM_INSTALLMENTS = "installments";
    private static final String PARAM_AMOUNT = "amount";
    private static final String PARAM_CURRENCY_POSITION = "currencyPosition";
    private static final String PARAM_CURRENCY_CODE = "currencyCode";
    private static final String PARAM_CALLER_ID = "callerId";
    private static final String PARAM_ORDER_ID = "orderId";
    private static final String PARAM_ALLOW_PRINT_CURRENT_TRANSACTION = "allowPrintCurrentTransaction";
    private static final String PARAM_DCC_RECEIPT_IMPLEMENTED = "dccReceiptImplemented";

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

    public Pagamento(Activity context) {
        this.context = context;
    }

    @Override
    public void iniciarPagamentoDeeplink(Activity activity, PaymentConfig config) {
        try {
            if (!validarConfigPagamento(config)) {
                FancyToast.makeText(activity, "Configuração de pagamento inválida",
                        FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
                return;
            }

            // Gera um callerId único conforme exigido pela Getnet v3
            String callerId = UUID.randomUUID().toString();

            // Mapeia o tipo de transação para o formato Getnet
            String paymentType = mapearTipoTransacao(config.getTransactionType());

            // Constrói o Bundle com parâmetros obrigatórios
            Bundle bundle = new Bundle();
            bundle.putString(PARAM_PAYMENT_TYPE, paymentType);
            bundle.putString(PARAM_AMOUNT, formatarValorGetnet(config.getAmountInCents()));
            bundle.putString(PARAM_CURRENCY_POSITION, "CURRENCY_BEFORE_AMOUNT");
            bundle.putString(PARAM_CURRENCY_CODE, "986"); // Real brasileiro
            bundle.putString(PARAM_CALLER_ID, callerId);

            // Adiciona orderId se fornecido
            if (config.getOrderId() != null && !config.getOrderId().isEmpty()) {
                bundle.putString(PARAM_ORDER_ID, config.getOrderId());
            }

            // Configura parcelamento se for crédito
            if (CREDIT.equals(paymentType) && config.getInstallments() > 1) {
                bundle.putString(PARAM_CREDIT_TYPE, CREDIT_MERCHANT); // ou CREDIT_ISSUER
                bundle.putString(PARAM_INSTALLMENTS, String.valueOf(config.getInstallments()));
            }

            // Configurações de impressão (opcional)
            bundle.putString(PARAM_ALLOW_PRINT_CURRENT_TRANSACTION, "false"); // Getnet imprime
            bundle.putString(PARAM_DCC_RECEIPT_IMPLEMENTED, "false");

            // Cria Intent com deeplink Getnet v3 (recomendado)
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("getnet://pagamento/v3/payment"));
            intent.putExtras(bundle);

            Log.d(TAG, "Iniciando pagamento Getnet com parâmetros:");
            Log.d(TAG, "PaymentType: " + paymentType);
            Log.d(TAG, "Amount: " + formatarValorGetnet(config.getAmountInCents()));
            Log.d(TAG, "CallerId: " + callerId);
            Log.d(TAG, "OrderId: " + config.getOrderId());

            // Tenta abrir o aplicativo Getnet
            try {
                activity.startActivityForResult(intent, REQUEST_CODE_PAYMENT);
                FancyToast.makeText(activity, "Iniciando pagamento via Getnet...",
                        FancyToast.LENGTH_LONG, FancyToast.INFO, true).show();

            } catch (Exception e) {
                Log.e(TAG, "Erro ao abrir App Getnet: " + e.getMessage());
                FancyToast.makeText(activity, "Erro: App Getnet não encontrado. " + e.getMessage(),
                        FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar pagamento Getnet: " + e.getMessage(), e);
            FancyToast.makeText(activity, "Erro ao iniciar pagamento: " + e.getMessage(),
                    FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
        }
    }

    @Override
    public void iniciarPagamentoProvider(Activity activity, PaymentConfig config) {
        // Utiliza o mesmo método para o provider
        iniciarPagamentoDeeplink(activity, config);
    }

    @Override
    public void realizarReembolso(Activity activity, PagamentoModel pag) {
        try {
            // Gera um callerId único para o reembolso
            String callerId = UUID.randomUUID().toString();

            Bundle bundle = new Bundle();
            bundle.putString(PARAM_AMOUNT, formatarValorGetnet((int)(pag.getPgpVlrpag() * 100)));
            bundle.putString(PARAM_CALLER_ID, callerId);

            // Adiciona informações do pagamento original para reembolso
            if (pag.getNsu() != null && !pag.getNsu().isEmpty()) {
                bundle.putString(ARG_NSU, pag.getNsu());
            }
            if (pag.getAutorizacao() != null && !pag.getAutorizacao().isEmpty()) {
                bundle.putString(ARG_AUTHORIZATION_CODE, pag.getAutorizacao());
            }
            if (pag.getIdOrder() != null && !pag.getIdOrder().isEmpty()) {
                bundle.putString(PARAM_ORDER_ID, pag.getIdOrder());
            }

            // Cria Intent com deeplink de reembolso Getnet
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("getnet://pagamento/v1/refund"));
            intent.putExtras(bundle);

            Log.d(TAG, "Iniciando reembolso Getnet:");
            Log.d(TAG, "Amount: " + formatarValorGetnet((int)(pag.getPgpVlrpag() * 100)));
            Log.d(TAG, "NSU: " + pag.getNsu());
            Log.d(TAG, "CallerId: " + callerId);

            try {
                activity.startActivityForResult(intent, REQUEST_CODE_CANCEL);
                FancyToast.makeText(activity, "Iniciando reembolso via Getnet...",
                        FancyToast.LENGTH_LONG, FancyToast.INFO, true).show();

            } catch (Exception e) {
                Log.e(TAG, "Erro ao abrir App Getnet para reembolso: " + e.getMessage());
                FancyToast.makeText(activity, "Erro: App Getnet não disponível para reembolso",
                        FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar reembolso Getnet: " + e.getMessage(), e);
            FancyToast.makeText(activity, "Erro ao iniciar reembolso: " + e.getMessage(),
                    FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
        }
    }

    @Override
    public void processarResultado(int requestCode, int resultCode, Intent data) {
        // O resultado será processado na PaymentCallbackActivity
        Log.d(TAG, "Resultado recebido - RequestCode: " + requestCode + ", ResultCode: " + resultCode);

        if (data != null && data.getExtras() != null) {
            Bundle extras = data.getExtras();
            Log.d(TAG, "Dados extras recebidos:");
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                Log.d(TAG, key + " = " + value);
            }
        }
    }

    /**
     * Mapeia tipo de transação para formato Getnet
     */
    private String mapearTipoTransacao(String transactionType) {
        if (transactionType == null || transactionType.isEmpty()) {
            return CREDIT;
        }

        switch (transactionType.toLowerCase()) {
            case "debit":
            case "debito":
            case "débito":
                return DEBIT;

            case "credit":
            case "credito":
            case "crédito":
                return CREDIT;

            case "pix":
                return PIX;

            case "voucher":
                return VOUCHER;

            default:
                return CREDIT;
        }
    }

    /**
     * Formata valor para o padrão Getnet (12 dígitos, últimos 2 são decimais)
     * Exemplo: 1234 centavos = "000000001234" = R$ 12,34
     */
    private String formatarValorGetnet(int amountInCents) {
        return String.format("%012d", amountInCents);
    }

    /**
     * Valida configuração de pagamento
     */
    private boolean validarConfigPagamento(PaymentConfig config) {
        if (config == null) {
            Log.e(TAG, "PaymentConfig é null");
            return false;
        }

        if (config.getAmountInCents() <= 0) {
            Log.e(TAG, "Valor inválido: " + config.getAmountInCents());
            return false;
        }

        return true;
    }

    /**
     * Método auxiliar para criar referência do pagamento
     */
    private String criarReferenciaPagamento(PaymentConfig config) {
        return BerpModel.getNmTpvend() + ": " + BerpModel.getNumMesa() + "-" +
                config.getOrderId() + " G: " + BerpModel.getFuncionario();
    }

    public void setCallback(IPagamentoCallback callback) {
        // Implementação do callback se necessário
    }

    /**
     * Retorna códigos de requisição para identificação
     */
    public static int getPaymentRequestCode() {
        return REQUEST_CODE_PAYMENT;
    }

    public static int getCancelRequestCode() {
        return REQUEST_CODE_CANCEL;
    }
}