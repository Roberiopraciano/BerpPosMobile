package br.com.berpsistemas.BerpPOSMobile.database

import android.content.Context
import android.util.Log
import br.com.berpsistemas.BerpPOSMobile.model.TransactionModel
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

/**
 * Gerenciador integrado de transações VERSÃO 2.0 - MODELO UNIVERSAL
 * ADAPTADO PARA OS CAMPOS ESSENCIAIS TEF/PIX
 *
 * COMPATIBILIDADE TOTAL:
 * ✅ Usa TransactionModel universal com campos essenciais
 * ✅ Compatível com Stone, Zoop, Cielo, iFood, PagSeguro
 * ✅ Busca inteligente por múltiplos IDs
 * ✅ Suporte completo para TEF, PIX e estornos
 * ✅ API e banco unificados
 */
class EnhancedTransactionManagerV2
    (private val context: Context) {

    companion object {
        private const val TAG = "EnhancedTransactionManagerV2"
        private const val DB_VERSION = 2
    }

    private val databaseHelper = TransactionDatabaseHelperV2(context)
    private val gson = Gson()

    // =================== OPERAÇÕES PRINCIPAIS ===================

    /**
     * Salva transação usando o modelo universal
     */
    fun saveTransaction(transaction: TransactionModel): Boolean {
        return try {
            // Validar dados essenciais
            if (!transaction.isValid()) {
                Log.w(TAG, "Transação inválida: ${transaction.id} - dados insuficientes")
                return false
            }

            val validTransaction = if (transaction.id.isEmpty()) {
                transaction.copy(id = generateTransactionId(transaction.acquirer))
            } else transaction

            val dbResult = databaseHelper.saveTransaction(validTransaction)

            Log.d(TAG, "Transação salva - ID: ${validTransaction.id}, " +
                    "Acquirer: ${validTransaction.acquirer}, " +
                    "Type: ${validTransaction.paymentTypeCode}, " +
                    "Amount: ${validTransaction.amount}")

            dbResult > 0

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar transação: ${e.message}", e)
            false
        }
    }

    /**
     * Salva transação a partir de callback universal
     */
    fun saveUniversalCallback(
        acquirer: String,
        callbackData: Map<String, Any>
    ): Boolean {
        return try {
            val transaction = TransactionModel.fromUniversalCallback(acquirer, callbackData)
            saveTransaction(transaction)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar callback $acquirer: ${e.message}", e)
            false
        }
    }

    // =================== OPERAÇÕES DE CONSULTA UNIVERSAL ===================

    /**
     * Obtém todas as transações
     */
    fun getAllTransactions(): List<TransactionModel> {
        return try {
            databaseHelper.getAllTransactions()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter todas as transações", e)
            emptyList()
        }
    }

    /**
     * Obtém transações ativas (aprovadas e não canceladas)
     */
    fun getActiveTransactions(): List<TransactionModel> {
        return getAllTransactions().filter { it.isApproved() && !it.isCancelled }
    }

    /**
     * Obtém última transação
     */
    fun getLastTransaction(): TransactionModel? {
        return getAllTransactions()
            .sortedByDescending { it.transactionDate }
            .firstOrNull()
    }

    /**
     * BUSCA INTELIGENTE: Por qualquer ID da transação
     */
    fun findTransactionById(searchId: String): TransactionModel? {
        if (searchId.isEmpty()) return null

        return getAllTransactions().find { transaction ->
            transaction.getSearchableIds().any {
                it.equals(searchId, ignoreCase = true)
            }
        }
    }

    /**
     * BUSCA ESPECÍFICA: Por transaction_id
     */
    fun getTransactionById(transactionId: String): TransactionModel? {
        return getAllTransactions().find {
            it.transactionId.equals(transactionId, ignoreCase = true)
        }
    }

    /**
     * BUSCA ESPECÍFICA: Por NSU/Autorização
     */
    fun getTransactionByNSU(nsu: String): TransactionModel? {
        return getAllTransactions().find {
            it.nsu.equals(nsu, ignoreCase = true)
        }
    }

    /**
     * BUSCA ESPECÍFICA: Por order_id (Stone, Zoop, etc.)
     */
    fun getTransactionByOrderId(orderId: String): TransactionModel? {
        return getAllTransactions().find {
            it.orderId.equals(orderId, ignoreCase = true)
        }
    }

    /**
     * BUSCA ESPECÍFICA: Por platform_id (ATK Stone, cieloCode Zoop, etc.)
     */
    fun getTransactionByPlatformId(platformId: String): TransactionModel? {
        return getAllTransactions().find {
            it.platformId.equals(platformId, ignoreCase = true)
        }
    }

    /**
     * BUSCA ESPECÍFICA: Por PIX End-to-End ID
     */
    fun getTransactionByPixEndToEndId(endToEndId: String): TransactionModel? {
        return getAllTransactions().find {
            it.pixEndToEndId.equals(endToEndId, ignoreCase = true)
        }
    }

    /**
     * BUSCA ESPECÍFICA: Por número do comprovante
     */
    fun getTransactionByReceiptNumber(receiptNumber: String): TransactionModel? {
        return getAllTransactions().find {
            it.receiptNumber.equals(receiptNumber, ignoreCase = true)
        }
    }

    // =================== MÉTODOS DE COMPATIBILIDADE STONE ===================

    /**
     * COMPATIBILIDADE STONE: Busca por Payment ID (alias para orderId)
     */
    fun getTransactionByPaymentId(paymentId: String): TransactionModel? {
        return getTransactionByOrderId(paymentId)
    }

    /**
     * COMPATIBILIDADE STONE: Busca por External ID (alias para platformId)
     */
    fun getTransactionByExternalId(externalId: String): TransactionModel? {
        return getTransactionByPlatformId(externalId)
    }

    // =================== FILTROS E BUSCAS AVANÇADAS ===================

    /**
     * Busca por adquirente
     */
    fun getTransactionsByAcquirer(acquirer: String): List<TransactionModel> {
        return getAllTransactions().filter {
            it.acquirer.equals(acquirer, ignoreCase = true)
        }
    }

    /**
     * Busca por tipo de pagamento
     */
    fun getTransactionsByPaymentType(paymentTypeCode: String): List<TransactionModel> {
        return getAllTransactions().filter {
            it.paymentTypeCode.equals(paymentTypeCode, ignoreCase = true)
        }
    }

    /**
     * Busca transações TEF (cartão)
     */
    fun getTEFTransactions(): List<TransactionModel> {
        return getAllTransactions().filter { it.isTEF() }
    }

    /**
     * Busca transações PIX
     */
    fun getPIXTransactions(): List<TransactionModel> {
        return getAllTransactions().filter { it.isPIX() }
    }

    /**
     * Busca transações de estorno
     */
    fun getRefundTransactions(): List<TransactionModel> {
        return getAllTransactions().filter { it.isRefund() }
    }

    /**
     * Busca por bandeira do cartão
     */
    fun getTransactionsByCardBrand(cardBrand: String): List<TransactionModel> {
        return getAllTransactions().filter {
            it.cardBrand.equals(cardBrand, ignoreCase = true)
        }
    }

    /**
     * Busca por valor mínimo
     */
    fun getTransactionsByMinAmount(minAmount: Double): List<TransactionModel> {
        return getAllTransactions().filter { it.amount >= minAmount }
    }

    /**
     * Busca por período
     */
    fun getTransactionsByPeriod(startDate: Date, endDate: Date): List<TransactionModel> {
        return getAllTransactions().filter { transaction ->
            val txDate = transaction.transactionDate
            txDate.after(startDate) && txDate.before(endDate)
        }
    }

    /**
     * Busca dos últimos N dias
     */
    fun getRecentTransactions(days: Int): List<TransactionModel> {
        val endDate = Date()
        val startDate = Calendar.getInstance().apply {
            time = endDate
            add(Calendar.DAY_OF_MONTH, -days)
        }.time

        return getTransactionsByPeriod(startDate, endDate)
    }

    /**
     * Busca por cliente
     */
    fun getTransactionsByCustomer(customerDocument: String): List<TransactionModel> {
        return getAllTransactions().filter {
            it.customerDocument.equals(customerDocument, ignoreCase = true)
        }
    }

    /**
     * Busca por terminal
     */
    fun getTransactionsByTerminal(terminalId: String): List<TransactionModel> {
        return getAllTransactions().filter {
            it.terminalId.equals(terminalId, ignoreCase = true)
        }
    }

    /**
     * Busca textual (busca em múltiplos campos)
     */
    fun searchTransactions(searchTerm: String): List<TransactionModel> {
        if (searchTerm.isEmpty()) return emptyList()

        return getAllTransactions().filter { transaction ->
            transaction.matchesSearch(searchTerm)
        }
    }

    // =================== OPERAÇÕES DE CONTROLE ===================

    /**
     * Cancela uma transação
     */
    fun cancelTransaction(transactionId: String, reason: String = ""): Boolean {
        return try {
            val transaction = findTransactionById(transactionId)
            if (transaction == null) {
                Log.w(TAG, "Transação não encontrada para cancelamento: $transactionId")
                return false
            }

            if (!transaction.canBeCancelled()) {
                Log.w(TAG, "Transação não pode ser cancelada: $transactionId (Status: ${transaction.status})")
                return false
            }

            val cancelledTransaction = transaction.copy(
                isCancelled = true,
                cancellationId = "CANCEL_${System.currentTimeMillis()}",
                status = "CANCELLED",
                errorReason = reason.ifEmpty { "Cancelada pelo usuário" },
                cancelledAt = Date(),
                updatedAt = Date()
            )

            val result = databaseHelper.updateTransaction(cancelledTransaction)
            if (result) {
                Log.d(TAG, "Transação cancelada: $transactionId")
            }

            result
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao cancelar transação: ${e.message}", e)
            false
        }
    }

    /**
     * Cria e salva transação de estorno
     */
    fun saveRefundTransaction(
        originalTransactionId: String,
        refundAmount: Double,
        reason: String,
        acquirer: String,
        transactionId: String,
        nsu: String,
        orderId: String,
        platformId: String,
    ): Boolean {
        return try {
            val originalTransaction = findTransactionById(originalTransactionId)
            if (originalTransaction == null) {
                Log.w(TAG, "Transação original não encontrada: $originalTransactionId")
                return false
            }

            val refundTransaction = TransactionModel.createRefund(
                originalTransaction = originalTransaction,
                refundAmount = refundAmount,
                reason = reason,
                transactionId,
                nsu
            )

            val result = saveTransaction(refundTransaction)
            if (result) {
                Log.d(TAG, "Estorno criado: ${refundTransaction.id} para transação $originalTransactionId")
            }

            result
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao criar estorno: ${e.message}", e)
            false
        }
    }

    /**
     * Marca comprovante como impresso
     */
    fun markReceiptPrinted(transactionId: String): Boolean {
        return try {
            val transaction = findTransactionById(transactionId)
            if (transaction == null) {
                Log.w(TAG, "Transação não encontrada para marcação de impressão: $transactionId")
                return false
            }

            val updatedTransaction = transaction.copy(
                receiptPrinted = true,
                reprintCount = transaction.reprintCount + 1,
                lastPrintDate = Date(),
                updatedAt = Date()
            )

            val result = databaseHelper.updateTransaction(updatedTransaction)
            if (result) {
                Log.d(TAG, "Comprovante marcado como impresso: $transactionId")
            }

            result
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao marcar impressão: ${e.message}", e)
            false
        }
    }

    // =================== VALIDAÇÕES ===================

    /**
     * Verifica se pode cancelar
     */
    fun canCancelTransaction(transactionId: String): Boolean {
        return findTransactionById(transactionId)?.canBeCancelled() ?: false
    }

    /**
     * Verifica se pode reimprimir
     */
    fun canReprintReceipt(transactionId: String): Boolean {
        return findTransactionById(transactionId)?.canReprint() ?: false
    }

    /**
     * Verifica se transação existe
     */
    fun transactionExists(transactionId: String): Boolean {
        return findTransactionById(transactionId) != null
    }

    // =================== ESTATÍSTICAS ===================

    /**
     * Estatísticas gerais
     */
    fun getTransactionStats(): Map<String, Any> {
        val transactions = getAllTransactions()
        val activeTransactions = transactions.filter { it.isApproved() && !it.isCancelled }

        return mapOf(
            "total" to transactions.size,
            "active" to activeTransactions.size,
            "cancelled" to transactions.count { it.isCancelled },
            "totalAmount" to activeTransactions.sumOf { it.amount },
            "avgAmount" to if (activeTransactions.isNotEmpty())
                activeTransactions.sumOf { it.amount } / activeTransactions.size else 0.0,
            "byAcquirer" to transactions.groupingBy { it.acquirer }.eachCount(),
            "byPaymentType" to transactions.groupingBy { it.paymentTypeCode }.eachCount(),
            "byStatus" to transactions.groupingBy { it.status }.eachCount()
        )
    }

    /**
     * Estatísticas por adquirente
     */
    fun getStatsByAcquirer(acquirer: String): Map<String, Any> {
        val transactions = getTransactionsByAcquirer(acquirer)
        val activeTransactions = transactions.filter { it.isApproved() && !it.isCancelled }

        return mapOf(
            "acquirer" to acquirer,
            "total" to transactions.size,
            "active" to activeTransactions.size,
            "totalAmount" to activeTransactions.sumOf { it.amount },
            "byPaymentType" to transactions.groupingBy { it.paymentTypeCode }.eachCount(),
            "byCardBrand" to transactions.filter { it.isTEF() }.groupingBy { it.cardBrand }.eachCount()
        )
    }

    /**
     * Estatísticas por período
     */
    fun getStatsByPeriod(startDate: Date, endDate: Date): Map<String, Any> {
        val transactions = getTransactionsByPeriod(startDate, endDate)
        val activeTransactions = transactions.filter { it.isApproved() && !it.isCancelled }

        return mapOf(
            "period" to "${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(startDate)} - ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(endDate)}",
            "total" to transactions.size,
            "active" to activeTransactions.size,
            "totalAmount" to activeTransactions.sumOf { it.amount },
            "byAcquirer" to transactions.groupingBy { it.acquirer }.eachCount(),
            "byPaymentType" to transactions.groupingBy { it.paymentTypeCode }.eachCount()
        )
    }

    // =================== UTILITÁRIOS ===================

    /**
     * Gera ID único da transação
     */
    private fun generateTransactionId(acquirer: String): String {
        return "${acquirer.uppercase()}_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }

    /**
     * Exporta transações para JSON
     */
    fun exportTransactions(): String {
        return try {
            val exportData = mapOf(
                "exportDate" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                "version" to DB_VERSION,
                "modelVersion" to 2,
                "statistics" to getTransactionStats(),
                "transactions" to getAllTransactions()
            )
            gson.toJson(exportData)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao exportar: ${e.message}", e)
            "{\"error\": \"Erro ao exportar dados\"}"
        }
    }

    /**
     * Limpa dados antigos
     */
    fun cleanOldData(daysToKeep: Int = 30): Int {
        return try {
            val cutoffDate = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, -daysToKeep)
            }.time

            val deletedCount = databaseHelper.deleteOldTransactions(cutoffDate)
            Log.d(TAG, "Limpeza concluída: $deletedCount registros removidos")
            deletedCount
        } catch (e: Exception) {
            Log.e(TAG, "Erro na limpeza: ${e.message}", e)
            0
        }
    }

    /**
     * Verifica integridade dos dados
     */
    fun validateDataIntegrity(): Map<String, Any> {
        val transactions = getAllTransactions()

        val duplicateTransactionIds = transactions
            .groupBy { it.transactionId }
            .filterValues { it.size > 1 }
            .keys

        val duplicateNSUs = transactions
            .filter { it.nsu.isNotEmpty() }
            .groupBy { it.nsu }
            .filterValues { it.size > 1 }
            .keys

        val invalidTransactions = transactions.filter { !it.isValid() }

        return mapOf(
            "totalTransactions" to transactions.size,
            "duplicateTransactionIds" to duplicateTransactionIds,
            "duplicateNSUs" to duplicateNSUs,
            "invalidTransactions" to invalidTransactions.map { it.id },
            "isValid" to (duplicateTransactionIds.isEmpty() && duplicateNSUs.isEmpty() && invalidTransactions.isEmpty())
        )
    }

    // =================== MÉTODOS DE COMPATIBILIDADE LEGACY ===================

    /**
     * COMPATIBILIDADE: Salva transação usando formato legacy
     */
    fun savePaymentFromLegacyCallback(
        brand: String,
        authCode: String,
        mask: String,
        cieloCode: String,
        terminal: String,
        adquirente: String,
        idPlataforma: String,
        idPlataformaResumido: String,
        codMaqPagamento: String,
        flavor: String,
        transactionId: String,
        paymentId: String,
        binCartao: String,
        debCre: String,
        amount: Double
    ): Boolean {
        val callbackData = mapOf(
            "transactionId" to transactionId,
            "nsu" to authCode,
            "orderId" to paymentId,
            "platformId" to idPlataforma,
            "amount" to amount,
            "paymentTypeCode" to debCre,
            "cardBrand" to brand,
            "maskedPan" to mask,
            "cardBin" to binCartao,
            "terminalId" to terminal,
            "flavorSpecific" to mapOf(
                "cieloCode" to cieloCode,
                "idPlataformaResumido" to idPlataformaResumido,
                "codMaqPagamento" to codMaqPagamento,
                "flavor" to flavor
            )
        )

        return saveUniversalCallback(adquirente, callbackData)
    }
}