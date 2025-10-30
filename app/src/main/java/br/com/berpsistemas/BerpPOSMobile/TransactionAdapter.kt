package br.com.berpsistemas.BerpPOSMobile

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.berpsistemas.BerpPOSMobile.model.TransactionModel
import br.com.berpsistemas.BerpPOSMobile.R
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter para a lista de transações no RecyclerView
 */
class TransactionAdapter(
    private val transactions: List<TransactionModel>,
    private val onActionClick: (TransactionModel, String) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTransactionId: TextView = view.findViewById(R.id.tvTransactionId)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvCardInfo: TextView = view.findViewById(R.id.tvCardInfo)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvTransactionType: TextView = view.findViewById(R.id.tvTransactionType)
        val tvAcquirer: TextView = view.findViewById(R.id.tvAcquirer)
        val btnReprintCustomer: Button = view.findViewById(R.id.btnReprintCustomer)
        val btnReprintEstablishment: Button = view.findViewById(R.id.btnReprintEstablishment)
        val btnCancel: Button = view.findViewById(R.id.btnCancel)
        val btnDetails: Button = view.findViewById(R.id.btnDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]

        // Dados básicos com verificações de segurança
        holder.tvTransactionId.text = "ID: ${transaction.transactionId}"
        holder.tvAmount.text = getFormattedAmount(transaction.amount)

        // Verificar se é transação de cartão antes de mostrar dados do cartão
        if (transaction.isTEF() && transaction.cardBrand.isNotEmpty()) {
            holder.tvCardInfo.text = "${transaction.cardBrand} • ${transaction.maskedPan}"
        } else if (transaction.isPIX()) {
            holder.tvCardInfo.text = "PIX"
        } else {
            holder.tvCardInfo.text = transaction.paymentTypeCode
        }

        holder.tvDate.text = getFormattedDate(transaction.transactionDate)
        holder.tvTransactionType.text = getFormattedPaymentType(transaction.paymentTypeCode)

        // Mostrar acquirer
        holder.tvAcquirer.text = transaction.acquirer

        // Status com cores e backgrounds dinâmicos
        setupStatusDisplay(holder, transaction)

        // Configurar botões
        setupActionButtons(holder, transaction)
    }

    private fun setupStatusDisplay(holder: TransactionViewHolder, transaction: TransactionModel) {
        when {
            // 1. CANCELADO tem prioridade máxima
            transaction.isCancelled -> {
                holder.tvStatus.text = "CANCELADO"
                holder.tvStatus.setTextColor(Color.parseColor("#D32F2F")) // Vermelho
                holder.tvStatus.setBackgroundColor(Color.parseColor("#757575"))
            }

            // 2. APROVADO (e não cancelado)
            transaction.isApproved() -> {
                holder.tvStatus.text = "APROVADO"
                holder.tvStatus.setTextColor(Color.parseColor("#FFFFFF")) // Verde
                holder.tvStatus.setBackgroundColor(Color.parseColor("#E8F5E8"))
            }

            // 3. NEGADO/REJEITADO
            transaction.isDenied() -> {
                holder.tvStatus.text = "NEGADO"
                holder.tvStatus.setTextColor(Color.parseColor("#D32F2F")) // Vermelho
                holder.tvStatus.setBackgroundColor(Color.parseColor("#FFEBEE"))
            }

            // 4. FALHA/ERRO
            transaction.isFailed() -> {
                holder.tvStatus.text = "FALHA"
                holder.tvStatus.setTextColor(Color.parseColor("#D32F2F")) // Vermelho
                holder.tvStatus.setBackgroundColor(Color.parseColor("#FFEBEE"))
            }

            // 5. PROCESSANDO
            transaction.isProcessing() -> {
                holder.tvStatus.text = "PROCESSANDO"
                holder.tvStatus.setTextColor(Color.parseColor("#1976D2")) // Azul
                holder.tvStatus.setBackgroundColor(Color.parseColor("#E3F2FD"))
            }

            // 6. PENDENTE
            transaction.isPending() -> {
                holder.tvStatus.text = "PENDENTE"
                holder.tvStatus.setTextColor(Color.parseColor("#F57C00")) // Laranja
                holder.tvStatus.setBackgroundColor(Color.parseColor("#FFF3E0"))
            }

            // 7. STATUS DESCONHECIDO - Fallback seguro
            else -> {
                val displayStatus = getHumanReadableStatus(transaction.status)
                holder.tvStatus.text = displayStatus
                holder.tvStatus.setTextColor(Color.parseColor("#757575")) // Cinza
                holder.tvStatus.setBackgroundColor(Color.parseColor("#F5F5F5"))
            }
        }

        // Adicionar padding para melhor aparência
        holder.tvStatus.setPadding(16, 8, 16, 8)
    }


    /**
     * Define background com fallback seguro
     */
    private fun setStatusBackground(textView: TextView, drawableRes: Int, fallbackColor: String) {
        try {
            textView.setBackgroundResource(drawableRes)
        } catch (e: Exception) {
            textView.setBackgroundColor(Color.parseColor(fallbackColor))
        }
    }

    /**
     * Converte status técnico para texto amigável
     */
    private fun getHumanReadableStatus(status: String): String {
        return when (status.uppercase().trim()) {
            "APPROVED" -> "APROVADO"
            "DENIED", "REJECTED" -> "NEGADO"
            "CANCELLED" -> "CANCELADO"
            "PENDING" -> "PENDENTE"
            "PROCESSING" -> "PROCESSANDO"
            "FAILED", "ERROR" -> "FALHA"
            "REFUNDED" -> "ESTORNADO"
            "PARTIAL_REFUND" -> "ESTORNO PARCIAL"
            "TIMEOUT" -> "TEMPO ESGOTADO"
            "UNKNOWN" -> "DESCONHECIDO"
            "" -> "PENDENTE" // Status vazio = pendente
            else -> {
                // Status desconhecido - mostrar apenas primeira palavra capitalizada
                status.split("_", " ").firstOrNull()?.lowercase()?.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase() else it.toString()
                } ?: "DESCONHECIDO"
            }
        }
    }

    private fun setupActionButtons(holder: TransactionViewHolder, transaction: TransactionModel) {
        // Botões de reimpressão - habilitados para transações aprovadas não canceladas
        val canReprint = true;// transaction.isApproved() && !transaction.isCancelled

       holder.btnReprintCustomer.isEnabled = canReprint
        holder.btnReprintEstablishment.isEnabled = canReprint

        if (canReprint) {
            holder.btnReprintCustomer.alpha = 1.0f
            holder.btnReprintEstablishment.alpha = 1.0f

            holder.btnReprintCustomer.setOnClickListener {
                onActionClick(transaction, "REPRINT_CUSTOMER")
            }

            holder.btnReprintEstablishment.setOnClickListener {
                onActionClick(transaction, "REPRINT_ESTABLISHMENT")
            }
        } else {
            holder.btnReprintCustomer.alpha = 0.5f
            holder.btnReprintEstablishment.alpha = 0.5f
            holder.btnReprintCustomer.setOnClickListener(null)
            holder.btnReprintEstablishment.setOnClickListener(null)
        }

        // Botão de cancelamento
        if (transaction.isCancelled) {
            holder.btnCancel.isEnabled = false
            holder.btnCancel.text = "Cancelado"
            holder.btnCancel.alpha = 0.5f
            holder.btnCancel.setOnClickListener(null)
        } else if (transaction.isApproved()) {
            holder.btnCancel.isEnabled = true
            holder.btnCancel.text = "Cancelar"
            holder.btnCancel.alpha = 1.0f
            holder.btnCancel.setOnClickListener {
                onActionClick(transaction, "CANCEL")
            }
        } else {
            holder.btnCancel.isEnabled = false
            holder.btnCancel.text = "N/A"
            holder.btnCancel.alpha = 0.5f
            holder.btnCancel.setOnClickListener(null)
        }

        // Botão de detalhes - sempre habilitado
        holder.btnDetails.setOnClickListener {
            onActionClick(transaction, "VIEW_DETAILS")
        }

        // Indicador visual para transações reimpressas
        if (transaction.receiptPrinted && transaction.reprintCount > 0) {
            holder.btnReprintCustomer.text = "Cliente (${transaction.reprintCount}x)"
            holder.btnReprintEstablishment.text = "Estab. (${transaction.reprintCount}x)"
        } else {
            holder.btnReprintCustomer.text = "Via Cliente"
            holder.btnReprintEstablishment.text = "Via Estabelecimento"
        }
    }

    // Métodos auxiliares para formatação
    private fun getFormattedAmount(amount: Double): String {
        return try {
            NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(amount)
        } catch (e: Exception) {
            "R$ %.2f".format(amount)
        }
    }

    private fun getFormattedDate(date: Date): String {
        return try {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date)
        } catch (e: Exception) {
            date.toString()
        }
    }

    private fun getFormattedPaymentType(paymentTypeCode: String): String {
        return when (paymentTypeCode.uppercase()) {
            "CRE" -> "Crédito"
            "DEB" -> "Débito"
            "PIX" -> "PIX"
            "VOU" -> "Voucher"
            "REF" -> "Estorno"
            else -> paymentTypeCode.ifEmpty { "Não informado" }
        }
    }

    override fun getItemCount() = transactions.size
}