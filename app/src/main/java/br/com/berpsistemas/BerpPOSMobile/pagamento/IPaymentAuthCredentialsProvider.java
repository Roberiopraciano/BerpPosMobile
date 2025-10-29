package br.com.berpsistemas.BerpPOSMobile.pagamento;

public interface IPaymentAuthCredentialsProvider {
    String getAccessToken(String defaultValue);
    String getClientId(String defaultValue);
    String getMerchantCode(String defaultValue); // Se aplicável a todos
    String getApiKey(String defaultValue);       // Se aplicável a todos
    String getSecretKey(String defaultValue);    // Se aplicável a todos

    // Métodos utilitários podem ser adicionados se forem comuns
    default String getAccessToken() { return getAccessToken(null); }
    default String getClientId() { return getClientId(null); }
    default String getMerchantCode() { return getMerchantCode(null); }
    default String getApiKey() { return getApiKey(null); }
    default String getSecretKey() { return getSecretKey(null); }
}