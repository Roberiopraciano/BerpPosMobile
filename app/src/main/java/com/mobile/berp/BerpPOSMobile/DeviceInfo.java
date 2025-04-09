package com.mobile.berp.BerpPOSMobile;



import android.content.Context;
import android.os.Build;
import android.provider.Settings;


import java.util.UUID;

public class DeviceInfo {

    public static String getDeviceName() {
        return UUID.randomUUID().toString(); // Nome único do dispositivo
    }

    public static String getAndroidID(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context não pode ser null");
        }

        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }


    /**
     * Obtém o nome configurado pelo usuário para o dispositivo.
     * Compatível a partir do Android 7.1 (API 25).
     *
     * @param context Contexto da aplicação ou Activity.
     * @return Nome do dispositivo configurado pelo usuário ou null se não disponível.
     */
    public static String getUserDeviceName(Context context) {
        String deviceName = null;

        // Verifica se está rodando em uma versão que suporta DEVICE_NAME
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            deviceName = Settings.Global.getString(context.getContentResolver(), Settings.Global.DEVICE_NAME);
        }

        // Fallback para null
        if (deviceName == null || deviceName.isEmpty()) {
            deviceName = "Desconhecido";
        }

        return deviceName;
    }

    /**
     * Obtém o modelo do dispositivo (exemplo: "Galaxy S21").
     *
     * @return Modelo do dispositivo.
     */
    public static String getDeviceModel() {
        return Build.MODEL; // Retorna o modelo do dispositivo
    }

    /**
     * Obtém o nome completo do dispositivo (fabricante + modelo).
     *
     * @return Nome completo do dispositivo.
     */
    public static String getFullDeviceName() {
        String manufacturer = Build.MANUFACTURER; // Fabricante
        String model = Build.MODEL; // Modelo

        // Se o modelo já inclui o fabricante, retorna apenas o modelo
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    /**
     * Obtém a versão do sistema operacional (exemplo: "Android 12").
     *
     * @return Versão do sistema operacional.
     */
    public static String getOSVersion() {
        return "Android " + Build.VERSION.RELEASE; // Versão do sistema operacional
    }

    /**
     * Capitaliza a primeira letra de uma string.
     *
     * @param str String a ser capitalizada.
     * @return String com a primeira letra em maiúsculo.
     */
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        char first = str.charAt(0);
        if (Character.isUpperCase(first)) {
            return str;
        } else {
            return Character.toUpperCase(first) + str.substring(1);
        }
    }

    public static String getAndroidVersion() {
        return Build.VERSION.RELEASE;  // Versão do Android
    }
}