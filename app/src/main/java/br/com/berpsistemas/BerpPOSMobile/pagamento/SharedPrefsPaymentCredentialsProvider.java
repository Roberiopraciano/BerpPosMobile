package br.com.berpsistemas.BerpPOSMobile.pagamento;

import android.content.Context;
import android.content.SharedPreferences;

// Esta classe pode ser a padrão em src/main/ ou específica para flavors
// que usam SharedPreferences. Se for em src/main, nomeie-a por exemplo
// SharedPrefsPaymentCredentialsProvider.java
// Se for para um flavor específico (ex: "getnet"), coloque em src/getnet/...
// e nomeie-a PaymentCredentialsProvider.java
public class SharedPrefsPaymentCredentialsProvider implements IPaymentAuthCredentialsProvider {
    private static final String PREFS_NAME = "PaymentAuthPrefs"; // Mesmo nome de antes
    private static final String PREFIX_ACCESS_TOKEN = "access_token_";
    private static final String PREFIX_CLIENT_ID = "client_id_";
    private static final String PREFIX_MERCHANT_CODE = "merchant_code_";
    private static final String PREFIX_API_KEY = "api_key_";
    private static final String PREFIX_SECRET_KEY = "secret_key_";

    private SharedPreferences prefs;
    private String providerName; // O nome do provedor para buscar nas prefs

    public SharedPrefsPaymentCredentialsProvider(Context context, String providerName) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.providerName = providerName.toLowerCase();
    }

    @Override
    public String getAccessToken(String defaultValue) {
        return prefs.getString(PREFIX_ACCESS_TOKEN + providerName, defaultValue);
    }

    @Override
    public String getClientId(String defaultValue) {
        return prefs.getString(PREFIX_CLIENT_ID + providerName, defaultValue);
    }

    @Override
    public String getMerchantCode(String defaultValue) {
        return prefs.getString(PREFIX_MERCHANT_CODE + providerName, defaultValue);
    }

    @Override
    public String getApiKey(String defaultValue) {
        return prefs.getString(PREFIX_API_KEY + providerName, defaultValue);
    }

    @Override
    public String getSecretKey(String defaultValue) {
        return prefs.getString(PREFIX_SECRET_KEY + providerName, defaultValue);
    }
}