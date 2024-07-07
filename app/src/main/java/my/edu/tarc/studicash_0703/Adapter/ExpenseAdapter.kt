package my.edu.tarc.studicash_0703.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.studicash_0703.Models.Expense
import my.edu.tarc.studicash_0703.R

class ExpenseAdapter(private val context: Context, private val expenses: List<Expense>) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.expense_item, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.bind(expense)
    }

    override fun getItemCount(): Int {
        return expenses.size
    }

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.expensetitle)
        private val amountTextView: TextView = itemView.findViewById(R.id.yourexpenseAmt)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateView)
        private val categoryTextView: TextView = itemView.findViewById(R.id.categories_spinner)
        private val paymentMethodTextView: TextView = itemView.findViewById(R.id.paymentmethod_spinner)

        fun bind(expense: Expense) {
            titleTextView.text = expense.expenseTitle
            amountTextView.text = expense.expenseAmount?.toString() ?: ""
            dateTextView.text = expense.date
            categoryTextView.text = expense.category
            paymentMethodTextView.text = expense.paymentmethod
        }
    }
}
