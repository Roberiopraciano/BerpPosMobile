package com.mobile.berp.BerpPOSMobile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.airbnb.lottie.LottieAnimationView;
import com.mobile.berp.BerpPOSMobile.model.BerpModel;
import com.mobile.berp.BerpPOSMobile.model.PedidoAdapter;
import com.mobile.berp.BerpPOSMobile.model.Produto;
import com.mobile.berp.BerpPosMobile.R;

import java.util.List;

public class PedidoActivity extends AppCompatActivity implements View.OnClickListener {

    private ListView listView;
    private PedidoAdapter adapter;
    private Button btnAddItem, btnEnviarPedido;
    private String 				excption;
    private ProgressDialog dialog;
    private String numeroMesa,localEntrega,nomeCliente,tpVend;
    private LottieAnimationView lottieLoading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedido);

        btnAddItem =  findViewById(R.id.btnAddItem);
        btnAddItem.setOnClickListener(this);
        btnEnviarPedido =  findViewById(R.id.btnEnviarPedido);
        btnEnviarPedido.setOnClickListener(this);
        lottieLoading = findViewById(R.id.lottieLoading);

        listView = findViewById(R.id.list_prods);





        List<Produto> ListItensPedido = BerpModel.listaProdutosMesa(BerpModel.getNumMesa());


        if(getIntent().hasExtra("NUMERO_MESA")){
            Bundle extras = getIntent().getExtras();
             numeroMesa = extras.getString("NUMERO_MESA");

        }

        if(getIntent().hasExtra("LOCAL_ENTREGA")){
            Bundle extras = getIntent().getExtras();
             localEntrega = extras.getString("LOCAL_ENTREGA");
        }

        if(getIntent().hasExtra("NOME_CLIENTE")){
            Bundle extras = getIntent().getExtras();
             nomeCliente = extras.getString("NOME_CLIENTE");
        }
        if (getIntent().hasExtra("TP_VEND")) {
            tpVend = getIntent().getStringExtra("TP_VEND");
        }


        if(ListItensPedido.size() > 0){
            adapter = new PedidoAdapter(this,ListItensPedido);
            listView.setAdapter(adapter);

            atualizarTotalPedido();

            listView.setOnItemClickListener((adapterView, view, i, l) -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Atenção");
                builder.setMessage("Deseja excluir o item?");
                builder.setPositiveButton("SIM", (dialog, id) -> {
                    try {
                        BerpModel.removeProd(BerpModel.getNumMesa(), i);
                        ListItensPedido.remove(i);
                        adapter.notifyDataSetChanged();
                        atualizarTotalPedido();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                builder.setNegativeButton("NÃO", (dialog, id) -> {
                    // User cancelled the dialog
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            });
        }

        Button btnCancelarPedido = findViewById(R.id.btnCancelarPedido);
        btnCancelarPedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Exibe um AlertDialog para confirmar o cancelamento
                new AlertDialog.Builder(PedidoActivity.this)
                        .setTitle("Confirmar Cancelamento")
                        .setMessage("Deseja realmente cancelar o pedido?")
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Lógica para cancelar o pedido
                                Toast.makeText(PedidoActivity.this, "Pedido cancelado", Toast.LENGTH_SHORT).show();
                                finish(); // Fecha a tela (opcional)
                            }
                        })
                        .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Fecha o diálogo sem realizar ação
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

    }

    private void atualizarTotalPedido() {
        List<Produto> listaProdutos = BerpModel.listaProdutosMesa(BerpModel.getNumMesa());
        double total = 0.0;

        for (Produto produto : listaProdutos) {
            total += produto.getValorTotal(); // Supondo que você tenha um método getPreco() e getQuantidade() na classe Produto
        }

        TextView txtTotalPedido = findViewById(R.id.txtTotalPedido);
        txtTotalPedido.setText(String.format("R$ %.2f", total)); // Atualiza o TextView com o total formatado
    }


    @Override
    public void onClick(View view) {
        Intent intent;
        if (view == btnAddItem) {
            intent = new Intent(this, ItensMesaActivity.class);
            intent.putExtra("NUMERO_MESA", numeroMesa);
            intent.putExtra("LOCAL_ENTREGA", localEntrega);
            intent.putExtra("NOME_CLIENTE", nomeCliente);
            intent.putExtra("TP_VEND",tpVend);
            startActivity(intent);
            PedidoActivity.this.finish();

        } else if (view == btnEnviarPedido){
            try {
                boolean enviar = BerpModel.maxMesa(BerpModel.getNumMesa());

                if ("BALCAO".equalsIgnoreCase(tpVend)){enviar=true;}

                if (enviar) {

                    Processo processo = new Processo(getApplicationContext());
                    processo.execute("","","");

                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private class Processo extends AsyncTask<String, String, Integer> {

        //private ProgressDialog progress;
        private Context context;

        public Processo(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            try {

                    lottieLoading.setVisibility(View.VISIBLE);
                    lottieLoading.playAnimation(); // Inicia a animação de carregamento
                TextView loadingMessage = findViewById(R.id.loadingMessage);
                loadingMessage.setVisibility(View.VISIBLE);
                loadingMessage.setText("Enviando pedido, por favor aguarde...");
//                dialog = new ProgressDialog(context);
//                dialog.setTitle("Enviando Pedido");
//                dialog.setMessage("Aguarde");
//                dialog.setIndeterminate(true);
//                dialog.setCancelable(false);
//                dialog.onSaveInstanceState();
//                dialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(Integer result) {

            try {
                lottieLoading.cancelAnimation();
                lottieLoading.setVisibility(View.GONE);

                TextView loadingMessage = findViewById(R.id.loadingMessage);
                loadingMessage.setVisibility(View.GONE);



                if (result == 99) {
                    // Animação de erro com opção de tentar novamente
                    showResultAnimation(R.raw.erro, "Erro ao enviar o pedido. Tente novamente.");
                } else {
                    // Animação de sucesso
                    showResultAnimation(R.raw.sucess, "Pedido enviado com sucesso!");
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }

        }

        @Override
        protected void onProgressUpdate(String... values) {

            TextView loadingMessage = findViewById(R.id.loadingMessage);
            if (values.length > 0) {
                loadingMessage.setText(values[0]);
            }
        }
        @Override
        protected Integer doInBackground(String... arg0) {
            // TODO Auto-generated method stub
            try{
                publishProgress("Verificando duplicidade...");
                excption = "";
                String controleDuplicidade = BerpModel.ControleDuplicidade();
                publishProgress("Enviando pedido...");
                if (BerpModel.enviarPedido(BerpModel.getNumMesa(), BerpModel.getFuncionario(), controleDuplicidade,BerpModel.getTpvend(),BerpModel.getLocalEntrega(),BerpModel.getNomeCliente())) {
                    publishProgress("Limpando mesa...");
                    BerpModel.limpaMesa(BerpModel.getNumMesa());
                    BerpModel.setNumMesa("0");
                }
            }
            catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                excption = e.getMessage();
                return  99;
            }
            return 1;
        }
    }

    private void showCustomDialog(String message, boolean shouldFinish) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setPositiveButton("OK", (dialog, id) -> {
            if (shouldFinish) {
                finish(); // Finaliza a tela apenas se for sucesso
            }
        });
        builder.show();
    }

    private void showResultAnimation(int animationRes, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Cria uma animação Lottie no diálogo
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_animation, null);
        LottieAnimationView lottieResult = dialogView.findViewById(R.id.lottieResult);
        lottieResult.setAnimation(animationRes);
        lottieResult.playAnimation();

        builder.setView(dialogView);
        builder.setMessage(message);

        // Verifica o tipo de animação para configurar os botões
        if (animationRes == R.raw.erro) {
            // Adiciona um botão para tentar novamente
            builder.setPositiveButton("Tentar Novamente", (dialog, id) -> {
                dialog.dismiss(); // Fecha o diálogo
                // Recomeça o processo de envio
                new Processo(getApplicationContext()).execute();
            });
            // Botão para cancelar
            builder.setNegativeButton("Cancelar", (dialog, id) -> {
                dialog.dismiss(); // Fecha o diálogo sem tentar novamente
            });
        } else {
            // Configura o botão padrão para o caso de sucesso
            builder.setPositiveButton("OK", (dialog, id) -> {
                dialog.dismiss(); // Fecha o diálogo
                finish(); // Finaliza a Activity no caso de sucesso
            });
        }

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false); // Impede que o diálogo seja fechado sem interação do usuário
        dialog.show();   }
}
