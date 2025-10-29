package br.com.berpsistemas.BerpPOSMobile.pagamento;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import br.com.berpsistemas.BerpPOSMobile.model.BerpModel;
import br.com.berpsistemas.BerpPOSMobile.model.PagamentoModel;

import com.shashank.sony.fancytoastlib.FancyToast;

public class Pagamento implements IPagamento {
    private static final int REQUEST_CODE_PAYMENT = 3001;
    private static final int REQUEST_CODE_CANCEL = 3002;
    private Activity context;

    // Constantes para tipos de pagamento da Stone
    public static final String DEBIT = "DEBIT";
    public static final String CREDIT = "CREDIT";
    public static final String VOUCHER = "VOUCHER";
    public static final String INSTANT_PAYMENT = "INSTANT_PAYMENT";
    public static final String PIX = "PIX";

    // Constantes para tipos de parcelamento
    public static final String INSTALLMENT_MERCHANT = "MERCHANT"; // parcelado sem juros
    public static final String INSTALLMENT_ISSUER = "ISSUER";     // parcelado com juros
    public static final String INSTALLMENT_NONE = "NONE";        // à vista

    public Pagamento(Activity context) {
        this.context = context;
    }

    @Override
    public void iniciarPagamentoDeeplink(Activity activity, PaymentConfig config) {
        try {
            // Mapeia o tipo de transação para o formato Stone
            String transactionType = mapearTipoTransacao(config.getTransactionType());

            // Determina tipo de parcelamento e quantidade
            String installmentType = INSTALLMENT_MERCHANT;
            int installmentCount = 1;

            if (CREDIT.equals(transactionType) && config.getInstallments() > 1) {
                installmentType = INSTALLMENT_MERCHANT; // ou INSTALLMENT_ISSUER conforme necessário
                installmentCount = config.getInstallments();
            }

            // Constrói a URI usando Uri.Builder conforme padrão Stone
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.authority("pay");
            uriBuilder.scheme("payment-app");
            uriBuilder.appendQueryParameter("return_scheme", "order");





            uriBuilder.appendQueryParameter("amount", String.valueOf(config.getAmountInCents()));
            uriBuilder.appendQueryParameter("editable_amount", "0"); // não editável
            uriBuilder.appendQueryParameter("transaction_type", transactionType);
            if (CREDIT.equals(transactionType) && config.getInstallments() > 0) {
                installmentType = INSTALLMENT_NONE; // ou INSTALLMENT_ISSUER conforme necessário
                installmentCount = config.getInstallments();
                uriBuilder.appendQueryParameter("installment_type", installmentType);
                uriBuilder.appendQueryParameter("installment_count", String.valueOf(installmentCount));
            }

            uriBuilder.appendQueryParameter("order_id", config.getOrderId());

            Uri paymentUri = uriBuilder.build();

            Log.d("STONE_PAYMENT", "URI de pagamento: " + paymentUri.toString());

            // Cria o intent para abrir o aplicativo Stone
            Intent paymentIntent = new Intent(Intent.ACTION_VIEW);
            paymentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            paymentIntent.setData(paymentUri);

            // Tenta abrir o deeplink diretamente
            try {
                activity.startActivityForResult(paymentIntent, REQUEST_CODE_PAYMENT);
                FancyToast.makeText(activity, "Iniciando pagamento via Stone...",
                        FancyToast.LENGTH_LONG, FancyToast.INFO, true).show();

            } catch (Exception e) {
                Log.e("STONE_PAYMENT", "Erro ao abrir Stone App: " + e.getMessage());
                FancyToast.makeText(activity, "Erro ao conectar com Stone: " + e.getMessage(),
                        FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
            }

        } catch (Exception e) {
            Log.e("STONE_PAYMENT", "Erro ao iniciar pagamento: " + e.getMessage(), e);
            FancyToast.makeText(activity, "Erro ao iniciar pagamento: " + e.getMessage(),
                    FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
        }
    }

    @Override
    public void iniciarPagamentoProvider(Activity activity, PaymentConfig config) {
        // Utiliza o mesmo método para o provider
        iniciarPagamentoDeeplink(activity, config);
    }

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

            case "instant_payment":
            case "pagamento_instantaneo":
                return INSTANT_PAYMENT;

            default:
                return CREDIT;
        }
    }

