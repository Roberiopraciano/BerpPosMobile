package br.com.berpsistemas.BerpPOSMobile.util

import br.com.berpsistemas.BerpPOSMobile.model.PagamentoModel
import br.com.berpsistemas.BerpPOSMobile.model.TransactionModel
import android.util.Log
import com.google.gson.Gson
import java.util.*

/**
 * Utilitário para conversão entre modelos de transação - VERSÃO UNIVERSAL
 * ADAPTADO PARA CAMPOS ESSENCIAIS TEF/PIX
 *
 * CARACTERÍSTICAS:
 * ✅ Conversões usando campos universais
 * ✅ Compatibilidade com PagamentoModel legacy
 * ✅ Extração inteligente de dados específicos
 * ✅ Validação universal de transações
 * ✅ Utilitários de busca e filtro otimizados
 */
object TransactionConverter {

    private const val TAG = "TransactionConverter"
    private val gson = Gson()

    // =================== CONVERSÕES MODELO UNIVERSAL ===================

    /**
     * Converte TransactionModel universal para PagamentoModel (compatibilidade legacy)
     */
    fun toPaymentModel(transaction: TransactionModel): PagamentoModel {
        return PagamentoModel().apply {
            // Campos universais mapeados
            bandeira = transaction.cardBrand
            autorizacao = transaction.nsu  // NSU universal
            cartao = transaction.maskedPan

            terminal = transaction.terminalId
            idOrder = transaction.orderId   // Order ID universal
            transactionId = transaction.transactionId
            idPlataforma = transaction.platformId  // Platform ID universal
            binCartao = transaction.cardBin
            tipoCartaoDebCre = transaction.paymentTypeCode
            pgpVlrpag = transaction.amount

            // Campos específicos baseados no adquirente
            when (transaction.acquirer.uppercase()) {
                "STONE" -> mapStoneFields(this, transaction)
                "ZOOP" -> mapZoopFields(this, transaction)
                "IFOOD" -> mapIFoodFields(this, transaction)
                "CIELO" -> mapCieloFields(this, transaction)
                else -> mapGenericFields(this, transaction)
            }

            Log.d(TAG, "Convertido TransactionModel universal para PagamentoModel: ${transaction.id}")
        }
    }

    /**
     * Converte PagamentoModel para TransactionModel universal
     */
    fun fromPaymentModel(payment: PagamentoModel, acquirer: String = "UNKNOWN"): TransactionModel {
        return TransactionModel(
            id = "CONV_${System.currentTimeMillis()}",

            // Identificadores universais
            transactionId = payment.transactionId ?: "",
            nsu = payment.autorizacao ?: "",
            orderId = payment.idOrder ?: "",
            platformId = payment.idPlataforma ?: "",

            // Dados financeiros
            amount = payment.pgpVlrpag,
            amountCents = (payment.pgpVlrpag * 100).toLong(),
            paymentTypeCode = payment.tipoCartaoDebCre ?: "",

            // Dados do cartão TEF
            cardBrand = payment.bandeira ?: "",
            maskedPan = payment.cartao ?: "",
            cardBin = payment.binCartao ?: "",

            // Sistema
            acquirer = acquirer.uppercase(),
            terminalId = payment.terminal ?: "",

            // Status
            status = "APPROVED",
            statusCode = "0",

            // Dados específicos em JSON
            flavorSpecificData = createFlavorSpecificData(payment, acquirer),
            modelVersion = 2
        ).also {
            Log.d(TAG, "Convertido PagamentoModel para TransactionModel universal: ${it.id}")
        }
    }

    /**
     * Cria TransactionModel genérico a partir de callback universal
     */
    fun createUniversalTransactionFromCallback(
        acquirer: String,
        callbackData: Map<String, Any>
    ): TransactionModel {
        return TransactionModel.fromUniversalCallback(acquirer, callbackData).also {
            Log.d(TAG, "Criado TransactionModel universal para $acquirer: ${it.id}")
        }
    }

    // =================== MAPEAMENTO ESPECÍFICO POR ADQUIRENTE ===================

