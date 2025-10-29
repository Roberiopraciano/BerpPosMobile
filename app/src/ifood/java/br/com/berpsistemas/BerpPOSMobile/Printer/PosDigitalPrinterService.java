package br.com.berpsistemas.BerpPOSMobile.Printer;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import br.com.berpsistemas.BerpPOSMobile.RestaurantBillGenerator;
import br.com.berpsistemas.BerpPOSMobile.application.MyBerpApplication;
import br.com.berpsistemas.BerpPOSMobile.managers.IPosAuthManager;
import br.com.berpsistemas.BerpPOSMobile.model.ContaFields;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class PosDigitalPrinterService implements IPrinterService {
    private Context context;
    private static final String TAG = "MaquinonaPrinterSvcInt";
    private static final String IFOOD_PRINT_API_URL = "https://movilepay-api.ifood.com.br/ifoodpay/mobile/api/v1/print/file";

    private IPosAuthManager posAuthManager;
    private PrinterStatusCallback printerStatusCallback;

    @Override
    public void register(Context context, PrinterStatusCallback callback) {
        this.context = context;
        this.printerStatusCallback = callback;

        // Inicializa o AuthManager
        if (!initializeAuthManager()) {
            if (callback != null) {
                callback.onError(new IllegalStateException("Serviço de autenticação POS não pôde ser inicializado."));
            }
            return;
        }

        // Verifica se está autenticado
        if (!this.posAuthManager.isAuthenticated()) {
            Log.w(TAG, "IPosAuthManager obtido, mas não está autenticado. A impressão pode falhar.");
            if (callback != null) {
                callback.onError(new IllegalStateException("Serviço POS não autenticado."));
            }
            return;
        }

        // Tudo OK
        if (callback != null) {
            callback.onConnected();
        }

        Log.d(TAG, "PosDigitalPrinterService (iFood) registrado com sucesso.");
    }

    @Override
    public void print(ContaFields conta) {
        // Verifica se o AuthManager está inicializado e autenticado
        if (!initializeAuthManager() || !this.posAuthManager.isAuthenticated()) {
            Log.e(TAG, "Serviço POS não autenticado para impressão.");
            if (printerStatusCallback != null) {
                printerStatusCallback.onError(new IllegalStateException("Serviço POS não autenticado para impressão."));
            }
            return;
        }

        String token = posAuthManager.getCurrentToken();
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Token de autenticação é nulo ou vazio.");
            if (printerStatusCallback != null) {
                printerStatusCallback.onError(new IllegalStateException("Token de autenticação inválido."));
            }
            return;
        }

        // Processa a impressão
        proceedWithPrint(conta, token);
    }

    private boolean initializeAuthManager() {
        if (this.posAuthManager == null && this.context != null) {
            Context appContext = this.context.getApplicationContext();
            if (appContext instanceof MyBerpApplication) {
                this.posAuthManager = ((MyBerpApplication) appContext).getPosAuthManager();
                if (this.posAuthManager == null) {
                    Log.e(TAG, "IPosAuthManager obtido da MyBerpApplication é nulo!");
                    return false;
                }
                Log.d(TAG, "IPosAuthManager obtido com sucesso da MyBerpApplication.");
            } else {
                Log.e(TAG, "Contexto da aplicação não é uma instância de MyBerpApplication.");
                return false;
            }
        }
        return this.posAuthManager != null;
    }

    private void proceedWithPrint(ContaFields conta, String authToken) {
        try {
            // Gera o bitmap da conta
            RestaurantBillGenerator billGenerator = new RestaurantBillGenerator(context);
            billGenerator.setConta(conta);
            Bitmap bitmap = billGenerator.generateBillBitmap();

            if (bitmap == null) {
                Log.e(TAG, "Bitmap gerado é nulo.");
                if (printerStatusCallback != null) {
                    printerStatusCallback.onError(new Exception("Falha ao gerar imagem para impressão."));
                }
                return;
            }

            // Converte para base64
            String base64Image = convertBitmapToBase64(bitmap);
            if (base64Image == null) {
                Log.e(TAG, "Falha ao converter bitmap para Base64.");
                if (printerStatusCallback != null) {
                    printerStatusCallback.onError(new Exception("Falha ao converter imagem para Base64."));
                }
                return;
            }

            // Envia para a API
            sendImageToPrintAPI(authToken, base64Image);

        } catch (Exception e) {
            Log.e(TAG, "Erro ao preparar para impressão: " + e.getMessage(), e);
            if (printerStatusCallback != null) {
                printerStatusCallback.onError(e);
            }
        }
    }

    private String convertBitmapToBase64(Bitmap bitmap) {
        try {
            // Limite oficial da Adyen: 102400 bytes (100KB)
            // Largura máxima recomendada: 384 pixels
            // Formato recomendado: 1-bit grayscale
            final int MAX_SIZE_BYTES = 102400; // 100KB
            final int MAX_WIDTH = 384;
            int currentHeight =0;

            Bitmap processedBitmap = bitmap;

            // 1. Redimensiona se necessário para respeitar largura máxima
            if (processedBitmap.getWidth() > MAX_WIDTH) {
                int newHeight = (int) ((float) MAX_WIDTH / processedBitmap.getWidth() * processedBitmap.getHeight());
                processedBitmap = Bitmap.createScaledBitmap(processedBitmap, MAX_WIDTH, newHeight, true);
                Log.d(TAG, "Imagem redimensionada para: " + MAX_WIDTH + "x" + newHeight + " (largura máxima Adyen)");
            }

            // 2. Converte para 1-bit grayscale (preto e branco)
            Bitmap grayscaleBitmap = convertToGrayscale(processedBitmap);
            if (processedBitmap != bitmap) {
                processedBitmap.recycle();
            }

            // 3. Comprime em PNG (melhor para 1-bit grayscale)
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            grayscaleBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            Log.d(TAG, "Imagem final: " + byteArray.length + " bytes (limite Adyen: " + MAX_SIZE_BYTES + " bytes)");

            // 4. Verifica se ainda está dentro do limite
            if (byteArray.length > MAX_SIZE_BYTES) {
                Log.w(TAG, "Imagem ainda excede limite Adyen (" + byteArray.length + " > " + MAX_SIZE_BYTES + "). Reduzindo altura...");

                // Reduz altura gradualmente até caber no limite
                 currentHeight = grayscaleBitmap.getHeight();
                while (byteArray.length > MAX_SIZE_BYTES && currentHeight > 100) {
                    currentHeight = (int) (currentHeight * 0.9); // Reduz 10%

                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(grayscaleBitmap, grayscaleBitmap.getWidth(), currentHeight, true);
                    grayscaleBitmap.recycle();
                    grayscaleBitmap = resizedBitmap;

                    byteArrayOutputStream.reset();
                    grayscaleBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                    byteArray = byteArrayOutputStream.toByteArray();

                    Log.d(TAG, "Altura reduzida para: " + currentHeight + ", novo tamanho: " + byteArray.length + " bytes");
                }
            }

            String base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP);
            grayscaleBitmap.recycle();

            Log.i(TAG, "✅ Imagem otimizada para Adyen: " + byteArray.length + " bytes, " +
                    grayscaleBitmap.getWidth() + "x" + currentHeight + " pixels, 1-bit grayscale");

            return base64String;

        } catch (Exception e) {
            Log.e(TAG, "Erro ao converter bitmap para Base64: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Converte bitmap para 1-bit grayscale (preto e branco) conforme recomendação Adyen
     */
    private Bitmap convertToGrayscale(Bitmap originalBitmap) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        Bitmap grayscaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = originalBitmap.getPixel(x, y);

                // Extrai componentes RGB
                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = pixel & 0xFF;

                // Calcula luminância (grayscale)
                int gray = (int) (0.299 * red + 0.587 * green + 0.114 * blue);

                // Converte para 1-bit (preto ou branco) usando threshold
                int bw = gray > 128 ? 0xFFFFFF : 0x000000; // Branco ou preto

                grayscaleBitmap.setPixel(x, y, bw);
            }
        }

        return grayscaleBitmap;
    }

    private void sendImageToPrintAPI(String authorizationToken, String imageBase64) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("authorizationHash", authorizationToken);
            payload.put("contentBase64", imageBase64);

            Log.i(TAG, "Enviando para API de impressão iFood. Token: " +
                    authorizationToken.substring(0, Math.min(10, authorizationToken.length())) + "...");

            new PrintApiTask().execute(IFOOD_PRINT_API_URL, payload.toString());

        } catch (JSONException e) {
            Log.e(TAG, "Erro ao criar JSON para API de impressão iFood: " + e.getMessage(), e);
            if (printerStatusCallback != null) {
                printerStatusCallback.onError(e);
            }
        }
    }

    private class PrintApiTask extends AsyncTask<String, Void, String> {
        private Exception exception;

        @Override
        protected void onPreExecute() {
            if (printerStatusCallback != null) {
                printerStatusCallback.onConnected();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0];
            String jsonPayload = params[1];
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setReadTimeout(15000);

                // Envia o payload
                OutputStream outStream = new BufferedOutputStream(urlConnection.getOutputStream());
                outStream.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
                outStream.flush();
                outStream.close();

                // Lê a resposta
                int responseCode = urlConnection.getResponseCode();
                Log.d(TAG, "Resposta da API de Impressão iFood: " + responseCode);

                BufferedReader reader;
                if (responseCode >= 200 && responseCode <= 299) {
                    reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                } else {
                    reader = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
                }

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                return responseCode + ":" + result.toString();

            } catch (IOException e) {
                this.exception = e;
                Log.e(TAG, "Erro na chamada da API de impressão iFood: " + e.getMessage(), e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (exception != null) {
                Log.e(TAG, "Falha ao enviar para impressão iFood: " + exception.getMessage(), exception);
                if (printerStatusCallback != null) {
                    printerStatusCallback.onError(exception);
                }
            } else if (result != null) {
                try {
                    String[] parts = result.split(":", 2);
                    int statusCode = Integer.parseInt(parts[0]);
                    String responseBody = parts.length > 1 ? parts[1] : "";

                    Log.d(TAG, "Corpo da resposta da API de Impressão iFood: " + responseBody);

                    if (statusCode >= 200 && statusCode <= 299) {
                        Log.i(TAG, "Impressão enviada com sucesso para a API iFood.");
                        if (printerStatusCallback != null) {
                            printerStatusCallback.onSucess("Impressão enviada com sucesso!");
                        }
                    } else {
                        Log.e(TAG, "Erro da API de impressão iFood (" + statusCode + "): " + responseBody);
                        String errorMsg = "Falha ao imprimir: " + statusCode;

                        try {
                            JSONObject errorJson = new JSONObject(responseBody);
                            errorMsg = errorJson.optString("message", errorMsg);
                        } catch (JSONException ignored) {
                            // Usa a mensagem padrão se não conseguir parsear o JSON
                        }

                        if (printerStatusCallback != null) {
                            printerStatusCallback.onError(new Exception(errorMsg));
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao processar resultado da API de impressão: " + result, e);
                    if (printerStatusCallback != null) {
                        printerStatusCallback.onError(e);
                    }
                }
            } else {
                Log.e(TAG, "Resultado da API de impressão iFood nulo e sem exceção.");
                if (printerStatusCallback != null) {
                    printerStatusCallback.onError(new Exception("Resposta da impressão desconhecida."));
                }
            }
        }
    }
}