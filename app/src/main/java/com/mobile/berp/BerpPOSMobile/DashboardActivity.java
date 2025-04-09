package com.mobile.berp.BerpPOSMobile;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.berp.BerpPOSMobile.Controller.Proxy;
import com.mobile.berp.BerpPOSMobile.model.BerpModel;
import com.mobile.berp.BerpPOSMobile.model.Configuracao;
import com.mobile.berp.BerpPOSMobile.model.Variaveis;
import com.mobile.berp.BerpPosMobile.BuildConfig;
import com.mobile.berp.BerpPosMobile.R;

import java.net.ConnectException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class DashboardActivity extends AppCompatActivity implements View.OnClickListener,OpcoesDialogFragment.OnOptionSelectedListener {

    private CardView itens,mesas,conta,pagamento;
    private Button btnOpcoes, btnSair;
    private ProgressDialog dialog;
    private boolean mesaAtiva, cartaoAtivo, balcaoAtivo,deliveryAtivo;
    private int opcoesAtivas;
    private TextView terminalname,userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        itens = findViewById(R.id.itens);
        mesas = findViewById(R.id.mesas);
        conta = findViewById(R.id.conta);
        pagamento = findViewById(R.id.pagamento);
        btnOpcoes = findViewById(R.id.btnOpcoes);
        btnSair = findViewById(R.id.btnSair);

        terminalname = findViewById(R.id.tvTerminalName);
        userName = findViewById(R.id.tvUserNameAndCode);


        itens.setOnClickListener(this);
        mesas.setOnClickListener(this);
        conta.setOnClickListener(this);
        pagamento.setOnClickListener(this);
        btnOpcoes.setOnClickListener(this);
        btnSair.setOnClickListener(this);



        if (BuildConfig.POS_MODEL.equals("celular")) {
           // pagamento.setVisibility(View.GONE);
            pagamento.setEnabled(false);
            pagamento.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray));
            pagamento.setAlpha(0.5f); // 50% de opacidade


            pagamento.setCardBackgroundColor(getResources().getColor(R.color.gray));
        }



