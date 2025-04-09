package com.mobile.berp.BerpPOSMobile.Printer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;


import com.mobile.berp.BerpPOSMobile.RestaurantBillGenerator;
import com.mobile.berp.BerpPOSMobile.model.ContaFields;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class PosDigitalPrinterService   implements IPrinterService {
    private Context context;


    @Override
    public void register(Context context, PrinterStatusCallback callback) {
        this.context = context;
//        PosDigital.register(context, new PosDigital.BindCallback() {
//            @Override
//            public void onError(Exception e) {
//                callback.onError(e);
//            }
//
//            @Override
//            public void onConnected() {
//                callback.onConnected();
//            }
//
//            @Override
//            public void onDisconnected() {
//                callback.onDisconnected();
//            }
//        }
        // );
    }


    @Override
    public void print(ContaFields conta) {
        try {
            RestaurantBillGenerator billGenerator = new RestaurantBillGenerator(context);
            billGenerator.setConta(conta);
            Bitmap bitmap = billGenerator.generateBillBitmap();



            File imageFile = saveImageToExternalStorage(bitmap);
            imprimirImagemCieloLio(imageFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e("PRINT_ERROR", "Erro ao gerar ou salvar bitmap", e);
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                Toast.makeText(context, "Erro ao preparar impressão: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });

        }
    }

    /**
     * Envia comando para imprimir uma imagem na impressora da Cielo LIO
     * @param imagePath caminho completo da imagem a ser impressa
     */
    private void imprimirImagemCieloLio(String imagePath) {
        try {
            // Monta o JSON conforme documentação da Cielo LIO
            JSONObject jsonPrint = new JSONObject();
            jsonPrint.put("operation", "PRINT_IMAGE");

            // Styles é um array (mesmo que vazio para imagens)
            JSONArray styles = new JSONArray();
            styles.put(new JSONObject());
            jsonPrint.put("styles", styles);

            // Value é um array com o caminho da imagem
            JSONArray value = new JSONArray();
            value.put(imagePath);
            jsonPrint.put("value", value);

            String jsonString = jsonPrint.toString();
            Log.d("PRINT_JSON", jsonString);

            // Converte para base64
            String base64 = Base64.encodeToString(jsonString.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);

            // Constrói a URI do deeplink conforme documentação da Cielo LIO
            String printUri = "lio://print?request=" + base64 + "&urlCallback=order://print_response";

            // Cria o intent para abrir o aplicativo da Cielo LIO
            Intent printIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(printUri));
            printIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


            if (printIntent.resolveActivity(context.getPackageManager()) != null) {
                if (context instanceof Activity) {
                    ((Activity) context).startActivityForResult(printIntent, 4001);
                } else {
                    printIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // necessário fora de uma Activity
                    context.startActivity(printIntent);
                }
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    Toast.makeText(context, "Enviando para impressão via Cielo LIO...", Toast.LENGTH_SHORT).show();
                });


            } else {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    Toast.makeText(context, "Aplicativo Cielo LIO não encontrado!", Toast.LENGTH_LONG).show();

                });

            }



//            // Verifica se existe algum app que possa lidar com esse deeplink
//            if (printIntent.resolveActivity(context.getPackageManager()) != null) {
//                if (context instanceof Activity) {
//                    ((Activity) context).startActivityForResult(printIntent, 4001);
//                } else {
//                    context.startActivity(printIntent); // só dispara, sem esperar retorno
//                }
//                Toast.makeText(context, "Enviando para impressão via Cielo LIO...", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(context, "Aplicativo Cielo LIO não encontrado!", Toast.LENGTH_LONG).show();
//            }

        } catch (Exception e) {
            Log.e("PRINT_ERROR", "Erro ao enviar comando de impressão", e);
            throw new RuntimeException("Falha ao imprimir imagem: " + e.getMessage(), e);
        }
    }


    private File saveImageToExternalStorage(Bitmap bitmap) throws IOException {
        File picturesDir;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Android 10 ou superior
            picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        } else {
            // Android 9 ou inferior
            picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        }

        File appDir = new File(picturesDir, "RestaurantBills");
        if (!appDir.exists()) {
            appDir.mkdirs();
        }


        File imageFile = new File(appDir, "conta.png");
        if (imageFile.exists()) {
            imageFile.delete();
        }

        FileOutputStream fos = new FileOutputStream(imageFile);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.flush();
        fos.close();

        MediaScannerConnection.scanFile(context,
                new String[]{imageFile.getAbsolutePath()}, null, null);

        return imageFile;
    }


}