    /**
     * Mapeia campos específicos do Stone
     */
    private fun mapStoneFields(pagamentoModel: PagamentoModel, transaction: TransactionModel) {
        val stoneData = extractFlavorData(transaction, "stone")

        // Stone usa ITK como transaction_id e ATK como platform_id
        pagamentoModel.apply {
            cvNumber = transaction.transactionId  // ITK
            idPlataforma = transaction.platformId  // ATK
            idOrder = transaction.orderId
            idMovvenda =transaction.orderId.toInt()
            nsu =transaction.nsu
            autorizacao = transaction.authorizationCode          // authorization_code
        }

        Log.d(TAG, "Campos Stone mapeados - ITK: ${transaction.transactionId}, ATK: ${transaction.platformId}")
    }

    /**
     * Mapeia campos específicos do Zoop
     */
    private fun mapZoopFields(pagamentoModel: PagamentoModel, transaction: TransactionModel) {
        val zoopData = extractFlavorData(transaction, "zoop")

        // Zoop usa transactionId e cieloCode como platform_id
        pagamentoModel.apply {
            transactionId = transaction.transactionId
            idPlataforma = transaction.platformId  // cieloCode
            idOrder = transaction.orderId          // paymentId
            autorizacao = transaction.nsu          // authCode
        }

        Log.d(TAG, "Campos Zoop mapeados - TransactionId: ${transaction.transactionId}, CieloCode: ${transaction.platformId}")
    }

    /**
     * Mapeia campos específicos do iFood
     */
    private fun mapIFoodFields(pagamentoModel: PagamentoModel, transaction: TransactionModel) {
        val ifoodData = extractFlavorData(transaction, "ifood")

        pagamentoModel.apply {
            transactionId = transaction.transactionId
            idOrder = transaction.orderId
            autorizacao = transaction.nsu
        }

        Log.d(TAG, "Campos iFood mapeados")
    }

    /**
     * Mapeia campos específicos do Cielo
     */
    private fun mapCieloFields(pagamentoModel: PagamentoModel, transaction: TransactionModel) {
        pagamentoModel.apply {
            transactionId = transaction.transactionId  // TID
            idPlataforma = transaction.platformId       // AID
            idOrder = transaction.orderId              // merchantOrderId
            autorizacao = transaction.nsu              // NSU
        }

        Log.d(TAG, "Campos Cielo mapeados - TID: ${transaction.transactionId}, AID: ${transaction.platformId}")
    }

    /**
     * Mapeia campos genéricos
     */
    private fun mapGenericFields(pagamentoModel: PagamentoModel, transaction: TransactionModel) {
        // Usar mapeamento padrão já aplicado
        Log.d(TAG, "Campos genéricos mapeados para ${transaction.acquirer}")
    }

    // =================== UTILITÁRIOS DE EXTRAÇÃO DE DADOS ===================

