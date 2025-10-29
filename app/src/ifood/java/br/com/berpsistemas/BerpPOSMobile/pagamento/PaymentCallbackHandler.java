package br.com.berpsistemas.BerpPOSMobile.pagamento;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import br.com.berpsistemas.BerpPOSMobile.model.PagamentoModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class PaymentCallbackHandler implements IPaymentCallbackHandler {
    private static final String TAG = "IFoodPaymentCallback";
    private PaymentListener paymentListener;
    private static PaymentCallbackHandler instance;

    // Constantes para iFood paymentType (da documentação)
    public static final String IFOOD_CREDIT = "CREDIT";
    public static final String IFOOD_DEBIT = "DEBIT";
    public static final String IFOOD_VOUCHER = "VOUCHER";
    public static final String IFOOD_PIX = "PIX";
    public static final String IFOOD_QRCODE = "QRCODE";

    public static synchronized PaymentCallbackHandler getInstance() {
        if (instance == null) {
            instance = new PaymentCallbackHandler();
        }
        return instance;
    }

    private PaymentCallbackHandler() {}

    @Override
    public void setPaymentListener(PaymentListener listener) {
        this.paymentListener = listener;
        Log.d(TAG, "Payment listener (iFood) registrado: " + (listener != null));
    }

    @Override
    public void handleCallback(Context context, Intent intent) {
        if (paymentListener == null) {
            Log.e(TAG, "PaymentListener não está registrado. Impossível processar callback.");
            Toast.makeText(context, "Erro interno: Listener de pagamento não encontrado.", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            Uri data = intent.getData();
            if (data == null) {
                throw new IllegalArgumentException("URI de callback nula.");
            }

            Log.d(TAG, "Callback iFood recebido: " + data.toString());

            // Para o iFood, o parâmetro é "result"
            String resultBase64 = data.getQueryParameter("result");
            if (resultBase64 == null || resultBase64.isEmpty()) {
                throw new IllegalArgumentException("Parâmetro 'result' ausente ou vazio no callback.");
            }

            String decodedResultJson = new String(Base64.decode(resultBase64, Base64.DEFAULT), StandardCharsets.UTF_8);
            Log.d(TAG, "JSON de resultado iFood decodificado: " + decodedResultJson);
            JSONObject jsonResponse = new JSONObject(decodedResultJson);

            // ✅ Campos REAIS da resposta iFood (conforme documentação oficial)
            String status = jsonResponse.optString("status");
            String transactionIdAnotaAi = jsonResponse.optString("transactionIdAnotaAi");
            String tableIdAnotaAi = jsonResponse.optString("tableIdAnotaAi");
            String transactionIdAdyen = jsonResponse.optString("transactionIdAdyen");
            String deviceSerialNumber = jsonResponse.optString("deviceSerialNumber");
            String cardBrand = jsonResponse.optString("cardBrand");
            String errorReason = jsonResponse.optString("errorReason");
            String transactionDate = jsonResponse.optString("transactionDate");
            String transactionTime = jsonResponse.optString("transactionTime");
            String authCode = jsonResponse.optString("authCode");

            // Processa informações de subsídio (se houver)
            int totalDiscountInCents = 0;
            if (jsonResponse.has("subsidyInformation")) {
                JSONArray subsidyArray = jsonResponse.getJSONArray("subsidyInformation");
                for (int i = 0; i < subsidyArray.length(); i++) {
                    JSONObject subsidy = subsidyArray.getJSONObject(i);
                    String subsidyType = subsidy.optString("subsidyType", "");
                    int discountInCents = subsidy.optInt("discountInCents", 0);
                    totalDiscountInCents += discountInCents;
                    Log.d(TAG, "Subsídio " + subsidyType + ": " + discountInCents + " centavos");
                }
            }

            switch (status.toUpperCase()) {
                case "SUCCESS":
                    Log.i(TAG, "✅ Pagamento iFood APROVADO para transactionId: " + transactionIdAnotaAi);
                    Toast.makeText(context, "Pagamento iFood aprovado!", Toast.LENGTH_LONG).show();

                    // ✅ Mapeia os dados do iFood para o listener existente
                    paymentListener.onPaymentSuccess(
                            cardBrand,                    // brandName do iFood
                            authCode,                     // authCode do iFood
                            "",                           // maskedPan (não vem na resposta iFood)
                            transactionIdAdyen,           // NSU (usa transactionIdAdyen)
                            deviceSerialNumber,           // terminalId
                            "IFOOD",                      // adquirente
                            transactionIdAnotaAi,         // idPlataforma
                            transactionIdAnotaAi,         // idPlataformaResumido
                            deviceSerialNumber,           // CodMaqPagamento
                            determinePaymentType(cardBrand), // Tipo baseado na bandeira
                            transactionIdAnotaAi,         // Seu ID de transação
                            transactionIdAdyen,           // externalPaymentId
                            "",                           // cardNumberBin (não disponível)
                            determinePaymentTypeCode(cardBrand), // Código do tipo (DEB/CRE)
                            0.0                           // valor (não vem na resposta)
                    );
                    break;

                case "ERROR":
                    String errorMessage = errorReason != null && !errorReason.isEmpty()
                            ? errorReason
                            : "Erro desconhecido no pagamento iFood.";

                    Log.e(TAG, "❌ Pagamento iFood com ERRO para transactionId: " + transactionIdAnotaAi + " - Mensagem: " + errorMessage);
                    Toast.makeText(context, "Erro no pagamento iFood: " + errorMessage, Toast.LENGTH_LONG).show();
                    paymentListener.onPaymentError("Erro iFood: " + errorMessage + " (ID: " + transactionIdAnotaAi + ")");
                    break;

                case "CANCELED":
                case "CANCELLED":
                    Log.i(TAG, "🚫 Pagamento iFood CANCELADO para transactionId: " + transactionIdAnotaAi);
                    Toast.makeText(context, "Pagamento iFood cancelado.", Toast.LENGTH_LONG).show();
                    paymentListener.onPaymentCancelled();
                    break;

                default:
                    Log.w(TAG, "⚠️ Status de pagamento iFood desconhecido: " + status + " para transactionId: " + transactionIdAnotaAi);
                    Toast.makeText(context, "Resposta de pagamento iFood inesperada.", Toast.LENGTH_LONG).show();
                    paymentListener.onPaymentError("Status iFood desconhecido: " + status);
                    break;
            }

        } catch (IllegalArgumentException | JSONException e) {
            Log.e(TAG, "Erro ao processar callback de pagamento iFood: " + e.getMessage(), e);
            Toast.makeText(context, "Erro ao processar retorno: " + e.getMessage(), Toast.LENGTH_LONG).show();
            if (paymentListener != null) {
                paymentListener.onPaymentError("Erro processamento callback: " + e.getMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro inesperado ao processar callback de pagamento iFood: " + e.getMessage(), e);
            Toast.makeText(context, "Erro inesperado no retorno: " + e.getMessage(), Toast.LENGTH_LONG).show();
            if (paymentListener != null) {
                paymentListener.onPaymentError("Erro inesperado callback: " + e.getMessage());
            }
        }
    }

    /**
     * Determina o tipo de pagamento baseado na bandeira do cartão
     * (já que o iFood não retorna o tipo diretamente na resposta)
     */
    private String determinePaymentType(String cardBrand) {
        if (cardBrand == null || cardBrand.isEmpty()) {
            return "Desconhecido";
        }

        // A partir da bandeira, assumimos que é cartão
        // Para PIX, voucher etc, seria necessário outra lógica
        return "Cartão " + cardBrand;
    }

    /**
     * Determina o código do tipo de pagamento para compatibilidade
     */
    private String determinePaymentTypeCode(String cardBrand) {
        if (cardBrand == null || cardBrand.isEmpty()) {
            return "";
        }

        // Sem mais informações na resposta iFood, assumimos crédito como padrão
        // Em implementações futuras, poderia salvar o tipo enviado e recuperar aqui
        return "CRE"; // Padrão: crédito
    }

    /**
     * Método para notificar sucesso diretamente (usado pela PaymentCallbackActivity alternativa)
     */
    public void notifyPaymentSuccess(Object paymentResult) {
        if (paymentListener != null) {
            // Converte o resultado genérico para os parâmetros esperados
            // Esta é uma implementação simplificada
            paymentListener.onPaymentSuccess("", "", "", "", "", "IFOOD", "", "", "", "", "", "", "", "", 0.0);
        }
    }

    /**
     * Método para notificar erro diretamente (usado pela PaymentCallbackActivity alternativa)
     */
    public void notifyPaymentError(String errorMessage) {
        if (paymentListener != null) {
            paymentListener.onPaymentError(errorMessage);
        }
    }
}