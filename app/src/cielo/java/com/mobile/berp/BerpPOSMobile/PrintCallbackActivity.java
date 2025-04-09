package com.mobile.berp.BerpPOSMobile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

/**
 * Activity responsável por receber callbacks da impressão via deeplink
 */
public class PrintCallbackActivity extends AppCompatActivity {

    private static final String TAG = "PrintCallback";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // Processa o intent de deeplink
            Intent intent = getIntent();
            if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
                processPrintCallback(intent);
            } else {
                // Se não for um deeplink válido, apenas finaliza a activity
                Log.e(TAG, "Intent inválido ou não é ACTION_VIEW");
                navigateToMainScreen();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar callback de impressão: " + e.getMessage(), e);
            Toast.makeText(this, "Erro ao processar retorno da impressão: " + e.getMessage(), Toast.LENGTH_LONG).show();
            navigateToMainScreen();
        }
    }

    /**
     * Processa o callback de impressão
     */
    private void processPrintCallback(Intent intent) {
        try {
            Uri uri = intent.getData();
            if (uri != null) {
                // Obtém o parâmetro de resposta (pode ser codificado em base64)
                String responseParam = uri.getQueryParameter("response");

                if (responseParam != null) {
                    // Tenta decodificar como Base64 (se for o caso)
                    String responseJson;
                    try {
                        byte[] responseBytes = Base64.decode(responseParam, Base64.DEFAULT);
                        responseJson = new String(responseBytes, StandardCharsets.UTF_8);
                    } catch (IllegalArgumentException e) {
                        // Se não for Base64 válido, usa a string diretamente
                        responseJson = responseParam;
                    }

                    Log.d(TAG, "Resposta de impressão: " + responseJson);

                    // Verifica se é JSON válido
                    try {
                        JSONObject jsonResponse = new JSONObject(responseJson);

                        // Verifica o status da impressão
                        boolean success = jsonResponse.optBoolean("success", false);
                        String message = jsonResponse.optString("message", "");

                        if (success) {
                            Toast.makeText(this, "Impressão realizada com sucesso", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Erro na impressão: " + message, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        // Se não for JSON válido, verifica por outros padrões conhecidos
                        if (responseJson.contains("success") || responseJson.contains("OK")) {
                            Toast.makeText(this, "Impressão realizada com sucesso", Toast.LENGTH_SHORT).show();
                        } else if (responseJson.contains("error") || responseJson.contains("fail")) {
                            Toast.makeText(this, "Erro na impressão: " + responseJson, Toast.LENGTH_LONG).show();
                        } else {
                            // Resposta desconhecida
                            Toast.makeText(this, "Resposta de impressão: " + responseJson, Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    // Se não houver parâmetro 'response', verifica outros parâmetros conhecidos
                    String status = uri.getQueryParameter("status");
                    String errorCode = uri.getQueryParameter("errorCode");

                    if (status != null) {
                        if ("success".equals(status) || "OK".equals(status)) {
                            Toast.makeText(this, "Impressão realizada com sucesso", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Status da impressão: " + status, Toast.LENGTH_SHORT).show();
                        }
                    } else if (errorCode != null) {
                        String errorMessage = uri.getQueryParameter("errorMessage");
                        Toast.makeText(this, "Erro na impressão: " + (errorMessage != null ? errorMessage : errorCode), Toast.LENGTH_LONG).show();
                    } else {
                        // Não encontrou parâmetros conhecidos
                        Toast.makeText(this, "Resposta de impressão recebida", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Log.e(TAG, "URI nula na resposta de impressão");
                Toast.makeText(this, "Resposta de impressão inválida", Toast.LENGTH_SHORT).show();
            }

            // Sempre navega para a tela principal após processar a impressão
            navigateToMainScreen();

        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar callback de impressão: " + e.getMessage(), e);
            Toast.makeText(this, "Erro ao processar retorno da impressão: " + e.getMessage(), Toast.LENGTH_LONG).show();
            navigateToMainScreen();
        } finally {
            // Finaliza esta activity após o processamento
            finish();
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

    /**
     * Limpa os dados do pagamento após a conclusão
     */
    private void clearPaymentData() {
        try {
            // Limpa as informações temporárias de pagamento
            getSharedPreferences("payment_info", MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao limpar dados de pagamento: " + e.getMessage(), e);
        }
    }
}