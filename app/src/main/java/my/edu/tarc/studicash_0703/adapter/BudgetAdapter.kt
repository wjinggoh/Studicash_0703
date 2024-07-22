package my.edu.tarc.studicash_0703.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.studicash_0703.BudgetItem
import my.edu.tarc.studicash_0703.Models.Budget
import my.edu.tarc.studicash_0703.R


class BudgetAdapter(private val budgets: List<BudgetItem>) : RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    inner class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val budgetName: TextView = itemView.findViewById(R.id.budgetName)
        val budgetAmount: TextView = itemView.findViewById(R.id.budgetAmount)
        val budgetSpent: TextView = itemView.findViewById(R.id.budgetSpent)
        val budgetProgressText: TextView = itemView.findViewById(R.id.budgetProgressText)
        val budgetProgress: ProgressBar = itemView.findViewById(R.id.budgetProgress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_budget, parent, false)
        return BudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val budget = budgets[position]

        holder.budgetName.text = budget.name
        holder.budgetAmount.text = "Amount: ${budget.amount}"
        holder.budgetSpent.text = "Spent: ${budget.spent}"

        // Ensure amounts are positive
        val maxAmount = budget.amount.toInt().coerceAtLeast(0)
        val spentAmount = budget.spent.toInt().coerceAtLeast(0)

        // Set ProgressBar max and progress values
        holder.budgetProgress.max = maxAmount
        holder.budgetProgress.progress = spentAmount

        // Display progress as text
        val progressPercentage = if (maxAmount > 0) {
            ((spentAmount.toDouble() / maxAmount) * 100).toInt()
        } else {
            0
        }
        holder.budgetProgressText.text = "Progress: $progressPercentage%"
    }

    override fun getItemCount() = budgets.size
}

