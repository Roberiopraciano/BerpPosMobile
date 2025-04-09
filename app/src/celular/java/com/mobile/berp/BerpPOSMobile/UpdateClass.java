package com.mobile.berp.BerpPOSMobile;

import android.app.Activity;

import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.mobile.berp.BerpPosMobile.BuildConfig;

public class UpdateClass {

    private static final int REQUEST_CODE_UPDATE = 1234;

    // Interface de callback para sinalizar a conclusão da verificação
    public interface UpdateCallback {
        void onUpdateCheckCompleted(boolean updateRequired);
    }

    /**
     * Verifica se há uma atualização disponível para o aplicativo.
     *
     * @param activity Atividade de contexto
     * @param callback Callback para informar se a atualização está disponível
     */
    public static void checkForUpdate(Activity activity, UpdateCallback callback) {
        if (!BuildConfig.FLAVOR.equals("celular")) {
            // Ignorar atualização para flavors diferentes de "celular"
            callback.onUpdateCheckCompleted(false);
            return;
        }

        // Criar o gerenciador de atualização
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(activity);

        // Verificar a disponibilidade de atualização
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                // Atualização disponível
                callback.onUpdateCheckCompleted(true);
            } else {
                // Nenhuma atualização disponível
                callback.onUpdateCheckCompleted(false);
            }
        }).addOnFailureListener(e -> {
            // Erro ao verificar atualizações
            e.printStackTrace();
            callback.onUpdateCheckCompleted(false);
        });
    }

    /**
     * Inicia o fluxo de atualização para o aplicativo.
     *
     * @param activity Atividade de contexto
     */
    public static void startUpdate(Activity activity, UpdateCallback callback) {
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(activity);

        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            activity,
                            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                            REQUEST_CODE_UPDATE
                    );
                    callback.onUpdateCheckCompleted(true); // Success
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onUpdateCheckCompleted(false); // Failure
                }
            } else {
                callback.onUpdateCheckCompleted(false); // No update required
            }
        }).addOnFailureListener(e -> {
            e.printStackTrace();
            callback.onUpdateCheckCompleted(false); // Failure
        });
    }
}