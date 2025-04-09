package com.mobile.berp.BerpPOSMobile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatEditText;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mobile.berp.BerpPOSMobile.Controller.Funcoes;
import com.mobile.berp.BerpPOSMobile.model.BerpModel;
import com.mobile.berp.BerpPOSMobile.model.Produto;
import com.mobile.berp.BerpPOSMobile.model.Variaveis;
import com.mobile.berp.BerpPosMobile.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class ItensMesaActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnlimparitem, btnpedido, btnVoltar, btnContinueItemmesa, btncomb;
    private ListView listView1,listView2,listView3,listView4;
    private LinearLayout produto2, produto3, produto4, layout_qtdpreco;
    private AppCompatEditText edtxtcodprod,edtxtcodprod2, edtxtcodprod3, edtxtcodprod4;
    private AppCompatEditText edtxtdescprod,edtxtdescprod2, edtxtdescprod3, edtxtdescprod4;
    private AppCompatEditText edtxtobsproduto;
    private ArrayList<String> proStrings = new ArrayList<>();
    private int LAYOUT = 0;
    private FloatingActionButton decreaseQTD,increaseQTD;
    private TextView txtQTDitem,tvnum_mesa,tvlocal_entrega,tvNome_cliente;
    private TextView txtValorProdT;
    private Intent intent = null;
    private int minteger = 1;
    double vlrdividido;
    private Locale myLocale;
    private String tpVend;
    private String sender_view;
    private String numeroMesa,nomeCliente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itens_mesa);

        btnlimparitem = findViewById(R.id.btnlimparitem);
        btnpedido = findViewById(R.id.btnpedido);
        btnVoltar = findViewById(R.id.btnVoltarItemmesa);
        btnContinueItemmesa = findViewById(R.id.btnContinueItemmesa);
        btncomb = findViewById(R.id.btncomb);

        txtQTDitem = findViewById(R.id.txtQTDitem);
        txtValorProdT = findViewById(R.id.txtValorProdT);
         tvnum_mesa = findViewById(R.id.tvnum_mesa);
         tvlocal_entrega= findViewById(R.id.tvlocal_entrega);
         tvNome_cliente =findViewById(R.id.tvNome_cliente);;

        decreaseQTD = findViewById(R.id.decreaseQTD);
        increaseQTD = findViewById(R.id.increaseQTD);

        produto2 = findViewById(R.id.produto2);
        produto3 = findViewById(R.id.produto3);
        produto4 = findViewById(R.id.produto4);
        layout_qtdpreco = findViewById(R.id.layout_qtdpreco);

        produto2.setVisibility(View.GONE);
        produto3.setVisibility(View.GONE);
        produto4.setVisibility(View.GONE);

        ScrollView scrollView = findViewById(R.id.scrollView);

        listView1 =  findViewById(R.id.listView1);
        listView2 =  findViewById(R.id.listView2);
        listView3 =  findViewById(R.id.listView3);
        listView4 =  findViewById(R.id.listView4);


        listView1.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (listView1.getVisibility() == View.VISIBLE) {
                scrollView.requestDisallowInterceptTouchEvent(true);
            } else {
                scrollView.requestDisallowInterceptTouchEvent(false);
            }
        });

        listView2.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (listView2.getVisibility() == View.VISIBLE) {
                scrollView.requestDisallowInterceptTouchEvent(true);
            } else {
                scrollView.requestDisallowInterceptTouchEvent(false);
            }
        });


        listView3.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (listView3.getVisibility() == View.VISIBLE) {
                scrollView.requestDisallowInterceptTouchEvent(true);
            } else {
                scrollView.requestDisallowInterceptTouchEvent(false);
            }
        });


        listView4.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (listView4.getVisibility() == View.VISIBLE) {
                scrollView.requestDisallowInterceptTouchEvent(true);
            } else {
                scrollView.requestDisallowInterceptTouchEvent(false);
            }
        });


        listView1.setOnItemClickListener(getItemSelecionado());
        listView2.setOnItemClickListener(getItemSelecionado());
        listView3.setOnItemClickListener(getItemSelecionado());
        listView4.setOnItemClickListener(getItemSelecionado());

        edtxtcodprod = findViewById(R.id.edtxtcodprod);
        edtxtcodprod2 = findViewById(R.id.edtxtcodprod2);
        edtxtcodprod3 = findViewById(R.id.edtxtcodprod3);
        edtxtcodprod4 = findViewById(R.id.edtxtcodprod4);

        edtxtdescprod = findViewById(R.id.edtxtdescprod);
        edtxtdescprod2 = findViewById(R.id.edtxtdescprod2);
        edtxtdescprod3 = findViewById(R.id.edtxtdescprod3);
        edtxtdescprod4 = findViewById(R.id.edtxtdescprod4);

        edtxtcodprod.setFilters(new InputFilter[] { new InputFilter.LengthFilter(6) });
        edtxtcodprod.addTextChangedListener(mudarCampos());
        edtxtcodprod2.setFilters(new InputFilter[] { new InputFilter.LengthFilter(6) });
        edtxtcodprod2.addTextChangedListener(mudarCampos());
        edtxtcodprod3.setFilters(new InputFilter[] { new InputFilter.LengthFilter(6) });
        edtxtcodprod3.addTextChangedListener(mudarCampos());
        edtxtcodprod4.setFilters(new InputFilter[] { new InputFilter.LengthFilter(6) });
        edtxtcodprod4.addTextChangedListener(mudarCampos());

        edtxtdescprod.addTextChangedListener(pesquisaDescricao());
        edtxtdescprod2.addTextChangedListener(pesquisaDescricao());
        edtxtdescprod3.addTextChangedListener(pesquisaDescricao());
        edtxtdescprod4.addTextChangedListener(pesquisaDescricao());


        edtxtobsproduto = findViewById(R.id.edtxtobsproduto);

        //se o paramentro estiver habilitado , aparece o botao de combinado habilitado
        if (!BerpModel.permissao("PROD_COMBINADO")) {
            btncomb.setEnabled(false);
            btncomb.setBackgroundColor(getResources().getColor(R.color.gray));
        }

        if(getIntent().hasExtra("NUMERO_MESA")){
            Bundle extras = getIntent().getExtras();
            numeroMesa = extras.getString("NUMERO_MESA");
            tvnum_mesa.setText(String.format("Mesa: %s", numeroMesa));
        }

        if(getIntent().hasExtra("LOCAL_ENTREGA")){
            Bundle extras = getIntent().getExtras();
            String localEntrega = extras.getString("LOCAL_ENTREGA");
            tvlocal_entrega.setText(String.format("Local: %s", localEntrega));
        }

        if(getIntent().hasExtra("NOME_CLIENTE")){
            Bundle extras = getIntent().getExtras();
            nomeCliente = extras.getString("NOME_CLIENTE");
            tvNome_cliente.setText(String.format("Nome: %s", nomeCliente));
        }
        if (getIntent().hasExtra("TP_VEND")) {
            tpVend = getIntent().getStringExtra("TP_VEND");
        }

        if (getIntent().hasExtra("SENDER_VIEW")){
            Bundle extras = getIntent().getExtras();
            sender_view = extras.getString("SENDER_VIEW");

        }

        configurarLayoutPorTipoVenda(tpVend);

        btncomb.setOnClickListener(this);
        btnVoltar.setOnClickListener(this);
        btnContinueItemmesa.setOnClickListener(this);
        btnpedido.setOnClickListener(this);
        btnlimparitem.setOnClickListener(this);

        decreaseQTD.setOnClickListener(this);
        increaseQTD.setOnClickListener(this);

        myLocale = new Locale("pt", "BR");

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
    }

    private void configurarLayoutPorTipoVenda(String tpVend) {
        if (tpVend != null) {
            switch (tpVend) {
                case "MESA":
                    tvnum_mesa.setText(String.format("Mesa: %s", numeroMesa)); // Nome original
                    tvnum_mesa.setVisibility(View.VISIBLE);
                    tvlocal_entrega.setVisibility(View.GONE);
                    tvNome_cliente.setVisibility(View.VISIBLE);
                    break;

                case "CARTAO":
                    tvnum_mesa.setText(String.format("Cartão: %s", numeroMesa)); // Alterar nome para cartão
                    tvnum_mesa.setVisibility(View.VISIBLE);
                    tvlocal_entrega.setVisibility(View.VISIBLE);
                    tvNome_cliente.setVisibility(View.VISIBLE); // Ocultar cliente, se necessário
                    break;

                case "BALCAO":
                    tvnum_mesa.setText(String.format("Balcao: %s", nomeCliente) ); // Alterar nome para balcão
                    tvnum_mesa.setVisibility(View.VISIBLE);
                    tvlocal_entrega.setVisibility(View.GONE);
                    tvNome_cliente.setVisibility(View.GONE);
                    break;

                default:
                    // Exibir todos os elementos para casos não definidos
                    tvnum_mesa.setText("Mesa:");
                    tvnum_mesa.setVisibility(View.VISIBLE);
                    tvlocal_entrega.setVisibility(View.VISIBLE);
                    tvNome_cliente.setVisibility(View.VISIBLE);
                    break;
            }
        } else {
            // Configuração padrão se TP_VEND não for enviado
            tvnum_mesa.setText("Mesa: Geral");
            tvnum_mesa.setVisibility(View.VISIBLE);
            tvlocal_entrega.setVisibility(View.VISIBLE);
            tvNome_cliente.setVisibility(View.VISIBLE);
        }
    }


    public TextWatcher mudarCampos() {

        TextWatcher watcher = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub

                String codigo;

                if(edtxtcodprod.hasFocus()){
                    codigo = Objects.requireNonNull(edtxtcodprod.getText()).toString();
                    edtxtdescprod.setText("");
                    if (!codigo.isEmpty()) {
                        edtxtcodprod.setError(null);
                        if(LAYOUT==0) {
                            edtxtdescprod.setText(BerpModel.prodDesc(codigo));
                            txtValorProdT.setText(NumberFormat.getCurrencyInstance(myLocale).format(Double.parseDouble(BerpModel.prodValor(codigo))));
                        }else {
                            edtxtdescprod.setText(confereItem(codigo, 1));
                        }
                    }else {
                        txtQTDitem.setText("1");
                        minteger = 1;
                        txtValorProdT.setText(NumberFormat.getCurrencyInstance(myLocale).format(0));
                    }
                }
                //LAYOUT 1 - COMBINADO
                if(LAYOUT==1) {
                    if (edtxtcodprod2.hasFocus()) {
                        codigo = Objects.requireNonNull(edtxtcodprod2.getText()).toString();
                        edtxtdescprod2.setText("");
                        if (!codigo.equals("")) {
                            edtxtdescprod2.setText(confereItem(edtxtcodprod2.getText().toString(), 1));
                            if(Objects.requireNonNull(edtxtdescprod2.getText()).toString().equals("Produto Inválido")){
                                edtxtcodprod2.setError("Código do Produto Inválido");
                                if (btnContinueItemmesa.isEnabled())
                                    btnContinueItemmesa.setEnabled(false);
                            }else {
                                if (!btnContinueItemmesa.isEnabled())
                                    btnContinueItemmesa.setEnabled(true);
                            }
                        }
                    }

                    if (edtxtcodprod3.hasFocus()) {
                        codigo = Objects.requireNonNull(edtxtcodprod3.getText()).toString();
                        edtxtdescprod3.setText("");
                        if (!codigo.isEmpty()) {
                            edtxtdescprod3.setText(confereItem(edtxtcodprod3.getText().toString(), 1));
                            if (Objects.requireNonNull(edtxtdescprod3.getText()).toString().equals("Produto Inválido")){
                                edtxtcodprod3.setError("Código do Produto Inválido");
                                if (btnContinueItemmesa.isEnabled())
                                    btnContinueItemmesa.setEnabled(false);
                            }else {
                                if (!btnContinueItemmesa.isEnabled())
                                    btnContinueItemmesa.setEnabled(true);
                            }
                        }
                    }

                    if (edtxtcodprod4.hasFocus()) {
                        codigo = edtxtcodprod4.getText().toString();
                        edtxtdescprod4.setText("");
                        if (!codigo.equals("")) {
                            edtxtdescprod4.setText(confereItem(edtxtcodprod4.getText().toString(), 1));
                            if (edtxtdescprod4.getText().toString().equals("Produto Inválido")){
                                edtxtcodprod4.setError("Código do Produto Inválido");
                                if (btnContinueItemmesa.isEnabled())
                                    btnContinueItemmesa.setEnabled(false);
                            }else {
                                if (!btnContinueItemmesa.isEnabled())
                                    btnContinueItemmesa.setEnabled(true);
                            }
                        }
                    }
                }

            /*    if(edObs1.hasFocus()){
                    codigo = edObs1.getText().toString();
                    edDesc1.setText("");
                    if (!codigo.equals("")) {
                        edDesc1.setText(confereItem(edObs1, 0));
                    }
                }*/

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
//				edtDesc1.setText("");
//				edtDesc2.setText("");
//				edtDesc3.setText("");
//				edtQtd.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        };
        return watcher;

    }

    public TextWatcher pesquisaDescricao() {

        TextWatcher watcher = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                String codigo;

                if (edtxtdescprod.hasFocus()) {
                    edtxtcodprod.setText("");
                    listView2.setAdapter(null);
                    listView3.setAdapter(null);
                    listView4.setAdapter(null);
                    codigo = edtxtdescprod.getText().toString();
                    if(!codigo.equals("")){
                        listView1.setVisibility(View.VISIBLE);

                        if(LAYOUT==0) {
                            preencherAdapter(codigo, 1);
                        }else {
                            preencherAdapter(codigo, 0);
                        }
                    }
                    if (codigo.equals("")) {
                        listView1.setAdapter(null);
                        listView1.setBackgroundColor(Color.TRANSPARENT);
                        listView1.setVisibility(View.GONE);
                    }
                }

                if (edtxtdescprod2.hasFocus()) {
                    edtxtcodprod2.setText("");
                    listView1.setAdapter(null);
                    listView3.setAdapter(null);
                    listView4.setAdapter(null);
                    codigo = edtxtdescprod2.getText().toString();
                    if(!codigo.equals("")){
                        listView2.setVisibility(View.VISIBLE);
                        preencherAdapter(codigo,0);
                        listView2.setBackgroundColor(Color.parseColor("#BF000000"));
                    }
                    if (codigo.equals("")) {
                        listView2.setAdapter(null);
                        listView2.setBackgroundColor(Color.TRANSPARENT);
                        listView2.setVisibility(View.GONE);
                    }
                }

                if (edtxtdescprod3.hasFocus()) {
                    edtxtcodprod3.setText("");
                    listView1.setAdapter(null);
                    listView2.setAdapter(null);
                    listView4.setAdapter(null);
                    codigo = edtxtdescprod3.getText().toString();
                    if(!codigo.equals("")){
                        listView3.setVisibility(View.VISIBLE);
                        preencherAdapter(codigo,0);
                        listView3.setBackgroundColor(Color.parseColor("#BF000000"));
                    }
                    if (codigo.equals("")) {
                        listView3.setAdapter(null);
                        listView3.setBackgroundColor(Color.TRANSPARENT);
                        listView3.setVisibility(View.GONE);
                    }
                }

                if (edtxtdescprod4.hasFocus()) {
                    edtxtcodprod4.setText("");
                    listView1.setAdapter(null);
                    listView3.setAdapter(null);
                    listView2.setAdapter(null);
                    codigo = edtxtdescprod4.getText().toString();
                    if(!codigo.equals("")){
                        listView4.setVisibility(View.VISIBLE);
                        preencherAdapter(codigo,0);
                        listView4.setBackgroundColor(Color.parseColor("#BF000000"));
                    }
                    if (codigo.equals("")) {
                        listView4.setAdapter(null);
                        listView4.setBackgroundColor(Color.TRANSPARENT);
                        listView4.setVisibility(View.GONE);
                    }
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        };

        return watcher;
    }

    private String confereItem(String codigo, int tipo) {

        if (!codigo.equals("")) {
            if (tipo == 1) {
                return BerpModel.prodDescComb(codigo);
            } else {
                return BerpModel.obsDesc(codigo);
            }
        }
        return "";
    }


    private void preencherAdapter(String codigo, int tipo) {
        // TODO Auto-generated method stub

        proStrings = Variaveis.preencherAutoComplete(codigo, tipo);
        ListAdapter pesquisa = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, proStrings);

        if(edtxtdescprod.hasFocus()){
            listView1.setAdapter(pesquisa);
        }
        if(edtxtdescprod2.hasFocus()){
            listView2.setAdapter(pesquisa);
        }
        if(edtxtdescprod3.hasFocus()){
            listView3.setAdapter(pesquisa);
        }
        if(edtxtdescprod4.hasFocus()){
            listView4.setAdapter(pesquisa);
        }
    }

    public AdapterView.OnItemClickListener getItemSelecionado(){


        return (arg0, view, position, index) -> {
            // TODO Auto-generated method stub

            if(edtxtdescprod.hasFocus()){
                edtxtdescprod.setText(proStrings.get(position));
                edtxtcodprod.setText(BerpModel.getProdCodigo(edtxtdescprod.getText().toString()));
                listView1.setVisibility(View.GONE);
                if(LAYOUT==0) {
                    txtValorProdT.setText(NumberFormat.getCurrencyInstance(myLocale).format(Double.parseDouble(BerpModel.prodValor(edtxtcodprod.getText().toString()))));
                }
            }
            if(edtxtdescprod2.hasFocus()){
                edtxtdescprod2.setText(proStrings.get(position));
                edtxtcodprod2.setText(BerpModel.getProdCodigo(edtxtdescprod2.getText().toString()));
                listView2.setVisibility(View.GONE);
            }
            if(edtxtdescprod3.hasFocus()){
                edtxtdescprod3.setText(proStrings.get(position));
                edtxtcodprod3.setText(BerpModel.getProdCodigo(edtxtdescprod3.getText().toString()));
                listView3.setVisibility(View.GONE);
            }
            if(edtxtdescprod4.hasFocus()){
                edtxtdescprod4.setText(proStrings.get(position));
                edtxtcodprod4.setText(BerpModel.getProdCodigo(edtxtdescprod4.getText().toString()));
                listView4.setVisibility(View.GONE);
            }
        };

    }

    private void limpaCampos() {
        edtxtcodprod.requestFocusFromTouch();
        edtxtcodprod.setText("");
        edtxtdescprod.setText("");
        edtxtobsproduto.setText("");
        if(LAYOUT==1){
            edtxtcodprod2.setText("");
            edtxtcodprod3.setText("");
            edtxtcodprod4.setText("");

            edtxtdescprod2.setText("");
            edtxtdescprod3.setText("");
            edtxtdescprod4.setText("");
        }else {
            txtValorProdT.setText(NumberFormat.getCurrencyInstance(myLocale).format(0));
            txtQTDitem.setText("1");
            minteger = 1;
        }

    }

    @Override
    public void onClick(View view) {

        String s = txtValorProdT.getText().toString().replace("R$", "").replace(",", ".").replaceAll("\\s+", "");
        double vlrdividido = new Funcoes().convertStringToDouble(s);
        double vlrunitario = 0.0;



        if(!Objects.requireNonNull(edtxtcodprod.getText()).toString().isEmpty()){
            vlrunitario = Double.parseDouble(BerpModel.prodValor(edtxtcodprod.getText().toString()));
        }

        if(view == btnVoltar){
            //BOTAO DE SAIR DA ACTIVITY


            if (sender_view!=null) {
                if (sender_view.equals("LISTA_MESAS")) {
                    intent = new Intent(this, ListMesasActivity.class);
                }
            }
            ItensMesaActivity.this.finish();
        }else if (view == btnlimparitem){
            //BOTAO DE LIMPAR CAMPOS
            limpaCampos();
        } else if (view == btnContinueItemmesa){
            // FAZ A VERIFICACAO DOS ITENS , CHAMA O DIALOG , QUE PODE O USUARIO INSERIR OUTRO ITEM O IR PARA A TELA DE ENVIR PEDIDO
            try{
                if(!edtxtcodprod.getText().toString().isEmpty()) {
                    if(BerpModel.verificaProduto(edtxtcodprod.getText().toString())){
                    String Obs = Objects.requireNonNull(edtxtobsproduto.getText()).toString();
                    String qtd;
                    if (LAYOUT == 0) {
                        qtd = txtQTDitem.getText().toString().replace('.', ',');
                        BerpModel.addProd(BerpModel.getNumMesa(), edtxtcodprod.getText().toString(), Obs, qtd); // adiciona o produto na mesa
                    } else if (LAYOUT == 1) {
                        int combinado = BerpModel.getCombinadoAtu(BerpModel.getNumMesa()) + 1;
                        int qtdComb = verificaqtdComb();
                        double valueqtd = (1.0 / qtdComb);
                        qtd = String.format("%.3f",valueqtd);
                        BerpModel.addProd(BerpModel.getNumMesa(), edtxtcodprod.getText().toString(), Obs, qtd, combinado);
                        if (!Objects.requireNonNull(edtxtcodprod2.getText()).toString().isEmpty()){
                            BerpModel.addProd(BerpModel.getNumMesa(), edtxtcodprod2.getText().toString(), "", qtd, combinado);
                        }
                        if (!Objects.requireNonNull(edtxtcodprod3.getText()).toString().isEmpty()){
                            if(qtdComb == 3){
                                BerpModel.addProd(BerpModel.getNumMesa(), edtxtcodprod3.getText().toString(), "", "0,334", combinado);
                            }else {
                                BerpModel.addProd(BerpModel.getNumMesa(), edtxtcodprod3.getText().toString(), "", qtd, combinado);
                            }

                        }

                        if (!Objects.requireNonNull(edtxtcodprod4.getText()).toString().isEmpty())
                            BerpModel.addProd(BerpModel.getNumMesa(), edtxtcodprod4.getText().toString(), "", qtd, combinado);
                    }

                    AlertDialog.Builder alertaRemover = new AlertDialog.Builder(
                            ItensMesaActivity.this)
                            .setCancelable(false)
                            .setTitle("Informe o que deseja fazer: ")
                            .setPositiveButton("Adicionar outro item",
                                    (dialog, whichButton) -> limpaCampos())
                            .setNegativeButton("Visualizar pedido",
                                    (dialog, whichButton) -> {
                                        visualizarPedido();
                                    });
                    alertaRemover.setCancelable(false);
                    alertaRemover.show();
                    }else {
                        edtxtcodprod.setError("Código do Produto Inválido");
                    }
                }else {
                    edtxtcodprod.setError("Esse campo deve sempre estar prenchido");
                }

            } catch (Exception e) {
                Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
            }


        } else if (view ==  btncomb) {
            if (BerpModel.permissao("PROD_COMBINADO")) {
                //LAYOUT 0 - LANCAMENTO SIMPLES , LAYOUT 1 LANCAMENTO COMBINADO
                if (LAYOUT == 0) {
                    btncomb.setText(R.string.lancamento_item_simples);
                    produto2.setVisibility(View.VISIBLE);
                    produto3.setVisibility(View.VISIBLE);
                    produto4.setVisibility(View.VISIBLE);
                    layout_qtdpreco.setVisibility(View.INVISIBLE);
                    limpaCampos();
                    LAYOUT = 1;
                } else {
                    btncomb.setText(R.string.lancamento_item_combinado);
                    produto2.setVisibility(View.GONE);
                    produto3.setVisibility(View.GONE);
                    produto4.setVisibility(View.GONE);
                    layout_qtdpreco.setVisibility(View.VISIBLE);
                    limpaCampos();
                    LAYOUT = 0;
                }
            }
        }else if (view == btnpedido) {
            visualizarPedido();

        }else if(decreaseQTD == view){
            //BOTAO DE DIMINUIR QUANTIDADE
            if (minteger >= 2 ){
                vlrdividido = (vlrdividido - vlrunitario);
                minteger = minteger - 1;
                txtValorProdT.setText(NumberFormat.getCurrencyInstance(myLocale).format(vlrdividido));
                display(minteger);
            }
        }else if(increaseQTD == view){
            //BOTAO DE AUMENTAR QUANTIDADE
            if(vlrdividido > 0){

                if (Variaveis.getConfiguracao("CONTROLAR_QTD_MAXIMA_POR_ITEN_VENDA").getValor().equalsIgnoreCase("S")) {
                    Produto P = Variaveis.getProduto(Integer.parseInt(edtxtcodprod.getText().toString()));
                    assert P != null;
                    if (minteger > P.getQtdAparente()) {
                        Toast.makeText(this,"Este Produto so permite um maximo de "+P.getQtdAparente()+" por vez",Toast.LENGTH_LONG).show();
                    }else {
                        minteger = minteger + 1;
                        vlrdividido = (vlrunitario*minteger);
                        txtValorProdT.setText(NumberFormat.getCurrencyInstance(myLocale).format(vlrdividido));
                        display(minteger);
                    }
                }else {
                    minteger = minteger + 1;
                    vlrdividido = (vlrunitario*minteger);
                    txtValorProdT.setText(NumberFormat.getCurrencyInstance(myLocale).format(vlrdividido));
                    display(minteger);
                }
            }

        }

    }

    private void visualizarPedido() {
        intent = new Intent(ItensMesaActivity.this, PedidoActivity.class);
        intent.putExtra("NUMERO_MESA", BerpModel.getNumMesa());
        intent.putExtra("LOCAL_ENTREGA", BerpModel.getLocalEntrega());
        intent.putExtra("NOME_CLIENTE", BerpModel.getNomeCliente());
        intent.putExtra("TP_VEND",BerpModel.getTpvend());
        startActivity(intent);

        finish();
    }

    private void display(int number) {
        txtQTDitem.setText(String.valueOf(number));
    }

    private int verificaqtdComb(){
        //VERIFICA QUANTOS ITENS TEM NA TELA QUANDO TIVER USANDO COMBINADO
        int tot = 1;

        if (!edtxtcodprod2.getText().toString().equalsIgnoreCase("")) {
            tot++;
        }
        if (!edtxtcodprod3.getText().toString().equalsIgnoreCase("")) {
            tot++;
        }
        if (!edtxtcodprod4.getText().toString().equalsIgnoreCase("")) {
            tot++;
        }
        if (tot < 2) {
            throw new IllegalArgumentException("Para combinar produtos devem haver pelo menos 2 produtos");
        }

        return tot;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            if (!BerpModel.isMesaEmpty(BerpModel.getNumMesa())) {

                new AlertDialog.Builder(this).setTitle("AVISO!")
                        .setMessage("Você tem certeza que deseja retornar ao menu principal? (todos os itens lançados neste pedido serão perdidos)")
                        .setPositiveButton("Sim", (dialog, which) -> ItensMesaActivity.this.finish()).setNegativeButton("Cancelar", null)
                        .setCancelable(false).show();

            } else {
                return false;
            }
        }

        return super.onKeyDown(keyCode, event);
    }
}
