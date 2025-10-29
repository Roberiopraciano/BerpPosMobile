package br.com.berpsistemas.BerpPOSMobile;

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



import br.com.berpsistemas.BerpPOSMobile.Controller.Proxy;
import br.com.berpsistemas.BerpPOSMobile.model.BerpModel;
import br.com.berpsistemas.BerpPOSMobile.model.Mesa;
import br.com.berpsistemas.BerpPOSMobile.model.Variaveis;
import br.com.berpsistemas.BerpPOSMobile.R;

import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.concurrent.CompletableFuture;

public class NMesaActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btNMesa, btnVoltar;
    private AppCompatEditText edtNMesa, edtLocalEntrega, edtNomeCliente;
    private ProgressBar progressBarNmesa;
    private TextView textViewLabel;
    private String tipoVenda, sender, numTipovenda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nmesa);

        btNMesa = findViewById(R.id.btnContinueNmesa);
        btnVoltar = findViewById(R.id.btnVoltarNmesa);
        edtNMesa = findViewById(R.id.edtxtNMesa);
        progressBarNmesa = findViewById(R.id.progressBarNmesa);
        textViewLabel = findViewById(R.id.textView);
        edtNomeCliente = findViewById(R.id.edtNomeCliente);
        edtLocalEntrega = findViewById(R.id.edtxtLocalEntrega);

        progressBarNmesa.setVisibility(View.GONE);
        btNMesa.setOnClickListener(this);
        btnVoltar.setOnClickListener(this);

        edtNMesa.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        numTipovenda = "0";
        tipoVenda = getIntent().getStringExtra("TP_VEND");
        sender = getIntent().getStringExtra("SENDER");
        if (sender == null) {
            sender = "";
        }
        configurarLabelETipoVenda(tipoVenda);
    }

    private void configurarLabelETipoVenda(String tipoVenda) {
        if (tipoVenda != null) {
            switch (tipoVenda) {
                case "MESA":
                    textViewLabel.setText("Qual o Número da Mesa");
                    numTipovenda = "0";
                    findViewById(R.id.input_layout_mesa).setVisibility(View.VISIBLE);
                    findViewById(R.id.input_layout_local_entrega).setVisibility(View.GONE);
                    findViewById(R.id.input_layout_nome_cliente).setVisibility(View.VISIBLE);
                    if (!"1".equalsIgnoreCase(sender)) {
                        findViewById(R.id.input_layout_nome_cliente).setVisibility(View.GONE);
                    }
                    break;

                case "CARTAO":
                    textViewLabel.setText("Qual o cartão de consumação?");
                    numTipovenda = "4";
                    findViewById(R.id.input_layout_mesa).setVisibility(View.VISIBLE);
                    findViewById(R.id.input_layout_local_entrega).setVisibility(View.VISIBLE);
                    findViewById(R.id.input_layout_nome_cliente).setVisibility(View.VISIBLE);
                    if (!"1".equalsIgnoreCase(sender)) {
                        findViewById(R.id.input_layout_local_entrega).setVisibility(View.GONE);
                        findViewById(R.id.input_layout_nome_cliente).setVisibility(View.GONE);
                    }
                    break;

                case "BALCAO":
                    textViewLabel.setText("Lançamento de novo balcão");
                    numTipovenda = "1";
                    findViewById(R.id.input_layout_mesa).setVisibility(View.GONE);
                    findViewById(R.id.input_layout_local_entrega).setVisibility(View.GONE);
                    findViewById(R.id.input_layout_nome_cliente).setVisibility(View.VISIBLE);
                    break;

                default:
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
                String mesaText = edtNMesa.getText().toString().trim();
                if (!"BALCAO".equalsIgnoreCase(tipoVenda) && mesaText.isEmpty()) {
                    FancyToast.makeText(this, "Preencha o número do atendimento", FancyToast.LENGTH_LONG, FancyToast.INFO, true).show();
                    return;
                }

                if (!"BALCAO".equalsIgnoreCase(tipoVenda) && !BerpModel.maxMesa(mesaText)) {
                    FancyToast.makeText(this, "Posição Inválida", FancyToast.LENGTH_LONG, FancyToast.WARNING, true).show();
                    return;
                }

                new Processo(this).execute();

            } catch (Exception e) {
                edtNMesa.setText("");
            }
        } else if (view == btnVoltar) {
            finish();
        }
    }

    private class Processo extends AsyncTask<Void, Void, String> {
        private Context contextInternal;
        private Intent intent;
        private String numeroMesa;
        private String exceptionMSG;
        private Mesa mesa;

        public Processo(Context context) {
            this.contextInternal = context;
        }

        @Override
        protected void onPreExecute() {
            numeroMesa = edtNMesa.getText().toString().trim();
            progressBarNmesa.setVisibility(View.VISIBLE);

            // Inicializa Mesa
            mesa = new Mesa();
            mesa.setNumMesa(numeroMesa);
            mesa.setTipoVenda(tipoVenda);
            mesa.setNomeCliente(edtNomeCliente.getText().toString().trim());
            mesa.setLocalEntrega(edtLocalEntrega.getText().toString().trim());
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                BerpModel.setNumMesa(numeroMesa);
                BerpModel.setTpvend(tipoVenda);
                BerpModel.setNomeCliente(mesa.getNomeCliente());
                BerpModel.setLocalEntrega(mesa.getLocalEntrega());

                int cmd = Integer.parseInt(BerpModel.getSelectedCMD());
                switch (cmd) {
                    case 1:
                        return handleCommand1().join();
                    case 2:
                        return handleCommand2().join();
                    case 3:
                        handleCommand3().join();
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

        @Override
        protected void onPostExecute(String result) {
            progressBarNmesa.setVisibility(View.GONE);
            try {
                switch (result) {
                    case "intent":
                        startActivity(intent);
                        finish();
                        break;
                    case "Fechada":
                        FancyToast.makeText(NMesaActivity.this, "Reabrindo atendimento...", FancyToast.LENGTH_LONG, FancyToast.INFO, true).show();
                        BerpModel.reAbrirMesaAsync(
                                BerpModel.getId(),
                                success -> runOnUiThread(() -> {
                                    if (success) {
                                        FancyToast.makeText(NMesaActivity.this, "Mesa reaberta com sucesso!", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, true).show();
                                        startItensMesaActivity(mesa);
                                    } else {
                                        FancyToast.makeText(NMesaActivity.this, "Não foi possível reabrir o atendimento.", FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
                                    }
                                }),
                                error -> runOnUiThread(() -> FancyToast.makeText(NMesaActivity.this, "Erro ao reabrir: " + error.getMessage(), FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show())
                        );
                        break;
                    case "Lançar itens":
                        Intent intent = new Intent(NMesaActivity.this, ItensMesaActivity.class);
                        intent.putExtra("NUMERO_MESA", mesa.getNumMesa());
                        intent.putExtra("LOCAL_ENTREGA", mesa.getLocalEntrega());
                        intent.putExtra("NOME_CLIENTE", mesa.getNomeCliente());
                        intent.putExtra("TP_VEND", tipoVenda);
                        startActivity(intent);
                        finish();
                        break;
                    case "nao pagamento":
                        FancyToast.makeText(NMesaActivity.this, "Mesa ou não existe ou não está com status fechado", FancyToast.LENGTH_LONG, FancyToast.WARNING, true).show();
                        break;
                    case "Mesa inválida ou indisponível":
                        new AlertDialog.Builder(NMesaActivity.this)
                                .setTitle("Atenção")
                                .setMessage("Atendimento indisponível")
                                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                .show();
                        break;
                    case "não existe":
                        new AlertDialog.Builder(NMesaActivity.this)
                                .setTitle("Atenção")
                                .setMessage("Mesa não existe")
                                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                .show();
                        break;
                    case "exit":
                        finish();
                        break;
                    case "exception":
                        FancyToast.makeText(NMesaActivity.this, exceptionMSG, FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
                        break;
                }
            } catch (Exception e) {
                FancyToast.makeText(NMesaActivity.this, e.getMessage(), FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
                finish();
            }
        }

        private CompletableFuture<String> handleCommand1() {
            return BerpModel.statusAtendimento(numeroMesa, numTipovenda)
                    .thenCompose(status -> {
                        if (status == 1) {
                            if ("S".equals(Variaveis.getConfiguracao("CELULAR_CARGA_ONLINE", "S").getValor())) {
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
                        } else if (status == 3) {
                            return CompletableFuture.completedFuture("Fechada");
                        }
                        else if (status==0){
                            return CompletableFuture.completedFuture("Lançar itens");
                        }
                        return CompletableFuture.completedFuture("Mesa inválida ou indisponível");
                    });
        }

        private CompletableFuture<String> handleCommand2() {
            return BerpModel.statusMesas(numeroMesa, numTipovenda)
                    .thenCompose(status -> {
                        if (status > 0) {
                            return BerpModel.idAtendimento(numeroMesa, numTipovenda)
                                    .thenApply(idAtendimento -> {
                                        Log.d("idatendimento", "IdAtendimento: " + idAtendimento);
                                        BerpModel.setId(String.valueOf(idAtendimento));
                                        intent = new Intent(NMesaActivity.this, ContaActivity.class);
                                        return "intent";
                                    });
                        }
                        return CompletableFuture.completedFuture("Mesa inválida ou indisponível");
                    });
        }

        private CompletableFuture<Void> handleCommand3() {
            return BerpModel.statusMesas(numeroMesa, numTipovenda)
                    .thenAccept(status -> runOnUiThread(() -> {
                        if (status == 1) {
                            new AlertDialog.Builder(NMesaActivity.this)
                                    .setMessage("Atendimento em Aberto, Deseja fechar?")
                                    .setPositiveButton("Sim", (dialog, which) -> BerpModel.idAtendimento(numeroMesa, numTipovenda)
                                            .thenAccept(idAtendimento -> {
                                                Log.d("idatendimento", "IdAtendimento: " + idAtendimento);
                                                BerpModel.setId(String.valueOf(idAtendimento));
                                                startActivity(new Intent(NMesaActivity.this, ContaActivity.class));
                                            })
                                            .exceptionally(e -> {
                                                Log.e("Erro", "Erro ao obter o ID do atendimento: " + e.getMessage());
                                                return null;
                                            }))
                                    .setNegativeButton("Não", (dialog, which) -> dialog.dismiss())
                                    .show();
                        } else if (status == 3) {
                            startActivity(new Intent(NMesaActivity.this, PagamentoActivity.class));
                        }
                    }));
        }
    }

    private void startItensMesaActivity(Mesa mesa) {
        Intent intent = new Intent(NMesaActivity.this, ItensMesaActivity.class);
        intent.putExtra("NUMERO_MESA", mesa.getNumMesa());
        intent.putExtra("ID", mesa.getId());
        intent.putExtra("NOME_CLIENTE", mesa.getNomeCliente());
        intent.putExtra("LOCAL_ENTREGA", mesa.getLocalEntrega());
        intent.putExtra("TP_VEND", mesa.getTipoVenda());
        intent.putExtra("SENDER_VIEW", "LISTA_MESAS");
        startActivity(intent);
        finish();
    }
}
