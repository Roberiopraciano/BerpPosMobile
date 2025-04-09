package com.mobile.berp.BerpPOSMobile;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatEditText;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.berp.BerpPOSMobile.Printer.IPrinter;
import com.mobile.berp.BerpPOSMobile.Printer.IPrinterService;
import com.mobile.berp.BerpPOSMobile.Printer.PrinterAlignMode;
import com.mobile.berp.BerpPOSMobile.Printer.PrinterCallback;
import com.mobile.berp.BerpPOSMobile.Printer.PrinterFactory;
import com.mobile.berp.BerpPOSMobile.Printer.PrinterFontFormat;
import com.mobile.berp.BerpPOSMobile.Printer.PrinterServiceFactory;
import com.mobile.berp.BerpPOSMobile.Printer.PrinterStatusCallback;
import com.mobile.berp.BerpPOSMobile.model.BerpModel;
import com.mobile.berp.BerpPOSMobile.model.ContaFields;
import com.mobile.berp.BerpPOSMobile.Controller.CpfCnpjMaks;
import com.mobile.berp.BerpPOSMobile.Controller.Proxy;
import com.mobile.berp.BerpPOSMobile.model.Pagamento;
import com.mobile.berp.BerpPOSMobile.model.Variaveis;
import com.mobile.berp.BerpPOSMobile.pagamento.IPagamento;
import com.mobile.berp.BerpPOSMobile.pagamento.PagamentoFactory;
import com.mobile.berp.BerpPOSMobile.pagamento.PaymentConfig;
import com.mobile.berp.BerpPosMobile.BuildConfig;
import com.mobile.berp.BerpPosMobile.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PagamentoActivity extends AppCompatActivity implements View.OnClickListener, PaymentOptionsFragment.PaymentOptionsListener {

    /*==================== Constantes ====================*/
    private final int REQUEST_CODE = 1001;
    private final String ARG_RESULT = "result";
    private final String ARG_RESULT_DETAILS = "resultDetails";
    private final String ARG_AMOUNT = "amount";
    private final String ARG_TYPE = "type";
    private final String ARG_INPUT_TYPE = "inputType";
    private final String ARG_INSTALLMENTS = "installments";
    private final String ARG_NSU = "nsu";
    private final String ARG_BRAND = "brand";
    private final String ARG_CVNUMBER = "cvNumber";
    private final String ARG_CARDBIN = "cardBin";
    private final String ARG_CARDLASTDIGITS = "cardLastDigits";
    private final String ARG_AUT = "authorizationCode";
    private final String ARG_NSU_LOCAL = "nsuLocal";

    private final String AMOUNT = "amount";
    private final String ORDER_ID = "order_id";
    private final String EDITABLE_AMOUNT = "editable_amount";
    private final String TRANSACTION_TYPE = "transaction_type";
    private final String INSTALLMENT_TYPE = "installment_type";
    private final String INSTALLMENT_COUNT = "installment_count";
    private final String RETURN_SCHEME = "return_scheme";
    private final String TAG = "SendDeeplinkPayment";

    /*==================== Variáveis Globais ====================*/
    private String funcao;

    private Button btnVoltarPag, btnContinuePag, btnFinalizePag;
    private AppCompatEditText edtxtVlrPaga;
    private Spinner spinner;
    private ArrayList<String> listpagamentos;
    private ArrayAdapter<String> adapter;
    private TextView txtvlrsubtotal, txtfaltapagar, txtLabelAtendimento, txtVlrtroco, txtvlrservico, txttotalLabel;
    private Locale myLocale;
    private ContaFields mesa;
    private String exceptMsg;
    private double pagar;
    private double valor;
    private ProgressDialog dialog;
    boolean sucesso;

    /*==================== Métodos do Ciclo de Vida ====================*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagamento);

        // Inicializa as views
        txtvlrsubtotal = findViewById(R.id.subtotal);
        txtfaltapagar = findViewById(R.id.remainingAmount);
        txtVlrtroco = findViewById(R.id.troco);
        txtvlrservico = findViewById(R.id.taxes);
        btnVoltarPag = findViewById(R.id.btnCancel);
        btnContinuePag = findViewById(R.id.btnAddPayment);
        btnFinalizePag = findViewById(R.id.btnFinalize);
        edtxtVlrPaga = findViewById(R.id.edtxtVlrPaga);
        txtLabelAtendimento = findViewById(R.id.mesaLabel);
        txttotalLabel = findViewById(R.id.totallabel);
        // spinner = findViewById(R.id.spinner);
        ListView listPedido = findViewById(R.id.listPedido);

        // Configura os listeners de botões
        btnVoltarPag.setOnClickListener(this);
        btnContinuePag.setOnClickListener(this);
        btnFinalizePag.setOnClickListener(this);
        btnFinalizePag.setEnabled(true);

        listpagamentos = new ArrayList<>();
        myLocale = new Locale("pt", "BR");

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listpagamentos);
        listPedido.setAdapter(adapter);

        // Configura os eventos do EditText
        edtxtVlrPaga.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    edtxtVlrPaga.post(new Runnable() {
                        @Override
                        public void run() {
                            edtxtVlrPaga.selectAll();
                        }
                    });
                }
            }
        });

        edtxtVlrPaga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtxtVlrPaga.selectAll();
            }
        });

        edtxtVlrPaga.setFilters(new InputFilter[]{
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end,
                                               Spanned dest, int dstart, int dend) {
                        try {
                            String newVal = dest.toString().substring(0, dstart)
                                    + source.toString()
                                    + dest.toString().substring(dend);
                            newVal = newVal.replace("R$", "").replace(" ", "").replace(",", ".");

                            if (newVal.isEmpty()) {
                                return null;
                            }

                            double input = Double.parseDouble(newVal);
                            if (input > 9999.99) {
                                return "";
                            }
                        } catch (NumberFormatException nfe) {
                            // Permite alteração caso não seja possível converter
                        }
                        return null;
                    }
                }
        });

        edtxtVlrPaga.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            private String current = "";

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (!s.toString().equals(current)) {
                    // Nesse bloco monta a máscara para money
                    edtxtVlrPaga.removeTextChangedListener(this);
                    String cleanString = s.toString().replaceAll("[R$,.]", "");

                    if (cleanString.isEmpty()) {
                        cleanString = "0";
                    }

                    double parsed = Double.parseDouble(cleanString);
                    String formatted = NumberFormat.getCurrencyInstance(myLocale).format((parsed / 100));
                    current = formatted;
                    edtxtVlrPaga.setText(formatted);
                    edtxtVlrPaga.setSelection(formatted.length());

                    edtxtVlrPaga.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // Getnet pediu pra colocar um comprovante de pagamento
        if (Variaveis.getConfiguracao("COMPROVANTE_NAOFISCAL_POS").getValor().equals("S")) {
            verificaStatusImpressora(getApplicationContext());
        }

        // Chama a conexão com o servidor
        ConnServer cs = new ConnServer(this);
        cs.execute("", "", "");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            cancelaPagamento();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try {
            Log.i("onNewIntent", intent.getDataString());
            if (intent.getDataString() != null) {
                Toast.makeText(this, intent.getDataString(), Toast.LENGTH_LONG).show();
                Log.i("DeeplinkPay Berp", intent.getDataString());
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("Deeplink berp error", e.getMessage());
        }
    }

    /*==================== Handlers de Eventos ====================*/
    @Override
    public void onClick(View view) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority("pay");
        uriBuilder.scheme("payment-app");
        uriBuilder.appendQueryParameter(RETURN_SCHEME, "berpstoneTest");

        AlertDialog.Builder builder;
        if (view == btnVoltarPag) {
            cancelaPagamento();
        } else if (view == btnContinuePag) {
            btnContinuePag.setEnabled(false);
            String vlrtroco;
            String s;
            valor = 0.0;
            pagar = 0.0;

            esconderTeclados();

            // Tratamento para valores nulos ou inválidos
            if (edtxtVlrPaga.getText() != null && !edtxtVlrPaga.getText().toString().trim().isEmpty()) {
                try {
                    // Carrega o valor do pagamento digitado
                    s = edtxtVlrPaga.getText().toString().replace("R$", "").replace(",", ".").trim();
                    valor = Double.parseDouble(s);
                } catch (NumberFormatException e) {
                    valor = 0.0; // Valor padrão se a conversão falhar
                }
            }

            if (valor > 0) {
                // Salva o valor a pagar na variável
                if (txtfaltapagar.getText() != null && !txtfaltapagar.getText().toString().trim().isEmpty()) {
                    try {
                        String pag = txtfaltapagar.getText().toString().replace("R$", "").replace(",", ".").trim();
                        pagar = Double.parseDouble(pag);
                    } catch (NumberFormatException e) {
                        pagar = 0.0; // Valor padrão se a conversão falhar
                    }
                }

                // Verifica o número de pagamentos
                if (listpagamentos.size() == 9 && pagar > valor) {
                    builder = new AlertDialog.Builder(this);
                    builder.setTitle("Atenção");
                    builder.setMessage("O número máximo de pagamentos é " +
                            Variaveis.getConfiguracao("MAX_PAGAMENTOS_POS").getValor() +
                            ". O valor do último pagamento deve ser igual.");
                    builder.setPositiveButton("OK", (dialog, id) -> edtxtVlrPaga.setText(
                            NumberFormat.getCurrencyInstance(myLocale).format(pagar)));
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    // Suponha que você tenha as variáveis "paymentId" e "valor" definidas
                    String paymentId = "1234"; // ou outro identificador
                    double valor = this.valor; // valor já convertido

                    PaymentOptionsFragment fragment = PaymentOptionsFragment.newInstance(paymentId, valor);

                    // Exibe o container do fragment
                    FrameLayout container = findViewById(R.id.fragment_container);
                    container.setVisibility(View.VISIBLE);
                    // Inicia o fragment de opções de pagamento
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit();

                    // Código comentado para pagamentos diretos (dinheiro/cartão) permanece inalterado
                    // switch (spinner.getSelectedItemPosition()) {
                    //     case 0: // DINHEIRO
                    //         ...
                    //         break;
                    //     case 1: // CARTÃO
                    //         ...
                    //         break;
                    // }
                }
            } else {
                btnContinuePag.setEnabled(true);
                builder = new AlertDialog.Builder(this);
                builder.setTitle("Atenção");
                builder.setMessage("O valor deve ser maior que 0,00!");
                builder.setPositiveButton("OK", (dialog, id) -> edtxtVlrPaga.setText(
                        NumberFormat.getCurrencyInstance(myLocale).format(pagar)));
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        } else if (view == btnFinalizePag) {

            btnFinalizePag.setEnabled(false);
            // showDialogCliente();

            IPagamento pagamento = PagamentoFactory.criarPagamento(this);

            // Cria a configuração do pagamento (ex.: R$100,00, crédito, orderId "ORDER123")
            PaymentConfig config = new PaymentConfig(10000, "debit", "ORDER123");
            if (BuildConfig.POS_MODEL.equals("pagseguro")) {
                pagamento.iniciarPagamentoProvider(this, config);
                Log.d(TAG, "Injetando fragment de pagamento");
                FrameLayout container = findViewById(R.id.fragment_container);
                container.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new PlugPagFragment())
                        .commit();
                Log.d(TAG, "Fragment injetado");
            } else {
                pagamento.iniciarPagamentoDeeplink(this, config);
            }
        }
    }

    @Override
    public void onPaymentMethodSelected(String method) {
        // Fecha o fragment
        getSupportFragmentManager().popBackStack();
        findViewById(R.id.fragment_container).setVisibility(View.GONE);

        // Continue o fluxo de pagamento baseado no método selecionado
        switch (method) {
            case "dinheiro":
                Toast.makeText(this, "Pagamento em Dinheiro", Toast.LENGTH_SHORT).show();
                break;
            case "credito":
                Toast.makeText(this, "Pagamento com Cartão de Crédito", Toast.LENGTH_SHORT).show();
                break;
            case "debito":
                Toast.makeText(this, "Pagamento com Cartão de Débito", Toast.LENGTH_SHORT).show();
                break;
            case "pix":
                Toast.makeText(this, "Pagamento com Pix", Toast.LENGTH_SHORT).show();
                break;
        }
        btnContinuePag.setEnabled(true);
        // Aqui você pode dar continuidade no fluxo de pagamento (ex.: chamar método de pagamento, etc.)
    }

    @Override
    public void onPaymentOptionsCancelled() {
        // Apenas fecha o fragment
        getSupportFragmentManager().popBackStack();
        findViewById(R.id.fragment_container).setVisibility(View.GONE);
        btnContinuePag.setEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK == resultCode && REQUEST_CODE == requestCode) {

            String result = data.getStringExtra(ARG_RESULT);
            String resultDetails = data.getStringExtra(ARG_RESULT_DETAILS);
            // Resultados da requisição com a API da Getnet
            Log.d("Pagamento", "Result: " + result);
            Log.d("Pagamento", "ResultDetails: " + resultDetails);

            switch (result) {
                case "0":
                    if (funcao.equals("pagamento")) {

                        String type = data.getStringExtra(ARG_TYPE); // 02 - Débito, 11 - Crédito à vista, etc.
                        String amount = data.getStringExtra(ARG_AMOUNT); // valor
                        String nsu = data.getStringExtra(ARG_NSU); // nsu
                        String brand = data.getStringExtra(ARG_BRAND); // Bandeira do cartão
                        String authorizationCode = data.getStringExtra("authorizationCode"); // Código único de autorização

                        String inputType = data.getStringExtra(ARG_INPUT_TYPE);  // 021 - tarja magnética, etc.
                        String installments = data.getStringExtra(ARG_INSTALLMENTS); // parcelas
                        String cvNumber = data.getStringExtra(ARG_CVNUMBER); // Número do CV
                        String cardBin = data.getStringExtra(ARG_CARDBIN); // Os 6 primeiros dígitos do cartão
                        String cardLastDigits = data.getStringExtra(ARG_CARDLASTDIGITS); // Os 4 últimos dígitos do cartão
                        String aut = data.getStringExtra(ARG_AUT); // Código único de autorização
                        String nsuLocal = data.getStringExtra(ARG_NSU_LOCAL); // NSU gerado no terminal

                        Log.d("Pagamento", "amount: " + amount);
                        Log.d("Pagamento", "type: " + type);
                        Log.d("Pagamento", "inputType: " + inputType);
                        Log.d("Pagamento", "installments: " + installments);
                        Log.d("Pagamento", "cvNumber: " + cvNumber);
                        Log.d("Pagamento", "cardBin: " + cardBin);
                        Log.d("Pagamento", "cardLastDigits: " + cardLastDigits);
                        Log.d("Pagamento", "brand: " + brand);
                        Log.d("Pagamento", "aut: " + aut);
                        Log.d("Pagamento", "nsu: " + nsu);
                        Log.d("Pagamento", "nsuLocal: " + nsuLocal);

                        amount = amount.substring(0, 10) + "." + amount.substring(10);

                        int cadfpaga;
                        if (type.equals("02")) {
                            cadfpaga = 86; // para débito
                        } else {
                            cadfpaga = 87; // para crédito
                        }
                        // Adiciona na lista de pagamentos da mesa
                        BerpModel.addPag(BerpModel.getNumMesa(), cadfpaga, Double.parseDouble(amount), 2, nsu, authorizationCode, brand, cvNumber);
                        String vlrpagar = NumberFormat.getCurrencyInstance(myLocale).format(pagar - valor);
                        txtfaltapagar.setText(vlrpagar);
                        edtxtVlrPaga.setText(vlrpagar);
                        // Adiciona na listview
                        listpagamentos.add(NumberFormat.getCurrencyInstance(myLocale).format(Double.parseDouble(amount)) + " " + spinner.getSelectedItem());
                        adapter.notifyDataSetChanged();

                        if ((pagar - valor) == 0) {
                            btnContinuePag.setBackgroundColor(getResources().getColor(R.color.gray));
                            btnContinuePag.setEnabled(false);
                            btnFinalizePag.setEnabled(true);
                        } else {
                            btnContinuePag.setEnabled(true);
                        }

                        chamarReimpressao();

                        Toast.makeText(PagamentoActivity.this, resultDetails, Toast.LENGTH_LONG).show();
                    } else if (funcao.equals("estorno")) {
                        Toast.makeText(PagamentoActivity.this, resultDetails, Toast.LENGTH_SHORT).show();
                        sucesso = true;
                    }
                    break;
                case "1":
                    Toast.makeText(PagamentoActivity.this, resultDetails, Toast.LENGTH_LONG).show();
                    if (funcao.equals("pagamento")) {
                        btnContinuePag.setBackgroundColor(getResources().getColor(R.color.yello));
                        btnContinuePag.setEnabled(true);
                    }
                    break;
                case "2":
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Atenção");
                    builder.setMessage("Transação cancelada pelo Usuario/Servidor");
                    builder.setPositiveButton("OK", (dialogInterface, i) -> {
                        if (funcao.equals("pagamento")) {
                            btnContinuePag.setBackgroundColor(getResources().getColor(R.color.yello));
                            btnContinuePag.setEnabled(true);
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    break;
                case "3":
                    Toast.makeText(PagamentoActivity.this, resultDetails, Toast.LENGTH_LONG).show();
                    if (funcao.equals("pagamento")) {
                        btnContinuePag.setBackgroundColor(getResources().getColor(R.color.yello));
                        btnContinuePag.setEnabled(true);
                    }
                    break;
                case "4":
                    Toast.makeText(PagamentoActivity.this, resultDetails, Toast.LENGTH_LONG).show();
                    if (funcao.equals("pagamento")) {
                        btnContinuePag.setBackgroundColor(getResources().getColor(R.color.yello));
                        btnContinuePag.setEnabled(true);
                    }
                    break;
            }
        } else if (RESULT_CANCELED == resultCode) {
            btnContinuePag.setEnabled(true);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Atenção");
            builder.setMessage("Transação cancelada pelo Usuario/Servidor");
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }



    public void imprimeComprovantePagamento(String comp) {
        IPrinter printer = PrinterFactory.getPrinterInstance(getApplicationContext());
        if (printer == null) {
            Toast.makeText(getApplicationContext(), "Serviço de impressão não habilitado", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            printer.init();
            printer.setGray(5);
            printer.setFontFormat(PrinterFontFormat.MEDIUM);
            printer.addText(PrinterAlignMode.LEFT, comp);
            printer.print(new PrinterCallback() {
                @Override
                public void onSuccess() {
                    // Impressão realizada com sucesso
                }

                @Override
                public void onError(int errorCode) {
                    String errorMsg = getErrorMessage(errorCode);
                    Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String getErrorMessage(int errorCode) {
        switch (errorCode) {
            case 1: // Exemplo de código de erro
                return "Impressora não iniciada";
            case 2:
                return "Impressora superaquecida";
            // Adicione os demais códigos conforme sua implementação
            default:
                return "Erro desconhecido (" + errorCode + ")";
        }
    }

    /*==================== Classes Internas (AsyncTask) ====================*/
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

        private boolean isNullOrBlank(String str) {
            return str == null || str.trim().isEmpty();
        }

        @Override
        protected Integer doInBackground(String... values) {
            try {
                if (BerpModel.verificaPagamentos(BerpModel.getNumMesa())) {
                    mesa = Proxy.visualizaConta(BerpModel.getId(), 0).get();
                    return 0;
                } else {
                    return 1;
                }
            } catch (Exception e) {
                e.printStackTrace();
                exceptMsg = e.getMessage();
                return 99;
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            dialog.setMessage(values[0]);
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }

            if (result == 1) {
                // Operação concluída com sucesso
                BerpModel.setSelectedCMD("3");
                finish();
                Intent i = new Intent(context, NMesaActivity.class);
                context.startActivity(i);
            } else if (result == 0) {
                // Atualizar valores na interface
                String vlrLiquido = mesa.getVlrLiquido();
                String vlrServico = mesa.getVlrServico();
                String vlrBruto = mesa.getVlrBruto();

                // Verificar se os valores estão nulos ou contêm apenas espaços
                txtvlrsubtotal.setText(isNullOrBlank(vlrLiquido) ? "R$0,00" : vlrLiquido);
                txtvlrservico.setText(isNullOrBlank(vlrServico) ? "R$0,00" : vlrServico);
                txtfaltapagar.setText(isNullOrBlank(vlrBruto) ? "R$0,00" : vlrBruto);
                txtLabelAtendimento.setText(new StringBuilder().append(mesa.getVen_nmtpvend()).append(" ").append(mesa.getCdMesa()).toString());
                txttotalLabel.setText(new StringBuilder().append("Total: ").append(mesa.getVlrBruto()).toString());

                // Converter valor bruto para pagamento
                if (!isNullOrBlank(vlrBruto)) {
                    String vlrPagam = vlrBruto.replace("R$", "").replace(",", ".");
                    try {
                        double vvlrpagar = Double.parseDouble(vlrPagam.trim());
                        edtxtVlrPaga.setText(NumberFormat.getCurrencyInstance(myLocale).format(vvlrpagar));
                        edtxtVlrPaga.setSelection(0, Objects.requireNonNull(edtxtVlrPaga.getText()).length());
                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "Erro ao processar o valor de pagamento.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    edtxtVlrPaga.setText("R$0,00");
                }
                edtxtVlrPaga.requestFocus();

                // Opcional: Abrir o teclado automaticamente
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            } else {
                // Erro ou exceção
                Toast.makeText(context, exceptMsg == null ? "Erro desconhecido." : exceptMsg, Toast.LENGTH_LONG).show();
                BerpModel.setSelectedCMD("3");
                finish();
                Intent i = new Intent(context, NMesaActivity.class);
                context.startActivity(i);
            }
        }
    }

    private class ProcessoPagamento extends AsyncTask<String, String, String> {

        private Context context;
        private ProgressDialog dialognovo;

        public ProcessoPagamento(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            try {
                dialognovo = new ProgressDialog(context);
                dialognovo.setTitle("Enviando Pagamento");
                dialognovo.setMessage("Aguarde");
                dialognovo.setIndeterminate(true);
                dialognovo.setCancelable(false);
                dialognovo.onSaveInstanceState();
                dialognovo.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            String retorno;
            try {
                retorno = BerpModel.efetuarPagamento(BerpModel.getNumMesa(), BerpModel.getFuncionario());
            } catch (Exception e) {
                e.printStackTrace();
                retorno = e.getMessage();
            }
            return retorno;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            dialognovo.setMessage(values[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            dialognovo.dismiss();

            if (result.equals("Pagamento enviado com sucesso!")) {
                // Getnet pediu pra colocar um comprovante de pagamento
                if (Variaveis.getConfiguracao("COMPROVANTE_NAOFISCAL_POS").getValor().equals("S")) {
                    try {
                        List<Pagamento> pags = BerpModel.listaPagamentosMesa(BerpModel.getNumMesa());
                        imprimeComprovantePagamento(BerpModel.mesaPagaPraImpressao(mesa, pags));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            showCustomDialog(result);
        }
    }

    /*==================== Métodos Auxiliares ====================*/
    private void showDialogCliente() {
        final Dialog dialogcliente = new Dialog(this);
        dialogcliente.setContentView(R.layout.custom_dialog_cliente); // carregando o layout do dialog
        dialogcliente.setTitle("Informar CPF do cliente");
        final Button ok = dialogcliente.findViewById(R.id.btndialogContinuar);
        final EditText edtcpfCliente = dialogcliente.findViewById(R.id.edtcpfcnpjcliente);
        final EditText edtnomeCliente = dialogcliente.findViewById(R.id.edtnomecliente);
        edtcpfCliente.addTextChangedListener(CpfCnpjMaks.insert(edtcpfCliente));

        ok.setOnClickListener(v -> {
            Variaveis.setCpfcliente(edtcpfCliente.getText().toString());
            Variaveis.setNomecliente(edtnomeCliente.getText().toString());
            dialogcliente.dismiss();
            // PROCESSO DE ENVIO DE PAGAMENTO PARA O BANCO
            // ProcessoPagamento processoPagamento = new ProcessoPagamento(getApplicationContext());
            // processoPagamento.execute("", "", "");
        });

        dialogcliente.show();
    }

    private void showCustomDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setPositiveButton("OK", (dialog, id) -> {
            if (message.equals("Pagamento enviado com sucesso!")) {
                finish();
            } else {
                btnFinalizePag.setEnabled(true);
            }
        });
        builder.show();
    }

    public void chamarReimpressao() {
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setTitle("Atenção");
        builder2.setMessage("Deseja imprimir outra via?");
        builder2.setPositiveButton("SIM", (dialogInterface, i) -> {
            funcao = "reimpressao";
            Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse("getnet://pagamento/v1/reprint"));
            startActivityForResult(intent1, REQUEST_CODE);
        });
        builder2.setNegativeButton("NAO", (dialogInterface, i) -> {
            // Nada a fazer
        });
        AlertDialog dialog = builder2.create();
        dialog.show();
    }

    public void chamarReembolso(double valor, String cvNumber) {
        funcao = "estorno";
        Bundle bundle = new Bundle();

        // Converte o valor para inteiro (formatação obrigatória)
        String st = String.format("%.2f", valor);
        st = st.replace(",", "");
        int vlrint = Integer.parseInt(st);

        // DADOS OBRIGATÓRIOS PARA O REEMBOLSO
        bundle.putString("amount", String.format("%012d", vlrint));
        bundle.putString("cvNumber", cvNumber);
        bundle.putString("originTerminal", Variaveis.getNumerologicoPOS());
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("getnet://pagamento/v1/refund"));
        intent.putExtras(bundle);
        startActivityForResult(intent, REQUEST_CODE);
    }

    public void esconderTeclados() {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(edtxtVlrPaga.getWindowToken(), 0);
    }

    public void verificaStatusImpressora(Context context) {
        IPrinterService printerService = PrinterServiceFactory.getPrinterService(context);
        if (printerService != null) {
            printerService.register(context, new PrinterStatusCallback() {
                @Override
                public void onError(Exception e) {
                    Toast.makeText(context, "Serviço em erro!!!", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onConnected() {
                    // Aqui você pode tratar a conexão, se necessário.
                    // Ex.: Toast.makeText(context, "Serviço Conectado!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onDisconnected() {
                    // Aqui você pode tratar a desconexão, se necessário.
                    // Ex.: Toast.makeText(context, "Serviço Desconectado!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(context, "Serviço de impressão não disponível", Toast.LENGTH_LONG).show();
        }
    }



    public void cancelaPagamento() {
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setTitle("Atenção");
        builder2.setMessage("Deseja cancelar o pagamento?");
        builder2.setPositiveButton("SIM", (dialog, id) -> {
            List pags = BerpModel.listaPagamentosMesa(BerpModel.getNumMesa());
            if (pags.size() > 0) {
                for (int j = 0; j < pags.size(); j++) {
                    Pagamento pag = (Pagamento) pags.get(j);
                    // Se for cartão chama a tela de reembolso; se for dinheiro, só exclui da lista
                    if ((pag.getCvNumber() != null) && (!pag.getCvNumber().equals(""))) {
                        chamarReembolso(pag.getValor(), pag.getCvNumber());
                        if (sucesso) {
                            try {
                                mesa.delPagamento(pag);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        try {
                            mesa.delPagamento(pag);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                BerpModel.limpaMesaPagamentos(BerpModel.getNumMesa(), pags);
            }
            PagamentoActivity.this.finish();
        });
        builder2.setNegativeButton("NÃO", (dialog, id) -> {
            // Usuário cancelou a ação
        });

        AlertDialog dialog = builder2.create();
        dialog.show();
    }
}