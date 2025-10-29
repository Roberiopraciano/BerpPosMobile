package br.com.berpsistemas.BerpPOSMobile.pagamento;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

// Remova a importação do BuildConfig daqui, a lógica de flavor será na instanciação
// import com.mobile.berpsistemas.BerpPosMobile.BuildConfig; // REMOVER

public class PaymentAuthHelper {
    private static final String PREFS_NAME = "PaymentAuthPrefs"; // Usado para salvar
    // Mantém os prefixos para salvar, pois o helper ainda pode salvar
    private static final String PREFIX_ACCESS_TOKEN = "access_token_";
    private static final String PREFIX_CLIENT_ID = "client_id_";
    private static final String PREFIX_MERCHANT_CODE = "merchant_code_";
    private static final String PREFIX_API_KEY = "api_key_";
    private static final String PREFIX_SECRET_KEY = "secret_key_";


    private SharedPreferences prefsForSaving; // Para salvar dados
    private IPaymentAuthCredentialsProvider credentialsProvider;
    private String currentProviderNameForSaving; // Usado para salvar nas SharedPreferences

    /**
     * Construtor que recebe o contexto.
     * A implementação específica do provedor de credenciais será instanciada internamente.
     * @param context Contexto da aplicação
     */
    public PaymentAuthHelper(Context context) {
        this.prefsForSaving = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // AQUI é onde a mágica dos flavors acontece.
        // A classe "PaymentCredentialsProvider" será a específica do flavor compilado.
        this.credentialsProvider = new PaymentCredentialsProvider(context);

        // Para salvar, podemos precisar de um nome de provedor.
        // Podemos tentar obtê-lo do BuildConfig ou passá-lo se a intenção é salvar
        // para um provedor específico que não é o do flavor atual.
        // Por simplicidade, vamos assumir que o "salvar" é para o provedor do flavor atual.
        // Ou, o método de salvar precisaria de um providerName.
        try {
            // Tenta acessar o BuildConfig via reflection para evitar dependências diretas
            // Se você puder obter o nome do flavor de outra forma, melhor.
            Class<?> buildConfigClass = Class.forName(context.getPackageName() + ".BuildConfig");
            java.lang.reflect.Field field = buildConfigClass.getField("FLAVOR"); // Ou POS_MODEL se preferir
            this.currentProviderNameForSaving = ((String) field.get(null)).toLowerCase();
        } catch (Exception e) {
            Log.w("PaymentAuthHelper", "Não foi possível obter FLAVOR do BuildConfig, usando 'default' para salvar: " + e.getMessage());
            this.currentProviderNameForSaving = "default";
        }
    }

    /**
     * Construtor alternativo para especificar um providerName para salvar/carregar
     * de SharedPreferences, independentemente do flavor atual (útil se você
     * estiver usando SharedPrefsPaymentCredentialsProvider diretamente).
     */
    public PaymentAuthHelper(Context context, String explicitProviderNameForSharedPrefs) {
        this.prefsForSaving = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.currentProviderNameForSaving = explicitProviderNameForSharedPrefs.toLowerCase();

        // Se você quer que este construtor use especificamente o SharedPrefsProvider:
        this.credentialsProvider = new SharedPrefsPaymentCredentialsProvider(context, this.currentProviderNameForSaving);
        // Ou, se ainda quer que ele use o do flavor, mas salve com um nome específico:
        // this.credentialsProvider = new PaymentCredentialsProvider(context);
        // A lógica de como `currentProviderNameForSaving` interage com `credentialsProvider`
        // precisa ser clara.
        // No modelo atual, `credentialsProvider` obtém dados conforme o flavor.
        // Os métodos save* usam `currentProviderNameForSaving` para a chave nas prefs.
    }