// Opcional: Alterar a cor para indicar que está desabilitado

        Configuracao mesaConfig = Variaveis.getConfiguracao("MOBILE_VENDA_MESA");
        Configuracao cartaoConfig = Variaveis.getConfiguracao("MOBILE_VENDA_CARTAO");
        Configuracao balcaoConfig = Variaveis.getConfiguracao("MOBILE_VENDA_BALCAO");
        Configuracao deliveryConfig = Variaveis.getConfiguracao("MOBILE_VENDA_DELIVERY");

        terminalname.setText(Variaveis.getTerminal_name());
        userName.setText(Variaveis.getUser_name());

        mesaAtiva = mesaConfig.getValor().equalsIgnoreCase("S");
        cartaoAtivo = cartaoConfig.getValor().equalsIgnoreCase("S");
        balcaoAtivo = balcaoConfig.getValor().equalsIgnoreCase("S");
        deliveryAtivo = deliveryConfig.getValor().equalsIgnoreCase("S");



        //mesas.setVisibility(mesaAtiva ? View.VISIBLE : View.GONE);
        //itens.setVisibility(cartaoAtivo ? View.VISIBLE : View.GONE);
        //pagamento.setVisibility(balcaoAtivo ? View.VISIBLE : View.GONE);

        // Verifica quantas opções estão ativas
        opcoesAtivas = 0;
        if (mesaAtiva) opcoesAtivas++;
        if (cartaoAtivo) opcoesAtivas++;
        if (balcaoAtivo && Objects.equals(BerpModel.getSelectedCMD(), "1")) opcoesAtivas++;

        // Se apenas uma opção estiver ativa, inicia a Activity correspondente






    }
    private void showDialog() {
        if (opcoesAtivas==0){
            startVendaActivity("MESA");
        }

        else if (opcoesAtivas == 1) {
            if (mesaAtiva) {
                startVendaActivity("MESA");
            } else if (cartaoAtivo) {
                startVendaActivity("CARTAO");
            } else if (balcaoAtivo) {
                startVendaActivity("BALCAO");
            }
           // Finaliza a DashboardActivity se uma opção foi escolhida
        }
        else {


            OpcoesDialogFragment dialog = OpcoesDialogFragment.newInstance(mesaAtiva, cartaoAtivo, balcaoAtivo);

            dialog.show(getSupportFragmentManager(), "OpcoesDialog");
        }

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Atenção");
            builder.setMessage("Deseja sair?");
            builder.setPositiveButton("SIM", (dialog, id) -> DashboardActivity.this.finish());
            builder.setNegativeButton("NÃO", (dialog, id) -> {
                // User cancelled the dialog
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onOptionSelected(String option) {
        Intent intent = null;

        // Aqui você pode decidir qual Intent iniciar com base na opção selecionada
        switch (option) {
            case "Mesa":

                startVendaActivity("MESA");
                break;
            case "Cartão":

                startVendaActivity("CARTAO");
                break;
            case "Balcão":

                startVendaActivity("BALCAO");
                break;
        }

        if (intent != null) {
            startActivity(intent);
        }
    }


    private void realizarLogout() {


        if (Variaveis.getToken() != null) {
            Proxy.invalidarSessao(Variaveis.getToken()).thenAccept(response -> {
                if (response) {
                    // Remove o token localmente
                    Variaveis.setToken("");

                    // Redireciona para a tela de login
                    Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }).exceptionally(e -> {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erro ao fazer logout: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
                return null;
            });
        }
    }


    private CompletableFuture<Boolean> validarSessao() {
        return Proxy.validarSessao(Variaveis.getToken())
                .thenApply(response -> {
                    if (!response) {
                        // Se a sessão for inválida, limpa o token
                        Variaveis.setToken("");

                        // Retorna falso indicando que a sessão é inválida
                        return false;
                    }
                    return true; // Sessão válida
                }).exceptionally(e -> {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Erro ao validar a sessão: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    return false; // Retorna falso em caso de erro
                });
    }

    @Override
    public void onClick(View view) {


        if (view == itens  ) {

            validarSessao().thenAccept(isValid -> {
                if (isValid) {
                    // Sessão válida, continue com a operação
                    BerpModel.setSelectedCMD("1");
                    showDialog();

                } else {
                    // Sessão inválida, redireciona para a tela de login
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Sessão inválida. Redirecionando para login.", Toast.LENGTH_SHORT).show();
                    });
                    Variaveis.setToken(""); // Limpa o token localmente
                    Intent i = new Intent(this, LoginActivity.class);

                    startActivity(i);
                    finish();
                }
            });





        } else if (view == mesas) {
            validarSessao().thenAccept(isValid -> {
                if (isValid) {
                    // Sessão válida, continue com a operação
                     Intent intent = new Intent(this, ListMesasActivity.class);
                    if (mesaAtiva) intent.putExtra("MESA", "S");
                    if (cartaoAtivo) intent.putExtra("CARTAO", "S");;
                    if (balcaoAtivo) intent.putExtra("BALCAO", "S");;
                    if (deliveryAtivo) intent.putExtra("DELIVERY", "S");;
                    startActivity(intent);
                } else {
                    // Sessão inválida, redireciona para a tela de login
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Sessão inválida. Redirecionando para login.", Toast.LENGTH_SHORT).show();
                    });
                    Variaveis.setToken(""); // Limpa o token localmente
                    Intent i = new Intent(this, LoginActivity.class);
                    startActivity(i);
                    finish();
                }
            });




        } else if (view == conta) {
            validarSessao().thenAccept(isValid -> {
                if (isValid) {
                    // Sessão válida, continue com a operação
                    BerpModel.setSelectedCMD("2");
                    showDialog();
                } else {
                    // Sessão inválida, redireciona para a tela de login
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Sessão inválida. Redirecionando para login.", Toast.LENGTH_SHORT).show();
                    });
                    Variaveis.setToken(""); // Limpa o token localmente
                    Intent i = new Intent(this, LoginActivity.class);
                    startActivity(i);
                    finish();
                }
            });



        } else if (view == pagamento) {
            validarSessao().thenAccept(isValid -> {
                if (isValid) {
                    // Sessão válida, continue com a operação
                    BerpModel.setSelectedCMD("3");
                    showDialog();
                } else {
                    // Sessão inválida, redireciona para a tela de login
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Sessão inválida. Redirecionando para login.", Toast.LENGTH_SHORT).show();
                    });
                    Variaveis.setToken(""); // Limpa o token localmente
                    Intent i = new Intent(this, LoginActivity.class);
                    startActivity(i);
                    finish();
                }
            });

        }else if(view == btnOpcoes){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.opcoes);

            String[] opcoes = {"Carga do sistema", "Trocar de Operador", "Fale conosco","Política de Privacidade"};
            builder.setItems(opcoes, (dialog, which) -> {
                switch (which) {
                    case 0: //Carga do sistema
                        Tarefa tarefa = new Tarefa(getApplicationContext());
                        tarefa.execute("", "", "");
                        break;
                    case 1: // Trocar de Operador
                        startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
                        BerpModel.setFuncionario("erro");
                        DashboardActivity.this.finish();
                        break;
                    case 2: // Fale conosco
                        startActivity(new Intent(DashboardActivity.this, InformationActivity.class));
                        break;
                    case 3: //Política de Privacidade
                        showDialogPoliticsPrivacy();
                        break;
                }
            });
            builder.setNegativeButton("CANCELAR", (dialog, id) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();
        }

        if(view == btnSair){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Atenção");
            builder.setMessage("Deseja sair?");
            builder.setPositiveButton("SIM", (dialog, id) -> realizarLogout());
            builder.setNegativeButton("NÃO", (dialog, id) -> {
                // User cancelled the dialog
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }



    }

    public void showDialogPoliticsPrivacy(){
        //mostra o dialog de politica de privacidade
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Política de Privacidade");
        builder.setView(R.layout.custom_dialog_scrollable);
        builder.setNegativeButton(R.string.fechar, (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });
        builder.show();
    }

    private class Tarefa extends AsyncTask<String, String, Integer> {

        private Context context;
        private String exception;

        Tarefa(Context context) {
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
            if(result == 1){
                Toast.makeText(context, "Carga Efetuada com Sucesso", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(context,exception,Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            dialog.setMessage(values[0]);
        }

        @Override
        protected Integer doInBackground(String... strings) {
            try {
                String funcionariologado = BerpModel.getFuncionario(); //salva o ultimo usuario logado na variavel local
                Proxy.cargas(); //da a carga das variaveis do sistema
                BerpModel.setFuncionario(funcionariologado); //seta na variavel novamente o ultimo usuario logado
            } catch (ConnectException ce) {
                exception = "Não foi possível se conectar ao servidor";
                return 99;
            } catch (Exception e) {
                exception = e.getMessage();
                return 99;
            }
            return 1;

        }
    }

    private void startVendaActivity(String tipoVenda) {
        Intent intent = new Intent(this, NMesaActivity.class);
        intent.putExtra("TP_VEND", tipoVenda);
        intent.putExtra("SENDER", BerpModel.getSelectedCMD());


        startActivity(intent);
    }


}
