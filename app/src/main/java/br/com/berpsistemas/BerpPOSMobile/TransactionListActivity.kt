package br.com.berpsistemas.BerpPOSMobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.berpsistemas.BerpPOSMobile.database.TransactionDatabaseHelperV2
import br.com.berpsistemas.BerpPOSMobile.model.TransactionModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import br.com.berpsistemas.BerpPOSMobile.operations.GenericPaymentOperations
import br.com.berpsistemas.BerpPOSMobile.ui.PaymentUIManager
import br.com.berpsistemas.BerpPOSMobile.R
import java.text.NumberFormat
import java.util.*

class TransactionListActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "TransactionListActivity"
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private lateinit var databaseHelper: TransactionDatabaseHelperV2
    private lateinit var uiManager: PaymentUIManager
    private lateinit var tvEmptyState: TextView
    private lateinit var tvStats: TextView
    private lateinit var fabRefresh: FloatingActionButton
    private lateinit var spFilter: Spinner

    // NOVO: Sistema genérico de operações
    private lateinit var paymentOperations: GenericPaymentOperations

    private var transactions = mutableListOf<TransactionModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_list)

        initializeComponents()
        setupRecyclerView()
        setupFilters()
        setupPaymentOperations()  // NOVO
        loadTransactions()
        updateStats()
    }

    private fun initializeComponents() {
        databaseHelper = TransactionDatabaseHelperV2(this)
        uiManager = PaymentUIManager(this)

        recyclerView = findViewById(R.id.recyclerViewTransactions)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        tvStats = findViewById(R.id.tvStats)
        fabRefresh = findViewById(R.id.fabRefresh)
        spFilter = findViewById(R.id.spFilter)

        fabRefresh.setOnClickListener {
            loadTransactions()
            updateStats()
        }

        supportActionBar?.apply {
            title = "Histórico de Transações"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    // NOVO: Configurar sistema genérico de operações
    private fun setupPaymentOperations() {
        paymentOperations = GenericPaymentOperations.getInstance()

        // Configurar listener para feedback das operações
        paymentOperations.setOperationListener(object : GenericPaymentOperations.PaymentOperationListener {
            override fun onOperationStarted(message: String) {
                runOnUiThread {
                    uiManager.showSDKMessage(message, "PROCESSING", true)
                }
            }

            override fun onOperationCompleted(message: String) {
                runOnUiThread {
                    uiManager.showSDKMessage(message, "SUCCESS", false)
                    Toast.makeText(this@TransactionListActivity, message, Toast.LENGTH_SHORT).show()

                    // Recarregar dados após operação
                    loadTransactions()
                    updateStats()

                    // Fechar mensagem após 2 segundos
                    Handler(Looper.getMainLooper()).postDelayed({
                        uiManager.dismissAll()
                    }, 2000)
                }
            }

            override fun onOperationError(error: String) {
                runOnUiThread {
                    uiManager.showSDKMessage(error, "ERROR", false)
                    Toast.makeText(this@TransactionListActivity, error, Toast.LENGTH_LONG).show()

                    Handler(Looper.getMainLooper()).postDelayed({
                        uiManager.dismissAll()
                    }, 3000)
                }
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(transactions) { transaction, action ->
            handleTransactionAction(transaction, action)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupFilters() {
        val filterOptions = arrayOf(
            "Todas as Transações",
            "Apenas Ativas",
            "Apenas Canceladas",
            "Último Mês",
            "Última Semana"
        )

        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spFilter.adapter = filterAdapter

        spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyFilter(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadTransactions() {
        try {
            val allTransactions = databaseHelper.getAllTransactions()
            Log.d(TAG, "=== DEBUG: Total de transações no banco: ${allTransactions.size} ===")

            // Log detalhado de cada transação
            allTransactions.forEachIndexed { index, transaction ->
                Log.d(TAG, "Transação $index: ID=${transaction.transactionId}, " +
                        "Acquirer=${transaction.acquirer}, " +
                        "Amount=${transaction.amount}, " +
                        "Status=${transaction.status}")
            }

            transactions.clear()
            transactions.addAll(allTransactions)
            adapter.notifyDataSetChanged()

            updateEmptyState()
            Log.d(TAG, "Lista atualizada com ${transactions.size} transações")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao carregar transações", e)
            Toast.makeText(this, "Erro ao carregar transações", Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyFilter(filterIndex: Int) {
        try {
            val filteredTransactions = when (filterIndex) {
                0 -> databaseHelper.getAllTransactions()
                1 -> databaseHelper.getAllTransactions().filter { it.isApproved() && !it.isCancelled }
                2 -> databaseHelper.getAllTransactions().filter { it.isCancelled }
                3 -> {
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.MONTH, -1)
                    val startDate = calendar.time
                    val endDate = Date()
                    databaseHelper.getAllTransactions().filter {
                        it.transactionDate >= startDate && it.transactionDate <= endDate
                    }
                }
                4 -> {
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.WEEK_OF_YEAR, -1)
                    val startDate = calendar.time
                    val endDate = Date()
                    databaseHelper.getAllTransactions().filter {
                        it.transactionDate >= startDate && it.transactionDate <= endDate
                    }
                }
                else -> databaseHelper.getAllTransactions()
            }

            transactions.clear()
            transactions.addAll(filteredTransactions)
            adapter.notifyDataSetChanged()
            updateEmptyState()

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao aplicar filtro", e)
            Toast.makeText(this, "Erro ao aplicar filtro", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateStats() {
        try {
            val allTransactions = databaseHelper.getAllTransactions()
            val activeTransactions = allTransactions.filter { it.isApproved() && !it.isCancelled }
            val cancelledTransactions = allTransactions.filter { it.isCancelled }
            val totalAmount = activeTransactions.sumOf { it.amount }
            val averageAmount = if (activeTransactions.isNotEmpty()) totalAmount / activeTransactions.size else 0.0

            val numberFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

            tvStats.text = """
                📊 Estatísticas:
                Total: ${allTransactions.size} transações
                Ativas: ${activeTransactions.size}
                Canceladas: ${cancelledTransactions.size}
                Valor Total: ${numberFormat.format(totalAmount)}
                Valor Médio: ${numberFormat.format(averageAmount)}
            """.trimIndent()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar estatísticas", e)
            tvStats.text = "Erro ao carregar estatísticas"
        }
    }

    private fun updateEmptyState() {
        if (transactions.isEmpty()) {
            recyclerView.visibility = View.GONE
            tvEmptyState.visibility = View.VISIBLE
            tvEmptyState.text = """
                📭 Nenhuma transação encontrada
                
                As transações aparecerão aqui após serem processadas.
            """.trimIndent()
        } else {
            recyclerView.visibility = View.VISIBLE
            tvEmptyState.visibility = View.GONE
        }
    }

    private fun handleTransactionAction(transaction: TransactionModel, action: String) {
        when (action) {
            "REPRINT_CUSTOMER" -> reprintReceiptGeneric(transaction, "customer", "Via do Cliente")
            "REPRINT_ESTABLISHMENT" -> reprintReceiptGeneric(transaction, "establishment", "Via do Estabelecimento")
            "CANCEL" -> showCancelConfirmation(transaction)
            "VIEW_DETAILS" -> showTransactionDetails(transaction)
        }
    }

    // NOVO: Reimpressão genérica usando o sistema universal
    private fun reprintReceiptGeneric(transaction: TransactionModel, receiptType: String, receiptName: String) {
        try {
            if (transaction.isCancelled) {
                Toast.makeText(this, "Não é possível reimprimir comprovante de transação cancelada", Toast.LENGTH_SHORT).show()
                return
            }

            Log.d(TAG, "Iniciando reimpressão genérica: ${transaction.acquirer} - $receiptType")

            // Usar o sistema genérico que detecta automaticamente o acquirer
            paymentOperations.reprintReceipt(this, transaction, receiptType)

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao iniciar reimpressão genérica", e)
            Toast.makeText(this, "Erro ao iniciar reimpressão: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCancelConfirmation(transaction: TransactionModel) {
        if (transaction.isCancelled) {
            Toast.makeText(this, "Esta transação já foi cancelada", Toast.LENGTH_SHORT).show()
            return
        }

        val message = """
            Confirmar cancelamento da transação?
            
            ID: ${transaction.transactionId}
            Acquirer: ${transaction.acquirer}
            Valor: ${transaction.getFormattedAmount()}
            Cartão: ${transaction.maskedPan}
            Data: ${transaction.getFormattedDate()}
            
            ⚠️ Esta ação não pode ser desfeita.
            O cancelamento será processado via ${transaction.acquirer}.
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Cancelar Transação")
            .setMessage(message)
            .setPositiveButton("Confirmar") { _, _ ->
                processCancellationGeneric(transaction)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // NOVO: Cancelamento genérico usando o sistema universal
    private fun processCancellationGeneric(transaction: TransactionModel) {
        try {
            Log.d(TAG, "Iniciando cancelamento genérico: ${transaction.acquirer}")

            val reason = "Cancelamento solicitado pelo usuário"

            // Usar o sistema genérico que detecta automaticamente o acquirer
            paymentOperations.cancelTransaction(this, transaction, reason)

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao processar cancelamento genérico", e)
            Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showTransactionDetails(transaction: TransactionModel) {
        val details = """
            🆔 ID da Transação: ${transaction.transactionId}
            🏪 Acquirer: ${transaction.acquirer}
            💰 Valor: ${transaction.getFormattedAmount()}
            💳 Cartão: ${transaction.maskedPan}
            🏪 Bandeira: ${transaction.cardBrand}
            📝 Tipo: ${transaction.getFormattedPaymentType()}
            📊 Status: ${transaction.status}
            📅 Data: ${transaction.getFormattedDate()}
            🖨️ Comprovante Impresso: ${if (transaction.receiptPrinted) "Sim (${transaction.reprintCount}x)" else "Não"}
            
            ${if (transaction.isCancelled) "❌ Cancelado em: ${transaction.updatedAt}\n🆔 ID Cancelamento: ${transaction.cancellationId}" else ""}
            
       
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Detalhes da Transação")
            .setMessage(details)
            .setPositiveButton("Fechar", null)
            .setNeutralButton("Exportar") { _, _ ->
                exportTransactionDetails(transaction)
            }
            .show()
    }

    // NOVO: Exportar detalhes da transação
    private fun exportTransactionDetails(transaction: TransactionModel) {
        try {
            val details = transaction.getSummary()

//            val shareIntent = Intent().apply {
//                Intent.setAction = Intent.ACTION_SEND
//                Intent.setType = "text/plain"
//                putExtra(Intent.EXTRA_TEXT, details)
//                putExtra(Intent.EXTRA_SUBJECT, "Detalhes da Transação ${transaction.transactionId}")
//            }

      //      startActivity(Intent.createChooser(shareIntent, "Compartilhar detalhes da transação"))

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao exportar detalhes", e)
            Toast.makeText(this, "Erro ao exportar detalhes", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        uiManager.dismissAll()
    }

    // NOVO: Método para processar callbacks de retorno
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleCallbackIntent(intent)
    }

    private fun handleCallbackIntent(intent: Intent?) {
        if (intent?.data != null) {
            val uri = intent.data
            Log.d(TAG, "Callback recebido: $uri")

            // Processar callback de reimpressão ou cancelamento
            when {
                uri.toString().contains("reprint") -> handleReprintCallback(uri)
                uri.toString().contains("cancel") -> handleCancelCallback(uri)
                else -> Log.w(TAG, "Callback não reconhecido: $uri")
            }
        }
    }

    private fun handleReprintCallback(uri: Uri?) {
        try {
            val success = uri?.getQueryParameter("success") == "true"
            val transactionId = uri?.getQueryParameter("transaction_id")

            if (success && transactionId != null) {
                // Marcar como impresso no banco
                databaseHelper.markReceiptPrinted(transactionId)
                loadTransactions() // Recarregar lista

                Toast.makeText(this, "Comprovante reimpress com sucesso!", Toast.LENGTH_SHORT).show()
            } else {
                val error = uri?.getQueryParameter("error") ?: "Erro desconhecido"
                Toast.makeText(this, "Erro na reimpressão: $error", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao processar callback de reimpressão", e)
            Toast.makeText(this, "Erro ao processar retorno da reimpressão", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleCancelCallback(uri: Uri?) {
        try {
            val success = uri?.getQueryParameter("success") == "true"
            val transactionId = uri?.getQueryParameter("transaction_id")
            val cancellationId = uri?.getQueryParameter("cancellation_id")

            if (success && transactionId != null) {
                // Marcar como cancelado no banco
                val cancelId = cancellationId ?: "CANCEL_${System.currentTimeMillis()}"
                databaseHelper.cancelTransaction(transactionId, cancelId)

                loadTransactions() // Recarregar lista
                updateStats() // Atualizar estatísticas

                Toast.makeText(this, "Transação cancelada com sucesso!", Toast.LENGTH_SHORT).show()
            } else {
                val error = uri?.getQueryParameter("error") ?: "Erro desconhecido"
                Toast.makeText(this, "Erro no cancelamento: $error", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao processar callback de cancelamento", e)
            Toast.makeText(this, "Erro ao processar retorno do cancelamento", Toast.LENGTH_SHORT).show()
        }
    }
}