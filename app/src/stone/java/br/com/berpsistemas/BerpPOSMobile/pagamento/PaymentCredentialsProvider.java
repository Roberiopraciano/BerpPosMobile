package br.com.berpsistemas.BerpPOSMobile.pagamento;

import br.com.berpsistemas.BerpPOSMobile.model.Variaveis; // Importe sua classe Variaveis


// Esta classe será usada APENAS quando o flavor "cielo" for compilado.
public class PaymentCredentialsProvider implements IPaymentAuthCredentialsProvider {

    // Construtor pode ser vazio ou receber Context se Variaveis.getConfiguracao precisar.
    public PaymentCredentialsProvider(android.content.Context context) {
        // Inicializar Variaveis se necessário, ou assumir que já está pronto
    }

    @Override
    public String getAccessToken(String defaultValue) {
        // A lógica original para Cielo
        String token = Variaveis.getConfiguracao("MOBILE_CIELO_CLIENT_TOKEN", "H8tiTQWi04OyE9YQQQklLDqBHYUrytZk1k6mQsBxHuXlFzvXS1").getValor();
        return (token != null && !token.isEmpty()) ? token : defaultValue;
    }

    @Override
    public String getClientId(String defaultValue) {
        // A lógica original para Cielo
        String clientId = Variaveis.getConfiguracao("MOBILE_CIELO_CLIENT_ID", "2PI5DICbOwAyHNITv4oUdAH3OwCLPRbt6cBsf37f2NeU3TTtkx").getValor();
        return (clientId != null && !clientId.isEmpty()) ? clientId : defaultValue;
    }

    @Override
    public String getMerchantCode(String defaultValue) {
        // Se Cielo não usa Merchant Code dessa forma, ou se ele vem de Variaveis.getConfiguracao:
        // String mc = Variaveis.getConfiguracao("CIELO_MERCHANT_CODE", defaultValue).getValor();
        // return (mc != null && !mc.isEmpty()) ? mc : defaultValue;
        return defaultValue; // Ou implemente conforme necessário para Cielo
    }

    @Override
    public String getApiKey(String defaultValue) {
        return defaultValue; // Ou implemente conforme necessário para Cielo
    }

    @Override
    public String getSecretKey(String defaultValue) {
        return defaultValue; // Ou implemente conforme necessário para Cielo
    }
}