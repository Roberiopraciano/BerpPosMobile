package br.com.berpsistemas.BerpPOSMobile.pagamento

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import br.com.berpsistemas.BerpPOSMobile.SmartPOSPluginManager
import br.com.berpsistemas.BerpPOSMobile.database.EnhancedTransactionManagerV2
import br.com.berpsistemas.BerpPOSMobile.model.BerpModel
import br.com.berpsistemas.BerpPOSMobile.model.PagamentoModel
import br.com.berpsistemas.BerpPOSMobile.ui.PaymentUIManager
import com.shashank.sony.fancytoastlib.FancyToast
import com.zoop.pos.Zoop
import com.zoop.pos.Zoop.getPlugins
import com.zoop.pos.collection.MenuOptionsData
import com.zoop.pos.collection.ReceiptType
import com.zoop.pos.collection.TransactionData
import com.zoop.pos.collection.UserInput
import com.zoop.pos.collection.UserSelection
import com.zoop.pos.plugin.DashboardConfirmationResponse
import com.zoop.pos.plugin.smartpos.SmartPOSPlugin
import com.zoop.pos.plugin.smartpos.manufacturer.terminal.Printer
import com.zoop.pos.plugin.smartpos.requestBuilder.SmartPOSMenuOptions
import com.zoop.pos.plugin.smartpos.requestBuilder.SmartPOSPaymentResponse
import com.zoop.pos.plugin.smartpos.requestBuilder.SmartPOSPixPaymentResponse
import com.zoop.pos.plugin.smartpos.requestBuilder.SmartPOSPrinterResponse
import com.zoop.pos.plugin.smartpos.requestBuilder.SmartPOSZoopKeyValidationResponse
import com.zoop.pos.requestfield.MessageCallbackRequestField
import com.zoop.pos.requestfield.PinCallbackRequestField
import com.zoop.pos.requestfield.QRCodeCallbackRequestField
import com.zoop.pos.requestfield.TransactionIdCallbackRequestField
import com.zoop.pos.type.Callback
import com.zoop.pos.type.Option
import com.zoop.pos.type.Request

class Pagamento(private val context: Activity) : IPagamento, PaymentUIManager.PaymentUICallback {

    companion object {
        private const val TAG = "Pagamento"

        // Constantes para tipos de pagamento
        const val DEBIT = "DEBIT"
        const val CREDIT = "CREDIT"
        const val VOUCHER = "VOUCHER"
        const val INSTANT_PAYMENT = "INSTANT_PAYMENT"
        const val PIX = "PIX"
    }

    // Componentes principais
    private val uiManager = PaymentUIManager(context)
    private val transactionManager = EnhancedTransactionManagerV2(context)
    private var pluginManager: SmartPOSPluginManager? = null
    private var isZoopInitialized = false

    // Estado da transação atual
    private var currentAmount = 0.0
    private var currentTransactionType = ""
    private var currentSummary: PaymentUIManager.PaymentSummary? = null
    private var currentCallback: PaymentCallback? = null

    // Requests do Zoop
    private var paymentRequest: Request? = null
    private var pixRequest: Request? = null
    private var printRequest: Request? = null

    // Interface para callbacks de pagamento
    interface PaymentCallback {
        fun onPaymentSuccess(transactionId: String, amount: String, cardBrand: String?)
        fun onPaymentError(errorMessage: String)
        fun onPaymentCanceled()
    }

    // Listener de eventos (opcional)
    interface PaymentEventListener {
        fun onMessage(message: String)
    }

    var listener: PaymentEventListener? = null

    init {
        uiManager.setCallback(this)
    }

    // =================== INICIALIZAÇÃO ZOOP ===================

    private fun initializeZoopPlugins() {
        try {
            if (isZoopInitialized) {
                Log.d(TAG, "Zoop já inicializado")
                return
            }

            // Configurar credenciais
            val credentials = DashboardConfirmationResponse.Credentials(
                "a27f7a594c2248779f49c8d9905f6455",  // marketplace
                "ae197f3604024385ba1c7041f4c65a51",  // seller
                "2840659033",  // terminal
                "7a1e00bb-a884-415f-b730-2ef696e5f0ac" // accessKey
            )

            // Inicializar plugin manager
            pluginManager = SmartPOSPluginManager(credentials)
            pluginManager!!.initialize(context)

            isZoopInitialized = true

            // Log dos plugins carregados
            Log.d(TAG, "Plugins carregados:")
            for (plugin in getPlugins()) {
                Log.d(TAG, "Plugin: ${plugin.javaClass.name}")
            }

            Log.d(TAG, "Zoop inicializado com sucesso")

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao inicializar Zoop: ${e.message}", e)
            isZoopInitialized = false
            throw e
        }
    }