    /**
     * Extrai dados específicos do flavor do JSON
     */
    fun extractFlavorData(transaction: TransactionModel, flavor: String): Map<String, Any> {
        return try {
            if (transaction.flavorSpecificData.isNotEmpty()) {
                val dataMap = gson.fromJson(transaction.flavorSpecificData, Map::class.java) as? Map<String, Any>
                val flavorKey = "${flavor.lowercase()}Specific"

                dataMap?.get(flavorKey) as? Map<String, Any> ?: dataMap ?: emptyMap()
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Erro ao extrair dados do flavor $flavor: ${e.message}")
            emptyMap()
        }
    }

    /**
     * Cria dados específicos do flavor em JSON
     */
    private fun createFlavorSpecificData(payment: PagamentoModel, acquirer: String): String {
        val flavorData = when (acquirer.uppercase()) {
            "STONE" -> mapOf(
                "stoneSpecific" to mapOf(
                    "itk" to (payment.transactionId ?: ""),
                    "atk" to (payment.idPlataforma ?: ""),
                    "orderId" to (payment.idOrder ?: "")
                )
            )
            "ZOOP" -> mapOf(
                "zoopSpecific" to mapOf(
                    "cieloCode" to (payment.idPlataforma ?: ""),
                    "paymentId" to (payment.idOrder ?: ""),
                    "productName" to ""
                )
            )
            "IFOOD" -> mapOf(
                "ifoodSpecific" to mapOf(
                    "transactionIdAdyen" to (payment.transactionId ?: ""),
                    "transactionIdAnotaAi" to (payment.idOrder ?: "")
                )
            )
            else -> mapOf(
                "genericData" to mapOf(
                    "originalAcquirer" to acquirer,
                    "conversionTime" to System.currentTimeMillis()
                )
            )
        }

        return gson.toJson(flavorData)
    }

    // =================== BUSCA E FILTRO UNIVERSAIS ===================

    /**
     * Busca inteligente por qualquer ID
     */
    fun findTransactionById(transactions: List<TransactionModel>, searchId: String): TransactionModel? {
        return transactions.find { transaction ->
            transaction.getSearchableIds().any { it.equals(searchId, ignoreCase = true) }
        }
    }

    /**
     * Busca por NSU/Autorização
     */
    fun findTransactionByNSU(transactions: List<TransactionModel>, nsu: String): TransactionModel? {
        return transactions.find { it.nsu.equals(nsu, ignoreCase = true) }
    }

    /**
     * Busca por order ID
     */
    fun findTransactionByOrderId(transactions: List<TransactionModel>, orderId: String): TransactionModel? {
        return transactions.find { it.orderId.equals(orderId, ignoreCase = true) }
    }

    /**
     * Busca por platform ID
     */
    fun findTransactionByPlatformId(transactions: List<TransactionModel>, platformId: String): TransactionModel? {
        return transactions.find { it.platformId.equals(platformId, ignoreCase = true) }
    }

    /**
     * Busca por PIX End-to-End ID
     */
    fun findTransactionByPixEndToEndId(transactions: List<TransactionModel>, endToEndId: String): TransactionModel? {
        return transactions.find { it.pixEndToEndId.equals(endToEndId, ignoreCase = true) }
    }

    /**
     * Filtra transações por adquirente
     */
    fun filterByAcquirer(transactions: List<TransactionModel>, acquirer: String): List<TransactionModel> {
        return transactions.filter { it.acquirer.equals(acquirer, ignoreCase = true) }
    }

    /**
     * Filtra transações por tipo de pagamento
     */
    fun filterByPaymentType(transactions: List<TransactionModel>, paymentTypeCode: String): List<TransactionModel> {
        return transactions.filter { it.paymentTypeCode.equals(paymentTypeCode, ignoreCase = true) }
    }

    /**
     * Filtra transações TEF (cartão)
     */
    fun filterTEFTransactions(transactions: List<TransactionModel>): List<TransactionModel> {
        return transactions.filter { it.isTEF() }
    }

    /**
     * Filtra transações PIX
     */
    fun filterPIXTransactions(transactions: List<TransactionModel>): List<TransactionModel> {
        return transactions.filter { it.isPIX() }
    }

    /**
     * Filtra transações de estorno
     */
    fun filterRefundTransactions(transactions: List<TransactionModel>): List<TransactionModel> {
        return transactions.filter { it.isRefund() }
    }

    /**
     * Filtra transações por período
     */
    fun filterByDateRange(
        transactions: List<TransactionModel>,
        startDate: Date,
        endDate: Date
    ): List<TransactionModel> {
        return transactions.filter {
            it.transactionDate.after(startDate) && it.transactionDate.before(endDate)
        }
    }

    /**
     * Filtra transações por status
     */
    fun filterByStatus(transactions: List<TransactionModel>, status: String): List<TransactionModel> {
        return transactions.filter { it.status.equals(status, ignoreCase = true) }
    }

    /**
     * Filtra transações aprovadas
     */
    fun getApprovedTransactions(transactions: List<TransactionModel>): List<TransactionModel> {
        return transactions.filter { it.isApproved() }
    }

    /**
     * Filtra transações canceladas
     */
    fun getCancelledTransactions(transactions: List<TransactionModel>): List<TransactionModel> {
        return transactions.filter { it.isCancelled }
    }

    /**
     * Filtra transações por bandeira do cartão
     */
    fun filterByCardBrand(transactions: List<TransactionModel>, cardBrand: String): List<TransactionModel> {
        return transactions.filter { it.cardBrand.equals(cardBrand, ignoreCase = true) }
    }

    /**
     * Filtra transações por valor mínimo
     */
    fun filterByMinAmount(transactions: List<TransactionModel>, minAmount: Double): List<TransactionModel> {
        return transactions.filter { it.amount >= minAmount }
    }

    /**
     * Filtra transações por cliente
     */
    fun filterByCustomer(transactions: List<TransactionModel>, customerDocument: String): List<TransactionModel> {
        return transactions.filter { it.customerDocument.equals(customerDocument, ignoreCase = true) }
    }

    /**
     * Filtra transações por terminal
     */
    fun filterByTerminal(transactions: List<TransactionModel>, terminalId: String): List<TransactionModel> {
        return transactions.filter { it.terminalId.equals(terminalId, ignoreCase = true) }
    }

    /**
     * Busca textual em múltiplos campos
     */
    fun searchTransactions(transactions: List<TransactionModel>, searchTerm: String): List<TransactionModel> {
        if (searchTerm.isEmpty()) return emptyList()

        return transactions.filter { transaction ->
            transaction.matchesSearch(searchTerm)
        }
    }

    // =================== VALIDAÇÃO UNIVERSAL ===================

    /**
     * Valida se uma transação tem dados mínimos necessários
     */
    fun isValidTransaction(transaction: TransactionModel): Boolean {
        return transaction.isValid()
    }

    /**
     * Valida transação específica por tipo
     */
    fun validateTransactionByType(transaction: TransactionModel): Map<String, Boolean> {
        val validations = mutableMapOf<String, Boolean>()

        // Validações universais
        validations["has_transaction_id"] = transaction.transactionId.isNotEmpty()
        validations["has_amount"] = transaction.amount > 0
        validations["has_acquirer"] = transaction.acquirer.isNotEmpty()
        validations["has_payment_type"] = transaction.paymentTypeCode.isNotEmpty()

        // Validações específicas por tipo
        when {
            transaction.isTEF() -> {
                validations["has_nsu"] = transaction.nsu.isNotEmpty()
                validations["has_card_brand"] = transaction.cardBrand.isNotEmpty()
                validations["has_masked_pan"] = transaction.maskedPan.isNotEmpty()
            }
            transaction.isPIX() -> {
                validations["has_pix_data"] = transaction.pixEndToEndId.isNotEmpty() ||
                        transaction.pixKey.isNotEmpty() ||
                        transaction.pixQrCode.isNotEmpty()
            }
            transaction.isRefund() -> {
                validations["has_original_reference"] = transaction.notes.contains("transação:")
            }
        }

        return validations
    }

    // =================== SERIALIZAÇÃO ===================

    /**
     * Converte TransactionModel para JSON
     */
    fun toJson(transaction: TransactionModel): String {
        return try {
            gson.toJson(transaction)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao converter para JSON: ${e.message}")
            "{}"
        }
    }

    /**
     * Converte JSON para TransactionModel
     */
    fun fromJson(json: String): TransactionModel? {
        return try {
            gson.fromJson(json, TransactionModel::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao converter de JSON: ${e.message}")
            null
        }
    }

    /**
     * Converte lista para JSON
     */
    fun listToJson(transactions: List<TransactionModel>): String {
        return try {
            gson.toJson(transactions)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao converter lista para JSON: ${e.message}")
            "[]"
        }
    }

    /**
     * Converte JSON para lista
     */
    fun jsonToList(json: String): List<TransactionModel> {
        return try {
            val array = gson.fromJson(json, Array<TransactionModel>::class.java)
            array.toList()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao converter JSON para lista: ${e.message}")
            emptyList()
        }
    }

    // =================== ESTATÍSTICAS E RELATÓRIOS ===================

    /**
     * Cria resumo estatístico universal
     */
    fun createUniversalSummary(transactions: List<TransactionModel>): Map<String, Any> {
        val approved = transactions.filter { it.isApproved() && !it.isCancelled }
        val cancelled = transactions.filter { it.isCancelled }
        val tef = transactions.filter { it.isTEF() }
        val pix = transactions.filter { it.isPIX() }
        val refunds = transactions.filter { it.isRefund() }

        val totalAmount = approved.sumOf { it.amount }
        val averageAmount = if (approved.isNotEmpty()) totalAmount / approved.size else 0.0

        return mapOf(
            // Contadores gerais
            "total_transactions" to transactions.size,
            "approved_transactions" to approved.size,
            "cancelled_transactions" to cancelled.size,
            "pending_transactions" to transactions.count { it.status == "PENDING" },

            // Por tipo de transação
            "tef_transactions" to tef.size,
            "pix_transactions" to pix.size,
            "refund_transactions" to refunds.size,

            // Valores financeiros
            "total_amount" to totalAmount,
            "average_amount" to averageAmount,
            "max_amount" to (approved.maxOfOrNull { it.amount } ?: 0.0),
            "min_amount" to (approved.minOfOrNull { it.amount } ?: 0.0),

            // Agrupamentos
            "by_acquirer" to transactions.groupingBy { it.acquirer }.eachCount(),
            "by_payment_type" to transactions.groupingBy { it.paymentTypeCode }.eachCount(),
            "by_card_brand" to tef.groupingBy { it.cardBrand }.eachCount(),
            "by_status" to transactions.groupingBy { it.status }.eachCount(),

            // Métricas de tempo
            "first_transaction" to transactions.minOfOrNull { it.transactionDate },
            "last_transaction" to transactions.maxOfOrNull { it.transactionDate },

            // Controle de qualidade
            "printed_receipts" to transactions.count { it.receiptPrinted },
            "total_reprints" to transactions.sumOf { it.reprintCount }
        ) as Map<String, Any>
    }

    /**
     * Cria relatório de conversão
     */
    fun createConversionReport(originalCount: Int, convertedCount: Int, errors: List<String>): Map<String, Any> {
        return mapOf(
            "original_count" to originalCount,
            "converted_count" to convertedCount,
            "success_rate" to if (originalCount > 0) (convertedCount.toDouble() / originalCount * 100) else 0.0,
            "errors" to errors,
            "conversion_time" to System.currentTimeMillis()
        )
    }

    // =================== UTILITÁRIOS AUXILIARES ===================

    /**
     * Combina dados de diferentes fontes
     */
    fun mergeTransactionData(
        base: TransactionModel,
        updates: Map<String, Any>
    ): TransactionModel {
        return base.copy(
            nsu = updates["nsu"] as? String ?: base.nsu,
            cardBrand = updates["cardBrand"] as? String ?: base.cardBrand,
            maskedPan = updates["maskedPan"] as? String ?: base.maskedPan,
            status = updates["status"] as? String ?: base.status,
            updatedAt = Date(),
            flavorSpecificData = gson.toJson(updates)
        )
    }

    /**
     * Mapeia código para tipo por extenso
     */
    fun mapPaymentTypeCodeToName(code: String): String {
        return when (code.uppercase()) {
            "CRE" -> "Crédito"
            "DEB" -> "Débito"
            "PIX" -> "PIX"
            "VOU" -> "Voucher"
            "REF" -> "Estorno"
            else -> "Desconhecido"
        }
    }

    /**
     * Valida integridade de uma lista de transações
     */
    fun validateTransactionListIntegrity(transactions: List<TransactionModel>): Map<String, Any> {
        val duplicateTransactionIds = transactions
            .groupBy { it.transactionId }
            .filterValues { it.size > 1 }
            .keys

        val duplicateNSUs = transactions
            .filter { it.nsu.isNotEmpty() }
            .groupBy { it.nsu }
            .filterValues { it.size > 1 }
            .keys

        val invalidTransactions = transactions.filter { !isValidTransaction(it) }

        return mapOf(
            "total_transactions" to transactions.size,
            "valid_transactions" to transactions.count { isValidTransaction(it) },
            "duplicate_transaction_ids" to duplicateTransactionIds,
            "duplicate_nsus" to duplicateNSUs,
            "invalid_transactions" to invalidTransactions.map { it.id },
            "is_list_valid" to (duplicateTransactionIds.isEmpty() && duplicateNSUs.isEmpty() && invalidTransactions.isEmpty())
        )
    }
}