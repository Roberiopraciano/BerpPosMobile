package com.mobile.berp.BerpPOSMobile.pagamento;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Classe auxiliar genérica para gerenciar tokens e credenciais de autenticação
 * para diferentes provedores de pagamento
 */
public class PaymentAuthHelper {
    private static final String PREFS_NAME = "PaymentAuthPrefs";
    private static final String PREFIX_ACCESS_TOKEN = "access_token_";
    private static final String PREFIX_CLIENT_ID = "client_id_";
    private static final String PREFIX_MERCHANT_CODE = "merchant_code_";
    private static final String PREFIX_API_KEY = "api_key_";
    private static final String PREFIX_SECRET_KEY = "secret_key_";

    private SharedPreferences prefs;
    private String providerName;

    /**
     * Construtor que recebe o contexto e o nome do provedor de pagamento
     * @param context Contexto da aplicação
     * @param providerName Nome do provedor (ex: "cielo", "stone", "getnet", etc.)
     */
    public PaymentAuthHelper(Context context, String providerName) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.providerName = providerName.toLowerCase();
    }

    /**
     * Construtor alternativo que usa o valor do modelo POS configurado
     * @param context Contexto da aplicação
     */
    public PaymentAuthHelper(Context context) {
        this(context, getPosModel());
    }

    /**
     * Obtém o modelo POS configurado
     * @return Nome do modelo POS
     */
    private static String getPosModel() {
        try {
            // Tenta acessar o BuildConfig via reflection para evitar dependências diretas
            Class<?> buildConfigClass = Class.forName("com.mobile.berp.BerpPosMobile.BuildConfig");
            java.lang.reflect.Field field = buildConfigClass.getField("POS_MODEL");
            return (String) field.get(null);
        } catch (Exception e) {
            Log.e("PaymentAuthHelper", "Erro ao acessar BuildConfig.POS_MODEL: " + e.getMessage());
            return "desconhecido";
        }
    }

    /**
     * Salva informações de autenticação básicas (accessToken, clientId, merchantCode)
     */
    public void saveAuthInfo(String accessToken, String clientId, String merchantCode) {
        SharedPreferences.Editor editor = prefs.edit();

        if (accessToken != null && !accessToken.isEmpty()) {
            editor.putString(PREFIX_ACCESS_TOKEN + providerName, accessToken);
        }

        if (clientId != null && !clientId.isEmpty()) {
            editor.putString(PREFIX_CLIENT_ID + providerName, clientId);
        }

        if (merchantCode != null && !merchantCode.isEmpty()) {
            editor.putString(PREFIX_MERCHANT_CODE + providerName, merchantCode);
        }

        editor.apply();
        Log.d("PaymentAuthHelper", "Informações de autenticação salvas para: " + providerName);
    }

    /**
     * Salva chaves de API (para provedores que usam esse método de autenticação)
     */
    public void saveApiKeys(String apiKey, String secretKey) {
        SharedPreferences.Editor editor = prefs.edit();

        if (apiKey != null && !apiKey.isEmpty()) {
            editor.putString(PREFIX_API_KEY + providerName, apiKey);
        }

        if (secretKey != null && !secretKey.isEmpty()) {
            editor.putString(PREFIX_SECRET_KEY + providerName, secretKey);
        }

        editor.apply();
        Log.d("PaymentAuthHelper", "Chaves de API salvas para: " + providerName);
    }

    /**
     * Obtém o access token salvo
     * @param defaultValue Valor padrão caso não exista
     * @return Token de acesso ou valor padrão se não configurado
     */
    public String getAccessToken(String defaultValue) {
        //return prefs.getString(PREFIX_ACCESS_TOKEN + providerName, defaultValue);
        return "H8tiTQWi04OyE9YQQQklLDqBHYUrytZk1k6mQsBxHuXlFzvXS1";
    }

    /**
     * Obtém o access token salvo
     * @return Token de acesso ou null se não configurado
     */
    public String getAccessToken() {
        return getAccessToken(null);
    }

    /**
     * Obtém o client ID salvo
     * @param defaultValue Valor padrão caso não exista
     * @return Client ID ou valor padrão se não configurado
     */
    public String getClientId(String defaultValue) {
       // return prefs.getString(PREFIX_CLIENT_ID + providerName, defaultValue);
        return "2PI5DICbOwAyHNITv4oUdAH3OwCLPRbt6cBsf37f2NeU3TTtkx";
    }

    /**
     * Obtém o client ID salvo
     * @return Client ID ou null se não configurado
     */
    public String getClientId() {
        return getClientId(null);
    }

    /**
     * Obtém o código do comerciante salvo
     * @param defaultValue Valor padrão caso não exista
     * @return Código do comerciante ou valor padrão se não configurado
     */
    public String getMerchantCode(String defaultValue) {
        return prefs.getString(PREFIX_MERCHANT_CODE + providerName, defaultValue);
    }

    /**
     * Obtém o código do comerciante salvo
     * @return Código do comerciante ou null se não configurado
     */
    public String getMerchantCode() {
        return getMerchantCode(null);
    }

    /**
     * Obtém a chave de API salva
     * @param defaultValue Valor padrão caso não exista
     * @return Chave de API ou valor padrão se não configurado
     */
    public String getApiKey(String defaultValue) {
        return prefs.getString(PREFIX_API_KEY + providerName, defaultValue);
    }

    /**
     * Obtém a chave de API salva
     * @return Chave de API ou null se não configurado
     */
    public String getApiKey() {
        return getApiKey(null);
    }

    /**
     * Obtém a chave secreta salva
     * @param defaultValue Valor padrão caso não exista
     * @return Chave secreta ou valor padrão se não configurado
     */
    public String getSecretKey(String defaultValue) {
        return prefs.getString(PREFIX_SECRET_KEY + providerName, defaultValue);
    }

    /**
     * Obtém a chave secreta salva
     * @return Chave secreta ou null se não configurado
     */
    public String getSecretKey() {
        return getSecretKey(null);
    }

    /**
     * Verifica se as credenciais básicas (accessToken e clientId) estão configuradas
     * @return true se as credenciais estiverem configuradas
     */
    public boolean hasBasicCredentials() {
        String token = getAccessToken();
        String clientId = getClientId();

        return token != null && !token.isEmpty() && clientId != null && !clientId.isEmpty();
    }

    /**
     * Verifica se as chaves de API estão configuradas
     * @return true se as chaves de API estiverem configuradas
     */
    public boolean hasApiKeys() {
        String apiKey = getApiKey();
        String secretKey = getSecretKey();

        return apiKey != null && !apiKey.isEmpty() && secretKey != null && !secretKey.isEmpty();
    }

    /**
     * Limpa todas as credenciais salvas para o provedor atual
     */
    public void clearCredentials() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(PREFIX_ACCESS_TOKEN + providerName);
        editor.remove(PREFIX_CLIENT_ID + providerName);
        editor.remove(PREFIX_MERCHANT_CODE + providerName);
        editor.remove(PREFIX_API_KEY + providerName);
        editor.remove(PREFIX_SECRET_KEY + providerName);
        editor.apply();

        Log.d("PaymentAuthHelper", "Credenciais removidas para: " + providerName);
    }

    /**
     * Limpa todas as credenciais salvas para todos os provedores
     */
    public static void clearAllCredentials(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Log.d("PaymentAuthHelper", "Todas as credenciais de pagamento foram removidas");
    }
}