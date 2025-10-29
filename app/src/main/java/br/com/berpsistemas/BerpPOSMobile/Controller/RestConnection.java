package br.com.berpsistemas.BerpPOSMobile.Controller;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.google.gson.Gson;

public class RestConnection {

    private static final String TAG = "RestConnection";
    private static Retrofit retrofit;
    private static ApiService apiService;
    // deixamos BASE_URL vazio, pois pegamos o valor real em tempo de execução
    private static final String BASE_URL = "";

    private RestConnection() { /* impede instanciação */ }

    // Cria/configura o Retrofit apenas uma vez, se a URL base for válida
    private static synchronized ApiService getApiService() {
        if (apiService != null) {
            return apiService;
        }

        String baseUrl = new Funcoes().getUrl(BASE_URL);
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            Log.w(TAG, "getApiService: baseUrl não configurada, cancelando setup de Retrofit");
            return null;
        }

        try {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);
            return apiService;
        } catch (Exception e) {
            // Loga e retorna null para não explodir a aplicação
            Log.e(TAG, "Erro ao criar Retrofit: " + e.getMessage(), e);
            return null;
        }
    }

    public static <T> void get(String url, Class<T> responseType, ResponseCallback<T> callback) {
        ApiService svc = getApiService();
        if (svc == null) {
            // não está configurado, devolve erro via callback, mas não lança exceção
            Log.w(TAG, "GET cancelado: ApiService não disponível");
            callback.onError(new Exception("Servidor não configurado. Verifique IP e Porta."));
            return;
        }

        Call<ResponseBody> call = svc.get(url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String conteudo = response.body().string();
                        if (responseType.equals(String.class)) {
                            callback.onSuccess(responseType.cast(conteudo));
                        } else {
                            T parsed = new Gson().fromJson(conteudo, responseType);
                            callback.onSuccess(parsed);
                        }
                    } catch (Exception e) {
                        callback.onError(new Exception("Erro ao parsear resposta: " + e.getMessage()));
                    }
                } else {
                    callback.onError(new Exception("Erro HTTP: " + response.message()));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onError(new Exception("Falha de conexão: " + t.getMessage()));
            }
        });
    }

    public static <T> void post(String url, String jsonBody, Class<T> responseType, ResponseCallback<T> callback) {
        ApiService svc = getApiService();
        if (svc == null) {
            Log.w(TAG, "POST cancelado: ApiService não disponível");
            callback.onError(new Exception("Servidor não configurado. Verifique IP e Porta."));
            return;
        }

        RequestBody body = RequestBody.create(jsonBody, okhttp3.MediaType.parse("application/json"));
        Call<ResponseBody> call = svc.post(url, body);
        Log.d(TAG, "POST URL: " + call.request().url());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String conteudo = response.body().string();
                        if (responseType.equals(String.class)) {
                            callback.onSuccess(responseType.cast(conteudo));
                        } else {
                            T parsed = new Gson().fromJson(conteudo, responseType);
                            callback.onSuccess(parsed);
                        }
                    } catch (Exception e) {
                        callback.onError(new Exception("Erro ao parsear resposta: " + e.getMessage()));
                    }
                } else {
                    callback.onError(new Exception("Erro HTTP: " + response.message()));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onError(new Exception("Falha de conexão: " + t.getMessage()));
            }
        });
    }

    public interface ResponseCallback<T> {
        void onSuccess(T response);
        void onError(Exception e);
    }
}
