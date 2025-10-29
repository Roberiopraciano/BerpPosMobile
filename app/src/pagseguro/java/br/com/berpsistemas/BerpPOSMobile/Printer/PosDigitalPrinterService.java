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

import br.com.berpsistemas.BerpPOSMobile.PlugPagProvider;
import br.com.berpsistemas.BerpPOSMobile.RestaurantBillGenerator;
import br.com.berpsistemas.BerpPOSMobile.model.ContaFields;
import com.shashank.sony.fancytoastlib.FancyToast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrintResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrinterData;

public class PosDigitalPrinterService implements IPrinterService {
    private Context context;
    private PlugPag plugPag;
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
            imprimirImagem(imageFile);
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
    private void imprimirImagem(File imageFile) {
        try {
            plugPag = PlugPagProvider.getInstance(this.context);
            new Thread(() -> {
                try {
                    Log.d("PagSeguro", "Verificando autenticação...");

                    PlugPagPrinterData bitmapPath2 = new PlugPagPrinterData(imageFile.getPath(), 100, 1);
                    PlugPagPrintResult printresult = plugPag.printFromFile(bitmapPath2);

                    // Sucesso - voltar para UI thread
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        handlePrintResult(printresult, null);
                    });

                } catch (Exception e) {
                    Log.e("PagSeguro", "Erro ao verificar autenticação: " + e.getMessage(), e);
                    // Erro - voltar para UI thread
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        handlePrintResult(null, e);
                    });
                }
            }).start();

        } catch (Exception e) {
            handlePrintResult(null, e);
        }
    }

    private void handlePrintResult(PlugPagPrintResult result, Exception error) {
        if (error != null) {
            FancyToast.makeText(context, error.getMessage(),
                    FancyToast.LENGTH_LONG, FancyToast.WARNING, false).show();
            Log.e("STONE_PRINT_ERROR", "Erro ao enviar comando de impressão Stone", error);
        } else if (result != null) {
            Log.d("PagSeguro", "result " + result.getMessage());
            Toast.makeText(context, "Impressão realizada com sucesso!",
                    Toast.LENGTH_SHORT).show();
        }
    }



    private File saveImageToExternalStorage(Bitmap bitmap) throws IOException {
        File picturesDir;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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



}