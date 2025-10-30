package br.com.berpsistemas.BerpPOSMobile;

import static br.com.berpsistemas.BerpPOSMobile.Controller.Proxy.finishOrder;

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
import android.widget.TextView;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import br.com.berpsistemas.BerpPOSMobile.application.MyBerpApplication;
import br.com.berpsistemas.BerpPOSMobile.database.TransactionDatabaseHelperV2;
import br.com.berpsistemas.BerpPOSMobile.model.TransactionModel;
import br.com.berpsistemas.BerpPOSMobile.Printer.IPrinter;
import br.com.berpsistemas.BerpPOSMobile.Printer.IPrinterService;
import br.com.berpsistemas.BerpPOSMobile.Printer.PrinterAlignMode;
import br.com.berpsistemas.BerpPOSMobile.Printer.PrinterCallback;
import br.com.berpsistemas.BerpPOSMobile.Printer.PrinterFactory;
import br.com.berpsistemas.BerpPOSMobile.Printer.PrinterFontFormat;
import br.com.berpsistemas.BerpPOSMobile.Printer.PrinterServiceFactory;
import br.com.berpsistemas.BerpPOSMobile.Printer.PrinterStatusCallback;
import br.com.berpsistemas.BerpPOSMobile.model.BerpModel;
import br.com.berpsistemas.BerpPOSMobile.model.ContaFields;
import br.com.berpsistemas.BerpPOSMobile.Controller.CpfCnpjMaks;
import br.com.berpsistemas.BerpPOSMobile.Controller.Proxy;
import br.com.berpsistemas.BerpPOSMobile.model.PagamentoModel;
import br.com.berpsistemas.BerpPOSMobile.model.PagamentoAdapter;
import br.com.berpsistemas.BerpPOSMobile.model.Variaveis;
import br.com.berpsistemas.BerpPOSMobile.pagamento.IPagamento;
import br.com.berpsistemas.BerpPOSMobile.pagamento.IPagamentoCallback;
import br.com.berpsistemas.BerpPOSMobile.pagamento.IPaymentCallbackHandler;
import br.com.berpsistemas.BerpPOSMobile.pagamento.PagamentoFactory;
import br.com.berpsistemas.BerpPOSMobile.pagamento.PaymentCallbackHandler;
import br.com.berpsistemas.BerpPOSMobile.pagamento.PaymentConfig;
import br.com.berpsistemas.BerpPOSMobile.util.TransactionConverter;

import br.com.berpsistemas.BerpPOSMobile.BuildConfig;
import br.com.berpsistemas.BerpPOSMobile.R;

