package br.com.berpsistemas.BerpPOSMobile.managers;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import br.com.berpsistemas.BerpPOSMobile.PlugPagProvider;

import org.json.JSONObject;
import java.nio.charset.StandardCharsets;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;

public class PosAuthManager implements IPosAuthManager {

    private static final String YOUR_APP_RETURN_SCHEME = "berppos";

    private PlugPag plugPag;


    @Override
    public void requestRefund(Activity activity, String originalTransactionId, int amountInCentsToRefund, RefundCallback refundCallback) {

    }

    @Override
    public void initialize(Context context, AuthCallback callback) {
        plugPag = PlugPagProvider.getInstance(context);
    }

    @Override
    public boolean isAuthenticated() {

        if (!plugPag.isAuthenticated()){
            plugPag.startOnBoarding();
        }
        else {
            return plugPag.isAuthenticated();
        }
        return false;
    }

    @Override
    public String getCurrentToken() {

        return "";
    }

    @Override
    public void requestAuthentication(Activity activity, AuthCallback callback) {
        new Thread(() -> {
            try {
                Log.d("PagSeguro", "Verificando autenticação...");
                boolean isActivated = plugPag.isAuthenticated();




            } catch (Exception e) {
                Log.e("PagSeguro", "Erro ao verificar autenticação: " + e.getMessage(), e);

            }
        }).start();
    }


    @Override
    public void handleAuthenticationResult(Context context, Uri intentData) {

    }
}