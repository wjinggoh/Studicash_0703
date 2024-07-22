package my.edu.tarc.studicash_0703

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.studicash_0703.databinding.ItemBudgetBinding

data class BudgetItem(
    val name: String,
    val amount: Double,
    val spent: Double,
    val progress: Int
)


class BudgetAdapter(private val budgetItems: List<BudgetItem>) : RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_budget, parent, false)
        return BudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val budgetItem = budgetItems[position]

        // Set TextViews
        holder.budgetName.text = budgetItem.name
        holder.budgetAmount.text = "Amount: ${budgetItem.amount}"
        holder.budgetSpent.text = "Spent: ${budgetItem.spent}"

        // Set ProgressBar based on actual amount and spent
        holder.budgetProgress.max = budgetItem.amount.toInt() // Max value is total amount
        holder.budgetProgress.progress = budgetItem.spent.toInt() // Progress is spent amount

        // Calculate progress percentage for display
        val progressPercentage = if (budgetItem.amount > 0) {
            ((budgetItem.spent / budgetItem.amount) * 100).toInt()
        } else {
            0
        }

        // Optional: Update the TextView showing progress percentage
        holder.budgetProgressText.text = "${progressPercentage}% used"
    }

    override fun getItemCount(): Int {
        return budgetItems.size
    }

    class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val budgetName: TextView = itemView.findViewById(R.id.budgetName)
        val budgetAmount: TextView = itemView.findViewById(R.id.budgetAmount)
        val budgetSpent: TextView = itemView.findViewById(R.id.budgetSpent)
        val budgetProgress: ProgressBar = itemView.findViewById(R.id.budgetProgress)
        val budgetProgressText: TextView = itemView.findViewById(R.id.budgetProgressText)
    }
}
