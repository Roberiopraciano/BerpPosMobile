package com.mobile.berp.BerpPOSMobile;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.berp.BerpPOSMobile.pagamento.IPagamento;
import com.mobile.berp.BerpPOSMobile.pagamento.PagamentoFactory;
import com.mobile.berp.BerpPOSMobile.pagamento.PaymentConfig;
import com.mobile.berp.BerpPosMobile.BuildConfig;

import com.mobile.berp.BerpPosMobile.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class ConfigActivity extends AppCompatActivity implements View.OnClickListener {

    Button btSalvar, btQrCode,btVoltar;
    EditText edtIp, edtPorta, edtTerminal;
    private androidx.appcompat.widget.SwitchCompat swContaComanda,swTracking;
    TextView tvDeviceId;

    private IPagamento pagamento;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        // Inicializa os componentes
        btSalvar = findViewById(R.id.btSalvar);
        btQrCode = findViewById(R.id.btQrCode);
        btVoltar = findViewById(R.id.btnVoltar);
        edtIp = findViewById(R.id.edtIp);
        edtPorta = findViewById(R.id.edtPorta);
        edtTerminal = findViewById(R.id.edtTerminal);
        swContaComanda = findViewById(R.id.swContaComanda);
        swTracking = findViewById(R.id.swTracking);
        tvDeviceId = findViewById(R.id.tvDeviceId);  // Novo TextView



        if (BuildConfig.POS_MODEL.equals("celular")) {
            swContaComanda.setVisibility(View.GONE);
        }
        SharedPreferences prefs = getSharedPreferences("preferencias_1", Context.MODE_PRIVATE);

        // Carrega o estado do rastreamento
        boolean isTrackingEnabled = prefs.getBoolean("tracking_enabled", true);
        swTracking.setChecked(isTrackingEnabled);

        // Configura listener para o Switch de rastreamento
//        swTracking.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            TrackingService.setTrackingEnabled(ConfigActivity.this, isChecked);
//            Toast.makeText(
//                    ConfigActivity.this,
//                    isChecked ? "Rastreamento habilitado" : "Rastreamento desabilitado",
//                    Toast.LENGTH_SHORT
//            ).show();
//        });

        swTracking.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            if (isChecked) {
//                // Inicia o serviço de rastreamento
//                Intent serviceIntent = new Intent(ConfigActivity.this, TrackingService.class);
//                startService(serviceIntent);
//            } else {
//                // Para o serviço de rastreamento
//                TrackingService.stopTrackingService(ConfigActivity.this);
//            }
//            Toast.makeText(
//                    ConfigActivity.this,
//                    isChecked ? "Rastreamento habilitado" : "Rastreamento desabilitado",
//                    Toast.LENGTH_SHORT
//            ).show();
        });


        // Carrega o número do dispositivo e exibe no TextView
        //String deviceId = Variaveis.getNumeroDispositivo();

        if (prefs.getString("Device_id", "")==""){
            tvDeviceId.setText(DeviceInfo.getAndroidID(this));
        }
        else {
            tvDeviceId.setText(prefs.getString("Device_id", ""));
        }
        // Preenche os campos com os dados salvos
        edtIp.setText(prefs.getString("IP", ""));
        edtPorta.setText(prefs.getString("Porta", ""));
        edtTerminal.setText(prefs.getString("Terminal", ""));
        swContaComanda.setChecked(prefs.getBoolean("Imprimir", false));

        btSalvar.setOnClickListener(this);
        btQrCode.setOnClickListener(this);
        btVoltar.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == btSalvar) {
            SharedPreferences prefs = getSharedPreferences("preferencias_1", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            // Salva as configurações
            editor.putString("IP", edtIp.getText().toString());
            editor.putString("Porta", edtPorta.getText().toString());
            editor.putString("Terminal", edtTerminal.getText().toString());
            editor.putBoolean("Imprimir", swContaComanda.isChecked());
            editor.putBoolean("tracking_enabled", swTracking.isChecked());
            editor.putString("Device_id",tvDeviceId.getText().toString());

            editor.apply();

            startActivity(new Intent(this, MainActivity.class));
            finish();
        };
        if (view == btQrCode) {
        };

            if (view == btVoltar) {
                try {

                    try {
                        // Verifica permissões se necessário (para Android < 10)


                        // Gera o bitmap da conta
                        ;

                        // Imprime a imagem usando o caminho do arquivo


                    } catch (Exception e) {
                        Log.e("IMPRESSAO", "Erro: " + e.getMessage(), e);
                        Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }




                    // Primeiro garantimos que temos as credenciais da Cielo salvas
                    // Essas credenciais poderiam vir de uma tela de configuração, mas para teste:
                    salvarCredenciaisCielo();




                    // Inicializa o objeto de pagamento usando a Factory
                    pagamento = PagamentoFactory.criarPagamento(this);

                    // Configura os parâmetros do pagamento
                    String orderId = generateOrderId();
                    int amount = 4610; // Valor em centavos (R$ 46,20)
                    String transactionType = "credit"; // Tipo de transação (crédito como padrão)

                    PaymentConfig config = new PaymentConfig(amount, transactionType, orderId);

                    // Inicia o pagamento via deeplink
                    pagamento.iniciarPagamentoDeeplink(this, config);

                    // Não finalizamos a activity para esperar o retorno do pagamento

                } catch (Exception e) {
                    Log.e("PAGAMENTO_ERRO", "Erro ao iniciar pagamento: " + e.getMessage());
                    Toast.makeText(this, "Erro ao iniciar pagamento: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
            }




    }

    /**
     * Método auxiliar para salvar credenciais da Cielo para teste
     * Em um ambiente de produção, estas credenciais viriam de uma configuração
     */
    private void salvarCredenciaisCielo() {
        SharedPreferences prefs = getSharedPreferences("PaymentAuthPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Valores para teste - em produção estes devem ser configurados pelo usuário
        editor.putString("access_token_cielo", "H8tiTQWi04OyE9YQQQklLDqBHYUrytZk1k6mQsBxHuXlFzvXS1");
        editor.putString("client_id_cielo", "2PI5DICbOwAyHNITv4oUdAH3OwCLPRbt6cBsf37f2NeU3TTtkx");

        editor.apply();
    }

    // Método auxiliar para gerar um ID de pedido único
    private String generateOrderId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return "ORDER-" + sdf.format(new Date());
    }

    // Sobreescrevemos o onActivityResult para processar o resultado do pagamento

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Código de requisição para pagamento
        final int PAYMENT_REQUEST_CODE = 3001; // Mesmo código usado na classe PagamentoPos

        if (requestCode == PAYMENT_REQUEST_CODE) {
            try {
                // Delega o processamento para o objeto de pagamento
                if (pagamento != null) {
                    pagamento.processarResultado(requestCode, resultCode, data);
                }

                // Processa o resultado conforme documentação da Cielo LIO
                // A resposta vem como Base64 na URI retornada
                if (data != null && Intent.ACTION_VIEW.equals(data.getAction())) {
                    Uri uri = data.getData();

                    if (uri != null) {
                        // Obtém o parâmetro de resposta (codificado em base64)
                        String responseBase64 = uri.getQueryParameter("response");

                        if (responseBase64 != null) {
                            // Decodifica o base64 para obter o JSON
                            byte[] responseBytes = Base64.decode(responseBase64, Base64.DEFAULT);
                            String responseJson = new String(responseBytes, StandardCharsets.UTF_8);

                            Log.d("PAGAMENTO_CIELO", "Resposta decodificada: " + responseJson);

                            // Analisa o JSON para determinar sucesso ou falha
                            JSONObject jsonResponse = new JSONObject(responseJson);

                            // Verifica se é uma resposta de erro (contém "code" e "reason")
                            if (jsonResponse.has("code") && jsonResponse.has("reason")) {
                                // É uma mensagem de erro
                                int errorCode = jsonResponse.getInt("code");
                                String reason = jsonResponse.getString("reason");

                                Toast.makeText(this, "Erro no pagamento: " + reason, Toast.LENGTH_LONG).show();
                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                            } else {
                                // É uma resposta de sucesso (contém "status", "payments", etc.)
                                // Verifica o statusCode dentro do array payments
                                if (jsonResponse.has("payments") && jsonResponse.getJSONArray("payments").length() > 0) {
                                    JSONObject payment = jsonResponse.getJSONArray("payments").getJSONObject(0);
                                    JSONObject paymentFields = payment.getJSONObject("paymentFields");

                                    String statusCode = paymentFields.getString("statusCode");

                                    if ("0".equals(statusCode) || "1".equals(statusCode)) {
                                        // Pagamento bem-sucedido (statusCode 0 para PIX, 1 para outros)
                                        String brand = payment.getString("brand");
                                        String authCode = payment.getString("authCode");

                                        Toast.makeText(this,
                                                "Pagamento realizado com sucesso!\nBandeira: " + brand +
                                                        "\nCódigo: " + authCode,
                                                Toast.LENGTH_LONG).show();

                                        // Imprime o cupom após pagamento bem-sucedido
                                     //   imprimirCupom();
                                    } else if ("2".equals(statusCode)) {
                                        // Cancelamento
                                        Toast.makeText(this, "Pagamento cancelado", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(this, MainActivity.class));
                                        finish();
                                    } else {
                                        // Outro status
                                        Toast.makeText(this, "Status de pagamento desconhecido: " + statusCode, Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(this, MainActivity.class));
                                        finish();
                                    }
                                } else {
                                    // Formato de resposta inesperado
                                    Toast.makeText(this, "Resposta de pagamento em formato inesperado", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(this, MainActivity.class));
                                    finish();
                                }
                            }
                        } else {
                            // Não encontrou o parâmetro response
                            Toast.makeText(this, "Resposta de pagamento inválida (sem dados)", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        }
                    } else {
                        // Uri nula
                        Toast.makeText(this, "Resposta de pagamento inválida (URI nula)", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    }
                } else {
                    // Intent não é do tipo ACTION_VIEW ou é nula
                    if (resultCode == RESULT_CANCELED) {
                        Toast.makeText(this, "Pagamento cancelado pelo usuário", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Resposta de pagamento inválida", Toast.LENGTH_LONG).show();
                    }
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
            } catch (Exception e) {
                Log.e("PAGAMENTO_ERRO", "Erro ao processar resultado: " + e.getMessage(), e);
                Toast.makeText(this, "Erro ao processar resultado: " + e.getMessage(), Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        }
    }

    private String gerarJsonCupomComColunasPeso() {
        try {
            JSONArray styles = new JSONArray();

            // Coluna 1 - Descrição (peso maior)
            JSONObject estiloColuna1 = new JSONObject();
            estiloColuna1.put("key_attributes_align", 0); // Esquerda
            estiloColuna1.put("key_attributes_textsize", 22);
            estiloColuna1.put("key_attributes_typeface", 0);
            estiloColuna1.put("key_attributes_weight", 4); // 40% da largura

            // Coluna 2 - Qtd x Unit
            JSONObject estiloColuna2 = new JSONObject();
            estiloColuna2.put("key_attributes_align", 1); // Centro
            estiloColuna2.put("key_attributes_textsize", 22);
            estiloColuna2.put("key_attributes_typeface", 0);
            estiloColuna2.put("key_attributes_weight", 3); // 30%

            // Coluna 3 - Total
            JSONObject estiloColuna3 = new JSONObject();
            estiloColuna3.put("key_attributes_align", 2); // Direita
            estiloColuna3.put("key_attributes_textsize", 22);
            estiloColuna3.put("key_attributes_typeface", 1); // Negrito
            estiloColuna3.put("key_attributes_weight", 3); // 30%

            styles.put(estiloColuna1);
            styles.put(estiloColuna2);
            styles.put(estiloColuna3);

            JSONArray values = new JSONArray();

            // Cabeçalho centralizado (em branco nas laterais)
            values.put(""); values.put("RESTAURANTE SABOR CASEIRO"); values.put("");

            // Dados da mesa e data
            values.put("Mesa 05"); values.put(""); values.put("Data: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));

            // Espaço
            values.put(""); values.put(""); values.put("");

            // Títulos das colunas
            values.put("Descrição"); values.put("Qtd x Unit"); values.put("Total");

            // Itens
            values.put("Feijoada"); values.put("1 x R$ 25,00"); values.put("R$ 25,00");
            values.put("Refrigerante"); values.put("2 x R$ 5,00"); values.put("R$ 10,00");
            values.put("Sobremesa"); values.put("1 x R$ 7,00"); values.put("R$ 7,00");

            // Separador
            values.put(""); values.put(""); values.put("-------------------------");

            // Totais
            values.put("Subtotal"); values.put(""); values.put("R$ 42,00");
            values.put("Serviço (10%)"); values.put(""); values.put("R$ 4,20");
            values.put("TOTAL"); values.put(""); values.put("R$ 46,20");

            // Espaço
            values.put(""); values.put(""); values.put("");

            // Mensagem final
            values.put(""); values.put("Obrigado pela preferência!"); values.put("");
            values.put(""); values.put("Volte sempre!"); values.put("");

            // Monta o JSON final
            JSONObject json = new JSONObject();
            json.put("operation", "PRINT_MULTI_COLUMN_TEXT");
            json.put("styles", styles);
            json.put("value", values);

            return json.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }


    /**
     * Gera uma imagem da conta do restaurante
     * @return arquivo de imagem gerado
     */
    private File generateRestaurantBill() {
        try {
            // Criamos uma instância da nossa classe RestaurantBillGenerator
            RestaurantBillGenerator billGenerator = new RestaurantBillGenerator(this);

            // Geramos a conta com dados de exemplo
            return billGenerator.generateBillImage();
        } catch (Exception e) {
            Log.e("BILL_ERROR", "Erro ao gerar conta", e);
            throw new RuntimeException("Falha ao gerar imagem da conta: " + e.getMessage(), e);
        }
    }






    /**
     * Gera a imagem da conta e retorna uma URI acessível para outros aplicativos
     * @return URI da imagem gerada via FileProvider
     */
    private Uri generateRestaurantBillWithUri() {
        try {
            // Criamos uma instância da nossa classe RestaurantBillGenerator
            RestaurantBillGenerator billGenerator = new RestaurantBillGenerator(this);

            // Geramos o bitmap
            Bitmap bitmap = billGenerator.generateBillBitmap();

            // Salvamos no diretório de arquivos do aplicativo
            File cacheDir = new File(getCacheDir(), "shared_images");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }

            String fileName = "conta_" + System.currentTimeMillis() + ".png";
            File imageFile = new File(cacheDir, fileName);

            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            // Criamos uma URI acessível usando FileProvider
            Uri contentUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    imageFile
            );

            Log.d("BILL_URI", "Imagem gerada com URI: " + contentUri.toString());

            // Concedemos permissão de leitura para qualquer aplicativo
            grantUriPermission(
                    "br.com.ingenico.mpos", // Pacote da Cielo LIO
                    contentUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            return contentUri;
        } catch (Exception e) {
            Log.e("BILL_ERROR", "Erro ao gerar conta com URI", e);
            throw new RuntimeException("Falha ao gerar URI da conta: " + e.getMessage(), e);
        }
    }


    /**
     * Imprime uma imagem na Cielo LIO usando URI de content provider
     * @param imageUri URI da imagem a ser impressa
     */
    private void imprimirImagemCieloLioComUri(Uri imageUri) {
        try {
            // Monta o JSON conforme documentação da Cielo LIO
            JSONObject jsonPrint = new JSONObject();
            jsonPrint.put("operation", "PRINT_IMAGE");

            // Styles é um array (mesmo que vazio para imagens)
            JSONArray styles = new JSONArray();
            styles.put(new JSONObject());
            jsonPrint.put("styles", styles);

            // Value é um array com a URI da imagem
            JSONArray value = new JSONArray();
            value.put(imageUri.toString());
            jsonPrint.put("value", value);

            String jsonString = jsonPrint.toString();
            Log.d("PRINT_JSON", jsonString);

            // Converte para base64
            String base64 = Base64.encodeToString(jsonString.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);

            // Constrói a URI do deeplink
            String printUri = "lio://print?request=" + base64 + "&urlCallback=order://print_response";

            // Cria o intent para abrir o aplicativo da Cielo LIO
            Intent printIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(printUri));
            printIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            if (printIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(printIntent, 4001);
                Toast.makeText(this, "Enviando para impressão via Cielo LIO...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Aplicativo Cielo LIO não encontrado!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e("PRINT_ERROR", "Erro ao enviar comando de impressão", e);
            throw new RuntimeException("Falha ao imprimir imagem: " + e.getMessage(), e);
        }
    }

    private File saveImageToExternalStorage(Bitmap bitmap) throws IOException {
        File picturesDir;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Para Android 10+, usamos getExternalFilesDir que não requer permissões
            picturesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        } else {
            // Para versões anteriores, usamos o diretório público de Pictures
            picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        }

        // Cria o diretório para nossa aplicação se não existir
        File appDir = new File(picturesDir, "RestaurantBills");
        if (!appDir.exists()) {
            appDir.mkdirs();
        }

        // Cria o arquivo com timestamp para evitar conflitos
        String fileName = "conta_" + System.currentTimeMillis() + ".png";
        File imageFile = new File(appDir, fileName);

        // Salva o bitmap no arquivo
        FileOutputStream fos = new FileOutputStream(imageFile);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.flush();
        fos.close();

        // Torna o arquivo visível na galeria (opcional)
        MediaScannerConnection.scanFile(this,
                new String[]{imageFile.getAbsolutePath()}, null, null);

        return imageFile;
    }

}