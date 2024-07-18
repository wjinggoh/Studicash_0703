package my.edu.tarc.studicash_0703.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.studicash_0703.Models.Budget
import my.edu.tarc.studicash_0703.R

class BudgetAdapter(private val budgets: List<Budget>) : RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val budgetAmount: TextView = itemView.findViewById(R.id.budget_amount)
        private val budgetCategory: TextView = itemView.findViewById(R.id.budget_category)
        private val budgetStartDate: TextView = itemView.findViewById(R.id.budget_start_date)
        private val budgetEndDate: TextView = itemView.findViewById(R.id.budget_end_date)

        fun bind(budget: Budget) {
            budgetAmount.text = budget.amount.toString()
            budgetCategory.text = budget.category
            budgetStartDate.text = budget.startDate
            budgetEndDate.text = budget.endDate
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.budget_item, parent, false)
        return BudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        holder.bind(budgets[position])
    }

    override fun getItemCount(): Int {
        return budgets.size
    }
}
