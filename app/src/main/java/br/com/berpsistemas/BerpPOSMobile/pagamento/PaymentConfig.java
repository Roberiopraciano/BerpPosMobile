package br.com.berpsistemas.BerpPOSMobile.pagamento;

public class PaymentConfig {
    // Campos da sua versão original
    private int amountInCents;         // Renomeado de 'amount' para clareza, continua em centavos
    private String transactionType;    // "credit", "debit", "voucher", "pix" (será convertido para maiúsculas para iFood)
    private String orderId;            // Identificador único do pedido (será usado como transactionId para iFood)
    private String currencyCode;       // Mantido por compatibilidade, mas não usado pelo iFood deeplink
    private String activationCode;     // Mantido por compatibilidade, mas não usado pelo iFood deeplink

    // Campos adicionais/específicos para iFood
    private String urlToReturn;        // Obrigatório para iFood
    private int installments;          // Opcional para iFood (crédito)
    private boolean editableValue;     // Opcional para iFood

    /**
     * Construtor principal para uso com iFood (e outros que se adaptem).
     *
     * @param amountInCents Valor da transação em centavos.
     * @param transactionType Tipo de transação (ex: "credit", "debit", "pix" - será normalizado).
     * @param orderId Identificador único do seu pedido.

     */
    public PaymentConfig(int amountInCents, String transactionType, String orderId) {
        this.amountInCents = amountInCents;
        this.transactionType = transactionType;
        this.orderId = orderId;
        this.urlToReturn = urlToReturn;

        // Valores padrão
        this.currencyCode = "986";  // Padrão Real (BRL)
        this.activationCode = "";   // Não aplicável ao iFood deeplink
        this.installments = 1;      // Padrão para crédito à vista ou não aplicável
        this.editableValue = false; // Padrão
    }

    // Getters
    public int getAmountInCents() {
        return amountInCents;
    }

    /**
     * @return Tipo de transação (ex: "credit", "debit"). Para iFood, use getIFoodPaymentType().
     */
    public String getTransactionType() {
        return transactionType;
    }

    /**
     * @return Tipo de pagamento formatado para a API do iFood (maiúsculas).
     */
    public String getIFoodPaymentType() {
        if (this.transactionType == null) return "";
        return this.transactionType.toUpperCase();
    }

    public String getOrderId() {
        return orderId;
    }

    /**
     * @return Código da moeda (ex: "986" para BRL). Não usado diretamente no deeplink iFood.
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * @return Código de ativação. Não usado no deeplink iFood.
     */
    public String getActivationCode() {
        // Se este valor é fixo como no seu exemplo original, pode retornar diretamente.
        // return "749879";
        // Ou retornar o valor do campo se ele puder ser definido.
        return activationCode;
    }

    public String getUrlToReturn() {
        return urlToReturn;
    }

    public int getInstallments() {
        return installments;
    }

    public boolean isEditableValue() {
        return editableValue;
    }

    // Setters (alguns mantidos por compatibilidade, outros para configurar iFood)
    public PaymentConfig setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
        return this;
    }

    public PaymentConfig setActivationCode(String activationCode) {
        this.activationCode = activationCode;
        return this;
    }

    public PaymentConfig setInstallments(int installments) {
        if (installments < 1) {
            this.installments = 1; // Mínimo de 1 parcela
        } else {
            this.installments = installments;
        }
        return this;
    }

    public PaymentConfig setEditableValue(boolean editableValue) {
        this.editableValue = editableValue;
        return this;
    }

    // Método utilitário
    public double getAmountInReais() {
        return amountInCents / 100.0;
    }
}