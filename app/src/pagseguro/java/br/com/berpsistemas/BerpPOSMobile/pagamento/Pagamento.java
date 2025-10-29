package br.com.berpsistemas.BerpPOSMobile.pagamento;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import br.com.berpsistemas.BerpPOSMobile.PlugPagProvider;
import br.com.berpsistemas.BerpPOSMobile.model.BerpModel;
import br.com.berpsistemas.BerpPOSMobile.model.PagamentoModel;
import br.com.berpsistemas.BerpPOSMobile.ui.PaymentUIManager;
import com.shashank.sony.fancytoastlib.FancyToast;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagCustomPrinterLayout;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventListener;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPaymentData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagVoidData;
import br.com.uol.pagseguro.plugpagservice.wrapper.exception.PlugPagException;

import com.mobile.berp.BerpPOSMobile.database.EnhancedTransactionManagerV2;
import com.mobile.berp.BerpPOSMobile.model.TransactionModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Date;

public class Pagamento implements IPagamento {
    private static final String TAG = "PagamentoSDK";

    // Constantes para tipos de pagamento
    public static final String DEBIT = "DEBIT";
    public static final String CREDIT = "CREDIT";
    public static final String PIX = "PIX";
    public static final String VOUCHER = "VOUCHER";

    private Activity context;
    private PlugPag plugPag;
    private PaymentCallbackHandler callbackHandler;
    private IPagamentoCallback pagamentoCallback;
    private ExecutorService executor;
    private Handler mainHandler;
    private PaymentUIManager uiManager;
    private EnhancedTransactionManagerV2 transactionManager;

    // Estado da transação atual
    private PaymentConfig currentConfig;
    private boolean isProcessing = false;
    private String pendingTransactionId;

    public Pagamento(Activity context) {
        this.context = context;
        this.plugPag = PlugPagProvider.getInstance(context);
        this.callbackHandler = PaymentCallbackHandler.getInstance();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.uiManager = new PaymentUIManager(context);
        this.transactionManager = new EnhancedTransactionManagerV2(context);

        // Configurar o listener de pagamento
        setupPaymentListener();
        setupUIManagerCallback();
    }

    private void setupPaymentListener() {
        callbackHandler.setPaymentListener(new IPaymentCallbackHandler.PaymentListener() {
            @Override
            public void onPaymentSuccess(String brand, String authCode, String mask, String doc,
                                         String terminal, String adquirente, String idPlataforma,
                                         String idPlataformaResumido, String codpagMaq, String rede,
                                         String transactionId, String idPagamento, String binCartao,
                                         String debcre, double amount) {

                Log.d(TAG, "Pagamento SDK bem-sucedido: " + transactionId);
                if (pagamentoCallback != null) {
                    // Ajustar para a assinatura correta do IPagamentoCallback
                    PagamentoModel pagamento = new PagamentoModel();
                    pagamento.setBandeira(brand);
                    pagamento.setAutorizacao(authCode);
                    pagamento.setCartao(mask);
                    pagamento.setNsu(doc);
                    pagamento.setTerminal(terminal);
                    pagamento.setAdquirente(adquirente);
                    pagamento.setIdPlataforma(idPlataforma);
                    pagamento.setIdPlataformaReduzido(idPlataformaResumido);
                    pagamento.setTransactionId(transactionId);
                    pagamento.setIdPagamentoMovpagpa(idPagamento);
                    pagamento.setBinCartao(binCartao);
                    pagamento.setTipoCartaoDebCre(debcre);
                    pagamento.setPgpVlrpag(amount);

                    pagamentoCallback.onPagamentoSucesso(pagamento);
                }
                isProcessing = false;
            }

            @Override
            public void onPaymentError(String reason) {
                Log.e(TAG, "Erro no pagamento SDK: " + reason);
                if (pagamentoCallback != null) {
                    pagamentoCallback.onPagamentoFalha(reason);
                }
                isProcessing = false;
            }

            @Override
            public void onPaymentCancelled() {
                Log.d(TAG, "Pagamento SDK cancelado");
                if (pagamentoCallback != null) {
                    pagamentoCallback.onPagamentoCancelado();
                }
                isProcessing = false;
            }

            @Override
            public void onRefundSuccess(PagamentoModel pagamentoCancelado) {
                Log.d(TAG, "Estorno SDK bem-sucedido");
                if (pagamentoCallback != null) {
                    pagamentoCallback.onRefundSuccess(pagamentoCancelado);
                }
                isProcessing = false;
            }
        });
    }

