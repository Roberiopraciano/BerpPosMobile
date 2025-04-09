package com.mobile.berp.BerpPOSMobile.pagamento;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import br.com.uol.pagseguro.plugpagservice.wrapper.AppIdentification;
import br.com.uol.pagseguro.plugpagservice.wrapper.IPlugPagWrapper;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagActivationData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagInitializationResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPaymentData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrintResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.data.request.PlugPagBeepData;
import br.com.uol.pagseguro.plugpagservice.wrapper.listeners.PlugPagActivationListener;
import br.com.uol.pagseguro.plugpagservice.wrapper.listeners.PlugPagIsActivatedListener;
import br.com.uol.pagseguro.plugpagservice.wrapper.listeners.PlugPagPaymentListener;

public class PagamentoPos implements IPagamento {

    private static final String TAG = "PagamentoPagSeguro";
    // Nome do pacote do serviço – verifique se está correto e se o app está instalado.
    private static final String PLUGPAG_SERVICE_PACKAGE_NAME = "br.com.uol.pagseguro.plugpagservice";

    private final Activity activity;
    private final IPlugPagWrapper plugPag;
    private final AppIdentification appIdentification;

    public PagamentoPos(Context context) {

        if (context instanceof Activity) {
            this.activity = (Activity) context;
        } else {

            throw new IllegalArgumentException("Contexto passado não é uma Activity!");
        }
        Log.d(TAG, "PagamentoPos: Iniciando");

        this.appIdentification = new AppIdentification(context);

        this.plugPag = new PlugPag(context);



        plugPag.setEventListener(eventData -> {
            if (eventData.getCustomMessage() != null) {
                Log.d(TAG, "Evento recebido: " + eventData.getCustomMessage());


            }
            // Aqui você pode implementar mais lógica dependendo do evento recebido.
        });


        // Supondo que 'context' já esteja definido

        PlugPagActivationListener listener = new PlugPagActivationListener() {
            @Override
            public void onActivationProgress(PlugPagEventData data) {
                Log.d(TAG, "onActivationProgress: "+data.toString());
            }

            @Override
            public void onSuccess(PlugPagInitializationResult result) {
                // Ação a ser executada ao ser finalizada uma ativação ou desativação com sucesso.
                Log.d(TAG, "onSuccess: "+result.toString());
            }

            @Override
            public void onError(PlugPagInitializationResult result) {
                Log.d(TAG, "onError: "+result.toString());
                // Ação a ser executada ao ocorrer uma falha na ativação ou desativação.
            }
        };

// Cria os dados de ativação usando o código de ativação informado
        PlugPagActivationData data = new PlugPagActivationData("749879");

// Chama o método assíncrono para inicializar e ativar o pinpad, passando os parâmetros
        plugPag.doAsyncInitializeAndActivatePinpad(data, listener);

        PlugPagIsActivatedListener listener2 = new PlugPagIsActivatedListener() {
            @Override
            public void onIsActivated(boolean isActivated) {
                Log.d(TAG, "onIsActivated: 1");
                // Ação a ser executada ao ser finalizada uma validação de usuário ativo.
            }

            @Override
            public void onError(String errorMessage) {
                Log.d(TAG, "onError: 2");
                // Ação a ser executada ao ocorrer uma falha na validação de usuário ativo.
            }
        };

// Chama o método asyncIsAuthenticated passando o listener criado
        plugPag.asyncIsAuthenticated(listener2);
        
        



    }

    /**
     * Verifica se o serviço do PlugPag está instalado e se está autenticado.
     */
    private boolean checkRequirements() {
        try {


            String Test= String.valueOf(activity.getPackageManager().getPackageInfo(PLUGPAG_SERVICE_PACKAGE_NAME, 0));
            Log.d(TAG, "checkRequirements: "+Test);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Serviço PlugPag não encontrado. Verifique a instalação.", e);
            return false;
        }
        return true;
    }

    public void beep() {
        for (int frequency = PlugPagBeepData.FREQUENCE_LEVEL_0; frequency <= PlugPagBeepData.FREQUENCE_LEVEL_6; frequency++) {
            plugPag.beep(new PlugPagBeepData((byte) frequency, 1));
        }
    }


