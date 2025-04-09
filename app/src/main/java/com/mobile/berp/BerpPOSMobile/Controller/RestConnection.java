package com.mobile.berp.BerpPOSMobile.Controller;

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

    private static Retrofit retrofit;
    private static ApiService apiService;
   // private static final String BASE_URL = "/datasnap/rest/TServerMethods1/";
   private static final String BASE_URL = "";

    private RestConnection() {
        // Construtor privado para evitar instanciação
    }

    public static ApiService getApiService() throws Exception {
        if (apiService == null) {
            synchronized (RestConnection.class) {
                if (apiService == null) {
                    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // Loga corpo das requisições e respostas


                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(new Funcoes().getUrl(BASE_URL))
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    apiService = retrofit.create(ApiService.class);
                }
            }
        }
        return apiService;
    }

    public static <T> void get(String url, Class<T> responseType, ResponseCallback<T> callback) {
        Call<ResponseBody> call = null;
        try {
            call = getApiService().get(url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String conteudo = response.body().string();
                      //  Log.d("RESPOSTA",conteudo);
                        // Verifica se o tipo esperado é String e trata de forma apropriada
                        if (responseType.equals(String.class)) {
                            // Usa o Gson para desserializar a string, removendo aspas extras
                            // String parsedContent = new Gson().fromJson(conteudo, String.class);
                            callback.onSuccess(responseType.cast(conteudo));
                        } else {
                            // Faz o parse usando Gson para outros tipos
                            T parsedResponse = new Gson().fromJson(conteudo, responseType);
                            callback.onSuccess(parsedResponse);
                        }
                    } catch (Exception e) {
                        callback.onError(new Exception("Erro ao parsear a resposta: " + e.getMessage()));
                    }
                } else {
                    callback.onError(new Exception("Erro: " + response.message()));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onError(new Exception("Erro de conexão: " + t.getMessage()));
            }
        });
    }

    // Novo método POST
    public static <T> void post(String url, String jsonBody, Class<T> responseType, ResponseCallback<T> callback) {
        try {
            RequestBody body = RequestBody.create(jsonBody, okhttp3.MediaType.parse("application/json"));




            Call<ResponseBody> call = getApiService().post(url, body);
            Log.d("URL: " , call.request().url().toString());

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String conteudo = response.body().string();
                            Log.d("RESPOSTA",conteudo);
                            // Verifica se o tipo esperado é String e trata de forma apropriada
                            if (responseType.equals(String.class)) {
                                // Usa o Gson para desserializar a string, removendo aspas extras
                               // String parsedContent = new Gson().fromJson(conteudo, String.class);
                                callback.onSuccess(responseType.cast(conteudo));
                            } else {
                                // Faz o parse usando Gson para outros tipos
                                T parsedResponse = new Gson().fromJson(conteudo, responseType);
                                callback.onSuccess(parsedResponse);
                            }
                        } catch (Exception e) {
                            callback.onError(new Exception("Erro ao parsear a resposta: " + e.getMessage()));
                        }
                    } else {
                        callback.onError(new Exception("Erro: " + response.message()));
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.d("ERRO",t.getMessage());
                    callback.onError(new Exception("Erro de conexão: " + t.getMessage()));
                }
            });

        } catch (Exception e) {
            throw new RuntimeException("Erro ao fazer a requisição POST: " + e.getMessage(), e);
        }
    }

    public interface ResponseCallback<T> {
        void onSuccess(T response);
        void onError(Exception e);
    }
}