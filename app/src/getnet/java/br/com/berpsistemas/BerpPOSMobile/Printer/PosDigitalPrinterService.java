package br.com.berpsistemas.BerpPOSMobile.Printer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import br.com.berpsistemas.BerpPOSMobile.RestaurantBillGenerator;
import br.com.berpsistemas.BerpPOSMobile.model.ContaFields;

// Importações do SDK Getnet
import com.getnet.posdigital.PosDigital;
import com.getnet.posdigital.printer.AlignMode;
import com.getnet.posdigital.printer.FontFormat;
import com.getnet.posdigital.printer.IPrinterCallback;

/**
 * Serviço de impressão usando SDK da Getnet
 * Imprime APENAS a imagem gerada pelo RestaurantBillGenerator
 */
public class PosDigitalPrinterService implements IPrinterService {
    private static final String TAG = "GetnetPrinterService";
    private Context context;
    private static final int REQUEST_PERMISSION_CODE = 1001;
    private ContaFields contaPendente;
    private PrinterStatusCallback statusCallback;
    private boolean isSDKConnected = false;

    // Handler para UI thread
    private Handler mainHandler;

    public PosDigitalPrinterService() {
        // Inicializa o handler da UI thread
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void register(Context context, PrinterStatusCallback callback) {
        this.context = context;
        this.statusCallback = callback;

        Log.d(TAG, "Iniciando conexão com SDK Getnet...");

        // Conecta com o serviço PosDigital da Getnet
        connectToGetnetService();
    }

    /**
     * Conecta com o serviço PosDigital da Getnet
     */
    private void connectToGetnetService() {
        try {
            PosDigital.register(context.getApplicationContext(), new PosDigital.BindCallback() {
                @Override
                public void onConnected() {
                    Log.i(TAG, "SDK Getnet conectado com sucesso");
                    isSDKConnected = true;

                    // Inicializa a impressora
                    initializePrinter();

                    if (statusCallback != null) {
                        statusCallback.onConnected();
                    }
                }

                @Override
                public void onDisconnected() {
                    Log.w(TAG, "SDK Getnet desconectado");
                    isSDKConnected = false;

                    if (statusCallback != null) {
                        statusCallback.onDisconnected();
                    }

                    // Tenta reconectar após um tempo usando o handler da UI thread
                    mainHandler.postDelayed(() -> {
                        Log.d(TAG, "Tentando reconectar ao SDK Getnet...");
                        connectToGetnetService();
                    }, 3000);
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Erro na conexão com SDK Getnet: " + e.getMessage(), e);
                    isSDKConnected = false;

                    if (statusCallback != null) {
                        statusCallback.onError(e);
                    }

                    // Tenta reconectar
                    if (PosDigital.getInstance().isInitiated()) {
                        PosDigital.unregister(context.getApplicationContext());
                    }
                    connectToGetnetService();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Erro ao registrar SDK Getnet: " + e.getMessage(), e);
            if (statusCallback != null) {
                statusCallback.onError(e);
            }
        }
    }

    /**
     * Inicializa a impressora Getnet
     */
    private void initializePrinter() {
        try {
            if (PosDigital.getInstance().isInitiated()) {
                PosDigital.getInstance().getPrinter().init();
                Log.d(TAG, "Impressora Getnet inicializada");
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inicializar impressora: " + e.getMessage(), e);
        }
    }

    @Override
    public void print(ContaFields conta) {
        this.contaPendente = conta;

        // Verifica se o SDK está conectado
        if (!isSDKConnected || !PosDigital.getInstance().isInitiated()) {
            Log.e(TAG, "SDK Getnet não está conectado");

            // Usa o handler para mostrar toast na UI thread
            mainHandler.post(() -> {
                Toast.makeText(context, "Impressora não está conectada. Tentando reconectar...",
                        Toast.LENGTH_LONG).show();
            });

            connectToGetnetService();
            return;
        }

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
                    mainHandler.post(() -> {
                        Toast.makeText(context, "Erro: Permissão de armazenamento necessária",
                                Toast.LENGTH_LONG).show();
                    });
                    return false;
                }
            }
        }
        return true; // Permissão já concedida ou não necessária
    }

    private void proceedWithPrint(ContaFields conta) {
        try {
            if (!PosDigital.getInstance().isInitiated()) {
                throw new Exception("SDK Getnet não está inicializado");
            }

            Log.d(TAG, "Iniciando impressão da imagem usando SDK Getnet...");

            // Verifica se a conta não é null
            if (conta == null) {
                throw new Exception("Dados da conta não disponíveis");
            }

            // Imprime usando o SDK da Getnet - apenas a imagem
            imprimirComSDKGetnet(conta);

        } catch (Exception e) {
            Log.e(TAG, "Erro ao gerar ou imprimir conta", e);
            final String errorMessage = e.getMessage();
            mainHandler.post(() -> {
                Toast.makeText(context, "Erro ao preparar impressão: " + errorMessage,
                        Toast.LENGTH_LONG).show();
                if (statusCallback != null) {
                    statusCallback.onError(e);
                }
            });
        }
    }

    /**
     * Imprime usando o SDK da Getnet - APENAS a imagem gerada pelo RestaurantBillGenerator
     */
    private void imprimirComSDKGetnet(ContaFields conta) {
        try {
            // Inicializa a impressora
            PosDigital.getInstance().getPrinter().init();

            // Configura nível de cinza (1-10, sendo 7 um bom valor para imagens)
            PosDigital.getInstance().getPrinter().setGray(7);

            // Gera a imagem da conta usando o RestaurantBillGenerator
            Log.d(TAG, "Gerando bitmap da conta...");
            RestaurantBillGenerator billGenerator = new RestaurantBillGenerator(context);
            billGenerator.setConta(conta);
            Bitmap bitmap = billGenerator.generateBillBitmap();

            if (bitmap != null) {
                // Redimensiona se necessário (máximo 378 pixels de largura)
                Bitmap resizedBitmap = resizeBitmapIfNeeded(bitmap);

                Log.d(TAG, "Imprimindo imagem da conta - Largura: " + resizedBitmap.getWidth() +
                        ", Altura: " + resizedBitmap.getHeight());

                // Adiciona apenas a imagem - sem cabeçalho ou rodapé
                PosDigital.getInstance().getPrinter().addImageBitmap(AlignMode.CENTER, resizedBitmap);

                // Adiciona alguns espaços no final para facilitar o corte do papel
                PosDigital.getInstance().getPrinter().addText(AlignMode.LEFT, " ");
                PosDigital.getInstance().getPrinter().addText(AlignMode.LEFT, " ");

            } else {
                Log.w(TAG, "Bitmap da conta é null, imprimindo mensagem de erro");

                // Se não conseguir gerar a imagem, imprime mensagem de erro
                PosDigital.getInstance().getPrinter().defineFontFormat(FontFormat.MEDIUM);
                PosDigital.getInstance().getPrinter().addText(AlignMode.CENTER, "Erro ao gerar imagem da conta");
                PosDigital.getInstance().getPrinter().addText(AlignMode.LEFT, " ");
            }

            // Executa a impressão com corte de papel
            // Executa a impressão com corte de papel
            PosDigital.getInstance().getPrinter().print(new IPrinterCallback.Stub() {
                @Override
                public void onSuccess() throws RemoteException {
                    Log.i(TAG, "Imagem da conta impressa com sucesso");
                    mainHandler.post(() -> {
                        Toast.makeText(context, "Conta impressa com sucesso!", Toast.LENGTH_SHORT).show();
                        if (statusCallback != null) {
                            statusCallback.onSucess("Conta impressa com sucesso");
                        }
                    });
                }

                @Override
                public void onError(int cause) throws RemoteException {
                    String errorMessage = parseErrorStatus(cause);
                    Log.e(TAG, "Erro na impressão da imagem: " + errorMessage);
                    mainHandler.post(() -> {
                        Toast.makeText(context, "Erro na impressão: " + errorMessage,
                                Toast.LENGTH_LONG).show();
                       // if (statusCallback != null) {
                       //     statusCallback.onError(e);
                       // }
                    });
                }
            });


        } catch (Exception e) {
            Log.e(TAG, "Erro ao imprimir com SDK", e);
            final String errorMessage = e.getMessage();
            mainHandler.post(() -> {
                Toast.makeText(context, "Falha na impressão: " + errorMessage, Toast.LENGTH_LONG).show();
                if (statusCallback != null) {
                    statusCallback.onError(e);
                }
            });
        }
    }

    /**
     * Redimensiona bitmap se necessário (máximo 378 pixels de largura)
     */
    private Bitmap resizeBitmapIfNeeded(Bitmap bitmap) {
        int maxWidth = 378;

        if (bitmap.getWidth() > maxWidth) {
            int newHeight = (bitmap.getHeight() * maxWidth) / bitmap.getWidth();
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true);
            Log.d(TAG, "Bitmap redimensionado de " + bitmap.getWidth() + "x" + bitmap.getHeight() +
                    " para " + maxWidth + "x" + newHeight);
            return resizedBitmap;
        }

        return bitmap;
    }

    /**
     * Converte código de erro em mensagem legível
     */
    private String parseErrorStatus(int cause) {
        switch (cause) {
            case 0: return "OK";
            case 1: return "Imprimindo";
            case 2: return "Impressora não iniciada";
            case 3: return "Impressora superaquecida";
            case 4: return "Fila de impressão muito grande";
            case 5: return "Parâmetros incorretos";
            case 10: return "Porta da impressora aberta";
            case 11: return "Temperatura baixa demais";
            case 12: return "Sem bateria suficiente";
            case 13: return "Motor com problemas";
            case 15: return "Sem bobina";
            case 16: return "Bobina acabando";
            case 17: return "Bobina travada";
            default: return "Erro desconhecido (código: " + cause + ")";
        }
    }

    /**
     * Método para ser chamado quando a permissão for concedida
     */
    public void onPermissionGranted() {
        if (contaPendente != null) {
            proceedWithPrint(contaPendente);
            contaPendente = null;
        }
    }

    /**
     * Verifica status da impressora
     */
    public int getPrinterStatus() {
        try {
            if (PosDigital.getInstance().isInitiated()) {
                return PosDigital.getInstance().getPrinter().getStatus();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao obter status da impressora", e);
        }
        return 2; // ERROR_NOT_INIT
    }

    /**
     * Desconecta do SDK quando não precisar mais
     */
    public void disconnect() {
        try {
            if (PosDigital.getInstance().isInitiated()) {
                PosDigital.unregister(context.getApplicationContext());
                isSDKConnected = false;
                Log.d(TAG, "Desconectado do SDK Getnet");
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao desconectar do SDK", e);
        }
    }

    /**
     * Verifica se o SDK está conectado
     */
    public boolean isConnected() {
        return isSDKConnected && PosDigital.getInstance().isInitiated();
    }
}