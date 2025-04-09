package com.mobile.berp.BerpPOSMobile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

/**
 * Activity responsável por receber callbacks de pagamento via deeplink
 */
public class PaymentCallbackActivity extends AppCompatActivity {

    private static final String TAG = "PaymentCallback";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // Processa o intent de deeplink
            Intent intent = getIntent();
            if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
                processPaymentCallback(intent);
            } else {
                // Se não for um deeplink válido, apenas finaliza a activity
                Log.e(TAG, "Intent inválido ou não é ACTION_VIEW");
                Toast.makeText(this, "Resposta de pagamento inválida", Toast.LENGTH_SHORT).show();
                navigateToMainScreen();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar callback: " + e.getMessage(), e);
            Toast.makeText(this, "Erro ao processar pagamento: " + e.getMessage(), Toast.LENGTH_LONG).show();
            navigateToMainScreen();
        }
    }

    /**
     * Processa o callback de pagamento
     */
    private void processPaymentCallback(Intent intent) {
        try {
            Uri uri = intent.getData();
            if (uri != null) {
                // Obtém o parâmetro de resposta (codificado em base64)
                String responseBase64 = uri.getQueryParameter("response");

                if (responseBase64 != null) {
                    // Decodifica o base64 para obter o JSON
                    byte[] responseBytes = Base64.decode(responseBase64, Base64.DEFAULT);
                    String responseJson = new String(responseBytes, StandardCharsets.UTF_8);

                    Log.d(TAG, "Resposta decodificada: " + responseJson);

                    // Analisa o JSON para determinar sucesso ou falha
                    JSONObject jsonResponse = new JSONObject(responseJson);

                    boolean isSuccess = false;
                    String message = "";
                    String responseCode = uri.getQueryParameter("responsecode");

                    // Verifica se é uma resposta de erro (contém "code" e "reason")
                    if (jsonResponse.has("code") && jsonResponse.has("reason")) {
                        // É uma mensagem de erro
                        int errorCode = jsonResponse.getInt("code");
                        String reason = jsonResponse.getString("reason");

                        message = "Erro no pagamento: " + reason;
                        isSuccess = false;
                    } else {
                        // É uma resposta de sucesso (contém "status", "payments", etc.)
                        // Verifica o statusCode dentro do array payments
                        if (jsonResponse.has("payments") && jsonResponse.getJSONArray("payments").length() > 0) {
                            JSONObject payment = jsonResponse.getJSONArray("payments").getJSONObject(0);

                            String brand = payment.optString("brand", "");
                            String authCode = payment.optString("authCode", "");
                            String externalId = payment.optString("externalId", "");

                            if (payment.has("paymentFields")) {
                                JSONObject paymentFields = payment.getJSONObject("paymentFields");
                                String statusCode = paymentFields.optString("statusCode", "");

                                if ("0".equals(statusCode) || "1".equals(statusCode)) {
                                    // Pagamento bem-sucedido (statusCode 0 para PIX, 1 para outros)
                                    message = "Pagamento realizado com sucesso!";
                                    if (!authCode.isEmpty()) {
                                        message += "\nCódigo: " + authCode;
                                    }
                                    if (!brand.isEmpty()) {
                                        message += "\nBandeira: " + brand;
                                    }

                                    isSuccess = true;

                                    // Salva informações relevantes do pagamento em SharedPreferences
                                    savePaymentInfo(externalId, authCode, brand, payment.optString("mask", ""));
                                } else if ("2".equals(statusCode)) {
                                    // Cancelamento
                                    message = "Pagamento cancelado";
                                    isSuccess = false;
                                } else {
                                    // Outro status
                                    message = "Status de pagamento desconhecido: " + statusCode;
                                    isSuccess = false;
                                }
                            } else {
                                message = "Formato de resposta inválido (sem paymentFields)";
                                isSuccess = false;
                            }
                        } else {
                            message = "Formato de resposta inválido (sem payments)";
                            isSuccess = false;
                        }
                    }

                    // Exibe mensagem para o usuário
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                    // Se o pagamento foi bem-sucedido, vai para a impressão
                    if (isSuccess) {
                        navigateToPrint();
                    } else {
                        navigateToMainScreen();
                    }
                } else {
                    Log.e(TAG, "Parâmetro 'response' não encontrado na URI");
                    Toast.makeText(this, "Resposta de pagamento inválida", Toast.LENGTH_SHORT).show();
                    navigateToMainScreen();
                }
            } else {
                Log.e(TAG, "URI nula na resposta");
                Toast.makeText(this, "Resposta de pagamento inválida", Toast.LENGTH_SHORT).show();
                navigateToMainScreen();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar callback: " + e.getMessage(), e);
            Toast.makeText(this, "Erro ao processar pagamento: " + e.getMessage(), Toast.LENGTH_LONG).show();
            navigateToMainScreen();
        } finally {
            // Finaliza esta activity após o processamento
            finish();
        }
    }

    /**
     * Salva informações relevantes do pagamento para uso posterior
     */
    private void savePaymentInfo(String externalId, String authCode, String brand, String cardMask) {
        try {
            getSharedPreferences("payment_info", MODE_PRIVATE)
                    .edit()
                    .putString("transaction_id", externalId)
                    .putString("auth_code", authCode)
                    .putString("brand", brand)
                    .putString("card_mask", cardMask)
                    .putLong("timestamp", System.currentTimeMillis())
                    .apply();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao salvar informações do pagamento: " + e.getMessage(), e);
        }
    }

    /**
     * Navega para a tela de impressão
     */
    private void navigateToPrint() {
        try {
            // Prepara o cupom para impressão
            String base64Cupom = prepareCupomForPrinting();

            // Cria a URI para impressão
            String printUri = "lio://print?request=" + base64Cupom + "&urlCallback=order://response";

            // Inicia o intent de impressão
            Intent printIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(printUri));
            startActivity(printIntent);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar impressão: " + e.getMessage(), e);
            Toast.makeText(this, "Erro ao iniciar impressão: " + e.getMessage(), Toast.LENGTH_LONG).show();
            navigateToMainScreen();
        }
    }

    /**
     * Prepara o cupom para impressão
     */
    private String prepareCupomForPrinting() {
        try {
            // Aqui você pode usar o código existente de gerarJsonCupomComColunasPeso()
            // Ou implementar uma nova versão específica para resultados de pagamento

            // Como exemplo, vamos usar uma versão simplificada
            JSONObject jsonPrint = new JSONObject();
            jsonPrint.put("operation", "PRINT_MULTI_COLUMN_TEXT");

            // Estilos (colunas)
            JSONArray styles = new JSONArray();

            JSONObject estiloColuna1 = new JSONObject();
            estiloColuna1.put("key_attributes_align", 0); // Esquerda
            estiloColuna1.put("key_attributes_textsize", 22);
            estiloColuna1.put("key_attributes_typeface", 0);
            estiloColuna1.put("key_attributes_weight", 4); // 40% da largura

            JSONObject estiloColuna2 = new JSONObject();
            estiloColuna2.put("key_attributes_align", 1); // Centro
            estiloColuna2.put("key_attributes_textsize", 22);
            estiloColuna2.put("key_attributes_typeface", 0);
            estiloColuna2.put("key_attributes_weight", 3); // 30%

            JSONObject estiloColuna3 = new JSONObject();
            estiloColuna3.put("key_attributes_align", 2); // Direita
            estiloColuna3.put("key_attributes_textsize", 22);
            estiloColuna3.put("key_attributes_typeface", 1); // Negrito
            estiloColuna3.put("key_attributes_weight", 3); // 30%

            styles.put(estiloColuna1);
            styles.put(estiloColuna2);
            styles.put(estiloColuna3);

            // Valores (conteúdo)
            JSONArray values = new JSONArray();

            // Cabeçalho
            values.put(""); values.put("COMPROVANTE DE PAGAMENTO"); values.put("");
            values.put(""); values.put(""); values.put("");

            // Recupera informações do pagamento
            String authCode = getSharedPreferences("payment_info", MODE_PRIVATE).getString("auth_code", "");
            String brand = getSharedPreferences("payment_info", MODE_PRIVATE).getString("brand", "");
            String cardMask = getSharedPreferences("payment_info", MODE_PRIVATE).getString("card_mask", "");

            // Informações do pagamento
            values.put("Valor Pago:"); values.put(""); values.put("R$ 46,20");
            values.put("Forma de Pagamento:"); values.put(""); values.put(brand);
            values.put("Cartão:"); values.put(""); values.put(cardMask);
            values.put("Código Autorização:"); values.put(""); values.put(authCode);

            // Rodapé
            values.put(""); values.put(""); values.put("");
            values.put(""); values.put("Obrigado pela preferência!"); values.put("");

            // Adiciona ao JSON final
            jsonPrint.put("styles", styles);
            jsonPrint.put("value", values);

            // Converte para Base64
            return Base64.encodeToString(jsonPrint.toString().getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);

        } catch (Exception e) {
            Log.e(TAG, "Erro ao preparar cupom: " + e.getMessage(), e);
            return "";
        }
    }

    /**
     * Navega para a tela principal
     */
    private void navigateToMainScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}