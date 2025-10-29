package br.com.berpsistemas.BerpPOSMobile.Printer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import br.com.berpsistemas.BerpPOSMobile.RestaurantBillGenerator;
import br.com.berpsistemas.BerpPOSMobile.model.ContaFields;
import com.shashank.sony.fancytoastlib.FancyToast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class PosDigitalPrinterService implements IPrinterService {
    private Context context;
    private static final int REQUEST_PERMISSION_CODE = 1001;
    private static final int STONE_PRINT_REQUEST_CODE = 4001;
    private ContaFields contaPendente;

    @Override
    public void register(Context context, PrinterStatusCallback callback) {
        this.context = context;
        // Stone não precisa de registro específico para deeplink
        // Chama callback de sucesso imediatamente
        if (callback != null) {
            callback.onConnected();
        }
    }

    @Override
    public void print(ContaFields conta) {
        this.contaPendente = conta;

        // Verifica permissões antes de prosseguir
        if (checkAndRequestPermissions()) {
            // Se já tem permissão, prossegue com a impressão
            proceedWithPrint(conta);
        }
        // Se não tem permissão, a impressão será realizada após a permissão ser concedida
    }

    private boolean checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                if (context instanceof Activity) {
                    ActivityCompat.requestPermissions((Activity) context,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_PERMISSION_CODE);
                    return false; // Permissão ainda não concedida
                } else {
                    // Se não for uma Activity, não pode solicitar permissão
                    Toast.makeText(context, "Erro: Permissão de armazenamento necessária",
                            Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        }
        return true; // Permissão já concedida ou não necessária
    }

    private void proceedWithPrint(ContaFields conta) {
        try {
            RestaurantBillGenerator billGenerator = new RestaurantBillGenerator(context);
            billGenerator.setConta(conta);
            Bitmap bitmap = billGenerator.generateBillBitmap();

            File imageFile = saveImageToExternalStorage(bitmap);
            imprimirImagemStone(imageFile);
        } catch (Exception e) {
            Log.e("PRINT_ERROR", "Erro ao gerar ou salvar bitmap", e);
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                Toast.makeText(context, "Erro ao preparar impressão: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            });
        }
    }

    // Método para ser chamado quando a permissão for concedida
    public void onPermissionGranted() {
        if (contaPendente != null) {
            proceedWithPrint(contaPendente);
            contaPendente = null;
        }
    }

    /**
     * Envia comando para imprimir uma imagem na impressora Stone usando deeplink
     * @param imageFile arquivo da imagem a ser impressa
     */
    private void imprimirImagemStone(File imageFile) {
        try {
            // Converte a imagem para base64
            String imageBase64 = convertImageToBase64(imageFile);

            // Monta o JSON conforme documentação da Stone - array direto
            JSONArray printItems = new JSONArray();

            // Item de imagem
            JSONObject imageItem = new JSONObject();
            imageItem.put("type", "image");
            imageItem.put("imagePath", imageBase64); // Campo correto é imagePath, não resource
            printItems.put(imageItem);

            String jsonString = printItems.toString();
            Log.d("STONE_PRINT_JSON", jsonString);

            // Constrói a URI usando Uri.Builder conforme o padrão Stone
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.authority("print");
            uriBuilder.scheme("printer-app");
            uriBuilder.appendQueryParameter("SHOW_FEEDBACK_SCREEN", "false");
            uriBuilder.appendQueryParameter("SCHEME_RETURN", "order");
            uriBuilder.appendQueryParameter("PRINTABLE_CONTENT", jsonString);

            Uri printUri = uriBuilder.build();

            Log.d("STONE_DEBUG", "Enviando deeplink para Stone: " + printUri.toString());

            // Cria o intent para abrir o aplicativo Stone
            Intent printIntent = new Intent(Intent.ACTION_VIEW);
            printIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            printIntent.setData(printUri);

            // Tenta abrir o deeplink diretamente
            try {
                if (context instanceof Activity) {
                    ((Activity) context).startActivityForResult(printIntent, STONE_PRINT_REQUEST_CODE);
                } else {
                    context.startActivity(printIntent);
                }

                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    Toast.makeText(context, "Enviando para impressão via Stone...", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                Log.e("STONE_DEBUG", "Erro ao abrir deeplink Stone: " + e.getMessage());
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    Toast.makeText(context, "Erro ao conectar com Stone: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }

        } catch (Exception e) {
            FancyToast.makeText(context, e.getMessage(), FancyToast.LENGTH_LONG, FancyToast.WARNING, false).show();
            Log.e("STONE_PRINT_ERROR", "Erro ao enviar comando de impressão Stone", e);
            throw new RuntimeException("Falha ao imprimir imagem via Stone: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica se o Stone App está instalado
     */
    private boolean isStoneAppInstalled() {
        PackageManager pm = context.getPackageManager();
        String[] possiblePackages = {
                "br.com.stone.posandroid",
                "stone.application.pos",
                "br.com.stone.application"
        };

        for (String packageName : possiblePackages) {
            try {
                pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
                Log.d("STONE_DEBUG", "Stone App encontrado: " + packageName);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                Log.d("STONE_DEBUG", "Package não encontrado: " + packageName);
            }
        }
        return false;
    }

    /**
     * Abre o Stone App na Play Store para instalação
     */
    private void openStoneAppInPlayStore() {
        try {
            Intent playStoreIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=br.com.stone.posandroid"));
            playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (playStoreIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(playStoreIntent);
            } else {
                // Fallback para browser
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=br.com.stone.posandroid"));
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(browserIntent);
            }

            Toast.makeText(context, "Redirecionando para instalação do Stone App...", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(context, "Stone App não encontrado e não foi possível abrir a Play Store",
                    Toast.LENGTH_LONG).show();
            Log.e("STONE_ERROR", "Erro ao abrir Play Store", e);
        }
    }

    /**
     * Método alternativo para impressão de texto simples (opcional)
     * @param texto texto a ser impresso
     */
    private void imprimirTextoStone(String texto) {
        try {
            // Array direto para itens de impressão
            JSONArray printItems = new JSONArray();

            // Item de texto
            JSONObject textItem = new JSONObject();
            textItem.put("type", "text");
            textItem.put("value", texto);
            textItem.put("align", "center"); // left, center, right
            printItems.put(textItem);

            String jsonString = printItems.toString();

            // Constrói a URI usando Uri.Builder conforme o padrão Stone
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.authority("print");
            uriBuilder.scheme("printer-app");
            uriBuilder.appendQueryParameter("SHOW_FEEDBACK_SCREEN", "false");
            uriBuilder.appendQueryParameter("SCHEME_RETURN", "order");
            uriBuilder.appendQueryParameter("PRINTABLE_CONTENT", jsonString);

            Uri printUri = uriBuilder.build();

            Intent printIntent = new Intent(Intent.ACTION_VIEW);
            printIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            printIntent.setData(printUri);

            if (context instanceof Activity) {
                ((Activity) context).startActivityForResult(printIntent, STONE_PRINT_REQUEST_CODE);
            } else {
                context.startActivity(printIntent);
            }

        } catch (Exception e) {
            Log.e("STONE_PRINT_ERROR", "Erro ao imprimir texto via Stone", e);
        }
    }

    /**
     * Converte arquivo de imagem para string base64
     * @param imageFile arquivo da imagem
     * @return string base64 da imagem
     */
    private String convertImageToBase64(File imageFile) throws IOException {
        FileInputStream fis = new FileInputStream(imageFile);
        byte[] imageBytes = new byte[(int) imageFile.length()];
        fis.read(imageBytes);
        fis.close();

        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
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

        File imageFile = new File(appDir, "conta_stone.png");
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

    /**
     * Método para processar resposta do Stone App (se necessário)
     * Deve ser chamado na Activity que iniciou a impressão
     */
    public void handleStoneResponse(Intent data) {
        if (data != null && data.getData() != null) {
            Uri responseUri = data.getData();
            String response = responseUri.getQueryParameter("response");

            if (response != null) {
                try {
                    String decodedResponse = new String(Base64.decode(response, Base64.DEFAULT));
                    JSONObject responseJson = new JSONObject(decodedResponse);

                    boolean success = responseJson.optBoolean("success", false);
                    String message = responseJson.optString("message", "");

                    if (success) {
                        Toast.makeText(context, "Impressão realizada com sucesso!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Erro na impressão: " + message, Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    Log.e("STONE_RESPONSE", "Erro ao processar resposta Stone", e);
                }
            }
        }
    }
}