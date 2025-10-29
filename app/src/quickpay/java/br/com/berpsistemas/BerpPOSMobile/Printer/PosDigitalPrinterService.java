package br.com.berpsistemas.BerpPOSMobile.Printer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.pax.dal.IDAL;
import com.pax.dal.exceptions.PrinterDevException;
import com.pax.dal.IPrinter;
import com.pax.neptunelite.api.NeptuneLiteUser;
import com.pax.dal.entity.EFontTypeAscii;
import com.pax.dal.entity.EFontTypeExtCode;

import br.com.berpsistemas.BerpPOSMobile.RestaurantBillGenerator;
import br.com.berpsistemas.BerpPOSMobile.model.ContaFields;
import com.shashank.sony.fancytoastlib.FancyToast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;



public class PosDigitalPrinterService implements IPrinterService {
    private Context context;

    private static final int REQUEST_PERMISSION_CODE = 1001;
    private static final int STONE_PRINT_REQUEST_CODE = 4001;
    private ContaFields contaPendente;


    static {
        try {
            // Carregue as bibliotecas na ordem correta
            System.loadLibrary("iconv");
            //    System.loadLibrary("DeviceConfig");
            System.loadLibrary("DCL");
            Log.d("PrinterHelper", "Bibliotecas nativas carregadas com sucesso");
        } catch (UnsatisfiedLinkError e) {
            Log.e("PrinterHelper", "Erro ao carregar bibliotecas nativas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private IDAL dal = null;
    private NeptuneLiteUser neptuneLiteUser;

    private Object result; // Assumindo que existe um objeto result para callbacks


    public PosDigitalPrinterService() {
     //
    }

    public void onCreate() throws Exception {

    }

    void printInit() {
        try {
            dal = neptuneLiteUser.getDal(context);
            if (dal != null && dal.getPrinter() != null) {
                dal.getPrinter().init();
                dal.getPrinter().setGray(3);
                // result.success("ok"); // Descomente se usando Flutter/callback
            }
        } catch (Exception e) {
            e.printStackTrace();
            // result.error("PrintText01", e.getMessage(), e.getCause()); // Descomente se usando Flutter/callback
        }
    }


    @Override
    public void register(Context context, PrinterStatusCallback callback) {
        this.context = context;
        this.neptuneLiteUser = NeptuneLiteUser.getInstance();


        if (callback != null) {
            callback.onConnected();
        }
    }

    @Override
    public void print(ContaFields conta) {
        this.contaPendente = conta;

        printInit();

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


            String imageJson = "{\"path\": \"" + imageFile.getPath() + "\"}";



            imprimirImagem(imageJson);
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
    private void imprimirImagem(String imageFile) {
        try {
             new Thread(() -> {
                try {
                Log.d("QuickPay", "Impressao...");
                JSONObject obj = new JSONObject(imageFile);
                if (!obj.has("path")) {
                    // result.error("invalid parameters", "parameters required not found.", null);
                    return;
                }

                File file = new File(obj.getString("path"));
                if (file.exists()) {
                    InputStream in = new FileInputStream(file);
                    Bitmap bitmap = BitmapFactory.decodeStream(in);

                    if (dal != null && dal.getPrinter() != null) {
                        dal.getPrinter().init();
                        dal.getPrinter().setGray(3);
                        dal.getPrinter().printBitmap(bitmap);

                        if (printerStart(dal.getPrinter()) == 0) {

                            // result.success("ok");
                        }
                    }
                    in.close();
                }
//                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
//                        handlePrintResult(printresult, null);
//                    });



                } catch (Exception e) {
                    Log.e("Quickpay", "Erro ao verificar autenticação: " + e.getMessage(), e);
                    // Erro - voltar para UI thread
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {

                    });
                }
            }).start();

        } catch (Exception e) {
            Log.e("Printer Error", e.getMessage());
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




    private int printerStart(IPrinter printer) {



        try {
            while (true) {
                int ret = printer.start();
                if (ret != 1) {
                    if (ret == 2) {
                        sendMessageResponse("printer", "ERROR", "Impressora sem Papel.", false);
                        return -1;
                    } else if (ret == 8) {
                        sendMessageResponse("printer", "ERROR", "Impressora super-aquecida.", false);
                        return -1;
                    } else if (ret == 9) {
                        sendMessageResponse("printer", "ERROR", "Voltagem muito baixa na impressora!", false);
                        return -1;
                    } else {
                        if (ret != 0) {
                            sendMessageResponse("printer", "ERROR", "Data length except!", false);
                        }
                        return (ret != 0) ? -1 : 0;
                    }
                }
                SystemClock.sleep(100L);
            }
        } catch (PrinterDevException e) {
            e.printStackTrace();
            return 0;
        }
    }


    private void sendMessageResponse(String type, String status, String message, boolean success) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            FancyToast.makeText(context, message+' '+status,
                    FancyToast.LENGTH_LONG, FancyToast.WARNING, false).show();
        });

        Log.e("QUICKPAY_PRINT_ERROR", "Erro ao enviar comando de impressão "+ type);
    }



}