    private void setupUIManagerCallback() {
        uiManager.setCallback(new PaymentUIManager.PaymentUICallback() {
            @Override
            public void onOptionSelected(int optionIndex, String optionValue) {
                Log.d(TAG, "Opção selecionada: " + optionIndex + " - " + optionValue);
                // Processar seleção de opções (ex: parcelas)
                if (currentConfig != null && optionValue.contains("x")) {
                    try {
                        int parcelas = Integer.parseInt(optionValue.split("x")[0].trim());
                        currentConfig.setInstallments(parcelas);
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao processar parcelas: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onInputProvided(String input) {
                Log.d(TAG, "Input fornecido: " + input);
                // Processar inputs do usuário se necessário
            }

            @Override
            public void onCancelPressed() {
                Log.d(TAG, "Cancelamento solicitado pelo usuário");
                cancelarTransacao();
            }

            @Override
            public void onConfirmPressed() {
                Log.d(TAG, "Confirmação da operação");
                // Continuar com a transação após confirmação
                if (currentConfig != null) {
                    switch (currentConfig.getTransactionType().toUpperCase()) {
                        case "PIX":
                            iniciarPagamentoPix(currentConfig);
                            break;
                        case "DEBIT":
                        case "DEBITO":
                            iniciarPagamentoCartao(currentConfig, PlugPag.TYPE_DEBITO);
                            break;
                        case "CREDIT":
                        case "CREDITO":
                        default:
                            iniciarPagamentoCartao(currentConfig, PlugPag.TYPE_CREDITO);
                            break;
                    }
                } else {
                    Log.e(TAG, "currentConfig is null. Cannot initiate payment.");
                    notifyError("Erro: Configuração de pagamento não encontrada.");
                }
            }

            @Override
            public void onPrintReceiptSelected(boolean shouldPrint) {
                Log.d(TAG, "Seleção de impressão: " + shouldPrint);
                if (shouldPrint) {
                    reprintClientReceipt();
                } else {
                    uiManager.dismissAll();
                }
            }

            @Override
            public void onReprintSelected(String receiptType) {
                Log.d(TAG, "Tipo de reimpressão selecionado: " + receiptType);
                if ("CUSTOMER".equals(receiptType)) {
                    reprintClientReceipt();
                } else if ("ESTABLISHMENT".equals(receiptType)) {
                    reprintEstablishmentReceipt();
                }
            }
        });
    }

    @Override
    public void iniciarPagamentoDeeplink(Activity activity, PaymentConfig config) {
        // Redireciona para o método SDK, sem mostrar o sumário inicial
        iniciarPagamentoProvider(activity, config, false);
    }

    @Override
    public void iniciarPagamentoProvider(Activity activity, PaymentConfig config, boolean showSummary) {
        if (!validarConfigPagamento(config)) {
            notifyError("Configuração de pagamento inválida");
            return;
        }

        if (isProcessing) {
            notifyError("Já existe uma transação em andamento");
            return;
        }

        this.currentConfig = config;
        this.isProcessing = true;

        try {
            // Criar TransactionModel inicial com status PENDING
            TransactionModel pendingTransaction = new TransactionModel(
                    UUID.randomUUID().toString(), // id
                    "", // transactionId (will be updated later)
                    config.getOrderId(), // paymentId
                    config.getOrderId(), // externalId
                    "", // authorizationCode
                    config.getAmountInCents() / 100.0, // amount
                    config.getAmountInCents(), // amountCents
                    config.getInstallments(), // installments
                    0.0, // discountAmount
                    "", // cardBrand
                    "", // maskedPan
                    "", // cardBin
                    "", // cardholderName
                    config.getTransactionType(), // paymentType
                    mapTransactionTypeToDebCre(config.getTransactionType()), // paymentTypeCode
                    "", // productName
                    "IFOOD", // acquirer (assuming "ifood" flavor)
                    "", // terminalId
                    "", // machineCode
                    "", // deviceSerial
                    "PENDING", // status
                    "", // statusCode
                    "", // errorReason
                    false, // isCancelled
                    "", // cancellationId
                    new Date(), // transactionDate
                    new Date(), // createdAt
                    new Date(), // updatedAt
                    false, // receiptPrinted
                    0, // reprintCount
                    null, // lastPrintDate
                    "", // metadata
                    "", // notes
                    1 // modelVersion
            );

            transactionManager.saveTransaction(pendingTransaction);
            this.pendingTransactionId = pendingTransaction.getId(); // Store the ID for later retrieval
            Log.d(TAG, "Transação pendente salva no banco de dados: " + this.pendingTransactionId);

            if (showSummary) {
                // Mostrar resumo da operação antes de iniciar
                PaymentUIManager.PaymentSummary summary = new PaymentUIManager.PaymentSummary(
                        config.getTransactionType(),
                        String.format("%.2f", config.getAmountInCents() / 100.0)
                );

                if (config.getInstallments() > 1) {
                    summary.setInstallments(config.getInstallments());
                }

                uiManager.showOperationSummary(summary, true);
            } else {
                // Se não for para mostrar o sumário, iniciar o pagamento diretamente
                switch (config.getTransactionType().toUpperCase()) {
                    case "PIX":
                        iniciarPagamentoPix(config);
                        break;
                    case "DEBIT":
                    case "DEBITO":
                        iniciarPagamentoCartao(config, PlugPag.TYPE_DEBITO);
                        break;
                    case "CREDIT":
                    case "CREDITO":
                    default:
                        iniciarPagamentoCartao(config, PlugPag.TYPE_CREDITO);
                        break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar pagamento: " + e.getMessage(), e);
            notifyError("Erro ao iniciar pagamento: " + e.getMessage());
            isProcessing = false;
        }
    }

    private void iniciarPagamentoCartao(PaymentConfig config, int paymentType) {
        executor.execute(() -> {
            try {
                // Mostrar status na UI
                mainHandler.post(() -> {
                    uiManager.showSDKMessage("Inicializando terminal...", "PROCESSING", true);
                });

                // Verificar se o serviço está ocupado e abortar se necessário
                if (plugPag.isServiceBusy()) {
                    plugPag.abort();
                }

                // Configurar listener de eventos
                plugPag.setEventListener(new PlugPagEventListener() {
                    @Override
                    public void onEvent(PlugPagEventData data) {
                        mainHandler.post(() -> handlePlugPagEvent(data));
                    }
                });

                // Configurar layout de impressão customizado
                configureCustomPrinterLayout();

                // Preparar dados do pagamento
                int installmentType = PlugPag.INSTALLMENT_TYPE_A_VISTA;
                int installments = 1;

                if (paymentType == PlugPag.TYPE_CREDITO && config.getInstallments() > 1) {
                    installmentType = PlugPag.INSTALLMENT_TYPE_PARC_VENDEDOR;
                    installments = config.getInstallments();
                }

                PlugPagPaymentData paymentData = new PlugPagPaymentData(
                        paymentType,
                        config.getAmountInCents(),
                        installmentType,
                        installments,
                        generateTransactionId(config),
                        true // printReceipt
                );

                Log.d(TAG, "Executando pagamento com cartão - Valor: " + config.getAmountInCents());

                mainHandler.post(() -> {
                    uiManager.showSDKMessage("Processando pagamento...", "PROCESSING", true);
                });

                // Executar o pagamento
                PlugPagTransactionResult result = plugPag.doPayment(paymentData);

                // Processar resultado na thread principal
                mainHandler.post(() -> handlePaymentResult(result, config));

            } catch (Exception e) {
                Log.e(TAG, "Erro na thread de pagamento: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    uiManager.showSDKMessage("Erro no pagamento", "ERROR", false);
                    notifyError("Erro no pagamento: " + e.getMessage());
                    isProcessing = false;
                });
            }
        });
    }

    private void iniciarPagamentoPix(PaymentConfig config) {
        executor.execute(() -> {
            try {
                // Mostrar status na UI
                mainHandler.post(() -> {
                    uiManager.showSDKMessage("Gerando PIX...", "PROCESSING", true);
                });

                // Verificar se o serviço está ocupado
                if (plugPag.isServiceBusy()) {
                    plugPag.abort();
                }

                // Configurar listener de eventos
                plugPag.setEventListener(new PlugPagEventListener() {
                    @Override
                    public void onEvent(PlugPagEventData data) {
                        mainHandler.post(() -> handlePlugPagEvent(data));
                    }
                });

                // Para PIX, usar o método específico se disponível
                // Nota: O PlugPag pode ter métodos específicos para PIX
                // Por enquanto, usando o método genérico de pagamento
                PlugPagPaymentData paymentData = new PlugPagPaymentData(
                        PlugPag.TYPE_PIX, // Assumindo que existe esta constante
                        config.getAmountInCents(),
                        PlugPag.INSTALLMENT_TYPE_A_VISTA,
                        1,
                        generateTransactionId(config),
                        true
                );

                Log.d(TAG, "Executando pagamento PIX - Valor: " + config.getAmountInCents());

                mainHandler.post(() -> {
                    uiManager.showSDKMessage("Aguardando pagamento PIX...", "PROCESSING", true);
                });

                PlugPagTransactionResult result = plugPag.doPayment(paymentData);
                mainHandler.post(() -> handlePaymentResult(result, config));

            } catch (Exception e) {
                Log.e(TAG, "Erro no pagamento PIX: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    uiManager.showSDKMessage("Erro no PIX", "ERROR", false);
                    notifyError("Erro no pagamento PIX: " + e.getMessage());
                    isProcessing = false;
                });
            }
        });
    }

    private void handlePlugPagEvent(PlugPagEventData data) {
        String message = "Processando...";
        String type = "PROCESSING";

        switch (data.getEventCode()) {
            case PlugPagEventData.EVENT_CODE_DIGIT_PASSWORD:
                Log.d(TAG, "Digitando senha...");
                message = "Digite a senha do cartão";
                uiManager.showSDKMessage(message, type, true);
                break;
            case PlugPagEventData.EVENT_CODE_NO_PASSWORD:
                Log.d(TAG, "Processando sem senha...");
                message = "Processando pagamento...";
                uiManager.showSDKMessage(message, type, true);
                break;
            case PlugPagEventData.EVENT_CODE_INSERTED_CARD:
                message = "Cartão inserido. Aguarde...";
                uiManager.showSDKMessage(message, type, true);
                break;
            case PlugPagEventData.EVENT_CODE_WAITING_REMOVE_CARD:
                message = "Remova o cartão";
                uiManager.showSDKMessage(message, "SUCCESS", false);
                break;
            case PlugPagEventData.EVENT_CODE_WAITING_CARD:
                message = "Insira o cartão ou aproxime";
                uiManager.showSDKMessage(message, type, true);
                break;
            default:
                if (data.getCustomMessage() != null && !data.getCustomMessage().isEmpty()) {
                    Log.d(TAG, "Mensagem do terminal: " + data.getCustomMessage());
                    message = data.getCustomMessage();
                    uiManager.showSDKMessage(message, type, true);
                }
                break;
        }
    }

    private void handlePaymentResult(PlugPagTransactionResult result, PaymentConfig config) {
        try {
            Log.d(TAG, "Resultado do pagamento: " + result.getMessage());

            if (result.getResult() == PlugPag.RET_OK) {
                // Pagamento aprovado
                processSuccessfulPayment(result, config);
            } else {
                // Pagamento falhou
                processFailedPayment(result, config);
            }

        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar resultado: " + e.getMessage(), e);
            notifyError("Erro crítico ao processar resultado: " + e.getMessage());
        }
    }

    private void processSuccessfulPayment(PlugPagTransactionResult result, PaymentConfig config) {
        try {
            String transactionId = result.getTransactionId() != null ? result.getTransactionId() : "N/A";
            String cardBrand = result.getCardBrand() != null ? result.getCardBrand() : "N/A";
            String authCode = result.getAutoCode() != null ? result.getTransactionCode() : "N/A";
            double amountReais = config.getAmountInCents() / 100.0;
            String debCre = mapTransactionTypeToDebCre(config.getTransactionType());

            Log.i(TAG, String.format(
                    "Pagamento aprovado: ID=%s, Valor=%.2f, Brand=%s, Auth=%s",
                    transactionId, amountReais, cardBrand, authCode
            ));

            // Mostrar sucesso na UI
            String transactionInfo = String.format(
                    "✅ Transação Aprovada\n💰 Valor: R$ %.2f\n💳 Cartão: %s\n🔢 ID: %s",
                    amountReais, cardBrand, transactionId
            );

            uiManager.showSDKMessage("Pagamento aprovado!", "SUCCESS", false);

            // Mostrar dialog de impressão após alguns segundos
            mainHandler.postDelayed(() -> {
                uiManager.showPrintReceiptDialog(transactionInfo, true);
            }, 2000);

            FancyToast.makeText(context,
                    String.format("Pagamento aprovado! R$ %.2f", amountReais),
                    FancyToast.LENGTH_LONG, FancyToast.SUCCESS, true).show();

            // Notificar através do callback handler
            if (pagamentoCallback != null) {
                PagamentoModel pagamento = new PagamentoModel();
                pagamento.setBandeira(cardBrand);
                pagamento.setAutorizacao(authCode);
                pagamento.setCartao(result.getBin() != null ? result.getBin() : ""); // Using bin as masked card
                pagamento.setNsu(result.getTransactionCode() != null ? result.getTransactionCode() : "");
                pagamento.setTerminal("SDK_TERMINAL");
                pagamento.setAdquirente("PAGSEGURO");
                pagamento.setIdPlataforma(transactionId);
                pagamento.setIdPlataformaReduzido(config.getOrderId());
                pagamento.setTransactionId(transactionId);
                pagamento.setIdPagamentoMovpagpa(config.getOrderId());
                pagamento.setBinCartao(result.getBin() != null ? result.getBin() : "");
                pagamento.setTipoCartaoDebCre(debCre);
                pagamento.setPgpVlrpag(amountReais);

                // Recuperar e atualizar TransactionModel
                TransactionModel transaction = transactionManager.getTransactionById(pendingTransactionId);
                if (transaction != null) {
                    transaction = transaction.copy(
                            transactionId = transactionId,
                            authorizationCode = authCode,
                            cardBrand = cardBrand,
                            maskedPan = result.getBin() != null ? result.getBin() : "",
                            cardBin = result.getBin() != null ? result.getBin() : "",
                            paymentType = config.getTransactionType(),
                            paymentTypeCode = debCre,
                            acquirer = "PAGSEGURO", // Assuming PagSeguro SDK
                            terminalId = "SDK_TERMINAL",
                            status = "APPROVED",
                            statusCode = String.valueOf(result.getResult()),
                            updatedAt = new Date()
                    );
                    transactionManager.saveTransaction(transaction); // Update the existing transaction
                    Log.d(TAG, "Transação atualizada no banco de dados: " + transaction.getTransactionId());
                } else {
                    Log.e(TAG, "Transação pendente não encontrada para atualização: " + pendingTransactionId);
                    // Fallback: create a new transaction if pending one not found
                    transaction = new TransactionModel(
                            UUID.randomUUID().toString(), // id
                            transactionId, // transactionId
                            config.getOrderId(), // paymentId (using orderId for now)
                            config.getOrderId(), // externalId (using orderId for now)
                            authCode, // authorizationCode
                            amountReais, // amount
                            config.getAmountInCents(), // amountCents
                            config.getInstallments(), // installments
                            0.0, // discountAmount
                            cardBrand, // cardBrand
                            result.getBin() != null ? result.getBin() : "", // maskedPan (using bin for now)
                            result.getBin() != null ? result.getBin() : "", // cardBin
                            "", // cardholderName
                            config.getTransactionType(), // paymentType
                            debCre, // paymentTypeCode
                            "", // productName
                            "PAGSEGURO", // acquirer
                            "SDK_TERMINAL", // terminalId
                            "", // machineCode
                            "", // deviceSerial
                            "APPROVED", // status
                            String.valueOf(result.getResult()), // statusCode
                            "", // errorReason
                            false, // isCancelled
                            "", // cancellationId
                            new Date(), // transactionDate
                            new Date(), // createdAt
                            new Date(), // updatedAt
                            false, // receiptPrinted
                            0, // reprintCount
                            null, // lastPrintDate
                            "", // metadata
                            "", // notes
                            1 // modelVersion
                    );
                    transactionManager.saveTransaction(transaction);
                    Log.d(TAG, "Nova transação criada como fallback: " + transaction.getTransactionId());
                }

                pagamentoCallback.onPagamentoSucesso(pagamento);
            }

        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar pagamento bem-sucedido: " + e.getMessage(), e);
            uiManager.showSDKMessage("Erro ao processar resultado", "ERROR", false);
            notifyError("Erro ao processar resultado do pagamento");
        } finally {
            isProcessing = false;
        }
    }

    private void processFailedPayment(PlugPagTransactionResult result, PaymentConfig config) {
        String errorMessage = result.getMessage() != null ? result.getMessage() : "Erro desconhecido";
        String status = "DENIED"; // Default status for failed payments
        String transactionId = result.getTransactionId() != null ? result.getTransactionId() : "N/A";
        String authCode = result.getAutoCode() != null ? result.getAutoCode() : "N/A";
        String cardBrand = result.getCardBrand() != null ? result.getCardBrand() : "N/A";
        double amountReais = config.getAmountInCents() / 100.0;
        String debCre = mapTransactionTypeToDebCre(config.getTransactionType());

        // Mapear códigos de erro específicos
        switch (result.getErrorCode()) {
            case "A202":
                Log.d(TAG, "Pagamento cancelado pelo usuário");
                uiManager.showSDKMessage("Pagamento cancelado", "WARNING", false);
                FancyToast.makeText(context, "Pagamento cancelado pelo usuário",
                        FancyToast.LENGTH_LONG, FancyToast.WARNING, true).show();

                status = "CANCELLED";
                if (pagamentoCallback != null) {
                    pagamentoCallback.onPagamentoCancelado();
                }
                break;

            case "A058":
                errorMessage = "Tempo esgotado para a transação";
                uiManager.showSDKMessage("Tempo esgotado", "ERROR", false);
                notifyError(errorMessage);
                break;

            case "A004":
                errorMessage = "Transação negada";
                uiManager.showSDKMessage("Transação negada", "ERROR", false);
                notifyError(errorMessage);
                break;

            case "A019":
                errorMessage = "Problema na comunicação. Tente novamente.";
                uiManager.showSDKMessage("Transação negada", "ERROR", false);
                notifyError(errorMessage);
                break;

            default:
                Log.e(TAG, "Pagamento falhou: " + errorMessage);
                uiManager.showSDKMessage("Erro no pagamento", "ERROR", false);
                notifyError(errorMessage);
                break;
        }

        // Recuperar e atualizar TransactionModel
        TransactionModel transaction = transactionManager.getTransactionById(pendingTransactionId);
        if (transaction != null) {
            transaction = transaction.copy(
                    transactionId = transactionId,
                    authorizationCode = authCode,
                    cardBrand = cardBrand,
                    maskedPan = result.getBin() != null ? result.getBin() : "",
                    cardBin = result.getBin() != null ? result.getBin() : "",
                    paymentType = config.getTransactionType(),
                    paymentTypeCode = debCre,
                    acquirer = "PAGSEGURO", // Assuming PagSeguro SDK
                    terminalId = "SDK_TERMINAL",
                    status = status,
                    statusCode = String.valueOf(result.getResult()),
                    errorReason = errorMessage,
                    isCancelled = status.equals("CANCELLED"),
                    updatedAt = new Date()
            );
            transactionManager.saveTransaction(transaction); // Update the existing transaction
            Log.d(TAG, "Transação falha atualizada no banco de dados: " + transaction.getTransactionId());
        } else {
            Log.e(TAG, "Transação pendente não encontrada para atualização: " + pendingTransactionId);
            // Fallback: create a new transaction if pending one not found
            transaction = new TransactionModel(
                    UUID.randomUUID().toString(), // id
                    transactionId, // transactionId
                    config.getOrderId(), // paymentId
                    config.getOrderId(), // externalId
                    authCode, // authorizationCode
                    amountReais, // amount
                    config.getAmountInCents(), // amountCents
                    config.getInstallments(), // installments
                    0.0, // discountAmount
                    cardBrand, // cardBrand
                    result.getBin() != null ? result.getBin() : "", // maskedPan (using bin for now)
                    result.getBin() != null ? result.getBin() : "", // cardBin
                    "", // cardholderName
                    config.getTransactionType(), // paymentType
                    debCre, // paymentTypeCode
                    "", // productName
                    "PAGSEGURO", // acquirer
                    "SDK_TERMINAL", // terminalId
                    "", // machineCode
                    "", // deviceSerial
                    status, // status
                    String.valueOf(result.getResult()), // statusCode
                    errorMessage, // errorReason
                    status.equals("CANCELLED"), // isCancelled
                    "", // cancellationId
                    new Date(), // transactionDate
                    new Date(), // createdAt
                    new Date(), // updatedAt
                    false, // receiptPrinted
                    0, // reprintCount
                    null, // lastPrintDate
                    "", // metadata
                    "", // notes
                    1 // modelVersion
            );
            transactionManager.saveTransaction(transaction);
            Log.d(TAG, "Nova transação falha criada como fallback: " + transaction.getTransactionId());
        }

        isProcessing = false;
    }

    @Override
    public void realizarReembolso(Activity activity, PagamentoModel pagamentoModel) {
        if (isProcessing) {
            uiManager.showSDKMessage("Aguarde a transação atual finalizar", "WARNING", false);
            notifyError("Aguarde a transação atual finalizar");
            return;
        }

        isProcessing = true;

        // Mostrar informações do estorno na UI
        String refundInfo = String.format(
                "🔄 Processando Estorno\n💰 Valor: R$ %.2f\nID: %s",
                pagamentoModel.getPgpVlrpag(),
                pagamentoModel.getNsu()
        );

        uiManager.showSDKMessage("Iniciando estorno...", "PROCESSING", true);

        executor.execute(() -> {
            try {
                FancyToast.makeText(activity, "Iniciando estorno via SDK...",
                        FancyToast.LENGTH_SHORT, FancyToast.INFO, true).show();

                // Buscar última transação aprovada
                PlugPagTransactionResult lastTransaction = plugPag.getLastApprovedTransaction();

                if (lastTransaction.getResult() == PlugPag.RET_OK) {
                    mainHandler.post(() -> {
                        uiManager.showSDKMessage("Processando estorno...", "PROCESSING", true);
                    });

                    // Executar estorno
                    PlugPagVoidData voidData = new PlugPagVoidData(
                            lastTransaction.getTransactionCode(),
                            lastTransaction.getTransactionId(),
                            true // printReceipt
                    );

                    PlugPagTransactionResult voidResult = plugPag.voidPayment(voidData);

                    mainHandler.post(() -> handleRefundResult(voidResult, pagamentoModel));

                } else {
                    mainHandler.post(() -> {
                        String error = "Nenhuma transação encontrada para estorno: " +
                                (lastTransaction.getMessage() != null ? lastTransaction.getMessage() : "");
                        uiManager.showSDKMessage("Erro no estorno", "ERROR", false);
                        notifyError(error);
                        isProcessing = false;
                    });
                }

            } catch (Exception e) {
                Log.e(TAG, "Erro no estorno: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    uiManager.showSDKMessage("Erro no estorno", "ERROR", false);
                    notifyError("Erro no estorno: " + e.getMessage());
                    isProcessing = false;
                });
            }
        });
    }

    private void handleRefundResult(PlugPagTransactionResult result, PagamentoModel originalPayment) {
        try {
            if (result.getResult() == PlugPag.RET_OK) {
                Log.d(TAG, "Estorno realizado com sucesso");

                String refundInfo = String.format(
                        "✅ Estorno Aprovado\n💰 Valor: R$ %.2f\n🔢 ID: %s",
                        originalPayment.getPgpVlrpag(),
                        result.getTransactionId()
                );

                uiManager.showSDKMessage("Estorno aprovado!", "SUCCESS", false);

                FancyToast.makeText(context, "Estorno realizado com sucesso!",
                        FancyToast.LENGTH_LONG, FancyToast.SUCCESS, true).show();

                // Mostrar dialog de impressão para o estorno
                mainHandler.postDelayed(() -> {
                    uiManager.showPrintReceiptDialog(refundInfo, true);
                }, 2000);

                // Criar modelo do pagamento cancelado
                PagamentoModel pagamentoCancelado = new PagamentoModel();
                pagamentoCancelado.setIdOrder(originalPayment.getIdOrder());
                pagamentoCancelado.setNsu(result.getTransactionId());
                pagamentoCancelado.setAutorizacao(result.getAutoCode());
                pagamentoCancelado.setPgpVlrpag(originalPayment.getPgpVlrpag());

                // Recuperar e atualizar TransactionModel para o estorno
                TransactionModel refundTransaction = transactionManager.getTransactionById(pendingTransactionId);
                if (refundTransaction != null) {
                    refundTransaction = refundTransaction.copy(
                            transactionId = result.getTransactionId(),
                            authorizationCode = result.getAutoCode(),
                            status = "REFUNDED",
                            statusCode = String.valueOf(result.getResult()),
                            isCancelled = true,
                            cancellationId = UUID.randomUUID().toString(),
                            updatedAt = new Date()
                    );
                    transactionManager.saveTransaction(refundTransaction);
                    Log.d(TAG, "Transação de estorno atualizada no banco de dados: " + refundTransaction.getTransactionId());
                } else {
                    Log.e(TAG, "Transação pendente de estorno não encontrada para atualização: " + pendingTransactionId);
                    // Fallback: create a new transaction if pending one not found
                    refundTransaction = new TransactionModel(
                            UUID.randomUUID().toString(), // id
                            result.getTransactionId(), // transactionId
                            originalPayment.getIdOrder(), // paymentId
                            originalPayment.getIdOrder(), // externalId
                            result.getAutoCode(), // authorizationCode
                            originalPayment.getPgpVlrpag(), // amount
                            (long) (originalPayment.getPgpVlrpag() * 100), // amountCents
                            1, // installments
                            0.0, // discountAmount
                            originalPayment.getBandeira(), // cardBrand
                            originalPayment.getCartao(), // maskedPan
                            originalPayment.getBinCartao(), // cardBin
                            "", // cardholderName
                            originalPayment.getTipoCartaoDebCre(), // paymentType
                            originalPayment.getTipoCartaoDebCre(), // paymentTypeCode
                            "", // productName
                            "PAGSEGURO", // acquirer
                            "SDK_TERMINAL", // terminalId
                            "", // machineCode
                            "", // deviceSerial
                            "REFUNDED", // status
                            String.valueOf(result.getResult()), // statusCode
                            "", // errorReason
                            true, // isCancelled (it's a refund)
                            UUID.randomUUID().toString(), // cancellationId
                            new Date(), // transactionDate
                            new Date(), // createdAt
                            new Date(), // updatedAt
                            false, // receiptPrinted
                            0, // reprintCount
                            null, // lastPrintDate
                            "", // metadata
                            "", // notes
                            1 // modelVersion
                    );
                    transactionManager.saveTransaction(refundTransaction);
                    Log.d(TAG, "Nova transação de estorno criada como fallback: " + refundTransaction.getTransactionId());
                }

                // Notificar através do callback handler
                if (pagamentoCallback != null) {
                    pagamentoCallback.onRefundSuccess(pagamentoCancelado);
                }

            } else {
                String errorMsg = result.getMessage() != null ? result.getMessage() : "Erro desconhecido no estorno";
                Log.e(TAG, "Falha no estorno: " + errorMsg);
                uiManager.showSDKMessage("Falha no estorno", "ERROR", false);
                notifyError("Falha no estorno: " + errorMsg);

                // Recuperar e atualizar TransactionModel para estorno falho
                TransactionModel failedRefundTransaction = transactionManager.getTransactionById(pendingTransactionId);
                if (failedRefundTransaction != null) {
                    failedRefundTransaction = failedRefundTransaction.copy(
                            transactionId = result.getTransactionId(),
                            authorizationCode = result.getAutoCode(),
                            status = "REFUND_FAILED",
                            statusCode = String.valueOf(result.getResult()),
                            errorReason = errorMsg,
                            updatedAt = new Date()
                    );
                    transactionManager.saveTransaction(failedRefundTransaction);
                    Log.d(TAG, "Transação de estorno falha atualizada no banco de dados: " + failedRefundTransaction.getTransactionId());
                } else {
                    Log.e(TAG, "Transação pendente de estorno falho não encontrada para atualização: " + pendingTransactionId);
                    // Fallback: create a new transaction if pending one not found
                    failedRefundTransaction = new TransactionModel(
                            UUID.randomUUID().toString(), // id
                            result.getTransactionId(), // transactionId
                            originalPayment.getIdOrder(), // paymentId
                            originalPayment.getIdOrder(), // externalId
                            result.getAutoCode(), // authorizationCode
                            originalPayment.getPgpVlrpag(), // amount
                            (long) (originalPayment.getPgpVlrpag() * 100), // amountCents
                            1, // installments
                            0.0, // discountAmount
                            originalPayment.getBandeira(), // cardBrand
                            originalPayment.getCartao(), // maskedPan
                            originalPayment.getBinCartao(), // cardBin
                            "", // cardholderName
                            originalPayment.getTipoCartaoDebCre(), // paymentType
                            originalPayment.getTipoCartaoDebCre(), // paymentTypeCode
                            "", // productName
                            "PAGSEGURO", // acquirer
                            "SDK_TERMINAL", // terminalId
                            "", // machineCode
                            "", // deviceSerial
                            "REFUND_FAILED", // status
                            String.valueOf(result.getResult()), // statusCode
                            errorMsg, // errorReason
                            false, // isCancelled
                            "", // cancellationId
                            new Date(), // transactionDate
                            new Date(), // createdAt
                            new Date(), // updatedAt
                            false, // receiptPrinted
                            0, // reprintCount
                            null, // lastPrintDate
                            "", // metadata
                            "", // notes
                            1 // modelVersion
                    );
                    transactionManager.saveTransaction(failedRefundTransaction);
                    Log.d(TAG, "Nova transação de estorno falha criada como fallback: " + failedRefundTransaction.getTransactionId());
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar resultado do estorno: " + e.getMessage(), e);
            uiManager.showSDKMessage("Erro no estorno", "ERROR", false);
            notifyError("Erro ao processar estorno: " + e.getMessage());
        } finally {
            isProcessing = false;
        }
    }

    private IPaymentCallbackHandler.PaymentListener createTemporaryListener() {
        return new IPaymentCallbackHandler.PaymentListener() {
            @Override
            public void onPaymentSuccess(String brand, String authCode, String mask, String doc,
                                         String terminal, String adquirente, String idPlataforma,
                                         String idPlataformaResumido, String codpagMaq, String rede,
                                         String transactionId, String idPagamento, String binCartao,
                                         String debcre, double amount) {
                if (pagamentoCallback != null) {
                    PagamentoModel pagamento = new PagamentoModel();
                    pagamento.setBandeira(brand);
                    pagamento.setAutorizacao(authCode);
                    pagamento.setCartao(mask);
                    pagamento.setNsu(doc);
                    pagamento.setTerminal(terminal);
                    pagamento.setAdquirente(adquirente);
                    pagamento.setIdPlataforma(idPlataforma);
                    pagamento.setIdPlataformaReduzido(idPlataformaResumido);
                    pagamento.setTransactionId(transactionId);
                    pagamento.setIdPagamentoMovpagpa(idPagamento);
                    pagamento.setBinCartao(binCartao);
                    pagamento.setTipoCartaoDebCre(debcre);
                    pagamento.setPgpVlrpag(amount);
                    pagamentoCallback.onPagamentoSucesso(pagamento);
                }
            }

            @Override
            public void onPaymentError(String reason) {
                if (pagamentoCallback != null) {
                    pagamentoCallback.onPagamentoFalha(reason);
                }
            }

            @Override
            public void onPaymentCancelled() {
                if (pagamentoCallback != null) {
                    pagamentoCallback.onPagamentoCancelado();
                }
            }

            @Override
            public void onRefundSuccess(PagamentoModel pagamentoCancelado) {
                if (pagamentoCallback != null) {
                    pagamentoCallback.onRefundSuccess(pagamentoCancelado);
                }
            }
        };
    }

    private void configureCustomPrinterLayout() {
        try {
            PlugPagCustomPrinterLayout printerLayout = new PlugPagCustomPrinterLayout(
                    "Imprimir via do cliente?",
                    "#000000",  // textColor
                    "#FFFFFF",  // backgroundColor
                    "#A0A0A0",  // buttonTextColor
                    "#FFFFFF",  // buttonBackgroundColor
                    "#000000",  // titleTextColor
                    "#808080",  // messageTextColor
                    "#FFFFFF",  // dialogBackgroundColor
                    60          // timeoutSeconds
            );

            plugPag.setPlugPagCustomPrinterLayout(printerLayout);
            Log.d(TAG, "Layout de impressão configurado");

        } catch (Exception e) {
            Log.w(TAG, "Erro ao configurar layout de impressão: " + e.getMessage());
        }
    }

    private String generateTransactionId(PaymentConfig config) {
        return BerpModel.getNmTpvend() + "_" +
                BerpModel.getNumMesa() + "_" +
                config.getOrderId() + "_" +
                System.currentTimeMillis();
    }

    private String mapTransactionTypeToDebCre(String transactionType) {
        if (transactionType == null) return "CRE";

        switch (transactionType.toUpperCase()) {
            case "DEBIT":
            case "DEBITO":
                return "DEB";
            case "PIX":
                return "PIX";
            case "VOUCHER":
                return "DEB"; // Voucher normalmente é tratado como débito
            case "CREDIT":
            case "CREDITO":
            default:
                return "CRE";
        }
    }

    private void notifyError(String errorMessage) {
        Log.e(TAG, errorMessage);

        // Mostrar erro na UI usando PaymentUIManager
        uiManager.showSDKMessage("Erro: " + errorMessage, "ERROR", false);

        FancyToast.makeText(context, errorMessage,
                FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();

        if (callbackHandler != null) {
            IPaymentCallbackHandler.PaymentListener listener = createTemporaryListener();
            callbackHandler.setPaymentListener(listener);
            listener.onPaymentError(errorMessage);
        }

        // Fechar UI após alguns segundos
        mainHandler.postDelayed(() -> uiManager.dismissAll(), 4000);
    }

    private boolean validarConfigPagamento(PaymentConfig config) {
        if (config == null) {
            Log.e(TAG, "PaymentConfig é null");
            return false;
        }

        if (config.getAmountInCents() <= 0) {
            Log.e(TAG, "Valor inválido: " + config.getAmountInCents());
            return false;
        }

        if (config.getOrderId() == null || config.getOrderId().isEmpty()) {
            Log.e(TAG, "OrderId é obrigatório");
            return false;
        }

        return true;
    }

    @Override
    public void processarResultado(int requestCode, int resultCode, Intent data) {
        // Com SDK, este método não é mais necessário, mas mantido por compatibilidade
        Log.d(TAG, "processarResultado chamado - usando SDK, callback automático");
    }

    @Override
    public void setCallback(IPagamentoCallback callback) {
        this.pagamentoCallback = callback;
        Log.d(TAG, "Callback de pagamento configurado: " + (callback != null));
    }

    /**
     * Método para cancelar transação em andamento
     */
    public void cancelarTransacao() {
        if (isProcessing) {
            try {
                uiManager.showSDKMessage("Cancelando transação...", "WARNING", false);
                uiManager.dismissAll();
                plugPag.abort();
                Log.d(TAG, "Transação cancelada");
                isProcessing = false;

                if (callbackHandler != null) {
                    IPaymentCallbackHandler.PaymentListener listener = createTemporaryListener();
                    callbackHandler.setPaymentListener(listener);
                    listener.onPaymentCancelled();
                }

                // Fechar UI após cancelamento
                mainHandler.postDelayed(() -> uiManager.dismissAll(), 2000);

            } catch (Exception e) {
                Log.e(TAG, "Erro ao cancelar transação: " + e.getMessage(), e);
                uiManager.showSDKMessage("Erro no cancelamento", "ERROR", false);
            }
        }
    }

    /**
     * Verifica se o terminal está pronto para uso
     */
    public boolean isTerminalReady() {
        try {
            return plugPag != null && !plugPag.isServiceBusy();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao verificar status do terminal: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Reimprime a via do cliente da última transação
     */
    public void reprintClientReceipt() {
        executor.execute(() -> {
            try {
                Log.d(TAG, "Reimprimindo via do cliente...");

                mainHandler.post(() -> {
                    uiManager.showSDKMessage("Reimprimindo via do cliente...", "PROCESSING", true);
                });

                br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrintResult result =
                        plugPag.reprintCustomerReceipt();

                mainHandler.post(() -> {
                    if (result.getResult() == PlugPag.RET_OK) {
                        uiManager.showSDKMessage("Via do cliente reimpressa!", "SUCCESS", false);
                        FancyToast.makeText(context, "Via do cliente reimpressa!",
                                FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();
                    } else {
                        uiManager.showSDKMessage("Falha na reimpressão", "ERROR", false);
                        notifyError("Falha na reimpressão: " + result.getMessage());
                    }

                    // Fechar UI após alguns segundos
                    mainHandler.postDelayed(() -> uiManager.dismissAll(), 3000);
                });

            } catch (Exception e) {
                Log.e(TAG, "Erro na reimpressão: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    uiManager.showSDKMessage("Erro na reimpressão", "ERROR", false);
                    notifyError("Erro na reimpressão: " + e.getMessage());
                });
            }
        });
    }

    /**
     * Reimprime a via do estabelecimento da última transação
     */
    public void reprintEstablishmentReceipt() {
        executor.execute(() -> {
            try {
                Log.d(TAG, "Reimprimindo via do estabelecimento...");

                mainHandler.post(() -> {
                    uiManager.showSDKMessage("Reimprimindo via do estabelecimento...", "PROCESSING", true);
                });

                br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrintResult result =
                        plugPag.reprintStablishmentReceipt();

                mainHandler.post(() -> {
                    if (result.getResult() == PlugPag.RET_OK) {
                        uiManager.showSDKMessage("Via do estabelecimento reimpressa!", "SUCCESS", false);
                        FancyToast.makeText(context, "Via do estabelecimento reimpressa!",
                                FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();
                    } else {
                        uiManager.showSDKMessage("Falha na reimpressão", "ERROR", false);
                        notifyError("Falha na reimpressão: " + result.getMessage());
                    }

                    // Fechar UI após alguns segundos
                    mainHandler.postDelayed(() -> uiManager.dismissAll(), 3000);
                });

            } catch (Exception e) {
                Log.e(TAG, "Erro na reimpressão: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    uiManager.showSDKMessage("Erro na reimpressão", "ERROR", false);
                    notifyError("Erro na reimpressão: " + e.getMessage());
                });
            }
        });
    }

    /**
     * Limpa recursos quando não precisar mais da instância
     */
    public void cleanup() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }

        try {
            if (plugPag != null && plugPag.isServiceBusy()) {
                plugPag.abort();
            }
        } catch (Exception e) {
            Log.w(TAG, "Erro ao limpar recursos: " + e.getMessage());
        }

        // Limpar UI
        if (uiManager != null) {
            uiManager.dismissAll();
        }

        isProcessing = false;
    }

    /**
     * Mostra o status atual do terminal na UI
     */
    public void showTerminalStatus() {
        String status = getTerminalStatus();
        uiManager.showSDKMessage(status, isTerminalReady() ? "SUCCESS" : "WARNING", false);

        // Auto-fechar após alguns segundos
        mainHandler.postDelayed(() -> uiManager.dismissAll(), 3000);
    }

    /**
     * Obtém o status do terminal para exibição
     */
    public String getTerminalStatus() {
        return isTerminalReady() ?
                "✅ Terminal pronto para uso" :
                "⚠️ Terminal não está pronto";
    }

    /**
     * Solicita entrada de dados do usuário através da UI
     */
    public void requestUserInput(String inputType, String message) {
        Log.d(TAG, "Solicitando input: " + inputType + " - " + message);

        mainHandler.post(() -> {
            switch (inputType.toUpperCase()) {
                case "CARDHOLDER_NAME":
                    uiManager.showInputDialog(
                            "Nome do Portador",
                            "Digite o nome como está no cartão",
                            "text",
                            true
                    );
                    break;
                case "CVV":
                    uiManager.showInputDialog(
                            "Código de Segurança",
                            "Digite o CVV (3 ou 4 dígitos)",
                            "password",
                            true
                    );
                    break;
                case "AMOUNT":
                    uiManager.showInputDialog(
                            "Valor da Transação",
                            "Digite o valor (ex: 25.90)",
                            "number",
                            true
                    );
                    break;
                default:
                    uiManager.showInputDialog(
                            "Entrada Requerida",
                            message,
                            "text",
                            true
                    );
                    break;
            }
        });
    }

    /**
     * Solicita seleção de opções do usuário (ex: parcelas)
     */
    public void requestUserOption(String title, String[] options) {
        Log.d(TAG, "Solicitando opção: " + title);

        mainHandler.post(() -> {
            uiManager.showOptionsList(title, options, true, 0);
        });
    }

    /**
     * Mostra diálogo para reimpressão de comprovantes
     */
    public void showReprintDialog() {
        String lastTransactionInfo = """
            📄 Última Transação
            💰 Valor: Disponível
            📱 Terminal: SDK PlugPag
            🕐 Reimpressão disponível
            """;

        mainHandler.post(() -> {
            uiManager.showReprintDialog(lastTransactionInfo);
        });
    }
}