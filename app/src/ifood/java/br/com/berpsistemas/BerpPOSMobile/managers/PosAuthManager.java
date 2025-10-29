package br.com.berpsistemas.BerpPOSMobile.managers;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;
import java.nio.charset.StandardCharsets;

public class PosAuthManager implements IPosAuthManager {
    private static final String TAG = "IFoodAuthManager";
    private static final String PREFS_NAME = "IFoodAuthPrefs";
    private static final String KEY_AUTH_TOKEN = "ifood_auth_token";
    private static final String KEY_TOKEN_CREATION_TIME_MS = "ifood_token_creation_time";
    private static final long TOKEN_VALIDITY_DURATION_MS = 23 * 60 * 60 * 1000; // 23 horas para margem

    private Context appContext;
    private SharedPreferences sharedPreferences;
    private AuthCallback pendingAuthCallback; // Para guardar o callback enquanto o deeplink está ativo

    // Constantes do iFood (poderiam vir de um arquivo de configuração do flavor)
    private static final String IFOOD_AUTH_DEEPLINK_BASE = "https://portal.ifood.com.br/print-file";
    private static final String YOUR_APP_INTEGRATION_ID = "BerpPOSMobileApp"; // Defina um ID para sua integração
    private static final String YOUR_APP_RETURN_SCHEME = "berppos";
    private static final String YOUR_APP_RETURN_HOST = "ifood-print-auth-result";
    public static final String URL_TO_RETURN_FOR_IFOOD_PRINT_AUTH = YOUR_APP_RETURN_SCHEME + "://" + YOUR_APP_RETURN_HOST + "?result=";


    @Override
    public void requestRefund(Activity activity, String originalTransactionId, int amountInCentsToRefund, RefundCallback refundCallback) {

    }

    @Override
    public void initialize(Context context, AuthCallback callback) {
        this.appContext = context.getApplicationContext();
        this.sharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Log.d(TAG, "IFoodPosAuthManager inicializado.");
        // Verifica se já tem token válido. Se sim, considera inicializado com sucesso.
        if (isAuthenticated()) {
            Log.d(TAG, "Token iFood já existente e válido.");
            if (callback != null) callback.onSuccess();
        } else {
            Log.d(TAG, "Nenhum token iFood válido encontrado na inicialização.");

            if (callback != null) callback.onSuccess(); // Sucesso na inicialização, mas não necessariamente autenticado
        }
    }

    @Override
    public boolean isAuthenticated() {
        String token = sharedPreferences.getString(KEY_AUTH_TOKEN, null);
        long creationTimeMs = sharedPreferences.getLong(KEY_TOKEN_CREATION_TIME_MS, 0);
        if (token != null && (System.currentTimeMillis() - creationTimeMs) < TOKEN_VALIDITY_DURATION_MS) {
            return true;
        }
        return false;
    }

    @Override
    public String getCurrentToken() {
        if (isAuthenticated()) {
            return sharedPreferences.getString(KEY_AUTH_TOKEN, null);
        }
        return null;
    }

    @Override
    public void requestAuthentication(Activity activity, AuthCallback callback) {
        this.pendingAuthCallback = callback;
        try {
            JSONObject contentJson = new JSONObject();
            contentJson.put("integrationApp", YOUR_APP_INTEGRATION_ID);
            contentJson.put("urlToReturn", URL_TO_RETURN_FOR_IFOOD_PRINT_AUTH);
            contentJson.put("sendResultInSameIntent", false);

            String base64Content = Base64.encodeToString(contentJson.toString().getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
            String deeplinkUrl = IFOOD_AUTH_DEEPLINK_BASE + "?content=" + base64Content;

            Log.d(TAG, "Solicitando autenticação iFood com deeplink: " + deeplinkUrl);

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(deeplinkUrl));
            // Adicionar flags pode ser útil dependendo de como o app do iFood lida com o Intent
            // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Se o app do iFood rodar em outra task
            // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Para limpar o topo da stack se ele já estiver aberto

            // Simplesmente inicia a Activity, assumindo que o app do iFood está lá
            activity.startActivity(intent);
            // O resultado virá para handleAuthenticationResult via PrintCallbackActivity

            // A verificação `resolveActivity` foi removida conforme solicitado.
            // Se, por algum motivo, o app do iFood não estiver instalado ou habilitado
            // para este deeplink, um ActivityNotFoundException será lançado aqui.
            // O bloco catch (Exception e) abaixo já lidaria com isso.

        } catch (android.content.ActivityNotFoundException anfe) {
            // Trata especificamente o caso de ActivityNotFoundException
            Log.e(TAG, "App da Maquinona iFood não encontrado para lidar com o deeplink: " + anfe.getMessage());
            if (pendingAuthCallback != null) {
                pendingAuthCallback.onError("App da Maquinona iFood não encontrado ou não configurado para este link.");
                pendingAuthCallback = null;
            }
        } catch (Exception e) {
            // Captura outras exceções (JSONException, etc.)
            Log.e(TAG, "Erro ao solicitar autenticação iFood: " + e.getMessage(), e);
            if (pendingAuthCallback != null) {
                pendingAuthCallback.onError("Erro ao iniciar autenticação: " + e.getMessage());
                pendingAuthCallback = null;
            }
        }
    }


    @Override
    public void handleAuthenticationResult(Context context, Uri intentData) {
        // Este método será chamado pela sua PrintCallbackActivity
        if (intentData == null) {
            Log.e(TAG, "Dados do intent de autenticação nulos.");
            if (pendingAuthCallback != null) pendingAuthCallback.onError("Dados de autenticação inválidos.");
            pendingAuthCallback = null;
            return;
        }

        String resultBase64 = intentData.getQueryParameter("result");
        if (resultBase64 == null || resultBase64.isEmpty()) {
            Log.e(TAG, "Parâmetro 'result' da autenticação iFood nulo ou vazio.");
            if (pendingAuthCallback != null) pendingAuthCallback.onError("Resposta de autenticação inválida.");
            pendingAuthCallback = null;
            return;
        }

        try {
            String decodedResultJson = new String(Base64.decode(resultBase64, Base64.DEFAULT), StandardCharsets.UTF_8);
            Log.d(TAG, "iFood auth response JSON: " + decodedResultJson);
            JSONObject resultJson = new JSONObject(decodedResultJson);

            String status = resultJson.optString("status");
            String hash = resultJson.optString("hash");

            if ("SUCCESS".equalsIgnoreCase(status) && hash != null && !hash.isEmpty()) {
                Log.i(TAG, "Autenticação iFood CONCEDIDA. Token: " + hash);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(KEY_AUTH_TOKEN, hash);
                editor.putLong(KEY_TOKEN_CREATION_TIME_MS, System.currentTimeMillis()); // Usa o tempo atual
                editor.apply();
                if (pendingAuthCallback != null) {
                    pendingAuthCallback.onSuccess();
                }
            } else {
                String errorMessage = resultJson.optString("message", "Status: " + status);
                Log.e(TAG, "Autenticação iFood FALHOU. " + errorMessage);
                if (pendingAuthCallback != null) {
                    pendingAuthCallback.onError("Falha na autenticação: " + errorMessage);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar resposta de autenticação iFood: " + e.getMessage(), e);
            if (pendingAuthCallback != null) {
                pendingAuthCallback.onError("Erro ao processar resposta de autenticação.");
            }
        } finally {
            pendingAuthCallback = null; // Limpa o callback pendente
        }
    }
}