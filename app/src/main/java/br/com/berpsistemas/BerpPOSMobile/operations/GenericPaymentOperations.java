package br.com.berpsistemas.BerpPOSMobile.operations;

import android.content.Context;
import android.util.Log;
import br.com.berpsistemas.BerpPOSMobile.model.TransactionModel;
import br.com.berpsistemas.BerpPOSMobile.BuildConfig;

/**
 * SISTEMA ULTRA SIMPLIFICADO
 *
 * Cada flavor tem sua própria PaymentStrategy
 * Não precisa de Map nem detecção de acquirer
 */
public class GenericPaymentOperations {

    private static final String TAG = "GenericPaymentOps";
    private static GenericPaymentOperations instance;

    private PaymentOperationListener listener;
    private final PaymentOperationHandler strategy;

    public static synchronized GenericPaymentOperations getInstance() {
        if (instance == null) {
            instance = new GenericPaymentOperations();
        }
        return instance;
    }

    private GenericPaymentOperations() {
        // Cada flavor tem sua própria implementação de PaymentStrategy
        // O sistema de build escolhe automaticamente a versão correta
        strategy = new br.com.berpsistemas.BerpPOSMobile.operations.PaymentOperationHandler();
    }

    public void setOperationListener(PaymentOperationListener listener) {
        this.listener = listener;
    }

    /**
     * Reimpressão - usa automaticamente a strategy do flavor atual
     */
    public void reprintReceipt(Context context, TransactionModel transaction, String receiptType) {
        try {
            Log.d(TAG, "Iniciando reimpressão - flavor: " + BuildConfig.FLAVOR);

            if (!strategy.canReprint(transaction)) {
                notifyError("Transação não pode ser reimpressa");
                return;
            }

            notifyOperationStarted("Iniciando reimpressão...");
            strategy.reprintReceipt(context, transaction, receiptType, new OperationCallback() {
                @Override
                public void onSuccess(String message) {
                    notifyOperationCompleted("Comprovante reimpress com sucesso!");
                }

                @Override
                public void onError(String error) {
                    notifyError("Erro na reimpressão: " + error);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Erro na reimpressão", e);
            notifyError("Erro interno na reimpressão");
        }
    }

    /**
     * Cancelamento - usa automaticamente a strategy do flavor atual
     */
    public void cancelTransaction(Context context, TransactionModel transaction, String reason) {
        try {
            Log.d(TAG, "Iniciando cancelamento - flavor: " + BuildConfig.FLAVOR);

            if (!strategy.canCancel(transaction)) {
                notifyError("Transação não pode ser cancelada");
                return;
            }

            notifyOperationStarted("Processando cancelamento...");
            strategy.cancelTransaction(context, transaction, reason, new OperationCallback() {
                @Override
                public void onSuccess(String message) {
                    notifyOperationCompleted("Transação cancelada com sucesso!");
                }

                @Override
                public void onError(String error) {
                    notifyError("Erro no cancelamento: " + error);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Erro no cancelamento", e);
            notifyError("Erro interno no cancelamento");
        }
    }

    /**
     * Verifica se transação pode ser reimpressa
     */
    public boolean canReprint(TransactionModel transaction) {
        return strategy.canReprint(transaction);
    }

    /**
     * Verifica se transação pode ser cancelada
     */
    public boolean canCancel(TransactionModel transaction) {
        return strategy.canCancel(transaction);
    }

    private void notifyOperationStarted(String message) {
        if (listener != null) {
            listener.onOperationStarted(message);
        }
    }

    private void notifyOperationCompleted(String message) {
        if (listener != null) {
            listener.onOperationCompleted(message);
        }
    }

    private void notifyError(String error) {
        if (listener != null) {
            listener.onOperationError(error);
        }
    }

    // =================== INTERFACES ===================

    public interface PaymentOperationListener {
        void onOperationStarted(String message);
        void onOperationCompleted(String message);
        void onOperationError(String error);
    }

    public interface OperationCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface PaymentOperationHandler {
        boolean canReprint(TransactionModel transaction);
        boolean canCancel(TransactionModel transaction);
        void reprintReceipt(Context context, TransactionModel transaction, String receiptType, OperationCallback callback);
        void cancelTransaction(Context context, TransactionModel transaction, String reason, OperationCallback callback);
    }
}