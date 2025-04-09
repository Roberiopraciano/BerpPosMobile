package com.mobile.berp.BerpPOSMobile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.mobile.berp.BerpPOSMobile.Controller.Proxy;
import com.mobile.berp.BerpPOSMobile.model.BerpModel;
import com.mobile.berp.BerpPOSMobile.model.ContaFields;
import com.mobile.berp.BerpPOSMobile.model.Impressora;
import com.mobile.berp.BerpPOSMobile.model.ImpressoraAdapter;
import com.mobile.berp.BerpPOSMobile.model.Variaveis;
import com.mobile.berp.BerpPOSMobile.Printer.PosDigitalPrinterService;
import com.mobile.berp.BerpPOSMobile.Printer.PrinterStatusCallback;
import com.mobile.berp.BerpPosMobile.BuildConfig;
import com.mobile.berp.BerpPosMobile.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ListaImpressorasActivity extends AppCompatActivity implements View.OnClickListener {

    private ImpressoraAdapter impressoraAdapter;
    private String numeroClientes = "1";
    private Button btnVoltarImp;
    private String imp = "";
    private ContaFields conta;

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
            Processo processo = new Processo(ListaImpressorasActivity.this);
            processo.execute();
        });

        btnVoltarImp.setOnClickListener(this);
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
        protected String doInBackground(Void... voids) {
            String retorno = "Sucesso";
            try {
                //carrega os dados da conta
                ContaFields mesa = Proxy.visualizaConta(BerpModel.getId(), 0).get();




                if ("-1".equals(imp)) {
                    PosDigitalPrinterService printerService = new PosDigitalPrinterService();
                    printerService.register(context, new PrinterStatusCallback() {
                        @Override
                        public void onConnected() {}
                        @Override
                        public void onDisconnected() {}
                        @Override
                        public void onError(Exception e) {}
                    });
                    printerService.print(mesa);
                }

                retorno = BerpModel.fecharMesa(BerpModel.getNumMesa(), BerpModel.getFuncionario(), imp, numeroClientes, BerpModel.getId());
            } catch (Exception e) {
                e.printStackTrace();
                retorno = e.getMessage();
            }
            return retorno;
        }

        @Override
        protected void onPostExecute(String result) {
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