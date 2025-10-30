package br.com.berpsistemas.BerpPOSMobile;


import static br.com.berpsistemas.BerpPOSMobile.Controller.Proxy.VersaoServer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import com.airbnb.lottie.LottieAnimationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;


import br.com.berpsistemas.BerpPOSMobile.Controller.Proxy;
import br.com.berpsistemas.BerpPOSMobile.Controller.WiFi;
import br.com.berpsistemas.BerpPOSMobile.application.MyBerpApplication;
import br.com.berpsistemas.BerpPOSMobile.managers.IPosAuthManager;
import br.com.berpsistemas.BerpPOSMobile.model.BerpModel;
import br.com.berpsistemas.BerpPOSMobile.model.Terminal;
import br.com.berpsistemas.BerpPOSMobile.model.Variaveis;
import br.com.berpsistemas.BerpPOSMobile.pagamento.PaymentAuthHelper;
import br.com.berpsistemas.BerpPOSMobile.R;

import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String MIN_SERVER_VERSION = "3.9.92.25";  // Exemplo: ajuste pra sua necessidade

    private Button btConfig,btiniciar;
    private ImageButton btInfo;
    private Intent it;
    private String exception;
    private ProgressBar progressBar;
    private Terminal terminal;
    private AppCompatTextView versaoServer;
    private IPosAuthManager posAuthManager;
    private LottieAnimationView lottieProcessing;

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            startTrackingService();  // Inicia o serviço se a permissão já foi concedida
        }

        btiniciar = findViewById(R.id.btIniciar);
        btConfig = findViewById(R.id.btConfig);
        btInfo = findViewById(R.id.btInfo);
        versaoServer = findViewById(R.id.txtversaoserver);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        btiniciar.setOnClickListener(this);
        btConfig.setOnClickListener(this);
        btInfo.setOnClickListener(this);
        lottieProcessing = findViewById(R.id.lottieProcessing);
        lottieProcessing.cancelAnimation();
        lottieProcessing.setVisibility(View.GONE);



        SharedPreferences prefs = getSharedPreferences("preferencias_1", Context.MODE_PRIVATE);

        Variaveis.setIpServidor(prefs.getString("IP", ""));
        Variaveis.setPortaServidor(prefs.getString("Porta", ""));
        Variaveis.setNumTerminal(prefs.getString("Terminal", ""));
        Variaveis.setImprimirConta(prefs.getBoolean("Imprimir",true));
        Variaveis.setNumeroDispositivo(prefs.getString("Device_id", ""));
        Variaveis.setDevice_id(prefs.getString("Device_id", ""));
        Variaveis.setTerminal_id(prefs.getString("Terminal_id", ""));
        Variaveis.setTerminal_name(prefs.getString("Terminal_Name", ""));
        String ip     = prefs.getString("IP", "");
        String porta  = prefs.getString("Porta", "");
        // se IP ou Porta vazios, avisa e abre ConfigActivity imediatamente
        if (ip.isEmpty() || porta.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Configuração Pendente")
                    .setMessage("Você precisa configurar o IP e a Porta do servidor antes de continuar.")
                    .setCancelable(false)
                    .setPositiveButton("Configurar", (dlg, which) -> {
                        startActivity(new Intent(this, ConfigActivity.class));
                        finish();
                    })
                    .show();
            return;
        }


        DadosTerminal();

        // inicialmente bloqueia o iniciar até a versão do servidor ser validada
        btiniciar.setEnabled(false);
        // chama a checagem de versão
        checkServerVersion();

        // só depois vem o restante:
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);


        // Obtenha a instância do IPosAuthManager da classe Application
        if (getApplication() instanceof MyBerpApplication) {
            posAuthManager = ((MyBerpApplication) getApplication()).getPosAuthManager();
        }

        // Verifique se o posAuthManager foi obtido com sucesso
        if (posAuthManager != null) {
            Log.d("MainActivity", "IPosAuthManager obtido. Iniciando...");
            posAuthManager.initialize(this, new IPosAuthManager.AuthCallback() {
                @Override
                public void onSuccess() {
                    Log.d("MainActivity", "IPosAuthManager.initialize() onSuccess.");
                    // Após a inicialização, verifique se está autenticado.
                    // Se não estiver, solicite a autenticação imediatamente.
                    if (posAuthManager.isAuthenticated()) {
                        Log.i("MainActivity", "POS já está autenticado após inicialização.");
                        // Habilitar funcionalidades principais do app
                    //    habilitarFuncionalidadesPrincipais();
                    } else {
                        Log.i("MainActivity", "POS não está autenticado. Solicitando autenticação agora...");
                        solicitarAutenticacaoPos();
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e("MainActivity", "Erro na inicialização do IPosAuthManager: " + errorMessage);
                    FancyToast.makeText(MainActivity.this, "Erro na config. POS: " + errorMessage, FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
                    // Manter funcionalidades principais desabilitadas ou mostrar mensagem de erro crítica
                    // Considere se o app pode continuar sem a autenticação POS.
                    // Se não puder, talvez mostrar um diálogo para fechar o app ou tentar novamente.
                }
            });
        } else {
            Log.e("MainActivity", "Falha ao obter IPosAuthManager da Application class. App não pode prosseguir corretamente.");
            FancyToast.makeText(this, "Erro crítico: Serviço POS indisponível.", FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
            // Mostrar um diálogo de erro crítico e talvez fechar o app
            new AlertDialog.Builder(this)
                    .setTitle("Erro Crítico")
                    .setMessage("O serviço de pagamento não está disponível. O aplicativo não pode continuar.")
                    .setCancelable(false)
                    .setPositiveButton("Sair", (dialog, which) -> finish())
                    .show();
        }

        // ... resto do seu código onCreate ...
    }

    private void solicitarAutenticacaoPos() {
        if (posAuthManager == null) {
            Log.e("MainActivity", "solicitarAutenticacaoPos chamado, mas posAuthManager é nulo.");
            return;
        }

        // Mostra um feedback para o usuário que a autenticação está sendo solicitada
        FancyToast.makeText(MainActivity.this, "Autorização do POS pendente...", FancyToast.LENGTH_LONG, FancyToast.INFO, true).show();
        // Você pode querer mostrar um ProgressBar aqui também

        posAuthManager.requestAuthentication(MainActivity.this, new IPosAuthManager.AuthCallback() {
            @Override
            public void onSuccess() {
                Log.i("MainActivity", "Autenticação POS bem-sucedida!");
                FancyToast.makeText(MainActivity.this, "POS autenticado com sucesso!", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();
                // Habilitar funcionalidades principais do app

            }

            @Override
            public void onError(String errorMessage) {
                Log.e("MainActivity", "Falha na autenticação POS: " + errorMessage);
                FancyToast.makeText(MainActivity.this, "Falha na autenticação POS: " + errorMessage, FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
                // Manter funcionalidades desabilitadas.
                // O usuário pode precisar tentar novamente ou verificar configurações.
                // Você pode oferecer um botão "Tentar autenticar novamente"
                // ou instruir o usuário a verificar o app da Maquinona/POS.
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Falha na Autenticação")
                        .setMessage("Não foi possível autenticar com o serviço de pagamento: " + errorMessage + "\n\nDeseja tentar novamente?")
                        .setPositiveButton("Tentar Novamente", (dialog, which) -> solicitarAutenticacaoPos())
                        .setNegativeButton("Cancelar", (dialog, which) -> {
                            // Opcional: fechar o app ou deixar funcionalidades restritas
                            // FancyToast.makeText(MainActivity.this, "Operações de pagamento estarão indisponíveis.", FancyToast.LENGTH_LONG, FancyToast.WARNING, true).show();
                        })
                        .show();
            }
        });



        PaymentAuthHelper authHelper = new PaymentAuthHelper(this);
        String token = authHelper.getAccessToken(); // Pegará da Cielo (Variaveis) se o flavor for cielo
        // Ou de SharedPreferences/Default para outros flavors.
        String clientId = authHelper.getClientId();

        if (authHelper.hasBasicCredentials()) {
            // ...
        }



    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                FancyToast.makeText(this, "Permissão concedida", FancyToast.LENGTH_LONG,FancyToast.INFO,true).show();
            } else {
                FancyToast.makeText(this, "Permissão negada",FancyToast.LENGTH_LONG,FancyToast.WARNING,true).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tenta iniciar o serviço novamente ao abrir o app (caso o serviço tenha sido finalizado)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startTrackingService();
        }
    }


    private void startTrackingService() {
//        Intent intent = new Intent(this, TrackingService.class);
//        startService(intent);
      //  Toast.makeText(this, "Rastreamento iniciado", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        try {

            if (view == btConfig) {
                it = new Intent(this, ConfigActivity.class);
                startActivity(it);
            } else if (view == btiniciar) {

                SharedPreferences prefs = getSharedPreferences("preferencias_1", Context.MODE_PRIVATE);

                Variaveis.setIpServidor(prefs.getString("IP", ""));
                Variaveis.setPortaServidor(prefs.getString("Porta", ""));
                Variaveis.setNumTerminal(prefs.getString("Terminal", ""));
                Variaveis.setImprimirConta(prefs.getBoolean("Imprimir",true));
                Variaveis.setNumeroDispositivo(prefs.getString("Device_id", ""));
                Variaveis.setDevice_id(prefs.getString("Device_id", ""));
                Variaveis.setTerminal_id(prefs.getString("Terminal_id", ""));
                Variaveis.setTerminal_name(prefs.getString("Terminal_Name", ""));


                it = new Intent(this, LoginActivity.class);

                if((!Variaveis.getIpServidor().isEmpty()) && (!Variaveis.getPortaServidor().isEmpty())){
                    Processo processo = new Processo(getApplicationContext());
                    processo.execute("", "", "");
                }else{
                    FancyToast.makeText(getApplicationContext(),"Dados de conexao não configurados",FancyToast.LENGTH_LONG,FancyToast.INFO,true).show();
                }

            }
            else if (view == btInfo){
                it = new Intent(this, InformationActivity.class);
                startActivity(it);
            }
        } catch (Exception e) {
            FancyToast.makeText(getApplicationContext(), e.getMessage(),
                    FancyToast.LENGTH_LONG,FancyToast.ERROR,true).show();
        }
    }


    public void DadosTerminal(){
        terminal = new Terminal();

        try {
            //carrega dados do terminal macwifi, ip , versao do app , e numero do terminal
            terminal.setMac(WiFi.getMacWifi(this));
            terminal.setIp(WiFi.getIpWifi(this));
            terminal.setNome(DeviceInfo.getAndroidID(this));
            terminal.setModelo(DeviceInfo.getDeviceModel());
            terminal.setFabricante(DeviceInfo.getFullDeviceName());
           // terminal.setVersao(BuildConfig.VERSION_NAME);
            terminal.setVersaoSO(DeviceInfo.getOSVersion());
            terminal.setNomeDispositivo(DeviceInfo.getUserDeviceName(this));

            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(),0);
            terminal.setVersao(pInfo.versionName);
            terminal.setNumTerminal("0");
            terminal.setDevice_id(Variaveis.getDevice_id());


            SharedPreferences prefs = getSharedPreferences("preferencias_1", Context.MODE_PRIVATE);
            Variaveis.setNumeroDispositivo(prefs.getString("Device_id", ""));
            Variaveis.setNumTerminal(prefs.getString("Terminal", ""));


           // Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("getnet://pagamento/v1/getinfos"));
           // startActivityForResult(intent,REQUEST_CODE);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int REQUEST_CODE = 1001;
        if(REQUEST_CODE == requestCode && RESULT_OK == resultCode){
            String result = data.getStringExtra("result");
            if (result.equals("0")){
             //   String ec = data.getStringExtra("ec");
             //   String numserie = data.getStringExtra("numserie");
                String numlogic = data.getStringExtra("numlogic");
                Variaveis.setNumerologicoPOS(numlogic);
             //   String version = data.getStringExtra("version");
             //   String cnpjEC = data.getStringExtra("cnpjEC");
              //  String nomeEC = data.getStringExtra("nomeEC");
            }

        }

    }

    /**
     * Retorna true se vCurr >= vMin, comparando cada parte numérica.
     */
    private boolean isVersionAtLeast(String vCurr, String vMin) {
        String[] curParts = vCurr.split("\\.");
        String[] minParts = vMin.split("\\.");
        int len = Math.max(curParts.length, minParts.length);

        for (int i = 0; i < len; i++) {
            int c = i < curParts.length ? Integer.parseInt(curParts[i]) : 0;
            int m = i < minParts.length ? Integer.parseInt(minParts[i]) : 0;
            if (c > m) return true;
            if (c < m) return false;
        }
        return true; // são iguais
    }


    private void checkServerVersion() {
        VersaoServer().thenAccept(serverVersao -> {
            runOnUiThread(() -> {
                versaoServer.setText(serverVersao);
                String atual = serverVersao.split("\\s+\\|")[0];
                if (!isVersionAtLeast(atual, MIN_SERVER_VERSION)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Atualização Necessária")
                            .setMessage("Solicite atualização do servidor para a versão mínima " + MIN_SERVER_VERSION)
                            .setCancelable(false)
                            .setPositiveButton("Sair", (d, w) -> finish())
                            .show();
                } else {
                    // versão OK, libera iniciar
                    btiniciar.setEnabled(true);
                }
            });
        }).exceptionally(ex -> {
            // em caso de erro ao chamar o servidor, só mostra o toast
            // e libera o botão pra não travar na tela
            runOnUiThread(() -> {
                FancyToast.makeText(this,
                        "Erro ao verificar versão do servidor:\n" + ex.getMessage(),
                        FancyToast.LENGTH_LONG, FancyToast.ERROR, true
                ).show();
                btiniciar.setEnabled(true);
            });
            return null;
        });
    }




    public class Processo extends AsyncTask<String, String, Integer> {

        // private ProgressDialog progress;
        private final Context context;

        Processo(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
          //  progressBar.setVisibility(View.VISIBLE);

            lottieProcessing.setVisibility(View.VISIBLE);
            lottieProcessing.playAnimation();
            btConfig.setEnabled(false);
            btInfo.setEnabled(false);
            btiniciar.setEnabled(false);
        }

        @Override
        protected void onPostExecute(Integer result) {
            try {
              //  progressBar.setVisibility(View.GONE);
                lottieProcessing.cancelAnimation();
                lottieProcessing.setVisibility(View.GONE);
                btConfig.setEnabled(true);
                btInfo.setEnabled(true);
                btiniciar.setEnabled(true);
                switch (result) {
                    case 1:
                      //  Toast.makeText(context, "Carga Efetuada Com Sucesso", Toast.LENGTH_SHORT).show();
                        startActivity(it);
                        break;
                    case 2:
                        FancyToast.makeText(context,"Número de Licenças em Uso Atingida.\n Entre em Contato Com o Suporte",FancyToast.LENGTH_LONG,FancyToast.INFO,true).show();
                        break;
                    case 3:
                        FancyToast.makeText(context,"Versão do Servidor Imcompatível",FancyToast.LENGTH_LONG,FancyToast.INFO,true).show();
                        break;
                    case 4:
                        FancyToast.makeText(context,"Erro ao consultar Status!",FancyToast.LENGTH_LONG,FancyToast.INFO,true).show();
                        break;
                    case 99:
                        FancyToast.makeText(context,exception,FancyToast.LENGTH_LONG,FancyToast.INFO,true).show();
                        break;
                    default:
                        break;
                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
                FancyToast.makeText(context,"Erro Inesperado",FancyToast.LENGTH_LONG,FancyToast.INFO,true).show();
                e.printStackTrace();
            }
        }

        @Override
        protected Integer doInBackground(String... strings) {
            try
            {
                DadosTerminal();

                terminal = Proxy.checaTerminal(terminal).join();
                Variaveis.setTerminal(terminal);
                Variaveis.setNumTerminal(terminal.getNumTerminal());
                Variaveis.setTerminal_name(terminal.getNome());
                Variaveis.setTerminal_id(terminal.getNumTerminal());

                if (terminal != null && terminal.getStatus() != null) {
                    try {
                        // Verifica se o status não está vazio e é um número válido
                        if (!terminal.getStatus().isEmpty() && Integer.parseInt(terminal.getStatus()) > 0) {
                            if (BerpModel.inicializar()) {
                                Variaveis.setDataCarga(Objects.requireNonNull(Variaveis.getConfiguracao("DATA_ULTIMA_CARGA","1")).getValor());
                                return 1;
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Erro ao converter FSTATUS para número: " + e.getMessage());
                        return 4; // Retorna 4 em caso de erro de conversão
                    }
                } else {
                    // Se terminal ou status forem nulos, retorna 4
                    return 99;
                }

// Caso nenhuma condição seja atendida
                return 4;

            } catch (Exception e) {
                exception = e.getMessage();
                return 99;
            }

        }
    }
}
