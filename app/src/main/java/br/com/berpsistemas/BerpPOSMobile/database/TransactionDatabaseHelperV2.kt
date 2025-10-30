package br.com.berpsistemas.BerpPOSMobile.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import br.com.berpsistemas.BerpPOSMobile.model.TransactionModel
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

/**
 * DatabaseHelper V2 - MODELO UNIVERSAL TEF/PIX
 * ADAPTADO PARA OS CAMPOS ESSENCIAIS UNIVERSAIS
 *
 * CARACTERÍSTICAS:
 * ✅ Tabela unificada com campos universais TEF/PIX
 * ✅ Schema otimizado para todos os adquirentes
 * ✅ Índices inteligentes para busca rápida
 * ✅ Suporte completo a migração de dados
 * ✅ Campos específicos em JSON para flexibilidade
 */
class TransactionDatabaseHelperV2(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {

    companion object {
        private const val TAG = "TransactionDatabaseV2"
        private const val DATABASE_NAME = "universal_transactions.db"
        private const val DATABASE_VERSION = 2

        // Nome da tabela unificada
        private const val TABLE_TRANSACTIONS = "universal_transactions"

        // =================== IDENTIFICADORES UNIVERSAIS ===================
        private const val COLUMN_ID = "id"
        private const val COLUMN_TRANSACTION_ID = "transaction_id"    // ITK Stone, transactionId Zoop, TID Cielo
        private const val COLUMN_NSU = "nsu"                         // authorization_code Stone, authCode Zoop, NSU Cielo
        private const val COLUMN_ORDER_ID = "order_id"               // order_id Stone, paymentId Zoop, merchantOrderId Cielo
        private const val COLUMN_PLATFORM_ID = "platform_id"         // ATK Stone, cieloCode Zoop, AID Cielo

        // =================== DADOS FINANCEIROS ===================
        private const val COLUMN_AMOUNT = "amount"                   // Valor em reais (decimal)
        private const val COLUMN_AMOUNT_CENTS = "amount_cents"       // Valor em centavos (compatibilidade)
        private const val COLUMN_INSTALLMENTS = "installments"       // Número de parcelas
        private const val COLUMN_PAYMENT_TYPE_CODE = "payment_type_code" // CRE, DEB, PIX, VOU, REF

        // =================== DADOS DO CARTÃO TEF ===================
        private const val COLUMN_CARD_BRAND = "card_brand"           // VISA, MASTERCARD, ELO
        private const val COLUMN_MASKED_PAN = "masked_pan"           // ****1234
        private const val COLUMN_CARD_BIN = "card_bin"               // Primeiros 6 dígitos
        private const val COLUMN_CARDHOLDER_NAME = "cardholder_name" // Nome do portador

        // =================== IDENTIFICAÇÃO DO SISTEMA ===================
        private const val COLUMN_ACQUIRER = "acquirer"               // STONE, ZOOP, CIELO, IFOOD
        private const val COLUMN_TERMINAL_ID = "terminal_id"         // ID do terminal
        private const val COLUMN_MERCHANT_ID = "merchant_id"         // ID do estabelecimento

        // =================== STATUS E CONTROLE ===================
        private const val COLUMN_STATUS = "status"                   // APPROVED, DENIED, CANCELLED, PENDING
        private const val COLUMN_STATUS_CODE = "status_code"         // 0=aprovado, outros=erro
        private const val COLUMN_ERROR_REASON = "error_reason"       // Motivo do erro
        private const val COLUMN_IS_CANCELLED = "is_cancelled"       // Transação cancelada
        private const val COLUMN_CANCELLATION_ID = "cancellation_id" // ID do cancelamento
        private const val COLUMN_CANCELLED_AT = "cancelled_at"       // Data do cancelamento

        // =================== DADOS TEMPORAIS ===================
        private const val COLUMN_TRANSACTION_DATE = "transaction_date" // Data/hora da transação
        private const val COLUMN_CREATED_AT = "created_at"           // Data da criação
        private const val COLUMN_UPDATED_AT = "updated_at"           // Data da atualização

        // =================== CONTROLE DE COMPROVANTE ===================
        private const val COLUMN_RECEIPT_NUMBER = "receipt_number"   // Número do comprovante
        private const val COLUMN_RECEIPT_PRINTED = "receipt_printed" // Comprovante impresso
        private const val COLUMN_REPRINT_COUNT = "reprint_count"     // Número de reimpressões
        private const val COLUMN_LAST_PRINT_DATE = "last_print_date" // Última impressão

        // =================== PIX ESPECÍFICO ===================
        private const val COLUMN_PIX_KEY = "pix_key"                 // Chave PIX
        private const val COLUMN_PIX_END_TO_END_ID = "pix_end_to_end_id" // ID fim-a-fim PIX
        private const val COLUMN_PIX_QR_CODE = "pix_qr_code"         // QR Code PIX

        // =================== CLIENTE ===================
        private const val COLUMN_CUSTOMER_DOCUMENT = "customer_document" // CPF/CNPJ
        private const val COLUMN_CUSTOMER_NAME = "customer_name"     // Nome do cliente
        private const val COLUMN_CUSTOMER_EMAIL = "customer_email"   // Email do cliente

        // =================== METADADOS ===================
        private const val COLUMN_FLAVOR_SPECIFIC_DATA = "flavor_specific_data" // Dados específicos em JSON
        private const val COLUMN_NOTES = "notes"                     // Observações
        private const val COLUMN_MODEL_VERSION = "model_version"     // Versão do modelo
    }

    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_TRANSACTIONS (
                $COLUMN_ID TEXT PRIMARY KEY,
                
                -- Identificadores universais (obrigatórios)
                $COLUMN_TRANSACTION_ID TEXT NOT NULL,
                $COLUMN_NSU TEXT NOT NULL,
                $COLUMN_ORDER_ID TEXT,
                $COLUMN_PLATFORM_ID TEXT,
                
                -- Dados financeiros (obrigatórios)
                $COLUMN_AMOUNT REAL NOT NULL DEFAULT 0,
                $COLUMN_AMOUNT_CENTS INTEGER NOT NULL DEFAULT 0,
                $COLUMN_INSTALLMENTS INTEGER DEFAULT 1,
                $COLUMN_PAYMENT_TYPE_CODE TEXT NOT NULL,
                
                -- Dados do cartão TEF (opcionais)
                $COLUMN_CARD_BRAND TEXT,
                $COLUMN_MASKED_PAN TEXT,
                $COLUMN_CARD_BIN TEXT,
                $COLUMN_CARDHOLDER_NAME TEXT,
                
                -- Identificação do sistema (obrigatórios)
                $COLUMN_ACQUIRER TEXT NOT NULL,
                $COLUMN_TERMINAL_ID TEXT,
                $COLUMN_MERCHANT_ID TEXT,
                
                -- Status e controle (obrigatórios)
                $COLUMN_STATUS TEXT NOT NULL DEFAULT 'PENDING',
                $COLUMN_STATUS_CODE TEXT,
                $COLUMN_ERROR_REASON TEXT,
                $COLUMN_IS_CANCELLED INTEGER DEFAULT 0,
                $COLUMN_CANCELLATION_ID TEXT,
                $COLUMN_CANCELLED_AT TEXT,
                
                -- Dados temporais (obrigatórios)
                $COLUMN_TRANSACTION_DATE TEXT NOT NULL,
                $COLUMN_CREATED_AT TEXT NOT NULL,
                $COLUMN_UPDATED_AT TEXT NOT NULL,
                
                -- Controle de comprovante
                $COLUMN_RECEIPT_NUMBER TEXT,
                $COLUMN_RECEIPT_PRINTED INTEGER DEFAULT 0,
                $COLUMN_REPRINT_COUNT INTEGER DEFAULT 0,
                $COLUMN_LAST_PRINT_DATE TEXT,
                
                -- PIX específico
                $COLUMN_PIX_KEY TEXT,
                $COLUMN_PIX_END_TO_END_ID TEXT,
                $COLUMN_PIX_QR_CODE TEXT,
                
                -- Cliente
                $COLUMN_CUSTOMER_DOCUMENT TEXT,
                $COLUMN_CUSTOMER_NAME TEXT,
                $COLUMN_CUSTOMER_EMAIL TEXT,
                
                -- Metadados
                $COLUMN_FLAVOR_SPECIFIC_DATA TEXT,
                $COLUMN_NOTES TEXT,
                $COLUMN_MODEL_VERSION INTEGER DEFAULT 2
            )
        """.trimIndent()

        db.execSQL(createTableQuery)
        createOptimizedIndexes(db)

        Log.d(TAG, "Tabela universal de transações criada com sucesso")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "Atualizando banco universal de $oldVersion para $newVersion")

        when (oldVersion) {
            1 -> migrateFromV1ToV2(db)
            else -> {
                Log.w(TAG, "Recriando tabela devido a versão desconhecida")
                db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
                onCreate(db)
            }
        }
    }

    // =================== OPERAÇÕES CRUD UNIVERSAIS ===================

    /**
     * Salva transação universal
     */
    fun saveTransaction(transaction: TransactionModel): Long {
        val db = writableDatabase
        return try {
            val values = transactionToContentValues(transaction)

            // 1️⃣ Primeiro, tenta inserir (se não existe)
            val result = db.insertWithOnConflict(
                TABLE_TRANSACTIONS,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE  // ✅ Ignora se já existe
            )

            // 2️⃣ Se inserção falhou (já existe), atualiza
            if (result == -1L) {
                val updated = db.update(
                    TABLE_TRANSACTIONS,
                    values,
                    "$COLUMN_ID = ?",
                    arrayOf(transaction.id)
                )

                if (updated > 0) {
                    Log.d(TAG, "Transação atualizada: ${transaction.id}")
                    return updated.toLong()
                }
            } else {
                Log.d(TAG, "Transação inserida: ${transaction.id}")
            }

            result
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar transação: ${e.message}", e)
            -1
        }
    }

    /**
     * Atualiza transação existente
     */
    fun updateTransaction(transaction: TransactionModel): Boolean {
        val db = writableDatabase
        return try {
            val values = transactionToContentValues(transaction)
            val updated = db.update(
                TABLE_TRANSACTIONS,
                values,
                "$COLUMN_ID = ?",
                arrayOf(transaction.id)
            )

            if (updated > 0) {
                Log.d(TAG, "Transação atualizada: ${transaction.id}")
            }

            updated > 0
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar transação ${transaction.id}: ${e.message}", e)
            false
        }
    }

    /**
     * Busca universal por qualquer ID
     */
    fun getTransactionByAnyId(searchId: String): TransactionModel? {
        val db = readableDatabase
        return try {
            val cursor = db.query(
                TABLE_TRANSACTIONS,
                null,
                "$COLUMN_ID = ? OR $COLUMN_TRANSACTION_ID = ? OR $COLUMN_NSU = ? OR " +
                        "$COLUMN_ORDER_ID = ? OR $COLUMN_PLATFORM_ID = ? OR $COLUMN_PIX_END_TO_END_ID = ? OR " +
                        "$COLUMN_RECEIPT_NUMBER = ?",
                arrayOf(searchId, searchId, searchId, searchId, searchId, searchId, searchId),
                null,
                null,
                null
            )

            cursor.use {
                if (it.moveToFirst()) {
                    cursorToTransaction(it)
                } else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar transação por ID: ${e.message}", e)
            null
        }
    }

    /**
     * Obtém todas as transações
     */
    fun getAllTransactions(): List<TransactionModel> {
        val db = readableDatabase
        val transactions = mutableListOf<TransactionModel>()

        return try {
            val cursor = db.query(
                TABLE_TRANSACTIONS,
                null,
                null,
                null,
                null,
                null,
                "$COLUMN_TRANSACTION_DATE DESC"
            )

            cursor.use {
                while (it.moveToNext()) {
                    val transaction = cursorToTransaction(it)
                    if (transaction != null) {
                        transactions.add(transaction)
                    }
                }
            }

            Log.d(TAG, "Recuperadas ${transactions.size} transações")
            transactions
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter todas as transações: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Busca por adquirente
     */
    fun getTransactionsByAcquirer(acquirer: String): List<TransactionModel> {
        return getTransactionsByQuery(
            "$COLUMN_ACQUIRER = ?",
            arrayOf(acquirer.uppercase())
        )
    }

    /**
     * Busca por tipo de pagamento
     */
    fun getTransactionsByPaymentType(paymentTypeCode: String): List<TransactionModel> {
        return getTransactionsByQuery(
            "$COLUMN_PAYMENT_TYPE_CODE = ?",
            arrayOf(paymentTypeCode.uppercase())
        )
    }

    /**
     * Busca transações TEF (cartão)
     */
    fun getTEFTransactions(): List<TransactionModel> {
        return getTransactionsByQuery(
            "$COLUMN_PAYMENT_TYPE_CODE IN ('CRE', 'DEB', 'VOU') AND $COLUMN_CARD_BRAND IS NOT NULL AND $COLUMN_CARD_BRAND != ''",
            null
        )
    }

    /**
     * Busca transações PIX
     */
    fun getPIXTransactions(): List<TransactionModel> {
        return getTransactionsByQuery(
            "$COLUMN_PAYMENT_TYPE_CODE = 'PIX' OR $COLUMN_PIX_END_TO_END_ID IS NOT NULL",
            null
        )
    }

    /**
     * Busca transações de estorno
     */
    fun getRefundTransactions(): List<TransactionModel> {
        return getTransactionsByQuery(
            "$COLUMN_PAYMENT_TYPE_CODE = 'REF'",
            null
        )
    }

    /**
     * Busca transações ativas (não canceladas)
     */
    fun getActiveTransactions(): List<TransactionModel> {
        return getTransactionsByQuery(
            "$COLUMN_IS_CANCELLED = 0 AND $COLUMN_STATUS = 'APPROVED'",
            null
        )
    }

    /**
     * Busca por período
     */
    fun getTransactionsByDateRange(startDate: String, endDate: String): List<TransactionModel> {
        return getTransactionsByQuery(
            "$COLUMN_TRANSACTION_DATE BETWEEN ? AND ?",
            arrayOf(startDate, endDate)
        )
    }

    /**
     * Busca por bandeira do cartão
     */
    fun getTransactionsByCardBrand(cardBrand: String): List<TransactionModel> {
        return getTransactionsByQuery(
            "$COLUMN_CARD_BRAND = ?",
            arrayOf(cardBrand.uppercase())
        )
    }

    /**
     * Busca por cliente
     */
    fun getTransactionsByCustomer(customerDocument: String): List<TransactionModel> {
        return getTransactionsByQuery(
            "$COLUMN_CUSTOMER_DOCUMENT = ?",
            arrayOf(customerDocument)
        )
    }

    /**
     * Busca por terminal
     */
    fun getTransactionsByTerminal(terminalId: String): List<TransactionModel> {
        return getTransactionsByQuery(
            "$COLUMN_TERMINAL_ID = ?",
            arrayOf(terminalId)
        )
    }

    // =================== OPERAÇÕES DE CONTROLE ===================

    /**
     * Marca comprovante como impresso
     */
    fun markReceiptPrinted(transactionId: String): Boolean {
        val db = writableDatabase
        return try {
            val values = ContentValues().apply {
                put(COLUMN_RECEIPT_PRINTED, 1)
                put(COLUMN_LAST_PRINT_DATE, dateFormat.format(Date()))
                put(COLUMN_UPDATED_AT, dateFormat.format(Date()))
            }

            // Incrementar contador de reimpressão
            db.execSQL(
                "UPDATE $TABLE_TRANSACTIONS SET $COLUMN_REPRINT_COUNT = $COLUMN_REPRINT_COUNT + 1 " +
                        "WHERE $COLUMN_TRANSACTION_ID = ? OR $COLUMN_ID = ?",
                arrayOf(transactionId, transactionId)
            )

            val updated = db.update(
                TABLE_TRANSACTIONS,
                values,
                "$COLUMN_TRANSACTION_ID = ? OR $COLUMN_ID = ?",
                arrayOf(transactionId, transactionId)
            )

            if (updated > 0) {
                Log.d(TAG, "Comprovante marcado como impresso: $transactionId")
            }

            updated > 0
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao marcar comprovante: ${e.message}", e)
            false
        }
    }

    /**
     * Cancela transação
     */
    fun cancelTransaction(transactionId: String, cancellationId: String, reason: String = ""): Boolean {
        val db = writableDatabase
        return try {
            val values = ContentValues().apply {
                put(COLUMN_IS_CANCELLED, 1)
                put(COLUMN_CANCELLATION_ID, cancellationId)
                put(COLUMN_STATUS, "CANCELLED")
                put(COLUMN_CANCELLED_AT, dateFormat.format(Date()))
                put(COLUMN_ERROR_REASON, reason)
                put(COLUMN_UPDATED_AT, dateFormat.format(Date()))
            }

            val updated = db.update(
                TABLE_TRANSACTIONS,
                values,
                "($COLUMN_TRANSACTION_ID = ? OR $COLUMN_ID = ?) AND $COLUMN_IS_CANCELLED = 0",
                arrayOf(transactionId, transactionId)
            )

            if (updated > 0) {
                Log.d(TAG, "Transação cancelada: $transactionId")
            }

            updated > 0
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao cancelar transação: ${e.message}", e)
            false
        }
    }

    /**
     * Deleta transações antigas
     */
    fun deleteOldTransactions(cutoffDate: Date): Int {
        val db = writableDatabase
        return try {
            val dateString = dateFormat.format(cutoffDate)
            val deleted = db.delete(
                TABLE_TRANSACTIONS,
                "$COLUMN_CREATED_AT < ?",
                arrayOf(dateString)
            )
            Log.d(TAG, "Transações antigas deletadas: $deleted")
            deleted
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao deletar transações antigas: ${e.message}", e)
            0
        }
    }

    // =================== MÉTODOS DE CONVERSÃO ===================

    /**
     * Converte TransactionModel para ContentValues
     */
    private fun transactionToContentValues(transaction: TransactionModel): ContentValues {
        return ContentValues().apply {
            put(COLUMN_ID, transaction.id)

            // Identificadores universais
            put(COLUMN_TRANSACTION_ID, transaction.transactionId)
            put(COLUMN_NSU, transaction.nsu)
            put(COLUMN_ORDER_ID, transaction.orderId)
            put(COLUMN_PLATFORM_ID, transaction.platformId)

            // Dados financeiros
            put(COLUMN_AMOUNT, transaction.amount)
            put(COLUMN_AMOUNT_CENTS, transaction.amountCents)
            put(COLUMN_INSTALLMENTS, transaction.installments)
            put(COLUMN_PAYMENT_TYPE_CODE, transaction.paymentTypeCode)

            // Dados do cartão TEF
            put(COLUMN_CARD_BRAND, transaction.cardBrand)
            put(COLUMN_MASKED_PAN, transaction.maskedPan)
            put(COLUMN_CARD_BIN, transaction.cardBin)
            put(COLUMN_CARDHOLDER_NAME, transaction.cardholderName)

            // Identificação do sistema
            put(COLUMN_ACQUIRER, transaction.acquirer)
            put(COLUMN_TERMINAL_ID, transaction.terminalId)
            put(COLUMN_MERCHANT_ID, transaction.merchantId)

            // Status e controle
            put(COLUMN_STATUS, transaction.status)
            put(COLUMN_STATUS_CODE, transaction.statusCode)
            put(COLUMN_ERROR_REASON, transaction.errorReason)
            put(COLUMN_IS_CANCELLED, if (transaction.isCancelled) 1 else 0)
            put(COLUMN_CANCELLATION_ID, transaction.cancellationId)
            put(COLUMN_CANCELLED_AT, transaction.cancelledAt?.let { dateFormat.format(it) })

            // Dados temporais
            put(COLUMN_TRANSACTION_DATE, dateFormat.format(transaction.transactionDate))
            put(COLUMN_CREATED_AT, dateFormat.format(transaction.createdAt))
            put(COLUMN_UPDATED_AT, dateFormat.format(transaction.updatedAt))

            // Controle de comprovante
            put(COLUMN_RECEIPT_NUMBER, transaction.receiptNumber)
            put(COLUMN_RECEIPT_PRINTED, if (transaction.receiptPrinted) 1 else 0)
            put(COLUMN_REPRINT_COUNT, transaction.reprintCount)
            put(COLUMN_LAST_PRINT_DATE, transaction.lastPrintDate?.let { dateFormat.format(it) })

            // PIX específico
            put(COLUMN_PIX_KEY, transaction.pixKey)
            put(COLUMN_PIX_END_TO_END_ID, transaction.pixEndToEndId)
            put(COLUMN_PIX_QR_CODE, transaction.pixQrCode)

            // Cliente
            put(COLUMN_CUSTOMER_DOCUMENT, transaction.customerDocument)
            put(COLUMN_CUSTOMER_NAME, transaction.customerName)
            put(COLUMN_CUSTOMER_EMAIL, transaction.customerEmail)

            // Metadados
            put(COLUMN_FLAVOR_SPECIFIC_DATA, transaction.flavorSpecificData)
            put(COLUMN_NOTES, transaction.notes)
            put(COLUMN_MODEL_VERSION, transaction.modelVersion)
        }
    }

    /**
     * Converte Cursor para TransactionModel
     */
    private fun cursorToTransaction(cursor: Cursor): TransactionModel? {
        return try {
            TransactionModel(
                id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),

                // Identificadores universais
                transactionId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_ID)),
                nsu = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NSU)) ?: "",
                orderId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_ID)) ?: "",
                platformId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLATFORM_ID)) ?: "",

                // Dados financeiros
                amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                amountCents = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT_CENTS)),
                installments = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_INSTALLMENTS)),
                paymentTypeCode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYMENT_TYPE_CODE)) ?: "",

                // Dados do cartão TEF
                cardBrand = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CARD_BRAND)) ?: "",
                maskedPan = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MASKED_PAN)) ?: "",
                cardBin = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CARD_BIN)) ?: "",
                cardholderName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CARDHOLDER_NAME)) ?: "",

                // Identificação do sistema
                acquirer = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACQUIRER)),
                terminalId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TERMINAL_ID)) ?: "",
                merchantId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MERCHANT_ID)) ?: "",

                // Status e controle
                status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)),
                statusCode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS_CODE)) ?: "",
                errorReason = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ERROR_REASON)) ?: "",
                isCancelled = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_CANCELLED)) == 1,
                cancellationId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CANCELLATION_ID)) ?: "",
                cancelledAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CANCELLED_AT))?.let { parseDate(it) },

                // Dados temporais
                transactionDate = parseDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_DATE))),
                createdAt = parseDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT))),
                updatedAt = parseDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT))),

                // Controle de comprovante
                receiptNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECEIPT_NUMBER)) ?: "",
                receiptPrinted = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECEIPT_PRINTED)) == 1,
                reprintCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REPRINT_COUNT)),
                lastPrintDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_PRINT_DATE))?.let { parseDate(it) },

                // PIX específico
                pixKey = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PIX_KEY)) ?: "",
                pixEndToEndId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PIX_END_TO_END_ID)) ?: "",
                pixQrCode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PIX_QR_CODE)) ?: "",

                // Cliente
                customerDocument = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUSTOMER_DOCUMENT)) ?: "",
                customerName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUSTOMER_NAME)) ?: "",
                customerEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUSTOMER_EMAIL)) ?: "",

                // Metadados
                flavorSpecificData = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FLAVOR_SPECIFIC_DATA)) ?: "",
                notes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTES)) ?: "",
                modelVersion = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MODEL_VERSION))
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao converter cursor: ${e.message}", e)
            null
        }
    }

    // =================== MÉTODOS AUXILIARES ===================

    /**
     * Busca genérica por query
     */
    private fun getTransactionsByQuery(whereClause: String, whereArgs: Array<String>?): List<TransactionModel> {
        val db = readableDatabase
        val transactions = mutableListOf<TransactionModel>()

        return try {
            val cursor = db.query(
                TABLE_TRANSACTIONS,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                "$COLUMN_TRANSACTION_DATE DESC"
            )

            cursor.use {
                while (it.moveToNext()) {
                    val transaction = cursorToTransaction(it)
                    if (transaction != null) {
                        transactions.add(transaction)
                    }
                }
            }

            transactions
        } catch (e: Exception) {
            Log.e(TAG, "Erro na busca por query: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Cria índices otimizados para busca rápida
     */
    private fun createOptimizedIndexes(db: SQLiteDatabase) {
        val indexes = listOf(
            // Índices principais para busca de IDs
            "CREATE INDEX IF NOT EXISTS idx_transaction_id ON $TABLE_TRANSACTIONS($COLUMN_TRANSACTION_ID)",
            "CREATE INDEX IF NOT EXISTS idx_nsu ON $TABLE_TRANSACTIONS($COLUMN_NSU)",
            "CREATE INDEX IF NOT EXISTS idx_order_id ON $TABLE_TRANSACTIONS($COLUMN_ORDER_ID)",
            "CREATE INDEX IF NOT EXISTS idx_platform_id ON $TABLE_TRANSACTIONS($COLUMN_PLATFORM_ID)",
            "CREATE INDEX IF NOT EXISTS idx_pix_end_to_end ON $TABLE_TRANSACTIONS($COLUMN_PIX_END_TO_END_ID)",

            // Índices para filtros comuns
            "CREATE INDEX IF NOT EXISTS idx_acquirer ON $TABLE_TRANSACTIONS($COLUMN_ACQUIRER)",
            "CREATE INDEX IF NOT EXISTS idx_payment_type ON $TABLE_TRANSACTIONS($COLUMN_PAYMENT_TYPE_CODE)",
            "CREATE INDEX IF NOT EXISTS idx_status ON $TABLE_TRANSACTIONS($COLUMN_STATUS)",
            "CREATE INDEX IF NOT EXISTS idx_cancelled ON $TABLE_TRANSACTIONS($COLUMN_IS_CANCELLED)",
            "CREATE INDEX IF NOT EXISTS idx_card_brand ON $TABLE_TRANSACTIONS($COLUMN_CARD_BRAND)",

            // Índices para datas
            "CREATE INDEX IF NOT EXISTS idx_transaction_date ON $TABLE_TRANSACTIONS($COLUMN_TRANSACTION_DATE)",
            "CREATE INDEX IF NOT EXISTS idx_created_at ON $TABLE_TRANSACTIONS($COLUMN_CREATED_AT)",

            // Índices compostos para consultas comuns
            "CREATE INDEX IF NOT EXISTS idx_acquirer_status ON $TABLE_TRANSACTIONS($COLUMN_ACQUIRER, $COLUMN_STATUS)",
            "CREATE INDEX IF NOT EXISTS idx_acquirer_date ON $TABLE_TRANSACTIONS($COLUMN_ACQUIRER, $COLUMN_TRANSACTION_DATE)",
            "CREATE INDEX IF NOT EXISTS idx_status_cancelled ON $TABLE_TRANSACTIONS($COLUMN_STATUS, $COLUMN_IS_CANCELLED)"
        )

        indexes.forEach { indexQuery ->
            try {
                db.execSQL(indexQuery)
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao criar índice: ${e.message}")
            }
        }

        Log.d(TAG, "Índices universais criados com sucesso")
    }

    /**
     * Converte string para Date
     */
    private fun parseDate(dateString: String): Date {
        return try {
            dateFormat.parse(dateString) ?: Date()
        } catch (e: Exception) {
            Log.w(TAG, "Erro ao fazer parse da data: $dateString")
            Date()
        }
    }

    /**
     * Migração da V1 para V2
     */
    private fun migrateFromV1ToV2(db: SQLiteDatabase) {
        try {
            Log.d(TAG, "Iniciando migração para modelo universal")

            // Verificar se existe tabela antiga
            val oldTableExists = isTableExists(db, "transactions") ||
                    isTableExists(db, "transaction_records")

            if (oldTableExists) {
                Log.d(TAG, "Tabela antiga encontrada, iniciando migração...")

                // Backup da tabela antiga
                if (isTableExists(db, "transactions")) {
                    db.execSQL("ALTER TABLE transactions RENAME TO transactions_backup_v1")
                }
                if (isTableExists(db, "transaction_records")) {
                    db.execSQL("ALTER TABLE transaction_records RENAME TO transaction_records_backup_v1")
                }

                // Criar nova tabela universal
                onCreate(db)

                // Aqui você pode implementar migração específica se necessário
                // Por exemplo, migrar dados da tabela antiga para nova estrutura
                migrateOldDataToUniversal(db)

                Log.d(TAG, "Migração para modelo universal concluída")
            } else {
                // Criar tabela universal diretamente
                onCreate(db)
                Log.d(TAG, "Tabela universal criada diretamente")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Erro na migração: ${e.message}", e)
            // Em caso de erro, recriar tabela
            db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
            onCreate(db)
        }
    }

    /**
     * Migra dados antigos para estrutura universal
     */
    private fun migrateOldDataToUniversal(db: SQLiteDatabase) {
        try {
            // Implementar migração específica se necessário
            // Exemplo básico de migração de estrutura antiga

            if (isTableExists(db, "transactions_backup_v1")) {
                Log.d(TAG, "Migrando dados da estrutura antiga...")

                val cursor = db.rawQuery("SELECT * FROM transactions_backup_v1", null)
                cursor.use {
                    while (it.moveToNext()) {
                        try {
                            // Mapear campos antigos para novos campos universais
                            val oldId = it.getString(it.getColumnIndex("id") ?: continue)
                            val oldTransactionId = it.getString(it.getColumnIndex("transaction_id") ?: continue)
                            val oldAmount = it.getDouble(it.getColumnIndex("amount") ?: continue)
                            val oldAcquirer = it.getString(it.getColumnIndex("acquirer") ?: continue)

                            // Criar ContentValues para nova estrutura
                            val values = ContentValues().apply {
                                put(COLUMN_ID, oldId)
                                put(COLUMN_TRANSACTION_ID, oldTransactionId)
                                put(COLUMN_NSU, oldTransactionId) // Fallback
                                put(COLUMN_ORDER_ID, oldId)
                                put(COLUMN_AMOUNT, oldAmount)
                                put(COLUMN_AMOUNT_CENTS, (oldAmount * 100).toLong())
                                put(COLUMN_PAYMENT_TYPE_CODE, "CRE") // Default
                                put(COLUMN_ACQUIRER, oldAcquirer)
                                put(COLUMN_STATUS, "APROVADO")
                                put(COLUMN_TRANSACTION_DATE, dateFormat.format(Date()))
                                put(COLUMN_CREATED_AT, dateFormat.format(Date()))
                                put(COLUMN_UPDATED_AT, dateFormat.format(Date()))
                                put(COLUMN_MODEL_VERSION, 2)
                            }

                            db.insert(TABLE_TRANSACTIONS, null, values)
                        } catch (e: Exception) {
                            Log.w(TAG, "Erro ao migrar registro: ${e.message}")
                        }
                    }
                }

                Log.d(TAG, "Migração de dados concluída")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Erro na migração de dados: ${e.message}", e)
        }
    }

    /**
     * Verifica se uma tabela existe
     */
    private fun isTableExists(db: SQLiteDatabase, tableName: String): Boolean {
        val cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
            arrayOf(tableName)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // =================== ESTATÍSTICAS UNIVERSAIS ===================

    /**
     * Estatísticas universais das transações
     */
    fun getUniversalStats(): Map<String, Any> {
        val db = readableDatabase
        return try {
            val stats = mutableMapOf<String, Any>()

            // Total de transações
            var cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_TRANSACTIONS", null)
            cursor.use {
                if (it.moveToFirst()) {
                    stats["total_transactions"] = it.getInt(0)
                }
            }

            // Transações ativas (aprovadas e não canceladas)
            cursor = db.rawQuery(
                "SELECT COUNT(*) FROM $TABLE_TRANSACTIONS WHERE $COLUMN_STATUS = 'APPROVED' AND $COLUMN_IS_CANCELLED = 0",
                null
            )
            cursor.use {
                if (it.moveToFirst()) {
                    stats["active_transactions"] = it.getInt(0)
                }
            }

            // Transações canceladas
            cursor = db.rawQuery(
                "SELECT COUNT(*) FROM $TABLE_TRANSACTIONS WHERE $COLUMN_IS_CANCELLED = 1",
                null
            )
            cursor.use {
                if (it.moveToFirst()) {
                    stats["cancelled_transactions"] = it.getInt(0)
                }
            }

            // Valor total das transações ativas
            cursor = db.rawQuery(
                "SELECT SUM($COLUMN_AMOUNT) FROM $TABLE_TRANSACTIONS WHERE $COLUMN_STATUS = 'APPROVED' AND $COLUMN_IS_CANCELLED = 0",
                null
            )
            cursor.use {
                if (it.moveToFirst()) {
                    stats["total_amount"] = it.getDouble(0)
                }
            }

            // Contagem por tipo de pagamento
            cursor = db.rawQuery(
                "SELECT $COLUMN_PAYMENT_TYPE_CODE, COUNT(*) FROM $TABLE_TRANSACTIONS GROUP BY $COLUMN_PAYMENT_TYPE_CODE",
                null
            )
            cursor.use {
                val paymentTypes = mutableMapOf<String, Int>()
                while (it.moveToNext()) {
                    val type = it.getString(0) ?: "UNKNOWN"
                    val count = it.getInt(1)
                    paymentTypes[type] = count
                }
                stats["by_payment_type"] = paymentTypes
            }

            // Contagem por adquirente
            cursor = db.rawQuery(
                "SELECT $COLUMN_ACQUIRER, COUNT(*) FROM $TABLE_TRANSACTIONS GROUP BY $COLUMN_ACQUIRER",
                null
            )
            cursor.use {
                val acquirers = mutableMapOf<String, Int>()
                while (it.moveToNext()) {
                    val acquirer = it.getString(0) ?: "UNKNOWN"
                    val count = it.getInt(1)
                    acquirers[acquirer] = count
                }
                stats["by_acquirer"] = acquirers
            }

            // Transações TEF
            cursor = db.rawQuery(
                "SELECT COUNT(*) FROM $TABLE_TRANSACTIONS WHERE $COLUMN_PAYMENT_TYPE_CODE IN ('CRE', 'DEB', 'VOU') AND $COLUMN_CARD_BRAND IS NOT NULL AND $COLUMN_CARD_BRAND != ''",
                null
            )
            cursor.use {
                if (it.moveToFirst()) {
                    stats["tef_transactions"] = it.getInt(0)
                }
            }

            // Transações PIX
            cursor = db.rawQuery(
                "SELECT COUNT(*) FROM $TABLE_TRANSACTIONS WHERE $COLUMN_PAYMENT_TYPE_CODE = 'PIX'",
                null
            )
            cursor.use {
                if (it.moveToFirst()) {
                    stats["pix_transactions"] = it.getInt(0)
                }
            }

            // Estornos
            cursor = db.rawQuery(
                "SELECT COUNT(*) FROM $TABLE_TRANSACTIONS WHERE $COLUMN_PAYMENT_TYPE_CODE = 'REF'",
                null
            )
            cursor.use {
                if (it.moveToFirst()) {
                    stats["refund_transactions"] = it.getInt(0)
                }
            }

            stats
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter estatísticas universais: ${e.message}", e)
            emptyMap()
        }
    }

    /**
     * Estatísticas por período
     */
    fun getStatsByPeriod(startDate: String, endDate: String): Map<String, Any> {
        val db = readableDatabase
        return try {
            val stats = mutableMapOf<String, Any>()

            // Total no período
            var cursor = db.rawQuery(
                "SELECT COUNT(*), SUM($COLUMN_AMOUNT) FROM $TABLE_TRANSACTIONS WHERE $COLUMN_TRANSACTION_DATE BETWEEN ? AND ?",
                arrayOf(startDate, endDate)
            )
            cursor.use {
                if (it.moveToFirst()) {
                    stats["total_transactions"] = it.getInt(0)
                    stats["total_amount"] = it.getDouble(1)
                }
            }

            // Por tipo de pagamento no período
            cursor = db.rawQuery(
                "SELECT $COLUMN_PAYMENT_TYPE_CODE, COUNT(*), SUM($COLUMN_AMOUNT) FROM $TABLE_TRANSACTIONS " +
                        "WHERE $COLUMN_TRANSACTION_DATE BETWEEN ? AND ? GROUP BY $COLUMN_PAYMENT_TYPE_CODE",
                arrayOf(startDate, endDate)
            )
            cursor.use {
                val byType = mutableMapOf<String, Map<String, Any>>()
                while (it.moveToNext()) {
                    val type = it.getString(0) ?: "UNKNOWN"
                    val count = it.getInt(1)
                    val amount = it.getDouble(2)
                    byType[type] = mapOf("count" to count, "amount" to amount)
                }
                stats["by_payment_type"] = byType
            }

            // Por adquirente no período
            cursor = db.rawQuery(
                "SELECT $COLUMN_ACQUIRER, COUNT(*), SUM($COLUMN_AMOUNT) FROM $TABLE_TRANSACTIONS " +
                        "WHERE $COLUMN_TRANSACTION_DATE BETWEEN ? AND ? GROUP BY $COLUMN_ACQUIRER",
                arrayOf(startDate, endDate)
            )
            cursor.use {
                val byAcquirer = mutableMapOf<String, Map<String, Any>>()
                while (it.moveToNext()) {
                    val acquirer = it.getString(0) ?: "UNKNOWN"
                    val count = it.getInt(1)
                    val amount = it.getDouble(2)
                    byAcquirer[acquirer] = mapOf("count" to count, "amount" to amount)
                }
                stats["by_acquirer"] = byAcquirer
            }

            stats["period"] = mapOf("start" to startDate, "end" to endDate)
            stats
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter estatísticas por período: ${e.message}", e)
            emptyMap()
        }
    }

    /**
     * Relatório de integridade dos dados
     */
    fun getDataIntegrityReport(): Map<String, Any> {
        val db = readableDatabase
        return try {
            val report = mutableMapOf<String, Any>()

            // IDs duplicados
            var cursor = db.rawQuery(
                "SELECT $COLUMN_TRANSACTION_ID, COUNT(*) FROM $TABLE_TRANSACTIONS GROUP BY $COLUMN_TRANSACTION_ID HAVING COUNT(*) > 1",
                null
            )
            cursor.use {
                val duplicates = mutableListOf<String>()
                while (it.moveToNext()) {
                    duplicates.add(it.getString(0))
                }
                report["duplicate_transaction_ids"] = duplicates
            }

            // NSUs duplicados
            cursor = db.rawQuery(
                "SELECT $COLUMN_NSU, COUNT(*) FROM $TABLE_TRANSACTIONS WHERE $COLUMN_NSU != '' GROUP BY $COLUMN_NSU HAVING COUNT(*) > 1",
                null
            )
            cursor.use {
                val duplicates = mutableListOf<String>()
                while (it.moveToNext()) {
                    duplicates.add(it.getString(0))
                }
                report["duplicate_nsus"] = duplicates
            }

            // Transações inválidas (sem dados essenciais)
            cursor = db.rawQuery(
                "SELECT COUNT(*) FROM $TABLE_TRANSACTIONS WHERE $COLUMN_TRANSACTION_ID = '' OR $COLUMN_AMOUNT <= 0 OR $COLUMN_ACQUIRER = '' OR $COLUMN_PAYMENT_TYPE_CODE = ''",
                null
            )
            cursor.use {
                if (it.moveToFirst()) {
                    report["invalid_transactions"] = it.getInt(0)
                }
            }

            // Transações órfãs (sem dados básicos)
            cursor = db.rawQuery(
                "SELECT COUNT(*) FROM $TABLE_TRANSACTIONS WHERE $COLUMN_NSU = '' AND $COLUMN_ORDER_ID = '' AND $COLUMN_PLATFORM_ID = ''",
                null
            )
            cursor.use {
                if (it.moveToFirst()) {
                    report["orphan_transactions"] = it.getInt(0)
                }
            }

            report["is_valid"] = (report["duplicate_transaction_ids"] as List<*>).isEmpty() &&
                    (report["duplicate_nsus"] as List<*>).isEmpty() &&
                    (report["invalid_transactions"] as Int) == 0

            report
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao gerar relatório de integridade: ${e.message}", e)
            mapOf("error" to "Erro ao gerar relatório")
        }
    }

    /**
     * Otimização do banco de dados
     */
    fun optimizeDatabase(): Boolean {
        val db = writableDatabase
        return try {
            // VACUUM para compactar e otimizar
            db.execSQL("VACUUM")

            // ANALYZE para atualizar estatísticas
            db.execSQL("ANALYZE")

            Log.d(TAG, "Banco de dados otimizado com sucesso")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao otimizar banco: ${e.message}", e)
            false
        }
    }

    /**
     * Limpa todos os dados (usar com cuidado!)
     */
    fun clearAllData(): Boolean {
        val db = writableDatabase
        return try {
            val deleted = db.delete(TABLE_TRANSACTIONS, null, null)
            Log.d(TAG, "Todos os dados limpos: $deleted registros removidos")
            deleted > 0
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao limpar dados: ${e.message}", e)
            false
        }
    }
}