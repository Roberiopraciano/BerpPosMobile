package com.mobile.berp.BerpPOSMobile;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import com.airbnb.lottie.LottieAnimationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mobile.berp.BerpPOSMobile.Controller.Proxy;
import com.mobile.berp.BerpPOSMobile.Controller.WiFi;
import com.mobile.berp.BerpPOSMobile.model.BerpModel;
import com.mobile.berp.BerpPOSMobile.model.Terminal;
import com.mobile.berp.BerpPOSMobile.model.Variaveis;
import com.mobile.berp.BerpPosMobile.R;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btConfig,btiniciar;
    private ImageButton btInfo;
    private Intent it;
    private String exception;
    private ProgressBar progressBar;
    private Terminal terminal;

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




        DadosTerminal();



    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissão concedida", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissão negada", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getApplicationContext(),"Dados de conexao não configurados",Toast.LENGTH_LONG).show();
                }

            }
            else if (view == btInfo){
                it = new Intent(this, InformationActivity.class);
                startActivity(it);
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(),
                    Toast.LENGTH_LONG).show();
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
                        Toast.makeText(context,"Número de Licenças em Uso Atingida.\n Entre em Contato Com o Suporte",Toast.LENGTH_LONG).show();
                        break;
                    case 3:
                        Toast.makeText(context,"Versão do Servidor Imcompatível",Toast.LENGTH_LONG).show();
                        break;
                    case 4:
                        Toast.makeText(context,"Erro ao consultar Status!",Toast.LENGTH_LONG).show();
                        break;
                    case 99:
                        Toast.makeText(context,exception,Toast.LENGTH_LONG).show();
                        break;
                    default:
                        break;
                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
                Toast.makeText(context,"Erro Inesperado",Toast.LENGTH_LONG).show();
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

// Verifica se o terminal e o status não são nulos
                if (terminal != null && terminal.getStatus() != null) {
                    try {
                        // Verifica se o status não está vazio e é um número válido
                        if (!terminal.getStatus().isEmpty() && Integer.parseInt(terminal.getStatus()) > 0) {
                            if (BerpModel.inicializar()) {
                                Variaveis.setDataCarga(Objects.requireNonNull(Variaveis.getConfiguracao("DATA_ULTIMA_CARGA")).getValor());
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
