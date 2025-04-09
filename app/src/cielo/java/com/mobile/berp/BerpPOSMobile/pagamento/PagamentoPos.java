package com.mobile.berp.BerpPOSMobile.pagamento;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class PagamentoPos implements IPagamento {
    private static final int REQUEST_CODE_PAYMENT = 3001;
    private Activity context;

    // Constantes para tipos de pagamento da Cielo LIO
    public static final String DEBITO_AVISTA = "DEBITO_AVISTA";
    public static final String CREDITO_AVISTA = "CREDITO_AVISTA";
    public static final String CREDITO_PARCELADO_LOJA = "CREDITO_PARCELADO_LOJA";
    public static final String PIX = "PIX";

    public PagamentoPos(Activity context) {
        this.context = context;
    }


    @Override
    public void iniciarPagamentoDeeplink(Activity activity, PaymentConfig config) {
        try {
            // Cria o JSON para o pagamento
            JSONObject jsonPayment = criarJsonPagamento(config);
            String jsonString = jsonPayment.toString();
            Log.d("PAGAMENTO_JSON", jsonString);

            // Converte para base64
            String base64 = Base64.encodeToString(jsonString.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);

            // Constrói a URI do deeplink conforme documentação da Cielo LIO
            // Constrói a URI do deeplink conforme documentação da Cielo LIO
            String checkoutUri = "lio://payment?request=" + base64 + "&urlCallback=order://response";

            // Cria o intent para abrir o aplicativo da Cielo LIO
            Intent paymentIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUri));
            paymentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Verifica se existe algum app que possa lidar com esse deeplink
            if (paymentIntent.resolveActivity(activity.getPackageManager()) != null) {
                activity.startActivityForResult(paymentIntent, REQUEST_CODE_PAYMENT);
                Toast.makeText(activity, "Iniciando pagamento via Cielo LIO...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "Aplicativo Cielo LIO não encontrado!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e("PAGAMENTO_POS", "Erro ao iniciar pagamento: " + e.getMessage(), e);
            Toast.makeText(activity, "Erro ao iniciar pagamento: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Cria o objeto JSON para o pagamento conforme as especificações da Cielo LIO
     */
    private JSONObject criarJsonPagamento(PaymentConfig config) throws JSONException {
        JSONObject jsonPayment = new JSONObject();

        // Obtém Access Token e ClientID das preferências salvas
        PaymentAuthHelper authHelper = new PaymentAuthHelper(context, "cielo");

        // Usar valores padrão para desenvolvimento caso não estejam configurados
       // String accessToken = //authHelper.getAccessToken("LFGB0gaIWjO11x4KkmBnFUNXftIiy0XCTuVwrpM2xxzEUxY2BW");
        //String clientId = //authHelper.getClientId("cwlMVOA76oXoneZECkuNSNI0pE63uEuVRbRkhK57cbtMXgwWrv");

                String accessToken = "LFGB0gaIWjO11x4KkmBnFUNXftIiy0XCTuVwrpM2xxzEUxY2BW";
                String clientId = "cwlMVOA76oXoneZECkuNSNI0pE63uEuVRbRkhK57cbtMXgwWrv";

        jsonPayment.put("accessToken", accessToken);
        jsonPayment.put("clientID", clientId);

        // Adiciona merchant code se existir (para multi-EC)
        String merchantCode = authHelper.getMerchantCode();
        if (merchantCode != null && !merchantCode.isEmpty()) {
            jsonPayment.put("merchantCode", merchantCode);
        }

        // Referência do pedido
        jsonPayment.put("reference", config.getOrderId());

        // Email do cliente (opcional)
        // jsonPayment.put("email", "cliente@exemplo.com");

        // Parcelas (0 para à vista)
        int installments = 0; // Valor padrão para pagamento à vista
        jsonPayment.put("installments", installments);

        // Itens do pedido
        JSONArray items = new JSONArray();
        JSONObject item = new JSONObject();
        item.put("name", "Pedido #" + config.getOrderId());
        item.put("quantity", 1);
        item.put("sku", "1");
        item.put("unitOfMeasure", "unidade");
        item.put("unitPrice", config.getAmount() ); // Converte centavos para reais
        items.put(item);
        jsonPayment.put("items", items);

        // Código de pagamento baseado no tipo de transação
        String paymentCode = mapearTipoTransacaoParaPaymentCode(config.getTransactionType(), installments);
        jsonPayment.put("paymentCode", paymentCode);

        // Valor total (deve ser string conforme exemplo)
        jsonPayment.put("value", String.valueOf(config.getAmount() )); // Converte centavos para reais

        return jsonPayment;
    }

    /**
     * Mapeia o tipo de transação para o paymentCode aceito pela Cielo LIO
     */
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
        if (requestCode == REQUEST_CODE_PAYMENT) {
            try {
                if (data != null && Intent.ACTION_VIEW.equals(data.getAction())) {
                    Uri uri = data.getData();

                    if (uri != null) {
                        // Obtém o parâmetro de resposta (codificado em base64)
                        String responseBase64 = uri.getQueryParameter("response");

                        if (responseBase64 != null) {
                            // Decodifica o base64 para obter o JSON
                            byte[] responseBytes = Base64.decode(responseBase64, Base64.DEFAULT);
                            String responseJson = new String(responseBytes, StandardCharsets.UTF_8);

                            Log.d("PAGAMENTO_POS", "Resposta decodificada: " + responseJson);

                            // Analisa o JSON para determinar sucesso ou falha
                            JSONObject jsonResponse = new JSONObject(responseJson);

                            // Verifica se é uma resposta de erro (contém "code" e "reason")
                            if (jsonResponse.has("code") && jsonResponse.has("reason")) {
                                // É uma mensagem de erro
                                int errorCode = jsonResponse.getInt("code");
                                String reason = jsonResponse.getString("reason");

                                Log.e("PAGAMENTO_POS", "Erro no pagamento: " + reason + " (código: " + errorCode + ")");
                            } else {
                                // É uma resposta de sucesso (contém "status", "payments", etc.)
                                // Verifica o statusCode dentro do array payments
                                if (jsonResponse.has("payments") && jsonResponse.getJSONArray("payments").length() > 0) {
                                    JSONObject payment = jsonResponse.getJSONArray("payments").getJSONObject(0);

                                    String brand = payment.optString("brand", "");
                                    String authCode = payment.optString("authCode", "");
                                    String mask = payment.optString("mask", "");

                                    if (payment.has("paymentFields")) {
                                        JSONObject paymentFields = payment.getJSONObject("paymentFields");
                                        String statusCode = paymentFields.optString("statusCode", "");

                                        if ("0".equals(statusCode) || "1".equals(statusCode)) {
                                            // Pagamento bem-sucedido (statusCode 0 para PIX, 1 para outros)
                                            Log.d("PAGAMENTO_POS", "Pagamento realizado com sucesso!" +
                                                    "\nBandeira: " + brand +
                                                    "\nCódigo de autorização: " + authCode +
                                                    "\nCartão: " + mask);
                                        } else if ("2".equals(statusCode)) {
                                            // Cancelamento
                                            Log.d("PAGAMENTO_POS", "Pagamento cancelado");
                                        } else {
                                            // Outro status
                                            Log.d("PAGAMENTO_POS", "Status de pagamento desconhecido: " + statusCode);
                                        }
                                    } else {
                                        Log.d("PAGAMENTO_POS", "Campo paymentFields não encontrado na resposta");
                                    }
                                } else {
                                    Log.d("PAGAMENTO_POS", "Array payments não encontrado na resposta");
                                }
                            }
                        } else {
                            Log.e("PAGAMENTO_POS", "Parâmetro 'response' não encontrado na URI");
                        }
                    } else {
                        Log.e("PAGAMENTO_POS", "URI nula na resposta");
                    }
                } else {
                    if (resultCode == Activity.RESULT_CANCELED) {
                        Log.d("PAGAMENTO_POS", "Pagamento cancelado pelo usuário");
                    } else {
                        Log.e("PAGAMENTO_POS", "Intent inválida na resposta");
                    }
                }
            } catch (Exception e) {
                Log.e("PAGAMENTO_POS", "Erro ao processar resultado: " + e.getMessage(), e);
            }
        }
    }
}