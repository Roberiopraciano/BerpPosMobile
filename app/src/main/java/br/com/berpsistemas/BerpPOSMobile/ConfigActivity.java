package br.com.berpsistemas.BerpPOSMobile;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import br.com.berpsistemas.BerpPOSMobile.pagamento.IPagamento;
import br.com.berpsistemas.BerpPOSMobile.BuildConfig;
import br.com.berpsistemas.BerpPOSMobile.R;


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





                    finish();






                } catch (Exception e) {

                    finish();
                }
            }




    }




















}