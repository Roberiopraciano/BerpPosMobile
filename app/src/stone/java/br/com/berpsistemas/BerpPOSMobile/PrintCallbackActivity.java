package br.com.berpsistemas.BerpPOSMobile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.shashank.sony.fancytoastlib.FancyToast;

public class PrintCallbackActivity extends Activity {

    private static final String TAG = "PrintCallbackActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Processa o callback da Stone
        Intent intent = getIntent();
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                processStoneCallback(data);
            }
        }

        // Fecha a activity após processar
        finish();
    }

    private void processStoneCallback(Uri data) {
        try {
            Log.d(TAG, "Callback recebido: " + data.toString());

            // Extrai o parâmetro DEEPLINK_RETURN que é o que a Stone envia
            String deeplinkReturn = data.getQueryParameter("DEEPLINK_RETURN");

            Log.d(TAG, "DEEPLINK_RETURN: " + deeplinkReturn);

            if (deeplinkReturn != null) {
                switch (deeplinkReturn.toUpperCase()) {
                    case "SUCCESS":
                        showSuccessMessage("Impressão realizada com sucesso!");
                        Log.i(TAG, "Impressão realizada com sucesso");
                        break;

                    case "PRINTER_OUT_OF_PAPER":
                        showErrorMessage("Impressora sem papel ou com a tampa de bobina aberta");
                        Log.e(TAG, "Erro: Impressora sem papel - " + deeplinkReturn);
                        break;

                    case "PRINTER_INIT_ERROR":
                        showErrorMessage("Erro ao inicializar a impressora");
                        Log.e(TAG, "Erro: Falha na inicialização - " + deeplinkReturn);
                        break;

                    case "PRINTER_LOW_ENERGY":
                        showErrorMessage("Máquina com baixa energia");
                        Log.e(TAG, "Erro: Bateria baixa - " + deeplinkReturn);
                        break;

                    case "PRINTER_BUSY":
                        showErrorMessage("Impressora ocupada. Aguarde e tente novamente");
                        Log.w(TAG, "Aviso: Impressora ocupada - " + deeplinkReturn);
                        break;

                    case "PRINTER_UNSUPPORTED_FORMAT":
                        showErrorMessage("Formato de impressão não suportado");
                        Log.e(TAG, "Erro: Formato inválido - " + deeplinkReturn);
                        break;

                    case "PRINTER_INVALID_DATA":
                        showErrorMessage("Dados de impressão inválidos (limite do buffer ultrapassado)");
                        Log.e(TAG, "Erro: Dados inválidos - " + deeplinkReturn);
                        break;

                    case "PRINTER_OVERHEATING":
                        showErrorMessage("Impressora superaquecida. Aguarde esfriar");
                        Log.e(TAG, "Erro: Superaquecimento - " + deeplinkReturn);
                        break;

                    case "PRINTER_PAPER_JAM":
                        showErrorMessage("Papel preso na impressora");
                        Log.e(TAG, "Erro: Papel travado - " + deeplinkReturn);
                        break;

                    case "PRINTER_PRINT_ERROR":
                        showErrorMessage("Erro genérico da impressora");
                        Log.e(TAG, "Erro: Erro genérico - " + deeplinkReturn);
                        break;

                    case "CANCELLED":
                    case "CANCEL":
                        showInfoMessage("Impressão cancelada pelo usuário");
                        Log.w(TAG, "Impressão cancelada - " + deeplinkReturn);
                        break;

                    default:
                        showInfoMessage("Status de impressão: " + deeplinkReturn);
                        Log.i(TAG, "Status não mapeado: " + deeplinkReturn);
                        break;
                }
            } else {
                // Fallback: verifica outros parâmetros possíveis
                Log.w(TAG, "DEEPLINK_RETURN não encontrado, verificando outros parâmetros...");

                // Lista todos os parâmetros para debug
                if (data.getQueryParameterNames() != null) {
                    for (String paramName : data.getQueryParameterNames()) {
                        String paramValue = data.getQueryParameter(paramName);
                        Log.d(TAG, "Parâmetro encontrado: " + paramName + " = " + paramValue);
                    }
                }

                showInfoMessage("Callback recebido da Stone, mas formato não reconhecido");
            }

        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar callback da Stone", e);
            showErrorMessage("Erro ao processar resposta da impressora");
        }
    }

    private void showSuccessMessage(String message) {
        FancyToast.makeText(this, message, FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false).show();
    }

    private void showErrorMessage(String message) {
        FancyToast.makeText(this, message, FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
    }

    private void showInfoMessage(String message) {
        FancyToast.makeText(this, message, FancyToast.LENGTH_LONG, FancyToast.INFO, false).show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Para o caso de launchMode="singleTop"
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                processStoneCallback(data);
            }
        }

        finish();
    }
}