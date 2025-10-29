package br.com.berpsistemas.BerpPOSMobile;

import android.app.Activity;

public class UpdateClass {

    // Interface de callback para manter compatibilidade
    public interface UpdateCallback {
        void onUpdateCheckCompleted(boolean updateRequired);
    }

    // Método vazio para manter compatibilidade
    public static void checkForUpdate(Activity activity, UpdateCallback callback) {
        callback.onUpdateCheckCompleted(false);
    }

    // Método vazio para manter compatibilidade
    public static void startUpdate(Activity activity, UpdateCallback callback) {
        callback.onUpdateCheckCompleted(false);
    }
}