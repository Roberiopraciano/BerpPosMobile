package com.mobile.berp.BerpPOSMobile;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatEditText;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.mobile.berp.BerpPOSMobile.Controller.Proxy;
import com.mobile.berp.BerpPOSMobile.model.BerpModel;
import com.mobile.berp.BerpPOSMobile.model.Variaveis;
import com.mobile.berp.BerpPosMobile.R;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class NMesaActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btNMesa,btnVoltar;
    private AppCompatEditText edtNMesa,edtLocalEntrega,edtNomeCliente;
    private ProgressBar progressBarNmesa;
    private TextView textViewLabel;
    private String tipoVenda,sender,numTipovenda;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nmesa);

        btNMesa = findViewById(R.id.btnContinueNmesa);
        btnVoltar = findViewById(R.id.btnVoltarNmesa);

        edtNMesa = findViewById(R.id.edtxtNMesa);

        progressBarNmesa = findViewById(R.id.progressBarNmesa);
        progressBarNmesa.setVisibility(View.GONE);

        textViewLabel = findViewById(R.id.textView);

        btNMesa.setOnClickListener(this);
        btnVoltar.setOnClickListener(this);

        edtNomeCliente  =findViewById(R.id.edtNomeCliente);
        edtLocalEntrega = findViewById(R.id.edtxtLocalEntrega);
        edtNMesa.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
        numTipovenda= String.valueOf(0);
        tipoVenda = getIntent().getStringExtra("TP_VEND");
        sender = getIntent().getStringExtra("SENDER");
        if (sender == null) {sender="";};
        configurarLabelETipoVenda(tipoVenda);


    }


    private void configurarLabelETipoVenda(String tipoVenda) {
        if (tipoVenda != null) {
            switch (tipoVenda) {
                case "MESA":
                    textViewLabel.setText("Qual o Numero da Mesa");
                    edtNMesa.setVisibility(View.VISIBLE);
                //    edtNMesa.setHint("Numero Mesa");
                    edtNomeCliente.setVisibility(View.VISIBLE);
                    numTipovenda= String.valueOf(0);
                    findViewById(R.id.input_layout_mesa).setVisibility(View.VISIBLE);
                    findViewById(R.id.input_layout_local_entrega).setVisibility(View.GONE);
                    findViewById(R.id.input_layout_nome_cliente).setVisibility(View.VISIBLE);

                    if (!"1".equalsIgnoreCase(sender)){
                        findViewById(R.id.input_layout_local_entrega).setVisibility(View.GONE);
                        findViewById(R.id.input_layout_nome_cliente).setVisibility(View.GONE);

                    }
                    break;

                case "CARTAO":
                    textViewLabel.setText("Qual o cartão de consumação?");
                    edtNMesa.setVisibility(View.VISIBLE);
                //    edtNMesa.setHint("Numero Cartão");
                    edtNomeCliente.setVisibility(View.VISIBLE);
                    edtLocalEntrega.setVisibility(View.VISIBLE);
                    numTipovenda= String.valueOf(4);
                    findViewById(R.id.input_layout_mesa).setVisibility(View.VISIBLE);
                    findViewById(R.id.input_layout_local_entrega).setVisibility(View.VISIBLE);
                    findViewById(R.id.input_layout_nome_cliente).setVisibility(View.VISIBLE);
                    if (!"1".equalsIgnoreCase(sender)){
                        findViewById(R.id.input_layout_local_entrega).setVisibility(View.GONE);
                        findViewById(R.id.input_layout_nome_cliente).setVisibility(View.GONE);

                    }
                    break;

                case "BALCAO":
                    textViewLabel.setText("Lançamento de novo balcão");
                    edtNMesa.setVisibility(View.GONE);
                    numTipovenda= String.valueOf(1);

                    edtNomeCliente.setVisibility(View.VISIBLE);
                    findViewById(R.id.input_layout_mesa).setVisibility(View.GONE);
                    findViewById(R.id.input_layout_local_entrega).setVisibility(View.GONE);
                    findViewById(R.id.input_layout_nome_cliente).setVisibility(View.VISIBLE);
                    break;

                default:
                    // Caso nenhum parâmetro válido seja enviado, definir label padrão
                    textViewLabel.setText("Número da Mesa");
                    findViewById(R.id.input_layout_mesa).setVisibility(View.VISIBLE);
                    findViewById(R.id.input_layout_local_entrega).setVisibility(View.VISIBLE);
                    findViewById(R.id.input_layout_nome_cliente).setVisibility(View.VISIBLE);
                    break;
            }
        }
    }


    @Override
    public void onClick(View view) {
        if (view == btNMesa) {
            try {
                if (!"BALCAO".equalsIgnoreCase(tipoVenda)) { // Comparação correta ignorando maiúsculas/minúsculas
                    if (edtNMesa.getText().toString().trim().isEmpty()) {
                        Toast.makeText(this, "Preencha o número do atendimento", Toast.LENGTH_LONG).show();
                    }
                    if (BerpModel.maxMesa(edtNMesa.getText().toString().trim())) {
                        Processo processo = new Processo(NMesaActivity.this);
                        processo.execute("", "", "");
                    }
                    else {Toast.makeText(this,"Posicao Invalida ",Toast.LENGTH_LONG).show();}
                } else { // Caso seja "BALCAO"
                    Processo processo = new Processo(NMesaActivity.this);
                    processo.execute("", "", "");
                }
            } catch (Exception e) {
                if (!e.getMessage().equals("A mesa " + BerpModel.getNumMesa() + " já encontra-se aguardando pagamento!!")) {
                    edtNMesa.setText("");
                }
            }
        } else if (view == btnVoltar) {
            finish();
        }
    }



    public class Processo extends AsyncTask<String, String, String> {

        private Context context;
        private Intent intent = null;
        private String numeroMesa;
        private int dataCarga;
        private String exceptionMSG;

        public Processo(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            numeroMesa = edtNMesa.getText().toString();
            progressBarNmesa.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String result) {
            progressBarNmesa.setVisibility(View.GONE);
             try{
                if(result.equals("intent"))
                {
                    startActivity(intent);
                    NMesaActivity.this.finish();
                }
                if(result.equals("Lançar itens"))
                {
                    intent = new Intent(NMesaActivity.this, ItensMesaActivity.class);
                    intent.putExtra("NUMERO_MESA", BerpModel.getNumMesa());
                    intent.putExtra("LOCAL_ENTREGA", edtLocalEntrega.getText().toString());
                    intent.putExtra("NOME_CLIENTE", edtNomeCliente.getText().toString());
                    intent.putExtra("TP_VEND",tipoVenda);
                    startActivity(intent);
                    NMesaActivity.this.finish();
                }

                 if(result.equals("nao pagamento")){
                     Toast.makeText(getApplicationContext(), "Mesa ou não existe ou não está com status fechado", Toast.LENGTH_LONG).show();
                 }

                 if (result.equals("Mesa inválida ou indisponível")) {
                     runOnUiThread(() -> {
                         AlertDialog.Builder builderm = new AlertDialog.Builder(NMesaActivity.this); // Use NMesaActivity.this
                         builderm.setTitle("Atenção");
                         builderm.setMessage("Atendimento Indisponível");
                         builderm.setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss());
                         builderm.show();
                     });
                 }

//                 if (result.equals("Mesa inválida ou indisponível")) {
//
//                     runOnUiThread(() -> {
//                         AlertDialog.Builder builderm = new AlertDialog.Builder(this.context, R.style.AppTheme);
//                         builderm.setTitle("Atenção");
//                         builderm.setMessage("Atendimento Indisponível");
//                         builderm.setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss());
//                         builderm.show();
//                     });
////                     AlertDialog.Builder builderm = new AlertDialog.Builder(this.context, R.style.AppTheme);
////                     builderm.setTitle("Atenção");
////                     builderm.setMessage("Atendimento Indisponível");
////                     builderm.setPositiveButton("OK", (dialogInterface, i) -> {
////                         // Não chame finish() aqui, deixe a Activity aberta
////                         dialogInterface.dismiss();
////                     });
////                     builderm.show();
//                 }

                if(result.equals("não existe"))
                {
                    AlertDialog.Builder builderm = new AlertDialog.Builder(this.context,R.style.AppTheme);
                    builderm.setTitle("Atenção");
                    builderm.setMessage("mesa não existe");
                    builderm.setPositiveButton("OK", (dialogInterface, i) -> {
                        // Não chame finish() aqui, deixe a Activity aberta
                        dialogInterface.dismiss();
                    });
                    builderm.show();

                }
                 if(result.equals("exit"))
                 {
                     finish();
                 }

                 if(result.equals("exception"))
                     Toast.makeText(getApplicationContext(), exceptionMSG, Toast.LENGTH_LONG).show();;

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        }



        private CompletableFuture<String> handleCommand1() {
            return BerpModel.statusAtendimento(numeroMesa, numTipovenda)
                    .thenCompose(status -> {
                        if (status) {
                            if (Variaveis.getConfiguracao("CELULAR_CARGA_ONLINE").getValor().equals("S")) {
                                return Proxy.dataCarga(Variaveis.getNumTerminal(), Variaveis.getDataCarga())
                                        .thenCompose(dataCarga -> {
                                            if (dataCarga > Integer.parseInt(Variaveis.getDataCarga())) {
                                                Variaveis.getProdutos().clear();
                                                Variaveis.getProdutosComb().clear();

                                                return Proxy.cargaProdutos()
                                                        .thenCompose(ignored -> Proxy.cargaProdutosComb())
                                                        .thenRun(() -> Variaveis.setDataCarga(String.valueOf(dataCarga)))
                                                        .thenApply(ignored -> "Lançar itens");
                                            }
                                            return CompletableFuture.completedFuture("Lançar itens");
                                        });
                            }
                            BerpModel.setNumMesa(numeroMesa);
                            return CompletableFuture.completedFuture("Lançar itens");
                        }
                        return CompletableFuture.completedFuture("Mesa inválida ou indisponível");
                    });
        }

        private CompletableFuture<String> handleCommand2() {
            return BerpModel.statusMesas(numeroMesa, numTipovenda)
                    .thenCompose(status -> {
                        if (status > 0) {
                            BerpModel.idAtendimento(numeroMesa, numTipovenda)
                                    .thenCompose(idatendimento -> {
                                        Log.d("idatendimento", "Idatendimento: " + idatendimento);
                                        BerpModel.setId(String.valueOf(idatendimento));
                                        intent = new Intent(NMesaActivity.this, ContaActivity.class);
                                        startActivity(intent);
                                        return CompletableFuture.completedFuture("intent");

                                    });




                        }
                        return CompletableFuture.completedFuture("Mesa inválida ou indisponível");
                    });
        }

        private CompletableFuture<Void> handleCommand3() {
            return BerpModel.statusMesas(numeroMesa, numTipovenda)
                    .thenAccept(status -> runOnUiThread(() -> {
                        if (status == 1) {
                            // Exibe um diálogo perguntando se o usuário deseja fechar
                            AlertDialog.Builder builder = new AlertDialog.Builder(NMesaActivity.this);
                            builder.setMessage("Atendimento em Aberto, Deseja fechar?");
                            builder.setPositiveButton("Sim", (dialog, which) -> {
                                // Vai para a atividade de conta
                                BerpModel.idAtendimento(numeroMesa, numTipovenda)
                                        .thenAccept(idatendimento -> {
                                            Log.d("idatendimento", "Idatendimento: " + idatendimento);
                                            BerpModel.setId(String.valueOf(idatendimento));
                                            Intent intent = new Intent(NMesaActivity.this, ContaActivity.class);
                                            startActivity(intent);
                                        })
                                        .exceptionally(e -> {
                                            Log.e("Erro", "Erro ao obter o ID do atendimento: " + e.getMessage());
                                            return null;
                                        });
                            });
                            builder.setNegativeButton("Não", (dialog, which) -> dialog.dismiss());
                            builder.create().show();
                        } else if (status == 3) {
                            Intent intent = new Intent(NMesaActivity.this, PagamentoActivity.class);
                            startActivity(intent);
                        } else {
                            Log.d("StatusHandler", "Nenhuma ação para este status");
                        }
                    }));
        }



        @Override
        protected String doInBackground(String... strings) {
            try {
                BerpModel.setNumMesa(numeroMesa);
                BerpModel.setTpvend(tipoVenda);
                BerpModel.setNomeCliente(Objects.requireNonNull(edtNomeCliente.getText()).toString());
                BerpModel.setLocalEntrega(Objects.requireNonNull(edtLocalEntrega.getText()).toString());

                switch (Integer.parseInt(BerpModel.getSelectedCMD())) {
                    case 1:
                        return handleCommand1().join(); // Processa o comando 1
                    case 2:
                        return handleCommand2().join(); // Processa o comando 2

                    case 3:
                        handleCommand3().join(); // Processa o comando 3 (não retorna valor)
                        return "Comando 3 processado";
                    default:
                        return "Menu inválido";
                }
            } catch (Exception e) {
                e.printStackTrace();
                exceptionMSG = e.getMessage();
                return "exception";
            }
        }
}}

