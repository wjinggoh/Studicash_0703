package my.edu.tarc.studicash_0703.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.studicash_0703.Models.Transaction
import my.edu.tarc.studicash_0703.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private val context: Context,
    private var transactions: MutableList<Transaction>,
    private val listener: OnTransactionClickListener
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    interface OnTransactionClickListener {
        fun onDelete(transactionId: String, isExpense: Boolean)
        fun onEdit(transactionId: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.transaction_item, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction)

        holder.itemView.setOnClickListener {
            // Ensure you're passing the correct ID
            listener.onDelete(transaction.id, transaction.expense)
        }
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    fun updateData(newTransactions: List<Transaction>) {
        transactions.clear()
        transactions.addAll(newTransactions)
        notifyDataSetChanged()
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.transactionTitle)
        private val amountTextView: TextView = itemView.findViewById(R.id.transactionAmount)
        private val dateTextView: TextView = itemView.findViewById(R.id.transactionDate)
        private val categoryTextView: TextView = itemView.findViewById(R.id.transactionCategory)
        private val paymentMethodTextView: TextView = itemView.findViewById(R.id.transactionPaymentMethod)
        private val indicatorView: View = itemView.findViewById(R.id.indicatorView)

        init {
            itemView.findViewById<ImageView>(R.id.transactionDeleteBtn).setOnClickListener {
                val transactionId = transactions[adapterPosition].id
                listener.onDelete(transactionId, transactions[adapterPosition].expense) // Pass isExpense
            }

            itemView.findViewById<ImageView>(R.id.transactionEditBtn).setOnClickListener {
                val transactionId = transactions[adapterPosition].id
                listener.onEdit(transactionId)
            }
        }

        fun bind(transaction: Transaction) {
            titleTextView.text = transaction.title
            amountTextView.text = String.format("%.2f", transaction.amount)
            dateTextView.text = formatDate(transaction.date)
            categoryTextView.text = transaction.category
            paymentMethodTextView.text = transaction.paymentMethod ?: ""

            val indicatorDrawable = if (transaction.expense) {
                ContextCompat.getDrawable(itemView.context, transaction.expenseColorRes)
            } else {
                ContextCompat.getDrawable(itemView.context, transaction.incomeColorRes)
            }
            indicatorView.background = indicatorDrawable
        }

        private fun formatDate(date: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val parsedDate = inputFormat.parse(date)
                parsedDate?.let { outputFormat.format(it) } ?: "Invalid Date"
            } catch (e: ParseException) {
                "Invalid Date"
            }
        }
    }
}
