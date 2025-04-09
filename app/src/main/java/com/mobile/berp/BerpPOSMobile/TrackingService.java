package com.mobile.berp.BerpPOSMobile;


import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.mobile.berp.BerpPOSMobile.model.BerpModel;
import com.mobile.berp.BerpPOSMobile.model.Variaveis;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.SharedPreferences;


import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;


public class TrackingService extends Service  implements DefaultLifecycleObserver {
    private static final String TRACKING_ENABLED_KEY = "tracking_enabled";

    private static final String TAG = "TrackingService";
    private static final long INTERVAL = 10000; // 10 segundos
    private static final String CHANNEL_ID = "tracking_channel";
    private LocationManager locationManager;
    private Handler handler = new Handler();
    private String endpointUrl = "https://berploc.requestcatcher.com/test";//https://seu-endpoint.com/api/location"; // Altere para seu endpoint

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();


//        startForeground(1, getNotification());
//        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
//
//        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//        requestLocationUpdates();



    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        onAppForegrounded();
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        onAppBackgrounded();
    }

    private static Location lastLocation;


    public boolean canUseGps(Context context) {
        boolean hasGps = isGpsAvailable(context);
        boolean gpsEnabled = isGpsEnabled(context);
        return hasGps && gpsEnabled;
    }

    private boolean isGpsEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private boolean isGpsAvailable(Context context) {

        PackageManager packageManager = context.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            lastLocation = location; // Atualiza a última localização
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            Log.d(TAG, "Localização: " + latitude + ", " + longitude);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override
        public void onProviderEnabled(@NonNull String provider) {}
        @Override
        public void onProviderDisabled(@NonNull String provider) {}
    };

    public static Location getLastLocation() {
        return lastLocation;
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded() {
        try {
            SharedPreferences preferences = getSharedPreferences("preferencias_1", Context.MODE_PRIVATE);
            boolean isTrackingEnabled = preferences.getBoolean(TRACKING_ENABLED_KEY, true);
            if (isTrackingEnabled) {
                if (canUseGps(this)){
                    requestLocationUpdates();
                } else {
                    stopLocationUpdates();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro no método onAppForegrounded", e);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
//        try {
//            SharedPreferences prefs = getSharedPreferences("preferencias_1", Context.MODE_PRIVATE);
//            boolean isTrackingEnabled = prefs.getBoolean(TRACKING_ENABLED_KEY, false);
//
//            if (isTrackingEnabled) {
//
//                stopLocationUpdates();
//                stopSelf();
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "Erro no método onAppBackgrounded", e);
//        }
    }




    public static void stopTrackingService(Context context) {
     //   Intent serviceIntent = new Intent(context, TrackingService.class);
     //   context.stopService(serviceIntent);
    }


    private void stopLocationUpdates() {
    //    if (locationManager != null) {
    //        locationManager.removeUpdates(locationListener);
    //    }
    }

    public static void setTrackingEnabled(Context context, boolean enabled) {
//        // = PreferenceManager.getDefaultSharedPreferences(context);
//        SharedPreferences preferences = context.getApplicationContext().getSharedPreferences("preferencias_1", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putBoolean(TRACKING_ENABLED_KEY, enabled);
//        editor.apply();
    }


//    private Notification getNotification() {
//        NotificationCompat.Builder builder;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            builder = new NotificationCompat.Builder(this, CHANNEL_ID);
//        } else {
//            builder = new NotificationCompat.Builder(this); // Sem CHANNEL_ID para SDK < 26
//        }
//
//        builder.setContentTitle("Rastreamento Ativo")
//                .setContentText("O serviço de rastreamento está em execução.")
//                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//
//        return builder.build();
//    }

    private void createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            // Para Android 8.0 e superior
//            NotificationChannel serviceChannel = new NotificationChannel(
//                    CHANNEL_ID,
//                    "Tracking Service Channel",
//                    NotificationManager.IMPORTANCE_DEFAULT
//            );
//            NotificationManager manager = getSystemService(NotificationManager.class);
//            manager.createNotificationChannel(serviceChannel);
//        } else {
//            // Para versões anteriores ao Android 8.0 (SDK 25 e abaixo)
//            // Não é necessário criar um canal, mas você pode configurar notificações normalmente
//            // usando NotificationCompat.Builder sem configurar canais.
//        }
    }

    private void requestLocationUpdates() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            Log.e(TAG, "Permissão de localização não concedida.");
//            stopSelf();
//            return;
//        }
//        if (locationManager!=null){
//            locationManager.requestLocationUpdates(
//                    LocationManager.GPS_PROVIDER,
//                    5000,
//                    5,
//                    locationListener
//            );
//        }

    }


    private void sendLocationToServer(double latitude, double longitude) {
//        new Thread(() -> {
//            try {
//                // Obtém os dados necessários
//                String terminal = Variaveis.getNumTerminal();
//                String garcon = BerpModel.getFuncionario();  // Ajuste de nomenclatura
//                String deviceId = Variaveis.getNumeroDispositivo();
//
//                // Cria a URL de conexão
//                URL url = new URL(endpointUrl);
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestMethod("POST");
//                connection.setRequestProperty("Content-Type", "application/json; utf-8");
//                connection.setDoOutput(true);
//
//                // Monta o JSON com todos os dados necessários
//                String jsonInputString = String.format(
//                        "{\"latitude\": %f, \"longitude\": %f, \"terminal\": \"%s\", \"garcon\": \"%s\", \"deviceId\": \"%s\"}",
//                        latitude, longitude, terminal, garcon, deviceId
//                );
//                Log.d(TAG,jsonInputString);
//                // Envia o JSON no corpo da requisição
//                try (OutputStream os = connection.getOutputStream()) {
//                    byte[] input = jsonInputString.getBytes("utf-8");
//                    os.write(input, 0, input.length);
//                }
//
//                // Lê a resposta do servidor
//                int responseCode = connection.getResponseCode();
//                Log.d(TAG, "Código de resposta do servidor: " + responseCode);
//
//                connection.disconnect();
//            } catch (Exception e) {
//                Log.e(TAG, "Erro ao enviar localização: ", e);
//            }
//        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;  // Mantém o serviço rodando
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }
}