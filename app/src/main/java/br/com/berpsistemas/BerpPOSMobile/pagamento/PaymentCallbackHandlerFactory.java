package br.com.berpsistemas.BerpPOSMobile.pagamento;

public class PaymentCallbackHandlerFactory {
    public static PaymentCallbackHandler getHandler() {
        return PaymentCallbackHandler.getInstance();
    }
}