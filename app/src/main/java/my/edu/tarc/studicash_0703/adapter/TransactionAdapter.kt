package my.edu.tarc.studicash_0703.adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.studicash_0703.R
import my.edu.tarc.studicash_0703.Models.Transaction
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(private val context: Context, private val transactions: MutableList<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.transaction_item, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction)
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

        fun bind(transaction: Transaction) {
            titleTextView.text = transaction.title
            amountTextView.text = String.format("%.2f", transaction.amount) // Format amount to two decimal places
            dateTextView.text = formatDate(transaction.date) // Format date here
            categoryTextView.text = transaction.category
            paymentMethodTextView.text = transaction.paymentMethod

            // Set color based on transaction type
            val indicatorDrawable = if (transaction.isExpense) {
                ContextCompat.getDrawable(itemView.context, transaction.expenseColorRes)
            } else {
                ContextCompat.getDrawable(itemView.context, transaction.incomeColorRes)
            }
            indicatorView.background = indicatorDrawable
        }

        private fun formatDate(date: String): String {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parsedDate = inputFormat.parse(date)
            return outputFormat.format(parsedDate)
        }
    }
}
