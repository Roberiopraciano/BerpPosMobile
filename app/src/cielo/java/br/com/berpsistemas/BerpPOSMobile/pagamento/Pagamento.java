package br.com.berpsistemas.BerpPOSMobile.pagamento;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import br.com.berpsistemas.BerpPOSMobile.model.BerpModel;
import br.com.berpsistemas.BerpPOSMobile.model.PagamentoModel;
import com.shashank.sony.fancytoastlib.FancyToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class Pagamento implements IPagamento {
    private static final int REQUEST_CODE_PAYMENT = 3001;
    private static final int REQUEST_CODE_CANCEL = 3002;
    private Activity context;


    // Constantes para tipos de pagamento da Cielo LIO
    public static final String DEBITO_AVISTA = "DEBITO_AVISTA";
    public static final String CREDITO_AVISTA = "CREDITO_AVISTA";
    public static final String CREDITO_PARCELADO_LOJA = "CREDITO_PARCELADO_LOJA";
    public static final String PIX = "PIX";

    public Pagamento(Activity context) {
        this.context = context;
    }


    @Override
    public void iniciarPagamentoDeeplink(Activity activity, PaymentConfig config) {
        try {
            // Converte o tipo vindo do config para a constante da Cielo
            int installments = 0; // ou outro valor se quiser customizar depois
            String paymentCode = mapearTipoTransacaoParaPaymentCode(config.getTransactionType(), installments);

            // Cria o JSON para o pagamento, passando o paymentCode já resolvido
            JSONObject jsonPayment = criarJsonPagamento(config, paymentCode, installments);
            String jsonString = jsonPayment.toString();
            Log.d("PAGAMENTO_JSON", jsonString);

            // Converte para base64
            String base64 = Base64.encodeToString(jsonString.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);

            // Constrói a URI do deeplink conforme documentação da Cielo LIO
            String checkoutUri = "lio://payment?request=" + base64 + "&urlCallback=order://response";

            // Cria o intent para abrir o aplicativo da Cielo LIO
            Intent paymentIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUri));
            paymentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Verifica se existe algum app que possa lidar com esse deeplink
            if (paymentIntent.resolveActivity(activity.getPackageManager()) != null) {
                activity.startActivityForResult(paymentIntent, REQUEST_CODE_PAYMENT);
                FancyToast.makeText(activity, "Iniciando pagamento via Cielo LIO...", FancyToast.LENGTH_LONG,FancyToast.INFO,true).show();
            } else {
                FancyToast.makeText(activity, "Aplicativo Cielo LIO não encontrado!", FancyToast.LENGTH_LONG,FancyToast.INFO,true).show();
            }
        } catch (Exception e) {
            Log.e("PAGAMENTO_POS", "Erro ao iniciar pagamento: " + e.getMessage(), e);
            FancyToast.makeText(activity, "Erro ao iniciar pagamento: " + e.getMessage(), FancyToast.LENGTH_LONG,FancyToast.INFO,true).show();
        }
    }

    private JSONObject criarJsonPagamento(PaymentConfig config, String paymentCode, int installments) throws JSONException {
        JSONObject jsonPayment = new JSONObject();

        PaymentAuthHelper authHelper = new PaymentAuthHelper(context, "cielo");

        String accessToken = authHelper.getAccessToken("LFGB0gaIWjO11x4KkmBnFUNXftIiy0XCTuVwrpM2xxzEUxY2BW");
        String clientId = authHelper.getClientId("cwlMVOA76oXoneZECkuNSNI0pE63uEuVRbRkhK57cbtMXgwWrv");

        jsonPayment.put("accessToken", accessToken);
        jsonPayment.put("clientID", clientId);

        String merchantCode = authHelper.getMerchantCode();
        if (merchantCode != null && !merchantCode.isEmpty()) {
            jsonPayment.put("merchantCode", merchantCode);
        }

        jsonPayment.put("reference", BerpModel.getNmTpvend()+ ": "+BerpModel.getNumMesa()+"-"+config.getOrderId()+" G: "+BerpModel.getFuncionario());
        jsonPayment.put("installments", installments);

        JSONArray items = new JSONArray();
        JSONObject item = new JSONObject();
        item.put("name", "Consumo: "+config.getOrderId());
        item.put("quantity", 1);
        item.put("sku", "1");
        item.put("unitOfMeasure", "unidade");
        item.put("unitPrice", config.getAmountInCents());
        items.put(item);
        jsonPayment.put("items", items);

        jsonPayment.put("paymentCode", paymentCode);
        jsonPayment.put("value", String.valueOf(config.getAmountInCents()));

        return jsonPayment;
    }

    private JSONObject criarJsonCancelamento(PagamentoModel pag) throws JSONException {
        JSONObject jsonCancel = new JSONObject();

        PaymentAuthHelper authHelper = new PaymentAuthHelper(context, "cielo");

        String accessToken = authHelper.getAccessToken("LFGB0gaIWjO11x4KkmBnFUNXftIiy0XCTuVwrpM2xxzEUxY2BW");
        String clientId = authHelper.getClientId("cwlMVOA76oXoneZECkuNSNI0pE63uEuVRbRkhK57cbtMXgwWrv");

        jsonCancel.put("id", pag.getIdOrder()); // id do pedido
        jsonCancel.put("clientID", clientId);
        jsonCancel.put("accessToken", accessToken);
        jsonCancel.put("cieloCode", pag.getNsu()); // Código da Cielo (normalmente NSU)
        jsonCancel.put("authCode", pag.getAutorizacao()); // Código de autorização da operadora
        jsonCancel.put("value", (int) (pag.getPgpVlrpag() * 100)); // valor em centavos

        return jsonCancel;
    }

    private String mapearTipoTransacaoParaPaymentCode(String transactionType, int installments) {
        if (transactionType == null || transactionType.isEmpty()) {
            return CREDITO_AVISTA;
        }

        switch (transactionType.toLowerCase()) {
            case "debit":
            case "debito":
                return DEBITO_AVISTA;

            case "credit":
            case "credito":
                if (installments > 1) {
                    return CREDITO_PARCELADO_LOJA;
                } else {
                    return CREDITO_AVISTA;
                }

            case "pix":
                return PIX;

            default:
                return CREDITO_AVISTA;
        }
    }

    @Override
    public void iniciarPagamentoProvider(Activity activity, PaymentConfig config) {
        // Utiliza o mesmo método para o provider
        iniciarPagamentoDeeplink(activity, config);
    }

    @Override
    public void processarResultado(int requestCode, int resultCode, Intent data) {

    }


    public void setCallback(IPagamentoCallback callback) {

    }

    @Override
    public void realizarReembolso(Activity activity, PagamentoModel pag) {
        try {
            // Converte o tipo vindo do config para a constante da Cielo
            int installments = 0; // ou outro valor se quiser customizar depois
            // Cria o JSON para o pagamento, passando o paymentCode já resolvido
            JSONObject jsonPayment = criarJsonCancelamento(pag);
            String jsonString = jsonPayment.toString();
            Log.d("Cancelamento_json", jsonString);

            // Converte para base64
            String base64 = Base64.encodeToString(jsonString.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);

            // Constrói a URI do deeplink conforme documentação da Cielo LIO
            String checkoutUri = "lio://payment-reversal?request=" + base64 + "&urlCallback=order://response";

            // Cria o intent para abrir o aplicativo da Cielo LIO
            Intent paymentIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUri));
            paymentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Verifica se existe algum app que possa lidar com esse deeplink
            if (paymentIntent.resolveActivity(activity.getPackageManager()) != null) {
                activity.startActivityForResult(paymentIntent, REQUEST_CODE_CANCEL);
                FancyToast.makeText(activity, "Iniciando pagamento via Cielo LIO...", FancyToast.LENGTH_LONG,FancyToast.INFO,true).show();
            } else {
                FancyToast.makeText(activity, "Aplicativo Cielo LIO não encontrado!", FancyToast.LENGTH_LONG,FancyToast.WARNING,true).show();
            }
        } catch (Exception e) {
            Log.e("PAGAMENTO_POS", "Erro ao iniciar pagamento: " + e.getMessage(), e);
            FancyToast.makeText(activity, "Erro ao iniciar pagamento: " + e.getMessage(),FancyToast.LENGTH_LONG,FancyToast.ERROR,true).show();
        }

    }




}