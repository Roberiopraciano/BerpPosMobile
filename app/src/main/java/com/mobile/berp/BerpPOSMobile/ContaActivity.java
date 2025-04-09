package com.mobile.berp.BerpPOSMobile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mobile.berp.BerpPOSMobile.Printer.PosDigitalPrinterService;
import com.mobile.berp.BerpPOSMobile.Printer.PrinterStatusCallback;
import com.mobile.berp.BerpPOSMobile.model.BerpModel;
import com.mobile.berp.BerpPOSMobile.model.ContaFields;
import com.mobile.berp.BerpPOSMobile.model.PedidoItemAdapter;
import com.mobile.berp.BerpPOSMobile.Controller.Proxy;
import com.mobile.berp.BerpPOSMobile.model.Produto;
import com.mobile.berp.BerpPOSMobile.model.Variaveis;
import com.mobile.berp.BerpPosMobile.BuildConfig;
import com.mobile.berp.BerpPosMobile.R;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ContaActivity extends AppCompatActivity implements View.OnClickListener {

    private ContaFields mesa;
    private String exceptMsg;
    private int LAYOUT = 1;
    private TextView txtAtendimento, txtAtendente,txtHoraAt ,tvStatus;
    private TextView txtSubTotalConta,txtServiConta,txtDesccConta,txtValorConta,txtvlrContaDiv;
    private ListView list;
    ArrayAdapter<Produto> pedidoAdapter;
    private Button btntipoconta,btnpargarConta, btnLancaItemConta, btnFecharConta, btnVoltarConta;
    private TextView lblAte;
    private TextView lblHora,lblAtendimento;
    private TextView displayInteger;
    private FloatingActionButton decrease,increase;
    private int minteger = 1;
    double vlrdividido;
    private Locale myLocale;
    private ProgressDialog dialog;
    private String sender_view;

    String conta;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conta);

        btntipoconta = findViewById(R.id.btntipoconta);
        btnpargarConta = findViewById(R.id.btnpargarConta);
        btnLancaItemConta = findViewById(R.id.btnLancaItemConta);
        btnFecharConta = findViewById(R.id.btnFecharConta);
        btnVoltarConta = findViewById(R.id.btnVoltarConta);

        txtAtendimento = findViewById(R.id.txtAtendimento);
        txtAtendente = findViewById(R.id.txtAtendente);
        txtHoraAt = findViewById(R.id.txtHoraAt);
        tvStatus = findViewById(R.id.tvStatus);

        txtSubTotalConta = findViewById(R.id.txtSubTotalConta);
        txtServiConta = findViewById(R.id.txtServiConta);
        txtDesccConta = findViewById(R.id.txtDesccConta);
        txtValorConta = findViewById(R.id.txtValorConta);
        txtvlrContaDiv = findViewById(R.id.txtvlrContaDiv);

        lblAtendimento=findViewById(R.id.lblAtendimento);
        decrease = findViewById(R.id.decrease);
        increase = findViewById(R.id.increase);

        lblAte =  findViewById(R.id.tvAtenditemConta);
        lblHora = findViewById(R.id.tvHoraitemConta);
        lblAte.setVisibility(View.GONE);
        lblHora.setVisibility(View.GONE);

        list = findViewById(R.id.listPedidoCONTA);

        btntipoconta.setOnClickListener(this);
        btnpargarConta.setOnClickListener(this);
        btnVoltarConta.setOnClickListener(this);
        btnFecharConta.setOnClickListener(this);
        btnLancaItemConta.setOnClickListener(this);


        if (BuildConfig.POS_MODEL.equals("celular")) {
            btnpargarConta.setEnabled(false);
            btnpargarConta.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray));
        }


        decrease.setOnClickListener(this);
        increase.setOnClickListener(this);

        displayInteger = findViewById(R.id.integer_number);

        myLocale = new Locale("pt", "BR");

        if (getIntent().hasExtra("SENDER_VIEW")){
            Bundle extras = getIntent().getExtras();
            sender_view = extras.getString("SENDER_VIEW");

        }


        ConnServer cs = new ConnServer(this);
        cs.execute("", "", "");




    }

    @Override
    public void onClick(View view) {
        double valorcontadividida;
        //muda o layout da tela - conta simples e detalhado
        if(btntipoconta == view){
            if(LAYOUT == 1){
                LAYOUT = 0;

                pedidoAdapter = new PedidoItemAdapter(this, R.layout.list_itemconta, mesa.getProdutosAsVector(), LAYOUT);
                list.setAdapter(pedidoAdapter);

                lblAte.setVisibility(View.VISIBLE);
                lblHora.setVisibility(View.VISIBLE);

                btntipoconta.setText(R.string.label_simples);
            }else{
                LAYOUT = 1;

                pedidoAdapter = new PedidoItemAdapter(this, R.layout.list_itemconta, mesa.getProdutosAsVector(), LAYOUT);
                list.setAdapter(pedidoAdapter);

                lblAte.setVisibility(View.GONE);
                lblHora.setVisibility(View.GONE);

                btntipoconta.setText(R.string.label_detalhado);


            }

        } else if(btnpargarConta == view){
            //chama a tela de pagamento
            Intent i = new Intent(ContaActivity.this, PagamentoActivity.class);
            startActivity(i);
            this.finish();
        }else if(btnVoltarConta == view){
            //volta pra tela de digitar o numero da mesa
            BerpModel.setSelectedCMD("2");
            if (sender_view!=null) {
                if (sender_view.equals("LISTA_MESAS")) {
                    Intent intent = new Intent(this, ListMesasActivity.class);
                    startActivity(intent);
                }
            }
//            Intent i = new Intent(ContaActivity.this, DashboardActivity.class);
//            startActivity(i);
            this.finish();
        }else if(btnFecharConta == view){
            //se nao for imprimir no aparelho chama a tela pra escolher a impressora que vai imprimir a conta
            Intent i = new Intent(ContaActivity.this, ListaImpressorasActivity.class);
            i.putExtra("NUMERO_CLIENTES",displayInteger.getText().toString());
            startActivityForResult(i,1);



        }else if(btnLancaItemConta == view){
            //caso a conta nao esteja fechada , chama a tela de lancamento de item
            Intent intent = new Intent(ContaActivity.this, ItensMesaActivity.class);
            intent.putExtra("NUMERO_MESA", mesa.getCdMesa());
            intent.putExtra("LOCAL_ENTREGA", mesa.getLocal_entrega());
            intent.putExtra("NOME_CLIENTE", mesa.getVen_nmcli());
            intent.putExtra("ID", mesa.getId());
            intent.putExtra("TP_VEND",mesa.getVen_nmtpvend());
            startActivity(intent);
            this.finish();
        }else if(decrease == view){ // divisao de conta  diminuir
            if (minteger >= 2 ){
                minteger = minteger - 1;
                valorcontadividida = (vlrdividido/minteger);
                txtvlrContaDiv.setText(NumberFormat.getCurrencyInstance(myLocale).format(valorcontadividida));
                display(minteger);
            }
        }else if(increase == view){ // divisao de conta aumentar
            minteger = minteger + 1;
            valorcontadividida = (vlrdividido/minteger);
            txtvlrContaDiv.setText(NumberFormat.getCurrencyInstance(myLocale).format(valorcontadividida));
            display(minteger);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            this.finish();
        }
    }

    private void display(int number) {
        displayInteger.setText(String.valueOf(number));
    }



    private void showCustomDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", (dialog, id) -> {
            if(mesa.getStatus().equals("Aberta")){
                ConnServer cs2 = new ConnServer(getApplicationContext());
                cs2.execute("", "", "");
            }

        });
        builder.show();
    }

    private void atualizarEstadoUI() {
        if (mesa.getStatus().equals("Aberta")) {
            btnpargarConta.setEnabled(true);
            btnpargarConta.setBackgroundColor(getResources().getColor(R.color.gray));

            btnLancaItemConta.setEnabled(true);
            btnLancaItemConta.setBackgroundColor(getResources().getColor(R.color.orang));
        } else {
            btnpargarConta.setEnabled(true);
            btnpargarConta.setBackgroundColor(getResources().getColor(R.color.orang));

            btnLancaItemConta.setEnabled(false);
            btnLancaItemConta.setBackgroundColor(getResources().getColor(R.color.gray));
        }

        // Verificar tipo de venda
        if (Objects.equals(mesa.getVen_tpvend(), "1")) {
            lblAtendimento.setText("Balcão");
            btnLancaItemConta.setEnabled(false);
            btnLancaItemConta.setBackgroundColor(getResources().getColor(R.color.gray));
        } else if (Objects.equals(mesa.getVen_tpvend(), "4")) {
            lblAtendimento.setText("Cartão");
        } else {
            lblAtendimento.setText("Mesa");
        }
    }





    private class ConnServer extends AsyncTask<String, String, Integer> {
        private Context context;

        ConnServer(Context context) {
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
        protected void onPostExecute(Integer result) {

            // TODO Auto-generated catch block
            if (result != 0) {
                Toast.makeText(context, exceptMsg, Toast.LENGTH_LONG).show();
                finish();
               // Intent i = new Intent(context, NMesaActivity.class);
               // startActivity(i);
            } else {
                txtAtendimento.setText(mesa.getCdMesa());
                txtAtendente.setText(mesa.getCdGarcom());

                Date DHoraAbertura = new Date();

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm",new Locale("pt", "BR"));

                try {
                    DHoraAbertura = sdf.parse(mesa.getDataHoraMovimento());
                } catch (ParseException e) {
                    e.printStackTrace();
                }


                String hora = new SimpleDateFormat("HH:mm",new Locale("pt", "BR")).format(DHoraAbertura);

                txtHoraAt.setText(hora);
                tvStatus.setText(mesa.getStatus());

                btnpargarConta.setEnabled(true);
                //Todo verificar se a mesa esta aberta ou fechada
                if(mesa.getStatus().equals("Aberta")){
                    btnpargarConta.setEnabled(true);
                    btnpargarConta.setBackgroundColor(getResources().getColor(R.color.gray));
                }else {

                    if(!btnpargarConta.isEnabled()){
                        btnpargarConta.setEnabled(true);
                        btnpargarConta.setBackgroundColor(getResources().getColor(R.color.orang));
                    }

                    btnLancaItemConta.setEnabled(false);
                    btnLancaItemConta.setBackgroundColor(getResources().getColor(R.color.gray));
                }

                if (Objects.equals(mesa.getVen_tpvend(), "1")) {

                    btnLancaItemConta.setEnabled(false);
                    btnLancaItemConta.setBackgroundColor(getResources().getColor(R.color.gray));
                    lblAtendimento.setText("Balcao");
                }

                if (Objects.equals(mesa.getVen_tpvend(), "2")) {lblAtendimento.setText("Balcao");}

                if (Objects.equals(mesa.getVen_tpvend(),"4")) {lblAtendimento.setText("Cartao");}



                txtSubTotalConta.setText(mesa.getVlrLiquido());
                txtServiConta.setText(mesa.getVlrServico());
                txtDesccConta.setText(mesa.getVlrDesconto());
                txtValorConta.setText(mesa.getVlrBruto());
                txtvlrContaDiv.setText(mesa.getVlrLiquido());


                String contadividida = mesa.getVlrLiquido();

                if (contadividida != null && !contadividida.isEmpty()) {
                    contadividida = contadividida.replace("R$", "").trim(); // Remove "R$" e espaços
                    contadividida = contadividida.replace(",", "."); // Substitui vírgula por ponto

                    try {
                        vlrdividido = Double.parseDouble(contadividida);
                    } catch (NumberFormatException e) {
                        vlrdividido = 0.0; // Valor padrão, ou trate conforme necessário
                        Toast.makeText(context, "Erro ao processar valor da conta: " + contadividida, Toast.LENGTH_LONG).show();
                    }
                } else {
                    vlrdividido = 0.0; // Valor padrão para strings vazias ou nulas
                    Toast.makeText(context, "Valor da conta não disponível.", Toast.LENGTH_LONG).show();
                }



                pedidoAdapter = new PedidoItemAdapter(context, R.layout.list_itemconta, mesa.getProdutosAsVector(), LAYOUT);

                list.setAdapter(pedidoAdapter);

                atualizarEstadoUI();

            }
            dialog.dismiss();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            dialog.setMessage(values[0]);
        }

        @Override
        protected Integer doInBackground(String... values) {
            // TODO Auto-generated method stub
            try {
                //carrega os dados da conta
                mesa = Proxy.visualizaConta(BerpModel.getId(), 0).get();

                if (mesa == null) {
                    exceptMsg = "Dados da conta não disponíveis.";
                    return 1;
                }

                return 0;

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                exceptMsg = e.getMessage();
                return 1;
            }

        }

    }
}
