package br.com.berpsistemas.BerpPOSMobile.model

import android.os.Parcelable
import com.google.gson.Gson
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Modelo de Transação Universal - CAMPOS ESSENCIAIS TEF/PIX
 * Compatível com: Stone, Zoop, Cielo, iFood, PagSeguro, Mercado Pago, etc.
 *
 * FOCO: Apenas campos universais presentes em TODAS as transações TEF/PIX
 * OBJETIVO: Unificar transaction, pagamento, banco e API
 */
@Parcelize
data class TransactionModel(

    // =================== IDENTIFICADORES UNIVERSAIS (OBRIGATÓRIOS) ===================

    /** ID único interno da transação no app */
    @SerializedName("id")
    val id: String = "",

    /** transaction_id: ID da transação na plataforma (ITK Stone, transactionId Zoop, TID Cielo) */
    @SerializedName("transaction_id")
    val transactionId: String = "",

    /** nsu: Número Sequencial Único / Código de Autorização */
    @SerializedName("nsu")
    val nsu: String = "",

    /** order_id: ID do pedido/ordem (order_id Stone, paymentId Zoop, merchantOrderId Cielo) */
    @SerializedName("order_id")
    val orderId: String = "",

    /** platform_id: ID específico da plataforma (ATK Stone, cieloCode Zoop, AID Cielo) */
    @SerializedName("platform_id")
    val platformId: String = "",

    // =================== DADOS FINANCEIROS (OBRIGATÓRIOS) ===================

    /** amount: Valor da transação em reais (formato decimal) */
    @SerializedName("amount")
    val amount: Double = 0.0,

    /** amount_cents: Valor em centavos para compatibilidade com APIs */
    @SerializedName("amount_cents")
    val amountCents: Long = 0L,

    /** installments: Número de parcelas (1 = à vista) */
    @SerializedName("installments")
    val installments: Int = 1,

    /** payment_type_code: Modalidade - CRE, DEB, PIX, VOU, REF */
    @SerializedName("payment_type_code")
    val paymentTypeCode: String = "",

    // =================== DADOS DO CARTÃO TEF (OPCIONAIS) ===================

    /** card_brand: Bandeira do cartão (VISA, MASTERCARD, ELO, etc.) */
    @SerializedName("card_brand")
    val cardBrand: String = "",

    /** masked_pan: Número mascarado do cartão (****1234) */
    @SerializedName("masked_pan")
    val maskedPan: String = "",

    /** card_bin: BIN do cartão (primeiros 6 dígitos) */
    @SerializedName("card_bin")
    val cardBin: String = "",

    /** cardholder_name: Nome do portador (quando disponível) */
    @SerializedName("cardholder_name")
    val cardholderName: String = "",

    // =================== IDENTIFICAÇÃO DO SISTEMA (OBRIGATÓRIOS) ===================

    /** acquirer: Adquirente/Flavor (STONE, ZOOP, CIELO, IFOOD, etc.) */
    @SerializedName("acquirer")
    val acquirer: String = "",

    /** terminal_id: ID do terminal/equipamento */
    @SerializedName("terminal_id")
    val terminalId: String = "",

    /** merchant_id: ID do estabelecimento/comerciante */
    @SerializedName("merchant_id")
    val merchantId: String = "",

    // =================== STATUS E CONTROLE (OBRIGATÓRIOS) ===================

    /** status: Status da transação (APPROVED, DENIED, CANCELLED, PENDING) */
    @SerializedName("status")
    val status: String = "PENDING",

    /** status_code: Código de status (0=aprovado, outros=erro) */
    @SerializedName("status_code")
    val statusCode: String = "",

    /** error_reason: Motivo de erro/negação */
    @SerializedName("error_reason")
    val errorReason: String = "",

    /** is_cancelled: Transação cancelada */
    @SerializedName("is_cancelled")
    val isCancelled: Boolean = false,

    /** cancellation_id: ID do cancelamento */
    @SerializedName("cancellation_id")
    val cancellationId: String = "",

    /** cancelled_at: Data/hora do cancelamento */
    @SerializedName("cancelled_at")
    val cancelledAt: Date? = null,

    // =================== DADOS TEMPORAIS (OBRIGATÓRIOS) ===================

    /** transaction_date: Data/hora da transação */
    @SerializedName("transaction_date")
    val transactionDate: Date = Date(),

    /** created_at: Data da criação no sistema */
    @SerializedName("created_at")
    val createdAt: Date = Date(),

    /** updated_at: Data da última atualização */
    @SerializedName("updated_at")
    val updatedAt: Date = Date(),

    // =================== CONTROLE DE COMPROVANTE (IMPORTANTES) ===================

    /** receipt_number: Número do comprovante fiscal */
    @SerializedName("receipt_number")
    val receiptNumber: String = "",

    /** receipt_printed: Comprovante foi impresso */
    @SerializedName("receipt_printed")
    val receiptPrinted: Boolean = false,

    /** reprint_count: Número de reimpressões */
    @SerializedName("reprint_count")
    val reprintCount: Int = 0,

    /** last_print_date: Data da última impressão */
    @SerializedName("last_print_date")
    val lastPrintDate: Date? = null,

    // =================== PIX ESPECÍFICO (QUANDO APLICÁVEL) ===================

    /** pix_key: Chave PIX utilizada */
    @SerializedName("pix_key")
    val pixKey: String = "",

    /** pix_end_to_end_id: ID fim-a-fim PIX */
    @SerializedName("pix_end_to_end_id")
    val pixEndToEndId: String = "",

    /** pix_qr_code: QR Code PIX */
    @SerializedName("pix_qr_code")
    val pixQrCode: String = "",

    // =================== CLIENTE (OPCIONAIS) ===================

    /** customer_document: CPF/CNPJ do cliente */
    @SerializedName("customer_document")
    val customerDocument: String = "",

    /** customer_name: Nome do cliente */
    @SerializedName("customer_name")
    val customerName: String = "",

    /** customer_email: Email do cliente */
    @SerializedName("customer_email")
    val customerEmail: String = "",

    // =================== METADADOS ESPECÍFICOS ===================

    /** flavor_specific_data: Dados específicos do flavor em JSON */
    @SerializedName("flavor_specific_data")
    val flavorSpecificData: String = "",

    /** notes: Observações adicionais */
    @SerializedName("notes")
    val notes: String = "",

    /** model_version: Versão do modelo para migração */
    @SerializedName("model_version")
    val modelVersion: Int = 2

) : Parcelable {

    // =================== CAMPOS COMPUTADOS ===================

    /** payment_type: Tipo por extenso baseado no código */
    val paymentType: String
        get() = when (paymentTypeCode.uppercase()) {
            "CRE" -> "CREDIT"
            "DEB" -> "DEBIT"
            "PIX" -> "PIX"
            "VOU" -> "VOUCHER"
            "REF" -> "REFUND"
            else -> "UNKNOWN"
        }

    /** authorization_code: Alias para NSU (compatibilidade) */
    val authorizationCode: String
        get() = nsu

    /** external_id: Alias para platformId (compatibilidade) */
    val externalId: String
        get() = platformId

    /** payment_id: Alias para orderId (compatibilidade) */
    val paymentId: String
        get() = orderId

    // =================== MÉTODOS DE VALIDAÇÃO ===================

    // Adicionar estes métodos na classe TransactionModel

    /**
     * Verifica se a transação foi aprovada
     */
    fun isApproved(): Boolean {
        return status.equals("APPROVED", ignoreCase = true) && !isCancelled
    }

    /**
     * Verifica se a transação foi negada/rejeitada
     */
    fun isDenied(): Boolean {
        return status.equals("DENIED", ignoreCase = true) ||
                status.equals("REJECTED", ignoreCase = true) ||
                status.equals("DECLINED", ignoreCase = true)
    }

    /**
     * Verifica se a transação falhou por erro
     */
    fun isFailed(): Boolean {
        return status.equals("FAILED", ignoreCase = true) ||
                status.equals("ERROR", ignoreCase = true) ||
                status.equals("TIMEOUT", ignoreCase = true)
    }

    /**
     * Verifica se a transação está sendo processada
     */
    fun isProcessing(): Boolean {
        return status.equals("PROCESSING", ignoreCase = true) ||
                status.equals("IN_PROGRESS", ignoreCase = true)
    }

    /**
     * Verifica se a transação está pendente
     */
    fun isPending(): Boolean {
        return status.equals("PENDING", ignoreCase = true) ||
                status.equals("WAITING", ignoreCase = true) ||
                status.isEmpty()
    }

    /**
     * Verifica se é transação TEF (cartão)
     */
    fun isTEF(): Boolean {
        return paymentTypeCode.uppercase() in listOf("CRE", "DEB", "VOU") &&
                cardBrand.isNotEmpty()
    }

    /**
     * Verifica se é transação PIX
     */
    fun isPIX(): Boolean {
        return paymentTypeCode.equals("PIX", ignoreCase = true) ||
                pixEndToEndId.isNotEmpty()
    }

    /**
     * Verifica se é estorno
     */
    fun isRefund(): Boolean {
        return paymentTypeCode.equals("REF", ignoreCase = true) ||
                paymentTypeCode.equals("REFUND", ignoreCase = true)
    }

    /**
     * Verifica se pode ser cancelada
     */
    fun canBeCancelled(): Boolean {
        return isApproved() && !isCancelled && !isRefund()
    }

    /**
     * Verifica se pode reimprimir comprovante
     */
    fun canReprint(): Boolean {
        return isApproved() && !isCancelled
    }

    /**
     * Verifica se a transação é válida (tem dados essenciais)
     */
    fun isValid(): Boolean {
        return transactionId.isNotEmpty() &&
                acquirer.isNotEmpty() &&
                paymentTypeCode.isNotEmpty() &&
                amount > 0
    }

    /**
     * Verifica se tem dados mínimos obrigatórios
     */


    // =================== MÉTODOS DE FORMATAÇÃO ===================

    /**
     * Valor formatado em moeda brasileira
     */
    fun getFormattedAmount(): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        return formatter.format(amount)
    }

    // Adicione estas funções no TransactionModel.kt
    fun updateFromPaymentSuccess(
        transactionId: String,
        nsu: String,
        platformId: String,
        cardBrand: String,
        maskedPan: String,
        cardBin: String
    ): TransactionModel {
        return copy(
            transactionId = transactionId,
            nsu = nsu,
            platformId = platformId,
            cardBrand = cardBrand,
            maskedPan = maskedPan,
            cardBin = cardBin,
            status = "APPROVED",
            updatedAt = Date()
        )
    }

    fun updateFromPaymentError(errorReason: String): TransactionModel {
        return copy(
            status = "DENIED",
            errorReason = errorReason,
            updatedAt = Date()
        )
    }

    fun updateFromPaymentCancelled(): TransactionModel {
        return copy(
            status = "CANCELLED",
            errorReason = "Cancelado pelo usuário",
            updatedAt = Date()
        )
    }

    /**
     * Data formatada
     */
    fun getFormattedDate(): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return formatter.format(transactionDate)
    }

    /**
     * Tipo de pagamento formatado
     */
    fun getFormattedPaymentType(): String = when (paymentTypeCode.uppercase()) {
        "CRE" -> "Crédito"
        "DEB" -> "Débito"
        "PIX" -> "PIX"
        "VOU" -> "Voucher"
        "REF" -> "Estorno"
        else -> "Desconhecido"
    }

    /**
     * Resumo completo da transação
     */
    fun getSummary(): String = buildString {
        append("Transação: ${transactionId.take(8)}...\n")
        append("NSU: $nsu\n")
        append("Valor: ${getFormattedAmount()}")
        if (installments > 1) append(" (${installments}x)")
        append("\nTipo: ${getFormattedPaymentType()}")
        if (isTEF()) append("\nBandeira: $cardBrand")
        if (isPIX()) append("\nPIX: ${pixKey.ifEmpty { "QR Code" }}")
        append("\nData: ${getFormattedDate()}")
        append("\nStatus: $status")
        if (isCancelled) append(" (CANCELADA)")
        append("\nAdquirente: $acquirer")
    }

    /**
     * Resumo simplificado para listagens
     */
    fun getShortSummary(): String = buildString {
        append("${getFormattedPaymentType()} - ${getFormattedAmount()}")
        if (isTEF()) append(" ($cardBrand)")
        append(" - $acquirer")
        if (isCancelled) append(" [CANCELADA]")
    }

    // =================== MÉTODOS DE BUSCA ===================

    /**
     * Obtém qualquer ID válido para busca
     */
    fun getSearchableIds(): List<String> = listOfNotNull(
        transactionId.takeIf { it.isNotEmpty() },
        nsu.takeIf { it.isNotEmpty() },
        orderId.takeIf { it.isNotEmpty() },
        platformId.takeIf { it.isNotEmpty() },
        receiptNumber.takeIf { it.isNotEmpty() },
        pixEndToEndId.takeIf { it.isNotEmpty() }
    )

    /**
     * Verifica se contém o termo de busca
     */
    fun matchesSearch(searchTerm: String): Boolean {
        val term = searchTerm.lowercase()
        return getSearchableIds().any { it.lowercase().contains(term) } ||
                cardBrand.lowercase().contains(term) ||
                customerName.lowercase().contains(term) ||
                getFormattedAmount().contains(term)
    }



    // =================== BUILDERS UNIVERSAIS ===================

    companion object {

        /**
         * Cria TransactionModel a partir de callback universal
         * MAPEAMENTO UNIVERSAL para todos os flavors
         */
        fun fromUniversalCallback(
            acquirer: String,
            callbackData: Map<String, Any>
        ): TransactionModel {

            return TransactionModel(
                id = "${acquirer.uppercase()}_${System.currentTimeMillis()}",

                // IDs universais com fallbacks
                transactionId = extractField(callbackData,
                    "transactionId", "transaction_id", "tid", "atk",""),

                nsu = extractField(callbackData,
                    "nsu", "authorization_code", "authCode", "auth_code"),

                orderId = extractField(callbackData,
                    "orderId", "order_id", "paymentId", "payment_id", "merchantOrderId"),

                platformId = extractField(callbackData,
                    "platformId", "platform_id", "atk", "cieloCode", "aid", "idPlataforma"),

                // Dados financeiros
                amount = extractAmount(callbackData),
                amountCents = (extractAmount(callbackData) * 100).toLong(),
                installments = extractInt(callbackData, "installments", "installment_count") ?: 1,
                paymentTypeCode = extractPaymentTypeCode(callbackData),

                // Cartão TEF
                cardBrand = extractField(callbackData, "brand", "card_brand", "bandeira"),
                maskedPan = extractField(callbackData, "mask", "masked_pan", "pan", "maskedCardNumber"),
                cardBin = extractField(callbackData, "cardBin", "card_bin", "binCartao"),
                cardholderName = extractField(callbackData, "cardholderName", "cardholder_name"),

                // Sistema
                acquirer = acquirer.uppercase(),
                terminalId = extractField(callbackData, "terminal", "terminal_id", "terminalId"),
                merchantId = extractField(callbackData, "merchant_id", "merchantId", "estabelecimento"),

                // Status
                status = extractField(callbackData, "status") ?: "APPROVED",
                statusCode = extractField(callbackData, "statusCode", "status_code", "code") ?: "0",
                errorReason = extractField(callbackData, "errorReason", "error_reason", "message"),

                // PIX
                pixKey = extractField(callbackData, "pixKey", "pix_key", "chave_pix"),
                pixEndToEndId = extractField(callbackData, "pixEndToEndId", "pix_end_to_end_id", "endToEndId"),
                pixQrCode = extractField(callbackData, "pixQrCode", "pix_qr_code", "qr_code"),

                // Cliente
                customerDocument = extractField(callbackData, "customerDocument", "customer_document", "cpf", "cnpj"),
                customerName = extractField(callbackData, "customerName", "customer_name"),
                customerEmail = extractField(callbackData, "customerEmail", "customer_email"),

                // Metadados
                flavorSpecificData = Gson().toJson(callbackData),
                modelVersion = 2
            )
        }

        /**
         * Cria transação vazia
         */
        fun createEmpty(acquirer: String): TransactionModel = TransactionModel(
            id = "${acquirer.uppercase()}_${System.currentTimeMillis()}",
            acquirer = acquirer.uppercase(),
            status = "PENDING",
            modelVersion = 2
        )

        /**
         * Cria transação de estorno
         */
        fun createRefund(
            originalTransaction: TransactionModel,
            refundAmount: Double = originalTransaction.amount,
            reason: String = "Estorno"
        ): TransactionModel = TransactionModel(
            id = "REF_${System.currentTimeMillis()}",
            transactionId = "REFUND_${originalTransaction.transactionId}",
            nsu = "REF_${originalTransaction.nsu}",
            orderId = "REF_${originalTransaction.orderId}",
            platformId = "REF_${originalTransaction.platformId}",
            amount = refundAmount,
            amountCents = (refundAmount * 100).toLong(),
            installments = originalTransaction.installments,
            paymentTypeCode = "REF",
            cardBrand = originalTransaction.cardBrand,
            maskedPan = originalTransaction.maskedPan,
            cardBin = originalTransaction.cardBin,
            cardholderName = originalTransaction.cardholderName,
            acquirer = originalTransaction.acquirer,
            terminalId = originalTransaction.terminalId,
            merchantId = originalTransaction.merchantId,
            status = "APPROVED",
            statusCode = "0",
            notes = "Estorno da transação: ${originalTransaction.transactionId}. Motivo: $reason",
            flavorSpecificData = Gson().toJson(mapOf(
                "originalTransactionId" to originalTransaction.transactionId,
                "refundReason" to reason,
                "refundType" to "FULL"
            )),
            modelVersion = 2
        )

        // =================== MÉTODOS AUXILIARES DE EXTRAÇÃO ===================

        private fun extractField(data: Map<String, Any>, vararg keys: String): String {
            for (key in keys) {
                val value = data[key] as? String
                if (!value.isNullOrEmpty()) return value
            }
            return ""
        }

        private fun extractInt(data: Map<String, Any>, vararg keys: String): Int? {
            for (key in keys) {
                when (val value = data[key]) {
                    is Int -> return value
                    is String -> value.toIntOrNull()?.let { return it }
                }
            }
            return null
        }

        private fun extractAmount(data: Map<String, Any>): Double {
            val amountKeys = arrayOf("amount", "value", "valor")
            for (key in amountKeys) {
                when (val value = data[key]) {
                    is Double -> return value
                    is Int -> return if (value > 1000) value / 100.0 else value.toDouble()
                    is String -> value.toDoubleOrNull()?.let { return it }
                }
            }
            return 0.0
        }

        private fun extractPaymentTypeCode(data: Map<String, Any>): String {
            val typeCode = extractField(data, "paymentTypeCode", "debCre", "payment_type_code")
            if (typeCode.isNotEmpty()) return typeCode.uppercase()

            val type = extractField(data, "type", "paymentType", "payment_type").lowercase()
            return when {
                type.contains("credit") || type.contains("crédito") -> "CRE"
                type.contains("debit") || type.contains("débito") -> "DEB"
                type.contains("pix") -> "PIX"
                type.contains("voucher") -> "VOU"
                type.contains("refund") || type.contains("estorno") -> "REF"
                else -> ""
            }
        }
    }
}