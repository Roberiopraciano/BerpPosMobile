package br.com.berpsistemas.BerpPOSMobile.pagamento;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import br.com.berpsistemas.BerpPOSMobile.model.PagamentoModel;


public class PagamentoPos implements IPagamento {

    private static final String TAG = "PagamentoPagSeguro";
    // Nome do pacote do serviço – verifique se está correto e se o app está instalado.
    private static final String PLUGPAG_SERVICE_PACKAGE_NAME = "br.com.uol.pagseguro.plugpagservice";

    private final Activity activity;

    public PagamentoPos(Context context) {

        if (context instanceof Activity) {
            this.activity = (Activity) context;
        } else {

            throw new IllegalArgumentException("Contexto passado não é uma Activity!");
        }
        Log.d(TAG, "PagamentoPos: Iniciando");




        // Supondo que 'context' já esteja definido

        



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

    @Override
    public void setCallback(IPagamentoCallback callback) {

    }

    @Override
    public void realizarReembolso(Activity activity, PagamentoModel pag) {

    }


}