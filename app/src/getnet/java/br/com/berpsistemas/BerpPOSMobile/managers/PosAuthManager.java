package br.com.berpsistemas.BerpPOSMobile.managers;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;
import java.nio.charset.StandardCharsets;

public class PosAuthManager implements IPosAuthManager {

    private static final String YOUR_APP_RETURN_SCHEME = "berppos";
    private static final String YOUR_APP_RETURN_HOST = "ifood-print-auth-result";


    @Override
    public void requestRefund(Activity activity, String originalTransactionId, int amountInCentsToRefund, RefundCallback refundCallback) {

    }

    @Override
    public void initialize(Context context, AuthCallback callback) {

    }

    @Override
    public boolean isAuthenticated() {

        return true;
    }

    @Override
    public String getCurrentToken() {

        return "";
    }

    @Override
    public void requestAuthentication(Activity activity, AuthCallback callback) {

    }


    @Override
    public void handleAuthenticationResult(Context context, Uri intentData) {

    }
}