    @Override
    public void realizarReembolso(Activity activity, PagamentoModel pag) {
        try {
            // Para cancelamento/estorno na Stone, normalmente seria um deeplink diferente
            // A documentação não mostrou o padrão de cancelamento, então usando o mesmo padrão

            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.authority("cancel"); // ou "reversal" - dependendo da documentação Stone
            uriBuilder.scheme("cancel-app");
            uriBuilder.appendQueryParameter("returnscheme", "order");
            uriBuilder.appendQueryParameter("atk", pag.getIdPlataforma().replace("\"", ""));
            uriBuilder.appendQueryParameter("amount", String.valueOf((int)(pag.getPgpVlrpag() * 100)));
            uriBuilder.appendQueryParameter("order_id", pag.getIdOrder());

            // Adiciona informações específicas do pagamento original se necessário
            if (pag.getNsu() != null && !pag.getNsu().isEmpty()) {
                uriBuilder.appendQueryParameter("transaction_id", pag.getNsu());
            }
            if (pag.getAutorizacao() != null && !pag.getAutorizacao().isEmpty()) {
                uriBuilder.appendQueryParameter("authorization_code", pag.getAutorizacao());
            }

            Uri cancelUri = uriBuilder.build();

            Log.d("STONE_CANCEL", "URI de cancelamento: " + cancelUri.toString());

            Intent cancelIntent = new Intent(Intent.ACTION_VIEW);
            cancelIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            cancelIntent.setData(cancelUri);

            try {
                activity.startActivityForResult(cancelIntent, REQUEST_CODE_CANCEL);
                FancyToast.makeText(activity, "Iniciando cancelamento via Stone...",
                        FancyToast.LENGTH_LONG, FancyToast.INFO, true).show();

            } catch (Exception e) {
                Log.e("STONE_CANCEL", "Erro ao abrir Stone App para cancelamento: " + e.getMessage());
                FancyToast.makeText(activity, "Erro ao conectar com Stone para cancelamento: " + e.getMessage(),
                        FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
            }

        } catch (Exception e) {
            Log.e("STONE_CANCEL", "Erro ao iniciar cancelamento: " + e.getMessage(), e);
            FancyToast.makeText(activity, "Erro ao iniciar cancelamento: " + e.getMessage(),
                    FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
        }
    }

    @Override
    public void realizarReimpressao(Activity activity, PagamentoModel pag) {

    }

    @Override
    public void processarResultado(int requestCode, int resultCode, Intent data) {

        Log.d("STONE_PAYMENT", "processarResultado foi chamado, mas a lógica agora está no onActivityResult da Activity.");

    }

    public void setCallback(IPagamentoCallback callback) {
        // Implementação do callback se necessário
    }

    /**
     * Método auxiliar para criar referência do pagamento
     */
    private String criarReferenciaPagamento(PaymentConfig config) {
        return BerpModel.getNmTpvend() + ": " + BerpModel.getNumMesa() + "-" +
                config.getOrderId() + " G: " + BerpModel.getFuncionario();
    }

    /**
     * Método para validar configuração de pagamento
     */
    private boolean validarConfigPagamento(PaymentConfig config) {
        if (config == null) {
            Log.e("STONE_PAYMENT", "PaymentConfig é null");
            return false;
        }

        if (config.getAmountInCents() <= 0) {
            Log.e("STONE_PAYMENT", "Valor inválido: " + config.getAmountInCents());
            return false;
        }

        if (config.getOrderId() == null || config.getOrderId().isEmpty()) {
            Log.e("STONE_PAYMENT", "OrderId é obrigatório");
            return false;
        }

        return true;
    }
}