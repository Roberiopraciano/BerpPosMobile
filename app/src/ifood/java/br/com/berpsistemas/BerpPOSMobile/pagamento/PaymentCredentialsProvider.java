package br.com.berpsistemas.BerpPOSMobile.pagamento;

// Esta classe será usada APENAS quando o flavor "generic" (ou outro default) for compilado.
public class PaymentCredentialsProvider implements IPaymentAuthCredentialsProvider {

    public PaymentCredentialsProvider(android.content.Context context) {
        // Construtor pode ser vazio
    }

    @Override
    public String getAccessToken(String defaultValue) {
        return defaultValue;
    }

    @Override
    public String getClientId(String defaultValue) {
        return defaultValue;
    }

    @Override
    public String getMerchantCode(String defaultValue) {
        return defaultValue;
    }

    @Override
    public String getApiKey(String defaultValue) {
        return defaultValue;
    }

    @Override
    public String getSecretKey(String defaultValue) {
        return defaultValue;
    }
}