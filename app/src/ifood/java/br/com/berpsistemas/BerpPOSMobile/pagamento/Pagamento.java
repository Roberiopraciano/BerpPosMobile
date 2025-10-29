package br.com.berpsistemas.BerpPOSMobile.pagamento;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import br.com.berpsistemas.BerpPOSMobile.application.MyBerpApplication; // Para obter IPosAuthManager
import br.com.berpsistemas.BerpPOSMobile.managers.IPosAuthManager;    // Para autorização de estorno
import br.com.berpsistemas.BerpPOSMobile.model.PagamentoModel;
import com.shashank.sony.fancytoastlib.FancyToast;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class Pagamento implements IPagamento {
    private static final String TAG = "IFoodPagamentoImpl";
    private static final int REQUEST_CODE_IFOOD_PAYMENT_INITIATED = 4001;
    private static final int REQUEST_CODE_IFOOD_REFUND_AUTH_INITIATED = 4002;

    private static final String IFOOD_CALLBACK_URL = "berppos://payment-result?result=";



    private Activity activityContext;
    private IPagamentoCallback iPagamentoCallback; // O callback que você definiu na interface
    private IPosAuthManager posAuthManager; // Para o fluxo de autorização de estorno

    // Deeplinks iFood
    private static final String IFOOD_PAYMENT_DEEPLINK_BASE = "https://portal.ifood.com.br/make-payment";
    private static final String IFOOD_REFUND_AUTH_DEEPLINK_BASE = "https://portal.ifood.com.br/refund-authorization";

    // Para callback de estorno (deve ser único e tratado pela sua Activity de callback)
    private static final String URL_TO_RETURN_FOR_IFOOD_REFUND_AUTH = "seuapp://ifood-refund-auth-result?result=";


    public Pagamento(Activity activity) {
        this.activityContext = activity;
        // Obter o IPosAuthManager da Application Class
        if (activity.getApplication() instanceof MyBerpApplication) {
            this.posAuthManager = ((MyBerpApplication) activity.getApplication()).getPosAuthManager();
        }
    }

    @Override
    public void iniciarPagamentoDeeplink(Activity activity, PaymentConfig config) {
        this.activityContext = activity;
        try {
            if (config == null || config.getOrderId() == null || config.getTransactionType() == null ) {
                handleError("Configuração de pagamento inválida.");
                return;
            }
            if (config.getAmountInCents() <= 0) {
                handleError("Valor do pagamento inválido.");
                return;
            }

            JSONObject jsonContent = new JSONObject();
            jsonContent.put("transactionId", config.getOrderId()); // Usando orderId como transactionId
            jsonContent.put("paymentMethod", config.getTransactionType().toUpperCase());
            jsonContent.put("value", config.getAmountInCents());
            jsonContent.put("printReceipt", true);


            jsonContent.put("sendResultInSameIntent", false); // ✅ false + nome correto
            jsonContent.put("tableId","1");
            jsonContent.put("urlToReturn", IFOOD_CALLBACK_URL); // Ex: "seuapp://payment-result?result="
            jsonContent.put("editableValue", config.isEditableValue());
            if ("CREDIT".equals(config.getTransactionType().toUpperCase()) && config.getInstallments() > 0) {
                jsonContent.put("installments", config.getInstallments());
            }

            String jsonString = jsonContent.toString();
            Log.d(TAG, "JSON para pagamento iFood: " + jsonString);

            String base64Content = Base64.encodeToString(jsonString.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
            String paymentUriString = IFOOD_PAYMENT_DEEPLINK_BASE + "?content=" + base64Content;

            Log.d("paymentIntent",paymentUriString);

            Intent paymentIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUriString));
            activity.startActivityForResult(paymentIntent, REQUEST_CODE_IFOOD_PAYMENT_INITIATED);
            // FancyToast.makeText(activity, "Iniciando pagamento iFood...", FancyToast.LENGTH_SHORT, FancyToast.INFO, true).show();

        } catch (android.content.ActivityNotFoundException anfe) {
            handleError("App iFood POS não encontrado!", anfe);
        } catch (Exception e) {
            handleError("Erro ao iniciar pagamento iFood: " + e.getMessage(), e);
        }
    }

    @Override
    public void iniciarPagamentoProvider(Activity activity, PaymentConfig config) {
        // Para iFood, é o mesmo que o deeplink
        iniciarPagamentoDeeplink(activity, config);
    }

    @Override
    public void processarResultado(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "processarResultado chamado. RequestCode: " + requestCode + ", ResultCode: " + resultCode);
        // Este método, para o iFood, não receberá o resultado final do pagamento.
        // Ele apenas confirma que o startActivityForResult para o deeplink foi chamado.
        // O resultado real vem via deeplink para a PaymentResultActivity.
        // Você pode querer logar ou fazer algo se requestCode == REQUEST_CODE_IFOOD_PAYMENT_INITIATED,
        // mas não haverá dados úteis de pagamento em 'data' ou 'resultCode'.

        if (requestCode == REQUEST_CODE_IFOOD_PAYMENT_INITIATED) {
            Log.d(TAG, "Intent de pagamento iFood foi disparado.");
            // Não há resultado direto aqui.
        } else if (requestCode == REQUEST_CODE_IFOOD_REFUND_AUTH_INITIATED) {
            Log.d(TAG, "Intent de autorização de estorno iFood foi disparado.");
            // O resultado virá para a URL_TO_RETURN_FOR_IFOOD_REFUND_AUTH
            // e deverá ser tratado por uma Activity de callback que então
            // chamaria o IPosAuthManager ou uma lógica similar para processar o token de estorno.
        }
    }

    @Override
    public void setCallback(IPagamentoCallback callback) {
        this.iPagamentoCallback = callback;
        // Se você está usando o PaymentCallbackHandler como um singleton para notificar
        // um PaymentListener global, este iPagamentoCallback pode ser redundante
        // ou usado para um feedback mais direto/local.
        // Por agora, vamos assumir que o PaymentCallbackHandler + PaymentListener é o principal.
        // Se iPagamentoCallback for diferente de PaymentListener, você precisará de uma ponte.
    }

    @Override
    public void realizarReembolso(Activity activity, PagamentoModel pag) {
        this.activityContext = activity;
        Log.d(TAG, "Iniciando processo de estorno iFood para transactionId: " + pag.getIdOrder());

        if (posAuthManager == null) {
            handleError("Serviço de autenticação POS não disponível para estorno.");
            return;
        }
        if (pag == null || pag.getIdOrder() == null || pag.getIdOrder().isEmpty() || pag.getPgpVlrpag() <= 0) {
            handleError("Dados inválidos para estorno.");
            return;
        }

        String originalTransactionId = pag.getIdOrder();
        int amountInCentsToRefund = (int) (pag.getPgpVlrpag() * 100);

        // O IPosAuthManager (implementação iFood) deve ter um método para
        // iniciar a autorização de estorno e depois chamar a API de estorno.
        // Este é um exemplo de como poderia ser.
        // A lógica real de chamar o deeplink de autorização de estorno e a API
        // estaria dentro do IPosAuthManager.
        posAuthManager.requestRefund(activity, originalTransactionId, amountInCentsToRefund, new IPosAuthManager.RefundCallback() {
            @Override
            public void onRefundSuccess(String message) {
                Log.i(TAG, "Estorno iFood bem-sucedido: " + message);
                FancyToast.makeText(activityContext, "Estorno realizado com sucesso: " + message, FancyToast.LENGTH_LONG, FancyToast.SUCCESS, true).show();
                if (iPagamentoCallback != null) {
                    // Você precisará definir como o IPagamentoCallback notifica o sucesso do estorno
                    // Ex: iPagamentoCallback.onRefundSuccess(pag); // Passando o PagamentoModel original
                }
            }

            @Override
            public void onRefundError(String errorMessage) {
                Log.e(TAG, "Erro no estorno iFood: " + errorMessage);
                FancyToast.makeText(activityContext, "Erro no estorno: " + errorMessage, FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
                if (iPagamentoCallback != null) {
                    // Ex: iPagamentoCallback.onRefundError(errorMessage);
                }
            }

            @Override
            public void onRefundAuthRequired(String authUrl) {
                // Este callback seria se o IPosAuthManager só lidasse com o deeplink de auth
                // e depois precisasse que esta classe chamasse a API.
                // Mas é melhor que o IPosAuthManager lide com todo o fluxo de estorno.
                Log.i(TAG, "Autorização de estorno iFood requerida. URL: " + authUrl);
                FancyToast.makeText(activityContext, "Autorização de estorno pendente.", FancyToast.LENGTH_LONG, FancyToast.INFO, true).show();
            }
        });
    }

    @Override
    public void realizarReimpressao(Activity activity, PagamentoModel pag) {

    }

    private void handleError(String message) {
        handleError(message, null);
    }

    private void handleError(String message, Throwable throwable) {
        Log.e(TAG, message, throwable);
        FancyToast.makeText(activityContext, message, FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
        if (iPagamentoCallback != null) {
            // Supondo que seu IPagamentoCallback tenha um método onError
            // iPagamentoCallback.onError(message);
        }
    }
}