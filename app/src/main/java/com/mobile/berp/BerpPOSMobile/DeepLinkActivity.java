package com.mobile.berp.BerpPOSMobile;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import com.mobile.berp.BerpPosMobile.BuildConfig;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;


public class DeepLinkActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri data = getIntent().getData();
        String flavor = BuildConfig.FLAVOR;

        Log.d("DeepLinkActivity", "Flavor: " + flavor);
        Log.d("DeepLinkActivity", "Link recebido: " + (data != null ? data.toString() : "null"));

        if (data != null) {
            switch (flavor) {
                case "cielo":
                    tratarCielo(data);
                    break;
                case "stone":
                    tratarStone(data);
                    break;
                case "getnet":
                    tratarGetnet(data);
                    break;
                default:
                    Log.w("DeepLinkActivity", "Flavor desconhecido");
            }
        }

        finish();
    }

    private void tratarCielo(Uri data) {
        try {
            // Pega o parâmetro "response" da URL
            String base64Response = data.getQueryParameter("response");

            if (base64Response == null) {
                Log.w("CIELO", "Nenhum parâmetro 'response' recebido.");
                return;
            }

            // Decodifica o Base64
            byte[] decodedBytes = Base64.decode(base64Response, Base64.DEFAULT);
            String jsonString = new String(decodedBytes, StandardCharsets.UTF_8);

            Log.d("CIELO", "JSON Decodificado: " + jsonString);

            // Transforma em objeto JSON
            JSONObject json = new JSONObject(jsonString);

            String status = json.optString("status");
            String transacao = json.optString("transacao");

            Log.d("CIELO", "Status: " + status + ", Transação: " + transacao);

            // Aqui você pode salvar local, mandar para o Flutter, etc.

        } catch (Exception e) {
            Log.e("CIELO", "Erro ao tratar base64 da resposta", e);
        }
    }

    private void tratarStone(Uri data) {
        // Mesma ideia, com tratamento específico
        Log.d("STONE", "Deep link: " + data.toString());
    }

    private void tratarGetnet(Uri data) {
        Log.d("GETNET", "Deep link: " + data.toString());
    }
}