    private fun ensureZoopInitialized(): Boolean {
        if (!isZoopInitialized) {
            try {
                initializeZoopPlugins()
            } catch (e: Exception) {
                showError("Erro ao inicializar SDK: ${e.message}")
                return false
            }
        }
        return isZoopInitialized
    }

    // =================== IMPLEMENTAÇÃO DA INTERFACE IPagamento ===================

    override fun iniciarPagamentoDeeplink(activity: Activity, config: PaymentConfig) {
        Log.d(TAG, "Iniciando pagamento QuickPay via config")

        if (!ensureZoopInitialized()) {
            return
        }

        val amountInReais = config.amountInCents / 100.0
        val transactionType = mapearTipoTransacao(config.transactionType)

        processPayment(amountInReais, transactionType, object : PaymentCallback {
            override fun onPaymentSuccess(transactionId: String, amount: String, cardBrand: String?) {
                Log.d(TAG, "Pagamento aprovado - ID: $transactionId")
                FancyToast.makeText(activity, "Pagamento aprovado! ID: $transactionId",
                    FancyToast.LENGTH_LONG, FancyToast.SUCCESS, true).show()
            }

            override fun onPaymentError(errorMessage: String) {
                Log.e(TAG, "Erro no pagamento: $errorMessage")
                FancyToast.makeText(activity, "Erro: $errorMessage",
                    FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show()
            }

            override fun onPaymentCanceled() {
                Log.d(TAG, "Pagamento cancelado")
                FancyToast.makeText(activity, "Pagamento cancelado",
                    FancyToast.LENGTH_SHORT, FancyToast.INFO, true).show()
            }
        })
    }

    override fun iniciarPagamentoProvider(activity: Activity, config: PaymentConfig) {
        iniciarPagamentoDeeplink(activity, config)
    }

    override fun realizarReembolso(activity: Activity, pag: PagamentoModel) {
        Log.d(TAG, "Iniciando reembolso para transação: ${pag.idPlataforma}")

        try {
            val transactionId = pag.idPlataforma?.replace("\"", "") ?: ""

            if (transactionId.isEmpty()) {
                showError("ID da transação não encontrado")
                return
            }

            // Verificar se a transação pode ser cancelada
            if (!transactionManager.canCancelTransaction(transactionId)) {
                showError("Esta transação não pode ser cancelada")
                return
            }

            uiManager.showSDKMessage("Processando cancelamento...", "PROCESSING", true)

            // Processar cancelamento
            val success = transactionManager.cancelTransaction(transactionId, "Cancelamento via reembolso")

            delayedExecution(2000) {
                if (success) {
                    uiManager.showSDKMessage("Cancelamento realizado com sucesso!", "SUCCESS", false)
                    FancyToast.makeText(activity, "Cancelamento realizado com sucesso",
                        FancyToast.LENGTH_LONG, FancyToast.SUCCESS, true).show()
                } else {
                    uiManager.showSDKMessage("Erro no cancelamento", "ERROR", false)
                    FancyToast.makeText(activity, "Erro ao processar cancelamento",
                        FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show()
                }

                delayedExecution(2000) {
                    uiManager.dismissAll()
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao processar reembolso", e)
            showError("Erro ao processar reembolso: ${e.message}")
        }
    }

    override fun processarResultado(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "processarResultado chamado - QuickPay usa callbacks internos")
        // No QuickPay, os resultados são tratados via callbacks do SDK
    }

    override fun setCallback(callback: IPagamentoCallback?) {
        Log.d(TAG, "Callback IPagamento configurado (compatibilidade)")
    }

    // =================== MÉTODOS PRINCIPAIS DE PAGAMENTO ===================

    fun processPayment(amount: Double, paymentType: String, callback: PaymentCallback) {
        this.currentCallback = callback
        this.currentAmount = amount
        this.currentTransactionType = paymentType

        Log.d(TAG, "Processando pagamento - Tipo: $paymentType, Valor: R$ $amount")

        if (!ensureZoopInitialized()) {
            return
        }

        try {
            // Mostrar resumo da operação antes de iniciar
            currentSummary = PaymentUIManager.PaymentSummary(paymentType, amount.toString())
            uiManager.showOperationSummary(currentSummary!!, true)

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao processar pagamento: ${e.message}", e)
            showError("Erro ao processar pagamento: ${e.message}")
        }
    }

    fun processPixPayment(amount: Double, callback: PaymentCallback) {
        this.currentCallback = callback
        this.currentAmount = amount
        this.currentTransactionType = "PIX"

        Log.d(TAG, "Processando pagamento PIX - Valor: R$ $amount")

        if (!ensureZoopInitialized()) {
            return
        }

        try {
            // Mostrar resumo do PIX
            currentSummary = PaymentUIManager.PaymentSummary("PIX", amount.toString())
            uiManager.showOperationSummary(currentSummary!!, true)

        } catch (e: Exception) {
            Log.e(TAG, "Erro no pagamento PIX: ${e.message}", e)
            showError("Erro PIX: ${e.message}")
        }
    }

    private fun startPaymentTransaction() {
        try {
            when (currentTransactionType.uppercase()) {
                "PIX" -> startPixTransaction()
                else -> startCardTransaction()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao iniciar transação: ${e.message}", e)
            showError("Erro ao iniciar transação: ${e.message}")
        }
    }

    private fun startCardTransaction() {
        try {
            val amountInCents = (currentAmount * 100).toLong()

            val opcao = when (currentTransactionType.uppercase()) {
                "DEBIT" -> Option.DEBIT
                else -> Option.CREDIT
            }

            Log.d(TAG, "Iniciando transação cartão: $currentTransactionType - R$ $currentAmount")

            paymentRequest = SmartPOSPlugin.createPaymentRequestBuilder()
                .amount(amountInCents)
                .option(opcao)
                .autoPrintEstablishmentReceipt(true)
                .callback(object : Callback<SmartPOSPaymentResponse>() {
                    override fun onStart() {
                        Log.d(TAG, "Iniciando pagamento...")
                        listener?.onMessage("Iniciando pagamento...")
                        uiManager.showSDKMessage("Iniciando pagamento...", "PROCESSING", true)
                    }

                    override fun onSuccess(response: SmartPOSPaymentResponse) {
                        Log.d(TAG, "Pagamento realizado com sucesso!")
                        listener?.onMessage("Pagamento realizado com sucesso!")

                        val transactionId = response.transactionData.transactionId ?: "N/A"
                        val cardBrand = response.transactionData.brand ?: "N/A"
                        val amountStr = String.format("%.2f", currentAmount)

                        // Salvar na base de dados
                        saveTransactionData(response.transactionData)

                        currentCallback?.onPaymentSuccess(transactionId, amountStr, cardBrand)

                        val transactionInfo = """
                            ✅ Transação Aprovada
                            💰 Valor: R$ $amountStr
                            💳 Cartão: $cardBrand
                            🔢 ID: $transactionId
                        """.trimIndent()

                        uiManager.showPrintReceiptDialog(transactionInfo, false)

                        delayedExecution(10000) {
                            uiManager.dismissAll()
                        }
                    }

                    override fun onFail(error: Throwable) {
                        handlePaymentError(error)
                    }
                })
                .messageCallback(object : Callback<MessageCallbackRequestField.MessageData>() {
                    override fun onSuccess(response: MessageCallbackRequestField.MessageData) {
                        Log.d(TAG, "Mensagem: ${response.message}")
                        listener?.onMessage("Mensagem: ${response.message}")
                        uiManager.showSDKMessage(response.message, "PROCESSING", true)
                    }

                    override fun onFail(error: Throwable) {
                        Log.d(TAG, "Falha na mensagem do pagamento")
                        uiManager.showSDKMessage("Processando...", "PROCESSING", true)
                    }
                })
                .pinCallback(object : Callback<PinCallbackRequestField.PinData>() {
                    override fun onSuccess(response: PinCallbackRequestField.PinData) {
                        handlePinEvent(response)
                    }

                    override fun onFail(error: Throwable) {
                        Log.e(TAG, "Erro de PIN: ${error.message}")
                        uiManager.showSDKMessage("Erro de PIN: ${error.message}", "ERROR", false)
                    }
                })
                .menuSelectionCallback(object : Callback<SmartPOSMenuOptions>() {
                    override fun onSuccess(response: SmartPOSMenuOptions) {
                        handleMenuSelection(response)
                    }

                    override fun onFail(error: Throwable) {
                        Log.e(TAG, "Erro no menu de opções: ${error.message}")
                    }
                })
                .userInputCallback(object : Callback<UserInput>() {
                    override fun onSuccess(response: UserInput) {
                        Log.d(TAG, "Input do usuário solicitado")
                        // Implementar se necessário
                    }

                    override fun onFail(error: Throwable) {
                        Log.e(TAG, "Erro no input do usuário: ${error.message}")
                    }
                })
                .build()

            Zoop.post(paymentRequest!!)

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao criar transação de cartão", e)
            showError("Erro ao criar transação: ${e.message}")
        }
    }

    private fun startPixTransaction() {
        try {
            val amountInCents = (currentAmount * 100).toLong()

            Log.d(TAG, "Iniciando transação PIX: R$ $currentAmount")

            pixRequest = SmartPOSPlugin.createPixPaymentRequestBuilder()
                .amount(amountInCents)
                .callback(object : Callback<SmartPOSPixPaymentResponse>() {
                    override fun onStart() {
                        Log.d(TAG, "Iniciando pagamento PIX...")
                        listener?.onMessage("Iniciando pagamento PIX...")
                        uiManager.showSDKMessage("Gerando PIX...", "PROCESSING", true)
                    }

                    override fun onSuccess(response: SmartPOSPixPaymentResponse) {
                        Log.d(TAG, "Pagamento PIX realizado com sucesso!")
                        listener?.onMessage("Pagamento PIX realizado com sucesso!")

                        val transactionId = response.transactionData.transactionId ?: "N/A"
                        val amountStr = String.format("%.2f", currentAmount)

                        saveTransactionData(response.transactionData)

                        currentCallback?.onPaymentSuccess(transactionId, amountStr, "PIX")

                        val transactionInfo = """
                            ✅ Transação PIX Aprovada
                            💰 Valor: R$ $amountStr
                            🔢 ID: $transactionId
                        """.trimIndent()

                        uiManager.showPrintReceiptDialog(transactionInfo, false)

                        // Imprimir via do estabelecimento automaticamente
                        printEstablishmentReceipt(response.transactionData)

                        delayedExecution(5000) {
                            uiManager.dismissAll()
                        }
                    }

                    override fun onFail(error: Throwable) {
                        handlePixError(error)
                    }
                })
                .qrCodeCallback(object : Callback<QRCodeCallbackRequestField.QRCodeData>() {
                    override fun onSuccess(qrCodeData: QRCodeCallbackRequestField.QRCodeData) {
                        Log.d(TAG, "QR Code PIX gerado")
                        listener?.onMessage("QR Code PIX gerado")

                        val qrCode = qrCodeData.data
                        val amountStr = String.format("%.2f", currentAmount)

                        context.runOnUiThread {
                            uiManager.showPixQRCode(qrCode, amountStr, true)
                        }
                    }

                    override fun onFail(error: Throwable) {
                        Log.e(TAG, "Erro ao gerar QR Code PIX: ${error.message}")
                        uiManager.showSDKMessage("Erro ao gerar QR Code PIX", "ERROR", false)
                    }
                })
                .transactionIdCallback(object : Callback<TransactionIdCallbackRequestField.transactionIdData>() {
                    override fun onSuccess(transactionIdData: TransactionIdCallbackRequestField.transactionIdData) {
                        Log.d(TAG, "Transaction ID PIX: ${transactionIdData.data}")
                        listener?.onMessage("Transaction ID PIX recebido")
                    }

                    override fun onFail(error: Throwable) {
                        Log.e(TAG, "Erro ao obter Transaction ID PIX: ${error.message}")
                    }
                })
                .messageCallback(object : Callback<MessageCallbackRequestField.MessageData>() {
                    override fun onSuccess(messageData: MessageCallbackRequestField.MessageData) {
                        Log.d(TAG, "Mensagem PIX: ${messageData.message}")
                        listener?.onMessage("Mensagem PIX: ${messageData.message}")
                        uiManager.showSDKMessage(messageData.message, "PROCESSING", true)
                    }

                    override fun onFail(error: Throwable) {
                        Log.e(TAG, "Erro na mensagem PIX: ${error.message}")
                    }
                })
                .build()

            Zoop.post(pixRequest!!)

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao iniciar transação PIX", e)
            showError("Erro ao iniciar transação PIX: ${e.message}")
        }
    }

    // =================== HANDLERS PARA CALLBACKS DO ZOOP ===================

    private fun handlePinEvent(response: PinCallbackRequestField.PinData) {
        Log.d(TAG, "Pin Event: ${response.type}")
        listener?.onMessage("Pin Event: ${response.type}")

        val eventTypeString = response.type.toString()
        context.runOnUiThread {
            when {
                eventTypeString.contains("Start", ignoreCase = true) -> {
                    Log.d(TAG, "Iniciando entrada de PIN")
                    uiManager.createViewToDisplayPasswordInput()
                }
                eventTypeString.contains("Finish", ignoreCase = true) -> {
                    Log.d(TAG, "Finalizando entrada de PIN")
                    uiManager.finishPasswordInput()
                }
                eventTypeString.contains("Insert", ignoreCase = true) -> {
                    Log.d(TAG, "Caractere de PIN inserido")
                    uiManager.handlePasswordCharacterInput()
                }
                eventTypeString.contains("Remove", ignoreCase = true) -> {
                    Log.d(TAG, "Caractere de PIN removido")
                    uiManager.handlePasswordCharacterRemoved()
                }
                else -> {
                    Log.d(TAG, "PIN limpo ou evento desconhecido: $eventTypeString")
                    uiManager.handlePasswordCharacterCleared()
                }
            }
        }
    }

    private fun handleMenuSelection(response: SmartPOSMenuOptions) {
        val options = convertMenuOptionsToStringArray(response.options)
        val title = response.title
        val defaultOption = response.defaultOption

        Log.d(TAG, "Menu de opções recebido: $title")
        uiManager.showOptionsList(title, options, true, defaultOption)
    }

    private fun convertMenuOptionsToStringArray(userSelection: UserSelection<MenuOptionsData>?): Array<String> {
        return try {
            userSelection?.items?.map { it.toString() }?.toTypedArray()
                ?: arrayOf("Nenhuma opção disponível")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao converter opções do menu: ${e.message}")
            arrayOf("Erro ao carregar opções")
        }
    }

    private fun handlePaymentError(error: Throwable) {
        var message = error.message ?: "Erro desconhecido"

        // Tratar mensagens específicas
        when {
            message.contains("invalid session", ignoreCase = true) ->
                message = "Sessão inválida - Faça login novamente"
            message.contains("canceled", ignoreCase = true) ->
                message = "Transação cancelada pelo usuário"
            message.contains("timeout", ignoreCase = true) ->
                message = "Tempo esgotado - Tente novamente"
            message.contains("declined", ignoreCase = true) ->
                message = "Transação negada"
        }

        Log.e(TAG, "Falha no pagamento: $message")
        listener?.onMessage("Falha no pagamento: $message")

        currentCallback?.onPaymentError(message)
        uiManager.showSDKMessage("Erro: $message", "ERROR", false)

        delayedExecution(3000) {
            uiManager.dismissAll()
        }
    }

    private fun handlePixError(error: Throwable) {
        var message = error.message ?: "Erro desconhecido no PIX"

        // Tratar mensagens específicas do PIX
        when {
            message.contains("invalid session", ignoreCase = true) ->
                message = "Sessão inválida - Faça login novamente"
            message.contains("canceled", ignoreCase = true) ->
                message = "PIX cancelado pelo usuário"
            message.contains("timeout", ignoreCase = true) ->
                message = "Tempo esgotado para pagamento PIX"
            message.contains("expired", ignoreCase = true) ->
                message = "QR Code PIX expirado"
        }

        Log.e(TAG, "Falha no pagamento PIX: $message")
        listener?.onMessage("Falha no pagamento PIX: $message")

        currentCallback?.onPaymentError(message)
        uiManager.showSDKMessage("Erro PIX: $message", "ERROR", false)

        delayedExecution(3000) {
            uiManager.dismissAll()
        }
    }

    // =================== MÉTODOS DE IMPRESSÃO ===================

    fun printCustomerReceipt() {
        val lastTransaction = getLastTransactionData()
        if (lastTransaction != null) {
            printReceipt(lastTransaction, ReceiptType.CUSTOMER, "via do cliente")
        } else {
            showError("Nenhuma transação disponível para impressão")
        }
    }

    fun printEstablishmentReceipt(transactionData: TransactionData? = null) {
        val transaction = transactionData ?: getLastTransactionData()
        if (transaction != null) {
            printReceipt(transaction, ReceiptType.ESTABLISHMENT, "via do estabelecimento")
        } else {
            showError("Nenhuma transação disponível para impressão")
        }
    }

    private fun printReceipt(transactionData: TransactionData, receiptType: ReceiptType, receiptName: String) {
        try {
            Log.d(TAG, "Iniciando impressão de $receiptName")

            printRequest = SmartPOSPlugin.createPrintRequestBuilder()
                .printData(Printer.PrintData(transactionData = transactionData))
                .receiptType(receiptType)
                .callback(object : Callback<SmartPOSPrinterResponse>() {
                    override fun onStart() {
                        Log.d(TAG, "Iniciando impressão de $receiptName")
                        listener?.onMessage("Iniciando impressão...")
                        uiManager.showSDKMessage("Imprimindo $receiptName...", "PROCESSING", true)
                    }

                    override fun onSuccess(response: SmartPOSPrinterResponse) {
                        Log.d(TAG, "$receiptName impresso com sucesso")
                        listener?.onMessage("$receiptName impresso!")
                        showToast("$receiptName impresso com sucesso!")
                        uiManager.showSDKMessage("Impressão concluída!", "SUCCESS", false)

                        delayedExecution(2000) {
                            uiManager.dismissAll()
                        }
                    }

                    override fun onFail(error: Throwable) {
                        val message = error.message ?: "Erro desconhecido na impressão"

                        val errorMessage = when {
                            message.contains("paper", ignoreCase = true) ->
                                "Papel acabou ou não está inserido corretamente"
                            message.contains("printer", ignoreCase = true) ->
                                "Problema na impressora - verifique se está conectada"
                            message.contains("busy", ignoreCase = true) ->
                                "Impressora ocupada - tente novamente em alguns instantes"
                            message.contains("offline", ignoreCase = true) ->
                                "Impressora offline - verifique a conexão"
                            else -> "Erro na impressão: $message"
                        }

                        Log.e(TAG, "Erro na impressão de $receiptName: $message")
                        listener?.onMessage("Erro na impressão: $message")
                        showToast(errorMessage)
                        uiManager.showSDKMessage("Erro na impressão", "ERROR", false)
                    }

                    override fun onComplete() {
                        Log.d(TAG, "Processo de impressão de $receiptName finalizado")
                    }
                })
                .build()

            Zoop.post(printRequest!!)

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao iniciar impressão de $receiptName: ${e.message}", e)
            showError("Erro ao iniciar impressão: ${e.message}")
        }
    }

    // =================== IMPLEMENTAÇÃO DOS CALLBACKS DA UI ===================

    override fun onOptionSelected(optionIndex: Int, optionValue: String) {
        Log.d(TAG, "Opção selecionada: $optionIndex - $optionValue")

        try {
            // Se for parcelamento, atualizar o resumo
            if (optionValue.contains("x")) {
                val installments = optionValue.split("x")[0].trim().toIntOrNull() ?: 1
                currentSummary?.installments = installments
            }

            uiManager.showSDKMessage("Processando opção selecionada...", "PROCESSING", false)

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao processar opção selecionada", e)
        }
    }

    override fun onInputProvided(input: String) {
        Log.d(TAG, "Input fornecido: $input")

        try {
            uiManager.showSDKMessage("Processando informações...", "PROCESSING", false)

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao processar input", e)
        }
    }

    override fun onCancelPressed() {
        Log.d(TAG, "Cancelamento solicitado pelo usuário")

        try {
            cancelCurrentTransaction()
            currentCallback?.onPaymentCanceled()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao cancelar", e)
        }
    }

    override fun onConfirmPressed() {
        Log.d(TAG, "Confirmação da operação")
        startPaymentTransaction()
    }

    override fun onPrintReceiptSelected(shouldPrint: Boolean) {
        Log.d(TAG, "Seleção de impressão: $shouldPrint")

        if (shouldPrint) {
            printCustomerReceipt()
        } else {
            finalizarTransacao()
        }
    }

    override fun onReprintSelected(receiptType: String) {
        Log.d(TAG, "Tipo de reimpressão selecionado: $receiptType")

        when (receiptType) {
            "CUSTOMER" -> printCustomerReceipt()
            "ESTABLISHMENT" -> printEstablishmentReceipt()
            else -> showError("Tipo de via inválido")
        }
    }

    // =================== MÉTODOS AUXILIARES ===================

    private fun mapearTipoTransacao(transactionType: String?): String {
        return when (transactionType?.lowercase()) {
            "debit", "debito", "débito" -> DEBIT
            "credit", "credito", "crédito" -> CREDIT
            "pix" -> PIX
            "voucher" -> VOUCHER
            "instant_payment", "pagamento_instantaneo" -> INSTANT_PAYMENT
            else -> CREDIT
        }
    }

    private fun cancelCurrentTransaction() {
        try {
            paymentRequest?.cancel()
            pixRequest?.cancel()
            printRequest?.cancel()

            uiManager.dismissAll()
            Log.d(TAG, "Transação cancelada")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao cancelar transação: ${e.message}", e)
        }
    }

    private fun finalizarTransacao() {
        Log.d(TAG, "Finalizando transação")
        uiManager.dismissAll()
    }

    private fun showError(error: String) {
        currentCallback?.onPaymentError(error)
        showToast(error)
    }

    private fun showToast(message: String) {
        context.runOnUiThread {
            FancyToast.makeText(context, message, FancyToast.LENGTH_LONG, FancyToast.INFO, true).show()
        }
    }

    private fun delayedExecution(delayMs: Long, action: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed(action, delayMs)
    }

    // =================== GERENCIAMENTO DE TRANSAÇÕES ===================

    private fun saveTransactionData(transactionData: TransactionData) {
        try {
            // Salvar usando o EnhancedTransactionManagerV2
            val additionalData = mapOf(
                "flavor" to "quickpay",
                "amount" to currentAmount,
                "transactionType" to currentTransactionType
            )

            val saved = transactionManager.saveGenericTransactionData(
                transactionData,
                "ZOOP",
                additionalData
            )

            if (saved) {
                Log.d(TAG, "Dados da transação salvos com sucesso")
            } else {
                Log.w(TAG, "Falha ao salvar dados da transação")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar dados da transação", e)
        }
    }

    private fun getLastTransactionData(): TransactionData? {
        try {
            // Buscar última transação do Zoop
            val transactions = transactionManager.getTransactionsByAcquirer("ZOOP")
            if (transactions.isNotEmpty()) {
                val lastTransaction = transactions[0]
                // TODO: Implementar conversão de TransactionModel para TransactionData
                // Por enquanto retornar null até implementar conversão
                Log.w(TAG, "Conversão de TransactionModel para TransactionData não implementada")
                return null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter última transação", e)
        }
        return null
    }

    // =================== MÉTODOS PÚBLICOS PARA USO EXTERNO ===================

    /**
     * Verificar se terminal está pronto
     */
    fun isTerminalReady(): Boolean {
        return isZoopInitialized
    }

    /**
     * Obter status do terminal
     */
    fun getTerminalStatus(): String {
        return if (isZoopInitialized) {
            "✅ Terminal QuickPay pronto para usar"
        } else {
            "❌ Terminal QuickPay não inicializado"
        }
    }

    /**
     * Mostrar dialog de reimpressão
     */
    fun showReprintDialog() {
        try {
            val transactionInfo = transactionManager.getLastTransactionSummary()
            if (transactionInfo != null) {
                uiManager.showReprintDialog(transactionInfo)
            } else {
                showError("Nenhuma transação disponível para reimpressão")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao mostrar dialog de reimpressão", e)
            showError("Erro ao acessar histórico de transações")
        }
    }

    /**
     * Obter informações da última transação
     */
    fun getLastTransactionInfo(): String? {
        return try {
            transactionManager.getLastTransactionSummary()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter informações da última transação", e)
            null
        }
    }

    /**
     * Limpar dados de transações
     */
    fun clearTransactionData() {
        try {
            transactionManager.cleanOldData(0) // Limpar todos os dados
            Log.d(TAG, "Dados de transações limpos")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao limpar dados de transações", e)
        }
    }

    /**
     * Verificar se uma transação pode ser cancelada
     */
    fun canCancelTransaction(transactionId: String): Boolean {
        return try {
            transactionManager.canCancelTransaction(transactionId)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar se transação pode ser cancelada", e)
            false
        }
    }

    /**
     * Cancelar uma transação específica
     */
    fun cancelSpecificTransaction(transactionId: String, reason: String = ""): Boolean {
        return try {
            transactionManager.cancelTransaction(transactionId, reason)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao cancelar transação específica", e)
            false
        }
    }

    /**
     * Obter estatísticas das transações
     */
    fun getTransactionStats(): Map<String, Any> {
        return try {
            transactionManager.getTransactionStats()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter estatísticas", e)
            emptyMap()
        }
    }

    /**
     * Verificar validação da configuração de pagamento
     */
    private fun validarConfigPagamento(config: PaymentConfig?): Boolean {
        if (config == null) {
            Log.e(TAG, "PaymentConfig é null")
            return false
        }

        if (config.amountInCents <= 0) {
            Log.e(TAG, "Valor inválido: ${config.amountInCents}")
            return false
        }

        if (config.orderId.isNullOrEmpty()) {
            Log.e(TAG, "OrderId é obrigatório")
            return false
        }

        return true
    }

    /**
     * Criar referência do pagamento
     */
    private fun criarReferenciaPagamento(config: PaymentConfig): String {
        return "${BerpModel.getNmTpvend()}: ${BerpModel.getNumMesa()}-${config.orderId} G: ${BerpModel.getFuncionario()}"
    }

    /**
     * Verificar se há transações salvas
     */
    fun hasTransactions(): Boolean {
        return try {
            transactionManager.hasTransactions()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar se há transações", e)
            false
        }
    }

    /**
     * Obter histórico de transações
     */
    fun getTransactionHistory(): List<Any> {
        return try {
            transactionManager.getAllTransactions()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter histórico de transações", e)
            emptyList()
        }
    }

    /**
     * Buscar transação por ID
     */
    fun findTransactionById(transactionId: String): Any? {
        return try {
            transactionManager.findTransactionById(transactionId)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar transação por ID", e)
            null
        }
    }

    /**
     * Obter transações por período
     */
    fun getTransactionsByPeriod(startDate: java.util.Date, endDate: java.util.Date): List<Any> {
        return try {
            transactionManager.getTransactionsByPeriod(startDate, endDate)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter transações por período", e)
            emptyList()
        }
    }

    /**
     * Obter transações dos últimos N dias
     */
    fun getRecentTransactions(days: Int): List<Any> {
        return try {
            transactionManager.getRecentTransactions(days)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter transações recentes", e)
            emptyList()
        }
    }

    /**
     * Exportar dados para backup
     */
    fun exportTransactions(): String {
        return try {
            transactionManager.exportTransactions()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao exportar transações", e)
            "Erro ao exportar dados"
        }
    }

    /**
     * Sincronizar dados
     */
    fun syncData(): Boolean {
        return try {
            transactionManager.syncData()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao sincronizar dados", e)
            false
        }
    }

    // =================== MÉTODOS DE CALLBACK PARA COMPATIBILIDADE ===================

    /**
     * Método de entrada principal para usar externamente
     */
    fun startPayment(amount: Double, paymentType: String, callback: PaymentCallback) {
        processPayment(amount, paymentType, callback)
    }

    /**
     * Método de entrada para PIX
     */
    fun startPixPayment(amount: Double, callback: PaymentCallback) {
        processPixPayment(amount, callback)
    }

    /**
     * Cancelar transação atual
     */
    fun cancelTransaction() {
        cancelCurrentTransaction()
    }

    /**
     * Finalizar transação atual
     */
    fun finishTransaction() {
        finalizarTransacao()
    }

    /**
     * Mostrar tela de PIN
     */
    fun showPinInput() {
        uiManager.createViewToDisplayPasswordInput()
    }

    /**
     * Dismiss todas as telas
     */
    fun dismissAll() {
        uiManager.dismissAll()
    }

    /**
     * Verificar credenciais Zoop
     */
    fun checkZoopKey() {
        if (!ensureZoopInitialized()) {
            return
        }

        try {
            SmartPOSPlugin.createZoopKeyValidationRequestBuilder()
                .callback(object : Callback<SmartPOSZoopKeyValidationResponse>() {
                    override fun onFail(error: Throwable) {
                        Log.e(TAG, "Erro ao validar chave Zoop: ${error.message}")
                    }

                    override fun onSuccess(response: SmartPOSZoopKeyValidationResponse) {
                        if (response.hasKey) {
                            Log.d(TAG, "Possui chave Zoop válida")
                        } else {
                            Log.d(TAG, "Chave Zoop ausente: necessário configurar")
                        }
                    }
                })
                .build().run { Zoop.post(this) }

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar chave Zoop: ${e.message}", e)
        }
    }

    /**
     * Finalizar SDK Zoop
     */
    fun terminateZoop() {
        try {
            pluginManager?.terminate()
            isZoopInitialized = false
            Log.d(TAG, "SDK Zoop finalizado")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao finalizar SDK Zoop: ${e.message}", e)
        }
    }
}