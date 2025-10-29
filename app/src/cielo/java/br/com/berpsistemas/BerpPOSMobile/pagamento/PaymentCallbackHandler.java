package br.com.berpsistemas.BerpPOSMobile.pagamento;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import br.com.berpsistemas.BerpPOSMobile.model.PagamentoModel;
import br.com.berpsistemas.BerpPOSMobile.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class PaymentCallbackHandler implements IPaymentCallbackHandler {
    private static final String TAG = "PaymentCallbackHandler";
    private PaymentListener paymentListener;
    private static PaymentCallbackHandler instance;

    // Singleton
    public static synchronized PaymentCallbackHandler getInstance() {
        if (instance == null) {
            instance = new PaymentCallbackHandler();
        }
        return instance;
    }

    // Construtor privado para o Singleton
    private PaymentCallbackHandler() {}

    @Override
    public void setPaymentListener(PaymentListener listener) {
        this.paymentListener = listener;
        Log.d(TAG, "Payment listener registrado: " + (listener != null));
    }

    @Override
    public void handleCallback(Context context, Intent intent) {
        try {
            Uri uri = intent.getData();
            if (uri == null) throw new Exception("URI inválida");

            String responseBase64 = uri.getQueryParameter("response");
            if (responseBase64 == null) throw new Exception("Parâmetro 'response' ausente");

            String responseJson = new String(Base64.decode(responseBase64, Base64.DEFAULT), StandardCharsets.UTF_8);
            JSONObject jsonResponse = new JSONObject(responseJson);

            // Verifica erro comum
            if (jsonResponse.has("code") && jsonResponse.has("reason")) {
                String reason = jsonResponse.getString("reason");
                notifyError(context, reason);
                return;
            }

            // Verifica se é callback de reembolso
            if (uri.toString().contains("payment-reversal")) {
                handleRefundCallback(jsonResponse);
                return;
            }

            // Verifica se é resposta de pagamento
            if (!jsonResponse.has("payments")) {
                notifyError(context, "Resposta sem pagamentos");
                return;
            }

            handlePaymentCallback(context, jsonResponse);

        } catch (Exception e) {
            Log.e(TAG, "Erro no callback: " + e.getMessage(), e);
            Toast.makeText(context, "Erro no callback: " + e.getMessage(), Toast.LENGTH_LONG).show();
            if (paymentListener != null) {
                paymentListener.onPaymentError("Erro no processamento: " + e.getMessage());
            }
        }
    }

    private void handleRefundCallback(JSONObject jsonResponse) {
        try {
            PagamentoModel pagCancelado = new PagamentoModel();
            pagCancelado.setCvNumber(jsonResponse.optString("cieloCode"));
            pagCancelado.setAutorizacao(jsonResponse.optString("authCode"));

            if (paymentListener != null) {
                paymentListener.onRefundSuccess(pagCancelado);
            } else {
                Log.e(TAG, "Listener não registrado para processar reembolso");
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar reembolso: " + e.getMessage(), e);
        }
    }

    private void handlePaymentCallback(Context context, JSONObject jsonResponse) throws JSONException {
        // 1) Pega o primeiro pagamento
        JSONArray payments = jsonResponse.optJSONArray("payments");
        if (payments == null || payments.length() == 0) {
            notifyError(context, "Nenhum pagamento encontrado");
            return;
        }
        JSONObject payment = ((org.json.JSONArray) payments).getJSONObject(0);

        // 2) Pega o objeto paymentFields e valida
        JSONObject fields = payment.optJSONObject("paymentFields");
        if (fields == null) {
            notifyError(context, "paymentFields ausente na resposta");
            return;
        }

        // 3) Extrai variáveis principais
        String statusCode    = fields.optString("statusCode", "");
        String brand         = payment.optString("brand", "");
        String authCode      = payment.optString("authCode", "");
        String mask          = payment.optString("mask", "");
        String cieloCode     = payment.optString("cieloCode", "");
        String terminal      = payment.optString("terminal", "");
        String paymentId     = jsonResponse.optString("id", "");
        String transactionId = fields.optString("paymentTransactionId", "");
        String flavor        = BuildConfig.FLAVOR;
        String binCartao     = fields.optString("bin", "");

        // 4) Determina débito/crédito/PIX/voucher a partir do productName
        String productName = fields.optString("productName", "");
        String debCre;
        if      (productName.contains("PIX"))     debCre = "PIX";
        else if (productName.contains("CREDITO")) debCre = "CRE";
        else if (productName.contains("DEBITO"))  debCre = "DEB";
        else if (productName.contains("VOUCHER")) debCre = "DEB";
        else                                     debCre = "";

        // 5) Valor em reais
        double amount = payment.optDouble("amount", 0) / 100.0;

        Log.d(TAG, String.format(
                "Pagamento recebido: status=%s, brand=%s, authCode=%s, valor=%.2f",
                statusCode, brand, authCode, amount
        ));

        // 6) Branch por statusCode
        switch (statusCode) {
            case "0":
            case "1":
                Toast.makeText(context, "Pagamento aprovado!", Toast.LENGTH_LONG).show();
                if (paymentListener != null) {
                    paymentListener.onPaymentSuccess(
                            brand,
                            authCode,
                            mask,
                            cieloCode,
                            terminal,
                            /*adquirente=*/ flavor,
                            /*idPlataforma*/   "",
                            /*idPlataformaResumido*/ "",
                            /*CodMaqPagamento=*/    "",
                            flavor,
                            transactionId,
                            paymentId,
                            binCartao,
                            debCre,
                            amount
                    );
                } else {
                    Log.w(TAG, "paymentListener não registrado para sucesso");
                }
                break;

            case "2":
                Toast.makeText(context, "Pagamento cancelado", Toast.LENGTH_LONG).show();
                if (paymentListener != null) {
                    paymentListener.onPaymentCancelled();
                } else {
                    Log.w(TAG, "paymentListener não registrado para cancelamento");
                }
                break;

            default:
                notifyError(context, "Status desconhecido: " + statusCode);
                break;
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

}