package com.mobile.berp.BerpPOSMobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class TrackingBootReceiver extends BroadcastReceiver {

    private static final String TAG = "TrackingBootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Recebido: " + intent.getAction());

        // Inicia o serviço de rastreamento quando o dispositivo for reiniciado ou o aplicativo for aberto
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
                Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {

            Intent serviceIntent = new Intent(context, TrackingService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            }
        }
    }
}