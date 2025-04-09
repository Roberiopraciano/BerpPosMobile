package com.mobile.berp.BerpPOSMobile.Controller;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;
import android.os.Build;

public class WiFi extends BroadcastReceiver {

    private WifiManager wifiManager;
    private WiFiListener wiFiListener;

    public void setWiFiListener(WiFiListener wiFiListener) {
        this.wiFiListener = wiFiListener;
    }

    public interface WiFiListener {
        void onResultScan(Context arg0, Intent arg1, List<ScanResult> results);
    }

    public static WiFi startScanWIFI(Context context) {
        WiFi wifi = new WiFi();

        if (!wifi.checkPermissions(context)) {
            Toast.makeText(context, "Conceda permissão de localização para escanear redes Wi-Fi.", Toast.LENGTH_LONG).show();
            return null;
        }

        wifi.wifiManager = wifi.getWifiManager(context);

        if (wifi.wifiManager == null) {
            Toast.makeText(context, "Wi-Fi não suportado no dispositivo.", Toast.LENGTH_SHORT).show();
            return null;
        }

        if (!wifi.wifiManager.isWifiEnabled()) {
            Toast.makeText(context, "Por favor, habilite o Wi-Fi manualmente.", Toast.LENGTH_LONG).show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
            }
            return null;
        }

        context.registerReceiver(wifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifi.wifiManager.startScan();
        return wifi;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Permissão de localização não concedida.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<ScanResult> results = wifiManager.getScanResults();

        if (wiFiListener != null) {
            wiFiListener.onResultScan(context, intent, results);
        }

        try {
            context.unregisterReceiver(this);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private boolean checkPermissions(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private WifiManager getWifiManager(Context context) {
        if (wifiManager == null) {
            wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        }
        return wifiManager;
    }

    public static String getMacWifi(Context context) {
        String mac = "";
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Desde o Android 6.0 (API 23), o endereço MAC real é inacessível para aplicativos
                mac = "02:00:00:00:00:00"; // Valor padrão retornado
            } else {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return "Permissão não concedida";
                }
                mac = wifiManager.getConnectionInfo().getMacAddress();
            }
        } catch (Exception e) {
            e.printStackTrace();
            mac = "Erro ao obter endereço MAC";
        }
        return mac;
    }

    public static String getIpWifi(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        return String.format("%d.%d.%d.%d",
                (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff));
    }
}