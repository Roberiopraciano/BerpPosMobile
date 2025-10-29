package br.com.berpsistemas.BerpPOSMobile;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import br.com.berpsistemas.BerpPOSMobile.Controller.Proxy;
import br.com.berpsistemas.BerpPOSMobile.Printer.PrinterStatusCallback;
import br.com.berpsistemas.BerpPOSMobile.model.BerpModel;
import br.com.berpsistemas.BerpPOSMobile.model.ContaFields;
import br.com.berpsistemas.BerpPOSMobile.model.Impressora;
import br.com.berpsistemas.BerpPOSMobile.model.ImpressoraAdapter;
import br.com.berpsistemas.BerpPOSMobile.model.Variaveis;
import br.com.berpsistemas.BerpPOSMobile.Printer.PosDigitalPrinterService;
import br.com.berpsistemas.BerpPOSMobile.R;



import java.util.Vector;

public class ListaImpressorasActivity extends AppCompatActivity implements View.OnClickListener {

    private ImpressoraAdapter impressoraAdapter;
    private String numeroClientes = "1";
    private Button btnVoltarImp;
    private String imp = "";
    private ContaFields conta;
    private ProgressDialog dialog;
    private static final int REQUEST_PERMISSION_CODE = 1001;
    private PosDigitalPrinterService printerService;
    private int positionPendente = -1; // Para armazenar a posição selecionada

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_impressoras);

        btnVoltarImp = findViewById(R.id.btnVoltarImp);
        ListView listImpr = findViewById(R.id.listImp);

        if (getIntent().hasExtra("NUMERO_CLIENTES")) {
            Bundle extras = getIntent().getExtras();
            numeroClientes = extras.getString("NUMERO_CLIENTES");
        }

        Vector<Impressora> impressoras = new Vector<>();
        if (!BuildConfig.POS_MODEL.equals("celular")) {
            Impressora impressoraIntegrada = new Impressora(-1, "Impressora Integrada");
            impressoras.add(impressoraIntegrada);
        }
        impressoras.addAll(Variaveis.getImpressoras());

        impressoraAdapter = new ImpressoraAdapter(this, R.layout.item_imp, impressoras);
        listImpr.setAdapter(impressoraAdapter);

        listImpr.setOnItemClickListener((adapterView, view, position, id) -> {
            for (int i = 0; i < listImpr.getChildCount(); i++) {
                if (position == i) {
                    listImpr.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.yello));
                } else {
                    listImpr.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
                }
            }

            imp = String.valueOf(impressoraAdapter.getItem(position).getCod());

            // Se for impressora integrada, verifica permissões primeiro
            if ("-1".equals(imp)) {
                positionPendente = position;
                if (checkAndRequestPermissions()) {
                    // Se já tem permissão, procede
                    executarProcesso();
                }
                // Se não tem, aguarda o callback de permissão
            } else {
                // Para outras impressoras, procede normalmente
                executarProcesso();
            }
        });

        btnVoltarImp.setOnClickListener(this);
    }

    private boolean checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION_CODE);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissão concedida, executa o processo
                if (positionPendente >= 0) {
                    executarProcesso();
                }
            } else {
                // Permissão negada
                Toast.makeText(this,
                        "Permissão de armazenamento é necessária para usar a impressora integrada",
                        Toast.LENGTH_LONG).show();
                positionPendente = -1;
            }
        }
    }

    private void executarProcesso() {
        Processo processo = new Processo(ListaImpressorasActivity.this);
        processo.execute();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            setResult(RESULT_CANCELED, new Intent());
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View view) {
        if (view == btnVoltarImp) {
            setResult(RESULT_CANCELED, new Intent());
            finish();
        }
    }

    private class Processo extends AsyncTask<Void, Void, String> {
        private Context context;

        Processo(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            try {
                dialog = new ProgressDialog(context);
                dialog.setTitle("Comunicando com o Servidor.");
                dialog.setMessage("Aguarde...");
                dialog.setIndeterminate(true);
                dialog.setCancelable(false);
                dialog.onSaveInstanceState();
                dialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            String retorno = "Sucesso";
            try {
                //carrega os dados da conta
                ContaFields mesa = Proxy.visualizaConta(BerpModel.getId(), 1).get();

                if ("-1".equals(imp)) {
                    // Agora não precisa mais verificar permissão aqui,
                    // pois já foi verificada antes
                    printerService = new PosDigitalPrinterService();
                    printerService.register(context, new PrinterStatusCallback() {
                        @Override
                        public void onConnected() {
                            printerService.print(mesa);
                        }
                        @Override
                        public void onDisconnected() {}
                        @Override
                        public void onSucess(String msg){

                        }
                        @Override
                        public void onError(Exception e) {}
                    });

                }

                retorno = BerpModel.fecharMesa(BerpModel.getNumMesa(),
                        BerpModel.getFuncionario(), imp, numeroClientes, BerpModel.getId());
            } catch (Exception e) {
                e.printStackTrace();
                retorno = e.getMessage();
            }
            return retorno;
        }

        @Override
        protected void onPostExecute(String result) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            showCustomDialog(result);
        }
    }

    private void showCustomDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", (dialog, id) -> {
            setResult(RESULT_OK, new Intent());
            Intent i = new Intent(ListaImpressorasActivity.this, ContaActivity.class);
            startActivity(i);
            finish();
        });
        builder.show();
    }
}