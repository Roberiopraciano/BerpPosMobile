package br.com.berpsistemas.BerpPOSMobile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// 1. Importe a interface IPosAuthManager
import br.com.berpsistemas.BerpPOSMobile.managers.IPosAuthManager; // Supondo que a interface está neste pacote

// 2. Importe sua classe Application (substitua pelo nome real)
import br.com.berpsistemas.BerpPOSMobile.application.MyBerpApplication; // Exemplo de nome da sua classe Application


public class PrintCallbackActivity extends AppCompatActivity {

    private static final String TAG = "CallbackActivity"; // Nome mais genérico, já que pode ser para Auth

    private IPosAuthManager posAuthManager;

    // Defina as constantes do scheme e host que esta Activity espera.
    // Idealmente, seriam configuradas via BuildConfig por flavor,
    // ou o Manifest já filtraria corretamente.
    // Para o iFood, seriam:
    private static final String EXPECTED_SCHEME = "berppos";
    private static final String EXPECTED_HOST = "ifood-print-auth-result";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Recebendo callback.");

        // 3. Obter a instância do IPosAuthManager da sua classe Application
        // Certifique-se de que MyBerpApplication existe e tem o método getPosAuthManager()
        if (getApplication() instanceof MyBerpApplication) {
            posAuthManager = ((MyBerpApplication) getApplication()).getPosAuthManager();
        }

        if (posAuthManager == null) {
            Log.e(TAG, "IPosAuthManager não está disponível. Finalizando.");
            Toast.makeText(this, "Erro interno: Serviço de autenticação não configurado.", Toast.LENGTH_LONG).show();
            finishAndNavigateBack();
            return;
        }

        processIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent: Recebendo novo callback.");
        setIntent(intent); // Atualiza o intent que será retornado por getIntent()

        // Re-inicializa o posAuthManager caso a Activity tenha sido recriada ou o app reiniciado
        // e o onNewIntent seja chamado antes de um onCreate completo.
        if (posAuthManager == null && getApplication() instanceof MyBerpApplication) {
            posAuthManager = ((MyBerpApplication) getApplication()).getPosAuthManager();
        }

        if (posAuthManager == null) {
            Log.e(TAG, "IPosAuthManager não está disponível no onNewIntent. Finalizando.");
            Toast.makeText(this, "Erro interno: Serviço de autenticação não configurado.", Toast.LENGTH_LONG).show();
            finishAndNavigateBack();
            return;
        }

        processIntent(intent);
    }

    private void processIntent(Intent intent) {
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();

            // 4. Validação do Scheme e Host (o AndroidManifest.xml já deve filtrar, mas é uma segurança extra)
            if (data != null &&
                    EXPECTED_SCHEME.equals(data.getScheme()) &&
                    EXPECTED_HOST.equals(data.getHost())) {

                // O parâmetro 'result' é específico do iFood.
                // A interface IPosAuthManager e sua implementação devem lidar com a extração
                // dos parâmetros corretos do 'data' (Uri).
                Log.d(TAG, "Callback recebido com scheme/host esperados. Uri: " + data.toString());

                // 5. Chamar o método correto no IPosAuthManager
                // A implementação do IPosAuthManager (ex: IFoodPosAuthManager)
                // será responsável por extrair o parâmetro "result" e processá-lo.
                posAuthManager.handleAuthenticationResult(this, data);

            } else {
                Log.e(TAG, "Intent de callback não corresponde ao scheme/host esperado. Uri: " + (data != null ? data.toString() : "null"));
                Toast.makeText(this, "Callback inválido.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "Intent inválido ou não é ACTION_VIEW. Action: " + (intent != null ? intent.getAction() : "null intent"));
            // Não mostrar Toast aqui, pois pode ser chamada sem ser por deeplink em alguns cenários de ciclo de vida.
        }

        // 6. Sempre finalizar esta Activity após o processamento.
        // A lógica de navegação ou atualização de UI deve ser tratada pelo callback do IPosAuthManager.
        finishAndNavigateBack();
    }


    private void finishAndNavigateBack() {
        Log.d(TAG, "Finalizando " + getClass().getSimpleName() + ".");
        finish();
    }
}