    // MÉTODOS DE SALVAR (usam SharedPreferences diretamente com currentProviderNameForSaving)
    public void saveAuthInfo(String accessToken, String clientId, String merchantCode) {
        SharedPreferences.Editor editor = prefsForSaving.edit();
        String providerKey = this.currentProviderNameForSaving;

        if (accessToken != null && !accessToken.isEmpty()) {
            editor.putString(PREFIX_ACCESS_TOKEN + providerKey, accessToken);
        }
        if (clientId != null && !clientId.isEmpty()) {
            editor.putString(PREFIX_CLIENT_ID + providerKey, clientId);
        }
        if (merchantCode != null && !merchantCode.isEmpty()) {
            editor.putString(PREFIX_MERCHANT_CODE + providerKey, merchantCode);
        }
        editor.apply();
        Log.d("PaymentAuthHelper", "Informações de autenticação salvas para (prefs key): " + providerKey);
    }

    public void saveApiKeys(String apiKey, String secretKey) {
        SharedPreferences.Editor editor = prefsForSaving.edit();
        String providerKey = this.currentProviderNameForSaving;

        if (apiKey != null && !apiKey.isEmpty()) {
            editor.putString(PREFIX_API_KEY + providerKey, apiKey);
        }
        if (secretKey != null && !secretKey.isEmpty()) {
            editor.putString(PREFIX_SECRET_KEY + providerKey, secretKey);
        }
        editor.apply();
        Log.d("PaymentAuthHelper", "Chaves de API salvas para (prefs key): " + providerKey);
    }


    // MÉTODOS GET (usam o credentialsProvider específico do flavor)
    public String getAccessToken(String defaultValue) {
        return credentialsProvider.getAccessToken(defaultValue);
    }

    public String getAccessToken() {
        return credentialsProvider.getAccessToken();
    }

    public String getClientId(String defaultValue) {
        return credentialsProvider.getClientId(defaultValue);
    }

    public String getClientId() {
        return credentialsProvider.getClientId();
    }

    public String getMerchantCode(String defaultValue) {
        return credentialsProvider.getMerchantCode(defaultValue);
    }

    public String getMerchantCode() {
        return credentialsProvider.getMerchantCode();
    }

    public String getApiKey(String defaultValue) {
        return credentialsProvider.getApiKey(defaultValue);
    }

    public String getApiKey() {
        return credentialsProvider.getApiKey();
    }

    public String getSecretKey(String defaultValue) {
        return credentialsProvider.getSecretKey(defaultValue);
    }

    public String getSecretKey() {
        return credentialsProvider.getSecretKey();
    }


    // MÉTODOS DE UTILIDADE (usam os getters que já usam o provider)
    public boolean hasBasicCredentials() {
        String token = getAccessToken();
        String clientId = getClientId();
        return token != null && !token.isEmpty() && clientId != null && !clientId.isEmpty();
    }

    public boolean hasApiKeys() {
        String apiKey = getApiKey();
        String secretKey = getSecretKey();
        return apiKey != null && !apiKey.isEmpty() && secretKey != null && !secretKey.isEmpty();
    }

    // MÉTODOS DE LIMPEZA (usam SharedPreferences com currentProviderNameForSaving)
    public void clearCredentials() {
        SharedPreferences.Editor editor = prefsForSaving.edit();
        String providerKey = this.currentProviderNameForSaving;
        editor.remove(PREFIX_ACCESS_TOKEN + providerKey);
        editor.remove(PREFIX_CLIENT_ID + providerKey);
        editor.remove(PREFIX_MERCHANT_CODE + providerKey);
        editor.remove(PREFIX_API_KEY + providerKey);
        editor.remove(PREFIX_SECRET_KEY + providerKey);
        editor.apply();
        Log.d("PaymentAuthHelper", "Credenciais removidas das prefs para (key): " + providerKey);
    }

    public static void clearAllCredentials(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        Log.d("PaymentAuthHelper", "TODAS as credenciais de pagamento foram removidas das SharedPreferences");
    }
}