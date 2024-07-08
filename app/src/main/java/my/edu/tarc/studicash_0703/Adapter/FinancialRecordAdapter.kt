package my.edu.tarc.studicash_0703.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.studicash_0703.Models.FinancialRecord
import my.edu.tarc.studicash_0703.R

class FinancialRecordAdapter(private val context: Context, private val records: List<FinancialRecord>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_INCOME = 0
        private const val VIEW_TYPE_EXPENSE = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (records[position]) {
            is FinancialRecord.Income -> VIEW_TYPE_INCOME
            is FinancialRecord.Expense -> VIEW_TYPE_EXPENSE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        return when (viewType) {
            VIEW_TYPE_INCOME -> {
                val view = inflater.inflate(R.layout.income_item, parent, false)
                IncomeViewHolder(view)
            }
            VIEW_TYPE_EXPENSE -> {
                val view = inflater.inflate(R.layout.expense_item, parent, false)
                ExpenseViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val record = records[position]) {
            is FinancialRecord.Income -> (holder as IncomeViewHolder).bind(record)
            is FinancialRecord.Expense -> (holder as ExpenseViewHolder).bind(record)
        }
    }

    override fun getItemCount(): Int = records.size

    inner class IncomeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.incomeTitleTextView)
        private val amountTextView: TextView = itemView.findViewById(R.id.incomeAmountTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.incomeDateTextView)
        private val categoryTextView: TextView = itemView.findViewById(R.id.incomeCategoryTextView)
        private val paymentMethodTextView: TextView = itemView.findViewById(R.id.incomePaymentMethodTextView)

        fun bind(income: FinancialRecord.Income) {
            titleTextView.text = income.incomeTitle
            amountTextView.text = income.incomeAmount?.toString() ?: ""
            dateTextView.text = income.date
            categoryTextView.text = income.category
            paymentMethodTextView.text = income.paymentMethod
        }
    }

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.expensetitle)
        private val amountTextView: TextView = itemView.findViewById(R.id.yourexpenseAmt)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateView)
        private val categoryTextView: TextView = itemView.findViewById(R.id.categories_spinner)
        private val paymentMethodTextView: TextView = itemView.findViewById(R.id.paymentmethod_spinner)

        fun bind(expense: FinancialRecord.Expense) {
            titleTextView.text = expense.expenseTitle
            amountTextView.text = expense.expenseAmount?.toString() ?: ""
            dateTextView.text = expense.date
            categoryTextView.text = expense.category
            paymentMethodTextView.text = expense.paymentmethod
        }
    }
}
