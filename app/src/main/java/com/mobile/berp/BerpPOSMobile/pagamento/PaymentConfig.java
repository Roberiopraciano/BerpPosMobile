package com.mobile.berp.BerpPOSMobile.pagamento;

public class PaymentConfig {
    private int amount;              // em centavos (ex.: 10000 = R$100,00)
    private String transactionType;  // "credit", "debit", "voucher", "pix"
    private String orderId;          // Identificador único do pedido
    private String currencyCode;     // Ex.: "986" (Real); pode ter default
    private String activationCode;   // Código de ativação do terminal

    // Construtor que recebe o código de ativação
    public PaymentConfig(int amount, String transactionType, String orderId, String activationCode) {
        this.amount = amount;
        this.transactionType = transactionType;
        this.orderId = orderId;
        this.currencyCode = "986";  // valor padrão para Real
        this.activationCode = activationCode;
    }

    // Construtor sem o código de ativação (opcional)
    public PaymentConfig(int amount, String transactionType, String orderId) {
        this(amount, transactionType, orderId, "");
    }

    // Getters e setters
    public int getAmount() {
        return amount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    // Método para retornar o código de ativação
    public String getActivationCode() {
        return "749879";
    }

    // Método para definir o código de ativação, se necessário atualizar depois
    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }
}