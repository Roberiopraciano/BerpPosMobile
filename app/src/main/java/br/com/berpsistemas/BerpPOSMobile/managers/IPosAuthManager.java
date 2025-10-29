package br.com.berpsistemas.BerpPOSMobile.managers;


import android.app.Activity;
import android.content.Context;

public interface IPosAuthManager {
    void requestRefund(Activity activity, String originalTransactionId, int amountInCentsToRefund, IPosAuthManager.RefundCallback refundCallback);

    interface AuthCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    interface RefundCallback{

        void onRefundAuthRequired(String errorMessage);

        void onRefundSuccess(String message);

        void onRefundError(String errorMessage);
    }

    void initialize(Context context, AuthCallback callback);
    boolean isAuthenticated(); // Renomeado de isReady para ser mais específico sobre auth
    String getCurrentToken(); // Para obter o token se já autenticado
    void requestAuthentication(Activity activity, AuthCallback callback); // Para iniciar o fluxo de auth
    void handleAuthenticationResult(Context context, android.net.Uri intentData); // Para processar o callback do deeplink
}