    @Override
    public void iniciarPagamentoDeeplink(Activity activity, PaymentConfig config) {
        // O PagSeguro não suporta integração via deeplink – lança exceção.
        throw new UnsupportedOperationException("PagSeguro não suporta integração via deeplink. Use provider.");
    }


    @Override
    public void iniciarPagamentoProvider(Activity activity, PaymentConfig config) {
        // 1. Verifica os requisitos






        if (!checkRequirements()) {




            Log.e(TAG, "Requisitos não atendidos para realizar o pagamento.");
            return;
        }

        // 2. Configura os dados de pagamento conforme o tipo (crédito, débito, etc.)
        int tipoPagamento;
        String transactionType = config.getTransactionType().toLowerCase();
        switch (transactionType) {
            case "credit":
                tipoPagamento = PlugPag.TYPE_CREDITO;
                break;
            case "debit":
                tipoPagamento = PlugPag.TYPE_DEBITO;
                break;
            case "voucher":
                tipoPagamento = PlugPag.TYPE_VOUCHER;
                break;
            case "pix":
                tipoPagamento = PlugPag.TYPE_PIX;
                break;
            default:
                throw new IllegalArgumentException("Tipo de pagamento inválido para PagSeguro.");
        }

        // 3. Configura os dados de ativação
        String activationCode = config.getActivationCode();
        if (activationCode == null || activationCode.isEmpty()) {
            Log.e(TAG, "Código de ativação não informado no PaymentConfig.");
            return;
        }
        PlugPagActivationData activationData = new PlugPagActivationData(activationCode);

        // 4. Exemplo de chamada do getPackageInfo utilizando o contexto da aplicação
        String packageInfo = String.valueOf(appIdentification.getPackageInfo(activity.getApplicationContext()));
        Log.d(TAG, "Package Info: " + packageInfo);

        // 5. Ativa o pinpad (síncrono ou assíncrono conforme o SDK)
       // PlugPagInitializationResult initResult = plugPag.initializeAndActivatePinpad(activationData);
        //if (initResult.getResult() != PlugPag.RET_OK) {
        //    Log.e(TAG, "Falha na ativação do terminal: " + initResult.getResult());
        //    return;
        //}

        // 6. Configura os dados do pagamento
        final PlugPagPaymentData paymentData = new PlugPagPaymentData(
                tipoPagamento,
                config.getAmount(),
                PlugPag.INSTALLMENT_TYPE_A_VISTA,
                1,
                config.getOrderId()
        );


      //  beep();

        PlugPagPaymentListener olistner = new PlugPagPaymentListener() {
            @Override
            public void onSuccess(@NotNull PlugPagTransactionResult plugPagTransactionResult) {
                String msg = "onSuccess: " + plugPagTransactionResult.toString();
                Log.d(TAG, msg);
                sendPlugPagEvent(msg);
            }

            @Override
            public void onError(@NotNull PlugPagTransactionResult plugPagTransactionResult) {
                String msg = "onError: " + plugPagTransactionResult.toString();
                Log.d(TAG, msg);
                sendPlugPagEvent(msg);
            }

            @Override
            public void onPaymentProgress(@NotNull PlugPagEventData plugPagEventData) {
                String msg = "onPaymentProgress: " + plugPagEventData.getCustomMessage();
                Log.d(TAG, msg);
                sendPlugPagEvent(msg);
            }

            @Override
            public void onPrinterSuccess(@NotNull PlugPagPrintResult plugPagPrintResult) {
                String msg = "onPrinterSuccess: " + plugPagPrintResult.getMessage();
                Log.d(TAG, msg);
                sendPlugPagEvent(msg);
            }

            @Override
            public void onPrinterError(@NotNull PlugPagPrintResult plugPagPrintResult) {
                String msg = "onPrinterError: " + plugPagPrintResult.getMessage();
                Log.d(TAG, msg);
                sendPlugPagEvent(msg);
            }
        };


         plugPag.doAsyncPayment(paymentData,  olistner);



    }
    private void sendPlugPagEvent(String message) {
        Intent intent = new Intent("com.myapp.PAGSEGURO_EVENT");
        intent.putExtra("plugpag_message", message);
        LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
    }




    @Override
    public void processarResultado(int requestCode, int resultCode, Intent data) {
        // Se no futuro o SDK passar a usar onActivityResult, implemente o processamento dos dados.
        Log.d(TAG, "processarResultado chamado, mas o pagamento é síncrono.");
    }


}