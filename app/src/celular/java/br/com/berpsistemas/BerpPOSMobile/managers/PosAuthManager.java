package br.com.berpsistemas.BerpPOSMobile.managers;


import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

public class PosAuthManager implements IPosAuthManager {
    private static final String TAG = "DefaultAuthManager";

    @Override
    public void initialize(Context context, AuthCallback callback) {
        Log.d(TAG, "DefaultPosAuthManager inicializado. Nenhuma autenticação específica necessária.");
        if (callback != null) {
            callback.onSuccess(); // Sempre sucesso, pois não faz nada
        }
    }

    @Override
    public boolean isAuthenticated() {
        return true; // Sempre autenticado para este flavor
    }

    @Override
    public String getCurrentToken() {
        return null; // Nenhum token para este flavor
    }

    @Override
    public void requestAuthentication(Activity activity, AuthCallback callback) {
        Log.d(TAG, "DefaultPosAuthManager: Nenhuma autenticação para solicitar.");
        if (callback != null) {
            callback.onSuccess(); // Já está "autenticado"
        }
    }

    @Override
    public void handleAuthenticationResult(Context context, Uri intentData) {
        Log.d(TAG, "DefaultPosAuthManager: Nenhum resultado de autenticação para manipular.");
        // Não faz nada
    }
}