import com.shashank.sony.fancytoastlib.FancyToast;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class PagamentoActivity extends AppCompatActivity implements View.OnClickListener, PaymentOptionsFragment.PaymentOptionsListener, IPaymentCallbackHandler.PaymentListener  {

    private static final String TAG = "PagamentoActivity";
    private final int REQUEST_CODE = 1001;
    private static final int REQUEST_CODE_CANCEL = 3002;

    private String funcao;
    private Button btnVoltarPag, btnContinuePag, btnFinalizePag;
    private AppCompatEditText edtxtVlrPaga;
    private ArrayList<String> listpagamentos;
    private ArrayAdapter<String> adapter;
    private TextView txtvlrsubtotal, txtfaltapagar, txtLabelAtendimento, txtVlrPago,txtVlrtroco, txtvlrservico, txttotalLabel;
    private Locale myLocale;
    private ContaFields mesa;
    private String exceptMsg;
    private double falta_pagar;
    private double total_venda;
    private double valor;
    private ProgressDialog dialog;
    private boolean sucesso;
    private ArrayList<PagamentoModel> pagamentosList = new ArrayList<>();

    // Integração com Banco de Dados Local
    private TransactionDatabaseHelperV2 dbHelper;
    private String currentLocalTransactionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagamento);

        // Inicializa DB Helper
        dbHelper = MyBerpApplication.getDbHelper();

        // Inicializa as views
        txtvlrsubtotal = findViewById(R.id.subtotal);
        txtfaltapagar = findViewById(R.id.remainingAmount);
        txtVlrPago = findViewById(R.id.pago);
        txtvlrservico = findViewById(R.id.taxes);
        btnVoltarPag = findViewById(R.id.btnCancel);
        btnContinuePag = findViewById(R.id.btnAddPayment);
        btnFinalizePag = findViewById(R.id.btnFinalize);
        edtxtVlrPaga = findViewById(R.id.edtxtVlrPaga);
        txtLabelAtendimento = findViewById(R.id.mesaLabel);
        txttotalLabel = findViewById(R.id.totallabel);
        ListView listPedido = findViewById(R.id.listPedido);

        // Configura os listeners de botões
        btnVoltarPag.setOnClickListener(this);
        btnContinuePag.setOnClickListener(this);
        btnFinalizePag.setOnClickListener(this);
        btnFinalizePag.setEnabled(true);

        listpagamentos = new ArrayList<>();
        myLocale = new Locale("pt", "BR");

        PaymentCallbackHandler.getInstance().setPaymentListener(this);

        List<String> descricoes = listpagamentos;
        List<PagamentoModel> modelos = pagamentosList;

        boolean permiteCancelamento = "S".equals(
                Variaveis.getConfiguracao("MOBILE_PERMITE_CANCELAMENTO","N").getValor()
        );
        boolean permiteReimpressao = "S".equals(
                Variaveis.getConfiguracao("MOBILE_PERMITE_REIMPRESSAO","N").getValor()
        );

        adapter = new PagamentoAdapter(
                this,
                descricoes,
                modelos,
                (position, pagamento) -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Confirmar cancelamento")
                            .setMessage("Deseja realmente cancelar este pagamento?")
                            .setPositiveButton("Sim", (dialog, which) ->
                                    cancelarPagamentoIndividual(position, pagamento)
                            )
                            .setNegativeButton("Não", null)
                            .show();
                },
                (position, pagamento) -> reimprimirComprovante(pagamento),
                permiteCancelamento,
                permiteReimpressao
        );

        listPedido.setAdapter(adapter);

        // Configura os eventos do EditText
        edtxtVlrPaga.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                edtxtVlrPaga.post(() -> edtxtVlrPaga.selectAll());
            }
        });

        edtxtVlrPaga.setOnClickListener(v -> edtxtVlrPaga.selectAll());

        edtxtVlrPaga.setFilters(new InputFilter[]{
                (source, start, end, dest, dstart, dend) -> {
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
        });

        TextWatcher moneyWatcher = new TextWatcher() {
            private String current = "";
            private boolean isUpdating = false;
            private final double MAX_VALUE = 10000.00;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) {
                    return;
                }
                current="";

                // Remove todos os caracteres não numéricos (CORRIGIDO)
                String cleanString = s.toString().replaceAll("[^\\d]", "");

                if (cleanString.isEmpty()) {
                    cleanString = "0";
                }

                long valueInCents;
                try {
                    valueInCents = Long.parseLong(cleanString);
                } catch (NumberFormatException e) {
                    valueInCents = 0;
                }

                if (valueInCents > (MAX_VALUE * 100)) {
                    valueInCents = (long)(MAX_VALUE * 100);
                }

                double value = valueInCents / 100.0;

                NumberFormat formatter = NumberFormat.getCurrencyInstance(myLocale);
                formatter.setMinimumFractionDigits(2);
                formatter.setMaximumFractionDigits(2);
                String formatted = formatter.format(value);

                if (!formatted.equals(current)) {
                    isUpdating = true;
                    current = formatted;
                    edtxtVlrPaga.setText(formatted);
                    edtxtVlrPaga.setSelection(formatted.length());
                    isUpdating = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        if (edtxtVlrPaga.getTag() instanceof TextWatcher) {
            edtxtVlrPaga.removeTextChangedListener((TextWatcher) edtxtVlrPaga.getTag());
        }

        edtxtVlrPaga.addTextChangedListener(moneyWatcher);
        edtxtVlrPaga.setTag(moneyWatcher);

        ConnServer cs = new ConnServer(this);
        cs.execute("", "", "");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try {
            Log.i("onNewIntent", intent.getDataString());
            if (intent.getDataString() != null) {
                FancyToast.makeText(this, intent.getDataString(), FancyToast.LENGTH_LONG,FancyToast.INFO,true).show();
                Log.i("DeeplinkPay Berp", intent.getDataString());
            }
        } catch (Exception e) {
            FancyToast.makeText(this, e.getMessage(), FancyToast.LENGTH_LONG,FancyToast.ERROR,true).show();
            Log.e("Deeplink berp error", e.getMessage());
        }
    }

    @Override
    public void onClick(View view) {
        AlertDialog.Builder builder;
        if (view == btnVoltarPag) {
            finish();
        } else if (view == btnContinuePag) {
            btnContinuePag.setEnabled(false);
            valor = 0.0;
            falta_pagar = 0.0;

            esconderTeclados();

            if (edtxtVlrPaga.getText() != null && !edtxtVlrPaga.getText().toString().trim().isEmpty()) {
                try {
                    String rawText = edtxtVlrPaga.getText().toString();
                    String s = rawText.replaceAll("[R$\\s.]", "")
                            .replace(",", ".");

                    Log.d(TAG, "Texto original: '" + rawText + "', processado: '" + s + "'");

                    valor = Double.parseDouble(s);
                } catch (NumberFormatException e) {
                    valor = 0.0;
                    Log.e(TAG, "Erro ao converter valor: " + e.getMessage());
                }
            }

            if (valor > 0) {
                if (txtfaltapagar.getText() != null && !txtfaltapagar.getText().toString().trim().isEmpty()) {
                    try {
                        String pag = txtfaltapagar.getText().toString().replace("Falta :", "").replace("R$", "").replace(",", ".").trim();
                        String rawText_pagar = pag;
                        String spag = rawText_pagar.replaceAll("[R$\\s.]", "").replace(",", ".");

                        falta_pagar = Double.parseDouble(spag);
                        falta_pagar = falta_pagar / 100;
                    } catch (NumberFormatException e) {
                        falta_pagar = 0.0;
                        Log.e(TAG, "Erro ao converter valor a pagar: " + e.getMessage());
                    }
                }

                if (listpagamentos.size() == 9 && falta_pagar > valor) {
                    builder = new AlertDialog.Builder(this);
                    builder.setTitle("Atenção");
                    builder.setMessage("O número máximo de pagamentos é " +
                            Variaveis.getConfiguracao("MAX_PAGAMENTOS_POS","9").getValor() +
                            ". O valor do último pagamento deve ser igual.");
                    builder.setPositiveButton("OK", (dialog, id) -> edtxtVlrPaga.setText(
                            NumberFormat.getCurrencyInstance(myLocale).format(falta_pagar)));
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    String paymentId = BerpModel.getId();

                    if (valor > falta_pagar) {
                        builder = new AlertDialog.Builder(this);
                        builder.setTitle("Atenção");
                        builder.setMessage("Não é permitido pagar um valor maior que o saldo restante!");
                        builder.setPositiveButton("OK", (dialog, id) -> edtxtVlrPaga.setText(
                                NumberFormat.getCurrencyInstance(myLocale).format(falta_pagar)));
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        btnContinuePag.setEnabled(true);
                        return;
                    }

                    PaymentOptionsFragment fragment = PaymentOptionsFragment.newInstance(paymentId, valor);
                    FrameLayout container = findViewById(R.id.fragment_container);
                    container.setVisibility(View.VISIBLE);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit();
                }
            } else {
                btnContinuePag.setEnabled(true);
                builder = new AlertDialog.Builder(this);
                builder.setTitle("Atenção");
                builder.setMessage("O valor deve ser maior que 0,00!");
                builder.setPositiveButton("OK", (dialog, id) -> edtxtVlrPaga.setText(
                        NumberFormat.getCurrencyInstance(myLocale).format(falta_pagar)));
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        } else if (view == btnFinalizePag) {
            finalizarPagamento();
        }
    }

    private void finalizarPagamento() {
        btnFinalizePag.setEnabled(false);

        new AsyncTask<Void, Void, Boolean>() {
            private Exception error;

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    // Se finishOrder retorna CompletableFuture<Boolean>,
                    // usamos get com timeout (compatível com API 22).
                    return finishOrder(Integer.parseInt(BerpModel.getId()))
                            .get(12, java.util.concurrent.TimeUnit.SECONDS);
                } catch (java.util.concurrent.TimeoutException te) {
                    error = new Exception("Timeout ao finalizar a conta");
                    return false;
                } catch (Exception e) {
                    error = e;
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PagamentoActivity.this)
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialog, which) -> {
                            dialog.dismiss();
                            if (success) {
                                // No sucesso, mantém o comportamento original: fecha a tela
                                finish();
                            } else {
                                // Em falha/erro, reabilita o botão para o operador tentar de novo
                                btnFinalizePag.setEnabled(true);
                            }
                        });

                if (error != null) {
                    builder.setTitle("Erro")
                            .setMessage("Ocorreu um erro ao finalizar a conta:\n" + error.getMessage())
                            .show();
                } else if (success) {
                    builder.setTitle("Sucesso")
                            .setMessage("Conta finalizada com sucesso!")
                            .show();
                } else {
                    builder.setTitle("Falha")
                            .setMessage("Não foi possível finalizar a conta. Tente novamente mais tarde.")
                            .show();
                }
            }
        }.execute();
    }


    @Override
    public void onPaymentMethodSelected(String method) {
        getSupportFragmentManager().popBackStack();
        findViewById(R.id.fragment_container).setVisibility(View.GONE);

        // PASSO 1: Salvar transação PENDING no banco local
        currentLocalTransactionId = UUID.randomUUID().toString();
        TransactionModel newTransaction = new TransactionModel(
                currentLocalTransactionId,
                "", // transactionId - será preenchido no sucesso
                "", // nsu - será preenchido no sucesso
                BerpModel.getId(),
                "", // platformId - será preenchido no sucesso
                valor,
                (long) (valor * 100),
                1, // installments
                method.toUpperCase(),
                "", // cardBrand - será preenchido no sucesso
                "", // maskedPan - será preenchido no sucesso
                "", // cardBin - será preenchido no sucesso
                "", // cardholderName - será preenchido no sucesso
                BuildConfig.FLAVOR.toUpperCase(),
                Variaveis.getNumTerminal(),
                "", // merchantId
                "PENDING",
                "",
                "",
                false,
                "",
                null,
                new Date(),
                new Date(),
                new Date(),
                "",
                false,
                0,
                null,
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                2
        );
        dbHelper.saveTransaction(newTransaction);
        Log.d(TAG, "Transação PENDING salva no DB local com ID: " + currentLocalTransactionId);

        IPagamento pagamento = PagamentoFactory.criarPagamento(this);

        pagamento.setCallback(new IPagamentoCallback() {
            @Override
            public void onPagamentoSucesso(PagamentoModel pag) {
                // A lógica de sucesso agora é tratada pelo listener global onPaymentSuccess
                // para evitar duplicidade.
                // O corpo deste método foi intencionalmente deixado em branco.
            }

            @Override
            public void onPagamentoFalha(String motivo) {
                runOnUiThread(() -> {
                    FancyToast.makeText(PagamentoActivity.this, "Falha no pagamento: " + motivo, FancyToast.LENGTH_LONG,FancyToast.ERROR,true).show();
                    btnContinuePag.setEnabled(true);
                });
            }

            @Override
            public void onPagamentoCancelado() {
                runOnUiThread(() -> {
                    FancyToast.makeText(PagamentoActivity.this, "Pagamento cancelado pelo usuário.",FancyToast.LENGTH_LONG,FancyToast.WARNING,true).show();
                    btnContinuePag.setEnabled(true);
                });
            }

            @Override
            public void onRefundSuccess(PagamentoModel pagamentoCancelado) {}
        });

        int valorEmCentavos = (int) (valor * 100);

        String tipo = "credit";
        if (method.equals("debito")) {
            tipo = "debit";
        } else if (method.equals("pix")) {
            tipo = "pix";
        }

        PaymentConfig config = new PaymentConfig(valorEmCentavos, tipo, BerpModel.getId());

        if (BuildConfig.POS_MODEL.equals("pagseguro")) {
            pagamento.iniciarPagamentoProvider(this, config);
        } else {
            pagamento.iniciarPagamentoDeeplink(this, config);
        }

        btnContinuePag.setEnabled(true);
    }

    private void atualizarTelaPagamento(PagamentoModel pag) {
        double novoValorFaltante = falta_pagar - pag.getPgpVlrpag();
        String vlrpagar = NumberFormat.getCurrencyInstance(myLocale).format(novoValorFaltante);
        txtfaltapagar.setText("Falta :" + vlrpagar);
        edtxtVlrPaga.setText(vlrpagar);

        String metodoPagamento;

        if (Objects.equals(pag.getTipoCartaoDebCre(), "DEB")) {
            metodoPagamento = "Débito";
        } else if (Objects.equals(pag.getTipoCartaoDebCre(), "CRE")) {
            metodoPagamento = "Crédito";
        } else if (Objects.equals(pag.getTipoCartaoDebCre(), "PIX")) {
            metodoPagamento = "PIX";
        } else {
            metodoPagamento = "Pagamento";
        }

        pagamentosList.add(pag);
        listpagamentos.add(NumberFormat.getCurrencyInstance(myLocale).format(pag.getPgpVlrpag()) + " " + metodoPagamento);
        adapter.notifyDataSetChanged();

        txtVlrPago.setText("Pagou: "+NumberFormat.getCurrencyInstance(myLocale).format(total_venda-novoValorFaltante));

        if (Math.abs(novoValorFaltante) < 0.01) {
            btnContinuePag.setEnabled(false);
            btnFinalizePag.setEnabled(true);
        } else {
            btnContinuePag.setEnabled(true);
        }
    }

    @Override
    public void onPaymentOptionsCancelled() {
        getSupportFragmentManager().popBackStack();
        findViewById(R.id.fragment_container).setVisibility(View.GONE);
        btnContinuePag.setEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE || requestCode == REQUEST_CODE_CANCEL) {
            IPagamento pagamento = PagamentoFactory.criarPagamento(this);
            pagamento.processarResultado(requestCode, resultCode, data);
        }
    }

    public void imprimeComprovantePagamento(String comp) {
        IPrinter printer = PrinterFactory.getPrinterInstance(getApplicationContext());
        if (printer == null) {
            FancyToast.makeText(getApplicationContext(), "Serviço de impressão não habilitado", FancyToast.LENGTH_LONG,FancyToast.INFO,true).show();
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
                    Log.d(TAG, "Impressão realizada com sucesso");
                }

                @Override
                public void onError(int errorCode) {
                    String errorMsg = getErrorMessage(errorCode);
                    FancyToast.makeText(getApplicationContext(), errorMsg, FancyToast.LENGTH_LONG,FancyToast.INFO,true).show();
                }
            });
        } catch (Exception e) {
            FancyToast.makeText(getApplicationContext(), e.getMessage(), FancyToast.LENGTH_LONG,FancyToast.INFO,true).show();
            Log.e(TAG, "Erro ao imprimir: " + e.getMessage());
        }
    }

    private String getErrorMessage(int errorCode) {
        switch (errorCode) {
            case 1:
                return "Impressora não iniciada";
            case 2:
                return "Impressora superaquecida";
            case 3:
                return "Sem papel na impressora";
            case 4:
                return "Erro ao abrir porta da impressora";
            default:
                return "Erro desconhecido (" + errorCode + ")";
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
                Log.e(TAG, "Erro ao mostrar diálogo: " + e.getMessage());
            }
        }

        private boolean isNullOrBlank(String str) {
            return str == null || str.trim().isEmpty();
        }

        @Override
        protected Integer doInBackground(String... values) {
            try {
                if (BerpModel.verificaPagamentos(BerpModel.getNumMesa())) {
                    try {
                        // Timeout compatível com API 22
                        mesa = Proxy.visualizaConta(BerpModel.getId(), 0)
                                .get(12, java.util.concurrent.TimeUnit.SECONDS);
                    } catch (java.util.concurrent.TimeoutException te) {
                        exceptMsg = "Timeout ao consultar a conta";
                        return 98; // erro transitório
                    }
                    return 0; // OK
                } else {
                    return 1; // sem pagamentos
                }
            } catch (Exception e) {
                e.printStackTrace();
                exceptMsg = e.getMessage();
                String msg = (exceptMsg == null ? "" : exceptMsg).toLowerCase();
                if (msg.contains("internal server error") || msg.contains("500")
                        || msg.contains("timeout") || msg.contains("failed to connect")
                        || msg.contains("unable to resolve host")
                        || msg.contains("transa")) { // “transação manual/distribuída”
                    return 98; // transitório
                }
                return 99; // fatal (não mapeado)
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (dialog != null && dialog.isShowing()) {
                dialog.setMessage(values[0]);
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (dialog != null && dialog.isShowing()) {
                try { dialog.dismiss(); } catch (Exception e) {
                    Log.e(TAG, "Erro ao fechar diálogo: " + e.getMessage());
                }
            }

            if (result == 1) {
                // Fluxo original quando não há pagamentos
                BerpModel.setSelectedCMD("3");
                finish();
                Intent i = new Intent(context, NMesaActivity.class);
                context.startActivity(i);
                return;
            }

            if (result == 0) {
                // SUCESSO: popular a UI com os valores da mesa
                String vlrLiquido = mesa.getVlrLiquido();
                String vlrServico = mesa.getVlrServico();
                String vlrBruto   = mesa.getVlrBruto();

                txtvlrsubtotal.setText("Liquido :" + (isNullOrBlank(vlrLiquido) ? "R$0,00" : vlrLiquido));
                txtvlrservico.setText("Servico :" + (isNullOrBlank(vlrServico) ? "R$0,00" : vlrServico));
                txtfaltapagar.setText("Falta :" + (isNullOrBlank(vlrBruto) ? "R$0,00" : vlrBruto));
                txtLabelAtendimento.setText(mesa.getVen_nmtpvend() + " " + mesa.getCdMesa());
                txttotalLabel.setText("Total: " + mesa.getVlrBruto());
                txtVlrPago.setText("Pago :"+ mesa.getTotalPagoStr());

                if (!isNullOrBlank(vlrBruto)) {
                    try {
                        Log.d(TAG, "Valor bruto original: '" + vlrBruto + "'");
                        String vlrPagam = vlrBruto.replaceAll("[^0-9,.]+", "").replace(",", ".");
                        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+(\\.\\d+)?)");
                        java.util.regex.Matcher matcher = pattern.matcher(vlrPagam);

                        double vvlrpagar = 0.0;
                        if (matcher.find()) {
                            String match = matcher.group(1);
                            Log.d(TAG, "Valor extraído após processamento: '" + match + "'");
                            vvlrpagar = Double.parseDouble(match);
                        }
                        String valorApresentar = NumberFormat.getCurrencyInstance(myLocale).format(vvlrpagar);
                        edtxtVlrPaga.setText(valorApresentar);
                        edtxtVlrPaga.setSelection(0, Objects.requireNonNull(edtxtVlrPaga.getText()).length());

                        total_venda = vvlrpagar;
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao processar valor: " + e.getMessage() + " para entrada: '" + vlrBruto + "'");
                        FancyToast.makeText(context, "Erro ao processar o valor de pagamento.", FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
                        edtxtVlrPaga.setText(NumberFormat.getCurrencyInstance(myLocale).format(0));
                    }
                } else {
                    edtxtVlrPaga.setText(NumberFormat.getCurrencyInstance(myLocale).format(0));
                }
                edtxtVlrPaga.requestFocus();
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

            } else if (result == 98) {
                // ERRO TRANSITÓRIO: não fecha a Activity; mostra retry
                new AlertDialog.Builder(context)
                        .setTitle("Servidor ocupado")
                        .setMessage((exceptMsg == null || exceptMsg.trim().isEmpty())
                                ? "Não foi possível consultar a conta agora."
                                : exceptMsg)
                        .setCancelable(false)
                        .setPositiveButton("Tentar novamente", (d, which) ->
                                // pequeno backoff antes de re-tentar
                                edtxtVlrPaga.postDelayed(() -> new ConnServer(context).execute("", "", ""), 1200)
                        )
                        .setNegativeButton("Voltar", null)
                        .show();

            } else {
                // 99 e outros: mantém comportamento original (volta para tela anterior)
                String errorMsg = exceptMsg == null ? "Erro desconhecido." : exceptMsg;
                FancyToast.makeText(context, errorMsg, FancyToast.LENGTH_LONG, FancyToast.CONFUSING, true).show();
                Log.e(TAG, "Erro na conexão: " + errorMsg);
                BerpModel.setSelectedCMD("3");
                finish();
                Intent i = new Intent(context, NMesaActivity.class);
                context.startActivity(i);
                return;
            }

            // ===== Carregar lista de pagamentos (compatível com API 22) =====
            new AsyncTask<Void, Void, List<PagamentoModel>>() {
                private Exception error;

                @Override
                protected List<PagamentoModel> doInBackground(Void... voids) {
                    try {
                        // Se listarPagamentos retorna CompletableFuture<List<PagamentoModel>>,
                        // usamos get com timeout (compatível):
                        return Proxy.listarPagamentos(BerpModel.getId())
                                .get(10, java.util.concurrent.TimeUnit.SECONDS);
                    } catch (java.util.concurrent.TimeoutException te) {
                        error = new Exception("Timeout ao consultar a lista de pagamentos");
                        return null;
                    } catch (Exception e) {
                        error = e;
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(List<PagamentoModel> pags) {
                    if (error != null || pags == null) {
                        Log.w(TAG, "listarPagamentos falhou: " + (error == null ? "null" : error.getMessage()));
                        FancyToast.makeText(
                                PagamentoActivity.this,
                                "Não foi possível atualizar a lista de pagamentos agora.",
                                FancyToast.LENGTH_SHORT, FancyToast.INFO, true
                        ).show();
                        return;
                    }

                    try {
                        pagamentosList.clear();
                        listpagamentos.clear();
                        mesa.clearPagamentos();

                        for (PagamentoModel pag : pags) {
                            pagamentosList.add(pag);
                            mesa.addPagamento(pag);

                            final String metodo =
                                    "DEB".equals(pag.getTipoCartaoDebCre()) ? "Débito" :
                                            "CRE".equals(pag.getTipoCartaoDebCre()) ? "Crédito" :
                                                    "PIX".equals(pag.getTipoCartaoDebCre()) ? "PIX" : "Pagamento";

                            listpagamentos.add(
                                    NumberFormat.getCurrencyInstance(myLocale).format(pag.getPgpVlrpag())
                                            + " " + metodo
                            );
                        }
                        adapter.notifyDataSetChanged();
                        atualizarValorFaltante();
                    } catch (Exception e) {
                        Log.e(TAG, "Falha ao atualizar lista de pagamentos: " + e.getMessage());
                    }
                }
            }.execute();
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
                Log.e(TAG, "Erro ao mostrar diálogo de pagamento: " + e.getMessage());
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
                Log.e(TAG, "Erro ao efetuar pagamento: " + e.getMessage());
            }
            return retorno;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (dialognovo != null && dialognovo.isShowing()) {
                dialognovo.setMessage(values[0]);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (dialognovo != null && dialognovo.isShowing()) {
                try {
                    dialognovo.dismiss();
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao fechar diálogo: " + e.getMessage());
                }
            }

            if ("Pagamento enviado com sucesso!".equals(result)) {
                if ("S".equals(Variaveis.getConfiguracao("COMPROVANTE_NAOFISCAL_POS","S").getValor())) {
                    try {
                        List<PagamentoModel> pags = BerpModel.listaPagamentosMesa(BerpModel.getNumMesa());
                        imprimeComprovantePagamento(BerpModel.mesaPagaPraImpressao(mesa, pags));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "Erro ao imprimir comprovante: " + e.getMessage());
                    }
                }
            }

            showCustomDialog(result);
        }
    }

    private void showDialogCliente() {
        final Dialog dialogcliente = new Dialog(this);
        dialogcliente.setContentView(R.layout.custom_dialog_cliente);
        dialogcliente.setTitle("Informar CPF do cliente");
        final Button ok = dialogcliente.findViewById(R.id.btndialogContinuar);
        final EditText edtcpfCliente = dialogcliente.findViewById(R.id.edtcpfcnpjcliente);
        final EditText edtnomeCliente = dialogcliente.findViewById(R.id.edtnomecliente);
        edtcpfCliente.addTextChangedListener(CpfCnpjMaks.insert(edtcpfCliente));

        ok.setOnClickListener(v -> {
            Variaveis.setCpfcliente(edtcpfCliente.getText().toString());
            Variaveis.setNomecliente(edtnomeCliente.getText().toString());
            dialogcliente.dismiss();

            ProcessoPagamento processoPagamento = new ProcessoPagamento(this);
            processoPagamento.execute();
        });

        dialogcliente.show();
    }

    private void showCustomDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setPositiveButton("OK", (dialog, id) -> {
            if ("Pagamento enviado com sucesso!".equals(message)) {
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

    public void esconderTeclados() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(edtxtVlrPaga.getWindowToken(), 0);
        }
    }

    public void verificaStatusImpressora(Context context) {
        IPrinterService printerService = PrinterServiceFactory.getPrinterService(context);
        if (printerService != null) {
            printerService.register(context, new PrinterStatusCallback() {
                @Override
                public void onError(Exception e) {
                    FancyToast.makeText(context, "Serviço de impressão em erro: " + e.getMessage(), FancyToast.LENGTH_LONG,FancyToast.INFO,true).show();
                    Log.e(TAG, "Erro no serviço de impressão: " + e.getMessage());
                }

                @Override
                public void onConnected() {
                    Log.d(TAG, "Serviço de impressão conectado");
                }

                @Override
                public void onSucess(String msg) {

                }

                @Override
                public void onDisconnected() {
                    Log.d(TAG, "Serviço de impressão desconectado");
                }
            });
        } else {
            FancyToast.makeText(context, "Serviço de impressão não disponível", FancyToast.LENGTH_LONG,FancyToast.INFO,true).show();
            Log.e(TAG, "Serviço de impressão não disponível");
        }
    }

    @Override
    public void onPaymentSuccess(String brand, String authCode, String mask, String Doc, String Terminal, String Adquirente, String Id_plataforma, String IdPlataformaResumido, String CodpagMaq, String Rede, String TransactionId, String idPgamento, String binCartao, String debcre, double amount) {
        Log.d(TAG, "onPaymentSuccess - ID Local: " + currentLocalTransactionId);

        TransactionModel transaction = dbHelper.getTransactionByAnyId(currentLocalTransactionId);
        if (transaction == null) {
            Log.e(TAG, "CRÍTICO: Transação não encontrada!");
            return;
        }

        // Atualiza a transação PENDING com os dados do pagamento bem-sucedido
        TransactionModel updatedTransaction = transaction.updateFromPaymentSuccess(
                TransactionId,
                Doc,
                Id_plataforma,
                brand,
                mask,
                binCartao
        );

        dbHelper.saveTransaction(updatedTransaction);
        Log.d(TAG, "Transação atualizada para APPROVED no DB.");

        // Converte a transação local (agora APPROVED) para um PagamentoModel.
        // Isso garante que o ID local e todos os outros dados sejam consistentes.
        PagamentoModel novoPagamento = TransactionConverter.INSTANCE.toPaymentModel(updatedTransaction);

        // Adiciona o pagamento ao modelo de dados em memória (BerpModel)
        BerpModel.addPag(
                BerpModel.getNumMesa(),
                novoPagamento.getPgpCdfpag(),
                novoPagamento.getPgpVlrpag(),
                2, // pgpEvtipo
                novoPagamento.getNsu(),
                novoPagamento.getAutorizacao(),
                novoPagamento.getBandeira(),
                novoPagamento.getCvNumber()
        );

        runOnUiThread(() -> {
            atualizarTelaPagamento(novoPagamento);
            atualizarDadosDoServidor(novoPagamento);
            ConnServer cs = new ConnServer(this);
            cs.execute("", "", "");
        });
    }

    @Override
    public void onPaymentError(String reason) {
        Log.d(TAG, "onPaymentError - ID Local: " + currentLocalTransactionId);

        TransactionModel transaction = dbHelper.getTransactionByAnyId(currentLocalTransactionId);
        if (transaction != null) {
            TransactionModel updatedTransaction = transaction.updateFromPaymentError(reason);
            dbHelper.saveTransaction(updatedTransaction);
            Log.d(TAG, "Transação atualizada para DENIED no DB.");
        }

        runOnUiThread(() -> {
            FancyToast.makeText(this, "Falha no pagamento: " + reason, FancyToast.LENGTH_LONG, FancyToast.WARNING, true).show();
            btnContinuePag.setEnabled(true);
        });
    }



    @Override
    public void onPaymentCancelled() {
        Log.d(TAG, "onPaymentCancelled - ID Local: " + currentLocalTransactionId);

        TransactionModel transaction = dbHelper.getTransactionByAnyId(currentLocalTransactionId);
        if (transaction != null) {
            TransactionModel updatedTransaction = transaction.updateFromPaymentCancelled();
            dbHelper.saveTransaction(updatedTransaction);
            Log.d(TAG, "Transação atualizada para CANCELLED no DB.");
        }

        runOnUiThread(() -> {
            FancyToast.makeText(this, "Pagamento cancelado pelo usuário.", FancyToast.LENGTH_LONG, FancyToast.WARNING, true).show();
            btnContinuePag.setEnabled(true);
        });
    }


    @Override
    public void onRefundSuccess(PagamentoModel pagCancelado) {
        // NÃO gravar no banco aqui - já foi feito no PaymentCallbackHandler!

        runOnUiThread(() -> {
            // Apenas atualizar a UI
            for (int i = 0; i < pagamentosList.size(); i++) {
                PagamentoModel p = pagamentosList.get(i);

                // Buscar por qualquer identificador disponível
                boolean isMatch = false;
                if (pagCancelado.getCvNumber() != null &&
                        p.getCvNumber() != null &&
                        p.getCvNumber().equals(pagCancelado.getCvNumber())) {
                    isMatch = true;
                } else if (pagCancelado.getNsu() != null &&
                        p.getNsu() != null &&
                        p.getNsu().equals(pagCancelado.getNsu())) {
                    isMatch = true;
                } else if (pagCancelado.getTransactionId() != null &&
                        p.getTransactionId() != null &&
                        p.getTransactionId().equals(pagCancelado.getTransactionId())) {
                    isMatch = true;
                }

                if (isMatch) {
                    // Remover da lista de memória (BerpModel)


                    FancyToast.makeText(this,
                            "Reembolso concluído com sucesso",
                            FancyToast.LENGTH_LONG,
                            FancyToast.INFO,
                            true).show();
                    break;
                }
            }
        });
    }

    private void atualizarDadosDoServidor(PagamentoModel pag) {
        new AsyncTask<Void, Void, String>() {
            private ProgressDialog dialog;

            @Override
            protected void onPreExecute() {
                dialog = new ProgressDialog(PagamentoActivity.this);
                dialog.setMessage("Atualizando dados...");
                dialog.setCancelable(false);
                dialog.show();
            }

            @Override
            protected String doInBackground(Void... voids) {
                try {
                    return BerpModel.sincronizarPagamentos(pag);
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao atualizar dados: " + e.getMessage());
                    return "Erro: " + e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }

                if (result.startsWith("Erro")) {
                    FancyToast.makeText(PagamentoActivity.this, result, FancyToast.LENGTH_LONG,FancyToast.ERROR,true).show();
                } else {
                    Log.d(TAG, "Dados atualizados com sucesso: " + result);
                }
            }
        }.execute();
    }

    private void cancelarPagamentoIndividual(int position, PagamentoModel pagamento) {
        if (pagamento.getCvNumber() != null && !pagamento.getCvNumber().isEmpty()) {
            IPagamento pagamentoHandler = PagamentoFactory.criarPagamento(this);
            pagamentoHandler.realizarReembolso(this, pagamento);
        } else {
            try {
                TransactionModel transacaoLocal = dbHelper.getTransactionByAnyId(pagamento.getIdOrder());
                if(transacaoLocal != null) {
                    dbHelper.cancelTransaction(transacaoLocal.getId(), "", "Cancelado antes de enviar");
                }
                mesa.delPagamento(pagamento);
                pagamentosList.remove(position);
                listpagamentos.remove(position);
                adapter.notifyDataSetChanged();
                atualizarValorFaltante();
                FancyToast.makeText(this, "Pagamento cancelado com sucesso", FancyToast.LENGTH_LONG,FancyToast.INFO,true).show();
            } catch (Exception e) {
                Log.e(TAG, "Erro ao cancelar pagamento: " + e.getMessage());
                FancyToast.makeText(this, "Erro ao cancelar pagamento: " + e.getMessage(), FancyToast.LENGTH_LONG,FancyToast.INFO,true).show();
            }
        }
    }

    private void atualizarValorFaltante() {
        try {
            double valorTotal = Double.parseDouble(mesa.getVlrBruto()
                    .replace("R$", "").replace(".", "").replace(",", ".").trim());
            double valorPago = 0;

            for (PagamentoModel pag : pagamentosList) {
                valorPago += pag.getPgpVlrpag();
            }

            double valorFaltante = valorTotal - valorPago;
            String vlrpagar = NumberFormat.getCurrencyInstance(myLocale).format(valorFaltante);
            txtfaltapagar.setText("Falta :" + vlrpagar);
            txtVlrPago.setText("Pago: R$ "+mesa.getTotalPagoStr());
            edtxtVlrPaga.setText(vlrpagar);

            btnContinuePag.setEnabled(true);
            btnFinalizePag.setEnabled(Math.abs(valorFaltante) < 0.01);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar valor faltante: " + e.getMessage());
        }
    }

    private void reimprimirComprovante(PagamentoModel pagamento) {
        TransactionModel transacaoLocal = dbHelper.getTransactionByAnyId(pagamento.getNsu());
        if (transacaoLocal != null) {
            dbHelper.markReceiptPrinted(transacaoLocal.getId());
        }

        try {
            if (pagamento.getPgpCdfpag() == 86 || pagamento.getPgpCdfpag() == 87) {
                funcao = "reimpressao";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("getnet://pagamento/v1/reprint"));
                startActivityForResult(intent, REQUEST_CODE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao reimprimir comprovante: " + e.getMessage());
            FancyToast.makeText(this, "Erro ao reimprimir: " + e.getMessage(), FancyToast.LENGTH_LONG,FancyToast.INFO,true).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PaymentCallbackHandler.getInstance().setPaymentListener(this);
    }
}